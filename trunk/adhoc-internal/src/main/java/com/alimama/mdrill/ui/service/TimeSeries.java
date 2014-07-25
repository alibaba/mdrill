package com.alimama.mdrill.ui.service;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import weka.core.Instances;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.WekaForecaster;

import java.io.*;

public class TimeSeries {
	private static Logger LOG = Logger.getLogger(TimeSeries.class);
	private static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

	private static String[] getAllFields(JSONArray list) throws JSONException
	{
		HashSet<String> hashSet=new HashSet<String>();
		for(int i=0;i<list.length();i++)
		{
			JSONObject item=list.getJSONObject(i);
			 Iterator  keys = item.keys();
		        while (keys.hasNext()) {
		        	hashSet.add((String) keys.next());
		        }
		}
		
		String[] fields=hashSet.toArray(new String[hashSet.size()]);
		return fields;
	}
	
	private static HashMap<String,String> fieldToShowName(String fl,String dimvalue)
	{
		String[] ffffields=fl.split(",");
		
		 HashMap<String,String> field2dimvalue=new HashMap<String, String>();
	       String[] dimvalues=dimvalue.replaceAll(" ", "").replaceAll("\t", "").replaceAll("^,", "").split(",");
	       for(int i=0;i<ffffields.length;i++)
		      {
		    	  String f=ffffields[i];
		    	
		    	  
		    	  if(f.indexOf("(")>=0&&f.indexOf(")")>=0)
		    	  {
		    		  if(i>0&&(i-1)<dimvalues.length)
			    	  {
			    		  field2dimvalue.put(f, dimvalues[i-1]);
			    	  }else{
				    	  field2dimvalue.put(f, f);
			    	  }
		    		 
		    	  }
		      }
	       
	       return field2dimvalue;
	}
	
	private static HashMap<String,HashMap<String,Object>> setUpLines(StringBuilder numberfield,String[] fields,boolean isSingleY, HashMap<String,String> field2dimvalue){

		HashMap<String,HashMap<String,Object>> lines=new HashMap<String,HashMap<String,Object>>();
	       String join="";

	      for(int i=0;i<fields.length;i++)
	      {
	    	  String f=fields[i];
	    	  if(f.indexOf("(")>=0&&f.indexOf(")")>=0)
	    	  {
	    		  numberfield.append(join);
		    	   numberfield.append(f);
		    	   join=",";
		    	   
		    	   
		    	  HashMap<String, Object> label=new HashMap<String, Object>();
		    	  label.put("Y", isSingleY?"Y":f);
		    	  label.put("label", field2dimvalue.get(f));
		    	  label.put("data", new ArrayList<ArrayList<Object>>());
		    	  lines.put(f, label);
		    	  HashMap<String, Object> labelpre=new HashMap<String, Object>();
		    	  labelpre.put("Y", isSingleY?"Y":f);
		    	  labelpre.put("label", field2dimvalue.get(f)+"_预测");
		    	  labelpre.put("data", new ArrayList<ArrayList<Object>>());
		    	  lines.put(f+"_pre", labelpre);
	    	  }
	      }
	      
	      return lines;
	}
	
