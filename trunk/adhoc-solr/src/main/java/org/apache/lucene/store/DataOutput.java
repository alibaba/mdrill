package org.apache.lucene.store;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.UnicodeUtil;

import com.alimama.mdrill.buffer.PForDelta;
import com.alimama.mdrill.buffer.RepeatCompress;
import com.alimama.mdrill.buffer.RepeatCompress.RepeatCompressRtn;

/**
 * Abstract base class for performing write operations of Lucene's low-level
 * data types.
 */
public abstract class DataOutput {

  /** Writes a single byte.
   * @see IndexInput#readByte()
   */
  public abstract void writeByte(byte b) throws IOException;

  /** Writes an array of bytes.
   * @param b the bytes to write
   * @param length the number of bytes to write
   * @see DataInput#readBytes(byte[],int,int)
   */
  public void writeBytes(byte[] b, int length) throws IOException {
    writeBytes(b, 0, length);
  }

  /** Writes an array of bytes.
   * @param b the bytes to write
   * @param offset the offset in the byte array
   * @param length the number of bytes to write
   * @see DataInput#readBytes(byte[],int,int)
   */
  public abstract void writeBytes(byte[] b, int offset, int length) throws IOException;

  /** Writes an int as four bytes.
   * @see DataInput#readInt()
   */
  public void writeInt(int i) throws IOException {
    writeByte((byte)(i >> 24));
    writeByte((byte)(i >> 16));
    writeByte((byte)(i >>  8));
    writeByte((byte) i);
  }
  
  
  private byte bytebuffer=0;
  private int bytepos=0;
  private static int BYTE_BITS=8; 
  private static int INT_BITS=32; 
  
  

  public void writeBits(int i,int bitslen) throws IOException
  {
	  int bytesLeave=bitslen;
	  int start=INT_BITS-bitslen;
	  while(bytesLeave>0)
	  {
		  int len=BYTE_BITS-this.bytepos;
		  if(len>=bytesLeave)
		  {
			  len=bytesLeave;
		  }
		  
		  int byteresult=this.copybitsToBytebuffer(i, start, len);
		  int bitsleave=BYTE_BITS-this.bytepos-len;
		  this.bytebuffer|=(byteresult<<bitsleave);

		  bytesLeave-=len;
		  start+=len;
		  this.bytepos+=len;
		  if(this.bytepos>=BYTE_BITS)
		  {
			  this.flushBits();
		  }  
	  }
  }
  


  
  public void startBits()
  {
	  this.bytepos=0;
	  this.bytebuffer=0;
  }
  

  
  private int copybitsToBytebuffer(int i,int start,int len)
  {
	  int cutLeft=start;
	  int cutRight=INT_BITS-cutLeft-len;
	  int copybits=(i<<cutLeft)>>>cutLeft;
	  return (copybits>>cutRight);
  }
  
  
  public void flushBits() throws IOException
  {
	  if(this.bytepos>0)
	  {
		  this.writeByte(this.bytebuffer);
		  this.bytebuffer=0;
	  }
	  this.bytepos=0;
  }
  


  
  
  public void writeInt(int i,int bytelen) throws IOException {
	  if(bytelen>3)
	    {
		  writeByte((byte)(i >> 24));
	    	writeByte((byte)(i >> 16));
	    	writeByte((byte)(i >>  8));

	    	writeByte((byte) i);
	    	return ;
	    }
	    if(bytelen>2)
	    {
	    	writeByte((byte)(i >> 16));
	    	writeByte((byte)(i >>  8));
	    	writeByte((byte) i);
	    	return ;
	    }
	    if(bytelen>1)
	    {
	    	writeByte((byte)(i >>  8));
	    	writeByte((byte) i);
	    	return ;
	    }
    	writeByte((byte) i);
	  }

  /** Writes an int in a variable-length format.  Writes between one and
   * five bytes.  Smaller values take fewer bytes.  Negative numbers are not
   * supported.
   * @see DataInput#readVInt()
   */
  public final void writeVInt(int i) throws IOException {
    while ((i & ~0x7F) != 0) {
      writeByte((byte)((i & 0x7f) | 0x80));
      i >>>= 7;
    }
    writeByte((byte)i);
  }
  
  public final static int BLOGK_SIZE_COMPRESS=10240;
  public final static int BLOGK_SIZE_USED_COMPRESS=32;

  protected final int[] buffer_compress=new int[BLOGK_SIZE_COMPRESS];
  private int uptopos_compress=0;
  private int uptopos_compress_record=0;
  private int allowblock_compress=0;
  public void resetBlockMode()
  {
      this.uptopos_compress=0;
      this.uptopos_compress_record=0;

  }
  
  
  public void setUsedBlock(int type)
  {
	  allowblock_compress=type;
  }
  public void writeCompressblock(int v,int i) throws IOException {
	 this.uptopos_compress_record+=i;
    buffer_compress[uptopos_compress++] = v;
    if (uptopos_compress == BLOGK_SIZE_COMPRESS) {
        flushCompressBlock();
    }
  }
  
