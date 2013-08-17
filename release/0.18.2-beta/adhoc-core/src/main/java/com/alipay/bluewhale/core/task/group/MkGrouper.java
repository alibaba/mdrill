package com.alipay.bluewhale.core.task.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import backtype.storm.generated.Grouping;
import backtype.storm.generated.JavaObject;
import backtype.storm.grouping.CustomStreamGrouping;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

import com.alipay.bluewhale.core.thrift.GroupingConstants;
import com.alipay.bluewhale.core.thrift.Thrift;
import com.alipay.bluewhale.core.utils.RandomRange;
import com.alipay.bluewhale.core.utils.StormUtils;

/**
 * 根据grouper，返回一个tuples应该被发送到那个task的索引上
 * 
 * @author yannian
 * 
 */
public class MkGrouper {
	private Fields out_fields;
	private Grouping thrift_grouping;
	private int num_tasks;
	private Grouping._Fields fields;
	private RandomRange randomrange;
	private Random random;
	private GrouperType grouptype;
	private MkCustomGrouper custom_grouper;
	private MkFieldsGrouper fields_grouper;

	public MkGrouper(Fields _out_fields, Grouping _thrift_grouping,
			int _num_tasks) {
		this.out_fields = _out_fields;
		this.thrift_grouping = _thrift_grouping;
		this.num_tasks = _num_tasks;
		this.fields = Thrift.groupingType(thrift_grouping);
		this.grouptype = this.parseGroupType();
	}

	public GrouperType gettype() {
		return grouptype;
	}

	private GrouperType parseGroupType() {

		GrouperType grouperType = null;

		if (GroupingConstants.fields.equals(fields)) {
			// 全局group 就是向taskid=0的task里发送tuple
			if (Thrift.isGlobalGrouping(thrift_grouping)) {
				grouperType = GrouperType.global;
			} else {
				List<String> fields_group = Thrift.fieldGrouping(thrift_grouping);
				Fields fields = new Fields(fields_group);
				fields_grouper = new MkFieldsGrouper(out_fields, fields,num_tasks);
				// 按照fields的值的hashcode分组
				grouperType = GrouperType.fields;
			}
		} else if (GroupingConstants.all.equals(fields)) {
			// 每个task都发送
			grouperType = GrouperType.all;
		} else if (GroupingConstants.shuffle.equals(fields)) {
			// 随机分配，与none的实现方式不同的是，随机的比较均衡
			this.randomrange = new RandomRange(num_tasks);
			grouperType = GrouperType.shuffle;
		} else if (GroupingConstants.none.equals(fields)) {
			// 通过生成随机数的方式的完全随机
			this.random = new Random();
			grouperType = GrouperType.none;
		} else if (GroupingConstants.custom_object.equals(fields)) {
			// 用户自定义分组
			JavaObject jobj = thrift_grouping.get_custom_object();
			CustomStreamGrouping g = Thrift.instantiateJavaObject(jobj);
			custom_grouper = new MkCustomGrouper(g, out_fields, num_tasks);
			grouperType = GrouperType.custom_obj;
		} else if (GroupingConstants.custom_serialized.equals(fields)) {
			// 用户自定义的序列化对象的分组
			byte[] obj = thrift_grouping.get_custom_serialized();
			CustomStreamGrouping g = (CustomStreamGrouping) Utils
					.deserialize(obj);
			custom_grouper = new MkCustomGrouper(g, out_fields, num_tasks);
			grouperType = GrouperType.custom_serialized;
		} else if (GroupingConstants.direct.equals(fields)) {
			// 直接发送-spout或bolt里直接指定了目标
			grouperType = GrouperType.direct;
		}
		return grouperType;
	}

	// 返回当前tuple应该被发送到那些task中去
	public List<Integer> grouper(List<Object> values) {
		if (GrouperType.global.equals(gettype())) {
			return StormUtils.mk_list(new Integer(0));
		}

		if (GrouperType.fields.equals(gettype())) {
			// 按照fields的值的hashcode分组
			return fields_grouper.grouper(values);
		}

		// 每个task都发送
		if (GrouperType.all.equals(gettype())) {
			List<Integer> rtn = new ArrayList<Integer>();
			for (int i = 0; i < num_tasks; i++) {
				rtn.add(i);
			}
			return rtn;
		}

		// 随机分配，与none的实现方式不同的是，随机的比价均衡
		if (GrouperType.shuffle.equals(gettype())) {
			int rnd = randomrange.nextInt();
			return StormUtils.mk_list(new Integer(rnd));
		}

		// 通过生成随机数的方式的完全随机
		if (GrouperType.none.equals(gettype())) {
			int rnd = random.nextInt() % num_tasks;
			return StormUtils.mk_list(new Integer(rnd));
		}

		// 用户自定义分组
		if (GrouperType.custom_obj.equals(gettype())) {
			return custom_grouper.grouper(values);
		}

		// 用户自定义的序列化对象的分组
		if (GrouperType.custom_serialized.equals(gettype())) {
			return custom_grouper.grouper(values);
		}

		return new ArrayList<Integer>();
	}

}
