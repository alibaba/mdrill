package com.alimama.mdrill.index;

import java.util.HashMap;

public class SplitStrEncode {
	public static void main(String[] args) {
		System.out.println(String.valueOf(parseSplit("8@1")).equals(String.valueOf("\001")));
		System.out.println(String.valueOf(parseSplit("8@5")).equals("\005"));
		System.out.println(String.valueOf(parseSplit("8@10")).equals("\010"));

		System.out.println(String.valueOf(parseSplit("16@10")).equals(String.valueOf((char)0x010)));
		System.out.println(String.valueOf(parseSplit("16@11")).equals(String.valueOf((char)0x011)));


		
	}
	private static HashMap<String,String> match=new  HashMap<String,String>();
	static{
		match.put("default", "\001");
		match.put("tab", "\t");
		for(byte i=0;i<Byte.MAX_VALUE&&i<500;i++)
		{
			match.put("8@"+Integer.toOctalString(i), String.valueOf((char)i));
		}
		for(byte i=0;i<Byte.MAX_VALUE&&i<500;i++)
		{
			match.put("16@"+Integer.toHexString(i), String.valueOf((char)i));
		}
		
	}
	public static String parseSplit(String str)
	{
		if(str.equals("\\001"))
		{
			return "\001";
		}
		if(str.equals("default"))
		{
			return "\001";
		}
		if(str.equals("tab"))
		{
			return "\t";
		}
		
		if(match.containsKey(str))
		{
			return match.get(str);
		}
		
		return str;
	}
}
