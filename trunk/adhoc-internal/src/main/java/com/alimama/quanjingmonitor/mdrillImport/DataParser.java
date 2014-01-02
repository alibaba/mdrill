package com.alimama.quanjingmonitor.mdrillImport;

import com.alimama.quanjingmonitor.parser.InvalidEntryException;

public abstract class DataParser implements com.alimama.quanjingmonitor.parser.Parser{
	private static final long serialVersionUID = 1L;
	public Object parse(String raw)
	throws InvalidEntryException {
		DataIter rtn=this.parseLine(raw);
		if(rtn==null)
		{
			throw new InvalidEntryException();
		}
		return rtn;
	}

	
	public abstract String[] getGroupName();
	public abstract String[] getSumName();
	public abstract String getTableName();

	public abstract  DataIter parseLine(String line) throws InvalidEntryException;
	
	public static interface DataIter {
		public boolean next();
		public long getTs();
		public String[] getGroup();
		public double[] getSum();
	}

}
