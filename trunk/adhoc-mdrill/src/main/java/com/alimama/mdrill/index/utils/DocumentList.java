package com.alimama.mdrill.index.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;



public class DocumentList  implements Writable
{
	private ArrayList<HashMap<String, String>> list ;
	public DocumentList()
	{
		list=new ArrayList<HashMap<String,String>>();
	}
	
	public DocumentList(ArrayList<HashMap<String, String>> l)
	{
	    this.list=l;
	}
	public void clean() throws IOException
	{
		this.list.clear();
	}
	
	public RamWriter toRamWriter(DocumentConverter documentConverter,Analyzer analyzer,
			Context context) throws IOException
	{
	    ArrayList<Document> list=this.transDodument(documentConverter,context);
	    RamWriter listform = new RamWriter();
	    listform.process(list, analyzer);
	    listform.closeWriter();
	    return listform;
	}

	
    private static int debuglines=0;

	private ArrayList<Document> transDodument(
			DocumentConverter documentConverter,
			Context context) throws IOException {
		ArrayList<Document> doclist = new ArrayList<Document>(list.size());
		for (HashMap<String, String> res : list) {
			try{
			Document doc = documentConverter.convert(res);
			doclist.add(doc);
			}catch(org.apache.solr.common.SolrException e){
				context.getCounter("higo", "skipdocument").increment(1);
				if(debuglines<100)
	    		{
	    			debuglines++;
	        		System.out.println("skipdocument: " + res.toString()+","+stringify_error(e));
	    		}
			}
		}
		return doclist;
	}
	
	
	public static String stringify_error(Throwable error) {
		StringWriter result = new StringWriter();
		PrintWriter printer = new PrintWriter(result);
		error.printStackTrace(printer);
		return result.toString();
	}
	
	public byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.close();
			return bos.toByteArray();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public Object deserialize(byte[] serialized) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object ret = ois.readObject();
			ois.close();
			return ret;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
    public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
	    byte[] data = new byte[size];
	    in.readFully(data);
	    list = (ArrayList<HashMap<String, String>>) this.deserialize(data);

    }

	@Override
    public void write(DataOutput out) throws IOException {
		byte[] data = this.serialize(this.list);
	    out.writeInt(data.length);
	    out.write(data);
    }
}
