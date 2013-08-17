package com.alimama.mdrill.solr.realtime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;


public class FileMessageSet  {
    private final FileChannel channel;
    private final AtomicLong messageCount;
    private final AtomicLong sizeInBytes;
    private final AtomicLong highWaterMark; 
    private final long offset; 
    private boolean mutable; 
    private final static Logger log = Logger.getLogger(FileMessageSet.class);


    
    public FileMessageSet(final FileChannel channel, final long offset, final long limit, final boolean mutable)
            throws IOException {
        super();
        this.channel = channel;
        this.offset = offset;
        this.messageCount = new AtomicLong(0);
        this.sizeInBytes = new AtomicLong(0);
        this.highWaterMark = new AtomicLong(0);
        this.mutable = mutable;
        if (mutable) {
            final long startMs = System.currentTimeMillis();
            final long truncated = this.recover();
            log.info("Recovery succeeded in " + (System.currentTimeMillis() - startMs) / 1000 + " seconds. " + truncated + " bytes truncated.");
        }
        else {
                this.sizeInBytes.set(Math.min(channel.size(), limit) - offset);
                this.highWaterMark.set(this.sizeInBytes.get());
        }
    }


    public AtomicLong getSizeInBytes() {
        return sizeInBytes;
    }



    public void setMutable(final boolean mutable) {
        this.mutable = mutable;
    }

    public long getMessageCount() {
        return this.messageCount.get();
    }


    public long highWaterMark() {
        return this.highWaterMark.get();
    }

    final static int HEADSIZE=Integer.SIZE/8;

    
    public long append(final IMessage msg) throws IOException {
        synchronized (this) {
            int size = msg.getData().length;
            ByteBuffer bufsize = ByteBuffer.allocate(HEADSIZE);
            bufsize.putInt(size);

            ByteBuffer buf = ByteBuffer.allocate(size);
            buf.put(msg.getData());
            bufsize.rewind();
            this.append(bufsize);
            buf.rewind();
            return this.append(buf);
        }

    }
    
    
    private long append(final ByteBuffer buf) throws IOException {
        if (!this.mutable) {
            throw new UnsupportedOperationException("Immutable message set");
        }
        final long offset = this.sizeInBytes.get();
        int sizeInBytes = 0;
        while (buf.hasRemaining()) {
            sizeInBytes += this.channel.write(buf);
        }
        this.sizeInBytes.addAndGet(sizeInBytes);
        this.messageCount.incrementAndGet();
        return offset;
    }


    
    public void flush() throws IOException {
        this.channel.force(true);
        this.highWaterMark.set(this.sizeInBytes.get());
    }


    FileChannel getFileChannel() {
        return this.channel;
    }


    
    public int read(final IMessage bf, long offset) throws IOException{
        ByteBuffer bufsize=ByteBuffer.allocate(HEADSIZE);
        this.read(bufsize,offset);
        bufsize.rewind();
        int size=bufsize.getInt();
        ByteBuffer buf=ByteBuffer.allocate(size);
        this.read(buf,offset+HEADSIZE);
        buf.rewind();
        bf.setData(buf.array());
        return size+HEADSIZE;
    }
    
    private int readMsgSize(long offset) throws IOException{
        ByteBuffer bufsize=ByteBuffer.allocate(HEADSIZE);
        this.read(bufsize,offset);
        bufsize.rewind();
        int size=bufsize.getInt();
        return size+HEADSIZE;
    }

    public int read(final IMessage bf) throws IOException{
       return this.read(bf,this.offset);
    }

    
    private void read(final ByteBuffer bf, final long offset) throws IOException {
        int size = 0;
        while (bf.hasRemaining()) {
            final int l = this.channel.read(bf, offset + size);
            if (l < 0) {
                break;
            }
            size += l;
        }
    }

    
    public void close() throws IOException {
        if (!this.channel.isOpen()) {
            return;
        }
        if (this.mutable) {
            this.flush();
        }
        this.channel.close();
    }



    private long recover() throws IOException {

        final long len = this.channel.size();
        long next = 0L;
        long msgCount = 0;
        while (next < len) {
            next+= this.readMsgSize(next);
            msgCount++;
        } ;
        this.channel.truncate(next);
        this.sizeInBytes.set(next);
        this.highWaterMark.set(next);
        this.messageCount.set(msgCount);
        this.channel.position(next);
        return len - next;
    }

}
