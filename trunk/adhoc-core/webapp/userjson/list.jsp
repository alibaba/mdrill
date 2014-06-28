<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.alimama.mdrill.ui.service.*" %>
<%@ page import="com.alimama.mdrill.json.*" %>
<%!

public String  getMD5(byte[] bytes) { 
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }; 
        char str[] = new char[16 * 2]; 
        try { 
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5"); 
            md.update(bytes); 
            byte tmp[] = md.digest(); 
            int k = 0; 
            for (int i = 0; i < 16; i++) { 
                byte byte0 = tmp[i]; 
                str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
                str[k++] = hexDigits[byte0 & 0xf]; 
            } 
        } catch (Exception e) { 
        } 
        return new String(str); 
    } ;
    
     
  
  public String  getMD5(String value) { 
        String result = ""; 
        try { 
            result = getMD5(value.getBytes("UTF-8")); 
        } catch (Exception e) { 
        } 
        return result; 
    } ;
 
 
 public static String addJsonHeader() throws Exception
	{

		StringBuffer buff=new StringBuffer();
		buff.append("<tr><td>");
		buff.append("<input type=\"text\" disabled=\"disabled\" name=\"userid_aaa\" value=\"登陆ID\" />");
		buff.append("<input type=\"text\" disabled=\"disabled\" name=\"email\" value=\"邮箱\" />");
		buff.append("<input type=\"text\" disabled=\"disabled\" name=\"cname\" value=\"花名\" />");
		buff.append("<input type=\"text\" disabled=\"disabled\" size=\"4\" name=\"role\" value=\"角色\" />");
		buff.append("<input type=\"text\" disabled=\"disabled\" name=\"permission\" value=\"表权限\" />");
		buff.append("<input disabled=\"disabled\" type=\"submit\" value=\"------\"  />");
		buff.append("</td><td>");
		buff.append("<input disabled=\"disabled\" type=\"submit\" value=\"---\"  />");
		buff.append("<input type=\"text\" disabled=\"disabled\" size=\"8\" disabled=\"disabled\" name=\"queryday\" value=\"最后现身\" />");
		buff.append("<input type=\"text\" disabled=\"disabled\" size=\"10\" disabled=\"disabled\" name=\"opuser\" value=\"管理员\" />");
    buff.append("<input type=\"text\" disabled=\"disabled\" size=\"20\" disabled=\"disabled\" name=\"optime\" value=\"更新时间\" />");

		buff.append("</td></tr>");

		return buff.toString();
		
	};
public static String addjson(String json) throws Exception
	{
		JSONObject jsonObj = new JSONObject(json);

		StringBuffer buff=new StringBuffer();
		buff.append("<tr><td><form action=\"./list.jsp\" method=\"post\"  onsubmit=\"return formsubmit(this)\" >");
		buff.append("<input type=\"hidden\"  name=\"userid\" value=\""+jsonObj.getString("userid")+"\" />");
		buff.append("<input type=\"text\" disabled=\"disabled\" name=\"userid_aaa\" value=\""+jsonObj.getString("userid")+"\" />");
		buff.append("<input type=\"text\" name=\"email\" value=\""+jsonObj.getString("email")+"\" />");
		buff.append("<input type=\"text\" name=\"cname\" value=\""+jsonObj.getString("cname")+"\" />");
		buff.append("<input type=\"text\" size=\"4\" name=\"role\" value=\""+String.valueOf(jsonObj.getInt("role"))+"\" />");
		buff.append("<input type=\"text\" name=\"permission\" value=\""+jsonObj.getJSONArray("permission").toString()+"\" />");
		buff.append("<input type=\"hidden\" name=\"type\" value=\"add\" />");
		buff.append("<input type=\"submit\" value=\"update\"  />");
		buff.append("</form>");
		buff.append("</td><td><form action=\"./list.jsp\" method=\"post\"  onsubmit=\"return formsubmit(this)\">");
		buff.append("<input type=\"hidden\"  name=\"userid\" value=\""+jsonObj.getString("userid")+"\" />");
		buff.append("<input type=\"hidden\" name=\"type\" value=\"del\" />");
		buff.append("<input type=\"submit\" value=\"del\"  />");
		buff.append("<input type=\"text\" size=\"8\" disabled=\"disabled\" name=\"queryday\" value=\""+jsonObj.getString("queryday")+"\" />");
		buff.append("<input type=\"text\" size=\"10\" disabled=\"disabled\" name=\"opuser\" value=\""+jsonObj.getString("opuser")+"\" />");
    buff.append("<input type=\"text\" size=\"20\" disabled=\"disabled\" name=\"optime\" value=\""+jsonObj.getString("optime")+"\" />");
		buff.append("</form>");

		buff.append("</td></tr>");

		return buff.toString();
		
	};
	
 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>user_json</title>
