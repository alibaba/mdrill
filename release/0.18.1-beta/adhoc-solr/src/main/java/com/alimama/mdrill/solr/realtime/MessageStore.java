package com.alimama.mdrill.solr.realtime;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

public class MessageStore implements Closeable {
    private final static Logger log = Logger.getLogger(MessageStore.class);
    private static final String FILE_SUFFIX = ".binlog";

    
    static class Segment {
        final long start;
        final File file;
        FileMessageSet fileMessageSet;




        public Segment(final long start, final File file, final boolean mutable) {
            super();
            this.start = start;
            this.file = file;
            log.warn("create segment " + this.file.getAbsolutePath());
            try {
                final FileChannel channel = new RandomAccessFile(this.file, "rw").getChannel();
                this.fileMessageSet = new FileMessageSet(channel, 0, channel.size(), mutable);
            }
            catch (final IOException e) {
                log.error("create Segment", e);
            }
        }


        public long size() {
            return this.fileMessageSet.highWaterMark();
        }


        public boolean contains(final long offset) {
            if (this.size() == 0 && offset == this.start || this.size() > 0 && offset >= this.start
                    && offset <= this.start + this.size() - 1) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    static class SegmentList {
        AtomicReference<Segment[]> contents = new AtomicReference<Segment[]>();

        public SegmentList(final Segment[] s) {
            this.contents.set(s);
        }


        public SegmentList() {
            super();
            this.contents.set(new Segment[0]);
        }


        public void append(final Segment segment) {
            while (true) {
                final Segment[] curr = this.contents.get();
                final Segment[] update = new Segment[curr.length + 1];
                System.arraycopy(curr, 0, update, 0, curr.length);
                update[curr.length] = segment;
                if (this.contents.compareAndSet(curr, update)) {
                    return;
                }
            }
        }


        public void delete(final Segment segment) {
            while (true) {
                final Segment[] curr = this.contents.get();
                int index = -1;
                for (int i = 0; i < curr.length; i++) {
                    if (curr[i] == segment) {
                        index = i;
                        break;
                    }

                }
                if (index == -1) {
                    return;
                }
                final Segment[] update = new Segment[curr.length - 1];
                System.arraycopy(curr, 0, update, 0, index);
                if (index + 1 < curr.length) {
                    System.arraycopy(curr, index + 1, update, index, curr.length - index - 1);
                }
                if (this.contents.compareAndSet(curr, update)) {
                    return;
                }
            }
        }


        public Segment[] view() {
            return this.contents.get();
        }


        public Segment last() {
            final Segment[] copy = this.view();
            if (copy.length > 0) {
                return copy[copy.length - 1];
            }
            return null;
        }


        public Segment first() {
            final Segment[] copy = this.view();
            if (copy.length > 0) {
                return copy[0];
            }
            return null;
        }

    }

    private SegmentList segments;
    private final File partitionDir;
    private int maxSegmentsCount=4;
    
    public int getMaxSegmentsCount() {
        return maxSegmentsCount;
    }

    public void setMaxSegmentsCount(int maxSegmentsCount) {
        this.maxSegmentsCount = maxSegmentsCount;
    }

    public MessageStore(final String dataPath) throws IOException {
        this(dataPath, 0);
    }

    public MessageStore(final String dataPath, final long offsetIfCreate) throws IOException {
        this.partitionDir = new File(dataPath);
        this.checkDir(this.partitionDir);
        this.loadSegments(offsetIfCreate);
    }



    @Override
    public void close() throws IOException {
        for (final Segment segment : this.segments.view()) {
            segment.fileMessageSet.close();
        }
    }

    private void loadSegments(final long offsetIfCreate) throws IOException {
        final List<Segment> accum = new ArrayList<Segment>();
        final File[] ls = this.partitionDir.listFiles();

        if (ls != null) {
            for (final File file : ls) {
                if (file.isFile() && file.toString().endsWith(FILE_SUFFIX)) {
                    if (!file.canRead()) {
                        throw new IOException("Could not read file " + file);
                    }
                    final String filename = file.getName();
                    final long start = Long.parseLong(filename.substring(0, filename.length() - FILE_SUFFIX.length()));
                    accum.add(new Segment(start, file, false));
                }
            }
        }

        if (accum.size() == 0) {
            final File newFile = new File(this.partitionDir, this.nameFromOffset(offsetIfCreate));
            accum.add(new Segment(offsetIfCreate, newFile,true));
        }
        else {
            Collections.sort(accum, new Comparator<Segment>() {
                @Override
                public int compare(final Segment o1, final Segment o2) {
                    if (o1.start == o2.start) {
                        return 0;
                    }
                    else if (o1.start > o2.start) {
                        return 1;
                    }
                    else {
                        return -1;
                    }
                }
            });
//            this.validateSegments(accum);
            final Segment last = accum.remove(accum.size() - 1);
            last.fileMessageSet.close();
            log.info("Loading the last segment in mutable mode and running recover on " + last.file.getAbsolutePath());
            final Segment mutable = new Segment(last.start, last.file,true);
            accum.add(mutable);
            log.info("Loaded " + accum.size() + " segments...");
        }

        this.segments = new SegmentList(accum.toArray(new Segment[accum.size()]));
    }


//    private void validateSegments(final List<Segment> segments) {
//        this.writeLock.lock();
//        try {
//            for (int i = 0; i < segments.size() - 1; i++) {
//                final Segment curr = segments.get(i);
//                final Segment next = segments.get(i + 1);
//                if (curr.start + curr.size() != next.start) {
//                    throw new IllegalStateException("The following segments don't validate: "
//                            + curr.file.getAbsolutePath() + ", " + next.file.getAbsolutePath());
//                }
//            }
//        }
//        finally {
//            this.writeLock.unlock();
//        }
//    }


    private void checkDir(final File dir) {
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Create directory failed:" + dir.getAbsolutePath());
            }
        }
        if (!dir.isDirectory()) {
            throw new RuntimeException("Path is not a directory:" + dir.getAbsolutePath());
        }
    }

