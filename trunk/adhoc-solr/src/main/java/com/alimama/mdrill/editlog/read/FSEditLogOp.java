package com.alimama.mdrill.editlog.read;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import com.alimama.mdrill.editlog.AddOp;
import com.alimama.mdrill.editlog.defined.FSEditLogOpCodes;
import com.alimama.mdrill.editlog.defined.HdfsConstants;
import com.alimama.mdrill.editlog.util.PureJavaCrc32;
import com.alimama.mdrill.editlog.write.DataOutputBuffer;
import com.google.common.base.Preconditions;


public abstract class FSEditLogOp {
  public final FSEditLogOpCodes opCode;
  public long txid;
  final public static class OpInstanceCache {
    private HashMap<FSEditLogOpCodes, FSEditLogOp> inst =  new HashMap<FSEditLogOpCodes, FSEditLogOp>();
    public OpInstanceCache() {
      inst.put(FSEditLogOpCodes.OP_ADD, new AddOp());
    }
    
    public FSEditLogOp get(FSEditLogOpCodes opcode) {
      return inst.get(opcode);
    }
  }

  protected FSEditLogOp(FSEditLogOpCodes opCode) {
    this.opCode = opCode;
    this.txid = HdfsConstants.INVALID_TXID;
  }

  public long getTransactionId() {
    Preconditions.checkState(txid != HdfsConstants.INVALID_TXID);
    return txid;
  }

  public String getTransactionIdStr() {
    return (txid == HdfsConstants.INVALID_TXID) ? "(none)" : "" + txid;
  }
  
  public boolean hasTransactionId() {
    return (txid != HdfsConstants.INVALID_TXID);
  }

  public void setTransactionId(long txid) {
    this.txid = txid;
  }
  


  public abstract void readFields(DataInputStream in, int logVersion)
      throws IOException;

  public abstract void writeFields(DataOutputStream out)
      throws IOException;

  public static class Writer {
    private final DataOutputBuffer buf;
    private final Checksum checksum;

    public Writer(DataOutputBuffer out) {
      this.buf = out;
      this.checksum = new PureJavaCrc32();
    }

    public void writeOp(FSEditLogOp op) throws IOException {
      int start = buf.getLength();
      buf.writeByte(op.opCode.getOpCode());
      buf.writeLong(op.txid);
      op.writeFields(buf);
      int end = buf.getLength();
      checksum.reset();
      checksum.update(buf.getData(), start, end-start);
      int sum = (int)checksum.getValue();
      buf.writeInt(sum);
    }
  }

 
  public static class Reader {
    private final DataInputStream in;
    private final int logVersion;
    private final Checksum checksum;
    private final OpInstanceCache cache;
    private int maxOpSize;

    public Reader(DataInputStream in,int logVersion,int maxOpSize) {
      this.logVersion = logVersion;
      this.checksum = new PureJavaCrc32();
      this.in = new DataInputStream(new CheckedInputStream(in, this.checksum));
      this.cache = new OpInstanceCache();
      this.maxOpSize = maxOpSize;
    }

    public void setMaxOpSize(int maxOpSize) {
      this.maxOpSize = maxOpSize;
    }

    public FSEditLogOp readOp(boolean skipBrokenEdits) throws IOException {

        try {
          return decodeOp();
        } 
        catch (IOException e) {
          if (!skipBrokenEdits) {
            throw e;
          }else{
        	  return null;
          }
        } catch (RuntimeException e) {
          if (!skipBrokenEdits) {
            throw e;
          }else{
        	  return null;
          }
        } catch (Throwable e) {
          if (!skipBrokenEdits) {
            throw new IOException("got unexpected exception " +
                e.getMessage(), e);
          }else{
        	  return null;
          }
        }
    }



    private FSEditLogOp decodeOp() throws IOException {
      in.mark(maxOpSize);

      checksum.reset();


      byte opCodeByte;
      try {
        opCodeByte = in.readByte();
      } catch (EOFException eof) {
        // EOF at an opcode boundary is expected.
        return null;
      }

      FSEditLogOpCodes opCode = FSEditLogOpCodes.fromByte(opCodeByte);
      if (opCode == FSEditLogOpCodes.OP_INVALID) {
          throw new IOException("Read invalid opcode " + opCode);
      }

      FSEditLogOp op = cache.get(opCode);
      if (op == null) {
        throw new IOException("Read invalid opcode " + opCode);
      }

      op.setTransactionId(in.readLong());
      op.readFields(in, logVersion);
      validateChecksum(in, checksum, op.txid);
      return op;
    }

    /**
     * Validate a transaction's checksum
     */
    private void validateChecksum(DataInputStream in,
                                  Checksum checksum,
                                  long txid)
        throws IOException {

        int calculatedChecksum = (int)checksum.getValue();
        int readChecksum = in.readInt(); // read in checksum
        if (readChecksum != calculatedChecksum) {
          throw new IOException(
              "Transaction is corrupt. Calculated checksum is " +
              calculatedChecksum + " but read checksum " + readChecksum+",txid="+txid);
        }
    }
  }

}

