package com.alipay.bluewhale.core.task.acker;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.task.IBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.TimeCacheMap;

import com.alipay.bluewhale.core.utils.StormUtils;


/**
 * akcer 原理
 * 1.spout产生一条tuple后，除了会向相关bolt发送外，还会发供给acker,消息类型为init,同时本地还会在TimeCacheMap里面存储此tuple
 * 2.bolt在处理完相关tuple和子tuple后,会通过ack或fail给acker发送消息
 * 3.acker接收到后，会判断整个tuple链是否全部处理成功，或者整个链失败，然后发送消息给tuple对应的源spout
 * 4.spout清除TimeCacheMap的缓冲，spout的ack或fail方法会被调用，但这里并没有自动进行重发-需要业务在fail或ack里自己完成。
 * @author yannian
 *
 */
public class Acker implements IBolt{
    public static String ACKER_COMPONENT_ID = "__acker";
    public static String ACKER_INIT_STREAM_ID = "__ack_init";
    public static String ACKER_ACK_STREAM_ID = "__ack_ack";
    public static String ACKER_FAIL_STREAM_ID = "__ack_fail";
    private static Logger LOG = Logger.getLogger(Acker.class);

    private OutputCollector collector = null;
    private TimeCacheMap pending = null;

    // update_ack 对current_set的:val的值与value进行xor运算
    private static synchronized void update_ack(AckObject current_set,
	    Object value) {
	Long old = current_set.val;
	if (old == null) {
	    old = 0l;
	}
	Long newvalue = StormUtils.bit_xor(old, value);
	current_set.val = newvalue;
    }

    // acker_emit_direct 将values信息发送到目标task
//    public static void acker_emit_direct(OutputCollector collector,   Integer task, String stream, List values) {
//    }

 

    // prepare 创建TimeCacheMap对象，用来保留msg_id
    @Override
    public void prepare(Map stormConf, TopologyContext context,
	    OutputCollector collector) {
	this.collector = collector;
	String key=Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS;
	pending = new TimeCacheMap(StormUtils.parseInt(stormConf.get(key)));
    }

    // execute acker一共有三种消息，ACKER_INIT,ACKER-ACK,ACKER_FAIL,本别代表初始化，成功，失败
    // TimeCacheMap中的value存储的数据类型为{":val":"xor message_id",":spout-task":"soput的task_id",":failed":"处理失败"}
    // 当val=0或failed=true的时候，直接向对应的spout发送ack或fail消息，spout会判断确定是否重新发送此tuple
    @Override
    public void execute(Tuple input) {

	Object id = input.getValue(0);

	AckObject curr = (AckObject) pending.get(id);
	if (curr == null) {
	    curr = new AckObject();
	}
	String stream_id = input.getSourceStreamId();

	if (stream_id.equals(Acker.ACKER_INIT_STREAM_ID)) {
	    Acker.update_ack(curr, input.getValue(1));
	    curr.spout_task = input.getValue(2);

	}

	if (stream_id.equals(Acker.ACKER_ACK_STREAM_ID)) {

	    Acker.update_ack(curr, input.getValue(1));
	}

	if (stream_id.equals(Acker.ACKER_FAIL_STREAM_ID)) {

	    curr.failed = true;
	}

	pending.put(id, curr);

	Integer task = (Integer) curr.spout_task;
	
	if (task != null) {

	    Long val = (Long) curr.val;

	    if (new Long(0).equals(val)) {
		pending.remove(id);
		List values =StormUtils.mk_list(id);

		collector.emitDirect(task, Acker.ACKER_ACK_STREAM_ID, values);

	    }

	    boolean isFail = (Boolean) curr.failed;
	    if (isFail) {
		pending.remove(id);
		List values = StormUtils.mk_list(id);
		collector.emitDirect(task, Acker.ACKER_FAIL_STREAM_ID, values);
	    }
	}

	collector.ack(input);


    }

    @Override
    public void cleanup() {

    }

}
