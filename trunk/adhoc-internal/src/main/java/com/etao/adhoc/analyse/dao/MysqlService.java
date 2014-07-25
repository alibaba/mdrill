package com.etao.adhoc.analyse.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.etao.adhoc.analyse.common.util.YamlUtils;
import com.etao.adhoc.analyse.vo.DayUserPv;
import com.etao.adhoc.analyse.vo.ModuleInfo;
import com.etao.adhoc.analyse.vo.QueryLog;
import com.etao.adhoc.analyse.vo.StartDay;
import com.etao.adhoc.analyse.vo.TotalUserPv;


public class MysqlService {
	private static Logger LOG = Logger.getLogger(MysqlService.class);

	private static final String JDBC_DRIVER = "org.gjt.mm.mysql.Driver";
	Map conf; 
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String url =null;
	String username = null;
	String password = null;
	//jps初始化问题
	public MysqlService()  {
		try {
			conf = YamlUtils.getConfigFromYamlFile("query-analyser.yaml");
			this.url = (String) conf.get("mysql.url");
			this.username = (String) conf.get("mysql.username");
			this.password = (String) conf.get("mysql.password");
			Class.forName(JDBC_DRIVER);

		} catch (Throwable e) {
			LOG.error("error",e);

		} 
		
	}
	public void insertQueryLog(QueryLog queryLog) {
		if(isValid(queryLog)){
			String sql = "INSERT INTO query_log" +
					"(query_date,nick,email,set_name,dimvalue,filter,bizdate)" +
					" VALUES(?,?,?,?,?,?,?) ";
			PreparedStatement pstmt=null;
			try {

				Connection conn = DriverManager.getConnection(url, username, password);

				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, new java.sql.Timestamp(queryLog.getDate().getTime())); 
				pstmt.setString(2, queryLog.getNick());
				pstmt.setString(3, queryLog.getEmail());
				pstmt.setString(4, queryLog.getSetName());
				pstmt.setString(5, queryLog.getDimvalue());
				pstmt.setString(6, queryLog.getFilter());
				pstmt.setString(7, queryLog.getBizdate());
				pstmt.executeUpdate();
				LOG.info(pstmt.toString());
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				String debugsql="";
				if(pstmt!=null)
				{
					debugsql=pstmt.toString();
				}
				LOG.error(debugsql,e);
			}
		}
	}
	private boolean isValid(QueryLog queryLog){
		if(queryLog.getNick().length() > 3)
			return false;
		return true;
	}
	public ModuleInfo getModuleInfo(String queryDay, String moduleName) {
		String sql = "SELECT query_cnt,uv FROM module_info" +
				" WHERE queryday=? " +
				" and module_name=? ";
		PreparedStatement pstmt=null;
		ModuleInfo moduleInfo = null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, queryDay); 
			pstmt.setString(2, moduleName);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				moduleInfo = new ModuleInfo();
				moduleInfo.setQueryDay(queryDay);
				moduleInfo.setModuleName(moduleName);
				moduleInfo.setQueryCnt(rs.getInt(1));
				moduleInfo.setUv(rs.getInt(2));

			}
			LOG.info(pstmt.toString());
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			String debugsql="";
			if(pstmt!=null)
			{
				debugsql=pstmt.toString();
			}
			LOG.error(debugsql,e);
		}
		return moduleInfo;
	}
	
	public ModuleInfo[] getModuleInfos(String queryDay) {
		String sql = "SELECT sum(query_cnt) as query_cnt,sum(uv) as uv,module_name,nicklist FROM module_info" +
				" WHERE queryday=? " +
				" group by module_name order by module_name ";
		PreparedStatement pstmt=null;
		ArrayList<ModuleInfo> list=new ArrayList<ModuleInfo>();
		try {
			Connection conn = DriverManager.getConnection(url, username, password);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, queryDay); 
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				ModuleInfo moduleInfo = new ModuleInfo();
				moduleInfo.setQueryDay(queryDay);
				moduleInfo.setQueryCnt(rs.getInt(1));
				moduleInfo.setUv(rs.getInt(2));
				moduleInfo.setModuleName(rs.getString(3));
				moduleInfo.setNicklist(rs.getString(4));
				list.add(moduleInfo);

			}
			LOG.info(pstmt.toString());
			pstmt.close();
			conn.close();

		} catch (SQLException e) {
			String debugsql="";
			if(pstmt!=null)
			{
				debugsql=pstmt.toString();
			}
			LOG.error(debugsql,e);
		}
		return list.toArray(new ModuleInfo[list.size()]);
	}
	public StartDay getStartDay(){
		String sql = "SELECT startday FROM start_day";
		StartDay startDay = null;
		Statement stmt=null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(rs.next()){
				startDay = new StartDay();
				startDay.setStartDay(rs.getString(1));
			}
			LOG.info(stmt.toString());
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			String debugsql="";
			if(stmt!=null)
			{
				debugsql=stmt.toString();
			}
			LOG.error(debugsql,e);
		}
		return startDay;
	}
	public List<TotalUserPv> getTotalTopUsers(int length) {
		String sql = "select a.nick as nick,a.query_cnt as query_cnt, b.department as department" +
				" from (" +
				"SELECT nick,query_cnt FROM total_user_pv" +
				" ORDER BY query_cnt" +
				" DESC LIMIT ?" +
				") a left join user_info b on (a.nick=b.nick) ORDER BY a.query_cnt desc";
		List<TotalUserPv> totalUserPvList = null;
		PreparedStatement pstmt=null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, length); 
			ResultSet rs = pstmt.executeQuery();
			totalUserPvList = new ArrayList<TotalUserPv>();
			while(rs.next()){
				TotalUserPv totalUserPv = new TotalUserPv();
				totalUserPv.setNick(rs.getString(1));
				totalUserPv.setQueryCnt(rs.getInt(2));
				totalUserPv.setDepartment(String.valueOf(rs.getString(3)));
				totalUserPvList.add(totalUserPv);
			}
			LOG.info(pstmt.toString());
			pstmt.close();
			conn.close();

		} catch (SQLException e) {
			String debugsql="";
			if(pstmt!=null)
			{
				debugsql=pstmt.toString();
			}
			LOG.error(debugsql,e);
		}
		return totalUserPvList;
	}
	public List<DayUserPv> getDayTopUsers(String queryDay,int length) {
		String sql = "select a.nick as nick,a.query_cnt as query_cnt, b.department as department" +
				" from (" +
				"" +
				"SELECT nick,query_cnt FROM day_user_pv " +
				" WHERE queryday = ?" +
				" ORDER BY query_cnt" +
				" DESC LIMIT ?" +
				"" +
				") a left join user_info b on (a.nick=b.nick) ORDER BY a.query_cnt desc";
		List<DayUserPv> dayUserPvList = null;
		PreparedStatement pstmt=null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, queryDay);
			pstmt.setInt(2, length); 
			ResultSet rs = pstmt.executeQuery();
			dayUserPvList = new ArrayList<DayUserPv>();
			while(rs.next()){
				DayUserPv dayUserPv = new DayUserPv();
				dayUserPv.setQueryDay(queryDay);
				dayUserPv.setNick(rs.getString(1));
				dayUserPv.setQueryCnt(rs.getInt(2));
				dayUserPv.setDepartment(String.valueOf(rs.getString(3)));
				dayUserPvList.add(dayUserPv);
			}
			LOG.info(pstmt.toString());
			pstmt.close();
			conn.close();

		} catch (SQLException e) {
			String debugsql="";
			if(pstmt!=null)
			{
				debugsql=pstmt.toString();
			}
			LOG.error(debugsql,e);
		}
		return dayUserPvList;
	}
	public List<DayUserPv> getUserPV(String nick) {
		String sql = "SELECT queryday,query_cnt FROM day_user_pv " +
				" WHERE nick = ?" +
				" ORDER BY queryday" +
				" DESC LIMIT ?";
		PreparedStatement pstmt=null;
		List<DayUserPv> dayUserPvList = null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, nick);
			pstmt.setInt(2, 100); 
			ResultSet rs = pstmt.executeQuery();
			dayUserPvList = new ArrayList<DayUserPv>();
			while(rs.next()){
				DayUserPv dayUserPv = new DayUserPv();
				dayUserPv.setQueryDay(rs.getString(1));
				dayUserPv.setNick(nick);
				dayUserPv.setQueryCnt(rs.getInt(2));
				dayUserPvList.add(dayUserPv);
			}
			LOG.info(pstmt.toString());
			pstmt.close();
			conn.close();

		} catch (SQLException e) {
			String debugsql="";
			if(pstmt!=null)
			{
				debugsql=pstmt.toString();
			}
			LOG.error(debugsql,e);
		}
		return dayUserPvList;
	}
	private static String mysqlDayFormat = "%Y%m%d";
	public void calModuleInfo(String queryDay){
		String sqldelete = "DELETE FROM module_info WHERE queryday='"+queryDay+"'";
		
		String sql = "INSERT INTO module_info(queryday,module_name,query_cnt,uv,nicklist) " +
		        "select ? AS queryday,set_name,sum(query_cnt) AS query_cnt,COUNT(distinct nick) AS uv,GROUP_CONCAT(CONCAT(nick,':',query_cnt)  order by query_cnt desc)  from ( "+
				" SELECT  set_name,nick ,COUNT(*) AS query_cnt " +
				" FROM query_log " +
				" WHERE DATE_FORMAT(query_date,?)=? " +
				" AND nick NOT IN (SELECT nick FROM dev_nicks) " +
				" GROUP BY set_name,nick ) a GROUP BY queryday,set_name" +
				"";
		PreparedStatement pstmt=null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sqldelete);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, queryDay); 
			pstmt.setString(2, mysqlDayFormat);
			pstmt.setString(3, queryDay); 
			pstmt.executeUpdate();
			LOG.info(pstmt.toString());
			pstmt.close();
			conn.close();

		} catch (SQLException e) {
			String debugsql="";
			if(pstmt!=null)
			{
				debugsql=pstmt.toString();
			}
			LOG.error(debugsql,e);
		}
	}
	
	
	public void calDayUserPv(String queryDay) {
		String sqldelete = "DELETE FROM day_user_pv WHERE queryday='"+queryDay+"'";

		String sql = "INSERT INTO day_user_pv(queryday,nick,query_cnt)" +
				" SELECT ? AS queryday, nick ,COUNT(*) AS query_cnt" +
				" FROM query_log " +
				" WHERE DATE_FORMAT(query_date,?)=?" +
				//" AND nick NOT IN (SELECT nick FROM dev_nicks) " +
				" GROUP BY queryday,nick " ;
		PreparedStatement pstmt=null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sqldelete);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, queryDay); 
			pstmt.setString(2, mysqlDayFormat);
			pstmt.setString(3, queryDay); 
			pstmt.executeUpdate();
			LOG.info(pstmt.toString());
			pstmt.close();
			conn.close();

		} catch (SQLException e) {
			String debugsql="";
			if(pstmt!=null)
			{
				debugsql=pstmt.toString();
			}
			LOG.error(debugsql,e);
		}
	}
	public void calTotalUserPv() {
		String sql = "DELETE FROM total_user_pv WHERE 1=1";
		Statement stmt=null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			sql = "INSERT INTO total_user_pv (nick, query_cnt )" +
					" SELECT nick, SUM(query_cnt)" +
					" FROM day_user_pv"  +
					" GROUP BY nick";
			stmt.executeUpdate(sql);
			LOG.info(stmt.toString());
			stmt.close();
			conn.close();

		} catch (SQLException e) {
			String debugsql="";
			if(stmt!=null)
			{
				debugsql=stmt.toString();
			}
			LOG.error(debugsql,e);
		}
		
	}
	
	public List<String> getRecentDays(String tableName, String fieldName, int n) {
		List<String> days = null;
		String sql = "SELECT DISTINCT %s" +
				" FROM %s" +
				" ORDER BY %s" +
				" DESC LIMIT %d";
		Statement stmt=null;
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(sql, fieldName,tableName,fieldName,n));
			days = new ArrayList<String>();
			while(rs.next()) {	
				days.add(rs.getString(1));
			} 
			LOG.info(stmt.toString());
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			String debugsql="";
			if(stmt!=null)
			{
				debugsql=stmt.toString();
			}
			LOG.error(debugsql,e);
		}
		return days;
	}
	
	public void close() {
	}
	
	public static void main(String[] args) {
		MysqlService server = new MysqlService();
		//server.calTotalUserPv();
		List<TotalUserPv> list = server.getTotalTopUsers(Integer.parseInt(args[0]));
		for(TotalUserPv pv : list)
			System.out.println(pv);
		List<DayUserPv>list2 = server.getUserPV(args[1]);
		for(DayUserPv pv : list2)
			System.out.println(pv);
		List<String> days = server.getRecentDays("module_info","queryday",5);
		for(String day : days)
			System.out.println(day);	
		System.out.println("=======");
		days = server.getRecentDays("day_user_pv","queryday",5);
		for(String day : days)
			System.out.println(day);	
	}
}
