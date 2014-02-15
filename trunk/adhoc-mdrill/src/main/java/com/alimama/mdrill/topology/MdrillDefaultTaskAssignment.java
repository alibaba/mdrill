package com.alimama.mdrill.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.custom.IAssignment;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.utils.StormUtils;

/**
 * 
 #----任务分配，注意，原先分布在同一个位置的任务，迁移后还必须分布在一起，比如说acker:0与merger:10分布在一起了，那么迁移后他们还要在一起，为了保险起见，建议同一个端口跑多个任务的，可以像下面acker，heartbeat那样----
 mdrill.task.ports.adhoc: "6901~6902"
 mdrill.task.assignment.adhoc:
     - "####看到这里估计大家都会晕，但是这个是任务调度很重要的地方，出去透透气，回来搞定这里吧####"	
     - "####下面为初始分布，用于同一台机器之间没有宕机的调度####"
     - "merge:10&shard@0:0~4;tiansuan1.kgb.cm4:6601,6701~6704;6602,6711~6714;6603,6721~6724"
     - "merge:1&shard@0:5~9;tiansuan2.kgb.cm4:6601,6701~6704;6602,6711~6714;6603,6721~6724"
     - "merge:2&shard@0:48~53;adhoc1.kgb.cm6:6601,6701~6705;6602,6711~6715;6603,6721~6725"
     - "merge:3&shard@0:10~14;adhoc2.kgb.cm6:6601,6701~6704;6602,6711~6714;6603,6721~6724"
     - "merge:4&shard@0:15~19;adhoc3.kgb.cm6:6601,6701~6704;6602,6711~6714;6603,6721~6724"
     - "merge:5&shard@0:20~24;adhoc4.kgb.cm6:6601,6701~6704;6602,6711~6714;6603,6721~6724"
     - "merge:6&shard@0:25~30;adhoc5.kgb.cm6:6601,6701~6705;6602,6711~6715;6603,6721~6725"
     - "merge:7&shard@0:31~36;adhoc6.kgb.cm6:6601,6701~6705;6602,6711~6715;6603,6721~6725"
     - "merge:8&shard@0:37~41;adhoc7.kgb.cm6:6601,6701~6704;6602,6711~6714;6603,6721~6724"
     - "merge:9&shard@0:42~47;adhoc8.kgb.cm6:6601,6701~6705;6602,6711~6715;6603,6721~6725"
     - "shard@0:54~59;adhocbak.kgb.cm6:6701~6705;6711~6715;6721~6725"
     - ""
     - "##__acker,heartbeat,merge:0他们共用同一个端口，记住要时时刻刻，他们共享的都是同一个端口,别拆开了##"
     - "__acker:0;adhocbak.kgb.cm6:6601;6602;6603"
     - "heartbeat:0;adhocbak.kgb.cm6:6601;6602;6603"
     - "merge:0;adhocbak.kgb.cm6:6601;6602;6603"
     - ""
     - "#######################################################################################################################"
     - "####如果某台机器宕机了，任务无法在固定的分配都某一台，就需要使用下面的配置，宕机的任务会迁移到新的机器上####"
     - "####注意这里的端口不要与上面的端口有重叠,某个任务也不要继续分配在上面的机器，因为那台机器已经宕机了####"
     - "####要注意如果某一台机器宕机，他的任务要均匀的分布到集群其他机器上，不要全部迁移到同一台机器上####"
     - "merge:2&shard@0:58,59,16,26,38;tiansuan1.kgb.cm4:6604,6721~6724;6605,6731~6734"
     - "merge:3&shard@0:59,50,17,27,39;tiansuan2.kgb.cm4:6604,6721~6724;6605,6731~6734"
     - "merge:4&shard@0:1,10,18,28,40,57;adhoc1.kgb.cm6:6604,6721~6725;6605,6731~6735"
     - "shard@0:2,51,19,29,41;adhoc2.kgb.cm6:6604,6721~6724;6605,6731~6734"
     - "merge:6&shard@0:3,52,20,30,42;adhoc3.kgb.cm6:6604,6721~6724;6605,6731~6734"
     - "merge:7&shard@0:4,53,33,31,43;adhoc4.kgb.cm6:6604,6721~6724;6605,6731~6734"
     - "merge:8&shard@0:6,11,21,32,44,56;adhoc5.kgb.cm6:6604,6721~6725;6605,6731~6735"
     - "merge:9&shard@0:7,12,22,37,45,55;adhoc6.kgb.cm6:6604,6721~6725;6605,6731~6735"
     - "merge:5&shard@0:8,13,23,34,46;adhoc7.kgb.cm6:6604,6721~6724;6605,6731~6734"
     - "merge:10&shard@0:9,14,24,35,54,5,;adhoc8.kgb.cm6:6604,6721~6725;6605,6731~6735"
     - "merge:1&shard@0:15,25,36,47,0;adhocbak.kgb.cm6:6604,6721~6725;6605,6731~6735"
     - "##__acker，heartbeat,merge:0他们共用同一个端口，记住要时时刻刻，他们共享的都是同一个端口##"
     - "__acker:0;adhoc2.kgb.cm6:6604;6605"
     - "heartbeat:0;adhoc2.kgb.cm6:6604;6605"
     - "merge:0;adhoc2.kgb.cm6:6604;6605"
  
 */
