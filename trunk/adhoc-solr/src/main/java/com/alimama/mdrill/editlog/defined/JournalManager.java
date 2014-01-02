package com.alimama.mdrill.editlog.defined;

import java.io.Closeable;
import java.io.IOException;

import com.alimama.mdrill.editlog.write.EditLogOutputStream;

public interface JournalManager extends Closeable, LogsPurgeable
{
  EditLogOutputStream startLogSegment(long txId) throws IOException;

  void finalizeLogSegment(long firstTxId, long lastTxId) throws IOException;

  void setOutputBufferCapacity(int size);

  void recoverUnfinalizedSegments() throws IOException;

  @Override
  void close() throws IOException;
  
  public static class CorruptionException extends IOException {
    static final long serialVersionUID = -4687802717006172702L;
    
    public CorruptionException(String reason) {
      super(reason);
    }
  }
}

