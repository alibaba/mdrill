package com.alipay.bluewhale.core.task.executer;

import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.spout.ISpout;

import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.stats.SpoutTaskStatsRolling;
import com.alipay.bluewhale.core.stats.Stats;
import com.alipay.bluewhale.core.task.transfer.TupleInfo;
/**
 * 当spout当发送一个tuple，在处理失败或超时的时候会通过此方法执行spout的fail方法
 * @author yannian
 *
 */
public class FailSpoutMsg implements Runnable {
    private static Logger LOG = Logger.getLogger(FailSpoutMsg.class);
    private ISpout spout;
    private Object msg_id;
    private TupleInfo tuple;
    private Long time_delta;
    private BaseTaskStatsRolling task_stats;

    public FailSpoutMsg(ISpout _spout, Map _storm_conf, Object _msg_id,
	    TupleInfo _tuple, Long _time_delta, BaseTaskStatsRolling _task_stats) {
	spout = _spout;
	msg_id = _msg_id;
	tuple = _tuple;
	time_delta = _time_delta;
	task_stats = _task_stats;
    }

    public void run() {
//	LOG.info("Failing message " + msg_id  );
	spout.fail(msg_id);
	if (time_delta != null) {
	    Stats.spout_failed_tuple((SpoutTaskStatsRolling) task_stats,tuple.getStream(), time_delta);
	}
    }

}