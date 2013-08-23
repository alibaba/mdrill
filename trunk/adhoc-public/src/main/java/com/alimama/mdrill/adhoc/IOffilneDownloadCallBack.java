package com.alimama.mdrill.adhoc;


public interface IOffilneDownloadCallBack extends IHiveExecuteCallBack {
	public boolean isfinished();
	public void setSqlMd5(String md5);
	public void setUserName(String username);
	public void setStoreDir(String store);
	public void setName(String store);
	public void setMemo(String memo);
	public void setDisplayParams(String store);
	public void setMailto(String mailto);
}
