package com.alimama.mdrill.editlog.write;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import com.alimama.mdrill.editlog.defined.HdfsConstants;
import com.alimama.mdrill.editlog.defined.JournalManager;
import com.alimama.mdrill.editlog.defined.NNStorage;
import com.alimama.mdrill.editlog.defined.StorageDirectory;
import com.alimama.mdrill.editlog.read.EditLogFileInputStream;
import com.alimama.mdrill.editlog.read.EditLogInputStream;
import com.alimama.mdrill.editlog.read.FSEditLogValidate.EditLogValidation;
import com.alimama.mdrill.editlog.util.FileUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.ComparisonChain;


public class FileJournalManager implements JournalManager {
	private void purgeLog(EditLogFile log) {
		LOG.info("Purging old edit log " + log.getFile().toString());
		deleteOrWarn(log.getFs(), log.getFile());
	}

	private void deleteOrWarn(FileSystem fs, Path file) {
		try {
			if (!fs.delete(file, true)) {
				LOG.warn("Could not delete " + file);
			}
		} catch (IOException e) {
			LOG.warn("Could not delete " + file);
		}
	}

  public static String EDITS_FILE_NAME = "mlog";
  public static String EDITS_INPROGRESS_FILE_NAME = "mlog_inprocess";
  private static final Log LOG = LogFactory.getLog(FileJournalManager.class);

  private final StorageDirectory sd;
  private int outputBufferCapacity = 1024*1024;

  private static final Pattern EDITS_REGEX = Pattern.compile(EDITS_FILE_NAME + "_(\\d+)-(\\d+)");
  private static final Pattern EDITS_INPROGRESS_REGEX = Pattern.compile(EDITS_INPROGRESS_FILE_NAME+ "_(\\d+)");

  private Path currentInProgress = null;

  public FileJournalManager(StorageDirectory sd) {
    this.sd = sd;
  }

  @Override 
  public void close() throws IOException {
	  
  }
  


  @Override
  synchronized public EditLogOutputStream startLogSegment(long txid) 
      throws IOException {
    try {
      currentInProgress = NNStorage.getInProgressEditsFile(sd, txid);
      EditLogOutputStream stm = new EditLogFileOutputStream(sd.getFileSystem(), currentInProgress, outputBufferCapacity);
      return stm;
    } catch (IOException e) {
      LOG.warn("Unable to start log segment " + txid +
          " at " + currentInProgress + ": " +
          e.getLocalizedMessage());
      throw e;
    }
  }

  @Override
  synchronized public void finalizeLogSegment(long firstTxId, long lastTxId)
      throws IOException {
    Path inprogressFile = NNStorage.getInProgressEditsFile(sd, firstTxId);

    Path dstFile = NNStorage.getFinalizedEditsFile(sd, firstTxId, lastTxId);
    LOG.info("Finalizing edits file " + inprogressFile + " -> " + dstFile);
    
    Preconditions.checkState(!sd.getFileSystem().exists(dstFile), "Can't finalize edits file " + inprogressFile + " since finalized file " +  "already exists");
    if (!sd.getFileSystem().rename(inprogressFile, dstFile)) {
      throw new IllegalStateException("Unable to finalize edits file " + inprogressFile);
    }
    if (inprogressFile.equals(currentInProgress)) {
      currentInProgress = null;
    }
  }

  @VisibleForTesting
  public StorageDirectory getStorageDirectory() {
    return sd;
  }

  @Override
  synchronized public void setOutputBufferCapacity(int size) {
    this.outputBufferCapacity = size;
  }

  @Override
  public void purgeLogsOlderThan(long minTxIdToKeep)
      throws IOException {
    LOG.info("Purging logs older than " + minTxIdToKeep);
    FileStatus[] files = FileUtil.listFiles(sd.getFileSystem(),sd.getCurrentDir());
    List<EditLogFile> editLogs =  FileJournalManager.matchEditLogs(sd.getFileSystem(),files);
    for (EditLogFile log : editLogs) {
      if (!log.isInProgress()&&log.getFirstTxId() < minTxIdToKeep &&
          log.getLastTxId() < minTxIdToKeep) {
        purgeLog(log);
      }
    }
  }

  
  public static List<EditLogFile> matchEditLogs(FileSystem fs,Path logDir) throws IOException {
    return matchEditLogs(fs,FileUtil.listFiles(fs,logDir));
  }
  
