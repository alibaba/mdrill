package com.alimama.mdrill.adhoc;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;



public class TransHigo_udf extends UDF {
    public Text evaluate(final Text d,String type) {
        if (d == null) {
            return new Text("_");
        }
        
        return new Text(transformSolrMetacharactor(d.toString(),type));
        
    }
    
    public static String transformSolrMetacharactor(String input,String tp){
//    	boolean isString=tp.indexOf("string")>=0;
    	if(input!=null)
    	{
    		input=input.trim();
    	}
//    	if(input==null||input.isEmpty()||input.equals("\\N")||input.equals("\\n"))
//    	{
//    		return isString?"_":"0";
//    	}
    	
    	
    	if(tp.toLowerCase().indexOf("date")>=0)
    	{
    		return ensureTdate(input);
    	}
    	
		return input;
//
//    	if(!isString)
//    	{
//    		
//    		return input;
//    	}
//    	
//	      StringBuffer sb = new StringBuffer();
//	      String regex = "[+\\-&|!(){}\\[\\]^\"~*?:(\\) #/]";
//	      Pattern pattern = Pattern.compile(regex);
//	      Matcher matcher = pattern.matcher(input);
//	      while(matcher.find()){
//	          matcher.appendReplacement(sb, "_");
//	      }
//	      matcher.appendTail(sb);
//	      return sb.toString();
	  }
    
    



	public static final String yyyymmdd_regex = "(\\d{4})(\\d{2})(\\d{2})";
	public static final Pattern yyyymmdd_pattern = Pattern.compile(yyyymmdd_regex);
	public static final Matcher yyyymmdd_matcher = yyyymmdd_pattern.matcher("");
	
	public static final String yyyy_mm_dd_regex = "(\\d{4})-(\\d{2})-(\\d{2})";
	public static final Pattern yyyy_mm_dd_pattern = Pattern.compile(yyyy_mm_dd_regex);
	public static final Matcher yyyy_mm_dd_matcher = yyyy_mm_dd_pattern.matcher("");
	
	public static final String yyyy_mm_dd_2_regex = "(\\d{4})/(\\d{2})/(\\d{2})";
	public static final Pattern yyyy_mm_dd_2_pattern = Pattern.compile(yyyy_mm_dd_2_regex);
	public static final Matcher yyyy_mm_dd_2_matcher = yyyy_mm_dd_2_pattern.matcher("");
	
	public static final String yyyymmddhhsshh_regex = "(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})";
	public static final Pattern yyyymmddhhsshh_pattern = Pattern.compile(yyyymmddhhsshh_regex);
	public static final Matcher yyyymmddhhsshh_matcher = yyyymmddhhsshh_pattern.matcher("");
	public static final String yyyy_mm_dd_hh_ss_hh_regex = "(\\d{4}-\\d{2}-\\d{2}) (\\d{2}:\\d{2}:\\d{2})";
	public static final Pattern yyyy_mm_dd_hh_ss_hh_pattern = Pattern.compile(yyyy_mm_dd_hh_ss_hh_regex);
	public static final Matcher yyyy_mm_dd_hh_ss_hh_matcher = yyyy_mm_dd_hh_ss_hh_pattern.matcher("");
	public static final String valid_regex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";
	public static final Pattern valid_pattern = Pattern.compile(valid_regex);
	public static final Matcher valid_matcher = valid_pattern.matcher("");
	

	/**
	 * 
	 * @param string
	 * @return
	 */
	public static String ensureTdate(String string) {
		try{
			yyyymmdd_matcher.reset(string);
			int len=string.length();
			if(len==8&&yyyymmdd_matcher.find()){
				return  yyyymmdd_matcher.group(1)+"-"+yyyymmdd_matcher.group(2)+"-"+yyyymmdd_matcher.group(3)+"T00:00:00Z";
			}
			
			yyyy_mm_dd_matcher.reset(string);
			if(len==10&&yyyy_mm_dd_matcher.find()){
				return  yyyy_mm_dd_matcher.group(1)+"-"+yyyy_mm_dd_matcher.group(2)+"-"+yyyy_mm_dd_matcher.group(3)+"T00:00:00Z";
			}
			
			yyyy_mm_dd_2_matcher.reset(string);
			if(len==10&&yyyy_mm_dd_2_matcher.find()){
				return  yyyy_mm_dd_2_matcher.group(1)+"-"+yyyy_mm_dd_2_matcher.group(2)+"-"+yyyy_mm_dd_2_matcher.group(3)+"T00:00:00Z";
			}
			
			
			yyyymmddhhsshh_matcher.reset(string);

			if(len==14&&yyyymmddhhsshh_matcher.find()){
			    return yyyymmddhhsshh_matcher.group(1)+"-"+yyyymmddhhsshh_matcher.group(2)+"-"+yyyymmddhhsshh_matcher.group(3)+"T"+yyyymmddhhsshh_matcher.group(4)+":"+yyyymmddhhsshh_matcher.group(5)+":"+yyyymmddhhsshh_matcher.group(6)+"Z";
			}
			
			yyyy_mm_dd_hh_ss_hh_matcher.reset(string);

			if(len==19&&yyyy_mm_dd_hh_ss_hh_matcher.find()){
			    return yyyy_mm_dd_hh_ss_hh_matcher.group(1)+"T"+yyyy_mm_dd_hh_ss_hh_matcher.group(2)+"Z";
			}
			
			valid_matcher.reset(string);

			if(valid_matcher.find()){
			    return valid_matcher.group();
			}
			
				return "2099-09-09T00:00:00Z";
			
		}catch(Exception e){
		}
		return "2099-09-09T00:00:00Z";
	}
	


}