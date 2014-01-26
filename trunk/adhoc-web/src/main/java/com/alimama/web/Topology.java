package com.alimama.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alimama.mdrill.partion.GetShards;
public class Topology {
	
	public static String[] active_storms() throws Exception
	{
		StormClusterState stat=GetShards.getCluster();
		List<String> list=stat.active_storms();
		String[] rtn=new String[list.size()];
		return list.toArray(rtn);
	}
	
	public static String[] heartbeat_storms() throws Exception
	{
		StormClusterState stat=GetShards.getCluster();
		List<String> list=stat.heartbeat_storms();
		String[] rtn=new String[list.size()];
		return list.toArray(rtn);
	}
	public static List<String> getStatus(String stormId) throws Exception
	{
		StormClusterState stat=GetShards.getCluster();
		
		Assignment ass=stat.assignment_info(stormId, null);
		Map<String, String> nodeHost=(ass==null)?(new HashMap<String, String>()):ass.getNodeHost();
		Map<Integer, Integer> taskStartTimeSecs=(ass==null)?(new HashMap<Integer, Integer>()):ass.getTaskStartTimeSecs();
		Map<Integer, NodePort> taskToNodeport=(ass==null)?(new HashMap<Integer, NodePort>()):ass.getTaskToNodeport();
		
		
		
		List<String> rtn=new ArrayList<String>();
		List<Integer> taskids=stat.task_ids(stormId);
		for(Integer tid:taskids)
		{
			try{
			TaskInfoContainer con=new TaskInfoContainer();
			con.setStarttime(taskStartTimeSecs.get(tid));
			NodePort np=taskToNodeport.get(tid);
			con.setNp(np);
			if(np!=null)
			{
				con.setHostname(nodeHost.get(np.getNode()));
			}else{
				con.setHostname("nohost");
			}
			con.setTaskId(tid);
			con.setTaskInfo(stat.task_info(stormId, tid));
			con.setHb(stat.task_heartbeat(stormId, tid));
			con.setTaskerrors(stat.task_errors(stormId, tid));
			
			StringBuffer buff=new StringBuffer();
			buff.append("getComponentId:"+ con.getTaskInfo().getComponentId()+"<br>\r\n");
			buff.append("任务:"+ String.format("%03d",con.getTaskId())+"<br>\r\n");
			buff.append("机器域名:"+con.getHostname()+"<br>\r\n");
			buff.append("nodeport:"+con.getNp()+"<br>\r\n");
			SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(con.getStarttime()!=null)
			{
				String yyyymmmddd=fmt.format(new Date(1000l*con.getStarttime()));
				buff.append("启动时间:"+yyyymmmddd+"<br>\r\n");
			}
			buff.append("心跳信息:"+con.getHb()+"<br>\r\n");
			buff.append("task异常信息:"+con.getTaskerrors()+"<br>\r\n");

			
			rtn.add(buff.toString());
			}catch(Throwable e){
				
			}
		}
		
		Collections.sort(rtn);
		return rtn;
	}

}
