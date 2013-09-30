package com.alimama.mdrill.index.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;



public class PairWriteable  implements WritableComparable<PairWriteable>
{
	

	private Boolean isNum=true;

	private Text uniq=new Text();
	private Integer index=0;
	
	public boolean isNum() {
		return isNum;
	}

	public Text getUniq() {
		return uniq;
	}

	public int getIndex() {
		return index;
	}

	public PairWriteable(Text uniq) {
		this.isNum = false;
		this.uniq = uniq;
		this.index=0;
	}
	
	public PairWriteable( int index) {
		super();
		this.isNum = true;
		this.uniq=new Text();
		this.index = index;
	}
	
	public PairWriteable()
	{
	}
	
	
		
	@Override
    public void readFields(DataInput in) throws IOException {
		this.isNum=in.readBoolean();
		this.uniq=new Text();
		this.index=0;
		if(this.isNum)
		{
			this.index=in.readInt();
		}else{
			this.uniq.readFields(in);
		}
		
    }

	@Override
    public void write(DataOutput out) throws IOException {
		out.writeBoolean(this.isNum);
		if(this.isNum)
		{
			out.writeInt(this.index);
		}else{
			this.uniq.write(out);
		}
		
    }

	@Override
	public int compareTo(PairWriteable o) {
		int rtn=this.isNum.compareTo(o.isNum);
		if(rtn!=0)
		{
			return rtn;
		}
		
		if(this.isNum)
		{
			return this.index.compareTo(o.index);
		}else
		{
			return this.uniq.compareTo(o.uniq);

		}	
	}
}
