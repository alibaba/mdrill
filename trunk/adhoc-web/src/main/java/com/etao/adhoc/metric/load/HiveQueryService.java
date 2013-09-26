package com.etao.adhoc.metric.load;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.etao.adhoc.common.util.YamlUtils;
import com.etao.adhoc.metric.Metric;

/**
 * mvn install:install-file -Dfile=/home/lingning/yannian.mu/adhoc-metric/lib/com/alipay/higo/bluewhale-jdbc/1.0.2/bluewhale-jdbc-1.0.2.jar -DgroupId=com.alipay.higo -DartifactId=bluewhale-jdbc -Dversion=1.0.2 -Dpackaging=jar

mvn install:install-file -Dfile=/home/lingning/yannian.mu/adhoc-metric/lib/com/alipay/higo/bluewhale-higo/1.0.2/bluewhale-higo-1.0.2.jar -DgroupId=com.alipay.higo -DartifactId=bluewhale-higo -Dversion=1.0.2 -Dpackaging=jar
mvn clean package
 * @author yannian.mu
 *
 */
public class HiveQueryService implements QueryService {
	private Connection conn ;
	private Statement stmt;
	private static String sqlFormat = "select thedate,count(1)%s " +
			"from %s where %s group by thedate limit 1";
	private static Map<String, String> TABLE_NAME_MAP;
	private static Map<String, List<String>> TABLE_COLUMNS;
	private static Set<String> DT_SET;
	private static Set<String> PT_SET;
	static {
		TABLE_NAME_MAP = new HashMap<String, String>();
		TABLE_NAME_MAP.put("auction", "rpt_p4padhoc_auction");
		TABLE_NAME_MAP.put("cust", "rpt_p4padhoc_cust");
		TABLE_NAME_MAP.put("product", "rpt_p4padhoc_product");
		TABLE_NAME_MAP.put("b2bad", "rpt_b2bad_hoc_memb_sum_d");
		TABLE_NAME_MAP.put("cpsitem", "r_rpt_cps_luna_item");
		TABLE_NAME_MAP.put("cpspid", "r_rpt_cps_adhoc_pid");
		TABLE_NAME_MAP.put("cpsseller", "r_rpt_cps_adhoc_seller");
		TABLE_NAME_MAP.put("rpt_seller_all_m", "rpt_seller_all_m");
		TABLE_NAME_MAP.put("fact_seller_all_d", "fact_seller_all_d");
		TABLE_NAME_MAP.put("r_rpt_cps_adhoc_payment", "r_rpt_cps_adhoc_payment");
		
		TABLE_COLUMNS = new HashMap<String, List<String>>();
		
		List<String> r_rpt_cps_adhoc_payment = new ArrayList<String>();
		r_rpt_cps_adhoc_payment.add("uv");
		r_rpt_cps_adhoc_payment.add("gmv_cnt");
		r_rpt_cps_adhoc_payment.add("fixed_gmv_outshop_amt");
		r_rpt_cps_adhoc_payment.add("gc_shop_alipay_amt");
		r_rpt_cps_adhoc_payment.add("p4p_e_gmv_direct_amt");
		r_rpt_cps_adhoc_payment.add("gc_total_gmv_num");
		r_rpt_cps_adhoc_payment.add("p4p_e_alipay_outshop_amt");
		TABLE_COLUMNS.put("r_rpt_cps_adhoc_payment", r_rpt_cps_adhoc_payment);
		
		List<String> fact_seller_all_d = new ArrayList<String>();
		fact_seller_all_d.add("uv");
		fact_seller_all_d.add("gmv_cnt");
		fact_seller_all_d.add("fixed_gmv_outshop_amt");
		fact_seller_all_d.add("gc_shop_alipay_amt");
		fact_seller_all_d.add("p4p_e_gmv_direct_amt");
		fact_seller_all_d.add("gc_total_gmv_num");
		fact_seller_all_d.add("p4p_e_alipay_outshop_amt");
		TABLE_COLUMNS.put("fact_seller_all_d", fact_seller_all_d);
		
		List<String> rpt_seller_all_m = new ArrayList<String>();
		rpt_seller_all_m.add("cps_total_ipv");
		rpt_seller_all_m.add("cps_alimm_amt");
		rpt_seller_all_m.add("fixed_alipay_total_amt_3");
		rpt_seller_all_m.add("trade_alipay_amt");
		rpt_seller_all_m.add("b2c_trade_amt");
		rpt_seller_all_m.add("cpt_click");
		rpt_seller_all_m.add("bid_click");
		TABLE_COLUMNS.put("rpt_seller_all_m", rpt_seller_all_m);
		
		List<String> cpsitemColumns = new ArrayList<String>();
		cpsitemColumns.add("landing_pv");
		cpsitemColumns.add("shop_ipv");
		cpsitemColumns.add("cm_direct_commision_amt");
		cpsitemColumns.add("cm_direct_alipay_num");
		cpsitemColumns.add("cm_direct_alipay_amt");
		cpsitemColumns.add("cm_direct_settle_amt");
		cpsitemColumns.add("cm_direct_settle_num");
		TABLE_COLUMNS.put("cpsitem", cpsitemColumns);
		
		List<String> cpspidColumns = new ArrayList<String>();
		cpspidColumns.add("pv");
		cpspidColumns.add("s_click");
		cpspidColumns.add("cm_settle_num");
		cpspidColumns.add("alipay_direct_num");
		cpspidColumns.add("alipay_direct_amt");
		cpspidColumns.add("alipay_indirect_num");
		cpspidColumns.add("alipay_indirect_amt");
		TABLE_COLUMNS.put("cpspid", cpspidColumns);
		
		List<String> cpssellerColumns = new ArrayList<String>();
		cpssellerColumns.add("shop_ipv");
		cpssellerColumns.add("landing_pv");
		cpssellerColumns.add("landing_uv");
		cpssellerColumns.add("cm_direct_settle_amt");
		cpssellerColumns.add("cm_direct_commision_amt");
		cpssellerColumns.add("cm_direct_alipay_commision_amt");
		cpssellerColumns.add("cm_indirect_alipay_num");
		TABLE_COLUMNS.put("cpsseller", cpssellerColumns);
		
		List<String> auctionColumns = new ArrayList<String>();
		auctionColumns.add("impression");
		auctionColumns.add("finclick");
		auctionColumns.add("finprice");
		auctionColumns.add("e_alipay_direct_cnt");
		auctionColumns.add("e_alipay_direct_amt");
		auctionColumns.add("e_alipay_indirect_cnt");
		auctionColumns.add("e_alipay_indirect_amt");
		TABLE_COLUMNS.put("auction", auctionColumns);
		
		List<String> custColumns = new ArrayList<String>();
		custColumns.add("impression");
		custColumns.add("finclick");
		custColumns.add("finprice");
		custColumns.add("alipay_direct_num");
		custColumns.add("alipay_direct_amt");
		custColumns.add("alipay_indirect_num");
		custColumns.add("alipay_indirect_amt");
		TABLE_COLUMNS.put("cust", custColumns);

		List<String> productColumns = new ArrayList<String>();
		productColumns.add("impression");
		productColumns.add("finclick");
		productColumns.add("finprice");
		productColumns.add("e_alipay_direct_cnt");
		productColumns.add("e_alipay_direct_amt");
		productColumns.add("e_alipay_indirect_cnt");
		productColumns.add("e_alipay_indirect_amt");
		TABLE_COLUMNS.put("product", productColumns);
		
		
		List<String> b2badColumns = new ArrayList<String>();
		b2badColumns.add("adver_cnt_1d_001");
		b2badColumns.add("p4p_click_cnt_1d_002");
		b2badColumns.add("p4p_cost_amt_1d_006");
		b2badColumns.add("pay_ord_cnt_1d_004");
		b2badColumns.add("pay_ord_amt_1d_004");
		b2badColumns.add("0");
		b2badColumns.add("0");
		TABLE_COLUMNS.put("b2bad", b2badColumns);
		
		DT_SET = new HashSet<String>();
		DT_SET.add("auction");
		DT_SET.add("cust");
		DT_SET.add("product");
		DT_SET.add("cpsitem");
		DT_SET.add("cpspid");
		DT_SET.add("cpsseller");
		DT_SET.add("fact_seller_all_d");
		DT_SET.add("rpt_seller_all_m");
		DT_SET.add("r_rpt_cps_adhoc_payment");
		
		PT_SET = new HashSet<String>();
		PT_SET.add("b2bad");
	}
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public HiveQueryService(Map conf) {
		String url = (String) conf.get("hive.conn.str");
		String username = (String)conf.get("hive.username");
		String password = (String)conf.get("hive.password");
		try {
			Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
			conn = DriverManager.getConnection(url, username, password);
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Can not find class: org.apache.hadoop.hive.jdbc.HiveDriver");
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when building HIVE Connection");
			e.printStackTrace();
		}
		
	}
	