  static volatile long printindex_compress=0;
  static volatile long printindex_compress_repeat=0;
  
  public static boolean compressEquals(int[] a, int[] a2,int a2len) {

	  if(a.length!=a2len)
	  {
		  return false;
	  }
      for (int i=0; i<a2len; i++)
      {
          if (a[i] != a2[i])
          {
              return false;
          }
      }

      return true;
  }
  

  
  public void flushCompressBlock() throws IOException {
	  if(this.uptopos_compress<=0)
	  {
		  return ;
	  }
	  if(this.allowblock_compress==0||this.uptopos_compress_record<BLOGK_SIZE_USED_COMPRESS)
	  {
		  for(int i=0;i<uptopos_compress;i++) {
		        this.writeVInt(buffer_compress[i]);
		  }
	      this.uptopos_compress=0;
		  return ;
	  }
      
	  int type=0;
	  RepeatCompressRtn repeat=RepeatCompress.compress(buffer_compress, uptopos_compress);
	  int[] repeatdecompress=RepeatCompress.decompress(repeat.bytes, repeat.index);
	  if(compressEquals(repeatdecompress, buffer_compress,uptopos_compress))
	  {
		  if(repeat.index<=uptopos_compress)
		  {
			  type=1;
		  }
	      int[] compressedBuffer =PForDelta.compressOneBlock(repeat.bytes, repeat.index);
	      int compresslen=compressedBuffer.length;
	      int[] decompress=PForDelta.decompressOneBlock(compressedBuffer, repeat.index);
	      if((compresslen*100/uptopos_compress)<50&&compressEquals(decompress, repeat.bytes,repeat.index))
	      {
	    	  if(type==0||(type==1&&repeat.index>(compresslen*4)))
	    	  {
				type=1<<1;
	    	  }
	      }
	      
	      if(type!=0)
	      {
		      if(type==1){
//		    	  if(printindex_compress_repeat++<1000)
//			      {
//			    	  System.out.println("##RepeatCompress##"+repeat.index+"@"+uptopos_compress);
//			      }
		          this.writeVInt((repeat.index<<2)+type);
			      for(int i=0;i<repeat.index;i++) {
			        this.writeVInt(repeat.bytes[i]);
			      }
		      }else{
//		    	   if(printindex_compress++<1000)
//		    	   {
//		    	    	  System.out.println("##PForDelta##"+compresslen+"@"+uptopos_compress);
//		    	   }
		    	      this.writeVInt((repeat.index<<2)+type);
			      this.writeVInt(compresslen);
			      for(int i=0;i<compresslen;i++) {
			        this.writeInt(compressedBuffer[i]);
			      }
		      }
	      }
		}

      if(type==0)
      {
          this.writeVInt((uptopos_compress<<2));
    	  for(int i=0;i<uptopos_compress;i++) {
  	        this.writeVInt(buffer_compress[i]);
  	      }
      } 
      
      this.uptopos_compress=0;
    }
  
  
  
    public void writeZipStream(ByteIndexInput read) throws IOException {
	this.writeByteZip(read.getbytes());
    }
    
    public static class ByteIndexInput extends IndexOutput
 {
	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	@Override
	public void writeByte(byte arg0) throws IOException {
	    byte[] write = { arg0 };
	    this.writeBytes(write, 0, write.length);
	}

	public byte[] getbytes() {
	    return bos.toByteArray();
	}

	@Override
	public void writeBytes(byte[] arg0, int arg1, int arg2)
	        throws IOException {
	    bos.write(arg0, arg1, arg2);
	}

	@Override
	public void flush() throws IOException {
	    bos.flush();
	}

	@Override
	public void close() throws IOException {
	    bos.close();
	}

	@Override
	public long getFilePointer() {
	    throw new RuntimeException("not allowed");
	}

	@Override
	public void seek(long pos) throws IOException {
	    throw new RuntimeException("not allowed");
	}

	@Override
	public long length() throws IOException {
	    return bos.size();
	}

    }

    private void writeByteZip(byte[] buf) throws IOException {
	byte[] writebuff = buf;
	if (buf.length > 256) {
	    byte[] b = null;
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    GZIPOutputStream gzip = new GZIPOutputStream(bos);
	    gzip.write(buf);
	    gzip.finish();
	    gzip.close();
	    b = bos.toByteArray();
	    bos.close();

	    if ( buf.length/b.length>2) {
		writebuff = b;
		this.writeVInt(-1);
//		System.out.println("compress:used:"+buf.length+","+b.length);
	    }
//	    else{
//		System.out.println("compress:unused:"+buf.length+","+b.length);
//	    }
	}
	this.writeVInt(writebuff.length);
	this.writeBytes(writebuff, 0, writebuff.length);
    }