	public static void writeArrfHeader( OutputStreamWriter ramwriter,String[] fields) throws IOException
	{

	     ramwriter.append("% Sales of Australian wine (thousands of litres)\r\n");
	    ramwriter.append("% from Jan 1980 - 1980-07-01 1995. Data is sorted in\r\n");
	    ramwriter.append("% time\r\n");
	      ramwriter.append("@relation mdrill\r\n");
	      ramwriter.append("\r\n");
	       

	      for(int i=0;i<fields.length;i++)
	      {
	    	  String f=fields[i];
	    	  if(f.indexOf("(")>=0&&f.indexOf(")")>=0)
	    	  {
	    		  ramwriter.append("@attribute "+f+" numeric\r\n");
	    	  }else if(f.equals("thedate")){
	    		  ramwriter.append("@attribute "+f+" date 'yyyyMMdd'\r\n");
	    	  }else{
	    		  ramwriter.append("@attribute "+f+" string\r\n");
	    	  }
	      }
	      ramwriter.append("\r\n");
	      ramwriter.append("@data\r\n");
	}
	
	
	public static class minMaxX{
		long min=Long.MAX_VALUE;
	      long max=Long.MIN_VALUE;
	}
	
	
	private static String maxThedate(String f,String maxThedate,Object val)
	{
		 if(f.equals("thedate"))
  	   {
		if(maxThedate==null||maxThedate.compareTo(String.valueOf(val))<=0)
		   {
			   maxThedate=String.valueOf(val);
		   }
  	   }
		return maxThedate;
	}
	public static void makePre(HashMap<String,HashMap<String,Object>> lines,boolean writeline,int end,int presize,minMaxX minmax,String[] fields,JSONArray list,StringBuilder numberfield) throws Exception
	{
	      ByteArrayOutputStream ramout=new ByteArrayOutputStream();
	      OutputStreamWriter ramwriter=new OutputStreamWriter(ramout);
	      writeArrfHeader(ramwriter, fields);

	      String maxThedate=null;
	      for(int i=0;i<end;i++)
		{
			JSONObject item=list.getJSONObject(i);
			 String joinl="";
		      StringBuilder bl = new StringBuilder();
		       for(String f:fields)
		       {
		    	   Object val=item.get(f);
		    	   maxThedate=maxThedate(f,maxThedate, val);

		    	   bl.append(joinl);
		    	   bl.append(val);
		    	   joinl=",";

		    	   if(writeline&&lines.containsKey(f))
		    	   {
		    		   ArrayList<ArrayList<Object>> d=(ArrayList<ArrayList<Object>>) lines.get(f).get("data");
		    		   ArrayList<Object> drow=new ArrayList<Object>();
		    		   long t=fmt.parse(item.getString("thedate")).getTime();
		    		   minmax.min=Math.min(minmax.min, t);
		    		   minmax.max=Math.max(minmax.max, t);
		    		   drow.add(0, t);
		    		   drow.add(1, val);
		    		   d.add(drow);
		    	   }
		       }
		       
		       ramwriter.append(bl.toString()+"\r\n");

		}
	      
	      ramwriter.close();
	     
	      byte[] data=ramout.toByteArray();
	      ByteArrayInputStream	 ramStrem=new ByteArrayInputStream(data);
	      Instances wine = new Instances(new InputStreamReader(ramStrem));
	      WekaForecaster forecaster = new WekaForecaster();
	      forecaster.setFieldsToForecast(numberfield.toString());
	      forecaster.setBaseForecaster(new GaussianProcesses());
	      forecaster.getTSLagMaker().setTimeStampField("thedate"); // date time stamp
	      forecaster.getTSLagMaker().setMinLag(1);
	      forecaster.getTSLagMaker().setMaxLag(12); // monthly data
	      forecaster.getTSLagMaker().setAddMonthOfYear(true);
	      forecaster.getTSLagMaker().setAddQuarterOfYear(true);
	      forecaster.buildForecaster(wine);
	      forecaster.primeForecaster(wine);

	      // training data
	      List<List<NumericPrediction>> forecast = forecaster.forecast(presize);

	     Date startdate=fmt.parse(maxThedate);
	     String[] numfieldsarr=numberfield.toString().split(",");
	      for (int i = 0; i < presize; i++) {
	        List<NumericPrediction> predsAtStep = forecast.get(i);
	        for (int j = 0; j < numfieldsarr.length; j++) {
	          NumericPrediction predForTarget = predsAtStep.get(j);
        
	          if(lines.containsKey(numfieldsarr[j]+"_pre"))
	    	   {
	    		   ArrayList<ArrayList<Object>> d=(ArrayList<ArrayList<Object>>) lines.get(numfieldsarr[j]+"_pre").get("data");
	    		   ArrayList<Object> drow=new ArrayList<Object>();
	    		   
	    		   long t=new Date(startdate.getTime()+1000l*3600*24*(i)).getTime();
	    		   minmax.min=Math.min(minmax.min, t);
	    		   minmax.max=Math.max(minmax.max, t);
	    		   drow.add(0, t);
	    		   drow.add(0, t);
	    		   drow.add(1, predForTarget.predicted());
	    		   d.add(drow);
	    	   }
	        }
	      }
	}
	
	public static String result(String projectName, String callback,
			String startStr, String rowsStr, String queryStr, String dist,
			String fl, String groupby, String sort, String order,String leftjoin,JspWriter out,String dimvalue,String singleY)
			throws Throwable {
	    HeartBeat hb=new HeartBeat(out);
		new Thread(hb).start();
		boolean isSingleY=false;
		if(singleY!=null&&singleY.toUpperCase().equals("Y"))
		{
			isSingleY=true;
		}
		  HashMap<String,Object> rtna=new HashMap<String, Object>();
		  if(groupby==null||!groupby.equals("thedate"))
		  {
			  rtna.put("code",0);
				rtna.put("msg","时间序列分析必须选择日期，且不能有其他维度");
				hb.stop();
				return new JSONObject(rtna).toString();
		  }
		  
		String rtn= MdrillService.result(projectName, null, "0", "100", queryStr, dist, fl, groupby, "thedate", "asc", leftjoin, null);
		
		JSONObject jsonObj = new JSONObject(rtn);
		if(!jsonObj.getString("code").equals("1"))
		{
			rtna.put("code",0);
			rtna.put("msg","服务器异常，请稍后再试");
			hb.stop();
			return new JSONObject(rtna).toString();
		}

		JSONArray list=jsonObj.getJSONObject("data").getJSONArray("docs");
		int presize=(list.length()/3)+1;
		
	  if(list.length()<2)
	  {
		  rtna.put("code",0);
			rtna.put("msg","请至少选择2天的数据");
			hb.stop();
			return new JSONObject(rtna).toString();
	  }
	  
		String[] fields=getAllFields(list);
		 HashMap<String,String> field2dimvalue=fieldToShowName(fl, dimvalue);

	      StringBuilder numberfield = new StringBuilder();

	      
		HashMap<String,HashMap<String,Object>> lines=setUpLines(numberfield,fields, isSingleY, field2dimvalue);

	      int len=list.length();
	      minMaxX minmax=new minMaxX();
		for(int i=10;i<len;i++)
		{
		      makePre(lines, false, i, 1, minmax, fields, list, numberfield);
		}
	      makePre(lines, true, len, presize, minmax, fields, list, numberfield);


		
	
	      hb.stop();
	    rtna.put("code", 1);
	  rtna.put("data", lines);
	  rtna.put("min", minmax.min);
	  rtna.put("max", minmax.max);
	  return new JSONObject(rtna).toString();
	      
	}
	
	
}
