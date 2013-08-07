package com.alimama.mdrill.distinct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.zip.CRC32;

import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistinctCount implements Writable {
	public static Logger LOG = LoggerFactory.getLogger(DistinctCount.class);
	private HashSet<Integer> uniq = new HashSet<Integer>();
	private Integer maxUniqSize = 10000;
	private Integer currentTimes = 1;
	private Integer TimesStep = 2;

	@Override
	public void readFields(DataInput in) throws IOException {
		this.maxUniqSize = in.readInt();
		this.currentTimes = in.readInt();
		this.TimesStep = in.readInt();
		this.uniq.clear();
		int usize = in.readInt();
		for (int i = 0; i < usize; i++) {
			this.uniq.add(in.readInt());
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(this.maxUniqSize);
		out.writeInt(this.currentTimes);
		out.writeInt(this.TimesStep);
		out.writeInt(this.uniq.size());
		for (Integer uin : this.uniq) {
			out.writeInt(uin);
		}
	}

	public DistinctCount(byte[] zipdata) {
		if (zipdata.length <= 0) {
			return;
		}
		try {
			ByteArrayInputStream bis2 = new ByteArrayInputStream(zipdata);
			DataInputStream in2 = new DataInputStream(bis2);
			this.readFields(in2);
			in2.close();
			bis2.close();
		} catch (Exception e) {
		}
	}

	public byte[] toBytes() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bos);

			this.write(dout);

			byte[] data = bos.toByteArray();
			bos.close();
			return data;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return new byte[0];

	}

	public DistinctCount() {

	}

	public void set(String item) {
		CRC32 crc32 = new CRC32();
		crc32.update(new String(item).getBytes());
		long crcvalue = crc32.getValue();
		this.add((int) crcvalue);
	}

	public Long getValue() {
		return (long) this.uniq.size() * currentTimes;
	}

	private void add(Integer crc) {
		if (this.isallow(crc)) {
			boolean isadd = this.uniq.add(crc);
			if (isadd && this.uniq.size() > getMaxUniqSize()) {
				int newtimes = currentTimes * TimesStep;
				this.reFilter(newtimes);
			}
		}
	}

	private boolean isallow(long crc) {
		if (crc % this.currentTimes == 0) {
			return true;
		}
		return false;
	}

	private void reFilter(int times) {
		if (this.currentTimes == times) {
			return;
		}

		this.currentTimes = times;
		HashSet<Integer> data = new HashSet<Integer>();

		for (Integer ucrc : this.uniq) {
			if (this.isallow(ucrc)) {
				data.add(ucrc);
			}
		}
		this.uniq = data;
	}

	public Integer getMaxUniqSize() {
		return maxUniqSize;
	}

	public void setMaxUniqSize(Integer maxUniqSize) {
		this.maxUniqSize = maxUniqSize;
	}

	public void merge(DistinctCount dc) {
		int newtimes = Math.max(dc.currentTimes, this.currentTimes);
		this.reFilter(newtimes);
		dc.reFilter(newtimes);
		for (Integer ucrc : dc.uniq) {
			this.add(ucrc);
		}
	}

}
