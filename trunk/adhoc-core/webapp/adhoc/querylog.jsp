<%@ page contentType="text/javascript; charset=UTF-8" %>
<%@ page info="收集ad浩克前端传来的查询记录，写到本地文件中" %>
<%@ page import="java.io.*" %>
<%@ page import="java.lang.reflect.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.etao.adhoc.analyse.vo.*" %>
<%@ page import="com.etao.adhoc.analyse.dao.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.etao.adhoc.analyse.common.util.YamlUtils" %>
    <%
        request.setCharacterEncoding("utf-8");
        String queryTime =request.getParameter("date");
        String nick =request.getParameter("nick");
        String email =request.getParameter("email") ;
        String set =request.getParameter("set");
        String dimValue =request.getParameter("dimvalue") ;
        String filter =request.getParameter("dimvalue") ;
        String bizdate = request.getParameter("bizdate");
 String callback = request.getParameter("callback");        

        String CTRL_A = "\u0001";
        SimpleDateFormat fromdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat todf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date thedate = fromdf.parse(queryTime);
        queryTime = todf.format(thedate);
        String logInfo = queryTime + CTRL_A + nick + CTRL_A + email + CTRL_A 
                + set + CTRL_A + dimValue + CTRL_A + filter + CTRL_A + bizdate;
        
        SimpleDateFormat logdf = new SimpleDateFormat("yyyyMMdd");
        String  logFileName = request.getRealPath("logs/" + logdf.format(new Date()) + ".log");
        File logFile = new File(logFileName);
        String lineEnd = "\n";
        boolean isNewFile = false;
        if(!logFile.exists()) {
            logFile.createNewFile();
            isNewFile = true;
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(logFile,true);
            if(!isNewFile)
                fw.write(lineEnd);
            fw.write(logInfo);
            fw.close();
        } catch(IOException e) {
            out.println(e.getMessage());
        } finally {
            if(fw != null)
                fw.close();
        }
	//写入mysql
	//MysqlService mysqlService = new MysqlService();
	QueryLog queryLog = new QueryLog();
	queryLog.setDate(thedate);
	queryLog.setNick(nick);
	queryLog.setEmail(email);
	queryLog.setSetName(set);
	queryLog.setDimvalue(dimValue);
	queryLog.setFilter(filter);
	queryLog.setBizdate(bizdate);
	//mysqlService.insertQueryLog(queryLog);	
	//mysqlService.close();
	
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	Connection conn;
	Map conf; 
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	conf = YamlUtils.getConfigFromYamlFile("query-analyser.yaml");
	String url = (String) conf.get("mysql.url");
	String username = (String) conf.get("mysql.username");
	String password = (String) conf.get("mysql.password");
	Class.forName(JDBC_DRIVER);
	conn = DriverManager.getConnection(url, username, password);

		String sql = "INSERT INTO query_log" +
					"(query_date,nick,email,set_name,dimvalue,filter,bizdate)" +
					" VALUES(?,?,?,?,?,?,?) ";
			PreparedStatement pstmt;
		
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, new java.sql.Timestamp(queryLog.getDate().getTime())); 
				pstmt.setString(2, queryLog.getNick());
				pstmt.setString(3, queryLog.getEmail());
				pstmt.setString(4, queryLog.getSetName());
				pstmt.setString(5, queryLog.getDimvalue());
				pstmt.setString(6, queryLog.getFilter());
				pstmt.setString(7, queryLog.getBizdate());
				pstmt.executeUpdate();
String strsql=pstmt.toString();
if(! conn.isClosed())
					conn.close();
    %>
<%=callback%>({code:1})

