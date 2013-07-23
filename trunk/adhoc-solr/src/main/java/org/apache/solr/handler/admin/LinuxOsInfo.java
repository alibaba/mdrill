package org.apache.solr.handler.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * linux 下cpu 内存 磁盘 jvm的使用监控
 * 
 * @author peng,.chen
 * 
 */
public class LinuxOsInfo {
	
	/**
	 * group1:use cpu used(%) <br>
	 * group2:system cpu used(%)
	 */
	static final String cpuRegex = "Cpu\\(s\\):\\s+(\\d+\\.\\d+)%us,\\s+(\\d+\\.\\d+)%sy.*";
	public static final Pattern cpuPattern = Pattern.compile(cpuRegex);
	/**
	 * group1: total mem(K)<br>
	 * group2: used mem(K)<br>
	 * group3: free mem(K)<br>
	 */
	static final String memRegex = "Mem:\\s+(\\d+)k\\s+total,\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+free,.*";
	public static final Pattern memPattern = Pattern.compile(memRegex);
	public static LinuxOsInfo INSTANCE = null;
	/**
	 * 
	 * @creation 2012-1-16 下午12:03:15
	 * @return
	 */
	public static LinuxOsInfo getInstance(){
		if(INSTANCE == null){
			return new LinuxOsInfo();
		}
		return INSTANCE;
	}
	
	private LinuxOsInfo() {
	}
	