	public Metric getMetric(String tablename, String thedate) {
		Metric metric = null;
		String sql = String.format(sqlFormat,
				getColumnsQuery(tablename),
				 TABLE_NAME_MAP.get(tablename), getPartitionValue(tablename,thedate));
		System.out.println("Hive SQL: " + sql);
		ResultSet rs;
		try {
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				metric = new Metric();
				metric.setThedate(thedate);
				metric.setType(0);
				metric.setTablename(tablename);
				metric.setLineCnt(rs.getLong(2));
				metric.setImpression(rs.getLong(3));
				metric.setFinClick(rs.getLong(4));
				metric.setFinPrice(rs.getFloat(5));
				metric.setAlipayDirectNum(rs.getLong(6));
				metric.setAlipayDirectAmt(rs.getFloat(7));
				metric.setAlipayIndirectNum(rs.getLong(8));
				metric.setAlipayIndirectAmt(rs.getFloat(9));
			} 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
					+ "Error when execute HIVE SQL: "
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

	private static String getColumnsQuery(String tablename){
		List<String> columns = TABLE_COLUMNS.get(tablename);
		StringBuilder sb = new StringBuilder();
		if(columns != null){
			for(String col : columns ){
				sb.append(",sum(" + col + ")");
			}
		}
		return sb.toString();
	}
	
	private static String getPartitionValue(String tablename, String thedate) {
		if(DT_SET.contains(tablename)){
			return "dt=" + thedate;
		}else if(PT_SET.contains(tablename)){
			return "pt=" + thedate + "000000";
		}
		return null;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Map conf = YamlUtils.getConfigFromYamlFile("adhoc-metric.yaml");
		HiveQueryService hive = new HiveQueryService(conf);
		Metric metric = hive.getMetric("auction", args[0]);
		hive.close();
		System.out.println(metric);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "HIVE";
	}

}
