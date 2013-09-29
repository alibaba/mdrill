package com.alimama.mdrill.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.CRC32;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.alimama.mdrill.index.utils.DocumentList;
import com.alimama.mdrill.index.utils.DocumentMap;
import com.alimama.mdrill.index.utils.JobIndexPublic;
import com.alimama.mdrill.index.utils.TdateFormat;



public class IndexMapper extends   Mapper<WritableComparable, Text, Text, DocumentMap> {
    private String[] fields = null;
    private Boolean[] isDate;
    private Boolean[] isString;
    private Boolean[] isStore;
    
    private String split="\001";
    private boolean usedthedate=true;
    private String thedate=null;
    
    private Integer Index=(int) (Math.random()*10000);
    
    private String uniqfield="";
    private boolean isuniqcheck=false;

    @Override
    public void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		String fieldStrs = context.getConfiguration().get("higo.index.fields");
		this.uniqfield= context.getConfiguration().get("uniq.check.field");
		if(this.uniqfield!=null&&this.uniqfield.length()>0)
		{
			this.isuniqcheck=true;
		}
		split=MakeIndex.parseSplit(context.getConfiguration().get("higo.column.split",split));
		String custfields=context.getConfiguration().get("higo.column.custfields","");
		usedthedate=context.getConfiguration().getBoolean("higo.column.userthedate", usedthedate);
		this.thedate=null;
		if(usedthedate)
		{
		 InputSplit inputSplit = context.getInputSplit();
	     Path filepath = ((FileSplit) inputSplit).getPath();
	     String inputbase = context.getConfiguration().get("higo.input.base");
	     this.thedate=JobIndexPublic.parseThedate(new Path(inputbase),filepath);
 		System.out.println("thedatepath: " + thedate+"@"+filepath.toString() +"@"+inputbase + "");
		}

		
		if(custfields==null||custfields.isEmpty())
		{
			String[] fieldslist = fieldStrs.split(",");
			this.fields = new String[fieldslist.length];
			this.isDate = new Boolean[fieldslist.length];
			this.isString = new Boolean[fieldslist.length];
			this.isStore = new Boolean[fieldslist.length];
		
			for (int i = 0; i < fieldslist.length; i++) {
			    String[] fieldSchema = fieldslist[i].split(":");
			    String fieldName = fieldSchema[0].trim().toLowerCase();
			    String type = fieldSchema[1];
			    this.isStore[i] = Boolean.valueOf(fieldSchema[3]);
			    this.fields[i] = fieldName;
			    this.isDate[i] = type.equalsIgnoreCase("tdate");
			    this.isString[i] = type.equalsIgnoreCase("string");
			}
		}else{
			String[] fieldslist = custfields.split(",");
			this.fields = new String[fieldslist.length];
			this.isDate = new Boolean[fieldslist.length];
			this.isString = new Boolean[fieldslist.length];
			this.isStore = new Boolean[fieldslist.length];
		
			for (int i = 0; i < fieldslist.length; i++) {
			    this.isStore[i] = Boolean.valueOf(false);
			    this.fields[i] = fieldslist[i];
			    this.isDate[i]= false;
			    this.isString[i] = true;
			}
		}
    }

    protected void cleanup(Context context) throws IOException,
	    InterruptedException {
    }
    
    private String parseDefault(String input)
    {
    	if (input == null) {
			return null;
		}
    	input=input.trim();
		if (input.isEmpty() || input.equals("\\N")|| input.equals("\\n")) {
			return null;
		}

		return input;
    }
	
    private int debuglines=0;
    private int printlines=0;
    
    private boolean validate(String[] values,String record,Context context)
    {
    	if(usedthedate)
    	{
	    	if(values.length<2)
	    	{
	    		if(debuglines<100)
	    		{
	    			debuglines++;
	        		System.out.println("miss columns values2: " + record.replaceAll(split, "#")   + "");
	    		}
	    		context.getCounter("higo", "skiprecords").increment(1);
	    		return false;
	    	}
    	}else{
        	if(parseDefault(record)==null)
        	{
	    		return false;
        	}

    	}
    	return true;
    }
    
    private boolean line(String record,Context context) throws IOException, InterruptedException
    {
		context.getCounter("higo", "totalrecord").increment(1);
    	String[] values = record.split(split,-1);
    	if(!this.validate(values, record, context))
    	{
    		return false;
    	}

    	HashMap<String, String> res = new HashMap<String, String>(fields.length);
    	for (int i = 0; i < fields.length; i++) {
    	    String fieldName = fields[i];
    	    String string =(i<values.length)?values[i]:null;
    	    String val=parseDefault(string);

			if (this.isDate[i]) {
				res.put(fieldName, TdateFormat.ensureTdate(val, fieldName));
			}else if(val!=null){
				res.put(fieldName,val);
			}else if(this.isString[i]){
				res.put(fieldName,"_");
			}
    	}
    	
    	if(usedthedate)
    	{	
    		if(thedate!=null)
    		{
    			res.put("thedate", thedate);//从文件的路径中获取
    		}
    		
	    	res.put("thedate", String.valueOf(res.get("thedate")).replaceAll("-", "").replaceAll("_", ""));
	    	
	    	if(res.get("thedate").length()!=8)
	    	{
	    		if(debuglines<100)
	    		{
		    		debuglines++;
		    		System.out.println("miss thedate values: " + record.replaceAll(split, "#")   + "");
	    		}
	    		context.getCounter("higo", "skiprecords").increment(1);
	    	}
	    	
	    	context.getCounter("higo", "dayrecord_"+String.valueOf(res.get("thedate"))).increment(1);
    	
	    	CRC32 crc32 = new CRC32();
			crc32.update(java.util.UUID.randomUUID().toString().getBytes());
	    	res.put("higo_uuid", Long.toString(crc32.getValue()));
    	
    	}
    	
    	
    	if(printlines<10)
		{
    		printlines++;
    		System.out.println("res: " + res.toString()   + " arrays,"+Arrays.toString(values));
		}
    	
	    context.write(new Text(String.valueOf(this.Index++)), new DocumentMap(res));

    	
    	if(this.isuniqcheck&&res.containsKey(this.uniqfield))
    	{
    		String notempty=res.get(this.uniqfield);
    		if(notempty.length()>0&&!notempty.equals("_"))
    		{
    	    context.write(new Text("uniq_"+notempty), new DocumentMap());
    		}
    	}
    	
    	return true;
        
    }

    @Override
    public void map(WritableComparable key, Text value, Context context)
 throws IOException, InterruptedException {
		String[] records = value.toString().split("[\n]+");
		for(String record:records)
		{
			this.line(record, context);
		}
	}

}
