package com.alipay.bluewhale.core.task.executer;

import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.spout.ISpout;

import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.stats.SpoutTaskStatsRolling;
import com.alipay.bluewhale.core.stats.Stats;
import com.alipay.bluewhale.core.task.transfer.TupleInfo;

/**
 * spout当成功发送一个tuple的时候会通过此方法执行spout的ack方法
 * @author yannian
 *
 */
public class AckSpoutMsg implements Runnable {
    private static Logger LOG = Logger.getLogger(AckSpoutMsg.class);

    private ISpout spout;
    private Map storm_conf;
    private Object msg_id;
    private TupleInfo tuple;
    private Long time_delta;
    private BaseTaskStatsRolling task_stats;

    public AckSpoutMsg(ISpout _spout, Map _storm_conf, Object _msg_id,
	    TupleInfo _tuple, Long _time_delta, BaseTaskStatsRolling _task_stats) {
	this.spout = _spout;
	this.storm_conf = _storm_conf;
	this.msg_id = _msg_id;
	this.tuple = _tuple;
	this.time_delta = _time_delta;
	this.task_stats = _task_stats;
    }

    public void run() {
	if (storm_conf.get(Config.TOPOLOGY_DEBUG).equals(Boolean.TRUE)) {
	    LOG.info("Acking message " + msg_id);
	}
	spout.ack(msg_id);
	if (time_delta != null) {
	    Stats.spout_acked_tuple((SpoutTaskStatsRolling) task_stats, tuple.getStream(), time_delta);
	}
    }

}
