package com.alimama.mdrill.editlog.read;


import java.io.Closeable;
import java.io.IOException;


public abstract class EditLogInputStream implements Closeable {
  private FSEditLogOp cachedOp = null; 

  public String getCurrentStreamName() {
    return getName();
  }

  public abstract String getName();
  
  public abstract long getFirstTxId();
  
  public abstract long getLastTxId();

  @Override
  public abstract void close() throws IOException;

  public FSEditLogOp readOp() throws IOException {
    FSEditLogOp ret;
    if (cachedOp != null) {
      ret = cachedOp;
      cachedOp = null;
      return ret;
    }
    return nextOp();
  }
  
  public void resync() {
    if (cachedOp != null) {
      return;
    }
    cachedOp = nextValidOp();
  }
  
  protected abstract FSEditLogOp nextOp() throws IOException;
  
  public FSEditLogOp nextValidOp() {
    try {
      return nextOp();
    } catch (Throwable e) {
      return null;
    }
  }
  
  public boolean skipUntil(long txid) throws IOException {
    while (true) {
      FSEditLogOp op = readOp();
      if (op == null) {
        return false;
      }
      if (op.getTransactionId() >= txid) {
        cachedOp = op;
        return true;
      }
    }
  }
  
  public abstract int getVersion() throws IOException;


  public abstract long length() throws IOException;
  
  public abstract boolean isInProgress();
  
  public abstract void setMaxOpSize(int maxOpSize);
}
