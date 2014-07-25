package com.etao.adhoc.analyse.cal;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.etao.adhoc.analyse.dao.MysqlService;

public class CalculateService {
	MysqlService server;
	public CalculateService () {
		server = new MysqlService();
	}
	public void calculate(String thedate){
		server.calModuleInfo(thedate);
		server.calDayUserPv(thedate);
		server.calTotalUserPv();
	}
	public void close(){
		if(server != null) {
			server.close();
		}
	}
	public static void main(String[] args) {
	 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	 Date lastDay=new Date(System.currentTimeMillis()-1000l*3600*24);
		String day=sdf.format(lastDay);
		if(args.length>0)
		{
			day=args[0];
		}
		CalculateService server = new CalculateService();
		server.calculate(day);
	}

}
