package com.alimama.mdrill.index.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.CRC32;

import org.apache.hadoop.io.Writable;



public class DocumentMap  implements Writable
{

	private String[] data =new String[0];
	@Override
	public String toString() {
		return "DocumentMap [data=" + Arrays.toString(data) + "]";
	}

	public int setMap(ArrayList<HashMap<String, String>> list,String[] fields) {

		HashMap<String, String> rtn=new HashMap<String, String>();
		for(int i=0;i<data.length&&i<fields.length;i++)
		{
			if(data[i]!=null)
			{
				rtn.put(fields[i], data[i]);
			}
		}
		
		if(rtn.size()<=0)
		{
			return 0;
		}
		CRC32 crc32 = new CRC32();
		crc32.update(java.util.UUID.randomUUID().toString().getBytes());
		rtn.put("higo_uuid", Long.toString(crc32.getValue()));
		list.add(rtn);
		return 1;
	}

	public DocumentMap()
	{
		data=new String[0];
	}
	
	public DocumentMap(String[] l)
	{
	    this.data=l;
	}
	
		
	@Override
    public void readFields(DataInput in) throws IOException {

		int size = in.readInt();
		data=new String[size];
		for(int i=0;i<size;i++)
		{
			if(in.readBoolean())
			{
				int len=in.readInt();
				byte[] bytes=new byte[len];
				in.readFully(bytes);
				data[i]=new String(bytes,"utf8");
			}else{
				data[i]=null;
			}
		}
	
    }

	@Override
    public void write(DataOutput out) throws IOException {
		out.writeInt(data.length);
		for(int i=0;i<data.length;i++)
		{
			if(data[i]==null)
			{
				out.writeBoolean(false);
			}else{
				out.writeBoolean(true);
				byte[] arr=data[i].getBytes("utf8");
				out.writeInt(arr.length);
				out.write(arr);
			}
		}
    }
	
	
	

	}
