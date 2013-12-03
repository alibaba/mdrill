package com.alimama.mdrill.index.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;



public class DocumentMapOld  implements Writable
{
	private HashMap<String, String> data ;
	public HashMap<String, String> getMap() {
		return data;
	}

	public DocumentMapOld()
	{
		data=new HashMap<String, String>();
	}
	
	public DocumentMapOld(HashMap<String, String> l)
	{
	    this.data=l;
	}
	
		
	@Override
    public void readFields(DataInput in) throws IOException {
		data=new HashMap<String, String>();
		int size = in.readInt();
		for(int i=0;i<size;i++)
		{
			this.data.put(in.readUTF(), in.readUTF());
		}
    }

	@Override
    public void write(DataOutput out) throws IOException {
		out.writeInt(this.data.size());
		for(Entry<String, String> e:this.data.entrySet())
		{
			out.writeUTF(e.getKey());
			out.writeUTF(e.getValue());
		}
    }
}
