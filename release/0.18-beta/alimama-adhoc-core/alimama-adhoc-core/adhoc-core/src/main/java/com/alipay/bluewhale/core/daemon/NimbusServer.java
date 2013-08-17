package com.alipay.bluewhale.core.daemon;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.server.THsHaServer;
import org.apache.thrift7.transport.TNonblockingServerSocket;

import backtype.storm.Config;
import backtype.storm.generated.Nimbus;
import backtype.storm.generated.Nimbus.Iface;
import backtype.storm.utils.BufferFileInputStream;
import backtype.storm.utils.TimeCacheMap;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.schedule.CleanRunnable;
import com.alipay.bluewhale.core.schedule.MonitorRunnable;
import com.alipay.bluewhale.core.utils.PathUtils;

/**
 * NimbusServer主要做以下工作：
 * (1) 清理中断了的topology
 *     删除在/storm-local-dir/stormdist下存在，zk中storm-zk-root/topologyid不存在的topology相关信息
 * (2) 将zk上storm-zk-root/storms下所有的topology的状态设置为启动状态。状态表见类StatusTransition
 * (3) 启动一个线程，每间隔nimbus.monitor.reeq.secs时间将zk上storm-zk-root/storms下所有的topology的状态转换为monitor状态
 *     转换成monitor状态的时候，会从新计算每隔topology的任务分配情况，监控是否与上一次的分配情况不同，如果存在不同，则替换
 * (4) 启动一个线程，间隔nimbus.cleanup.inbox.freq.secs时间清理一次过期的jar包
 *
 */
public class NimbusServer {

	private static Logger LOG = Logger.getLogger(NimbusServer.class);

	/**
	 * Nimbus Server 主类
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// 读取配置文件
		@SuppressWarnings("rawtypes")
		Map config = Utils.readStormConfig();
		// launch-server
		launch_server(config);

	}

	@SuppressWarnings("rawtypes")
	private static void launch_server(Map conf) throws Exception {
		// 1、验证是否分布式模式
		NimbusUtils.validate_distributed_mode(conf);

		NimbusData data=service_handler(conf);

		
		// 设置定时操作线程
		final ScheduledExecutorService scheduExec= data.getScheduExec();

		//Schedule Nimbus monitor
		MonitorRunnable r1 = new MonitorRunnable(data);
		
		int monitor_freq_secs = (Integer) conf.get(Config.NIMBUS_MONITOR_FREQ_SECS);
		scheduExec.scheduleAtFixedRate(r1, 0, monitor_freq_secs,TimeUnit.SECONDS);
		
		//Schedule Nimbus inbox cleaner.清理/nimbus/inbox下过期的jar
		String dir_location=StormConfig.masterInbox(conf);
		int inbox_jar_expiration_secs=(Integer)conf.get(Config.NIMBUS_INBOX_JAR_EXPIRATION_SECS);
		CleanRunnable r2 = new CleanRunnable(dir_location,inbox_jar_expiration_secs);
		int cleanup_inbox_freq_secs = (Integer) conf.get(Config.NIMBUS_CLEANUP_INBOX_FREQ_SECS);
		scheduExec.scheduleAtFixedRate(r2, 0, cleanup_inbox_freq_secs,TimeUnit.SECONDS);

		//Thrift server配置及启动操作
		Integer thrift_port = (Integer) conf.get(Config.NIMBUS_THRIFT_PORT);
		TNonblockingServerSocket socket = new TNonblockingServerSocket(
				thrift_port);
		THsHaServer.Args args = new THsHaServer.Args(socket);
		args.workerThreads(64);
		args.protocolFactory(new TBinaryProtocol.Factory());
		final ServiceHandler service_handler = new ServiceHandler(data);
		args.processor(new Nimbus.Processor<Iface>(service_handler));
		final THsHaServer server = new THsHaServer(args);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				service_handler.shutdown();
				scheduExec.shutdown();
				server.stop();
			}

		});
		LOG.info("Starting BlueWhale server...");
		server.serve();
	}
	
	//for test
	@SuppressWarnings("rawtypes")
	public static NimbusData s_hander(Map conf) throws Exception{
	    return service_handler(conf);
	}
	
	@SuppressWarnings("rawtypes")
	private static NimbusData service_handler(Map conf) throws Exception {
		LOG.info("Starting BlueWhale with conf " + conf);

		TimeCacheMap.ExpiredCallback<Object, Object> expiredCallback = new TimeCacheMap.ExpiredCallback<Object, Object>() {
			@Override
			public void expire(Object key, Object val) {
				try {
					if (val!=null) {
						if(val instanceof Channel){
							Channel channel = (Channel) val;
							channel.close();		
						}else if(val instanceof BufferFileInputStream){
							BufferFileInputStream is=(BufferFileInputStream)val;
							is.close();
						}
					}
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}	

			}
		};
		int file_copy_expiration_secs = (Integer) conf
				.get(Config.NIMBUS_FILE_COPY_EXPIRATION_SECS);
		TimeCacheMap<Object, Object> uploaders = new TimeCacheMap<Object, Object>(
				file_copy_expiration_secs, expiredCallback);
		TimeCacheMap<Object, Object> downloaders = new TimeCacheMap<Object, Object>(
				file_copy_expiration_secs, expiredCallback);

		// Callback callback=new TimerCallBack();
		// StormTimer timer=Timer.mkTimerTimer(callback);
		NimbusData data = new NimbusData(conf, downloaders, uploaders);

		// 清理中断的topology
		cleanup_corrupt_topologies(data);

		// 获取zk里面存活的topology id列表
		List<String> active_ids = data.getStormClusterState().active_storms();

		if (active_ids != null){
		    
		    for (String topologyid : active_ids) {
			//切换为 :startup 状态
			    StatusTransition.transition(data, topologyid, false, StatusType.startup);
		    }
		    
		}

		return data;

	}


	/**
	 * 清理仍然有状态在zookeeper上面，但是在nimbus本地目录下不存在的topology
	 * 
	 * @param data
	 *            NimbusData
	 */
	private static void cleanup_corrupt_topologies(NimbusData data) {
		// 获取StormClusterState
		StormClusterState stormClusterState = data.getStormClusterState();
		// 获取nimbus下数据存储目录/nimbus/stormdist路径
		String master_stormdist_root = StormConfig.masterStormdistRoot(data
				.getConf());
		// 获取/nimbus/stormdist路径下面文件名称集合(topology id集合)
		List<String> code_ids = PathUtils
				.read_dir_contents(master_stormdist_root);
		// 获取当前ZK上面仍然存有状态的topology id集合
		List<String> active_ids = data.getStormClusterState().active_storms();
		if (active_ids != null && active_ids.size() > 0) {
			if (code_ids != null) {
				// 获取不在本地目录下，但是仍然存在zk里面的topology id集合
				active_ids.removeAll(code_ids);
			}
			for (String corrupt : active_ids) {
				LOG.info("Corrupt topology "
						+ corrupt
						+ " has state on zookeeper but doesn't have a local dir on Nimbus. Cleaning up...");
				// 执行清理ZK下面topology id
				stormClusterState.remove_storm(corrupt);
			}
		}

	}
	

}
