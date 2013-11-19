package com.alimama.mdrill.buffer;

import java.io.IOException;

import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.IndexInput;
import org.apache.solr.request.uninverted.GrobalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.buffer.BlockBufferMalloc.block;
import com.alimama.mdrill.buffer.BlockBufferMalloc.blockData;




/**
 * 本类是为了tis,frq文件准备，考虑到海狗的特殊性，经常使用group by,需要经常使用tis,frq等文件，故对tis,frq等文件进行按照block方式缓存在内存中，采用lru的方式进行管理
 * 海狗要经常的扫描几亿甚至几十亿的记录，针对扫描我们也做了不少的优化
1.	针对lucene倒排索引的特性，扫描记录意味着扫描tii,tis与frq三个文件
Tii本身由lucene加载到内存,放到数组中，没什么可优化的
tis与frq则分别存储了每个field的值以及该值对应了那些文档，在扫描记录的时候要频繁的切换这俩文件，频繁的切换意味着并非顺序读，实际上为随即读,我们知道因为磁盘本身的物理特性随即读的性能远远小于顺序读的性能，这个地方要进行转换
   针对这种情形我们的优化思路是这样的，因为是按列扫描，最终这个列的所有的值都是要被读出来的，那么每次读的时候可以多读一些，比如说一次读他个1M的数据放到内存中，这样来回切换tis 与frq的时候，就会减少与本地硬盘的交互次数，性能就会提升很多
   按照这个思路，我们实现了BlockBufferInput，实际上他实现了lucene的IndexInput接口，将文件分割成一个一个的block，每次读的时候读取一个block，多个block之间采用lru的方式进行管理。

 * @author yannian.mu
 *
 */
public class BlockBufferInput extends BufferedIndexInput {
	public static Logger log = LoggerFactory.getLogger(BlockBufferInput.class);
	public static int BLOCK_SIZE_OFFSET=16;//1024*64
	public static int BLOCK_SIZE = 1<<BLOCK_SIZE_OFFSET;
	private Descriptor descriptor;
	private boolean isOpen;
	private boolean isClone;
	private Object key;
	
	
	public blockData lastbuff =new blockData(null, 0);
	public Long lastBlockIndex = -1l;
	
	public BlockBufferInput(IndexInput input,Object key) {
		super("BlockBufferInput", 1024);
		this.descriptor = new Descriptor(input);
		this.isOpen = true;
		this.key=key;
	}
	
	
	private int createcount=0;
	private static Object lock=new Object();
	private int getbuff(long position,byte[] b,int offset,int len) throws IOException
	{
		long blockIndex=position>>BLOCK_SIZE_OFFSET;
		long blocktart=blockIndex<<BLOCK_SIZE_OFFSET;
		blockData blockdata=null;
		if(blockIndex==this.lastBlockIndex)
		{
			blockdata=this.lastbuff;
		}
		
		if(blockdata==null){
			block blk=new block(this.key, blockIndex);
			blockdata=(blockData) GrobalCache.fieldValueCache.get(blk);
			if (blockdata == null) {
				synchronized (lock) {
					blockdata = (blockData) GrobalCache.fieldValueCache.get(blk);
					if (blockdata == null) {
						long end = this.length();
						int size = BLOCK_SIZE;
						if (blocktart + size >= end) {
							size = (int) (end - blocktart);
						}
						blockdata = BlockBufferMalloc.malloc(size);// new
																	// blockData(new
																	// byte[BLOCK_SIZE],size);
						synchronized (this.descriptor.in) {
							this.descriptor.in.seek(blocktart);
							this.descriptor.in.readBytes(blockdata.buff, 0,size);
						}
						blockdata.allowFree.incrementAndGet();
						GrobalCache.fieldValueCache.put(blk, blockdata);
						createcount++;
					} else {
						blockdata.allowFree.incrementAndGet();
					}
				}
			}else{
				blockdata.allowFree.incrementAndGet();

			}

			this.lastbuff.allowFree.decrementAndGet();
			this.lastbuff=blockdata;
			this.lastBlockIndex=blockIndex;
		}
		
		int blockoffset=(int)(position-blocktart);
		int leftsize=blockdata.size-blockoffset;
		int returnsize=leftsize;
		if(len<returnsize)
		{
			returnsize=len;
		}
        System.arraycopy(blockdata.buff, blockoffset, b, offset, returnsize);
		return returnsize;
	}
	
	
	protected void finalize() throws Throwable
    {
		super.finalize();
		if (!isClone && isOpen) {
			close(); 
		    }
		this.freeBlock();
    }
	
	private void freeBlock()
	{
		this.lastbuff.allowFree.decrementAndGet();
		this.lastbuff=new blockData(null, 0);
		this.lastBlockIndex=-1l;
	}
	

	@Override
	protected void readInternal(byte[] b, int offset, int length)
			throws IOException {
		long position = getFilePointer();
		int off = offset;
		int len = length;
		while (len > 0) {
			int size = this.getbuff(position, b, off, len);
			position += size;
			off += size;
			len -= size;
		}
	}

	@Override
	protected void seekInternal(long pos) throws IOException {
		
	}

	public Object clone() {
		BlockBufferInput clone = (BlockBufferInput) super.clone();
	    clone.isClone = true;
	    clone.lastbuff=new blockData(null, 0);
	    clone.lastBlockIndex=-1l;
	    return clone;
	}
	
	@Override
	public void close() throws IOException {
		this.freeBlock();
	    if (!isClone) {
		if (isOpen) {
			log.info("##buffer_close##"+"@free:"+BlockBufferMalloc.free.size()+"#malloc:"+BlockBufferMalloc.mallocTimes.get()+":"+BlockBufferMalloc.reusedTimes.get()+"@create:"+this.createcount);
		    descriptor.in.close();
		    isOpen = false;
		} else {
		    throw new IOException("Index file "   + " already closed");
		}
	    }
	}
	

	@Override
	public long length() {
	    return descriptor.in.length();
	}
	
	
	private class Descriptor {
		public final IndexInput in;
		public Descriptor(IndexInput input) {
			this.in = input;
		}
	}
	
	public static class KeyInput extends IndexInput
	{

		public IndexInput input;
		public String key;

		public KeyInput(IndexInput input, String key) {
			super("KeyInput");
			this.input = input;
			this.key = key;
		}

		@Override
		public void close() throws IOException {
			input.close();
			
		}

		@Override
		public long getFilePointer() {
			return input.getFilePointer();
		}

		@Override
		public void seek(long pos) throws IOException {
			input.seek(pos);
		}

		@Override
		public long length() {
			return input.length();
		}

		@Override
		public byte readByte() throws IOException {
			return input.readByte();
		}

		@Override
		public void readBytes(byte[] b, int offset, int len) throws IOException {
			input.readBytes(b, offset, len);
		}
		
	}
}
