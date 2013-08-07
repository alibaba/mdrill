package com.alipay.bluewhale.core.work.transfer;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.daemon.Shutdownable;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.messaging.IContext;
import com.alipay.bluewhale.core.messaging.MsgLoader;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.WorkCommon;

/**
 * worker的消息分发入口
 * 
 * @author yannian
 * 
 */
public class WorkerVirtualPort {

	private final static Logger LOG = Logger.getLogger(WorkerVirtualPort.class);

	@SuppressWarnings("rawtypes")
	public WorkerVirtualPort(Map conf, String supervisor_id, String storm_id,
			Integer port, IContext mq_context, Set<Integer> task_ids) {
		this.conf = conf;
		this.supervisorId = supervisor_id;
		this.port = port;
		this.mqContext = mq_context;
		this.taskIds = task_ids;
		this.stormId = storm_id;
	}

	@SuppressWarnings("rawtypes")
	private Map conf;
	private String supervisorId;
	private Integer port;
	private IContext mqContext;
	private Set<Integer> taskIds;
	private String stormId;

	public Shutdownable launch() throws InterruptedException {
		Shutdownable sd = null;
		if (!WorkCommon.localModeZmq(conf)) {
			sd = new DefaultTransferShutdown();
		}
		String msg = "Launching virtual port for supervisor";
		LOG.info(msg + ":" + supervisorId + " stormid:" + stormId + " port:" + port);
		
		try {
			boolean islocal = conf.get(Config.STORM_CLUSTER_MODE).equals("local");
			RunnableCallback killfn = StormUtils.getDefaultKillfn();
			int priority = Thread.NORM_PRIORITY;
			sd = MsgLoader.launchVirtualPort(islocal, mqContext, port, true,killfn, priority, taskIds);

		} catch (InterruptedException e) {
			LOG.error("WorkerVirtualPort->launch_virtual_port error", e);
			throw e;
		}
		return sd;
	}

}
