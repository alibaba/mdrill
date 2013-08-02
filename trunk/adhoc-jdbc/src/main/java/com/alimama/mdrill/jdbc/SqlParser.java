package com.alimama.mdrill.jdbc;

import java.io.StringReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import java.lang.reflect.Method;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;

public class SqlParser {
	private String sql;
	public String tablename;
	public String fl;
	public String groupby;
	public String sort;
	public String order;
	public String queryStr;
	public String start;
	public String rows;
	public String[] colsAliasNames;
	public String[] colsNames;
	
	public static void main(String[] args) {
		System.out.println("###");
		SqlParser p=new SqlParser();
//		p.parse("select myn,abc from test where _higopartions_ = '20130101,20120303'  ");
//		System.out.println(p.toString());
//		p.parse("select count(pv) as pv,sum(pv) as sumpv from test where  _higopartions_ = '20130101,20120303' ");
//		System.out.println(p.toString());
//		
//		p.parse("select count(pv) as pv,sum(pv) as sumpv from test where  _higopartions_ = '20130101,20120303' and myn eq 123 and username neq 'yannian'");
//		System.out.println(p.toString());
//		
//		p.parse("select count(pv) as pv,sum(pv) as sumpv from test where  _higopartions_ = '20130101,20120303' and price gt 20 and buycnt lt 30 ");
//		System.out.println(p.toString());
//		
		p.parse("select count(pv) as pv,sum(pv) as sumpv from test where  thedate >=20130101 and thedate <=20120303 and userid not    in (abc,dec,a,e,f,'13','45') ");
//		System.out.println(p.toString());
//		
//		p.parse("select myn,abc,count(pv) as pv,sum(pv) as sumpv from test where  _higopartions_ = '20130101,20120303' group by myn,abc");
//		System.out.println(p.toString());
//		
//		p.parse("select myn,abc,count(pv) as pv,sum(pv) as sumpv from test where  _higopartions_ = '20130101,20120303' group by myn,abc order by pv");
//		System.out.println(p.toString());
//		
//		p.parse("select myn,abc,count(pv) as pv,sum(pv) as sumpv from test where  _higopartions_ = '20130101,20120303' group by myn,abc order by pv desc");
//		System.out.println(p.toString());
//		
//		p.parse("select myn,abc,count(pv) as pv,sum(pv) as sumpv from test where  _higopartions_ = '20130101,20120303' group by myn,abc order by sum(pv) limit 5,60");
//		System.out.println(p.toString());
//		
//		p.parse("select thedate,category_level1_name,user_id,count(*) from rpt_hitfake_auctionall_d where thedate >'20130625' and  thedate <'20130705' and (thedate ='20130704' or thedate ='20130705' or thedate<='20130702')   and ((custid='1104981405' and user_id='136018175') or user_id='932280506' or user_id like '%9999%') and category_level1_name='3C数码配件' group by thedate,user_id,category_level1_name limit 0,100");
		//select thedate,category_level1_name,count(*),count(suit_sum) as cnt,sum(suit_sum) as sam from rpt_hitfake_auctionall_d where thedate >'20130625' and  thedate <'20130705' and (thedate ='20130704' or thedate ='20130705' or thedate<='20130702')   and ((custid='1104981405' and user_id='136018175') or user_id='932280506' or user_id like '%9999%')  and category_level1_name like '%电%' group by thedate,category_level1_name order by sam desc limit 0,100
		System.out.println(p.toString());
		

		
	}
	
	
	
