package com.alipay.bluewhale.core.daemon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.generated.ComponentCommon;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.ThriftTopologyUtils;
import backtype.storm.utils.Utils;

import com.alimama.mdrill.utils.UniqConfig;
import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.custom.CustomAssignment;
import com.alipay.bluewhale.core.custom.IAssignment;
import com.alipay.bluewhale.core.daemon.NodePort;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;
import com.alipay.bluewhale.core.task.TkHbCacheTime;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeat;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.utils.TimeUtils;

public class NimbusUtils {
	
	private static Logger LOG = Logger.getLogger(NimbusUtils.class);

	/**
	 * 验证是否分布式模式
	 * 
	 * @param conf
	 */
	public static void validate_distributed_mode(Map<?, ?> conf) {
		if (StormConfig.local_mode(conf)) {
			throw new IllegalArgumentException(
					"Cannot start server in local mode!");
		}

	}

	public static void mkAssignments(NimbusData nimbus, String topologyid)
			throws IOException {
		mkAssignments(nimbus, topologyid, false);
	}

	/**
	 * make assignments for a topology
	 * 
	 * get existing assignment (just the task->node+port map) -> default to {}
         * filter out ones which have a task timeout
         * figure out available slots on cluster. add to that the used valid slots to get total slots. figure out how many tasks should be in each slot (e.g., 4, 4, 4, 5)
         * only keep existing slots that satisfy one of those slots. for rest, reassign them across remaining slots
         * edge case for slots with no task timeout but with supervisor timeout... just treat these as valid slots that can be reassigned to. worst comes to worse the task will timeout and won't assign here next time around
	 * 
	 * @param data 
	 *            NimbusData
	 * @param topologyid
	 *            String
	 * @param isScratch
	 *            Boolean: isScratch is false unless rebalancing the topology
	 * @throws IOException
	 */
	public static  void mkAssignments(NimbusData data, String topologyid,
			boolean isScratch) throws IOException {
			LOG.debug("Determining assignment for " + topologyid);
			Map<?, ?> conf = data.getConf();
			StormClusterState stormClusterState = data.getStormClusterState();
			//定义zk callback事件
			RunnableCallback callback =new TransitionZkCallback(data, topologyid);
			//获取所有的supervisor节点信息。
			Map<String, SupervisorInfo> supInfos = allSupervisorInfo(stormClusterState, callback);
			//获取<supervisorid,hostname>map集合，例如：node->host {"4b83cd41-e863-4bd6-b26d-3e27e5ff7799" "dw-perf-3.alipay.net","b8f1664d-5555-4950-8139-5098fb109a81" "dw-perf-2.alipay.net"}
			Map<String, String> nodeHost = getNodeHost(supInfos);
			//获取指定topologyid的assignment信息。
			Assignment existingAssignment = stormClusterState.assignment_info(topologyid, null);
			//计算获取topology任务对应新的NodePort
	
			
			Map<Integer, NodePort> taskNodePort = computeNewTaskToNodePort(data,
					topologyid, existingAssignment, stormClusterState, callback,
					supInfos, isScratch);
	
		
			Map<String, String> allNodeHost = new HashMap<String, String>();
			
			if (existingAssignment != null){
			    allNodeHost = existingAssignment.getNodeHost();//这样是防止supervisor挂了，task还能依然工作？
			}
			
			if (nodeHost != null){
			    allNodeHost.putAll(nodeHost);
			}
			Set<Integer> reassignIds = null;
			if (existingAssignment != null && existingAssignment.getTaskToNodeport() != null){
			    reassignIds = changeIds(existingAssignment.getTaskToNodeport(),taskNodePort);
			}else{
			    //FIXME changeIds必须执行，否则startTimes->taskid有可能为null
			    reassignIds = changeIds(new HashMap<Integer, NodePort>(),taskNodePort);
			}
	
	
			//初始化开始时间
			Map<Integer, Integer> startTimes = new HashMap<Integer, Integer>();
			if (existingAssignment != null){
			    Map<Integer, Integer> taskStartTimeSecs = existingAssignment.getTaskStartTimeSecs();
			    if (taskStartTimeSecs!= null){
			        startTimes.putAll(taskStartTimeSecs);
			    }
			}
			//如果重新分配了，则要重置初始化时间
			if (reassignIds != null){
				int nowSecs = TimeUtils.current_time_secs();
			    for (Integer reassignid:reassignIds) {
			    	startTimes.put(reassignid, nowSecs);
			    }
			}
			
			
			//select-keys all-node->host (map first (vals task->node+port))
			Map<String, String> storeNodeHosts = new HashMap<String, String>();
	
			if (taskNodePort != null){
				HashSet<String> toSaveHosts=new HashSet<String>();
			    for (Entry<Integer, NodePort> entry:taskNodePort.entrySet()) {
			    	toSaveHosts.add((entry.getValue()).getNode());
			    }
			    
			    for(String node:toSaveHosts)
			    {
			    	String host=allNodeHost.get(node);
			    	storeNodeHosts.put(node, host);
			    }
			}else{
				storeNodeHosts.putAll(allNodeHost);
			}
			Assignment assignment = new Assignment(StormConfig.masterStormdistRoot(
					conf, topologyid), taskNodePort, storeNodeHosts, startTimes);
			if (assignment.equals(existingAssignment)) {
				LOG.debug("Assignment for " + topologyid + " hasn't changed");
			} else {
				LOG.info("Setting new assignment for storm id " + topologyid + ": "
						+ assignment);
				stormClusterState.set_assignment(topologyid, assignment);
			}
		

	}

