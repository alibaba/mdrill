package com.alimama.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.jsp.JspWriter;

import com.alimama.mdrill.jdbc.MdrillQueryResultSet;
public class Sql {
	
	public static class HeartBeat implements Runnable
	{
		public Object lock=new Object();

		JspWriter out;
		public HeartBeat(JspWriter out) {
			super();
			this.out = out;
		}
		AtomicBoolean isstop=new AtomicBoolean(false);
		

		public void setIsstop(boolean isstop) {
				this.thrStop.set(isstop);
		}
		
		AtomicBoolean thrStop=new AtomicBoolean(false);

		public boolean isstop()
		{
			return thrStop.get();
		}
		

		@Override
		public void run() {
			while(true)
			{
					if(this.thrStop.get())
					{
						thrStop.set(true);
						return ;
					}
				try {
					synchronized (this.lock) {
						if(this.out!=null)
						{
							this.out.write(" ");
							this.out.flush();
						}
					}
				} catch (Throwable e) {
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
		}
		
	}
	
public static void execute(String sql,String connstr,JspWriter out) throws ClassNotFoundException, SQLException, IOException
{
	HeartBeat hb=new HeartBeat(out);
	new Thread(hb).start();
	StringBuffer buff=new StringBuffer();
	long time1=System.currentTimeMillis();

	Class.forName("com.alimama.mdrill.jdbc.MdrillDriver");

	Connection con = DriverManager.getConnection(connstr, "", "");

	Statement stmt = con.createStatement();

	MdrillQueryResultSet res = null;

	res = (MdrillQueryResultSet) stmt.executeQuery(sql);
	buff.append("totalRecords:"+res.getTotal());
	buff.append("<br>\r\n");
	buff.append("<table border=1><tr>");
	List<String> colsNames = res.getColumnNames();
	for (int i = 0; i < colsNames.size(); i++) {
		buff.append("<td>");
	    buff.append(colsNames.get(i));
	    buff.append("</td>");
	}
	buff.append("</tr>");
	while (res.next()) {
		buff.append("<tr>");
	    for (int i = 0; i < colsNames.size(); i++) {
	    	buff.append("<td>");
		buff.append(res.getString(colsNames.get(i)));
		buff.append("</td>");
	    }
	    buff.append("</tr>");
	}
	con.close();
	buff.append("</table>");
	long time2=System.currentTimeMillis();
    buff.append("times taken "+((time2-time1)*1.0/1000)+" seconds");
    buff.append("<br>\r\n");
    
    hb.setIsstop(true);
    while(!hb.isstop())
    {
    	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
    }
    
    synchronized (hb.lock) {
		out.write(buff.toString());
	}

    
}
}