public class MdrillDefaultTaskAssignment implements IAssignment {
	private static Logger LOG = Logger.getLogger(MdrillDefaultTaskAssignment.class);
	public static String MDRILL_ASSIGNMENT_DEFAULT = "mdrill.task.assignment";
	public static String MDRILL_ASSIGNMENT_PORTS = "mdrill.task.ports";
	private HashMap<TaskIndex,ArrayList<HostPort>> taskIndexAssign=new HashMap<TaskIndex, ArrayList<HostPort>>();
	private HashMap<TaskId,Integer> taskId2Index=new HashMap<TaskId, Integer>();
	private HashMap<Integer,TaskInfo> taskId2TaskInfo=new HashMap<Integer,TaskInfo>();
	
	private HashSet<Integer> allowPorts=new HashSet<Integer>();
	private HashSet<HostPort> hostports=new HashSet<HostPort>();
	private Map<String, SupervisorInfo> supInfos;
	private Map topology_conf;
	private StormClusterState zkCluster;
	private String topologyId;
	private int reassign_num=0;

	@Override
	public void setup(Map topology_conf, String topologyId,
			StormClusterState zkCluster,
			Map<NodePort, List<Integer>> keepAssigned,
			Map<String, SupervisorInfo> supInfos) {
		LOG.info("assignment "+MdrillDefaultTaskAssignment.class.getCanonicalName()+" setup "+topologyId);
		this.topology_conf=topology_conf;
		this.supInfos = supInfos;
		this.zkCluster = zkCluster;
		this.topologyId = topologyId;
		
	}
	
	public Map<NodePort, List<Integer>> keeperSlots(
			Map<NodePort, List<Integer>> aliveAssigned, int numTaskIds,
			int numWorkers)
	{
		Map<NodePort, List<Integer>> rtn=new HashMap<NodePort, List<Integer>>();
		rtn.putAll(aliveAssigned);
		return rtn;
	}
	

