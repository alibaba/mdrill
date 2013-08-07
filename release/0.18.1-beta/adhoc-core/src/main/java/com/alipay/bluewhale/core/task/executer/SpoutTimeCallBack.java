package com.alipay.bluewhale.core.task.executer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import backtype.storm.utils.TimeCacheMap;

import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.task.transfer.TupleInfo;
import com.alipay.bluewhale.core.utils.TimeUtils;

/**
 * 用于处理spout中的tuple当acker应答超时后的处理
 * 
 * @author yannian
 *
 * @param <K>
 * @param <V>
 */
public class SpoutTimeCallBack<K, V> implements
	TimeCacheMap.ExpiredCallback<K, V> {
    private static Logger LOG = Logger.getLogger(SpoutTimeCallBack.class);

    private ConcurrentLinkedQueue<Runnable> event_queue;
    private backtype.storm.spout.ISpout spout;
    private Map storm_conf;
    private BaseTaskStatsRolling task_stats;

    public SpoutTimeCallBack(ConcurrentLinkedQueue<Runnable> _event_queue,
	    backtype.storm.spout.ISpout _spout, Map _storm_conf,BaseTaskStatsRolling stat) {
	this.event_queue = _event_queue;
	this.spout = _spout;
	this.storm_conf = _storm_conf;
	this.task_stats=stat;
    }

    @Override
    public void expire(K key, V val) {
	if (val == null) {
	    return;
	}
	try {
	    List list = (List) val;
	    Object msgId = list.get(0);
	    Object tuple =  list.get(1);
	    Long time_delta = 0l;

	    if (list.get(2) != null) {
		Object start_time_ms =  list.get(2);
		if (start_time_ms != null) {
		    time_delta = TimeUtils.time_delta_ms((Long)start_time_ms);
		}
	    }
	    if(tuple==null||msgId==null)
	    {
        	   return;
	    }
	    event_queue.add(new FailSpoutMsg(spout, storm_conf, msgId,
		    (TupleInfo)tuple, time_delta, task_stats));
	} catch (Exception e) {
	    LOG.error("expire error", e);
	}
    }
}
