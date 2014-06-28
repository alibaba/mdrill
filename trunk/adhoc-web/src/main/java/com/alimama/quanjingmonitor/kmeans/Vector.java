package com.alimama.quanjingmonitor.kmeans;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;



public class Vector implements Writable{
	LinkedHashMap<String,Vector_val> vector=new LinkedHashMap<String, Vector_val>();
	private long numPoints = 0;
	
	public Vector(){};

	protected void setNumPoints(long l) {
		this.numPoints = l;
	}
	long getNumPoints() {
		return numPoints;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.vector.clear();
		this.numPoints = in.readLong();

		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			String sk=in.readUTF();
			int type=in.readInt();
			if(type==1)
			{
				String_val val=new String_val();
				val.readFields(in);
				vector.put(sk, val);
			}
			if(type==2)
			{
				Number_val val=new Number_val();
				val.readFields(in);
				vector.put(sk, val);
			}
		}
	}
	  
	  @Override
	  public void write(DataOutput out) throws IOException {
		out.writeLong(numPoints);
	    out.writeInt(this.vector.size());
	    for(Entry<String,Vector_val> e:vector.entrySet())
	    {
	    	String s=e.getKey();
			Vector_val obj_val=e.getValue();
			out.writeUTF(s);
	    	if(obj_val instanceof String_val)
			{
	    		out.writeInt(1);
				String_val obj_val_str=(String_val) obj_val;
				obj_val_str.write(out);
			}
			
			if(obj_val instanceof Number_val)
			{
				out.writeInt(2);
				Number_val obj_val_num=(Number_val) obj_val;
				obj_val_num.write(out);
			
			}
	    }
	  }
	@Override
	public String toString() {
		return "numPoints:"+this.numPoints+","+vector.toString();
	}
	public static void main(String[] args) {
		Vector a=new Vector();
		Vector b=new Vector();
		Vector c=new Vector();

//		
//		for(int i=0;i<100;i++)
//		{
//			a.add("i_"+i, i, 1);
//
//			b.add("i_"+i, i, 1);
//		}
		
		for(int i=0;i<3;i++)
		{
			a.add("s_"+i, String.valueOf(i), 1);

			b.add("s_"+i, String.valueOf(i+1), 1);
			
			c.add("s_"+i, String.valueOf(i), 1);
		}
		
//		a.add("abc", 1110001, 1);
//		b.add("abc", 991, 1);
		
		
		a.add("abcd", "1", 100);
		a.add("abcd", "2", 100);
		a.add("abcd", "3", 100);
		a.add("abcd", "4", 100);

		b.add("abcd", "1", 100);
		b.add("abcd", "2", 100);
		b.add("abcd", "3", 100);
		b.add("abcd", "3", 100);
		
		c.add("abcd", "1", 100);
		c.add("abcd", "4", 100);
		c.add("abcd", "5", 100);
		c.add("abcd", "6", 100);
		
//		System.out.println(a.toString());
//		System.out.println(b.toString());
//		System.out.println(c.toString());

		System.out.println(a.distiance(b)*100);
		System.out.println(a.distiance(c)*100);

	}
	
	public void merger(Vector vector)
	{
		this.numPoints+=vector.numPoints;
		for(Entry<String, Vector_val> e:vector.vector.entrySet())
		{
			String s=e.getKey();
			Vector_val obj_val=e.getValue();
			Vector_val obj_val_cmp=this.vector.get(s);
			if(obj_val_cmp==null)
			{
				this.vector.put(s, obj_val.copy());
				continue;
			}


			if(obj_val instanceof String_val&&obj_val_cmp instanceof String_val)
			{

				String_val obj_val_str=(String_val) obj_val;
				String_val obj_val_cmp_str=(String_val) obj_val_cmp;
				obj_val_cmp_str.merger(obj_val_str);
		
			}
			
			if(obj_val instanceof Number_val&&obj_val_cmp instanceof Number_val)
			{
				Number_val obj_val_num=(Number_val) obj_val;
				Number_val obj_val_cmp_num=(Number_val) obj_val_cmp;
				obj_val_cmp_num.merger(obj_val_num);
			
			}
		}
	}
	public void add(String col,String val,double weight)
	{
		String_val obj_val=(String_val) vector.get(col);
		if(obj_val==null)
		{
			obj_val=new String_val();
			obj_val.setWeight(weight);
			vector.put(col, obj_val);
		}
		
		obj_val.add(val);
	}
	
	public void addImportand(String col,String val,double weight)
	{
		String_val obj_val=(String_val) vector.get(col);
		if(obj_val==null)
		{
			obj_val=new String_val();
			obj_val.setWeight(weight);
			vector.put(col, obj_val);
		}
		obj_val.setMustVeryLike(true);
		
		obj_val.add(val);
	}
	
	public void add(String col,double val,double weight)
	{
		Number_val obj_val=(Number_val) vector.get(col);
		if(obj_val==null)
		{
			obj_val=new Number_val();
			obj_val.setWeight(weight);
			vector.put(col, obj_val);
		}
		
		obj_val.add(val);
	}
	
	public void addImporatnt(String col,double val,double weight,double diff)
	{
		Number_val obj_val=(Number_val) vector.get(col);
		if(obj_val==null)
		{
			obj_val=new Number_val();
			obj_val.setWeight(weight);
			vector.put(col, obj_val);
		}
		
		obj_val.setMustVeryLike(true);
		obj_val.setMustveryLikeNumber(diff);
		
		obj_val.add(val);
	}
	
	public boolean Deny(Vector v)
	{
		double sumalla=0d;
		double sumallb=0d;
		
		double sumall_importanta=0d;
		double sumall_importantb=0d;
		for(Entry<String, Vector_val> e:this.vector.entrySet())
		{
			String s=e.getKey();

			Vector_val obj_val=e.getValue();
			Vector_val obj_val_cmp=v.vector.get(s);
			if(obj_val_cmp==null)
			{
				continue;
			}


			if(obj_val instanceof String_val&&obj_val_cmp instanceof String_val)
			{

				String_val obj_val_str=(String_val) obj_val;
				String_val obj_val_cmp_str=(String_val) obj_val_cmp;
				if(obj_val_str.Deny(obj_val_cmp_str))
				{
					return true;
				}
			}
			
			if(obj_val instanceof Number_val&&obj_val_cmp instanceof Number_val)
			{
				Number_val obj_val_num=(Number_val) obj_val;
				Number_val obj_val_cmp_num=(Number_val) obj_val_cmp;
				
				if(obj_val_num.isMustVeryLike()||obj_val_cmp_num.isMustVeryLike())
				{
					sumall_importanta+=obj_val_num.getVal();
					sumall_importantb+=obj_val_cmp_num.getVal();
				}else{
					sumalla+=obj_val_num.getVal();
					sumallb+=obj_val_cmp_num.getVal();
				}
				if(obj_val_num.Deny(obj_val_cmp_num))
				{
					return true;
				}
			}
		}
		return Math.abs(sumalla)<0.5||Math.abs(sumallb)<0.5||Math.abs(sumall_importanta)<0.01||Math.abs(sumall_importantb)<0.01;
	}
	
	public double distiance(Vector v)
	{
		double sum_distance=0;
		for(Entry<String, Vector_val> e:this.vector.entrySet())
		{
			String s=e.getKey();

			Vector_val obj_val=e.getValue();
			Vector_val obj_val_cmp=v.vector.get(s);
			if(obj_val_cmp==null)
			{
				continue;
			}


			if(obj_val instanceof String_val&&obj_val_cmp instanceof String_val)
			{

				String_val obj_val_str=(String_val) obj_val;
				String_val obj_val_cmp_str=(String_val) obj_val_cmp;
				double weight=obj_val_str.getWeight();
				double distance=(weight*Math.pow(obj_val_str.distiance(obj_val_cmp_str),2));
				sum_distance+=distance;
			}
			
			if(obj_val instanceof Number_val&&obj_val_cmp instanceof Number_val)
			{
				Number_val obj_val_num=(Number_val) obj_val;
				Number_val obj_val_cmp_num=(Number_val) obj_val_cmp;
				double weight=obj_val_num.getWeight();

				double distance=(weight*Math.pow(obj_val_num.distiance(obj_val_cmp_num), 2));
//				System.out.println("dd"+weight+","+distance);

				sum_distance+=distance;
			}
		}
		

		return Math.sqrt(sum_distance);
	}
	
	
	private static class Vector_val  implements Writable
	{
		private double weight=1;
		private boolean mustVeryLike=false;

		private double mustveryLikeNumber=0;
		
		public boolean isMustVeryLike() {
			return mustVeryLike;
		}

		public void setMustVeryLike(boolean mustVeryLike) {
			this.mustVeryLike = mustVeryLike;
		}

		public double getMustveryLikeNumber() {
			return mustveryLikeNumber;
		}

		public void setMustveryLikeNumber(double mustveryLikeNumber) {
			this.mustveryLikeNumber = mustveryLikeNumber;
		}
		public Vector_val copy()
		{
			Vector_val rtn=new Vector_val();
			rtn.weight=this.weight;
			rtn.mustVeryLike=this.mustVeryLike;
			rtn.mustveryLikeNumber=this.mustveryLikeNumber;
			 return rtn;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}
		
		@Override
		public void readFields(DataInput arg0) throws IOException {
			this.weight=arg0.readDouble();
			this.mustveryLikeNumber=arg0.readDouble();
			this.mustVeryLike=arg0.readBoolean();
			
		}
		@Override
		public void write(DataOutput arg0) throws IOException {
			arg0.writeDouble(this.weight);
			arg0.writeDouble(this.mustveryLikeNumber);
			arg0.writeBoolean(this.mustVeryLike);

			
		}

		@Override
		public String toString() {
			return "Vector_val [weight=" + weight + "]";
		}
		
	}
	
	private static class String_val extends Vector_val implements Writable{
		  private final static float LOADFACTOR = 0.75f;
		  private final static int MAX_SIZE = 64;
		Map<String,Integer> val=new LinkedHashMap<String,Integer>((int) Math.ceil(MAX_SIZE / LOADFACTOR) + 1, LOADFACTOR, true) {
			private static final long serialVersionUID = 1L;

			@Override
		      protected boolean removeEldestEntry(Map.Entry<String,Integer> eldest) {
		        return size() > MAX_SIZE;
		      }
		    };
		public int count=0;
		
		@Override
		public void readFields(DataInput arg0) throws IOException {
			super.readFields(arg0);

			this.val.clear();
			this.count=arg0.readInt();
			int size=arg0.readInt();
			for(int i=0;i<size;i++)
			{
				String kstr=arg0.readUTF();
				Integer cnt=arg0.readInt();
				this.val.put(kstr, cnt);
			}
			
		}
		@Override
		public void write(DataOutput arg0) throws IOException {
			super.write(arg0);

			arg0.writeInt(this.count);
			arg0.writeInt(this.val.size());
			for(Entry<String, Integer> e:this.val.entrySet())
			{
				arg0.writeUTF(e.getKey());
				arg0.writeInt(e.getValue());
			}
		}
		
		public String_val copy()
		{
			String_val rtn=new String_val();
			 rtn.merger(this);
			 return rtn;
		}
		
		public void merger(String_val v)
		{
			this.count+=v.count;
			this.setWeight(v.getWeight());
			this.setMustVeryLike(v.isMustVeryLike());
			this.setMustveryLikeNumber(v.getMustveryLikeNumber());
			for(Entry<String,Integer> e:v.val.entrySet())
			{
				this.addval(e.getKey(), e.getValue());
			}
		}
		
		private void addval(String strval,int num)
		{
			Integer oldval=this.val.get(strval);
			if(oldval==null)
			{
				oldval=0;
			}
			this.val.put(strval, oldval+num);
		}
		
		public void add(String strval)
		{
			this.addval(strval,1);
			this.count+=1;

		}
		
		/**
		 * http://www.ruanyifeng.com/blog/2013/03/cosine_similarity.html
		 * @param obj_val_cmp_str
		 * @return
		 */
		
		
		public String getTopField()
		{
			String f=null;
			int count=0;
			for(Entry<String, Integer> e:this.val.entrySet())
			{
				if(f==null||count<=e.getValue())
				{
					f=e.getKey();
				}
			}
			return String.valueOf(f);
		}
		
		public boolean Deny(String_val obj_val_cmp_str)
		{
			if(this.isMustVeryLike())
			{
				if(this.getTopField().equals(obj_val_cmp_str.getTopField()))
				{
					return false;
				}
				return true;
			}
			return false;
		}
		
		public double distiance(String_val obj_val_cmp_str)
		{
			if(this.count==0||obj_val_cmp_str.count==0)
			{
				return 1;
			}
			
			
			HashSet<String> allwords=new HashSet<String>();
			allwords.addAll(this.val.keySet());
			allwords.addAll(obj_val_cmp_str.val.keySet());
			
			double sumall=0;
			double powa=0;
			double powb=0;
			for(String words:allwords)
			{
				Integer tf=this.val.get(words);
				if(tf==null)
				{
					tf=0;
				}
				Integer tfCmp=obj_val_cmp_str.val.get(words);
				if(tfCmp==null)
				{
					tfCmp=0;
				}
				sumall+=(tf*tfCmp);
				powa+=Math.pow(tf, 2);
				powb+=Math.pow(tfCmp, 2);
			}
			
			if(powa==0||powb==0||sumall==0)
			{

				return 1;
			}
			
			double powab=Math.sqrt(powa)*Math.sqrt(powb);
			double rtn= Math.abs(sumall/powab);
			return 1-rtn;
		}

		public static class forsort{
			@Override
			public String toString() {
				return "" + key + ":" + cnt + "";
			}
			String key;
			int cnt;
		}
		@Override
		public String toString() {
			ArrayList<forsort> list=new ArrayList<Vector.String_val.forsort>();
			for(Entry<String, Integer> e:this.val.entrySet())
			{
				forsort s=new forsort();
				s.cnt=e.getValue();
				s.key=e.getKey();
				list.add(s);
			}
			Collections.sort(list,new Comparator<forsort>() {
				@Override
				public int compare(forsort o1, forsort o2) {
					long t1 = o1.cnt;
					long t2 = o2.cnt;
					return t1 == t2 ? 0 : t1 < t2 ? 1 : -1;
				}
			});
			
			ArrayList<forsort> print=new ArrayList<Vector.String_val.forsort>();
			int index=0;
			for(forsort s:list)
			{
				if(index++>5)
				{
					break;
				}
				print.add(s);
			}

			
			return "[val=" + print + ", count=" + count + "]";
		}
		
	}
	
	private static class Number_val extends Vector_val implements Writable
	{
		public double val=0;
		public double getVal() {
			return val;
		}
		public int count=0;
		
		public Number_val copy()
		{
			Number_val rtn=new Number_val();
			 rtn.merger(this);
			 return rtn;
		}
		public void merger(Number_val v)
		{
			this.setWeight(v.getWeight());
			this.setMustVeryLike(v.isMustVeryLike());
			this.setMustveryLikeNumber(v.getMustveryLikeNumber());
			this.val+=v.val;
			this.count+=v.count;
		}
		public void add(double v)
		{
			this.val+=v;
			this.count++;
		}
		
		public double avg()
		{
			if(count==0)
			{
				return 0;
			}
			return this.val/this.count;
		}
		
		public boolean Deny(Number_val obj_val_cmp_str)
		{
			if(this.isMustVeryLike())
			{
				double diff=Math.abs(this.avg()-obj_val_cmp_str.avg());
				if(diff<(this.getMustveryLikeNumber()*2))
				{
					return false;
				}
				return true;

			}
			return false;
		}
		
		public double distiance(Number_val obj_val_cmp_str)
		{
			//a.b / (|a|^2 + |b|^2 - a.b)
			
				
			double a=this.avg();
			double b=obj_val_cmp_str.avg();
			
			if(this.val<=0.001||this.val<=0.001)
			{
				return 1;
			}
			
//			if(a<0.00001&&b<0.00001)
//			{
//				return 0.75;
//			}
			
			double ab=a*b;
			double maxval=a*a+b*b-ab;
			if(maxval==0)
			{
				return 1;
			}
			double rtn=Math.abs(ab/maxval);
			return 1-rtn;
			
		}

		@Override
		public String toString() {
			return String.valueOf(this.avg());//"[val=" + val + ", count=" +this.count+" ,avg=" +  + "]";
		}
		@Override
		public void readFields(DataInput arg0) throws IOException {
			this.count=arg0.readInt();
			this.val=arg0.readDouble();
			
		}
		@Override
		public void write(DataOutput arg0) throws IOException {
			arg0.writeInt(this.count);
			arg0.writeDouble(this.val);
		}
	}

}