	@Override
	public List<NodePort> slotsAssignment(List<NodePort> freedSlots,
			int reassign_num, Set<Integer> reassignIds) {
		this.reassign_num=reassign_num;
		return freedSlots;
	}

	
	@Override
	public Map<Integer, NodePort> tasksAssignment(List<NodePort> reassignSlots,
			Set<Integer> reassignIds) {
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();

		if(reassignIds.size()<=0)
		{
			return rtn; 
		}
		
		this.initAssignment();
		this.taskIdToIndex();
		LOG.info("taskId2Index "+taskId2Index.toString());
		LOG.info("taskIndexAssign "+taskIndexAssign.toString());
		LOG.info("taskId2TaskInfo "+taskId2TaskInfo.toString());
		LOG.info("hostports "+hostports.toString());
		LOG.info("supInfos "+supInfos.toString());

		ArrayList<Integer> nonAssignTask=new ArrayList<Integer>();
		ArrayList<NodePort> nonAssignNodePort=new ArrayList<NodePort>();
		HashSet<NodePort> UsedNodePort=new HashSet<NodePort>();
		HashMap<HostPort,NodePort> host2node=new HashMap<HostPort, NodePort>();
		
		for(NodePort p:reassignSlots)
		{
			
			SupervisorInfo info=this.supInfos.get(p.getNode());
			if(info!=null)
			{
				HostPort hp=new HostPort(info.getHostName(), p.getPort());
				if(hostports.contains(hp))
				{
					host2node.put(hp,p);
					continue;
				}
			}
			
			if(!this.allowPorts.contains(p.getPort()))
			{
				continue;
			}

			nonAssignNodePort.add(p);
		}
		
		for(Integer task:reassignIds)
		{
			TaskInfo info=taskId2TaskInfo.get(task);
			if(info==null)
			{
				nonAssignTask.add(task);
				LOG.info("can`t find task:"+task+","+this.topologyId);
				continue;
			}
			
			String componname=info.getComponentId();
			Integer index=taskId2Index.get(new TaskId(componname, task));
			if(index==null)
			{
				nonAssignTask.add(task);
				LOG.info("can`t find taskindex:"+task+","+this.topologyId+",index:"+index);
				continue;
			}
			ArrayList<HostPort> taskAssignList=taskIndexAssign.get(new TaskIndex(componname, index));
			
			if(taskAssignList==null||taskAssignList.size()==0)
			{
				nonAssignTask.add(task);
				continue;
			}
			boolean isbreak=false;
			for(HostPort hp:taskAssignList)
			{
				NodePort np=host2node.get(hp);
				if(np!=null)
				{
					UsedNodePort.add(np);
					LOG.info("assign:task:"+componname+"@"+task+"@"+index+",HostPort:"+hp.toString()+","+",NodePort:"+np.toString());
					rtn.put(task, np);
					isbreak=true;
					break;
				}
			}
			if(isbreak)
			{
				continue;
			}
			nonAssignTask.add(task);			
		}
		
		
		if (nonAssignTask.size() > 0 ) {
			int left=this.reassign_num-UsedNodePort.size();
			ArrayList<NodePort> randomList=new ArrayList<NodePort>();
			
			if(left>0)
			{
				List<NodePort> sortlist=sortSlots(nonAssignNodePort);
				randomList.addAll( sortlist.subList(0, Math.min(left, sortlist.size())));
			}
			if(randomList.size()>0)
			{
				LOG.info("random assign:nonAssignTask:"+nonAssignTask.toString()+",randomList:"+randomList.toString());

				rtn.putAll(this.randAssign(randomList, nonAssignTask,rtn));
			}else{
				LOG.error("nofreeNode"+nonAssignTask.toString()+">>"+reassignSlots.toString());
			}
		}
		
		return rtn;
	}
	
	public static List<NodePort> sortSlots(ArrayList<NodePort> allSlots) {
		List<NodePort> sortedFreeSlots=null;
		if(allSlots!=null){
			Map<String, List<NodePort>> tmp = new HashMap<String, List<NodePort>>();

			// group by first,按照node来分类
			for (Iterator<NodePort> it = allSlots.iterator(); it.hasNext();) {
				NodePort np =it.next();
				if (tmp.containsKey(np.getNode())) {
					List<NodePort> lst = tmp.get(np.getNode());
					lst.add(np);
					tmp.put(np.getNode(), lst);
				} else {
					List<NodePort> lst = new ArrayList<NodePort>();
					lst.add(np);
					tmp.put(np.getNode(), lst);
				}
			}

			// interleave
			List<List<NodePort>> splitup=new ArrayList<List<NodePort>>(tmp.values());
			sortedFreeSlots = StormUtils.interleave_all(splitup);
		}
		
		return sortedFreeSlots;
	}
	
	private Map<Integer, NodePort> randAssign(ArrayList<NodePort> randomNP,ArrayList<Integer> nonassign,Map<Integer, NodePort> assignRef)
	{
		Map<NodePort,ArrayList<Integer>> reref = new HashMap<NodePort,ArrayList<Integer>>();
		for(Entry<Integer, NodePort> e:assignRef.entrySet())
		{
			NodePort np=e.getValue();
			ArrayList<Integer> tlis=reref.get(np);
			if(tlis==null)
			{
				tlis=new ArrayList<Integer>();
				reref.put(np, tlis);
			}
			tlis.add(e.getKey());
		}

		
		Map<Integer, NodePort> rtn = new HashMap<Integer, NodePort>();
		int index = 0;
		if (nonassign != null && randomNP != null && randomNP.size() != 0) {
			for (Integer task : nonassign) {
				while(true)
				{
					if (index >= randomNP.size()) {
						index = 0;
					}
					
					NodePort np=randomNP.get(index);
					ArrayList<Integer> tlis=reref.get(np);
					if(tlis!=null&&tlis.size()>0)
					{
						tlis.remove(0);
						index++;
						continue;
					}
					rtn.put(task, np);
					index++;
					break;
				}
			}
		}
		return rtn;
	}

