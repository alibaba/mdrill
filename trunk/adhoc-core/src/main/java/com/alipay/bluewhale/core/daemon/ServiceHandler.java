package com.alipay.bluewhale.core.daemon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.thrift7.TException;

import backtype.storm.Config;
import backtype.storm.daemon.Shutdownable;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.Bolt;
import backtype.storm.generated.ClusterSummary;
import backtype.storm.generated.ComponentCommon;
import backtype.storm.generated.ErrorInfo;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.Nimbus.Iface;
import backtype.storm.generated.NotAliveException;
import backtype.storm.generated.RebalanceOptions;
import backtype.storm.generated.SpoutSpec;
import backtype.storm.generated.StateSpoutSpec;
import backtype.storm.generated.StormTopology;
import backtype.storm.generated.SupervisorSummary;
import backtype.storm.generated.TaskStats;
import backtype.storm.generated.TaskSummary;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.generated.TopologySummary;
import backtype.storm.utils.BufferFileInputStream;
import backtype.storm.utils.TimeCacheMap;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.cluster.Common;
import com.alipay.bluewhale.core.cluster.DaemonCommon;
import com.alipay.bluewhale.core.cluster.StormBase;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.cluster.StormStatus;
import com.alipay.bluewhale.core.daemon.supervisor.SupervisorInfo;
import com.alipay.bluewhale.core.stats.BaseStatsData;
import com.alipay.bluewhale.core.task.common.Assignment;
import com.alipay.bluewhale.core.task.common.TaskInfo;
import com.alipay.bluewhale.core.task.error.TaskError;
import com.alipay.bluewhale.core.task.heartbeat.TaskHeartbeat;
import com.alipay.bluewhale.core.thrift.Thrift;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.utils.TimeUtils;

public class ServiceHandler implements Iface, Shutdownable, DaemonCommon {
	// TODO 后续Conf和StormConf泛型问题处理

	private final static Logger LOG = Logger.getLogger(ServiceHandler.class);

	private NimbusData data;

	private Map<Object, Object> conf;

	public ServiceHandler(NimbusData data) {
		this.data = data;
		conf = data.getConf();
	}
        /** 提交一个topology
         * @param topologyname
         *        String：topology的名字
         * @param uploadedJarLocation
         *        String：jar包所在目录
         * @param jsonConf
         *        String：配置文件
         * @param topology
         *        StormTopology：topology的信息 
         */
	@SuppressWarnings("unchecked")
	@Override
	public void submitTopology(String topologyname, String uploadedJarLocation,
			String jsonConf, StormTopology topology)
			throws AlreadyAliveException, InvalidTopologyException, TException {

		try {
			checkTopologyActive(data, topologyname, false);
		} catch (NotAliveException e) {
			LOG.info(e.get_msg());
		}
		data.getSubmittedCount().incrementAndGet();
		String topologyId = topologyname + "-" + data.getSubmittedCount().get()
				+ "-" + TimeUtils.current_time_secs();
		// TODO json串反序列化成对象,可以换掉此处转换方式
		Map<Object, Object> serializedConf = (Map<Object, Object>) StormUtils
				.from_json(jsonConf);
		if (serializedConf == null) {
			serializedConf = new HashMap<Object, Object>();
		}
		serializedConf.put(Config.STORM_ID, topologyId);// not used now
		// 注册自定义序列化方式
		Map<Object, Object> stormConf = NimbusUtils.normalizeConf(conf,
				serializedConf, topology);
		Map<Object, Object> totalStormConf = new HashMap<Object, Object>(conf);
		totalStormConf.putAll(stormConf);
		totalStormConf.putAll(serializedConf);
		StormTopology newtopology = new StormTopology(topology);
		// TODO TOPOLOGY_OPTIMIZE
		// if ((Boolean) totalStormConf.get(Config.TOPOLOGY_OPTIMIZE)) {
		// newtopology = optimizeTopology(topology);
		// }
		StormClusterState stormClusterState = data.getStormClusterState();
		// this validates the structure of the topology
		Common.system_topology(totalStormConf, newtopology);
		LOG.info("Received topology submission for " + topologyname
				+ " with conf " + serializedConf);
		synchronized (data.getSubmitLock()) {
			try {
				// 上传code，并序列化topology和配置信息
				setupStormCode(conf, topologyId, uploadedJarLocation,
						serializedConf, newtopology);
				// 设置心跳
				stormClusterState.setup_heartbeats(topologyId);
				//为task分配对应componentid
				setupStormStatic(conf, topologyId, stormClusterState);
				//make assignments for a topology
				NimbusUtils.mkAssignments(data, topologyId);
				//启动topology：设置状态为:active
				startTopology(topologyname, stormClusterState, topologyId);
			} catch (IOException e) {
				LOG.info("setupStormCode stormId: " + topologyId
						+ " uploadedJarLocation: " + uploadedJarLocation
						+ "failed; OR" + "mkAssignments stormId: " + topologyId
						+ " failed!");
			}
		}

	}

