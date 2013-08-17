package com.alipay.bluewhale.core.task.transfer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.task.TopologyContext;

import com.alipay.bluewhale.core.stats.BaseTaskStatsRolling;
import com.alipay.bluewhale.core.stats.Stats;
import com.alipay.bluewhale.core.task.group.GrouperType;
import com.alipay.bluewhale.core.task.group.MkGrouper;
import com.alipay.bluewhale.core.utils.EvenSampler;

/**
 * 创建tuple发送对象,计算tuple具体应该被发送到那些tasks,并更新统计
 * @author yannian
 *
 */
public class TaskSendTargets {
    private static Logger LOG = Logger.getLogger(TaskSendTargets.class);

    private Map stormConf;
    private String taskReadableName;
    private Map<String, Map<String, MkGrouper>> streamComponentgrouper;
    private TopologyContext topologyContext;
    private EvenSampler emitSampler;
    private Map<String, List<Integer>> componentTasks;
    private BaseTaskStatsRolling taskStats;

    public TaskSendTargets(Map _storm_conf, String _task_readable_name,
	    Map<String, Map<String, MkGrouper>> _stream_component_grouper,
	    TopologyContext _topology_context,
	     EvenSampler _emit_sampler,
	    Map<String, List<Integer>> _component_tasks,
	    BaseTaskStatsRolling _task_stats) {
	this.stormConf = _storm_conf;
	this.taskReadableName = _task_readable_name;
	this.streamComponentgrouper = _stream_component_grouper;
	this.topologyContext = _topology_context;
	this.emitSampler = _emit_sampler;
	this.componentTasks = _component_tasks;
	this.taskStats = _task_stats;

    }

    // direct send,直接发送到指定task_id
    public java.util.Set<Integer> get(Integer out_task_id, String stream, List<Object> tuple) {
	if (stormConf.get(Config.TOPOLOGY_DEBUG).equals(Boolean.TRUE)) {
	    LOG.info("Emitting direct: " + out_task_id + "; "    + taskReadableName + " " + stream );
	}

	String target_component = topologyContext.getComponentId(out_task_id);
	Map<String, MkGrouper> component_prouping = streamComponentgrouper.get(stream);
	MkGrouper grouping = component_prouping.get(target_component);
	if (grouping != null && !GrouperType.direct.equals(grouping.gettype())) {
	    throw new IllegalArgumentException("Cannot emitDirect to a task expecting a regular grouping");
	}

	// 更新统计
	if (emitSampler.getResult()) {
	    Stats.emitted_tuple(taskStats, stream);

	    if (out_task_id != null) {
		Stats.transferred_tuples(taskStats, stream, 1);
	    }
	}
	java.util.Set<Integer> out_tasks = new HashSet<Integer>();
	out_tasks.add(out_task_id);
	return out_tasks;
    }

    // 按照分组规则发送tuple
    public java.util.Set<Integer> get(String stream, List<Object> tuple) {
	if (stormConf.get(Config.TOPOLOGY_DEBUG).equals(Boolean.TRUE)) {
	    LOG.info("Emitting: " + taskReadableName + " " + stream );
	}

	// 获取流对应的grouper,然后根据grouper,计算应该发送到那些task上
	Map<String, MkGrouper> componentCrouping = streamComponentgrouper.get(stream);

	java.util.Set<Integer> out_tasks = new HashSet<Integer>();

	if(componentCrouping!=null)
	{
        	for (Entry<String, MkGrouper> ee : componentCrouping.entrySet()) {
        	    MkGrouper g = ee.getValue();
        	    if (GrouperType.direct.equals(g.gettype())) {
        		throw new IllegalArgumentException("Cannot do regular emit to direct stream");
        	    }
        
        	    List<Integer> tasks = componentTasks.get(ee.getKey());
        	    List<Integer> indices = g.grouper(tuple);
        	    for (Integer i : indices) {
        		Integer outtask=tasks.get(i);
        		if(outtask!=null)
        		{
        		    out_tasks.add(outtask);
        		}else{
        		    LOG.error("can`t found task at tasks by "+i);
        		}
        	    }
        	}
	}

	int num_out_tasks = out_tasks.size();

	// 更新统计信息
	if (emitSampler.getResult()) {
	    Stats.emitted_tuple(taskStats, stream);
	    Stats.transferred_tuples(taskStats, stream, num_out_tasks);
	}

	return out_tasks;
    }
}