	@Override
	public void cleanup() {

	}
	
	private void taskIdToIndex()
	{
		Set<Integer> allTaskIds = StormUtils.listToSet(this.zkCluster.task_ids(this.topologyId));
		
		HashSet<String> componnames=new HashSet<String>();
		HashMap<String,ArrayList<Integer>> componname2Ids=new HashMap<String,ArrayList<Integer>>();
		for (Integer tid : allTaskIds) {
			TaskInfo info = this.zkCluster.task_info(this.topologyId, tid);
			if (info == null) {
				LOG.info("can`t find TaskInfo "+this.topologyId+","+ tid);
				continue;
			}
			taskId2TaskInfo.put(tid, info);
			
			String componentId=info.getComponentId();
			componnames.add(componentId);
			ArrayList<Integer> tasks=componname2Ids.get(componentId);
			if(tasks==null)
			{
				tasks=new ArrayList<Integer>();
				componname2Ids.put(componentId, tasks);
			}
			
			tasks.add(tid);
		}
		
		for(String componentId:componnames)
		{
			ArrayList<Integer> tasks=componname2Ids.get(componentId);
	        Collections.sort(tasks);
	        for(int j=0; j<tasks.size(); j++) {
	        	taskId2Index.put(new TaskId(componentId, tasks.get(j)), j);
	        }
		}
	}
	
	
	private ArrayList<TaskIndex> parseTaskIndexList(String taskIndexListstr)
	{
		ArrayList<TaskIndex> taskIndexList=new ArrayList<TaskIndex>();
		String[] comp_taskIndexs=taskIndexListstr.split("&");
		for(String comp:comp_taskIndexs)
		{
		
			String[] taskList=comp.split(":");
			if(taskList.length<2)
			{
				continue;
			}
			String componname=taskList[0];
			ArrayList<Integer> tasks=this.parseInts(taskList[1]);
			for(Integer index:tasks)
			{
				taskIndexList.add(new TaskIndex(componname, index));
			}
			
		
		}
		
		return taskIndexList;
	}
	
	private ArrayList<HostPort> parsePerHostPort(String hostportstr,ArrayList<HostPort> prev_hostports)
	{
		String[] hostSetlist=hostportstr.split("&");
		
		ArrayList<HostPort> hostports=new ArrayList<HostPort>();
		int hostports_index=0;
		for(String subhost:hostSetlist)
		{
			if(subhost==null||subhost.isEmpty())
			{
				continue;
			}
				String[] hostPorts=subhost.split(":");
				
				boolean isUsedPrefHost=false;
				if(hostPorts.length<1)
				{
					continue;
				}
				if(hostPorts.length==1)
				{
					isUsedPrefHost=true;
				}
				
				
				
				String hostname=null;
				ArrayList<Integer> ports=null;
				if(isUsedPrefHost)
				{
					hostname="prev";
					ports=this.parseInts(hostPorts[0]);
				}else{
					hostname=hostPorts[0];
					ports=this.parseInts(hostPorts[1]);
				}
				
				if(ports.size()==0)
				{
					continue;
				}
				
				for(Integer p:ports)
				{
					if(isUsedPrefHost)
					{
						String privhostname=null;
						if(prev_hostports!=null)
						{
							HostPort prevHostport=prev_hostports.get(hostports_index);
							if(prevHostport!=null)
							{
								privhostname=prevHostport.hostname;
							}
						}
						if(privhostname==null)
						{
							privhostname="mdrill_unset_hostname";
						}
						hostports.add(new HostPort(privhostname, p));
					}else{
						hostports.add(new HostPort(hostname, p));
					}
					hostports_index++;
				}
		}
		
		return hostports;
	}
	