	/** 杀掉一个topology
         * @param topologyname
         *        String：topology的名字
         */
	@Override
	public void killTopology(String name) throws NotAliveException, TException {
		killTopologyWithOpts(name, new KillOptions());

	}

	@Override
	public void killTopologyWithOpts(String topologyName, KillOptions options)
			throws NotAliveException, TException {
		try {
		        //判断topology是不是active的状态
			checkTopologyActive(data, topologyName, true);
			int wait_amt = 0;
			if (options.is_set_wait_secs()) {
				wait_amt = options.get_wait_secs();
			}
			//将topology的状态置为killed
			StatusTransition.transitionName(data, topologyName, true,StatusType.kill, wait_amt);
		} catch (AlreadyAliveException e) {
			LOG.error("KillTopology Error", e);
		}

	}

	/** 使一个topology的状态变为active
         * @param topologyname
         *        String：topology的名字
         */
	@Override
	public void activate(String topologyName) throws NotAliveException,
			TException {
		StatusTransition.transitionName(data, topologyName, true,StatusType.activate);
	}

	/** 使一个topology的状态变为inactive
         * @param topologyname
         *        String：topology的名字
         */
	@Override
	public void deactivate(String topologyName) throws NotAliveException,
			TException {
		StatusTransition.transitionName(data, topologyName, true,StatusType.inactivate);
	}

	/** 使一个topology重新分发任务，目的是为了使得已经运行的topology能够达到新的负载均衡，
	 *  例如：集群中增加了supervisor节点后，希望已经运行的topology也能够负载均衡
         * @param topologyname
         *        String：topology的名字
         * @param options
         *        RebalanceOptions： 设置一个延迟执行的时间，目的是为了让topology有时间将正在执行未执行完的工作做完
         */
	@Override
	public void rebalance(String topologyName, RebalanceOptions options)
			throws NotAliveException, TException {
		
		try {
			checkTopologyActive(data, topologyName, true);
			int wait_amt=0;
			if (options.is_set_wait_secs()) {
				wait_amt = options.get_wait_secs();
			}
			StatusTransition.transitionName(data, topologyName, true,StatusType.rebalance, wait_amt);
		} catch (AlreadyAliveException e) {
			LOG.error("Rebalance Error", e);
		}
		
	}

	/** 
	 * 以下几个方法主要完成jar包的提交，具体的使用可以参考StormSubmitter中submitJar方法
         */
	@Override
	public String beginFileUpload() throws TException {
		String fileLoc = StormConfig.masterInbox(conf) + "/stormjar-"+ UUID.randomUUID() + ".jar";
		try {
			data.getUploaders().put(fileLoc,Channels.newChannel(new FileOutputStream(fileLoc)));
			LOG.info("Uploading file from client to " + fileLoc);
		} catch (FileNotFoundException e) {
			LOG.error(" file not found " + fileLoc);
		}
		return fileLoc;
	}

	@Override
	public void uploadChunk(String location, ByteBuffer chunk)
			throws TException {
		TimeCacheMap<Object,Object> uploaders = data.getUploaders();
		Object obj = uploaders.get(location);
		if (obj == null) {
			throw new RuntimeException(
					"File for that location does not exist (or timed out)");
		}
		try {
			if(obj instanceof WritableByteChannel){
				WritableByteChannel channel=(WritableByteChannel)obj;
				channel.write(chunk);
				uploaders.put(location, channel);
			}
		} catch (IOException e) {
			LOG.error(" WritableByteChannel write filed when uploadChunk "
					+ location);
		}

	}