</head>
     <script language="javascript" type="text/javascript" src="./jquery.js"></script>

<body>
	<div id="tableinfomsg"></div>

<script type="text/javascript">
function formsubmit(form)
{
	if(confirm("confirm?"))
	{
		return true;
	}
	
	return false;
}

var requestparams={rnd:"123"};
$.post("./aside.json",requestparams,

		function (data, textStatus){
			
			var tableInfo="<table cellpadding='1' cellspacing='0' border='1'><tr><td>表名</td><td>内部表名</td><td>编号</td></tr>";
			var tablelist=data.data.navList['modules'];
    for(var i=0;i<tablelist.length;i++)
    {
    	tableInfo+="<tr><td>p"+i+"@"+tablelist[i]["moduleName"]+"</td><td><a href='http://quanjing.alimama.com:9999/tableshards.jsp?tablename="+tablelist[i]["moduleTableName"]+"' target='_blank'>"+tablelist[i]["moduleTableName"]+"</a> <a href='http://quanjing.alimama.com:9999/tableSchema.jsp?tablename="+tablelist[i]["moduleTableName"]+"' target='_blank'>查看表结构</a></td><td>"+tablelist[i]["moduleId"]+"</td></tr>";
    }
    
    $("#tableinfomsg").html(tableInfo);
		
		}, "json");

</script>


<table cellpadding="0" cellspacing="0" border="0">
	<tr>
<td>
<form action="./list.jsp" method="post"  onsubmit="return formsubmit(this)" >
<input type="text" name="userid" value="登陆ID" />
<input type="text" name="email" value="邮箱" />
<input type="text" name="cname" value="花名" />
<input type="text" name="role" value="0" />
<input type="text" name="permission" value="[1,2,3,4,10,11,12,13,21,30]" />
<input type="hidden" name="type" value="add" />

<input type="submit" value="add"  />
</form>
</td>
<td>

</td>
</tr>





<tr style="display:none">
<td>
<form action="./list.jsp" method="post"  onsubmit="return formsubmit(this)" >
<textarea  name="jsonlist" >
	
</textarea>

<input type="hidden" name="type" value="jsonlist" />

<input type="submit" value="jsonlist"  />
</form>
</td>
<td>

</td>
</tr>

</table>
<hr>
<table cellpadding="0" cellspacing="0" border="0">


<%
	
	String checkuser= String.valueOf(request.getParameter("checkuser"));
	String checkpwd= String.valueOf(request.getParameter("checkpwd"));
	
	if(checkpwd.equals(getMD5(checkuser+"1qazxcvfr432wsde"))&&(checkuser.equals("yannian.mu")||checkuser.equals("xuner.zr")||checkuser.equals("zhangzhuang.sbb")||checkuser.equals("chengjian.cqf")||checkuser.equals("linyun.jly")||checkuser.equals("wenjing.wuwj")||checkuser.equals("lingning")))
	{

	String tp= String.valueOf(request.getParameter("type"));

			Map<String,String> mapval=new HashMap<String,String>();
			mapval.put("userid",String.valueOf(request.getParameter("userid")));
			mapval.put("email",String.valueOf(request.getParameter("email")));
						mapval.put("cname",String.valueOf(request.getParameter("cname")));

			
			mapval.put("role",String.valueOf(request.getParameter("role")));
     mapval.put("permission",String.valueOf(request.getParameter("permission")));

	if(tp.equals("add"))
	{
		
		out.println(UserJson.add(mapval.get("userid"),mapval,checkuser));
	}
	
	if(tp.equals("jsonlist"))
	{
		UserJson.addJsonList(String.valueOf(request.getParameter("jsonlist")).trim(),checkuser);
	}
	
	if(tp.equals("del"))
	{
		out.println(UserJson.del(mapval.get("userid"),checkuser));
	}

		String days=request.getParameter("daystart");

	String resultstr=UserJson.getJson(days,true);

	JSONObject jsonObj = new JSONObject(resultstr);
	JSONArray list=jsonObj.getJSONObject("data").getJSONArray("users");
		
			String stritem2=addJsonHeader();
						%><%=stritem2%><%
		for(int i=0;i<list.length();i++)
		{
			JSONObject obj=list.getJSONObject(i);
			String stritem=addjson(obj.toString());
						%><%=stritem%><%
		}
		
	}else{
			%><tr><td>not allowed</td><td></td></tr><%
		}
	
%>



</table>
</body>
</html>

 
 