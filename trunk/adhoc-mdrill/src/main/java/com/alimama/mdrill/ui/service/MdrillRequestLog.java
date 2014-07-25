package com.alimama.mdrill.ui.service;

import org.apache.log4j.Logger;

public class MdrillRequestLog {
	private static Logger LOG = Logger.getLogger(MdrillRequestLog.class);

	public static String cutString(String str)
	{
		String rtn=String.valueOf(str);
		
		return rtn.substring(0,Math.min(rtn.length(), 10240));
	}
	

	
	public static String logRequest(String projectName, String callback,
			String startStr, String rowsStr, String queryStr, String dist,
			String fl, String groupby, String sort, String order,String leftjoin) {
		StringBuffer debugBuffer = new StringBuffer();
		debugBuffer.append(projectName).append(",").append(startStr)
				.append(",").append(rowsStr).append(",").append(queryStr)
				.append(",").append(fl).append(",");
		debugBuffer.append(groupby).append(",").append(sort).append(",")
				.append(order).append(",").append(leftjoin).append(",");
		
		String rtn=MdrillRequestLog.cutString(debugBuffer.toString());
		LOG.info("logRequest:" + rtn);

		return rtn;
	}

}
