package com.alipay.bluewhale.core.task.transfer;

import java.io.Serializable;
import java.util.List;

public class TupleInfo implements Serializable {

	private static final long serialVersionUID = -3348670497595864118L;
	private String stream;
	private List<Object> values;

	public TupleInfo(String stream, List<Object> values) {
		this.stream = stream;
		this.values = values;
	}

	public String getStream() {
		return stream;
	}

	public void setStream(String stream) {
		this.stream = stream;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	@Override
	public boolean equals(Object ti) {
		if (ti instanceof TupleInfo && ((TupleInfo) ti).stream.equals(stream)
				&& ((TupleInfo) ti).values.equals(values)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return values.hashCode() + stream.hashCode();
	}

	@Override
	public String toString() {
		return "TupleInfo [stream=" + stream + ", values=" + values + "]";
	}

}
