/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alimama.quanjingmonitor.kmeans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptID;

public class KMeansClusterReduce extends Reducer<Text, Text, Text, Text> {

	  private final Collection<Cluster> clusters = new ArrayList<Cluster>();

	  private Map<String, Cluster> clusterMap;
	  ParseVector parse=new ParseVector();

	  private void setClusterMap(Collection<Cluster> clusters) {
		    clusterMap = new HashMap<String, Cluster>();
		    for (Cluster cluster : clusters) {
		      clusterMap.put(String.valueOf(cluster.getId()), cluster);
		    }
		    clusters.clear();
		  }
	  int rep=2;
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			this.clusters.clear();

			Configuration conf = context.getConfiguration();
			parse.setup(conf);
			this.rep=conf.getInt(KMeansDriver.CLUSTER_CONVERGENCE_ABTEST_REP, 2);
			try {

				String clusterPath = conf.get(KMeansDriver.CLUSTER_PATH_KEY);
				if (clusterPath != null && clusterPath.length() > 0) {
					KmeansPublic.configureWithClusterInfo(conf, new Path(clusterPath), clusters);
					if (clusters.isEmpty()) {
						throw new IllegalStateException(
								"No clusters found. Check your -c path.");
					}
					this.setClusterMap(clusters);
				}
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		}
		
		Comparator<String> cmp=new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String[] cols1=o1.toString().split("@abtest@");
				String[] cols2=o2.toString().split("@abtest@");

		    	double t1=Double.parseDouble(cols1[0]);
		    	double t2=Double.parseDouble(cols2[0]);
				return t1 == t2 ? 0 : t1 > t2 ? 1 : -1;


			}
		};
		
	
		
		
  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context)
    throws IOException, InterruptedException {
	  Cluster clu=clusterMap.get(key.toString());
	  int numberSelect=1;
	  int limit=1;
	  if(clu!=null)
	  {
		  limit=Math.min(clu.getNumselect()*this.rep*100, 100000);
		  numberSelect=clu.getNumselect();
		  System.out.println("key:"+key+","+numberSelect);

	  }else{
		  System.out.println("can nott found key:"+key);
	  }
	  
	  if(limit<5000)
	  {
		  limit=5000;
	  }
	  
	  
	  
	  PriorityQueue<String> res= new PriorityQueue<String>(limit,Collections.reverseOrder(cmp));
    for (Text value : values) {

    	if (res.size() < limit) {
			res.add(value.toString());
		} else if (cmp.compare(res.peek(),value.toString()) > 0) {
			res.add(value.toString());
			res.poll();
		}
    }
    
    ArrayList<String> list=new ArrayList<String>(res);
    Collections.sort(list,cmp);
    
    comPair[] writelist=new comPair[numberSelect];
    int end=list.size();
    
    ArrayList<String> left=new ArrayList<String>(res);

    for(int i=0;i<end;i++)
    {
    	String s=list.get(i);
    	System.out.println("111>>"+s);
    	String[] cols=s.split("@abtest@");
    	String line=cols[1];
		Vector group=parse.parseVector(line);
    	for(int j=0;j<writelist.length;j++)
    	{
    		if(writelist[j]==null)
    		{
    			comPair p=new comPair();
    			p.s1=s;
    			p.v1=group;
    			writelist[j]=p;
    			s=null;
    			break;
    		}
    		
    		boolean deny=writelist[j].v1.Deny(group);
    		double dis=writelist[j].v1.distiance(group);
        	System.out.println("222>>"+dis);

    		if(!deny&&writelist[j].distance>dis)
    		{
    			writelist[j].distance=dis;
    			String s_tmp=writelist[j].s2;
    			Vector group_tmp=writelist[j].v2;
    			writelist[j].s2=s;
    			writelist[j].v2=group;
    			s=s_tmp;
    			group=group_tmp;
    			if(s_tmp==null)
    			{
    				break;
    			}
    			
    		}	
    	}
    	
    	if(s!=null)
    	{
    		left.add(s);
    	}
    }
    
    
    int end2=left.size();

    for(int i=0;i<end2;i++)
    {
    	String s=left.get(i);
    	String[] cols=s.split("@abtest@");
    	String line=cols[1];
		Vector group=parse.parseVector(line);
		boolean isset=false;
    	for(int j=0;j<writelist.length;j++)
    	{
    		if(writelist[j]==null||writelist[j].s2!=null)
    		{
    			continue;
    		}
    		
    		double dis=writelist[j].v1.distiance(group);
    		if(writelist[j].distance>dis)
    		{
    	    	System.out.println("333>>"+s);
    			isset=true;
    			writelist[j].distance=dis;
    			String s_tmp=writelist[j].s2;
    			Vector group_tmp=writelist[j].v2;
    			writelist[j].s2=s;
    			writelist[j].v2=group;
    			if(s_tmp==null)
    			{
    				break;
    			}
    			s=s_tmp;
    			group=group_tmp;
    		}	
    	}

    	if(!isset)
    	{
    		break;
    	}
    }
    
    for(int i=0;i<writelist.length;i++)
    {
    	if(writelist[i]!=null&&writelist[i].s2!=null)
    	{
    		int rrr=(int) ((Math.random()*10000)%2);
    		int rrr2=(rrr+1)%2;
    		System.out.println(writelist[i].toString());
    		context.write(key, new Text(writelist[i].distance+"\t"+i+"\trep"+rrr+"_1\t"+writelist[i].s1));
    		context.write(key, new Text(writelist[i].distance+"\t"+i+"\trep"+rrr2+"_2\t"+writelist[i].s2));
    	}
    }
    

  
  }
  public static class comPair{
			public String s1=null;
			@Override
			public String toString() {
				return "comPair [distance="+distance+",s1=" + s1 + ", s2=" + s2 + "]";
			}
			public String s2=null;
			public Vector v1=null;
			public Vector v2=null;
			public double distance=Integer.MAX_VALUE;
  }
  
  

}
