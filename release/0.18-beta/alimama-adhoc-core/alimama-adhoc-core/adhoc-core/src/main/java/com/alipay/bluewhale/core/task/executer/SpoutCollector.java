package com.alipay.bluewhale.core.task.executer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.task.acker.Acker;
import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.task.transfer.UnanchoredSend;
import com.alipay.bluewhale.core.task.transfer.TaskSendTargets;
import com.alipay.bluewhale.core.task.transfer.TupleInfo;
import com.alipay.bluewhale.core.utils.EvenSampler;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;

import backtype.storm.Config;
import backtype.storm.spout.ISpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.MessageId;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.TimeCacheMap;

/**
 * spout的emit方法最终是通过这里将tuple发送出去的
 * @author yannian
 *
 */
public  class SpoutCollector implements ISpoutOutputCollector {
    private static Logger LOG = Logger.getLogger(SpoutCollector.class);

    private TaskSendTargets sendTargets;
    private Map storm_conf;
    private WorkerTransfer transfer_fn;
    private TimeCacheMap pending;
    private TopologyContext topology_context;

    private EvenSampler sampler;
    private ConcurrentLinkedQueue<Runnable> event_queue;
    private BaseTaskStatsRolling task_stats;
    private backtype.storm.spout.ISpout spout;

    private Integer task_id;

    public SpoutCollector(Integer task_id, backtype.storm.spout.ISpout spout,
	    BaseTaskStatsRolling task_stats, TaskSendTargets sendTargets,
	    Map _storm_conf, WorkerTransfer _transfer_fn, TimeCacheMap pending,
	    TopologyContext topology_context,
	    ConcurrentLinkedQueue<Runnable> event_queue) {
	this.sendTargets = sendTargets;
	this.storm_conf = _storm_conf;
	this.transfer_fn = _transfer_fn;
	this.pending = pending;
	this.topology_context = topology_context;
	this.sampler = StormConfig.mk_stats_sampler(storm_conf);
	this.event_queue = event_queue;
	this.task_stats = task_stats;
	this.spout = spout;
	this.task_id = task_id;

    }

    @Override
    public List<Integer> emit(String streamId, List<Object> tuple,
	    Object messageId) {
	return sendSpoutMsg(streamId, tuple, messageId, null);
    }

    @Override
    public void emitDirect(int taskId, String streamId, List<Object> tuple,
	    Object messageId) {
	sendSpoutMsg(streamId, tuple, messageId, taskId);
    }

    private List<Integer> sendSpoutMsg(String out_stream_id,List<Object> values, Object message_id, Integer out_task_id) {
	

	// 调用worker提供的send_fn，发送tuple
	java.util.Set<Integer> out_tasks = null;
	if (out_task_id != null) {
	    out_tasks = sendTargets.get(out_task_id, out_stream_id, values);
	} else {
	    out_tasks = sendTargets.get(out_stream_id, values);
	}
	
	long root_id = MessageId.generateId();
	Integer askers=StormUtils.parseInt(storm_conf.get(Config.TOPOLOGY_ACKERS));
	Boolean isroot = (message_id != null)&&(askers > 0);

	for (Integer t : out_tasks) {
	    MessageId msgid;
	    if (isroot) {
		msgid = MessageId.makeRootId(root_id, t);
	    } else {
		msgid = MessageId.makeUnanchored();
	    }

	    Tuple tp = new Tuple(topology_context, values, task_id,out_stream_id, msgid);
	    transfer_fn.transfer(t, tp);

	}

	TupleInfo info = new TupleInfo(out_stream_id,values);

	if (isroot) {
	    Long ms = null;
	    if (sampler.getResult()) {
		ms = System.currentTimeMillis();
	    }

	    pending.put(root_id, StormUtils.mk_list(message_id, info, ms));
	    UnanchoredSend.send(
		    topology_context,
		    sendTargets,
		    transfer_fn,
		    Acker.ACKER_INIT_STREAM_ID,
		    StormUtils.mk_list((Object) root_id,StormUtils.bit_xor_vals(out_tasks), task_id));
	}else if (askers<=0) {
	// 如果不使用acker,则本地（event_queue）队列添加ack_spout_msg事件，最终事件调用
	    event_queue.add(new AckSpoutMsg(spout, storm_conf, message_id,info, null, task_stats));
	}

	return StormUtils.mk_list(out_tasks);
    }

}