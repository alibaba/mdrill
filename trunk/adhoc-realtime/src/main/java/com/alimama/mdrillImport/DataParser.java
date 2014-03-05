package com.alimama.mdrillImport;

import java.util.Map;

import backtype.storm.task.TopologyContext;


public abstract class DataParser implements Parser{
	private boolean isSpout;
	private Map conf;
	private TopologyContext context;
	public void init(boolean isSpout,Map conf, TopologyContext context){
		this.isSpout=isSpout;
		this.conf=conf;
		this.context=context;
	}
	private static final long serialVersionUID = 1L;
	public Object parse(Object raw)
	throws InvalidEntryException {
		return this.parseObject(raw);
	}

	
	public abstract String[] getGroupName();
	public abstract String[] getSumName();
	public abstract String getTableName();
	public DataIter parseObject(Object line) throws InvalidEntryException {
		return this.parseLine(line==null?null:(String)line);
	}
	public   DataIter parseLine(String line) throws InvalidEntryException{
		return null;
	}
	
	public static interface DataIter {
		public boolean next();
		public long getTs();
		public Object[] getGroup();
		public Number[] getSum();
	}
	
	public boolean isSpout() {
		return isSpout;
	}




	public Map getConf() {
		return conf;
	}


	


	public TopologyContext getContext() {
		return context;
	}



}
