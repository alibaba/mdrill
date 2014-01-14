package com.alimama.mdrill.index.utils;


import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;



public class DocumentList  
{
	private ArrayList<HashMap<String, String>> list ;
	private int count=0;
	public DocumentList()
	{
		list=new ArrayList<HashMap<String,String>>();
	}
	
	
	public int add(DocumentMap res,String[] fields)
	{
		int cnt=res.setMap(this.list, fields);
		this.count+=cnt;
		return cnt;
	}
	
	public boolean isoversize()
	{
		return this.count>100;
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
			if(doc.getFields().size()<=0)
			{
				context.getCounter("higo", "skipdocument2").increment(1);
			}else{
				doclist.add(doc);
			}
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
	
	
}
