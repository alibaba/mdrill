package com.etao.adhoc.analyse.vo;

public class TotalUserPv {
	private String department;

	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
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
	@Override
	public String toString() {
		return "TotalUserPv [nick=" + nick + ", queryCnt=" + queryCnt + "]";
	}

}
