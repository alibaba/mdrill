package com.etao.adhoc.metric.load;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.alimama.mdrill.jdbc.MdrillQueryResultSet;
import com.etao.adhoc.metric.Metric;
/**
 * mvn install:install-file -Dfile=/home/lingning/yannian.mu/adhoc-metric/lib/com/alipay/higo/bluewhale-jdbc/1.0.2/bluewhale-jdbc-1.0.2.jar -DgroupId=com.alipay.higo -DartifactId=bluewhale-jdbc -Dversion=1.0.2 -Dpackaging=jar

mvn install:install-file -Dfile=/home/lingning/yannian.mu/adhoc-metric/lib/com/alipay/higo/bluewhale-higo/1.0.2/bluewhale-higo-1.0.2.jar -DgroupId=com.alipay.higo -DartifactId=bluewhale-higo -Dversion=1.0.2 -Dpackaging=jar
mvn -DskipTests clean package
 * @author yannian.mu
 *
 */
public class HigoQueryService implements QueryService {
	private Connection conn ;
	private Statement stmt;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Map conf;
	public HigoQueryService(Map conf) {
		this.conf=conf;
		String url = (String) conf.get("metric.higo.conn.str");
		try {
			Class.forName("com.alimama.mdrill.jdbc.MdrillDriver");
			conn = DriverManager.getConnection(url, "", "");
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Can not find class: com.alimama.mdrill.jdbc.MdrillDriver");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when building Higo Connection");
			e.printStackTrace();
		}
		
	}
	public Metric getMetric(String tablename, String thedate) throws IOException{
		String sqlFormat=(String) conf.get("adhoc.metric.mdrill.sql."+tablename);
		Metric metric = null;
		String sql = String.format(sqlFormat, thedate);
		System.out.println("Higo SQL:" + sql);
		MdrillQueryResultSet rs;
		try {
			rs = (MdrillQueryResultSet) stmt.executeQuery(sql);
			if(rs.next()){
				metric = new Metric();
				metric.setThedate(thedate);
				metric.setType(1);
				metric.setTablename(tablename);
				metric.setLineCnt((long)Float.parseFloat(rs.getString(1)));
				metric.setImpression((long)Float.parseFloat(rs.getString(2)));
				metric.setFinClick((long)Float.parseFloat(rs.getString(3)));
				metric.setFinPrice(Float.parseFloat(rs.getString(4)));
				metric.setAlipayDirectNum((long)Float.parseFloat(rs.getString(5)));
				metric.setAlipayDirectAmt(Float.parseFloat(rs.getString(6)));
				metric.setAlipayIndirectNum((long)Float.parseFloat(rs.getString(7)));
				metric.setAlipayIndirectAmt(Float.parseFloat(rs.getString(8)));
			} 
		} catch (SQLException e) {
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when execute Higo SQL: "
					+ sql);
			e.printStackTrace();
		}
		
		return metric;
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


	public String getName() {
		return "HIGO";
	}
	
	

}