  static List<EditLogFile> matchEditLogs(FileSystem fs,FileStatus[] filesInStorage) {
    List<EditLogFile> ret = Lists.newArrayList();
    for (FileStatus f : filesInStorage) {
      String name = f.getPath().getName();
      // Check for edits
      Matcher editsMatch = EDITS_REGEX.matcher(name);
      if (editsMatch.matches()) {
        try {
          long startTxId = Long.valueOf(editsMatch.group(1));
          long endTxId = Long.valueOf(editsMatch.group(2));
          ret.add(new EditLogFile(fs,f.getPath(), startTxId, endTxId));
        } catch (NumberFormatException nfe) {
          LOG.error("Edits file " + f + " has improperly formatted " +
                    "transaction ID");
          // skip
        }
      }
      
      // Check for in-progress edits
      Matcher inProgressEditsMatch = EDITS_INPROGRESS_REGEX.matcher(name);
      if (inProgressEditsMatch.matches()) {
        try {
          long startTxId = Long.valueOf(inProgressEditsMatch.group(1));
          ret.add(new EditLogFile(fs,f.getPath(), startTxId, HdfsConstants.INVALID_TXID, true));
        } catch (NumberFormatException nfe) {
          LOG.error("In-progress edits file " + f + " has improperly " +       "formatted transaction ID");
          // skip
        }
      }
    }
    return ret;
  }

  @Override
  synchronized public void selectInputStreams(
      Collection<EditLogInputStream> streams, long fromTxId,
      boolean inProgressOk, boolean forReading) throws IOException {
    List<EditLogFile> elfs = matchEditLogs(sd.getFileSystem(),sd.getCurrentDir());
    LOG.debug(this + ": selecting input streams starting at " + fromTxId + 
        (inProgressOk ? " (inProgress ok) " : " (excluding inProgress) ") +
        "from among " + elfs.size() + " candidate file(s)");
    addStreamsToCollectionFromFiles(elfs, streams, fromTxId, inProgressOk);
  }
  
  static void addStreamsToCollectionFromFiles(Collection<EditLogFile> elfs,
      Collection<EditLogInputStream> streams, long fromTxId, boolean inProgressOk) {
    for (EditLogFile elf : elfs) {
      if (elf.isInProgress()) {
        if (!inProgressOk) {
          LOG.debug("passing over " + elf + " because it is in progress " +
              "and we are ignoring in-progress logs.");
          continue;
        }
        try {
          elf.validateLog();
        } catch (IOException e) {
          LOG.error("got IOException while trying to validate header of " +
              elf + ".  Skipping.", e);
          continue;
        }
      }
      if (elf.lastTxId < fromTxId) {
        assert elf.lastTxId != HdfsConstants.INVALID_TXID;
        LOG.debug("passing over " + elf + " because it ends at " +
            elf.lastTxId + ", but we only care about transactions " +
            "as new as " + fromTxId);
        continue;
      }
      EditLogFileInputStream elfis = new EditLogFileInputStream(elf.getFs(),elf.getFile(),
            elf.getFirstTxId(), elf.getLastTxId(), elf.isInProgress());
      LOG.debug("selecting edit log stream " + elf);
      streams.add(elfis);
    }
  }

  @Override
  synchronized public void recoverUnfinalizedSegments() throws IOException {
    Path currentDir = sd.getCurrentDir();
    LOG.info("Recovering unfinalized segments in " + currentDir);
    List<EditLogFile> allLogFiles = matchEditLogs(sd.getFileSystem(),currentDir);

    for (EditLogFile elf : allLogFiles) {
      if (elf.getFile().equals(currentInProgress)) {
        continue;
      }
      if (elf.isInProgress()) {
    	  try{
	    	  if (elf.getLength() == 0) {
	          LOG.info("Deleting zero-length edit log file " + elf);
	          if (!elf.delete()) {
	            throw new IOException("Unable to delete file " + elf.getFile());
	          }
	          
	          continue;
	        }
    	  }catch(Throwable e){
    		  continue;
    	  }


        elf.validateLog();

        if (elf.hasCorruptHeader()) {
          elf.moveAsideCorruptFile();
          throw new CorruptionException("In-progress edit log file is corrupt: "
              + elf);
        }
        if (elf.getLastTxId() == HdfsConstants.INVALID_TXID) {
          LOG.info("Moving aside edit log file that seems to have zero " +
              "transactions " + elf);
          elf.moveAsideEmptyFile();
          continue;
        }
        finalizeLogSegment(elf.getFirstTxId(), elf.getLastTxId());
      }
    }
  }

