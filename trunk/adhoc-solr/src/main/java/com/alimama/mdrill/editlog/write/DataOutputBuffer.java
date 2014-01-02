package com.alimama.mdrill.editlog.write;


import java.io.*;

public class DataOutputBuffer extends DataOutputStream {

  private static class Buffer extends ByteArrayOutputStream {
    public byte[] getData() { return buf; }
    public int getLength() { return count; }

    public Buffer() {
      super();
    }
    
    public Buffer(int size) {
      super(size);
    }
    
    public void write(DataInput in, int len) throws IOException {
      int newcount = count + len;
      if (newcount > buf.length) {
        byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
      }
      in.readFully(buf, count, len);
      count = newcount;
    }
  }

  private Buffer buffer;
  
  public DataOutputBuffer() {
    this(new Buffer());
  }
  
  public DataOutputBuffer(int size) {
    this(new Buffer(size));
  }
  
  private DataOutputBuffer(Buffer buffer) {
    super(buffer);
    this.buffer = buffer;
  }

  /** Returns the current contents of the buffer.
   *  Data is only valid to {@link #getLength()}.
   */
  public byte[] getData() { return buffer.getData(); }

  public int getLength() { return buffer.getLength(); }

  public DataOutputBuffer reset() {
    this.written = 0;
    buffer.reset();
    return this;
  }

  public void write(DataInput in, int length) throws IOException {
    buffer.write(in, length);
  }

  public void writeTo(OutputStream out) throws IOException {
    buffer.writeTo(out);
  }
}