	private void setEachAssignment(String ass)
	{	
		String[] cols=ass.split(";");
		if(cols.length<2)
		{
			return ;
		}
		
		ArrayList<TaskIndex> taskIndexList=this.parseTaskIndexList(cols[0]);
		if(taskIndexList.size()==0)
		{
			return ;
		}
		
		ArrayList<HostPort> prev_hostports=null;
		
		for(int j=1;j<cols.length;j++)
		{
			if(cols[j]==null||cols[j].isEmpty())
			{
				continue;
			}
			
			
			ArrayList<HostPort> hostports=this.parsePerHostPort(cols[j], prev_hostports);
			prev_hostports=hostports;

			int index=0;
			int maxsize=hostports.size();
			for(TaskIndex task:taskIndexList)
			{
				if(index>=maxsize)
				{
					index=0;
				}
				HostPort hp=hostports.get(index);
				index++;

				ArrayList<HostPort> list=this.taskIndexAssign.get(task);
				if(list==null)
				{
					list=new ArrayList<HostPort>();
					this.taskIndexAssign.put(task, list);
				}
				
				list.add(hp);
				this.hostports.add(hp);
			}
		}
	}
	
	private void initAssignment()
	{
		
		Object allowPortsstr=topology_conf.get(MDRILL_ASSIGNMENT_PORTS);
		if(allowPortsstr!=null)
		{
			ArrayList<Integer> ports=this.parseInts(String.valueOf(allowPortsstr));
			this.allowPorts.addAll(ports);
		}

		List<String> assignment=(List<String>) topology_conf.get(MDRILL_ASSIGNMENT_DEFAULT);
		if(assignment==null)
		{
			return ;
		}
		for(String ass:assignment)
		{
			if(ass==null||ass.isEmpty()||ass.startsWith("#"))
			{
				continue;
			}	
			
			this.setEachAssignment(ass);
		}
	}
	
	
	private ArrayList<Integer> parseInts(String str)
	{
		ArrayList<Integer> rtn=new ArrayList<Integer>();
		String[] list=str.split(",");
		for(String s:list)
		{
			String[] spans=s.split("~");
			int begin=Integer.parseInt(spans[0]);
			int end=begin;
			if(spans.length>1)
			{
				end=Integer.parseInt(spans[1]);
			}
			
			for(int i=begin;i<=end;i++)
			{
				rtn.add(i);
			}
		}
		
		return rtn;
	}

	public static class TaskIndex{
	
		private String componname;
		private int index;

		public TaskIndex(String componname, int index) {
			this.componname = componname;
			this.index = index;
		}
		@Override
		public String toString() {
			return "TaskIndex [componname=" + componname + ", index=" + index
					+ "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((componname == null) ? 0 : componname.hashCode());
			result = prime * result + index;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TaskIndex other = (TaskIndex) obj;
			if (componname == null) {
				if (other.componname != null)
					return false;
			} else if (!componname.equals(other.componname))
				return false;
			if (index != other.index)
				return false;
			return true;
		}

	

	}
	
	public static class TaskId{
		
		private String componname;
		private int tid;

		public TaskId(String componname, int index) {
			this.componname = componname;
			this.tid = index;
		}
		@Override
		public String toString() {
			return "TaskId [componname=" + componname + ", tid=" + tid + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((componname == null) ? 0 : componname.hashCode());
			result = prime * result + tid;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TaskId other = (TaskId) obj;
			if (componname == null) {
				if (other.componname != null)
					return false;
			} else if (!componname.equals(other.componname))
				return false;
			if (tid != other.tid)
				return false;
			return true;
		}

		

		
	}
	
	public static class HostPort{
		private String hostname;
	

		private  int port;

		public HostPort(String hostnme, int port) {
			this.hostname = hostnme;
			this.port = port;
		}
		@Override
		public String toString() {
			return "HostPort [hostname=" + hostname + ", port=" + port + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((hostname == null) ? 0 : hostname.hashCode());
			result = prime * result + port;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HostPort other = (HostPort) obj;
			if (hostname == null) {
				if (other.hostname != null)
					return false;
			} else if (!hostname.equals(other.hostname))
				return false;
			if (port != other.port)
				return false;
			return true;
		}

		
	}

}
