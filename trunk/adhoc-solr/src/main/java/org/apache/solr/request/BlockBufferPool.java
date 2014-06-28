package org.apache.solr.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.utils.UniqConfig;

public class BlockBufferPool<T> {
	private static Logger log = LoggerFactory.getLogger(BlockBufferPool.class);
	public static BlockBufferPool<Integer> INT_POOL=new BlockBufferPool<Integer>();
	public static BlockBufferPool<Short> SHORT_POOL=new BlockBufferPool<Short>();
	public static BlockBufferPool<Byte> BYTE_POOL=new BlockBufferPool<Byte>();
	public static BlockBufferPool<Long> LONG_POOL=new BlockBufferPool<Long>();
	public static BlockBufferPool<Double> DOUBLE_POOL=new BlockBufferPool<Double>();
	
	private final static int intervalBits = 16; 
	private final static int intervalMask = 0xffffffff >>> (32 - intervalBits);
	public final static int interval = 1 << intervalBits;// 1024*256
	
	private AtomicLong mallocTimes = new AtomicLong(0l);
	private AtomicLong reusedTimes = new AtomicLong(0l);

	private ArrayList<BlockInterface<T>> freeByteBlocks = new ArrayList<BlockInterface<T>>();

	public void recycleByteBlocks(BlockArray<T> data) {
		this.recycleByteBlocks(data,0);
		
	}
	
	public void recycleByteBlocks(BlockArray<T> data,int start) {
		synchronized (BlockBufferPool.this) {
			int allowsize = UniqConfig.getBlockBufferSize() - this.freeByteBlocks.size();
			final int size = data.data.length;
			for (int i = start; i < size; i++) {
				if (i < allowsize) {
					freeByteBlocks.add(data.data[i]);
				}
				data.data[i] = null;
			}
			data.data = null;
			data.size = 0;
		}
	}
	
	private BlockInterface<T> getByteBlock(CreateArr<T> c) {
      synchronized(BlockBufferPool.this) {
        final int size = freeByteBlocks.size();
        final  BlockInterface<T> b;
			if (0 == size) {
				b = c.create(interval);
				mallocTimes.incrementAndGet();
			} else {
				b = freeByteBlocks.remove(size - 1);
				reusedTimes.incrementAndGet();
			}
        return b;
      }
    }
	
	public BlockArray<T> calloc(int size, CreateArr<T> c, T init) {
		int block = size / interval + 1;
		BlockInterface<T>[] data = c.createBlocks(block);
		for (int i = 0; i < data.length; i++) {
			data[i]=getByteBlock(c);
			data[i].allset(init);
		}
		
		return new BlockArray<T>(data, size);
	}
	
	
	public BlockArray<T> reCalloc(BlockArray<T> old,int size, CreateArr<T> c, T init) {
		int block = size / interval + 1;
		BlockInterface<T>[] data = c.createBlocks(block);
		for (int i = 0; i < data.length; i++) {
			if(i<old.data.length)
			{
				data[i]=old.data[i];
			}else{
				data[i]=getByteBlock(c);
				data[i].allset(init);
			}
		}
		
		this.recycleByteBlocks(old,data.length);
		return new BlockArray<T>(data, size);
	}
	
	
	public void allset(BlockArray<T> last, CreateArr<T> c, T init) {
		for (int i = 0; i < last.data.length; i++) {
			last.data[i].allset(init);
		}
	}
	
	
	public void copy(BlockArray<T> newd, BlockArray<T> old) {
		for (int i = 0; i < newd.data.length && i < old.data.length; i++) {
			newd.data[i].copyFrom(old.data[i]);
		}
	}

	public static CreateArr<Byte> BYTE_CREATE = new CreateArr<Byte>() {
		@Override
		public BlockInterface<Byte> create(int size) {
			return new BlockByte(size);
		}

		@Override
		public BlockInterface<Byte>[] createBlocks(int size) {
			return new BlockByte[size];
		}
	};

	public static CreateArr<Integer> INT_CREATE = new CreateArr<Integer>() {

		@Override
		public BlockInterface<Integer> create(int size) {
			return new BlockInteger(size);
		}

		@Override
		public BlockInterface<Integer>[] createBlocks(int size) {
			return new BlockInteger[size];
		}

	};

	public static CreateArr<Short> SHORT_CREATE = new CreateArr<Short>() {

		@Override
		public BlockInterface<Short> create(int size) {
			return new BlockShort(size);
		}

		@Override
		public BlockInterface<Short>[] createBlocks(int size) {
			return new BlockShort[size];
		}

	};

