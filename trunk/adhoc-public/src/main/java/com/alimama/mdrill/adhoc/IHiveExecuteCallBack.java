package com.alimama.mdrill.adhoc;

public interface IHiveExecuteCallBack {
	public void setConfdir(String line);
	public void sync() ;
	public void init(String hql,String[] cmd,String[] env);
	public void WriteStdOutMsg(String line);
	public void WriteSTDERRORMsg(String line);
	public void setSlotCount(int slotCount);
	public void setResultKb(long kb);
	public void setResultRows(long rows);
	public void setPercent(String percent);
	public void setStage(String stage);
	public void addJobId(String percent);
	public void setExitValue(int val);
	public void addException(String msg);
	public void setFailed(String failmsg);
	public void maybeSync();
	public void finish();
}