	/**
	 * 
	 * @creation 2012-1-16 下午12:55:07
	 */
	public static Info getInfo(){
		Info info = new Info();
		double[] cpuData = new double[]{0, 0};
		try {
			cpuData = getInstance().getCpuData();
		} catch (Exception e) {
		}
		info.setCpuUsed(cpuData[0]);
		double diskFree = getInstance().getDiskFree();
		info.setDiskFree(diskFree);
		double diskTotal = getInstance().getDiskTotal();
		info.setDiskTotal(diskTotal);
		double[] memData = new double[]{0, 0, 0};
		try {
			memData = getInstance().getMemData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		info.setMemUsed(memData[1]);
		info.setMemTotal(memData[0]);
		info.setPath(new File("/").getAbsolutePath());
		return info;
	}
	
	/**
	 * 获取cpu使用情况, 返回double数组{usCpu, syCpu}
	 * 
	 * @return
	 * @throws Exception
	 */
	public double[] getCpuData() throws Exception {
		double[] data = null;
		if(!isLinuxOs()){
			return new double[0];
		}
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec("top -b -n 1");// 调用系统的“top"命令

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;

			while ((str = in.readLine()) != null) {
				Matcher matcher = cpuPattern.matcher(str);
				if(matcher.find()){
					String usCpu = matcher.group(1);
					String syCpu = matcher.group(2);
					data = new double[]{Double.parseDouble(usCpu), Double.parseDouble(syCpu)};
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		return data;
	}

	/**
	 * 内存监控, 返回double数组{totalMem, usedMem, freeMem}
	 * 
	 * @return
	 * @throws Exception
	 */
	public double[] getMemData() throws Exception {

		double[] data = null;
		if(!isLinuxOs()){
			return new double[0];
		}
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec("top -b -n 1");// 调用系统的“top"命令

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;

			while ((str = in.readLine()) != null) {
				Matcher matcher = memPattern.matcher(str);
				if(matcher.find()){
					String totalMem = matcher.group(1);
					String usedMem = matcher.group(2);
					String freeMem = matcher.group(3);
					data = new double[]{Double.parseDouble(totalMem), Double.parseDouble(usedMem), Double.parseDouble(freeMem)};
//					System.out.println("totalMem:"+totalMem+", usedMem:"+usedMem+", freeMem:"+freeMem);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		return data;
	}

	/**
	 * 获得当前磁盘总容量
	 * @creation 2012-1-16 下午12:06:22
	 * @return
	 */
	public double getDiskTotal(){
		return new File("/").getTotalSpace();
	}
	
	/**
	 * 获得剩余空间容量
	 * @creation 2012-1-16 下午12:06:54
	 * @return
	 */
	public double getDiskFree(){
		return new File("/").getFreeSpace();
	}
	
	/**
	 * 获取磁盘空间大小
	 * 
	 * @return
	 * @throws Exception
	 */
	public double getDeskUsage() throws Exception {
		double totalHD = 0;
		double usedHD = 0;
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec("df -hl");// df -hl 查看硬盘空间

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			String[] strArray = null;
			int flag = 0;
			while ((str = in.readLine()) != null) {
				int m = 0;
				// if (flag > 0) {
				// flag++;
				strArray = str.split(" ");
				for (String tmp : strArray) {
					if (tmp.trim().length() == 0)
						continue;
					++m;
					// System.out.println("----tmp----" + tmp);
					if (tmp.indexOf("G") != -1) {
						if (m == 2) {
							// System.out.println("---G----" + tmp);
							if (!tmp.equals("") && !tmp.equals("0"))
								totalHD += Double.parseDouble(tmp.substring(0,
										tmp.length() - 1)) * 1024;

						}
						if (m == 3) {
							// System.out.println("---G----" + tmp);
							if (!tmp.equals("none") && !tmp.equals("0"))
								usedHD += Double.parseDouble(tmp.substring(0,
										tmp.length() - 1)) * 1024;

						}
					}
					if (tmp.indexOf("M") != -1) {
						if (m == 2) {
							// System.out.println("---M---" + tmp);
							if (!tmp.equals("") && !tmp.equals("0"))
								totalHD += Double.parseDouble(tmp.substring(0,
										tmp.length() - 1));

						}
						if (m == 3) {
							// System.out.println("---M---" + tmp);
							if (!tmp.equals("none") && !tmp.equals("0"))
								usedHD += Double.parseDouble(tmp.substring(0,
										tmp.length() - 1));
							// System.out.println("----3----" + usedHD);
						}
					}

				}

				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		return (usedHD / totalHD) * 100;
	}
	
	/**
	 * 是否是linux OS
	 * @creation 2012-5-11 下午5:01:19
	 * @return
	 */
	public boolean isLinuxOs(){
		String os = System.getProperty("os.name");
		if(os.contains("Linux")){
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @creation 2012-1-16 下午12:04:21
	 * @param args
	 */
	public static void main1(String[] args){
		System.out.println(Runtime.getRuntime().maxMemory());;//Returns the maximum amount of memory that the Java virtual machine will attempt to use. 
		System.out.println(Runtime.getRuntime().totalMemory());;//Returns the total amount of memory in the Java virtual machine.
		System.out.println(Runtime.getRuntime().freeMemory());//Returns the amount of free memory in the Java Virtual Machine.
		System.out.println(Runtime.getRuntime().availableProcessors());
//		Runtime.getRuntime().removeShutdownHook(hook);
//		Runtime.getRuntime().addShutdownHook(hook)
	}
	
	public static void main2(String[] args) throws Exception {
		LinuxOsInfo cpu = new LinuxOsInfo();
//		System.out.println("---------------cpu used:" + cpu.getCpuUsage() + "%");
//		System.out.println("---------------mem used:" + cpu.getMemUsage() + "%");
//		System.out.println("---------------HD used:" + cpu.getDeskUsage() + "%");
		System.out.println("---------------disk free:" + cpu.getDiskFree());
		System.out.println("---------------disk total:" + cpu.getDiskTotal());
		System.out.println("------------jvm监控----------------------");
		Runtime lRuntime = Runtime.getRuntime();
		System.out.println("--------------Free Momery:" + lRuntime.freeMemory() + "K");
		System.out.println("--------------Max Momery:" + lRuntime.maxMemory() + "K");
		System.out.println("--------------Total Momery:" + lRuntime.totalMemory() + "K");
		System.out.println("---------------Available Processors :" + lRuntime.availableProcessors());
	}
	
	public static void main(String[] args) {
		Info info = LinuxOsInfo.getInfo();
		System.out.println("cpuUsed : "+info.getCpuUsed());
		System.out.println("diskFree : "+info.getDiskFree());
		System.out.println("diskTotal : "+info.getDiskTotal());
		System.out.println("memTotal : "+info.getMemTotal());
		System.out.println("memUsed : "+info.getMemUsed());
		System.out.println("currentPath : "+info.getPath());
		System.out.println();
	}
	
	
	
	
}

