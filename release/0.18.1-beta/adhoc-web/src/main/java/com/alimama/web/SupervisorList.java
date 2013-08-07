package com.alimama.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.alimama.mdrill.partion.GetShards;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;

public class SupervisorList {
	public static String[] list() throws Exception
	{
		List<String> rtn=new ArrayList<String>();

		StormClusterState stat=GetShards.getCluster();
		List<String> list=stat.supervisors(null);
		for(String supervisor:list)
		{
			SupervisorContainer container=new SupervisorContainer();
			container.setName(supervisor);
			SupervisorInfo info=stat.supervisor_info(supervisor);
			container.setInfo(info);
			StringBuffer buff=new StringBuffer();
			buff.append("机器域名："+info.getHostName()+"<br>");
			buff.append("机器id："+supervisor+"<br>");
			buff.append("启动的端口号："+info.getWorkPorts().toString()+"<br>");
			   SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String yyyymmmddd=fmt.format(new Date(1000l*info.getTimeSecs()));
			buff.append("最后一次心跳时间："+yyyymmmddd+"<br>");
			buff.append("运行时间："+info.getUptimeSecs()/3600+"小时<br>");
			rtn.add(buff.toString());
		}
		
		Collections.sort(rtn);

		
		String[] rtnarr=new String[rtn.size()];
		return rtn.toArray(rtnarr);
	}
}