    private final Lock writeLock = new ReentrantLock();


    public long append(final IMessage req) throws IOException {
        return this.appendBuffer(req);
    }



    private long appendBuffer(final IMessage buffer) throws IOException {
  
            this.writeLock.lock();
            try {
                final Segment cur = this.segments.last();
                long offset = cur.start + cur.fileMessageSet.append(buffer);
                this.mayBeRoll();
                return offset;
            }
            catch (final IOException e) {
                log.error("Append file failed", e);
                throw e;
            }
            finally {
                this.writeLock.unlock();
            }
            
        
    }

    private long maxSegmentSize=1024*1024*64;

    public long getMaxSegmentSize() {
        return maxSegmentSize;
    }


    public void setMaxSegmentSize(long maxSegmentSize) {
        this.maxSegmentSize = maxSegmentSize;
    }


    private void mayBeRoll() throws IOException {
        if (this.segments.last().fileMessageSet.getSizeInBytes().get() >= this.maxSegmentSize) {
            this.roll();
        }
    }


    String nameFromOffset(final long offset) {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(20);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(false);
        return nf.format(offset) + FILE_SUFFIX;
    }


    private void roll() throws IOException {
        final long newOffset = this.nextAppendOffset();
        final File newFile = new File(this.partitionDir, this.nameFromOffset(newOffset));
        this.segments.last().fileMessageSet.flush();
        this.segments.last().fileMessageSet.setMutable(false);
        this.segments.append(new Segment(newOffset, newFile,true));
        
        while(this.segments.view().length>this.getMaxSegmentsCount())
        {
            this.segments.delete(this.segments.first());
        }
    }


    private long nextAppendOffset() throws IOException {
        final Segment last = this.segments.last();
        last.fileMessageSet.flush();
        return last.start + last.size();
    }


    public void flush() throws IOException {
        this.writeLock.lock();
        try {
            this.flush0();
        }
        finally {
            this.writeLock.unlock();
        }
    }


    private void flush0() throws IOException {
        this.segments.last().fileMessageSet.flush();
    }


    public long getMaxOffset() {
        final Segment last = this.segments.last();
        return last.start + last.size();
    }


    public long getMinOffset() {
        return this.segments.first().start;
    }
    
	public int read(final IMessage bf, long offset) throws IOException {
		Segment seg = this.findSegment(this.segments.view(), offset);
		if (seg == null) {
			return -1;
		}
		return seg.fileMessageSet.read(bf, offset - seg.start);
	}
    
    
    public long skipToNext(long offset) throws IOException{
        Segment seg=this.findSegment(this.segments.view(), offset);
        if(seg==null)
        {
        	return -1;
        }
        Segment rtn= this.findSegmentNext(this.segments.view(), seg);
        if(rtn==null)
        {
        	return -1;
        }
        
        return rtn.start;

    }

    public long getNearestOffset(final long offset) {
        return this.getNearestOffset(offset, this.segments);
    }


    long getNearestOffset(final long offset, final SegmentList segments) {
        try {
            final Segment segment = this.findSegment(segments.view(), offset);
            if (segment != null) {
                return segment.start;
            }
            else {
                final Segment last = segments.last();
                return last.start + last.size();
            }
        }
        catch (final ArrayIndexOutOfBoundsException e) {
            return segments.first().start;
        }
    }

    Segment findSegmentNext(final Segment[] segments,Segment seg) {
        if (segments == null || segments.length < 1) {
            return null;
        }
        long endseg=segments.length-1;
        for(int i=0;i<endseg;i++)
        {
        	if(segments[i].equals(seg))
        	{
        		return segments[i+1];
        	}
        }
        return null;
    }

    Segment findSegment(final Segment[] segments, final long offset) {
        if (segments == null || segments.length < 1) {
            return null;
        }
        final Segment last = segments[segments.length - 1];
        if (offset < segments[0].start) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (offset >= last.start + last.size()) {
            return null;
        }
        int low = 0;
        int high = segments.length - 1;
        while (low <= high) {
            final int mid = high + low >>> 1;
            final Segment found = segments[mid];
            if (found.contains(offset)) {
                return found;
            }
            else if (offset < found.start) {
                high = mid - 1;
            }
            else {
                low = mid + 1;
            }
        }
        return null;
    }
}
