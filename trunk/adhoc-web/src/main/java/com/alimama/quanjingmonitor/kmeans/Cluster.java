package com.alimama.quanjingmonitor.kmeans;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;




public class Cluster implements Writable {
	private int id;

	boolean converged=false;
	
	private int numselect=0;

	public int getNumselect() {
		return numselect;
	}

	public void setNumselect(int numselect) {
		this.numselect = numselect;
	}

	public boolean isConverged() {
		return converged;
	}

	public void setConverged(boolean converged) {
		this.converged = converged;
	}

	private Vector center = new Vector();

	public Cluster() {
	}

	public Cluster(Cluster cl) {
		this.center.merger(cl.center);
		this.id = cl.id;
		this.converged = cl.converged;
		this.numselect = cl.numselect;
	}
	public Cluster(Vector center, int clusterId) {
		this.center.merger(center);
		this.id = clusterId;
	}

	public int getId() {
		return this.id;
	}

	public Vector getCenter() {
		return center;
	}

	protected void setId(int id) {
		this.id = id;
	}

	

	protected void setCenter(Vector center) {
		this.center = center;
	}

	
	@Override
	public void readFields(DataInput in) throws IOException {
		this.id = in.readInt();
		this.numselect=in.readInt();
//		System.out.println(this.numselect+"=======");
		this.center.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(this.id);
		out.writeInt(this.numselect);
		this.center.write(out);
	}

	public String asFormatString() {
		StringBuffer buf = new StringBuffer();
		buf.append("id=");
		buf.append(this.getId()+","+this.numselect);
		buf.append(" ");

		buf.append(center.toString());
		return buf.toString();
	}
	
	public String toString() {
		return this.asFormatString();
	}

		
}
