package com.etao.adhoc.metric.load;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.adhoc.HiveExecute;
import com.alimama.mdrill.adhoc.IHiveExecuteCallBack;
import com.alimama.mdrill.utils.HadoopBaseUtils;
import com.etao.adhoc.metric.Metric;

public class HiveQueryService implements QueryService,IHiveExecuteCallBack {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Map conf;

	public HiveQueryService(Map conf) {
		this.conf=conf;

	}
	public Metric getMetric(String tablename, String thedate) throws IOException {

		String sqlFormat=(String) conf.get("adhoc.metric.hive.sql."+tablename);
		String hdpConf = (String) conf.get("hadoop.conf.dir");

				
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String day = fmt.format(new Date());
	
		String store = (String) conf.get("higo.download.offline.store")
				+ "/" + day + "/" + java.util.UUID.randomUUID().toString();
		
		Metric metric = null;
		String sql = String.format(sqlFormat, thedate);
		
		HiveExecute hivexec=new HiveExecute();
		hivexec.setConfdir(hdpConf);
		hivexec.setStoreDir(store);
		hivexec.setHql("INSERT OVERWRITE DIRECTORY '" + store +"' "+sql+"");
		hivexec.setCallback(this);
		hivexec.init();
		hivexec.run();
		
		
		Configuration hconf=new Configuration();
 		HadoopBaseUtils.grabConfiguration(hdpConf, hconf);
		FileSystem fs = FileSystem.get(hconf);
		Path dir = new Path(store);
		if (!fs.exists(dir)) {
			throw new IOException("can not found path:" + store);
		}
		FileStatus[] filelist = fs.listStatus(dir);
	
		Long bytesRead = 0l;
		long maxsize = 1024l * 1024 * 1024 * 10;
	
		String[] result=null;
		
		boolean isbreak=false;
		for (FileStatus f : filelist) {
			System.out.println(f.getPath().toString());
			if(isbreak)
			{
				break;
			}
			if (!f.isDir() && !f.getPath().getName().startsWith("_")) {
				FSDataInputStream in = fs.open(f.getPath());
				BufferedReader bf=new BufferedReader(new InputStreamReader(in)); 
				String line;
				while ((line = bf.readLine()) != null) {
					bytesRead += line.getBytes().length;
					String towrite=line.replaceAll("\001", ",").replaceAll("\t", ",");
					System.out.println(towrite);
					if(!towrite.isEmpty())
					{
						result=towrite.split(",");
						if(result.length<8)
						{
							isbreak=true;
							result=null;
						}
					}
					if (bytesRead >= maxsize) {
						bf.close();
						in.close();
						isbreak=true;
					}
					
					if(isbreak)
					{
						break;
					}
				}
				bf.close();
				in.close();
			}
		}
		
		System.out.println(Arrays.toString(result));
		if(result!=null&&result.length>=8)
		{
			metric = new Metric();
			metric.setThedate(thedate);
			metric.setType(0);
			metric.setTablename(tablename);
			metric.setLineCnt((long)Float.parseFloat(result[0]));
			metric.setImpression((long)Float.parseFloat(result[1]));
			metric.setFinClick((long)Float.parseFloat(result[2]));
			metric.setFinPrice(Float.parseFloat(result[3]));
			metric.setAlipayDirectNum((long)Float.parseFloat(result[4]));
			metric.setAlipayDirectAmt(Float.parseFloat(result[5]));
			metric.setAlipayIndirectNum((long)Float.parseFloat(result[6]));
			metric.setAlipayIndirectAmt(Float.parseFloat(result[7]));
			System.out.println(metric.toString());
		}
		return metric;
	
	}
	public void close() {
	}





	public String getName() {
		return "HIVE";
	}
	@Override
	public void setConfdir(String line) {
		
	}
	@Override
	public void sync() {
		
	}
	@Override
	public void init(String hql, String[] cmd, String[] env) {
		System.out.println(hql);
		System.out.println(Arrays.toString(cmd));
		System.out.println(Arrays.toString(env));
	}
	@Override
	public void WriteStdOutMsg(String line) {
		System.out.println(line);
	}
	@Override
	public void WriteSTDERRORMsg(String line) {
		System.out.println(line);
	}
	@Override
	public void setSlotCount(int slotCount) {
		
	}
	@Override
	public void setResultKb(long kb) {
		
	}
	@Override
	public void setResultRows(long rows) {
		
	}
	@Override
	public void setPercent(String percent) {
		
	}
	@Override
	public void setStage(String stage) {
		
	}
	@Override
	public void addJobId(String percent) {
		
	}
	@Override
	public void setExitValue(int val) {
		
	}
	@Override
	public void addException(String msg) {
		
	}
	@Override
	public void setFailed(String failmsg) {
		
	}
	@Override
	public void maybeSync() {
		
	}
	@Override
	public void finish() {
		
	}
	

}
