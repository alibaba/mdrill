package com.alimama.mdrill.jdbc;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.expression.*;

import com.alimama.mdrill.json.JSONArray;
import com.alimama.mdrill.json.JSONException;
import com.alimama.mdrill.json.JSONObject;

public class InsertParser {
	private String sql;

	public String tablename;
	public String[] fl;
	public String jsons;

	
	public static void main(String[] args) throws JSQLParserException, JSONException {
		
		InsertParser p=new InsertParser();
		p.parse("INSERT INTO table_name (列1, 列2) VALUES ('111', '22') ");
		System.out.println(p.toString());
		
	}
	
	
	
	public void parse(String sql) throws JSQLParserException, JSONException
	{
		this.sql=sql;

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
		 Insert insert = (Insert) parserManager.parse(new StringReader(sql));
         this.tablename=insert.getTable().getName();
         
         fl=new String[insert.getColumns().size()];
         for(int i=0;i<fl.length;i++)
         {
        	 fl[i]=((Column) insert.getColumns().get(i)).getColumnName();
         }
         
         
 		ExpressionList explist=((ExpressionList) insert.getItemsList());
	 		JSONObject item=new JSONObject();

	 		List expressions=explist.getExpressions();
 		for(int i=0;i<expressions.size();i++)
 		{
 			
 			Object val=expressions.get(i);
 			if(val instanceof StringValue)
 			{
 				StringValue vv=(StringValue)val;
 				item.put(fl[i], String.valueOf(vv.getValue()));
 			}
 			if(val instanceof LongValue)
 			{
 				LongValue vv=(LongValue)val;
 				item.put(fl[i], String.valueOf(vv.getValue()));
 			}
 			
 			if(val instanceof DoubleValue)
 			{
 				DoubleValue vv=(DoubleValue)val;
 				item.put(fl[i], String.valueOf(vv.getValue()));
 			}
 		}
		
		
        JSONArray list=new JSONArray();
        list.put(item);

        this.jsons=list.toString();
	
	}
	
	@Override
	public String toString() {
		return "InsertParser [sql=" + sql + ", tablename=" + tablename
				+ ", fl=" + Arrays.toString(fl) + ", jsons=" + jsons + "]";
	}
	
	
}
