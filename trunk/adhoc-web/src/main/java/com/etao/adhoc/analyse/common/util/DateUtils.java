package com.etao.adhoc.analyse.common.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateUtils {
	public static List<String> getRecentDates(int length){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		List<String> list = new ArrayList<String>();
		Calendar c = Calendar.getInstance();  
		for(int i = 0; i < length; i++){
			c.add(Calendar.DATE,-1);
			list.add(sdf.format(c.getTime()));
		}
		return list;
	}
}
