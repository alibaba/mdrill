
package com.alimama.quanjingmonitor.kmeans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class KMeansReducer extends Reducer<Text, Vector, Text, Cluster> {

  private Map<String, Cluster> clusterMap;
  private double convergenceDelta;

  @Override
  protected void reduce(Text key, Iterable<Vector> values, Context context)
    throws IOException, InterruptedException {
	  Vector cluster = new Vector();
	  Vector cluster_default = new Vector();
	  boolean isset=false;
	  boolean ismerger=false;

    for (Vector value : values) {
    	if(value.getNumPoints()>0)
    	{
    	      cluster.merger(value);
    	      ismerger=true;
    	}else{
    		cluster_default.merger(value);
    		isset=true;
    	}
    }
    
    if(!ismerger&&isset)
    {
	      cluster.merger(cluster_default);
    }
    
    Cluster clusterconv = clusterMap.get(key.toString());
    boolean converged = cluster.distiance(clusterconv.getCenter())<this.convergenceDelta;
    if (converged) {
      context.getCounter("Clustering", "Converged Clusters").increment(1);
    }
    
    Cluster clu=new Cluster(cluster, Integer.parseInt(key.toString()));
    clu.setConverged(converged);
    context.write(new Text(key), clu);
  }

  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    super.setup(context);
    Configuration conf = context.getConfiguration();
    try {
    

      this.convergenceDelta = Double.parseDouble(conf.get(KMeansDriver.CLUSTER_CONVERGENCE_KEY));
      this.clusterMap = new HashMap<String, Cluster>();

      String path = conf.get(KMeansDriver.CLUSTER_PATH_KEY);
      if (path.length() > 0) {
        Collection<Cluster> clusters = new ArrayList<Cluster>();
        KmeansPublic.configureWithClusterInfo(conf, new Path(path), clusters);
        setClusterMap(clusters);
        if (clusterMap.isEmpty()) {
          throw new IllegalStateException("Cluster is empty!");
        }
      }
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

  private void setClusterMap(Collection<Cluster> clusters) {
    clusterMap = new HashMap<String, Cluster>();
    for (Cluster cluster : clusters) {
      clusterMap.put(String.valueOf(cluster.getId()), cluster);
    }
    clusters.clear();
  }

 

}
