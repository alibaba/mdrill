package org.apache.solr.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.request.uninverted.RamTermNumValue;
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
	
	private final static int intervalBits = 12; 
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
			int allowsize = UniqConfig.getBlockBufferPoolSize() - this.freeByteBlocks.size();
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
				b = c.create();
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
	
	

	public static CreateArr<Byte> BYTE_CREATE = new CreateArr<Byte>() {
		@Override
		public BlockInterface<Byte> create() {
			return new BlockByte();
		}

		@Override
		public BlockInterface<Byte>[] createBlocks(int size) {
			return new BlockByte[size];
		}
	};

	public static CreateArr<Integer> INT_CREATE = new CreateArr<Integer>() {

		@Override
		public BlockInterface<Integer> create() {
			return new BlockInteger();
		}

		@Override
		public BlockInterface<Integer>[] createBlocks(int size) {
			return new BlockInteger[size];
		}

	};

	public static CreateArr<Short> SHORT_CREATE = new CreateArr<Short>() {

		@Override
		public BlockInterface<Short> create() {
			return new BlockShort();
		}

		@Override
		public BlockInterface<Short>[] createBlocks(int size) {
			return new BlockShort[size];
		}

	};

	public static CreateArr<Long> LONG_CREATE = new CreateArr<Long>() {

		@Override
		public BlockInterface<Long> create() {
			return new BlockLong();

		}

		@Override
		public BlockInterface<Long>[] createBlocks(int size) {
			return new BlockLong[size];

		}

	};

	public static CreateArr<Double> DOUBLE_CREATE = new CreateArr<Double>() {
		@Override
		public BlockInterface<Double> create() {
			return new BlockDouble();
		}

		@Override
		public BlockInterface<Double>[] createBlocks(int size) {
			return new BlockDouble[size];
		}

	};

	public static interface CreateArr<T> {
		public BlockInterface<T> create();

		public BlockInterface<T>[] createBlocks(int size);
	}

	private static class QuickArrayFillInt
	{
		private static int[] data_1=make(-1);

		private static int[] make(int def)
		{
			int[] data=new int[BlockBufferPool.interval];
			Arrays.fill(data, def);	
			return data;
		}
		
		public static void fill(int[] data,int v)
		{
			if(v==-1)
			{
				System.arraycopy(data_1, 0, data, 0,data_1.length);
			}else{
				Arrays.fill(data, v);	
			}
		}
	}
	
	
	private static class QuickArrayFillShort
	{
		private static short cmp=(short)-1;

		private static short[] data_1=make(cmp);

		private static short[] make(short def)
		{
			short[] data=new short[BlockBufferPool.interval];
			Arrays.fill(data, def);	
			return data;
		}
		
		public static void fill(short[] data,short v)
		{
			if(v==cmp)
			{
				System.arraycopy(data_1, 0, data, 0,data_1.length);
			}else{
				Arrays.fill(data, v);	
			}
		}
	}
	
	
	private static class QuickArrayFillByte
	{
		private static byte cmp=(byte)-1;

		private static byte[] data_1=make((byte)-1);

		private static byte[] make(byte def)
		{
			byte[] data=new byte[BlockBufferPool.interval];
			Arrays.fill(data, def);	
			return data;
		}
		
		public static void fill(byte[] data,byte v)
		{
			if(v==cmp)
			{
				System.arraycopy(data_1, 0, data, 0,data_1.length);
			}else{
				Arrays.fill(data, v);	
			}
		}
	}
	
	private static class QuickArrayFillLong
	{

		private static long[] data_1=make(-1l);
		private static long[] data_2=make(RamTermNumValue.TERMNUM_NAN_VALUE);


		private static long[] make(long def)
		{
			long[] data=new long[BlockBufferPool.interval];
			Arrays.fill(data, def);	
			return data;
		}
		
		public static void fill(long[] data,long v)
		{
			if(v==-1l)
			{
				System.arraycopy(data_1, 0, data, 0,data_1.length);
			}else if(v==RamTermNumValue.TERMNUM_NAN_VALUE)
			{
				System.arraycopy(data_2, 0, data, 0,data_2.length);
			}else{
				Arrays.fill(data, v);	
			}
		}
	
	}
	
	private static class QuickArrayFillDouble
	{

		private static double[] data_1=make(-1d);
		private static double dbl=(double)RamTermNumValue.TERMNUM_NAN_VALUE;
		private static double[] data_2=make(dbl);


		private static double[] make(double def)
		{
			double[] data=new double[BlockBufferPool.interval];
			Arrays.fill(data, def);	
			return data;
		}
		
		public static void fill(double[] data,double v)
		{
			if(v==-1l)
			{
				System.arraycopy(data_1, 0, data, 0,data_1.length);
			}else if(v==dbl)
			{
				System.arraycopy(data_2, 0, data, 0,data_2.length);
			}else{
				Arrays.fill(data, v);	
			}
		}
	
	}
	
	
	public static class BlockInteger implements BlockInterface<Integer>
	{
		private boolean hasInit=false;
		private int def=0;
		private int[] data=null;
		
		@Override
		public Integer get(int i) {
			if(hasInit)
			{
				return data[i];
			}
			return def;
		}

		@Override
		public void set(int i, Integer v) {
			this.maybeinit();
			data[i]=v;
		}

		@Override
		public void allset(Integer v) {
			this.def=v;
			hasInit=false;
		}
		
		private void maybeinit()
		{
			if(hasInit)
			{
				return ;
			}
			if(this.data==null)
			{
				data=new int[BlockBufferPool.interval];
			}
			
			QuickArrayFillInt.fill(data, def);	
			hasInit=true;
		}

		@Override
		public void replace(Integer a, Integer b) {
			if(this.hasInit)
			{
				for(int i=0;i<this.data.length;i++)
				{
					if(this.data[i]==a)
					{
						this.data[i]=b;
					}
				}
			}else if(this.def==a){
				this.allset(b);
			}
					
		}
		
		@Override
		public void fillByInt(BlockInteger d,int skip){
			if(d.hasInit)
			{
				if(this.hasInit)
				{
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=d.data[i];
						}
					}
				}else{
					data=new int[BlockBufferPool.interval];
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=d.data[i];
						}else{
							data[i]=this.def;
						}
					}
					hasInit=true;
				}
			}else if(d.def!=skip){
				this.allset(d.def);
			}
		}

	}
	
	public static class BlockShort implements BlockInterface<Short>
	{
		private boolean hasInit=false;
		private short def=0;
		
		short[] data=null;

		@Override
		public Short get(int i) {
			if(hasInit)
			{
				return data[i];
			}
			return def;
		}
		
		@Override
		public void fillByInt(BlockInteger d,int skip){

			if(d.hasInit)
			{
				if(this.hasInit)
				{
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(short)d.data[i];
						}
					}
				}else{
					data=new short[BlockBufferPool.interval];
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(short)d.data[i];
						}else{
							data[i]=this.def;
						}
					}
					hasInit=true;
				}
			}else if(d.def!=skip){
				this.allset((short)d.def);
			}
		
		
		}

		@Override
		public void set(int i, Short v) {
			this.maybeinit();
			data[i]=v;
		}

		@Override
		public void allset(Short v) {
			this.def=v;
			hasInit=false;
		
		}
		
		private void maybeinit()
		{
			if(hasInit)
			{
				return ;
			}
			if(this.data==null)
			{
				data=new short[BlockBufferPool.interval];
			}
			
			QuickArrayFillShort.fill(data, def);	
			hasInit=true;
		}

		@Override
		public void replace(Short a, Short b) {

			if(this.hasInit)
			{
				for(int i=0;i<this.data.length;i++)
				{
					if(this.data[i]==a)
					{
						this.data[i]=b;
					}
				}
			}else if(this.def==a){
				this.allset(b);
			}
					
		
			
		}
		
	
	}
	
	public static class BlockLong implements BlockInterface<Long>
	{
		private boolean hasInit=false;
		private long def=0;
		
		long[] data=null;


		@Override
		public Long get(int i) {
			if(hasInit)
			{
				return data[i];
			}
			return def;
		}
		
		@Override
		public void fillByInt(BlockInteger d,int skip){


			if(d.hasInit)
			{
				if(this.hasInit)
				{
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(long)d.data[i];
						}
					}
				}else{
					data=new long[BlockBufferPool.interval];
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(long)d.data[i];
						}else{
							data[i]=this.def;
						}
					}
					hasInit=true;
				}
			}else if(d.def!=skip){
				this.allset((long)d.def);
			}
		
		}

		@Override
		public void set(int i, Long v) {
			this.maybeinit();
			data[i]=v;
		}

		@Override
		public void allset(Long v) {
			this.def=v;
			hasInit=false;
		}
		
		private void maybeinit()
		{
			if(hasInit)
			{
				return ;
			}
			if(this.data==null)
			{
				data=new long[BlockBufferPool.interval];
			}
			
			QuickArrayFillLong.fill(data, def);	
			hasInit=true;
		}

		@Override
		public void replace(Long a, Long b) {

			if(this.hasInit)
			{
				for(int i=0;i<this.data.length;i++)
				{
					if(this.data[i]==a)
					{
						this.data[i]=b;
					}
				}
			}else if(this.def==a){
				this.allset(b);
			}
					
		
		}
	
	}
	
	
	public static class BlockDouble implements BlockInterface<Double>
	{
		private boolean hasInit=false;
		private double def=0;
		
		double[] data=null;

		
		@Override
		public Double get(int i) {
			if(hasInit)
			{
				return data[i];
			}
			return def;
		}

		@Override
		public void set(int i, Double v) {
			this.maybeinit();
			data[i]=v;
		}

		@Override
		public void allset(Double v) {
			this.def=v;
			hasInit=false;
		}
		
		@Override
		public void fillByInt(BlockInteger d,int skip){


			if(d.hasInit)
			{
				if(this.hasInit)
				{
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(double)d.data[i];
						}
					}
				}else{
					data=new double[BlockBufferPool.interval];
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(double)d.data[i];
						}else{
							data[i]=this.def;
						}
					}
					hasInit=true;
				}
			}else if(d.def!=skip){
				this.allset((double)d.def);
			}
		
		}
		
		private void maybeinit()
		{
			if(hasInit)
			{
				return ;
			}
			if(this.data==null)
			{
				data=new double[BlockBufferPool.interval];
			}
			
			QuickArrayFillDouble.fill(data, def);	
			hasInit=true;
		}

		@Override
		public void replace(Double a, Double b) {

			if(this.hasInit)
			{
				for(int i=0;i<this.data.length;i++)
				{
					if(this.data[i]==a)
					{
						this.data[i]=b;
					}
				}
			}else if(this.def==a){
				this.allset(b);
			}
					
		
			
		}
	}
	
	public static class BlockByte implements BlockInterface<Byte>
	{
		private boolean hasInit=false;
		private byte def=0;
		
		public byte[] data=null;
	
		
		@Override
		public Byte get(int i) {
			if(hasInit)
			{
				return data[i];
			}
			return def;
		}

		@Override
		public void set(int i, Byte v) {
			this.maybeinit();
			data[i]=v;
		}
		
		@Override
		public void fillByInt(BlockInteger d,int skip){

			if(d.hasInit)
			{
				if(this.hasInit)
				{
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(byte)d.data[i];
						}
					}
				}else{
					data=new byte[BlockBufferPool.interval];
					for(int i=0;i<d.data.length;i++)
					{
						if(d.data[i]!=skip)
						{
							data[i]=(byte)d.data[i];
						}else{
							data[i]=this.def;
						}
					}
					hasInit=true;
				}
			}else if(d.def!=skip){
				this.allset((byte)d.def);
			}
		
		
		}

		@Override
		public void allset(Byte v) {
			this.def=v;
			hasInit=false;
		}
		
		private void maybeinit()
		{
			if(hasInit)
			{
				return ;
			}
			if(this.data==null)
			{
				data=new byte[BlockBufferPool.interval];
			}
			
			QuickArrayFillByte.fill(data, def);	
			hasInit=true;
		}

		@Override
		public void replace(Byte a, Byte b) {

			if(this.hasInit)
			{
				for(int i=0;i<this.data.length;i++)
				{
					if(this.data[i]==a)
					{
						this.data[i]=b;
					}
				}
			}else if(this.def==a){
				this.allset(b);
			}
		}
	}
	
	
	public static interface BlockInterface<K>{
		public  K get(int i);
		public  void set(int i,K v);
		public  void allset(K v);
		
		public void fillByInt(BlockInteger d,int skip);
		public void replace(K a,K b);


	}
	
	public static class BlockArray<K> {
		private int size = 0;
		public BlockInterface<K>[] data;
		public BlockArray(BlockInterface<K>[] data, int size) {
			this.data = data;
			this.size = size;
		}
		
		public void fillByInt(BlockArray<Integer> d,int skip)
		{
			for(int i=0;i<d.data.length&&i<this.data.length;i++)
			{
				this.data[i].fillByInt((BlockInteger)(d.data[i]), skip);
			}
		}
		
		public void replace(K a,K b)
		{
			for(int i=0;i<this.data.length;i++)
			{
				this.data[i].replace(a,b);
			}
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