	public void parse(String sql)
	{
		try {
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.sql=sql;
		String otherSql=sql; 
		otherSql=this.parseColumns(otherSql);
		otherSql=this.parseTableName(otherSql);
		otherSql=this.parselimit(otherSql);
		otherSql=this.parseOrderBy(otherSql);
		otherSql=this.parseGroupBy(otherSql);
		try {
			this.parseFq(otherSql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public String toString() {
		return "SqlParser [\n  sql=" + sql + ",\n  tablename=" + tablename + ",\n  fl="
				+ fl + ",\n  groupby=" + groupby + ",\n  sort=" + sort + ",\n  order="
				+ order + ",\n  queryStr=" + queryStr + ",\n start=" + start + ",\n rows=" + rows + ",\n  colsAliasNames="
				+ Arrays.toString(colsAliasNames) + ",\n  colsNames="
				+ Arrays.toString(colsNames) + "\n]";
	}

	
	Pattern fromReg=Pattern.compile("from", Pattern.CASE_INSENSITIVE); 
	Pattern selectReg=Pattern.compile(".*select[ ]*", Pattern.CASE_INSENSITIVE); 
	Pattern columnAliasReg=Pattern.compile("as", Pattern.CASE_INSENSITIVE); 
	private String parseColumns(String otherSql)
	{
		String[] fromSplit=fromReg.split(otherSql);
		String[] cols=selectReg.matcher(fromSplit[0]).replaceAll("").trim().split(",");
		StringBuffer flBuffer=new StringBuffer();
		this.colsAliasNames=new String[cols.length];
		this.colsNames=new String[cols.length];
		for(int i=0;i<cols.length;i++)
		{
			String[] alinames=columnAliasReg.split(cols[i]);
			String realColumn=alinames[0].trim();
			this.colsNames[i]=realColumn;
			if(i!=0)
			{
				flBuffer.append(",");
			}
			flBuffer.append(realColumn);
			if(alinames.length>1){
				colsAliasNames[i]=alinames[1].trim();
			}else{
				colsAliasNames[i]=realColumn;
			}
		}
		this.fl=flBuffer.toString();
		return fromSplit[1];
	}
	
	private String parseTableName(String otherSql)
	{
		String tblname=otherSql.trim();
		int end=tblname.length();
		int index=tblname.indexOf(" ");
		if(index>0)
		{
			end=index;
		}
		this.tablename=tblname.substring(0, end);
		return tblname.substring(end);
	}
	
	
	Pattern groupbyReg=Pattern.compile("group[ ]+by", Pattern.CASE_INSENSITIVE); 

	private String parseGroupBy(String otherSql)
	{
		String[] group=groupbyReg.split(otherSql);
		if(group.length>1)
		{
			this.groupby=group[1].trim();
		}
		return group[0];
	}
	
	
	Pattern limitReg=Pattern.compile("limit", Pattern.CASE_INSENSITIVE); 

	private String parselimit(String otherSql)
	{
		String[] limit=limitReg.split(otherSql);
		this.start="0";
		this.rows="20";
		if(limit.length>1)
		{
			String[] cols=limit[1].trim().split(",");
			this.start=cols[0].trim();
			this.rows=cols[1].trim();
		}
		return limit[0];
	}
	
	
	Pattern orderbyReg=Pattern.compile("order[ ]+by", Pattern.CASE_INSENSITIVE); 
	private String parseOrderBy(String otherSql)
	{
		String[] split=orderbyReg.split(otherSql);
		if(split.length>1)
		{
			String[] sort=split[1].trim().split("[ ]+");
			this.setSort(sort[0].trim());
			this.order="asc";
			if(sort.length>1)
			{
				this.order=sort[1].trim().toLowerCase();
			}
		}
		return split[0];
	}
	
	private void setSort(String sort)
	{
		for(int i=0;i<this.colsAliasNames.length;i++)
		{
			if(sort.equals(this.colsAliasNames[i]))
			{
				this.sort=this.colsNames[i];
				return ;
			}
		}
		this.sort=sort;
	}
	Pattern whereReg=Pattern.compile("where[ ]*", Pattern.CASE_INSENSITIVE); 
	protected Expression getExpressionWithoutParenthesis(Expression ex){
		if(ex instanceof Parenthesis){
			Expression child = ((Parenthesis)ex).getExpression();
			return getExpressionWithoutParenthesis(child);
		}else{
			return ex;
		}
		
	}
	
	public JSONArray generateList(Expression ex , JSONArray linkList,JSONObject parent) throws JSONException{
		if(ex==null){
			return linkList;
		}
		if(ex instanceof OrExpression||ex instanceof AndExpression){
			parent.put("subQuery", "1");
			parent.put("filter", (ex instanceof OrExpression)?"OR":"AND");
			
			BinaryExpression be = (BinaryExpression)ex;
			generateList(be.getLeftExpression(), linkList,parent);
			generateList(be.getRightExpression(), linkList,parent);
	
		}else if(ex instanceof Parenthesis){
			JSONArray sublist = new JSONArray();//{colname:{operate:1,value:xxxx}}
			JSONObject subQuery=new JSONObject();
			subQuery.put("subQuery", "1");
			subQuery.put("filter", "AND");
			subQuery.put("list", sublist);
			Expression exp = getExpressionWithoutParenthesis(ex);
			linkList.put(subQuery);
			generateList(exp,sublist,subQuery);
			
		}else{
			JSONObject item=new JSONObject();//
			processExpression(ex,item);
			linkList.put(item);
		}
		return linkList;
	}
	
	private Object invokeMethod(Object obj, String methodFunc){
		try {
			Method method = obj.getClass().getMethod(methodFunc, null);
			return method.invoke(obj, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	private JSONArray toJSONArray(String str,String split)
	{
		String[] list=str.split(split);
		JSONArray rtn=new JSONArray();
		for(String s:list)
		{
			rtn.put(s.trim().replaceAll("^'", "").replaceAll("'$", ""));
		}
		return rtn;
	}
	
	protected ObjectExpression processExpression(Expression e,JSONObject item) throws JSONException{
		ObjectExpression oe = new ObjectExpression();
		Object columnObj = invokeMethod(e, "getLeftExpression");
		if(columnObj instanceof LongValue){
			LongValue longValue = (LongValue)columnObj;
			oe.setColumnname(longValue.getStringValue());
		}else{
			Column column = (Column)invokeMethod(e, "getLeftExpression");		
			oe.setColumnname(column.getColumnName());
		}
		if (e instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) e;
			oe.setExp(be.getStringExpression());
			if(be.getRightExpression() instanceof Function){
				oe.setValue(invokeMethod(be.getRightExpression(), "toString"));
			}else{
				oe.setValue(invokeMethod(be.getRightExpression(), "getValue"));
			}
		}else{
			oe.setExp((String)invokeMethod(e, "toString"));
		}
		
		JSONObject subitem=new JSONObject();
		String op=oe.getExp();
		String colname=oe.getColumnname();
		String val=String.valueOf(oe.getValue());
		JSONArray rtn=new JSONArray();
		rtn.put(val);
		subitem.put("value", rtn);
		if(op.equals("="))
		{
			subitem.put("operate", "1");
		}else if(op.equals("<>"))
		{
			subitem.put("operate", "2");
		}
		else if(op.equals(">="))
		{
			subitem.put("operate", "3");
		}
		else if(op.equals(">"))
		{
			subitem.put("operate", "13");
		}
		else if(op.equals("<="))
		{
			subitem.put("operate", "4");
		}
		else if(op.equals("<"))
		{
			subitem.put("operate", "14");
		}
		else if(op.toLowerCase().equals("like"))
		{
			subitem.put("operate", "1000");
			subitem.put("value", colname+":"+transValue(val.replaceAll("%", "*")));
			colname=String.valueOf(Math.random());
		}
		else if(op.indexOf("NOT IN")>=0 )
		{
			String[] cols=op.split("NOT IN");
			String list=cols[1].replaceAll("^[ ]*\\(", "").replaceAll("\\)[ ]*$", "");
			subitem.put("operate", "7");
			subitem.put("value", toJSONArray(list,","));
		}
		else if(op.indexOf("IN")>=0 )
		{
			String[] cols=op.split("IN");
			String list=cols[1].replaceAll("^[ ]*\\(", "").replaceAll("\\)[ ]*$", "");
			subitem.put("operate", "5");
			subitem.put("value", toJSONArray(list,","));
		}else{
			subitem.put("operate", op);
			subitem.put("value", toJSONArray(val,","));
		}
		item.put(colname, subitem);
		
		return oe;
	}
	private void parseFq(String otherSql) throws JSONException, JSQLParserException
	{
		String[] split=whereReg.split(this.tablename +" "+otherSql.trim());

		if(split.length>1)
		{
			String where=split[1].trim();
			CCJSqlParserManager pm = new CCJSqlParserManager();
			PlainSelect plainSelect =  (PlainSelect)((Select) pm.parse(new StringReader("select * from abc where "+where))).getSelectBody();
			Expression e  = getExpressionWithoutParenthesis(plainSelect.getWhere());
			JSONArray jsonObj = new JSONArray();//{colname:{operate:1,value:xxxx}}

			JSONObject subQuery=new JSONObject();
			subQuery.put("subQuery", "1");
			subQuery.put("filter", "AND");
			subQuery.put("list", jsonObj);
			this.queryStr=generateList(e, jsonObj, subQuery).toString();
		}
	}
	
	private String transValue(String val)
	{
		return val.trim().replaceAll("'", "");
	}
}
