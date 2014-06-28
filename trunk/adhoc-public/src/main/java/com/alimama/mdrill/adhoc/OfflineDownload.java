package com.alimama.mdrill.adhoc;


import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;



public class OfflineDownload {
    private static final ExecutorService       EXECUTE  = ExecutorSerives.EXECUTE;
    private IOffilneDownloadCallBack offline;
	
	private String hql = null;
	private String md5 = null;

	private String useName = null;
	private String jobName = null;
	private String memo = "";
	private String displayParams = null;

	private HiveExecute hivexec=new HiveExecute();
    private String storeDir;
    
    private String mailto="";
    
    private Runnable Processer=null;
    

	public Runnable getProcesser() {
		return Processer;
	}

	public void setProcesser(Runnable processer) {
		Processer = processer;
	}


	private String confdir=	System.getenv("HADOOP_CONF_DIR");

	public void setConfdir(String confdir) {
		this.confdir = confdir;
	}

    public void setMailto(String mailto) {
		this.mailto = mailto;
	}
    
	public void setSqlMd5(String md5) {
		this.md5=md5;
	}
    public void setStoreDir(String storeDir) {
		this.storeDir = storeDir;
	}

	public void setOffline(IOffilneDownloadCallBack offline) {
		this.offline = offline;
	}
    
    public void setUseName(String useName) {
		this.useName = useName;
	}
    
    public void setHql(String hql) {
		this.hql = hql;
	}
    
    public void setDisplayParams(String displayParams) {
		this.displayParams = displayParams;
	}
    
    public void setJobName(String jobName) {
		this.jobName = jobName;
	}
    
    public void setMemo(String memo) {
		this.memo = memo;
	}
    
    public void run() {
    	
    	this.offline.setSqlMd5(md5);
    	this.offline.setMailto(mailto);
    	this.offline.setStoreDir(this.storeDir);
    	this.offline.setUserName(useName);
    	this.offline.setName(this.jobName);
    	this.offline.setDisplayParams(displayParams);
    	this.hivexec.setConfdir(confdir);
    	this.hivexec.setStoreDir(this.storeDir);
    	this.hivexec.setHql(this.hql);
    	this.hivexec.setCallback(this.offline);
    	this.hivexec.setProcesser(this.getProcesser());
    	this.hivexec.init();
    	EXECUTE.execute(this.hivexec);
    }
    
    
public static void main(String[] args) throws UnsupportedEncodingException {
		MySqlConn conn=new MySqlConn("jdbc:mysql://tiansuan1.kgb.cm4:3306/adhoc_download", "adhoc", "adhoc");
		MysqlCallback callback=new MysqlCallback(conn);
		OfflineDownload download=new OfflineDownload();
		download.setOffline(callback);
		download.setMailto("yannian.mu@alipay.com");
		download.setHql(args[0]);
		download.setUseName(args[1]);
		download.setJobName(args[2]);
		download.setDisplayParams(args[3]);
		download.setStoreDir(args[4]);
		download.run();
		while(!callback.isfinished())
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