	@Override
	public void finishFileUpload(String location) throws TException {
		TimeCacheMap<Object,Object> uploaders = data.getUploaders();
		Object obj = uploaders.get(location);
		if (obj == null) {
			throw new RuntimeException(
					"File for that location does not exist (or timed out)");
		}
		try {
			if(obj instanceof WritableByteChannel){
				WritableByteChannel channel=(WritableByteChannel)obj;
				channel.close();
				LOG.info("Finished uploading file from client: " + location);
				uploaders.remove(location);
			}
		} catch (IOException e) {
			LOG.error(" WritableByteChannel close failed when finishFileUpload "
					+ location);
		}

	}

	@Override
	public String beginFileDownload(String file) throws TException {
		BufferFileInputStream is = null;
		String id = null;
		try {
			is = new BufferFileInputStream(file);
			id = UUID.randomUUID().toString();
			data.getDownloaders().put(id, is);
		} catch (FileNotFoundException e) {
			LOG.error(e + "file:" + file + " not found");
		}

		return id;
	}

	@Override
	public ByteBuffer downloadChunk(String id) throws TException {
		TimeCacheMap<Object,Object> downloaders = data.getDownloaders();
		Object obj = downloaders.get(id);
		if (obj == null) {
			throw new RuntimeException(
					"Could not find input stream for that id");
		}
		byte[] ret = null;
		try {
			if(obj instanceof BufferFileInputStream){
				BufferFileInputStream is=(BufferFileInputStream) obj;
				ret=is.read();
				if (ret != null) {
					downloaders.put(id, (BufferFileInputStream) is);
				}
			}
		} catch (IOException e) {
			LOG.error(e
					+ "BufferFileInputStream read failed when downloadChunk ");
		}

		return ByteBuffer.wrap(ret);
	}

	
	/** 获得集群的信息ClusterSummary
	 *  (1)supervisorSummaries
	 *   所有supervisor的信息：主机名、运行时间、worker数和已用的worker数
	 *  (2)nimbus的运行时间
	 *  (3)topologySummaries
	 *  所有topology的信息：topologyid、topologyname、任务数、worker数、运行时间和状态
	 *  @return ClusterSummary
         */
	@Override
	public ClusterSummary getClusterInfo() throws TException {
		StormClusterState stormClusterState = data.getStormClusterState();
		//获得已分配的端口和端口上运行的任务
		Map<String, Set<Integer>> assigned = NimbusUtils
				.assigned_Slots(stormClusterState);
		//获得所有supervisor的信息
		Map<String, SupervisorInfo> supervisorInfos = NimbusUtils
				.allSupervisorInfo(stormClusterState, null);
		//获得supervisorSummaries
		List<SupervisorSummary> supervisorSummaries = new ArrayList<SupervisorSummary>();
		if (supervisorInfos == null) {
			supervisorInfos = new HashMap<String, SupervisorInfo>();
		}
		Set<Entry<String, SupervisorInfo>> sinfoEntry = supervisorInfos.entrySet();
		for (Iterator<Entry<String, SupervisorInfo>> it = sinfoEntry.iterator(); it.hasNext();) {
			Entry<String, SupervisorInfo> entry = it.next();
			String supervisorId = entry.getKey();
			SupervisorInfo info = entry.getValue();
			
			List<Integer> ports = info.getWorkPorts();
			int num_used_workers = 0;
			int num_workers = 0;
			if (assigned != null && assigned.get(supervisorId) != null){
			    num_used_workers = assigned.get(supervisorId).size();
			}
                        if (ports != null){
                            num_workers = ports.size();
			}
			supervisorSummaries.add(new SupervisorSummary(info.getHostName(),
					info.getUptimeSecs(),num_workers , num_used_workers));
		}
		//获得nimbus的运行时间
		int uptime = data.uptime();
		
		//获得所有状态为active的topology
		Map<String, StormBase> bases = Common.topology_bases(stormClusterState);

		if (bases == null) {
			bases = new HashMap<String, StormBase>();
		}

		//获得所有集群的信息
		List<TopologySummary> topologySummaries = new ArrayList<TopologySummary>();

		Set<Entry<String, StormBase>> basesEntry = bases.entrySet();
		for (Iterator<Entry<String, StormBase>> it = basesEntry.iterator(); it.hasNext();) {
			Entry<String, StormBase> entry = it.next();
			String stormId = entry.getKey();
			StormBase base = entry.getValue();
			Assignment assignment = stormClusterState.assignment_info(stormId,null);
			if (assignment != null) {
				HashSet<NodePort> workers = new HashSet<NodePort>();
				Collection<NodePort> entryColl = assignment.getTaskToNodeport().values();
				workers.addAll(entryColl);
				topologySummaries.add(new TopologySummary(stormId, base.getStormName(), assignment.getTaskToNodeport().size(),
						workers.size(), TimeUtils.time_delta(base
								.getLanchTimeSecs()), extractStatusStr(base)));
			}
		}
		return new ClusterSummary(supervisorSummaries, uptime,topologySummaries);
	}