  public List<EditLogFile> getLogFiles(long fromTxId) throws IOException {
    Path currentDir = sd.getCurrentDir();
    List<EditLogFile> allLogFiles = matchEditLogs(sd.getFileSystem(),currentDir);
    List<EditLogFile> logFiles = Lists.newArrayList();
    
    for (EditLogFile elf : allLogFiles) {
      if (fromTxId <= elf.getFirstTxId() ||
          elf.containsTxId(fromTxId)) {
        logFiles.add(elf);
      }
    }
    
    Collections.sort(logFiles, EditLogFile.COMPARE_BY_START_TXID);
    return logFiles;
  }
  


  @Override
  public String toString() {
    return String.format("FileJournalManager(root=%s)", sd.getCurrentDir());
  }

  public static class EditLogFile {
    private Path file;
    private FileSystem fs;
    private final long firstTxId;
    private long lastTxId;

    private boolean hasCorruptHeader = false;
    private final boolean isInProgress;

    final static Comparator<EditLogFile> COMPARE_BY_START_TXID 
      = new Comparator<EditLogFile>() {
      @Override
      public int compare(EditLogFile a, EditLogFile b) {
        return ComparisonChain.start()
        .compare(a.getFirstTxId(), b.getFirstTxId())
        .compare(a.getLastTxId(), b.getLastTxId())
        .result();
      }
    };

    EditLogFile(FileSystem fs,Path file,
        long firstTxId, long lastTxId) {
      this(fs,file, firstTxId, lastTxId, false);
      assert (lastTxId != HdfsConstants.INVALID_TXID)
        && (lastTxId >= firstTxId);
    }
    
    EditLogFile(FileSystem fs,Path file, long firstTxId, 
                long lastTxId, boolean isInProgress) { 
      assert (lastTxId == HdfsConstants.INVALID_TXID && isInProgress)
        || (lastTxId != HdfsConstants.INVALID_TXID && lastTxId >= firstTxId);
      assert (firstTxId > 0) || (firstTxId == HdfsConstants.INVALID_TXID);
      assert file != null;
      
      Preconditions.checkArgument(!isInProgress ||
          lastTxId == HdfsConstants.INVALID_TXID);
      
      this.firstTxId = firstTxId;
      this.lastTxId = lastTxId;
      this.file = file;
      this.fs=fs;
      this.isInProgress = isInProgress;
    }
    
    public long getFirstTxId() {
      return firstTxId;
    }
    
    public long getLastTxId() {
      return lastTxId;
    }
    
    boolean containsTxId(long txId) {
      return firstTxId <= txId && txId <= lastTxId;
    }

    public void validateLog() throws IOException {
      EditLogValidation val = EditLogFileInputStream.validateEditLog(fs,file);
      this.lastTxId = val.getEndTxId();
      this.hasCorruptHeader = val.hasCorruptHeader();
    }

    public boolean isInProgress() {
      return isInProgress;
    }

    public Path getFile() {
      return file;
    }
    
    public boolean delete() throws IOException
    {
    	return fs.delete(this.file,true);
    }
    public long getLength() throws IOException
    {
    	return this.fs.getFileStatus(this.file).getLen();
    }
    
    public FileSystem getFs() {
        return fs;
      }
    
    boolean hasCorruptHeader() {
      return hasCorruptHeader;
    }

    void moveAsideCorruptFile() throws IOException {
      assert hasCorruptHeader;
      renameSelf(".corrupt");
    }

    public void moveAsideEmptyFile() throws IOException {
      assert lastTxId == HdfsConstants.INVALID_TXID;
      renameSelf(".empty");
    }
      
    private void renameSelf(String newSuffix) throws IOException {
      Path src = file;
      Path dst = new Path(src.getParent(), src.getName() + newSuffix);
      boolean success = fs.rename(src,dst);
      if (!success) {
        throw new IOException(
          "Couldn't rename log " + src + " to " + dst);
      }
      file = dst;
    }

    @Override
    public String toString() {
      return String.format("EditLogFile(file=%s,first=%019d,last=%019d,"
                           +"inProgress=%b,hasCorruptHeader=%b)",
                           file.toString(), firstTxId, lastTxId,
                           isInProgress(), hasCorruptHeader);
    }
  }

}
