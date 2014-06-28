
package com.alimama.quanjingmonitor.kmeans;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class KMeansGroupCombine extends Reducer<Text, Cluster, Text, Cluster> {

  @Override
  protected void reduce(Text key, Iterable<Cluster> values, Context context)
    throws IOException, InterruptedException {
	  Cluster clrnew=null;
    for (Cluster value : values) {
    	if(clrnew==null)
    	{
    		clrnew=new Cluster(value);
    	}else{
    		clrnew.getCenter().merger(value.getCenter());

    	}
    }
    context.write(key, clrnew);
  }
  
  



 

}
