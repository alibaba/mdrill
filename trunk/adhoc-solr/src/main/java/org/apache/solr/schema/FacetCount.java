package org.apache.solr.schema;

/**
 * distinct统计用类。给namedlist用。
 * 
 * @author jian.qin 2012-7-24
 */
public class FacetCount {
	// public String key;
	public int count = 0;
	public boolean isDouble = false;

	public long lastDate = Long.MIN_VALUE;
	public long firstDate = Long.MAX_VALUE;
	public double sumDouble = 0;
	public double maxDouble = Double.NEGATIVE_INFINITY;
	public double minDouble = Double.POSITIVE_INFINITY;
	public double lastDouble = Double.NEGATIVE_INFINITY;;
	public double firstDouble = Double.POSITIVE_INFINITY;;

	public long sumLong = 0;
	public long maxLong = Long.MIN_VALUE;
	public long minLong = Long.MAX_VALUE;
	public long lastLong = Long.MIN_VALUE;
	public long firstLong = Long.MAX_VALUE;

	private double currentDouble = 0;
	private long currentLong = 0;
	private long currentDate = 0;
	
	public FacetCount(boolean isDouble) {
		this.isDouble = isDouble;
	}
	
	public void setSum(double v) {
		currentDouble = v;
		sumDouble += currentDouble;
		if (currentDouble > maxDouble) {
			maxDouble = currentDouble;
		} else if (currentDouble < minDouble) {
			minDouble = currentDouble;
		}
	}
	
	public void setSum(long v) {
		currentLong = v;
		sumLong += currentLong;
		if (currentLong > maxLong) {
			maxLong = currentLong;
		} else if (currentLong < minLong) {
			minLong = currentLong;
		}
	}

	public void setDate(long v) throws java.text.ParseException {
		currentDate = v;
		if (isDouble) {
			if (currentDate > lastDate) {
				lastDate = currentDate;
				lastDouble = currentDouble;
			} else if (currentDate < firstDate) {
				firstDate = currentDate;
				firstDouble = currentDouble;
			}
		} else {
			if (currentDate > lastDate) {
				lastDate = currentDate;
				lastLong = currentLong;
			} else if (currentDate < firstDate) {
				firstDate = currentDate;
				firstLong = currentLong;
			}
		}
	}
}