package com.alipay.bluewhale.core.task.transfer;

import java.util.List;

import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;

/**
 * 用于向acker发送init,ack,fail
 * @author yannian
 *
 */

public class UnanchoredSend {
    public static void send(TopologyContext topologyContext,
	    TaskSendTargets taskTargets, WorkerTransfer transfer_fn,
	    String stream, List<Object> values) {
	java.util.Set<Integer> tasks = taskTargets.get(stream, values);
	Integer taskId = topologyContext.getThisTaskId();
	Tuple tup = new Tuple(topologyContext, values, taskId, stream);
	for (Integer task : tasks) {
	    transfer_fn.transfer(task, tup);
	}
    }
}
