package com.etao.adhoc.analyse.vo;

public class ModuleInfo {
	private String queryDay;
	private String moduleName;
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
	@Override
	public String toString() {
		return "ModuleInfo [queryDay=" + queryDay + ", moduleName="
				+ moduleName + ", queryCnt=" + queryCnt + ", uv=" + uv + "]";
	}
}