  /** Writes a long as eight bytes.
   * @see DataInput#readLong()
   */
  public void writeLong(long i) throws IOException {
    writeInt((int) (i >> 32));
    writeInt((int) i);
  }

  /** Writes an long in a variable-length format.  Writes between one and nine
   * bytes.  Smaller values take fewer bytes.  Negative numbers are not
   * supported.
   * @see DataInput#readVLong()
   */
  public final void writeVLong(long i) throws IOException {
    while ((i & ~0x7F) != 0) {
      writeByte((byte)((i & 0x7f) | 0x80));
      i >>>= 7;
    }
    writeByte((byte)i);
  }
  
  public final void writeVVLong(long i) throws IOException
  {
	  this.writeVLong(VNumberic.enVLong(i));
  }
  
  public final void writeVVInt(int i) throws IOException
  {
      this.writeVInt(VNumberic.enVInt(i));
  }
  
  
    public final void writeVVVLong(long i) throws IOException {
	if (i < 0) {
	    this.writeVInt(1);
	    this.writeVLong(i * -1);
	} else {
	    this.writeVInt(0);
	    this.writeVLong(i);
	}
    }

    public final void writeVVVInt(int i) throws IOException {
	if (i < 0) {
	    this.writeVInt(1);
	    this.writeVInt(i * -1);
	} else {
	    this.writeVInt(0);
	    this.writeVInt(i);
	}
    }

  /** Writes a string.
   * @see DataInput#readString()
   */
  public void writeString(String s) throws IOException {
    final BytesRef utf8Result = new BytesRef(10);
    UnicodeUtil.UTF16toUTF8(s, 0, s.length(), utf8Result);
    writeVInt(utf8Result.length);
    writeBytes(utf8Result.bytes, 0, utf8Result.length);
  }

  private static int COPY_BUFFER_SIZE = 16384;
  private byte[] copyBuffer;

  /** Copy numBytes bytes from input to ourself. */
  public void copyBytes(DataInput input, long numBytes) throws IOException {
    assert numBytes >= 0: "numBytes=" + numBytes;
    long left = numBytes;
    if (copyBuffer == null)
      copyBuffer = new byte[COPY_BUFFER_SIZE];
    while(left > 0) {
      final int toCopy;
      if (left > COPY_BUFFER_SIZE)
        toCopy = COPY_BUFFER_SIZE;
      else
        toCopy = (int) left;
      input.readBytes(copyBuffer, 0, toCopy);
      writeBytes(copyBuffer, 0, toCopy);
      left -= toCopy;
    }
  }

  /** Writes a sub sequence of characters from s as the old
   *  format (modified UTF-8 encoded bytes).
   * @param s the source of the characters
   * @param start the first character in the sequence
   * @param length the number of characters in the sequence
   * @deprecated -- please pre-convert to utf8 bytes
   * instead or use {@link #writeString}
   */
  @Deprecated
  public void writeChars(String s, int start, int length)
       throws IOException {
    final int end = start + length;
    for (int i = start; i < end; i++) {
      final int code = s.charAt(i);
      if (code >= 0x01 && code <= 0x7F)
	writeByte((byte)code);
      else if (((code >= 0x80) && (code <= 0x7FF)) || code == 0) {
	writeByte((byte)(0xC0 | (code >> 6)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      } else {
	writeByte((byte)(0xE0 | (code >>> 12)));
	writeByte((byte)(0x80 | ((code >> 6) & 0x3F)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      }
    }
  }

  /** Writes a sub sequence of characters from char[] as
   *  the old format (modified UTF-8 encoded bytes).
   * @param s the source of the characters
   * @param start the first character in the sequence
   * @param length the number of characters in the sequence
   * @deprecated -- please pre-convert to utf8 bytes instead or use {@link #writeString}
   */
  @Deprecated
  public void writeChars(char[] s, int start, int length)
    throws IOException {
    final int end = start + length;
    for (int i = start; i < end; i++) {
      final int code = s[i];
      if (code >= 0x01 && code <= 0x7F)
	writeByte((byte)code);
      else if (((code >= 0x80) && (code <= 0x7FF)) || code == 0) {
	writeByte((byte)(0xC0 | (code >> 6)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      } else {
	writeByte((byte)(0xE0 | (code >>> 12)));
	writeByte((byte)(0x80 | ((code >> 6) & 0x3F)));
	writeByte((byte)(0x80 | (code & 0x3F)));
      }
    }
  }

  public void writeStringStringMap(Map<String,String> map) throws IOException {
    if (map == null) {
      writeInt(0);
    } else {
      writeInt(map.size());
      for(final Map.Entry<String, String> entry: map.entrySet()) {
        writeString(entry.getKey());
        writeString(entry.getValue());
      }
    }
  }
}