	/**
	 * 获取supervisor节点列表。
	 * @param stormClusterState
	 * @param callback
	 * @return
	 */
	public static Map<String, String> getNodeHost(Map<String, SupervisorInfo> supInfos) {
		//TODO 需要优化，此处啰嗦，完全可以从allSupervisorInfo()里面获取该信息
		Map<String, String> rtn = null;
		if(supInfos!=null){
			rtn = new HashMap<String, String>();
			for (Entry<String, SupervisorInfo> entry:supInfos.entrySet()) {
				SupervisorInfo superinfo = entry.getValue();
				String supervisorid = entry.getKey();
				rtn.put(supervisorid, superinfo.getHostName());
			}
		}	
		return rtn;
	}

	/**
	 * get all SupervisorInfo of storm cluster
	 * 
	 * @param stormClusterState
	 * @param callback
	 * @return Map<String, SupervisorInfo> String: supervisorId SupervisorInfo:
	 *         [time-secs hostname worker-ports uptime-secs]
	 */
	public static Map<String, SupervisorInfo> allSupervisorInfo(
			StormClusterState stormClusterState, RunnableCallback callback) {
		Map<String, SupervisorInfo> rtn =null;
		//获取 /supervisors下面节点列表
		Set<String> supervisorIds = StormUtils.listToSet(stormClusterState.supervisors(callback));
		if(supervisorIds!=null){
			rtn= new HashMap<String, SupervisorInfo>();
			for (String supervisorId:supervisorIds) {
				 //获取/supervisors/supervisorid的节点值
				SupervisorInfo supervisorInfo = stormClusterState.supervisor_info(supervisorId);
				rtn.put(supervisorId, supervisorInfo);
			}
		}
		
		return rtn;
	}

