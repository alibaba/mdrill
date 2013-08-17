package org.apache.solr.handler.admin;

/**
 * solr core所在机器的内存 磁盘 cpu等信息
 * @author peng.chen E-mail: chenpeng0122@hotmail.com
 * @creation  2012-1-16 下午12:11:46
 */
public class Info {

	/**
	 * 内存空闲量
	 */
	private double memUsed = 0;
	/**
	 * 内存总大小
	 */
	private double memTotal = 0;
	/**
	 * 磁盘剩余空间
	 */
	private double diskFree = 0;
	/**
	 * 磁盘总容量
	 */
	private double diskTotal = 0;
	/**
	 * cpu使用率
	 */
	private double cpuUsed = 0;
	/**
	 * 当前路径
	 */
	private String path = null;
	
	public double getMemUsed() {
		return memUsed;
	}
	public void setMemUsed(double memUsed) {
		this.memUsed = memUsed;
	}
	public double getMemTotal() {
		return memTotal;
	}
	public void setMemTotal(double memTotal) {
		this.memTotal = memTotal;
	}
	public double getDiskFree() {
		return diskFree;
	}
	public void setDiskFree(double diskFree) {
		this.diskFree = diskFree;
	}
	public double getCpuUsed() {
		return cpuUsed;
	}
	public void setCpuUsed(double cpuUsed) {
		this.cpuUsed = cpuUsed;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public double getDiskTotal() {
		return diskTotal;
	}
	public void setDiskTotal(double diskTotal) {
		this.diskTotal = diskTotal;
	}
	/**
	 * 将toString函数产生的字符串解析值并赋值给当前对象
	 * @creation 2012-1-16 下午2:57:37
	 * @param infoStr
	 * @return
	 */
	public Info parser(String infoStr){
		String[] infos = infoStr.split(",");
		if(infos.length != 6){
			return this;
		}
		// cpuUsed
		String[] temps = infos[0].split(":");
		cpuUsed = Double.parseDouble(temps[1]);
		// memTotal
		temps = infos[1].split(":");
		memTotal = Double.parseDouble(temps[1]);
		// memUsed
		temps = infos[2].split(":");
		memUsed = Double.parseDouble(temps[1]);
		//diskTotal
		temps = infos[3].split(":");
		diskTotal = Double.parseDouble(temps[1]);
		//diskFree
		temps = infos[4].split(":");
		diskFree = Double.parseDouble(temps[1]);
		// path
		temps = infos[5].split(":");
		path = temps[1];
		return this;
	}
	
	@Override
	public String toString() {
		return "cpuUsed:"+cpuUsed+", memTotal:"+memTotal+", memUsed: "+memUsed+", diskTotal:"+diskTotal+", diskFree:"+diskFree+", path:"+path;
	}
	
	
}
