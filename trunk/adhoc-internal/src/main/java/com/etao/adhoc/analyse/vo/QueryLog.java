package com.etao.adhoc.analyse.vo;

import java.util.Date;

public class QueryLog {
	private Date date;
	private String nick;
	private String email;
	private String setName;
	private String dimvalue;
	private String filter;
	private String bizdate; //跟其它vo模块里的bizdate不是一个意思
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDimvalue() {
		return dimvalue;
	}
	public void setDimvalue(String dimvalue) {
		this.dimvalue = dimvalue;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getBizdate() {
		return bizdate;
	}
	public void setBizdate(String bizdate) {
		this.bizdate = bizdate;
	}
	public String getSetName() {
		return setName;
	}
	public void setSetName(String setName) {
		this.setName = setName;
	}
	@Override
	public String toString() {
		return "QueryLog [date=" + date + ", nick=" + nick + ", email=" + email
				+ ", setName=" + setName + ", dimvalue=" + dimvalue
				+ ", filter=" + filter + ", bizdate=" + bizdate + "]";
	}
}