	/** 获得topology的信息TopologyInfo
	 *  (1)topologyid
	 *  (2)topologyname
	 *  (3)uptime_secs
	 *     运行时间
	 *  (4)TaskSummaries
	 *     所有任务的信息：任务id, component_id、 主机名、运行时间，
	 *     出现的错误、任务的状态（发送，传输，完成时间，处理时间等等，详见TaskStats类）
	 *  (5)status
	 *     topology的状态
	 *  @return TopologyInfo
         */
	@Override
	public TopologyInfo getTopologyInfo(String topologyId)	throws NotAliveException, TException {
		
		TopologyInfo topologyInfo=null;
		
		StormClusterState stormClusterState = data.getStormClusterState();
		
		//获得topology的任务信息： 任务ID 和 componentid
		HashMap<Integer, String> taskInfo = Common.topology_task_info(stormClusterState, topologyId);// <taskid,componentid>
		
		//获得topology的name、发起时间和状态
		StormBase base = stormClusterState.storm_base(topologyId, null);
		
		//获得topology的任务分配信息
		Assignment assignment = stormClusterState.assignment_info(topologyId,null);
		
		if (base != null && assignment != null) {
			List<TaskSummary> taskSummarys = new ArrayList<TaskSummary>();
			Set<Entry<Integer, String>> taskInfoSet = taskInfo.entrySet();
			for (Iterator<Entry<Integer, String>> it = taskInfoSet.iterator(); it.hasNext();) {
				Entry<Integer, String> entry =  it.next();
				Integer taskId =  entry.getKey();
				String componentId =  entry.getValue();
				NodePort np = (NodePort) assignment.getTaskToNodeport().get(taskId);
				//获得指定task的心跳信息
				TaskHeartbeat heartbeat = stormClusterState.task_heartbeat(topologyId, taskId);
				if (np == null || heartbeat == null) {
					continue;
				}
				String host = (String) assignment.getNodeHost().get(np.getNode());
				List<TaskError> errors = stormClusterState.task_errors(topologyId, taskId);
				List<ErrorInfo> newErrors = new ArrayList<ErrorInfo>();

				if (errors != null) {
					int size = errors.size();
					for (int i = 0; i < size; i++) {
						TaskError e = (TaskError) errors.get(i);
						newErrors.add(new ErrorInfo(e.getError(), e.getTimSecs()));
					}
				}
				BaseStatsData status = (BaseStatsData) heartbeat.getStats();
				TaskStats tkStatus = status.getTaskStats();
				int uptimeSecs = heartbeat.getUptimeSecs();
				TaskSummary taskSummary = new TaskSummary(taskId, componentId,
						host, np.getPort(), uptimeSecs, newErrors);
				taskSummary.set_stats(tkStatus);
				taskSummarys.add(taskSummary);
			}
			topologyInfo = new TopologyInfo(topologyId,base.getStormName(), TimeUtils.time_delta(base.getLanchTimeSecs()), taskSummarys,extractStatusStr(base));
		}
		
		return topologyInfo;
	}

	/** 获得topology的信息配置信息
	 *  @param id
	 *         String: topology id
	 *  @return String
         */
	@Override
	public String getTopologyConf(String id) throws NotAliveException,
			TException {
		String rtn = StormUtils.to_json(readStormConf(conf, id));
		return rtn;
	}

	/** 获得topology的信息
	 *  bolt和spout的信息，并发数，分组，stream等信息
	 *  @param id
	 *         String: topology id
	 *  @return StormTopology
         */
	@Override
	public StormTopology getTopology(String id) throws NotAliveException,
			TException {
		StormTopology topology = null;
		try {
			StormTopology stormtopology = readStormTopology(conf, id);
			if (stormtopology == null) {
				throw new InvalidTopologyException("topology:" + id + "is null");
			}
			topology = Common.system_topology(readStormConf(conf, id),
					stormtopology);
		} catch (InvalidTopologyException e) {
			LOG.error(e + "system_topology failed");
		} catch (IOException e) {
			LOG.error(e + "system_topology failed");
		}
		return topology;
	}

