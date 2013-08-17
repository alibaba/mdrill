package com.alipay.bluewhale.core.task.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.generated.Bolt;
import backtype.storm.generated.ComponentObject;
import backtype.storm.generated.Grouping;
import backtype.storm.generated.JavaObject;
import backtype.storm.generated.ShellComponent;
import backtype.storm.generated.SpoutSpec;
import backtype.storm.generated.StateSpoutSpec;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.ISpout;
import backtype.storm.spout.ShellSpout;
import backtype.storm.task.IBolt;
import backtype.storm.task.ShellBolt;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.TimeCacheMap;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.callback.RunnableCallback;
import com.alipay.bluewhale.core.cluster.StormConfig;
import com.alipay.bluewhale.core.messaging.IConnection;
import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.stats.Stats;
import com.alipay.bluewhale.core.task.error.ITaskReportErr;
import com.alipay.bluewhale.core.task.executer.BoltExecutors;
import com.alipay.bluewhale.core.task.executer.SpoutExecutors;
import com.alipay.bluewhale.core.task.group.MkGrouper;
import com.alipay.bluewhale.core.task.transfer.TaskSendTargets;
import com.alipay.bluewhale.core.thrift.Thrift;
import com.alipay.bluewhale.core.utils.StormUtils;
import com.alipay.bluewhale.core.utils.TimeUtils;
import com.alipay.bluewhale.core.work.transfer.WorkerTransfer;

/**
 * task的公用方法
 * 
 * @author yannian
 * 
 */
public class TasksCommon {
	private final static Logger LOG = Logger.getLogger(TasksCommon.class);

	public static BaseTaskStatsRolling mk_task_stats(Object obj, int rate) {
		BaseTaskStatsRolling stat=null;
		if (obj instanceof IBolt) {
			stat= Stats.mk_bolt_stats(rate);
		}else{
			stat=Stats.mk_spout_stats(rate);
		}
		return stat;
	}

	public static Long tuple_time_delta(TimeCacheMap start_times, Tuple tuple) {
		Object start_time = start_times.remove(tuple);
		if (start_time != null) {
			return TimeUtils.time_delta_ms((Long) start_time);
		}
		return null;
	}

	public static Long tuple_time_delta(Map start_times, Tuple tuple) {
		Object start_time = start_times.remove(tuple);
		if (start_time != null) {
			return TimeUtils.time_delta_ms((Long) start_time);
		}
		return null;
	}

	public static void put_xor(TimeCacheMap pending, Object key, Object id) {
		//synchronized (pending) {
			Object curr = new Integer(0);
			if (pending.containsKey(key)) {
				curr = pending.get(key);
			}
			pending.put(key, StormUtils.bit_xor(curr, id));
		//}
	}

	public static void put_xor(Map pending, Object key, Object id) {
		//synchronized (pending) {
			Object curr = new Integer(0);
			if (pending.containsKey(key)) {
				curr = pending.get(key);
			}
			pending.put(key, StormUtils.bit_xor(curr, id));
		//}
	}

	/**
	 * 获取指定component_id上对应的task-object
	 * @param topology
	 * @param component_id
	 * @return
	 */
	public static Object get_task_object(StormTopology topology,
			String component_id) {
		Map<String, SpoutSpec> spouts = topology.get_spouts();
		Map<String, Bolt> bolts = topology.get_bolts();
		Map<String, StateSpoutSpec> state_spouts = topology.get_state_spouts();

		ComponentObject obj = null;
		if (spouts.containsKey(component_id)) {
			obj = spouts.get(component_id).get_spout_object();
		}else if (bolts.containsKey(component_id)) {
			obj = bolts.get(component_id).get_bolt_object();
		}else if (state_spouts.containsKey(component_id)) {
			obj = state_spouts.get(component_id).get_state_spout_object();
		}

		if (obj == null) {
			LOG.error("get_task_object->obj == null");
			throw new RuntimeException("Could not find " + component_id
					+ " in " + topology.toString());
		}

		Object componentObject = getSetComponentObject(obj);

		Object rtn=null;

		if (componentObject instanceof JavaObject) {
			rtn=Thrift.instantiateJavaObject((JavaObject) componentObject);
		}else if (componentObject instanceof ShellComponent) {
			if (spouts.containsKey(component_id)) {
				rtn=new ShellSpout((ShellComponent) componentObject);
			} else {
				rtn= new ShellBolt((ShellComponent) componentObject);
			}
		}else{
			rtn=componentObject;
		}
		return rtn;

	}
	
