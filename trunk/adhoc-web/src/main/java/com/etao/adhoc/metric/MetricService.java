package com.etao.adhoc.metric;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.etao.adhoc.common.util.YamlUtils;

public class MetricService {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	Connection conn;
	Map conf;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public MetricService() {
		try {
			conf = YamlUtils.getConfigFromYamlFile("adhoc-metric.yaml");
			String url = (String) conf.get("mysql.url");
			String username = (String) conf.get("mysql.username");
			String password = (String) conf.get("mysql.password");
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(url, username, password);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when open YAML File");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Can not find class: " + JDBC_DRIVER);
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when building MetricService Connection");
			e.printStackTrace();
		}
	}
	public void insert(Metric metric) {
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when insert Metric: "
					+ metric);
			e.printStackTrace();
		}
	}
	public void delete(Metric metric) {
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when delete Metric: "
					+ metric);
			e.printStackTrace();
		}
		
	}
	public Metric getMetric(String thedate, int type, String tablename) {
		String sql = "SELECT linecnt,impression,finclick,finprice," +
				"alipay_direct_num,alipay_direct_amt," +
				"alipay_indirect_num,alipay_indirect_amt " +
				" FROM adhoc_metric" +
				" WHERE thedate=?" +
				" AND type=?" +
				" AND tablename=?";
		PreparedStatement pstmt;
		Metric metric = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, thedate);
			pstmt.setInt(2, type);
			pstmt.setString(3, tablename);
			ResultSet rs = pstmt.executeQuery();
			metric = new Metric();
			if(rs.next()){
				metric.setThedate(thedate);
				metric.setType(type);
				metric.setTablename(tablename);
				metric.setLineCnt(rs.getLong(1));
				metric.setImpression(rs.getLong(2));
				metric.setFinClick(rs.getLong(3));
				metric.setFinPrice(rs.getFloat(4));
				metric.setAlipayDirectNum(rs.getLong(5));
				metric.setAlipayDirectAmt(rs.getFloat(6));
				metric.setAlipayIndirectNum(rs.getLong(7));
				metric.setAlipayIndirectAmt(rs.getFloat(8));
			} else {
				return null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when get Metric"
					+ "thedate: " + thedate 
					+ "type: " + type
					+ "tablename: " + tablename);
			e.printStackTrace();
		}
		
		return metric;
	}
	
	public List<String> getRecentDays(String tableName, String fieldName, int n) {
		List<String> days = null;
		String sql = "SELECT DISTINCT %s" +
				" FROM %s" +
				" ORDER BY %s" +
				" DESC LIMIT %d";
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(sql, fieldName,tableName,fieldName,n));
			days = new ArrayList<String>();
			while(rs.next()) {	
				days.add(rs.getString(1));
			} 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when get recent days");
			e.printStackTrace();
		}
		return days;
	}
	
	public void close() {
		if(conn != null) {
			try {
				if(! conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
						+ "Error when close Connection");
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Metric metric = new Metric();
//		metric.setThedate("20130226");
//		metric.setType(0);
//		metric.setTablename("auction");
//		metric.setLineCnt(10);
//		metric.setImpression(100);
//		metric.setFinClick(200);
//		metric.setFinPrice((float) 200.1);
//		metric.setAlipayDirectNum(300);
//		metric.setAlipayDirectAmt((float) 300.1);
//		metric.setAlipayIndirectNum(400);
//		metric.setAlipayIndirectAmt((float) 400.1);
		MetricService service = new MetricService();
////		service.insert(metric);
//		metric = service.getMetric("20130226", 0, "auction");
//		System.out.println(metric);
		List<String> days = service.getRecentDays("adhoc_metric","thedate",5);
		for(String day : days)
			System.out.println(day);	
		
	}
	

}