	public static CreateArr<Long> LONG_CREATE = new CreateArr<Long>() {

		@Override
		public BlockInterface<Long> create(int size) {
			return new BlockLong(size);

		}

		@Override
		public BlockInterface<Long>[] createBlocks(int size) {
			return new BlockLong[size];

		}

	};

	public static CreateArr<Double> DOUBLE_CREATE = new CreateArr<Double>() {
		@Override
		public BlockInterface<Double> create(int size) {
			return new BlockDouble(size);
		}

		@Override
		public BlockInterface<Double>[] createBlocks(int size) {
			return new BlockDouble[size];
		}

	};

	public static interface CreateArr<T> {
		public BlockInterface<T> create(int size);

		public BlockInterface<T>[] createBlocks(int size);
	}

	
	
	public static class BlockInteger implements BlockInterface<Integer>
	{
		int[] data;
		public BlockInteger(int size)
		{
			data=new int[size];
		}

		@Override
		public Integer get(int i) {
			return data[i];
		}

		@Override
		public void set(int i, Integer v) {
			data[i]=v;
		}

		@Override
		public void allset(Integer v) {
			Arrays.fill(data, v);	
		}

		@Override
		public void copyFrom(BlockInterface<Integer> v) {
			BlockInteger old=(BlockInteger)v;
			System.arraycopy(old.data, 0, this.data, 0,old.data.length);
		}
	}
	
	public static class BlockShort implements BlockInterface<Short>
	{
		short[] data;
		public BlockShort(int size)
		{
			data=new short[size];
		}

		@Override
		public Short get(int i) {
			return data[i];
		}

		@Override
		public void set(int i, Short v) {
			data[i]=v;
		}

		@Override
		public void allset(Short v) {
			Arrays.fill(data, v);
		
		}
		
		@Override
		public void copyFrom(BlockInterface<Short> v) {
			BlockShort old=(BlockShort)v;
			System.arraycopy(old.data, 0, this.data, 0,old.data.length);
		}
	}
	
	public static class BlockLong implements BlockInterface<Long>
	{
		long[] data;
		public BlockLong(int size)
		{
			data=new long[size];
		}

		@Override
		public Long get(int i) {
			return data[i];
		}

		@Override
		public void set(int i, Long v) {
			data[i]=v;
		}

		@Override
		public void allset(Long v) {
			Arrays.fill(data, v);
		}
		
		@Override
		public void copyFrom(BlockInterface<Long> v) {
			BlockLong old=(BlockLong)v;
			System.arraycopy(old.data, 0, this.data, 0,old.data.length);
		}
	}
	
	
	public static class BlockDouble implements BlockInterface<Double>
	{
		double[] data;
		public BlockDouble(int size)
		{
			data=new double[size];
		}

		@Override
		public void copyFrom(BlockInterface<Double> v) {
			BlockDouble old=(BlockDouble)v;
			System.arraycopy(old.data, 0, this.data, 0,old.data.length);
		}
		
		@Override
		public Double get(int i) {
			return data[i];
		}

		@Override
		public void set(int i, Double v) {
			data[i]=v;
		}

		@Override
		public void allset(Double v) {
			Arrays.fill(data, v);
		}
	}
	
	public static class BlockByte implements BlockInterface<Byte>
	{
		public byte[] data;
		public BlockByte(int size)
		{
			data=new byte[size];
		}

		@Override
		public void copyFrom(BlockInterface<Byte> v) {
			BlockByte old=(BlockByte)v;
			System.arraycopy(old.data, 0, this.data, 0,old.data.length);
		}
		
		@Override
		public Byte get(int i) {
			return data[i];
		}

		@Override
		public void set(int i, Byte v) {
			data[i]=v;
		}

		@Override
		public void allset(Byte v) {
			Arrays.fill(data, v);
		}
	}
	
	
	public static interface BlockInterface<K>{
		public  K get(int i);
		public  void set(int i,K v);
		public  void allset(K v);
		public  void copyFrom( BlockInterface<K> v);

	}
	
	public static class BlockArray<K> {
		private int size = 0;
		private BlockInterface<K>[] data;
		public BlockArray(BlockInterface<K>[] data, int size) {
			this.data = data;
			this.size = size;
		}
		public K get(int i) {
			int block = i >>> intervalBits;
			int pos = i & intervalMask;
			return data[block].get(pos);
		}
		public void set(int i, K v) {
			int block = i >>> intervalBits;
			int pos = i & intervalMask;
			data[block].set(pos, v);
		}
		public int getSize() {
			return size;
		}
		
		public int getMemSize() {
			return Math.max(data.length*interval, size);
		}
	}

}
