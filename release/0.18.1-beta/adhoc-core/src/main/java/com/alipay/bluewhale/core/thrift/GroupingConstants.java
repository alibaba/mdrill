package com.alipay.bluewhale.core.thrift;

import backtype.storm.generated.Grouping;
/**
 * groupingµƒ¿‡–Õ
 * @author yannian
 *
 */
public class GroupingConstants {
	public  static Grouping._Fields fields=Grouping._Fields.FIELDS;
	public  static Grouping._Fields shuffle=Grouping._Fields.SHUFFLE;
	public  static Grouping._Fields all=Grouping._Fields.ALL;
	public  static Grouping._Fields none=Grouping._Fields.NONE;
	public  static Grouping._Fields custom_serialized=Grouping._Fields.CUSTOM_SERIALIZED;
	public  static Grouping._Fields custom_object=Grouping._Fields.CUSTOM_OBJECT;
	public  static Grouping._Fields direct=Grouping._Fields.DIRECT;
}
