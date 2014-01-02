package com.etao.adhoc.analyse.vo;

public class ModuleInfo {
	private String queryDay;
	@Override
	public String toString() {
		return "ModuleInfo [queryDay=" + queryDay + ", moduleName="
				+ moduleName + ", nicklist=" + nicklist + ", queryCnt="
				+ queryCnt + ", uv=" + uv + "]";
	}
	private String moduleName;
	private String nicklist;

	public String getNicklist() {
		return nicklist;
	}
	public void setNicklist(String nicklist) {
		this.nicklist = nicklist;
	}
	private int queryCnt;
	private int uv;
	public String getQueryDay() {
		return queryDay;
	}
	public void setQueryDay(String queryDay) {
		this.queryDay = queryDay;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public int getQueryCnt() {
		return queryCnt;
	}
	public void setQueryCnt(int queryCnt) {
		this.queryCnt = queryCnt;
	}
	public int getUv() {
		return uv;
	}
	public void setUv(int uv) {
		this.uv = uv;
	}
	
}