	/**
	 * assigned new tasks to <node,port>
	 * 
	 * @param conf
	 * @param tablename
	 * @param existingAssignment
	 * @param stormClusterState
	 * @param callback
	 * @param taskHeartbeatsCache
	 * @param isScratch
	 * @return Map<Integer, NodePort> <taskid, NodePort>
	 * @throws IOException
	 */
	public static Map<Integer, NodePort> computeNewTaskToNodePort(NimbusData data,
			String topologyid, Assignment existingAssignment,
			StormClusterState stormClusterState, RunnableCallback callback,
			Map<String, SupervisorInfo> supInfos, boolean isScratch)
			throws IOException {
		
		//taskheartcache(Map<stormid, Map<taskid, Map<tkHbCacheTime, time>>>) 
		ConcurrentHashMap<String, Map<Integer, Map<TkHbCacheTime, Integer>>> taskHeartbeatsCache=data.getTaskHeartbeatsCache();
		Map<?, ?> topology_conf=readStormConf(data.getConf(),topologyid);
		//获取所有可用Slots,不包括当前topology timeout的端口，在后面会对timeout的端口进行add
		Set<NodePort> availableSlots = availableSlots(supInfos, stormClusterState, callback);
		//获取所有taskid==/tasks/topologyid/下节点列表
		Set<Integer> allTaskIds = StormUtils.listToSet(stormClusterState.task_ids(topologyid));
		Map<NodePort, List<Integer>> existingAssigned = new HashMap<NodePort, List<Integer>> ();
		if (existingAssignment != null)
		{
		    existingAssigned = StormUtils.reverse_map(existingAssignment.getTaskToNodeport());// Map<NodePort, List>
		}

		//通过taskheartcache计算获取当前存活的task
		Set<Integer> aliveIds = null;
		if (isScratch) {
			//isScratch rebalance判断，
			aliveIds = allTaskIds;
		}else if (existingAssignment != null){
			aliveIds = aliveTasks(data.getConf(), topologyid, stormClusterState, allTaskIds,
					existingAssignment.getTaskStartTimeSecs(),taskHeartbeatsCache);			
		}
		Map<NodePort, List<Integer>> aliveAssigned = new HashMap<NodePort, List<Integer>>();
	
		if (existingAssignment != null){
		   //获取已被分配的NodePort和task列表信息
		    for (Entry<NodePort, List<Integer>> entry:existingAssigned.entrySet()) {
		    	NodePort np=entry.getKey();
		    	List<Integer> tasks=entry.getValue();
			    if (aliveIds != null && aliveIds.containsAll(tasks)) {			
			    	//如果有一个taskid不存在于aliveIds中，则该NodePort上面task都会被重新分配。
			    	aliveAssigned.put(np, tasks);
			    }
		    }
		}

		//获取TOPOLOGY_WORKERS
		Integer workers = 0;
		if (topology_conf.get(Config.TOPOLOGY_WORKERS) instanceof Long){
		    Long tmp = (Long) topology_conf.get(Config.TOPOLOGY_WORKERS);
		    workers  = Integer.parseInt(tmp.toString());
		}else{
		    workers = (Integer) topology_conf.get(Config.TOPOLOGY_WORKERS);
		}

		//获取总的slots使用数
		int totalSlotsToUse = Math.min(workers,
				availableSlots.size() + aliveAssigned.size());

		IAssignment customAssignment=CustomAssignment.getAssignmentInstance(topology_conf);

		Map<NodePort, List<Integer>> keepAssigned =null;
		if (!isScratch && allTaskIds!= null) {
			//获取保持均衡的nodeport和task的分配信息
	        if (customAssignment != null) {
	        	keepAssigned=customAssignment.keeperSlots(aliveAssigned, allTaskIds.size(),totalSlotsToUse);
	        }else{
	        	keepAssigned = keeperSlots(aliveAssigned, allTaskIds.size(),totalSlotsToUse);// <NodePort, List>
	        }
		}
		//重新分配数
		int reassign_num=totalSlotsToUse;
		if(keepAssigned!=null){
			for (Entry<NodePort, List<Integer>> entry:keepAssigned.entrySet()) {
				aliveAssigned.remove(entry.getKey());
			}
			reassign_num=totalSlotsToUse-keepAssigned.size();
		}
		//获取所有空闲的slot
		Set<NodePort> freedSlots = new HashSet<NodePort>();
		Set<NodePort> freedSlotstmp = aliveAssigned.keySet();
		if (freedSlotstmp.size() == 0){
		    freedSlotstmp = new HashSet<NodePort>();
		}
		freedSlots.addAll(freedSlotstmp);
		freedSlots.addAll(availableSlots);
        if (customAssignment != null) {
            customAssignment.setup(topology_conf, topologyid, stormClusterState, keepAssigned,supInfos);
        }


		Collection<List<Integer>> keepAssignedTaskSet = null;
		if (keepAssigned != null){
		    keepAssignedTaskSet=keepAssigned.values();
		}
		Set<Integer> reassignIds = null;
		if (allTaskIds != null){
		    reassignIds = new HashSet<Integer>(allTaskIds);
		}
		if (keepAssignedTaskSet != null && reassignIds != null){
		    //FIXME 不能直接对keepAssignedTaskSet进行removeAll,无效的 yannian add
		    for(List<Integer> rm:keepAssignedTaskSet)
		    {
		    	reassignIds.removeAll(rm);
		    }
		}
		//分配未keep的task到相应的slot上面。
		Map<Integer, NodePort> reassignment = new HashMap<Integer, NodePort>();
		List<NodePort> reassignSlots=null;
		List<NodePort> sortedFreeSlots = sortSlots(freedSlots);
            //获取排序后，需要被分配的slots节点
            if (customAssignment != null) {
                reassignSlots = customAssignment.slotsAssignment(sortedFreeSlots, reassign_num,reassignIds);
            } else {
                
                if (sortedFreeSlots != null) {
                    reassignSlots = sortedFreeSlots.subList(0, reassign_num);
                }
            }
        //分配未keep的task到相应的slot上面。
        if(customAssignment!=null)
        {
            reassignment=customAssignment.tasksAssignment(reassignSlots, reassignIds);
        }else{
           
    		int index = 0;
    		if (reassignIds != null && reassignSlots != null && reassignSlots.size() != 0)
    		{       
    		    for (Iterator<Integer> it = reassignIds.iterator(); it.hasNext();) {
    			Integer entry = it.next();
    			if (index >= reassignSlots.size()) {
    				index = 0;
    			}
    			reassignment.put(entry, reassignSlots.get(index));
    			index++;
    		    }
    		}
        }

		//转换获取keepAssigned，得到key为taskid，value为NodePort的map
		Map<Integer, NodePort> stayAssignment = new HashMap<Integer, NodePort>();
		if(keepAssigned!=null){
		    for (Entry<NodePort, List<Integer>> entry:keepAssigned.entrySet()) {
				NodePort np = entry.getKey();
				List<Integer> tasks = entry.getValue();
				for (Integer taskid : tasks) {
					stayAssignment.put(taskid, np);
				}
		    }
		}

		if (reassignment.size() > 0) {
			LOG.info("Reassigning " + topologyid + " to " + totalSlotsToUse	+ " slots isScratch="+isScratch+",totalSlotsToUse="+totalSlotsToUse);
			LOG.info("Reassign ids: " + reassignIds+",keepAssigned="+keepAssigned+",existingAssigned="+existingAssigned);
			LOG.info("Available slots: " + availableSlots);
		}
		//汇总所有重新分配结果。
		reassignment.putAll(stayAssignment);
		
		if (customAssignment != null) {
            customAssignment.cleanup();
        }
		return reassignment;

	}

