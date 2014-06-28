
package com.alimama.quanjingmonitor.kmeans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskID;


public class KMeansGroupReducer extends Reducer<Text, Cluster, Text, Cluster> {

	public static class Clusterlist{
		ArrayList<Cluster> list=new ArrayList<Cluster>();
		@Override
		public String toString() {
			return "Clusterlist [ key=" + key + ",list=" + list.toString() + "]";
		}
		Text key;
		public int count()
	  	{
	  		int rtn=0;
	  		for(Cluster cl:this.list)
	  		{
	  			rtn+=cl.getCenter().getNumPoints();
	  		}
	  		return rtn;
	  	}
	}
	  PriorityQueue<Clusterlist> res;
	  static Comparator<Clusterlist>  cmp=new Comparator<Clusterlist>() {
		  	
			@Override
			public int compare(Clusterlist o1, Clusterlist o2) {
				int t1=o1.count();
				int t2=o2.count();
				return t1 == t2 ? 0 : t1 < t2 ? 1 : -1;


			}
		};
		
		int Index=0;
  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    super.setup(context);
    Configuration conf = context.getConfiguration();
    this.res= new PriorityQueue<Clusterlist>(limit,Collections.reverseOrder(cmp));

	TaskID taskId = context.getTaskAttemptID().getTaskID();
	this.Index = taskId.getId()*10000;
  }
  
  int limit=256;
  
  int outputrecord=32;
  
  protected void cleanup(Context context) throws IOException,
			InterruptedException {

		
		ArrayList<Clusterlist> clusters_list=new ArrayList<Clusterlist>();
		for(Clusterlist list:this.res)
		{
			clusters_list.add(list);
		}
		
		System.out.println(clusters_list.size()+"##################");
		int index=0;
		int writecount=0;

		while(true)
		{
			boolean iswrite=false;
			for(Clusterlist list:clusters_list)
			{
				if(list.list.size()>index)
				{
					Cluster tmp=list.list.get(index);
					if(tmp.getCenter().getNumPoints()<10)
					{
						continue;
					}
					Cluster w=new Cluster(tmp.getCenter(),Index+writecount);
					System.out.println(list.key+"\t"+w.toString());
					context.write(list.key, w);
					if(writecount++>outputrecord)
					{
						return ;
					}
					iswrite=true;
				}
			}
			index++;
			if(!iswrite)
			{
				break;
			}
		}
		
		
		
		
		
	


	}
  

  @Override
  protected void reduce(Text key, Iterable<Cluster> values, Context context)
    throws IOException, InterruptedException {

	  Clusterlist list=new Clusterlist();
	  list.key=new Text(key.toString());
	  
	  int eachMaxSize=3;

	  int last_size=0;
    for (Cluster value : values) {
    	
    	int listsize=list.list.size();
    	if(listsize>eachMaxSize)
    	{
    		int index=(int) (Math.random()*100000)%eachMaxSize;
    		list.list.get(index).getCenter().merger(value.getCenter());
    	}else if(list.list.size()<=0||last_size>40)
    	{
    		last_size=0;
    		list.list.add(new Cluster(value));
    	}else{
    		list.list.get(listsize-1).getCenter().merger(value.getCenter());
        	last_size+=value.getCenter().getNumPoints();
    	}
    	
    	context.progress();
    }
    
    
   System.out.println(">>>>>"+list.toString());
    if (this.res.size() < limit) {
    	this.res.add(list);
	} else if (cmp.compare(res.peek(), list) > 0) {
		this.res.add(list);
		this.res.poll();
	}

   
    
    
    
  }
  
}