	/**
	 * 获取 Task Object
	 * @param obj
	 * @return
	 */
	private static Object getSetComponentObject(ComponentObject obj) {
		if (obj.getSetField() == ComponentObject._Fields.SERIALIZED_JAVA) {
			return Utils.deserialize(obj.get_serialized_java());
		} else if (obj.getSetField() == ComponentObject._Fields.JAVA_OBJECT) {
			return obj.get_java_object();
		} else {
			return obj.get_shell();
		}
	}

	// 获取当前task的每个stream应该流向那些commponID,以及他们是如何分组的
	public static Map<String, Map<String, MkGrouper>> outbound_components(
			TopologyContext topology_context) {
		Map<String, Map<String, MkGrouper>> rr = new HashMap<String, Map<String, MkGrouper>>();

		// <Stream_id,<component,Grouping>>
		Map<String, Map<String, Grouping>> output_groupings = topology_context
				.getThisTargets();

		for (Entry<String, Map<String, Grouping>> entry : output_groupings
				.entrySet()) {

			Map<String, Grouping> component_grouping = entry.getValue();
			String stream_id = entry.getKey();
			Fields out_fields = topology_context.getThisOutputFields(stream_id);

			Map<String, MkGrouper> componentGrouper = new HashMap<String, MkGrouper>();
			for (Entry<String, Grouping> cg : component_grouping.entrySet()) {
				String component = cg.getKey();
				Grouping tgrouping = cg.getValue();
				int num_tasks = topology_context.getComponentTasks(component)
						.size();
				if (num_tasks > 0) {
					MkGrouper grouper = new MkGrouper(out_fields, tgrouping,
							num_tasks);
					componentGrouper.put(component, grouper);
				}
			}
			if (componentGrouper.size() > 0) {
				rr.put(stream_id, componentGrouper);
			}
		}
		return rr;
	}

	// get-readable-name
	public static String get_readable_name(TopologyContext topology_context) {
		return topology_context.getThisComponentId();
	}

	/**
	 * 获取component对应的配置信息
	 * @param storm_conf
	 * @param topology_context
	 * @param component_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map component_conf(Map storm_conf,
			TopologyContext topology_context, String component_id) {
		List<Object> to_remove = StormConfig.All_CONFIGS();
		to_remove.remove(Config.TOPOLOGY_DEBUG);
		to_remove.remove(Config.TOPOLOGY_MAX_SPOUT_PENDING);
		to_remove.remove(Config.TOPOLOGY_MAX_TASK_PARALLELISM);
		to_remove.remove(Config.TOPOLOGY_TRANSACTIONAL_ID);

		Map<Object,Object> spec_conf = new HashMap<Object,Object>();

		String jconf = topology_context.getComponentCommon(component_id)
				.get_json_conf();
		if (jconf != null) {
			spec_conf = (Map<Object,Object>) StormUtils.from_json(jconf);
		}
		for (Object p : to_remove) {
			spec_conf.remove(p);
		}

		spec_conf.putAll(storm_conf);

		return spec_conf;
	}

	public static RunnableCallback mk_executors(Object _task_obj,
			WorkerTransfer transfer_fn, Map _storm_conf, IConnection _puller,
			TaskSendTargets sendTargets, AtomicBoolean _storm_active_atom,
			TopologyContext _topology_context, TopologyContext _user_context,
			BaseTaskStatsRolling _task_stats, ITaskReportErr _report_error) {
		if (_task_obj instanceof IBolt) {
			return new BoltExecutors((IBolt) _task_obj, transfer_fn,
					_storm_conf, _puller, sendTargets, _storm_active_atom,
					_topology_context, _user_context, _task_stats,
					_report_error);
		}

		if (_task_obj instanceof ISpout) {
			return new SpoutExecutors((ISpout) _task_obj, transfer_fn,
					_storm_conf, _puller, sendTargets, _storm_active_atom,
					_topology_context, _user_context, _task_stats);
		}

		return null;
	}
	


}