	/**
	 * get all taskids which should be reassigned
	 * 
	 * @param taskToNodePort
	 * @param newtaskToNodePort
	 * @return Set<Integer> taskid which should reassigned
	 */
	public static Set<Integer> changeIds(Map<Integer, NodePort> taskToNodePort,
			Map<Integer, NodePort> newtaskToNodePort) {
		Map<NodePort, List<Integer>> slotAssigned = StormUtils.reverse_map(taskToNodePort);
		Map<NodePort, List<Integer>> newSlotAssigned = StormUtils.reverse_map(newtaskToNodePort);
		Set<Integer> brandNewSlots = new HashSet<Integer>();
		//返回在newtaskToNodePort，不在taskToNodePort的value值集合。
		for (Entry<NodePort, List<Integer>> entry:newSlotAssigned.entrySet() ){
			
			List<Integer> tasks=slotAssigned.get(entry.getKey());
			List<Integer> lst = entry.getValue();
			if (tasks==null|| tasks.size()!=lst.size()||!lst.containsAll(tasks)) {
				brandNewSlots.addAll(lst);
			}
		}
		return brandNewSlots;
	}

	/**
	 * sort slots, the purpose is to ensure that the tasks are assigned in
	 * balancing
	 * 
	 * @param allSlots
	 * @return List<NodePort>
	 * 
	 */
	public static List<NodePort> sortSlots(Set<NodePort> allSlots) {
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

	/**
	 * keep slots which are assigned tasks in balancing : integer_divided() will
	 * return tasks assigned results
	 * 
	 * @param aliveAssigned
	 * @param numTaskIds
	 * @param numWorkers
	 * @return those tasks of slots will be keep
	 */
	public static Map<NodePort, List<Integer>> keeperSlots(
			Map<NodePort, List<Integer>> aliveAssigned, int numTaskIds,
			int numWorkers) {
		Map<NodePort, List<Integer>> keepers = null;

		if (numWorkers != 0) {
			keepers = new HashMap<NodePort, List<Integer>>();
			Map<Integer, Integer> distribution = StormUtils.integer_divided(numTaskIds, numWorkers);

			for (Entry<NodePort, List<Integer>> entry:aliveAssigned.entrySet()) {
				NodePort nodeport = entry.getKey();
				List<Integer> tasklist = entry.getValue();
				Integer taskCount = tasklist.size();
				Integer val = distribution.get(taskCount);
				if (val != null && val > 0) {
				    keepers.put(nodeport, tasklist);
				    distribution.put(taskCount,val - 1);
				}
			}
		}
		return keepers;
	}

	/**
	 * find all ports which can be assigned
	 * 
	 * @param conf
	 * @param stormClusterState
	 * @param callback
	 * @return Set<NodePort> : form supervisorid to ports
	 * 
	 */
	public static Set<NodePort> availableSlots(Map<String, SupervisorInfo> supervisorInfos,
			StormClusterState stormClusterState, RunnableCallback callback) {

		Set<NodePort> rtn = new HashSet<NodePort>();
		//TODO 是否可以减少多次调用
		//Set<String> supervisorIds = StormUtils.listToSet(stormClusterState.supervisors(callback));
		List<String> supervisorIds = stormClusterState.supervisors(callback);
		if (supervisorIds == null || supervisorInfos == null) {
			return rtn;
		}

		//获取所有的Slots。例如(["b8f1664d-5555-4950-8139-5098fb109a81" 6700] ["b8f1664d-5555-4950-8139-5098fb109a81" 6701])
		Map<String, List<Integer>> allSlots = new HashMap<String, List<Integer>>();
		for (Entry<String, SupervisorInfo> entry:supervisorInfos.entrySet()) {
			allSlots.put(entry.getKey(),entry.getValue().getWorkPorts());
		}

		//获取已经分配的slots
		Map<String, Set<Integer>> assignedSlots = assigned_Slots(stormClusterState);

		//获取可分配的slots
		for (Entry<String, List<Integer>> entry:allSlots.entrySet()) {
			String supervisorid =entry.getKey();
			//supervisor对应的所有port列表
			List<Integer> s = entry.getValue();
			if (assignedSlots != null) {
				//获取该supervisor已经分配的port列表
				Set<Integer> e =assignedSlots.get(entry.getKey());
				if (e != null){
				    s.removeAll(e);
				}
			}
			for (Integer port:s) {
				rtn.add(new NodePort(supervisorid,port));
			}	
		}
		return rtn;
	}
	
	


	/**
	 * stormconf is mergered into clusterconf
	 * 
	 * @param conf
	 * @param stormId
	 * @return Map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map readStormConf(Map conf, String stormId) {
		String stormroot = StormConfig.masterStormdistRoot(conf, stormId);
		Map stormconf = null;
		try {
			stormconf = (Map) Utils.deserialize(FileUtils
					.readFileToByteArray(new File(StormConfig
							.masterStormconfPath(stormroot))));
		} catch (IOException e) {
			LOG.error(e + "readStormConf exception");
		}
		Map rtn = new HashMap();
		rtn.putAll(conf);
		rtn.putAll(stormconf);
		return rtn;
	}

	/**
	 * find all alived taskid
	 * 
	 * Does not assume that clocks are synchronized. Task heartbeat is only used so that
	 * nimbus knows when it's received a new heartbeat. All timing is done by nimbus and
	 * tracked through task-heartbeat-cache
	 * 
	 * @param conf
	 * @param topologyid
	 * @param stormClusterState
	 * @param taskIds
	 * @param taskStartTimes
	 * @param taskHeartbeatsCache --Map<stormid, Map<taskid, Map<tkHbCacheTime, time>>> 
	 * @return Set<Integer> : taskid
	 */
	public static Set<Integer> aliveTasks(Map<?,?> conf, String topologyid,
			StormClusterState stormClusterState, Set<Integer> taskIds,
			Map<Integer,Integer> taskStartTimes, ConcurrentHashMap<String, Map<Integer, Map<TkHbCacheTime, Integer>>> taskHeartbeatsCache) {


		Set<Integer> rtn = null;

		if(taskHeartbeatsCache==null){
			taskHeartbeatsCache=new ConcurrentHashMap<String, Map<Integer, Map<TkHbCacheTime, Integer>>>();
		}
		if(taskIds!=null){
			 rtn = new HashSet<Integer>();
			for (Integer taskId:taskIds) {
				//获取task心跳对象
				TaskHeartbeat taskHeartbeat = stormClusterState.task_heartbeat(topologyid, taskId);

				Integer reportTime = null;
				if (taskHeartbeat != null) {
					reportTime = taskHeartbeat.getTimeSecs();
				}

				Map<Integer, Map<TkHbCacheTime, Integer>> val =taskHeartbeatsCache.get(topologyid);
				if(val==null)
				{
					LOG.info("topologyid is null:topologyid"+topologyid);
				    val=new HashMap<Integer, Map<TkHbCacheTime, Integer>>();
				    taskHeartbeatsCache.put(topologyid, val);
				}
				

			    Map<TkHbCacheTime, Integer> last = taskHeartbeatsCache.get(topologyid).get(taskId);
			    if(last==null)
			    {
			    	LOG.info("topologyid taskid is null:topologyid:"+topologyid+",taskId:"+taskId);
			    	last = new HashMap<TkHbCacheTime, Integer>();
					val.put(taskId, last);
			    }
			    
			    Integer lastNimbusTime =last.get(TkHbCacheTime.nimbusTime);
			    Integer lastReportedTime =last.get(TkHbCacheTime.taskReportedTime);
			    if(reportTime!=null)
				{
					last.put(TkHbCacheTime.taskReportedTime, reportTime);
				}
			
				//获取task启动时间
				Integer taskStartTime =taskStartTimes.get(taskId);
				
				Integer nimbusTime=null;
				if(lastNimbusTime==null||lastReportedTime==null)
				{
					nimbusTime = TimeUtils.current_time_secs();
				}else if(!lastReportedTime.equals(reportTime))
				{
					nimbusTime = TimeUtils.current_time_secs();
				}else{
					nimbusTime = lastNimbusTime;
				}
				
				last.put(TkHbCacheTime.nimbusTime, nimbusTime);
				if (taskStartTime != null ) {
					boolean isLantch=TimeUtils.time_delta(taskStartTime) < (Integer) conf.get(Config.NIMBUS_TASK_LAUNCH_SECS);
					if(isLantch)
					{
						rtn.add(taskId);
						continue;
					}
					
					if(nimbusTime == null)
					{
						rtn.add(taskId);
						continue;
					}
					
					boolean ishb=TimeUtils.time_delta(nimbusTime) < (Integer) conf.get(Config.NIMBUS_TASK_TIMEOUT_SECS);
					
					if(ishb)
					{
						rtn.add(taskId);
					}else{
						LOG.info("Task2 " + topologyid + ":" + taskId + " timed out taskStartTime="+String.valueOf(taskStartTime)+",nimbustime="+String.valueOf(nimbusTime));
					}
				} else {

					LOG.info("Task " + topologyid + ":" + taskId + " timed out taskStartTime="+String.valueOf(taskStartTime)+",nimbustime="+String.valueOf(nimbusTime));
				}

			}
		}
		
		return rtn;
	}

	/**
	 * find all assigned slots of the storm cluster
	 * 
	 * @param stormClusterState
	 * @return Map<String, Set<Integer>> : from node-id to a set of ports
	 */
	public static Map<String, Set<Integer>> assigned_Slots(
			StormClusterState stormClusterState) {

		//获取/assignments下节点列表 {topologyid}
		Set<String> assignments = StormUtils.listToSet(stormClusterState.assignments(null));
		Map<String, Set<Integer>> rtn = null;

		if (assignments != null) {
			rtn = new HashMap<String, Set<Integer>>();
			for (String topologyid:assignments) {
				Assignment assignment = stormClusterState.assignment_info(topologyid,null);
				if (assignment == null) {
					continue;
				}
				//例：:task->node+port {1 ["b8f1664d-5555-4950-8139-5098fb109a81" 6702], 2 ["4b83cd41-e863-4bd6-b26d-3e27e5ff7799" 6701]}
				Map<Integer, NodePort> taskNodePort = assignment.getTaskToNodeport();
				if(taskNodePort!=null){
					for (Entry<Integer, NodePort> entry:taskNodePort.entrySet()) {
						NodePort np =entry.getValue();
						Set<Integer> ports = rtn.get(np.getNode());
						if(ports==null)
						{
							ports=new HashSet<Integer>(); 
							rtn.put(np.getNode(), ports);
						}
						ports.add(np.getPort());
					}
				}
			}
		}
		return rtn;
	}
        /**
         * add coustom KRYO serialization
         * 
         */
	private static Map mapifySerializations(List sers) {
		Map rtn =  new HashMap();
		if (sers != null){
		    int size = sers.size();
		    for (int i = 0; i < size; i++) {
		        if (sers.get(i) instanceof Map) {
			    rtn.putAll((Map) sers.get(i));
		         } else {
			    rtn.put(sers.get(i), null);
		         }
		    }
		}
		return rtn;
	}
	public static Map normalizeConf(Map conf, Map stormConf, StormTopology topology) {

		List baseSers = (List) stormConf.get(Config.TOPOLOGY_KRYO_REGISTER);
	        if (baseSers == null) {
		    baseSers = (List) conf.get(Config.TOPOLOGY_KRYO_REGISTER);
		}
		Set<String> cids = ThriftTopologyUtils.getComponentIds(topology);
		List componentSers = new ArrayList();
		for (Iterator it = cids.iterator(); it.hasNext();) {
		    String componentId = (String) it.next();
		    ComponentCommon common = ThriftTopologyUtils.getComponentCommon(
			    topology, componentId);
		    String json = common.get_json_conf();
		    if (json != null){
		        Map mtmp = (Map) StormUtils.from_json(json);
		        List ltmp = (List) mtmp.get(Config.TOPOLOGY_KRYO_REGISTER);
		        componentSers.add(ltmp);
		    }
		}

		Map totalConf = new HashMap();
		totalConf.putAll(conf);
		totalConf.putAll(stormConf);

		Map rtn = new HashMap();
		rtn.putAll(stormConf);
		Map map = new HashMap();
		map.putAll(mapifySerializations(componentSers));
		map.putAll(mapifySerializations(baseSers));
		rtn.put(Config.TOPOLOGY_KRYO_REGISTER, map);
		rtn.put(Config.TOPOLOGY_ACKERS, totalConf.get(Config.TOPOLOGY_ACKERS));
		return rtn;
	}
}
