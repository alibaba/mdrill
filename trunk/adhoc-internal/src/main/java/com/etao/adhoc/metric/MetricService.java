package com.etao.adhoc.metric;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alimama.web.adhoc.Upload;

import backtype.storm.utils.Utils;


public class MetricService {
	private static Logger LOG = Logger.getLogger(Upload.class);

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	Connection conn;
	Map conf;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public MetricService() {}
	
	private void open()
	{
		conf = Utils.readStormConfig("adhoc.yaml");
		String url = (String) conf.get("metric.mysql.url");
		String username = (String) conf.get("metric.mysql.username");
		String password = (String) conf.get("metric.mysql.password");
		try {
		
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			LOG.error(url+","+username+","+password,e);
		} catch (SQLException e) {
			LOG.error(url+","+username+","+password,e);
		}
	
	}
	public void insert(Metric metric) {
		this.open();
		String sql = "INSERT INTO adhoc_metric" +
				"(thedate,type,tablename,linecnt,impression,finclick,finprice," +
				"alipay_direct_num,alipay_direct_amt,alipay_indirect_num," +
				"alipay_indirect_amt)" +
				" VALUES(?,?,?,?,?,?,?,?,?,?,?) ";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,	metric.getThedate()); 
			pstmt.setLong(2, metric.getType());
			pstmt.setString(3,metric.getTablename());
			pstmt.setLong(4,metric.getLineCnt());
			pstmt.setLong(5, metric.getImpression());
			pstmt.setLong(6, metric.getFinClick());
			pstmt.setFloat(7, metric.getFinPrice());
			pstmt.setLong(8, metric.getAlipayDirectNum());
			pstmt.setFloat(9, metric.getAlipayDirectAmt());
			pstmt.setLong(10, metric.getAlipayIndirectNum());
			pstmt.setFloat(11,metric.getAlipayIndirectAmt());
			pstmt.executeUpdate();
			System.out.println(pstmt.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when insert Metric: "
					+ metric);
			e.printStackTrace();
		}
		this.close();
	}
	public void delete(Metric metric) {
		this.open();
		String sql = "DELETE FROM adhoc_metric" +
				" WHERE thedate=?" +
				" AND type=?" +
				" AND tablename=? ";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,	metric.getThedate()); 
			pstmt.setLong(2, metric.getType());
			pstmt.setString(3,metric.getTablename());
			pstmt.executeUpdate();
			System.out.println(pstmt.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when delete Metric: "
					+ metric);
			e.printStackTrace();
		}
		this.close();
		
	}
	public HashMap<String,ArrayList<Metric>> getMetric(String thedate) {
		this.open();
		String sql = "SELECT sum(linecnt) as linecnt,sum(impression) as impression,sum(finclick) as finclick,sum(finprice) as finprice," +
				"sum(alipay_direct_num) as alipay_direct_num,sum(alipay_direct_amt)," +
				"sum(alipay_indirect_num),sum(alipay_indirect_amt),tablename,type " +
				" FROM adhoc_metric" +
				" WHERE thedate=?" +
				" group by tablename,type " +
				" order by tablename,type";
		PreparedStatement pstmt=null;
		
		HashMap<String,ArrayList<Metric>> rtn=new HashMap<String,ArrayList<Metric>>();
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, thedate);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				Metric metric = new Metric();
				metric.setThedate(thedate);
			
				metric.setLineCnt(rs.getLong(1));
				metric.setImpression(rs.getLong(2));
				metric.setFinClick(rs.getLong(3));
				metric.setFinPrice(rs.getFloat(4));
				metric.setAlipayDirectNum(rs.getLong(5));
				metric.setAlipayDirectAmt(rs.getFloat(6));
				metric.setAlipayIndirectNum(rs.getLong(7));
				metric.setAlipayIndirectAmt(rs.getFloat(8));
				String tablename=rs.getString(9);
				metric.setType(rs.getLong(10));

				metric.setTablename(tablename);
				ArrayList<Metric> list=rtn.get(tablename);
				if(list==null)
				{
					list=new ArrayList<Metric>();
					rtn.put(tablename, list);
				}
				list.add(metric);
			}
		} catch (SQLException e) {
			LOG.error(String.valueOf(pstmt),e);

		}
		
		
		this.close();
		return rtn;
	}
	
	public List<String> getRecentDays(String tableName, String fieldName, int n) throws SQLException {
		this.open();
		List<String> days = null;
		String sql = "SELECT thedate" +
				" FROM adhoc_metric" +
				" group by thedate order by thedate desc" +
				" LIMIT %d";
		Statement stmt;
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(sql,n));
			days = new ArrayList<String>();
			while(rs.next()) {	
				days.add(rs.getString(1));
			} 
			this.close();
		return days;
	}
	
	public void close() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
						+ "Error when close Connection");
				e.printStackTrace();
			}
			
			conn=null;
		}
	}

	

}
