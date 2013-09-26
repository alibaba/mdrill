package com.etao.adhoc.metric;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Metric {

	public static void main(String[] args) {
		System.out.println(parsePercent("2","Stage-1 map = 100.0%, reduce = 97.49997%",false));
	}
	
	private static Pattern  pat=null;
	public static String parsePercent(String stage,String percent,boolean issuccess)
	{
		if(issuccess)
		{
			return "100%";
		}
//		Stage-2 map = 100%,  reduce = 100%
		if(pat==null)
		{
			pat= Pattern.compile(".*Stage.*[^\\d]*(\\d+)[^\\d]map[^\\d]*(\\d+)[^\\d].*reduce[^\\d]*(\\d+)[^\\d].*");
		}
		
		if(!percent.startsWith("Stage"))
		{
			return "0%";
		}
		
		Integer totalstage=Integer.parseInt(stage);
		Integer currStage=0;
		double map=0;
		double reduce=0;
		
		Matcher mat = pat.matcher(percent);
        while (mat.find()) {
        	currStage=Integer.parseInt(mat.group(1));
        	map=Double.parseDouble(mat.group(2));
        	reduce=Double.parseDouble(mat.group(3));

        }
        
        if(currStage<1)
        {
        	currStage=1;
        }
        
        if(totalstage>0)
        {
        	double result= (100d*(currStage-1)+map*0.5d+reduce*0.5d)/totalstage;
        	return String.valueOf(result)+"%";
        }
		return "100%";
	}
	private long lineCnt;
	private long impression;
	private long finClick;
	private float finPrice;
	private long alipayDirectNum;
	private float alipayDirectAmt;
	private long alipayIndirectNum;
	private float alipayIndirectAmt;
	private long type;//0是hive，1是higo
	private String tablename;
	private String thedate; //yyyyMMdd

	@Override
	public String toString() {
		return "Metric [lineCnt=" + lineCnt + ", impression=" + impression
				+ ", finClick=" + finClick + ", finPrice=" + finPrice
				+ ", alipayDirectNum=" + alipayDirectNum + ", alipayDirectAmt="
				+ alipayDirectAmt + ", alipayIndirectNum=" + alipayIndirectNum
				+ ", alipayIndirectAmt=" + alipayIndirectAmt + ", type=" + type
				+ ", tablename=" + tablename + ", thedate=" + thedate + "]";
	}

	public long getLineCnt() {
		return lineCnt;
	}

	public void setLineCnt(long lineCnt) {
		this.lineCnt = lineCnt;
	}

	public long getImpression() {
		return impression;
	}

	public void setImpression(long impression) {
		this.impression = impression;
	}

	public long getFinClick() {
		return finClick;
	}

	public void setFinClick(long finClick) {
		this.finClick = finClick;
	}

	public float getFinPrice() {
		return finPrice;
	}

	public void setFinPrice(float finPrice) {
		this.finPrice = finPrice;
	}

	public long getAlipayDirectNum() {
		return alipayDirectNum;
	}

	public void setAlipayDirectNum(long alipayDirectNum) {
		this.alipayDirectNum = alipayDirectNum;
	}

	public float getAlipayDirectAmt() {
		return alipayDirectAmt;
	}

	public void setAlipayDirectAmt(float alipayDirectAmt) {
		this.alipayDirectAmt = alipayDirectAmt;
	}

	public long getAlipayIndirectNum() {
		return alipayIndirectNum;
	}

	public void setAlipayIndirectNum(long alipayIndirectNum) {
		this.alipayIndirectNum = alipayIndirectNum;
	}

	public float getAlipayIndirectAmt() {
		return alipayIndirectAmt;
	}

	public void setAlipayIndirectAmt(float alipayIndirectAmt) {
		this.alipayIndirectAmt = alipayIndirectAmt;
	}

	public long getType() {
		return type;
	}

	public void setType(long type) {
		this.type = type;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getThedate() {
		return thedate;
	}

	public void setThedate(String thedate) {
		this.thedate = thedate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(alipayDirectAmt);
		result = prime * result
				+ (int) (alipayDirectNum ^ (alipayDirectNum >>> 32));
		result = prime * result + Float.floatToIntBits(alipayIndirectAmt);
		result = prime * result
				+ (int) (alipayIndirectNum ^ (alipayIndirectNum >>> 32));
		result = prime * result + (int) (finClick ^ (finClick >>> 32));
		result = prime * result + Float.floatToIntBits(finPrice);
		result = prime * result + (int) (impression ^ (impression >>> 32));
		result = prime * result + (int) (lineCnt ^ (lineCnt >>> 32));
		result = prime * result
				+ ((tablename == null) ? 0 : tablename.hashCode());
		result = prime * result + ((thedate == null) ? 0 : thedate.hashCode());
		result = prime * result + (int) (type ^ (type >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metric other = (Metric) obj;
		if (Float.floatToIntBits(alipayDirectAmt) != Float
				.floatToIntBits(other.alipayDirectAmt))
			return false;
		if (alipayDirectNum != other.alipayDirectNum)
			return false;
		if (Float.floatToIntBits(alipayIndirectAmt) != Float
				.floatToIntBits(other.alipayIndirectAmt))
			return false;
		if (alipayIndirectNum != other.alipayIndirectNum)
			return false;
		if (finClick != other.finClick)
			return false;
		if (Float.floatToIntBits(finPrice) != Float
				.floatToIntBits(other.finPrice))
			return false;
		if (impression != other.impression)
			return false;
		if (lineCnt != other.lineCnt)
			return false;
		if (tablename == null) {
			if (other.tablename != null)
				return false;
		} else if (!tablename.equals(other.tablename))
			return false;
		if (thedate == null) {
			if (other.thedate != null)
				return false;
		} else if (!thedate.equals(other.thedate))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	

}
