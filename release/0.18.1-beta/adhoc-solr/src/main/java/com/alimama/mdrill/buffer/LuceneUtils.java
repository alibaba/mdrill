package com.alimama.mdrill.buffer;

import java.util.zip.CRC32;

import org.apache.lucene.index.IndexReader;




public class LuceneUtils  {
	
	public static String crcKey(IndexReader r)
	{
		String key= r.getStringCacheKey();
		CRC32 crc32 = new CRC32();
		crc32.update(new String(key).getBytes());
		long crcvalue = crc32.getValue();
		
		StringBuffer buff=new StringBuffer();
		buff.append(abs(key.hashCode()));
		buff.append("_");
		buff.append(abs(crcvalue));
		buff.append("_");
		buff.append(key.length());
		return buff.toString();
	}
	
	
	public static long abs(long num)
	{
		if(num<0)
		{
			return num*-1;
		}
		return num;
	}
}
