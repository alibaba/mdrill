package com.alipay.bluewhale.core.task.executer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import backtype.storm.task.IOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.MessageId;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.TimeCacheMap;

import com.alipay.bluewhale.core.task.acker.Acker;
import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.stats.BoltTaskStatsRolling;
import com.alipay.bluewhale.core.stats.Stats;
import com.alipay.bluewhale.core.task.common.TasksCommon;
import com.alipay.bluewhale.core.task.error.ITaskReportErr;
import com.alipay.bluewhale.core.task.transfer.UnanchoredSend;
import com.alipay.bluewhale.core.task.transfer.TaskSendTargets;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;

/**
 * bolt 所需要的IInternalOutputCollector接口的实现
 * 用户处理用户提交的emit,ack,fail操作
 * @author yannian
 *
 */
public class BoltCollector implements IOutputCollector {
    private static Logger LOG = Logger.getLogger(BoltCollector.class);

    private ITaskReportErr reportError;
    private TaskSendTargets sendTargets;
    private WorkerTransfer workerTransfer;
    private TopologyContext topologyContext;
    private Integer task_id;
    private TimeCacheMap<Tuple, Long> tuple_start_times;
    private BaseTaskStatsRolling task_stats;
    private TimeCacheMap<Tuple, Long> pending_acks;
    public BoltCollector(int message_timeout_secs,ITaskReportErr report_error, TaskSendTargets _send_fn, WorkerTransfer _transfer_fn, TopologyContext _topology_context,Integer task_id,TimeCacheMap<Tuple, Long> tuple_start_times,BaseTaskStatsRolling _task_stats
	    ) {
	this.reportError=report_error;
	this.sendTargets = _send_fn;
	this.workerTransfer = _transfer_fn;
	this.topologyContext = _topology_context;
	this.task_id=task_id;
	this.task_stats = _task_stats;
	
	
	//原先是 pending-acks (ConcurrentHashMap.)类型
	//	主要原因是 在bolt中，如果业务逻辑有bug，在某一条件下忘记调用ack或fail，就会导致这俩map越来越大，直到内存不够用
	//目前超时时间设置与spoutcollector设置一致 都是读的storm_conf.get(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS) 这个值
	this.pending_acks = new TimeCacheMap<Tuple, Long>(message_timeout_secs);
	this.tuple_start_times=tuple_start_times;
	
    }

    @Override
    public List<Integer> emit(String streamId, Collection<Tuple> anchors,List<Object> tuple) {
	return boltEmit(streamId, anchors, tuple, null);
    }

    @Override
    public void emitDirect(int taskId, String streamId,Collection<Tuple> anchors, List<Object> tuple) {
	boltEmit(streamId, anchors, tuple, taskId);
    }
    

    private List<Integer> boltEmit(String out_stream_id,Collection<Tuple> anchors, List<Object> values, Integer out_task_id) {
	try {
	    java.util.Set<Integer> out_tasks = null;
	    if (out_task_id != null) {
		out_tasks = sendTargets.get(out_task_id, out_stream_id, values);
	    } else {
		out_tasks = sendTargets.get(out_stream_id, values);
	    }

	    for (Integer t : out_tasks) {
		HashMap anchors_to_ids = new HashMap();
		if (anchors != null) {
		    for (Tuple a : anchors) {
			Long edge_id = MessageId.generateId();
			TasksCommon.put_xor(pending_acks, a, edge_id);
			for (Long root_id : a.getMessageId().getAnchorsToIds().keySet()) {
			    TasksCommon.put_xor(anchors_to_ids, root_id, edge_id);
			}
		    }
		}
		MessageId msgid=MessageId.makeId(anchors_to_ids);
		workerTransfer.transfer(t,new Tuple(topologyContext, values, task_id,out_stream_id, msgid));

	    }
	    return StormUtils.mk_list(out_tasks);
	} catch (Exception e) {
	    LOG.error("bolt emit", e);
	}
	return new ArrayList<Integer>();
    }


    @Override
    public void ack(Tuple input) {

	Object ack_val = pending_acks.remove(input);
	if (ack_val == null) {
	    ack_val = 0l;
	}

	for (Entry<Long, Long> e : input.getMessageId().getAnchorsToIds().entrySet()) {

	    UnanchoredSend.send(
		    topologyContext,
		    sendTargets,
		    workerTransfer,
		    Acker.ACKER_ACK_STREAM_ID,
		    StormUtils.mk_list((Object)e.getKey(),StormUtils.bit_xor(e.getValue(), ack_val)));
		}

        	Long delta = TasksCommon.tuple_time_delta(tuple_start_times, input);
        	if (delta != null) {
        	    Stats.bolt_acked_tuple((BoltTaskStatsRolling) task_stats,
        		    input.getSourceComponent(), input.getSourceStreamId(),
        		    delta);
        	}
    }

    @Override
    public void fail(Tuple input) {
	pending_acks.remove(input);
	for (Entry<Long, Long> e : input.getMessageId().getAnchorsToIds().entrySet()) {
	    UnanchoredSend.send(topologyContext, sendTargets,
		    workerTransfer, Acker.ACKER_FAIL_STREAM_ID,
		    StormUtils.mk_list((Object) e.getKey()));
	}

	    Long delta = TasksCommon.tuple_time_delta(tuple_start_times,input);
	    if (delta != null) {
		Stats.bolt_failed_tuple((BoltTaskStatsRolling) task_stats,
			input.getSourceComponent(), input.getSourceStreamId(),
			delta);
	    }
    }

    @Override
    public void reportError(Throwable error) {
	reportError.report(error);
    }

}
