package com.alimama.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import com.alimama.mdrill.jdbc.MdrillQueryResultSet;
public class Sql {
public static String execute(String sql,String connstr) throws ClassNotFoundException, SQLException
{
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

	return buff.toString();

    
}
}
