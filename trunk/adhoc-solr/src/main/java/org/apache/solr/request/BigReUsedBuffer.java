package org.apache.solr.request;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigReUsedBuffer<T> {
	private static Logger log = LoggerFactory.getLogger(BigReUsedBuffer.class);
	private final static int intervalBits = 16; 
	private final static int intervalMask = 0xffffffff >>> (32 - intervalBits);
	public final static int interval = 1 << intervalBits;// 1024*256
	public static AtomicLong mallocTimes = new AtomicLong(0l);
	public static AtomicLong reusedTimes = new AtomicLong(0l);

	private LinkedBlockingQueue<BlockInterface<T>> free = new LinkedBlockingQueue<BlockInterface<T>>();

	public void free(BlockArray<T> data) {
			boolean allowadd=this.free.size() < 100;
			for (BlockInterface<T> d:data.data) {
				if (d != null) {
					if(allowadd)
					{
						this.free.add( d);
					}
				}
			}
	}
	
	public BlockArray<T> calloc(int size, CreateArr<T> c, T init) {
			int block = size / interval + 1;
			BlockInterface<T>[] data = c.createBlocks(block);
			int index = 0;
			while(true)
			{
				if (index >= block) {
					break;
				}
				BlockInterface<T> d=this.free.poll();
				if(d==null)
				{
					break;
				}
				reusedTimes.incrementAndGet();
				data[index] = d;
				index++;
			}
			

			for (int i = index; i < block; i++) {
				data[i] = c.create(interval);
				mallocTimes.incrementAndGet();
			}

			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < interval; j++) {
					data[i].set(j, init) ;
				}
			}
			
			log.info("####BigByteBuffer### calloc free:"+free.size()+",mallocTimes:"+mallocTimes.get()+",reusedTimes:"+reusedTimes.get());
			return new BlockArray<T>(data, size);
	}
	
	public static CreateArr<Byte> BYTE_CREATE=new CreateArr<Byte>() {
		@Override
		public BlockInterface<Byte> create(int size) {
			return new BlockByte(size);
		}

		@Override
		public BlockInterface<Byte>[] createBlocks(int size) {
			return new BlockByte[size];
		}
		};
		
		public static CreateArr<Integer> INT_CREATE=new CreateArr<Integer>() {

			@Override
			public BlockInterface<Integer> create(int size) {
				return new BlockInteger(size);
			}

			@Override
			public BlockInterface<Integer>[] createBlocks(int size) {
				return new BlockInteger[size];
			}
			
			
		};
		
		public static CreateArr<Short> SHORT_CREATE=new CreateArr<Short>() {

			@Override
			public BlockInterface<Short> create(int size) {
				return new BlockShort(size);
			}

			@Override
			public BlockInterface<Short>[] createBlocks(int size) {
				return new BlockShort[size];
			}
			
			
		};
		
		public static CreateArr<Long> LONG_CREATE=new CreateArr<Long>() {

			@Override
			public BlockInterface<Long> create(int size) {
				return new BlockLong(size);

			}

			@Override
			public BlockInterface<Long>[] createBlocks(int size) {
				return new BlockLong[size];

			}
		
		};
		
	public static CreateArr<Double> DOUBLE_CREATE=new CreateArr<Double>() {
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
//		long lasttime=System.currentTimeMillis();
//		public long getLasttime() {
//			return lasttime;
//		}
//
//		public void updateLasttime() {
//			this.lasttime = System.currentTimeMillis();
//		}
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
//		long lasttime=System.currentTimeMillis();
//		public long getLasttime() {
//			return lasttime;
//		}
//
//		public void updateLasttime() {
//			this.lasttime = System.currentTimeMillis();
//		}
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
//		long lasttime=System.currentTimeMillis();
//		public long getLasttime() {
//			return lasttime;
//		}
//
//		public void updateLasttime() {
//			this.lasttime = System.currentTimeMillis();
//		}
	}
	
	
	public static class BlockDouble implements BlockInterface<Double>
	{
		double[] data;
		public BlockDouble(int size)
		{
			data=new double[size];
		}

		@Override
		public Double get(int i) {
			return data[i];
		}

		@Override
		public void set(int i, Double v) {
			data[i]=v;
		}
//		long lasttime=System.currentTimeMillis();
//		public long getLasttime() {
//			return lasttime;
//		}
//
//		public void updateLasttime() {
//			this.lasttime = System.currentTimeMillis();
//		}
	}
	
	public static class BlockByte implements BlockInterface<Byte>
	{
		public byte[] data;
		public BlockByte(int size)
		{
			data=new byte[size];
		}

		@Override
		public Byte get(int i) {
			return data[i];
		}

		@Override
		public void set(int i, Byte v) {
			data[i]=v;
		}
//		long lasttime=System.currentTimeMillis();
//		public long getLasttime() {
//			return lasttime;
//		}
//
//		public void updateLasttime() {
//			this.lasttime = System.currentTimeMillis();
//		}
	}
	
	public static interface BlockInterface<K>{
//		public void updateLasttime() ;
//		public long getLasttime();
		public  K get(int i);
		public  void set(int i,K v);
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
