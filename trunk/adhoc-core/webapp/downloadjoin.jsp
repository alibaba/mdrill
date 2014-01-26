<%@ page contentType="application/vnd.ms-csv; charset=gbk" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.web.*" %><%
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Origin","http://localhost,http://adhoc.etao.com:9999");
	request.setCharacterEncoding("utf-8");
	%>
	 <%! 
public String getIpAddr(HttpServletRequest request) {
        String ip = String.valueOf(request.getHeader("X-Forwarded-For"))
        + String.valueOf(request.getHeader("Proxy-Client-IP"))+
         String.valueOf(request.getHeader("WL-Proxy-Client-IP"))+
         String.valueOf(request.getHeader("HTTP_CLIENT_IP"))+
         String.valueOf(request.getHeader("HTTP_X_FORWARDED_FOR"))
        + String.valueOf(request.getRemoteAddr());
        
        
          Cookie[] cookies = request.getCookies(); 
			  for(int i=0;i<cookies.length;i++){
			      if (cookies[i].getValue().indexOf("ip")>=0||cookies[i].getName().indexOf("ip")>=0){
			          ip =ip+ ","+cookies[i].getValue();
			      }     
			  }
        
        return ip;
    }
	%>
	<%	

	String uuid = request.getParameter("uuid");
	String did=TableJoin.getDownloadId(uuid);

	
		response.setHeader("Content-disposition","attachment; filename="+new String( new String(did).getBytes("utf-8"), "ISO8859-1" ) +"");

    String strip=getIpAddr(request);
    if(!(strip.matches(".*172.24.*")||strip.matches(".*10.246.173.*")||strip.matches(".*10.99.*")||strip.matches(".*10.194.*")))
    {
    
    
		%>

			请进入七星阵下载<br>
		 七星阵使用指南: http://twiki.corp.taobao.com/bin/view/SRE/Taobao_Security/VmSecDomainWhiteBook?spm=0.0.0.0.ySYMil
			
		<%   
		
		} else { 	
    	
	
	out.clear();
  out = pageContext.pushBody();
	OutputStreamWriter outStream = new OutputStreamWriter(response.getOutputStream(),"gbk"); 
	TableJoin.readJoinResult(uuid,outStream);
	outStream.flush();
  outStream.close();
  
  }
%>