	@Override
	public void shutdown() {
		LOG.info("Shutting down master");
		// Timer.cancelTimer(nimbus.getTimer());
		data.getScheduExec().shutdownNow();
		data.getStormClusterState().disconnect();
		LOG.info("Shut down master");

	}

	@Override
	public boolean waiting() {
		// FIXME 需要重新实现,目前假设用不到
		return false;
	}

	/**
	 * 检查topology是否存活
	 * 
	 * @param nimbus
	 * @param topologyName
	 * @param bActive
	 * @throws NotAliveException
	 * @throws AlreadyAliveException
	 */
	public void checkTopologyActive(NimbusData nimbus, String topologyName,
			boolean bActive) throws NotAliveException, AlreadyAliveException {
		if (isTopologyActive(nimbus.getStormClusterState(), topologyName) == !bActive) {
			if (bActive) {
				throw new NotAliveException(topologyName + " is not alive");
			} else {
				throw new AlreadyAliveException(topologyName
						+ " is already active");
			}
		}
	}

	/**
	 * whether the topology is active by topology name
	 * 
	 * @param stormClusterState
	 *            see Cluster_clj
	 * @param topologyName
	 * @return boolean if the storm is active, return true, otherwise return
	 *         false;
	 */
	public boolean isTopologyActive(StormClusterState stormClusterState,
			String topologyName) {
		boolean rtn = false;
		if (Common.get_storm_id(stormClusterState, topologyName) != null) {
			rtn = true;
		}
		return rtn;
	}

	/**
	 * 创建topology本地目录，上传jar，创建配置信息、及topology的序列化文件
	 * 
	 * @param conf
	 * @param stormId
	 * @param tmpJarLocation
	 * @param stormConf
	 * @param topology
	 * @throws IOException
	 */
	private void setupStormCode(Map<Object, Object> conf, String stormId,
			String tmpJarLocation, Map<Object, Object> stormConf,
			StormTopology topology) throws IOException {
		String stormroot = StormConfig.masterStormdistRoot(conf, stormId);
		FileUtils.forceMkdir(new File(stormroot));
		FileUtils.cleanDirectory(new File(stormroot));
		setupJar(conf, tmpJarLocation, stormroot);
		FileUtils.writeByteArrayToFile(
				new File(StormConfig.masterStormcodePath(stormroot)),
				Utils.serialize(topology));
		FileUtils.writeByteArrayToFile(
				new File(StormConfig.masterStormconfPath(stormroot)),
				Utils.serialize(stormConf));
	}

	/**
	 * Copy jar到stormroot目录下
	 * 
	 * @param conf
	 * @param tmpJarLocation
	 * @param stormroot
	 * @throws IOException
	 */
	private void setupJar(Map<Object, Object> conf, String tmpJarLocation,
			String stormroot) throws IOException {
		File srcFile = new File(tmpJarLocation);
		if (!srcFile.exists()) {
			throw new IllegalArgumentException(tmpJarLocation + " to copy to "
					+ stormroot + " does not exist!");
		}
		String path = StormConfig.masterStormjarPath(stormroot);
		File destFile = new File(path);
		FileUtils.copyFile(srcFile, destFile);
	}

	/**
	 * 为task分配对应componentid
	 * 
	 * @param conf
	 * @param stormId
	 * @param stormClusterState
	 * @throws IOException
	 * @throws InvalidTopologyException
	 */
	public void setupStormStatic(Map<Object, Object> conf, String stormId,
			StormClusterState stormClusterState) throws IOException,
			InvalidTopologyException {
		Map<Integer, String> taskToComponetId = mkTaskComponentAssignments(
				conf, stormId);
		if (taskToComponetId == null) {
			return;
		}
		Set<Entry<Integer, String>> entrySet = taskToComponetId.entrySet();
		for (Iterator<Entry<Integer, String>> it = entrySet.iterator(); it
				.hasNext();) {
			// key is taskid, value is taskinfo
			Entry<Integer, String> entry = it.next();
			TaskInfo taskinfo = new TaskInfo(entry.getValue());
			stormClusterState.set_task(stormId, entry.getKey(), taskinfo);
		}
	}

