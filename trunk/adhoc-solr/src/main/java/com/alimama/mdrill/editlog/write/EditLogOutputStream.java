package com.alimama.mdrill.editlog.write;


import java.io.IOException;
import java.io.Closeable;

import com.alimama.mdrill.editlog.read.FSEditLogOp;


public abstract class EditLogOutputStream implements Closeable {
  private long numSync;        // number of sync(s) to disk
  private long totalTimeSync;  // total time to sync

  public EditLogOutputStream() throws IOException {
    numSync = totalTimeSync = 0;
  }

  abstract public void write(FSEditLogOp op) throws IOException;

  abstract public void writeRaw(byte[] bytes, int offset, int length)
      throws IOException;

  abstract public void create() throws IOException;

  @Override
  abstract public void close() throws IOException;

  abstract public void abort() throws IOException;
  
  abstract public void setReadyToFlush() throws IOException;

  abstract public void flushAndSync(boolean durable) throws IOException;

  public void flush() throws IOException {
    flush(true);
  }
  
  public void flush(boolean durable) throws IOException {
    numSync++;
    long start =System.currentTimeMillis();
    flushAndSync(durable);
    long end = System.currentTimeMillis();
    totalTimeSync += (end - start);
  }

 
  
 public long getTotalSyncTime() {
    return totalTimeSync;
  }

  public long getNumSync() {
    return numSync;
  }


}
