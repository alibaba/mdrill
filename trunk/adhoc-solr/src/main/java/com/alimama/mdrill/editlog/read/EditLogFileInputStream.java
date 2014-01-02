package com.alimama.mdrill.editlog.read;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


import com.alimama.mdrill.editlog.defined.DFSConfigKeys;
import com.alimama.mdrill.editlog.defined.HdfsConstants;
import com.alimama.mdrill.editlog.util.IOUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;


public class EditLogFileInputStream extends EditLogInputStream {
  private final LogSource log;
  private final long firstTxId;
  private final long lastTxId;
  private final boolean isInProgress;
  private int maxOpSize;
  static private enum State {
    UNINIT,
    OPEN,
    CLOSED
  }
  private State state = State.UNINIT;
  private InputStream fStream = null;
  private int logVersion = 0;
  private FSEditLogOp.Reader reader = null;
//  private FSEditLogLoader.PositionTrackingInputStream tracker = null;
  private DataInputStream dataIn = null;
  static final Log LOG = LogFactory.getLog(EditLogInputStream.class);
  
  EditLogFileInputStream(FileSystem fs,Path name)
      throws LogHeaderCorruptException, IOException {
    this(fs,name, HdfsConstants.INVALID_TXID, HdfsConstants.INVALID_TXID, false);
  }
  
  

  public EditLogFileInputStream(FileSystem fs,Path name,long firstTxId, long lastTxId,
      boolean isInProgress) {
    this(new FsFileLog(name,fs), firstTxId, lastTxId, isInProgress);
  }
  
  private EditLogFileInputStream(LogSource log,
      long firstTxId, long lastTxId,
      boolean isInProgress) {
      
    this.log = log;
    this.firstTxId = firstTxId;
    this.lastTxId = lastTxId;
    this.isInProgress = isInProgress;
    this.maxOpSize = DFSConfigKeys.DFS_NAMENODE_MAX_OP_SIZE_DEFAULT;
  }

  private void init() throws LogHeaderCorruptException, IOException {
    Preconditions.checkState(state == State.UNINIT);
    BufferedInputStream bin = null;
    try {
      fStream = log.getInputStream();
      bin = new BufferedInputStream(fStream);
      dataIn = new DataInputStream(bin);
      try {
        logVersion = readLogVersion(dataIn);
      } catch (EOFException eofe) {
        throw new LogHeaderCorruptException("No header found in log");
      }
      reader = new FSEditLogOp.Reader(dataIn, logVersion,this.maxOpSize);
      state = State.OPEN;
    } finally {
      if (reader == null) {
        IOUtils.cleanup(LOG, dataIn, bin, fStream);
        state = State.CLOSED;
      }
    }
  }

  @Override
  public long getFirstTxId() {
    return firstTxId;
  }
  
  @Override
  public long getLastTxId() {
    return lastTxId;
  }

  @Override
  public String getName() {
    return log.getName();
  }

  private FSEditLogOp nextOpImpl(boolean skipBrokenEdits) throws IOException {
    FSEditLogOp op = null;
    switch (state) {
    case UNINIT:
      try {
        init();
      } catch (Throwable e) {
        LOG.error("caught exception initializing " + this, e);
        if (skipBrokenEdits) {
          return null;
        }
        Throwables.propagateIfPossible(e, IOException.class);
      }
      Preconditions.checkState(state != State.UNINIT);
      return nextOpImpl(skipBrokenEdits);
    case OPEN:
      op = reader.readOp(skipBrokenEdits);
      if ((op != null) && (op.hasTransactionId())) {
        long txId = op.getTransactionId();
        if ((txId > lastTxId) && (lastTxId != HdfsConstants.INVALID_TXID)) {
        	return null;
        }
      }
      break;
      case CLOSED:
        break; // return null
    }
    return op;
  }

  @Override
  protected FSEditLogOp nextOp() throws IOException {
    return nextOpImpl(false);
  }

  @Override
  public FSEditLogOp nextValidOp() {
    try {
      return nextOpImpl(true);
    } catch (Throwable e) {
      LOG.error("nextValidOp: got exception while reading " + this, e);
      return null;
    }
  }

  @Override
  public int getVersion() throws IOException {
    if (state == State.UNINIT) {
      init();
    }
    return logVersion;
  }


  @Override
  public void close() throws IOException {
    if (state == State.OPEN) {
      dataIn.close();
    }
    state = State.CLOSED;
  }

  @Override
  public long length() throws IOException {
    return log.length();
  }
  
  @Override
  public boolean isInProgress() {
    return isInProgress;
  }
  
  @Override
public String toString() {
	return "["+log.toString()+"]";
}

 public static FSEditLogValidate.EditLogValidation validateEditLog(FileSystem fs,Path file) throws IOException {
    EditLogFileInputStream in;
    try {
      in = new EditLogFileInputStream(fs,file);
      in.getVersion(); // causes us to read the header
    } catch (LogHeaderCorruptException e) {
      // If the header is malformed or the wrong value, this indicates a corruption
      LOG.warn("Log file " + file + " has no valid header", e);
      return new FSEditLogValidate.EditLogValidation(
          HdfsConstants.INVALID_TXID, true);
    }
    
    try {
      return FSEditLogValidate.validateEditLog(in);
    } finally {
      IOUtils.closeStream(in);
    }
  }

  /**
   * Read the header of fsedit log
   * @param in fsedit stream
   * @return the edit log version number
   * @throws IOException if error occurs
   */
  static int readLogVersion(DataInputStream in)
      throws IOException, LogHeaderCorruptException {
    int logVersion;
    try {
      logVersion = in.readInt();
    } catch (EOFException eofe) {
      throw new LogHeaderCorruptException(
          "Reached EOF when reading log header");
    }
    return logVersion;
  }
  
  /**
   * Exception indicating that the header of an edits log file is
   * corrupted. This can be because the header is not present,
   * or because the header data is invalid (eg claims to be
   * over a newer version than the running NameNode)
   */
  static class LogHeaderCorruptException extends IOException {
    private static final long serialVersionUID = 1L;

    private LogHeaderCorruptException(String msg) {
      super(msg);
    }
  }
  
  public interface LogSource {
    public InputStream getInputStream() throws IOException;
    public long length();
    public String getName();
  }
  
  public static class FsFileLog  implements LogSource
  {
	  private Path file;
		@Override
	public String toString() {
		return "FsFileLog [file=" + file + "]";
	}


		private FileSystem fs;

	    public FsFileLog(Path file, FileSystem fs) {
		super();
		this.file = file;
		this.fs = fs;
	}
	    

	    @Override
	    public InputStream getInputStream() throws IOException {
	      return fs.open(file);
	    }


		@Override
		public long length() {
			try {
				return fs.getFileStatus(file).getLen();
			} catch (IOException e) {
				return 0;
			}
		}


		@Override
		public String getName() {
			return file.toString();
		}
  }
  


  @Override
  public void setMaxOpSize(int maxOpSize) {
    this.maxOpSize = maxOpSize;
    if (reader != null) {
      reader.setMaxOpSize(maxOpSize);
    }
  }
}