	/**
	 * generate a taskid(Integer) for every task
	 * 
	 * @param conf
	 * @param topologyid
	 * @return Map<Integer, String>: from taskid to componentid
	 * @throws IOException
	 * @throws InvalidTopologyException
	 */
	public Map<Integer, String> mkTaskComponentAssignments(
			Map<Object, Object> conf, String topologyid) throws IOException,
			InvalidTopologyException {
		Map<Object, Object> stormConf = readStormConf(conf, topologyid);
		StormTopology stopology = readStormTopology(conf, topologyid);
		StormTopology topology = null;
		Map<Integer, String> rtn = new HashMap<Integer, String>();

		if (stopology != null) {
			topology = Common.system_topology(stormConf, stopology);
			Integer count = 0;
			count = mkTaskMaker(stormConf, topology.get_bolts(), rtn, count);
			count = mkTaskMaker(stormConf, topology.get_spouts(), rtn, count);
			mkTaskMaker(stormConf, topology.get_state_spouts(), rtn, count);
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
	@SuppressWarnings("unchecked")
	public Map<Object, Object> readStormConf(Map<Object, Object> conf,
			String stormId) {
		String stormroot = StormConfig.masterStormdistRoot(conf, stormId);
		Map<Object, Object> stormconf = new HashMap<Object, Object>();
		try {
			byte[] bconf = FileUtils.readFileToByteArray(new File(StormConfig
					.masterStormconfPath(stormroot)));
			if (bconf != null) {
				stormconf = (Map<Object, Object>) Utils.deserialize(bconf);
			}
		} catch (IOException e) {
			LOG.error(e + "readStormConf exception");
		}
		Map<Object, Object> rtn = new HashMap<Object, Object>();
		rtn.putAll(conf);
		rtn.putAll(stormconf);
		return rtn;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Integer mkTaskMaker(Map<Object, Object> stormConf,
			Map<String, ?> cidSpec, Map<Integer, String> rtn, Integer cnt) {
		if (cidSpec == null) {
			return cnt;
		}
		Set<?> entrySet = cidSpec.entrySet();
		for (Iterator<?> it = entrySet.iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			Object obj = entry.getValue();
			ComponentCommon common = null;
			if (obj instanceof Bolt) {
				common = ((Bolt) obj).get_common();

			} else if (obj instanceof SpoutSpec) {
				common = ((SpoutSpec) obj).get_common();

			} else if (obj instanceof SpoutSpec) {
				common = ((StateSpoutSpec) obj).get_common();

			}
			int declared = Thrift.parallelismHint(common);
			// Map tmp = (Map) Utils_clj.from_json(common.get_json_conf());

			Map newStormConf = new HashMap(stormConf);
			// newStormConf.putAll(tmp);
			Integer maxParallelism = (Integer) newStormConf
					.get(Config.TOPOLOGY_MAX_TASK_PARALLELISM);
			Integer parallelism = declared;
			if (maxParallelism != null) {
				parallelism = Math.min(maxParallelism, declared);
			}
			for (int i = 0; i < parallelism; i++) {
				cnt++;
				rtn.put(cnt, (String) entry.getKey());
			}
		}
		return cnt;
	}

	/**
	 * 从本地序列化文件读取StormTopology信息
	 * 
	 * @param conf
	 * @param topologyId
	 * @return
	 * @throws IOException
	 */
	public StormTopology readStormTopology(Map<Object, Object> conf,
			String topologyId) throws IOException {
		String stormroot = StormConfig.masterStormdistRoot(conf, topologyId);
		StormTopology topology = null;
		byte[] bTopo = FileUtils.readFileToByteArray(new File(StormConfig
				.masterStormcodePath(stormroot)));
		if (bTopo != null) {
			topology = (StormTopology) Utils.deserialize(bTopo);
		}
		return topology;
	}

	public String extractStatusStr(StormBase stormBase) {
		StatusType t = stormBase.getStatus().getStatusType();
		return t.getStatus().toUpperCase();
	}

	/**
	 * start a topology: set active status of the topology
	 * 
	 * @param topologyName
	 * @param stormClusterState
	 * @param stormId
	 */
	public void startTopology(String topologyName,
			StormClusterState stormClusterState, String stormId) {
		LOG.info("Activating " + topologyName + ": " + stormId);
		StormStatus status = new StormStatus(StatusType.active);
		StormBase stormBase = new StormBase(topologyName,
				TimeUtils.current_time_secs(), status);
		stormClusterState.activate_storm(stormId, stormBase);
	}

}
