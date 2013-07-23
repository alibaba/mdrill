package com.alipay.bluewhale.core.task.group;

import java.util.List;

import backtype.storm.grouping.CustomStreamGrouping;
import backtype.storm.tuple.Fields;

/**
 * 用户自定义的grouper处理
 * @author yannian
 *
 */
public  class MkCustomGrouper
{
	private CustomStreamGrouping grouping;
	private Fields out_fields;
	private int num_tasks;
	public MkCustomGrouper(CustomStreamGrouping _grouping,Fields _out_fields ,int _num_tasks)
	{
		this.grouping=_grouping;
		this.num_tasks=_num_tasks;
		this.out_fields=_out_fields;
		this.grouping.prepare(this.out_fields,this.num_tasks);
	}
	public List<Integer> grouper(List<Object> values)
	{
		return this.grouping.taskIndices(values);
	}
}
