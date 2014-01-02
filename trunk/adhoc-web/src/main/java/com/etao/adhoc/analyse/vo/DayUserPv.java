package com.etao.adhoc.analyse.vo;

public class DayUserPv {
	private String department;

	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	private String queryDay;
	private String nick;
	private int queryCnt;
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public int getQueryCnt() {
		return queryCnt;
	}
	public void setQueryCnt(int queryCnt) {
		this.queryCnt = queryCnt;
	}
	public String getQueryDay() {
		return queryDay;
	}
	public void setQueryDay(String queryDay) {
		this.queryDay = queryDay;
	}
	@Override
	public String toString() {
		return "DayUserPv [queryDay=" + queryDay + ", nick=" + nick
				+ ", queryCnt=" + queryCnt + "]";
	}
}
