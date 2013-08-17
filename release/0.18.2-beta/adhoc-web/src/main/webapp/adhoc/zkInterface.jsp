<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.io.*" %><%@ page import="javax.servlet.*" %><%@ page import="java.net.*" %><%@ page import="java.util.*" %><%@ page import="java.util.regex.*" %><%@ page import="java.text.*" %><%@ page import="com.alimama.web.*" %><%
<%
          String varout = request.getParameter("callback");
          String varoutstr="";
          String varoutstrend="";
          if(varout!=null&&!varout.equals(""))
          {
                varoutstr=varout+"(";
                varoutstrend=")";
          }
                String type = request.getParameter("type");
    String key = request.getParameter("key");
    String data = request.getParameter("data");

    String rtnstr="{code:0,message:\"need params\"}";
                if(type!=null && key!=null && !type.equals("") && !key.equals("")){
                        if(type.equals("del"))
                        {
                                rtnstr=TableJoin.del(key);
                        }

                        if(type.equals("get"))
                        {
                                rtnstr=TableJoin.get(key);
                        }

                        if(type.equals("set"))//&&data!=null&&!data.equals(""))
                        {
                                rtnstr=TableJoin.set(key,data);
                        }
                }
                ;


%><%=varoutstr%><%=rtnstr%><%=varoutstrend%>
