package com.alimama.mdrill.editlog.write;


import java.io.IOException;
import java.io.OutputStream;


import com.alimama.mdrill.editlog.defined.HdfsConstants;
import com.alimama.mdrill.editlog.read.FSEditLogOp;
import com.alimama.mdrill.editlog.read.FSEditLogOp.Writer;
import com.alimama.mdrill.editlog.util.IOUtils;
import com.google.common.base.Preconditions;

public class EditsDoubleBuffer {

  private TxnBuffer bufCurrent; // current buffer for writing
  private TxnBuffer bufReady; // buffer ready for flushing
  private final int initBufferSize;

  public EditsDoubleBuffer(int defaultBufferSize) {
    initBufferSize = defaultBufferSize;
    bufCurrent = new TxnBuffer(initBufferSize);
    bufReady = new TxnBuffer(initBufferSize);

  }
    
  public void writeOp(FSEditLogOp op) throws IOException {
    bufCurrent.writeOp(op);
  }

  public void writeRaw(byte[] bytes, int offset, int length) throws IOException {
    bufCurrent.write(bytes, offset, length);
  }
  
  public void close() throws IOException {
    Preconditions.checkNotNull(bufCurrent);
    Preconditions.checkNotNull(bufReady);

    int bufSize = bufCurrent.size();
    if (bufSize != 0) {
      throw new IOException("FSEditStream has " + bufSize
          + " bytes still to be flushed and cannot be closed.");
    }

    IOUtils.cleanup(null, bufCurrent, bufReady);
    bufCurrent = bufReady = null;
  }
  
  public void setReadyToFlush() {
    assert isFlushed() : "previous data not flushed yet";
    TxnBuffer tmp = bufReady;
    bufReady = bufCurrent;
    bufCurrent = tmp;
  }
  
  /**
   * Writes the content of the "ready" buffer to the given output stream,
   * and resets it. Does not swap any buffers.
   */
  public void flushTo(OutputStream out) throws IOException {
    bufReady.writeTo(out); // write data to file
    bufReady.reset(); // erase all data in the buffer
  }
  
  public boolean shouldForceSync() {
    return bufCurrent.size() >= initBufferSize;
  }

  public DataOutputBuffer getReadyBuf() {
    return bufReady;
  }
  
  public DataOutputBuffer getCurrentBuf() {
    return bufCurrent;
  }

  public boolean isFlushed() {
    return bufReady.size() == 0;
  }

  public int countBufferedBytes() {
    return bufReady.size() + bufCurrent.size();
  }

  /**
   * @return the transaction ID of the first transaction ready to be flushed 
   */
  public long getFirstReadyTxId() {
    assert bufReady.firstTxId > 0;
    return bufReady.firstTxId;
  }

  /**
   * @return the number of transactions that are ready to be flushed
   */
  public int countReadyTxns() {
    return bufReady.numTxns;
  }

  /**
   * @return the number of bytes that are ready to be flushed
   */
  public int countReadyBytes() {
    return bufReady.size();
  }
  
  private static class TxnBuffer extends DataOutputBuffer {
    long firstTxId;
    int numTxns;
    private Writer writer;
    
    public TxnBuffer(int initBufferSize) {
      super(initBufferSize);
      writer = new FSEditLogOp.Writer(this);
      reset();
    }

    public void writeOp(FSEditLogOp op) throws IOException {
      if (firstTxId == HdfsConstants.INVALID_TXID) {
        firstTxId = op.txid;
      } else {
        assert op.txid > firstTxId;
      }
      writer.writeOp(op);
      numTxns++;
    }
    
    @Override
    public DataOutputBuffer reset() {
      super.reset();
      firstTxId = HdfsConstants.INVALID_TXID;
      numTxns = 0;
      return this;
    }
  }

}
