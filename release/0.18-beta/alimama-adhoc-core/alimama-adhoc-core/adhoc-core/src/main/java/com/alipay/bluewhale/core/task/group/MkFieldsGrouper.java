package com.alipay.bluewhale.core.task.group;

import java.util.List;

import backtype.storm.tuple.Fields;

import com.alipay.bluewhale.core.utils.StormUtils;

//mk-fields-grouper
/**
 * 按照fileds的分组实现
 * 
 * @author yannian
 * 
 */
public class MkFieldsGrouper {
	private Fields out_fields;
	private Fields group_fields;
	private int num_tasks;

	public MkFieldsGrouper(Fields _out_fields, Fields _group_fields,
			int _num_tasks) {
		this.out_fields = _out_fields;
		this.group_fields = _group_fields;
		this.num_tasks = _num_tasks;
	}

	public List<Integer> grouper(List<Object> values) {
		int hashcode = this.out_fields.select(this.group_fields, values).hashCode();
		if (hashcode < 0) {
			hashcode *= -1;
		}
		int group = hashcode % this.num_tasks;
		return StormUtils.mk_list(group);
	}
}