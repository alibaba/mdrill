package com.alipay.bluewhale.core.task.executer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.serialization.KryoTupleDeserializer;
import backtype.storm.spout.ISpoutOutputCollector;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.Time;
import backtype.storm.utils.TimeCacheMap;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.task.acker.Acker;
import com.alipay.bluewhale.core.task.transfer.TaskSendTargets;
import com.alipay.bluewhale.core.task.transfer.TupleInfo;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.utils.TimeUtils;
import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;

/**
 * spout处理器，不断的调用spout的nextTuple方法
 * 执行acker的处理结果，并调用spout的ack或fail方法
 * @author yannian
 *
 */
public class SpoutExecutors extends RunnableCallback {
    private static Logger LOG = Logger.getLogger(SpoutExecutors.class);

    private Map storm_conf;
    private IConnection puller;
    private AtomicBoolean zkActive;
    private TopologyContext user_context;
    private BaseTaskStatsRolling task_stats;
    private ConcurrentLinkedQueue<Runnable> event_queue;
    private backtype.storm.spout.ISpout spout;
    private TimeCacheMap pending;
    private ISpoutOutputCollector output_collector;
    private Integer max_spout_pending;
    private KryoTupleDeserializer deserializer;
    private String component_id;
    
    private AtomicBoolean isRecvRun;
    private Object lockrecv=new Object();

    private Exception error=null;

    public SpoutExecutors(backtype.storm.spout.ISpout _spout,
	    WorkerTransfer _transfer_fn, Map _storm_conf, IConnection _puller,
	    TaskSendTargets sendTargets, AtomicBoolean _storm_active_atom,
	    TopologyContext topology_context, TopologyContext _user_context,
	    BaseTaskStatsRolling _task_stats) {
	this.spout = _spout;
	this.storm_conf = _storm_conf;
	this.puller = _puller;
	this.zkActive = _storm_active_atom;
	this.user_context = _user_context;
	this.task_stats = _task_stats;
	Integer task_id = topology_context.getThisTaskId();

	this.component_id = topology_context.getThisComponentId();
	this.max_spout_pending = StormUtils.parseInt(storm_conf.get(Config.TOPOLOGY_MAX_SPOUT_PENDING));

	this.deserializer = new KryoTupleDeserializer(storm_conf,topology_context);// (KryoTupleDeserializer.
	// storm-conf
	this.event_queue = new ConcurrentLinkedQueue<Runnable>();

	int message_timeout_secs =StormUtils.parseInt(storm_conf.get(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS));

	// 创建本地处理tuple的队列，如果等待处理的tuple太长，则spout会暂停，TimeCacheMap，如果tuple处理超时，则调用tuple.fail，这样会通知acker重发此typle
	this.pending = new TimeCacheMap(message_timeout_secs,new SpoutTimeCallBack<Object, Object>(event_queue, spout,storm_conf,task_stats));

	// 创建collector,实际上是调用send_spout_msg
	this.output_collector = new SpoutCollector(task_id,spout,task_stats,sendTargets,storm_conf,_transfer_fn,pending,topology_context,event_queue);

	LOG.info("Opening spout " + component_id + ":" + task_id);
	this.spout.open(storm_conf, user_context, new SpoutOutputCollector(output_collector));
	LOG.info("Opend spout " + component_id + ":" + task_id);
	
	this.isRecvRun=new AtomicBoolean();
    }

    
    private boolean IsActive() {
	return zkActive.get();
    }

    private void executeEvent() {
	while (true) {
	    // 将event_querey里的调用acker的ack或fail信息发送过去
	    Runnable event = event_queue.poll();
	    if (event != null) {
		event.run();
	    } else {
		break;
	    }
	}
    }

    private void executeNextTupe() {
	// 如果队列长度，小于设定的缓冲区大小，那么执行spout.nextTuple();
	if (max_spout_pending == null || pending.size() < max_spout_pending) {

	    if (this.IsActive()) {

		try {

		    spout.nextTuple();
		} catch (RuntimeException e) {
		    error = e;
		    LOG.error("spout execute error ", e);
		} catch (Exception e) {
		    error = e;
		    LOG.error("spout execute error ", e);
		}
	    } else {

		try {
		    Time.sleep(100);
		} catch (InterruptedException e) {
		    LOG.error("spout sleep error ", e);
		}
	    }
	}
    }

    private void recv() {
	// 从本地zeroMq接收应答消息，分别向event_queue里添加ack或fail消息

	while (true) {

	    byte[] ser_msg = puller.recv();

	    if (ser_msg != null && ser_msg.length > 0) {
		Tuple tuple = deserializer.deserialize(ser_msg);
		Object id = tuple.getValue(0);
		Object olist = pending.remove(id);

		if (olist == null) {
		    continue;
		}

		List list = (List) olist;

		Object start_time_ms = list.get(2);
		Long time_delta = null;
		if (start_time_ms != null) {
		    time_delta = TimeUtils.time_delta_ms((Long) start_time_ms);
		}

		Object msgId = list.get(0);
		Object tupleInfo =  list.get(1);

		if (msgId == null||tupleInfo==null) {
		    continue;
		}

		String stream_id = tuple.getSourceStreamId();
		if (stream_id.equals(Acker.ACKER_ACK_STREAM_ID)) {
		    event_queue.add(new AckSpoutMsg(spout, storm_conf,
			    msgId, (TupleInfo)tupleInfo, time_delta, task_stats));
		} else if (stream_id.equals(Acker.ACKER_FAIL_STREAM_ID)) {
		    event_queue.add(new FailSpoutMsg(spout, storm_conf,
			    msgId, (TupleInfo)tupleInfo, time_delta, task_stats));
		}

	    }
	}
    }

    @Override
    public void run() {

	this.executeEvent();

	this.executeNextTupe();
	synchronized (lockrecv) {
	    if (!SpoutExecutors.this.isRecvRun.get()) {
		SpoutExecutors.this.isRecvRun.set(true);
		new Thread(new Runnable() {
		    @Override
		    public void run() {
			try {
			    SpoutExecutors.this.recv();
			} finally {
			    SpoutExecutors.this.isRecvRun.set(false);
			}
		    }
		}).start();
	    }
	}

    }

    @Override
    public Object getResult() {
	if (this.IsActive()) {
	    return 0;
	} else {
	    return -1;
	}
    }

    @Override
    public Exception error() {
	return error;
    }

}
