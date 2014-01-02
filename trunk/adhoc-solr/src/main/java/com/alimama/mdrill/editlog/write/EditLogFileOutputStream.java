package com.alimama.mdrill.editlog.write;


import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.editlog.defined.HdfsConstants;
import com.alimama.mdrill.editlog.read.FSEditLogOp;
import com.alimama.mdrill.editlog.util.IOUtils;
import com.google.common.annotations.VisibleForTesting;


public class EditLogFileOutputStream extends EditLogOutputStream {
  private static Log LOG = LogFactory.getLog(EditLogFileOutputStream.class);
  public static final int MIN_PREALLOCATION_LENGTH = 1024 * 1024;

  private Path file;
  FileSystem fs;
  private FSDataOutputStream fp; // file stream for storing edit logs
  private EditsDoubleBuffer doubleBuf;
  
  private int size;

  public EditLogFileOutputStream(FileSystem fs,Path name, int size)
      throws IOException {
    super();
    this.fs=fs;
    this.file = name;
    this.size=size;
    this.create();
  }

  @Override
  public void write(FSEditLogOp op) throws IOException {
    doubleBuf.writeOp(op);
  }

  @Override
  public void writeRaw(byte[] bytes, int offset, int length) throws IOException {
    doubleBuf.writeRaw(bytes, offset, length);
  }

	@Override
	public void create() throws IOException {
		if (fp != null) {
			fp.close();
		}

		if (doubleBuf != null) {
			doubleBuf.close();
			doubleBuf = null;
		}

		doubleBuf = new EditsDoubleBuffer(size);
		if (this.fs.exists(this.file)) {
			fs.delete(this.file, true);
		}
		fp = this.fs.create(this.file, true);
		writeHeader(doubleBuf.getCurrentBuf());
		setReadyToFlush();
		flush();
	}

  @VisibleForTesting
  public static void writeHeader(DataOutputStream out) throws IOException {
    out.writeInt(HdfsConstants.LAYOUT_VERSION);
  }

  @Override
  public void close() throws IOException {
    if (fp == null) {
      throw new IOException("Trying to use aborted output stream");
    }

    try {
      if (doubleBuf != null) {
        doubleBuf.close();
        doubleBuf = null;
      }
      fp.close();
      fp = null;
    } finally {
      IOUtils.cleanup(LOG, fp);
      doubleBuf = null;
      fp = null;
    }
    fp = null;
  }
  
  @Override
  public void abort() throws IOException {
    if (fp == null) {
      return;
    }
    IOUtils.cleanup(LOG, fp);
    fp = null;
  }

  @Override
  public void setReadyToFlush() throws IOException {
    doubleBuf.setReadyToFlush();
  }


  @Override
  public void flushAndSync(boolean durable) throws IOException {
    if (fp == null) {
      throw new IOException("Trying to use aborted output stream");
    }
    if (doubleBuf.isFlushed()) {
      LOG.info("Nothing to flush");
      return;
    }
    doubleBuf.flushTo(fp);
    if (durable) {
    	fp.flush();
    	fp.sync();
    }
  }

  
  public boolean shouldForceSync() {
    return doubleBuf.shouldForceSync();
  }


  public Path getFile() {
    return file;
  }
  
  public FileSystem getFileSystem() {
		return fs;
	}
  
  @Override
  public String toString() {
    return "EditLogFileOutputStream(" + file + ")";
  }

  public boolean isOpen() {
    return fp != null;
  }
  

}

