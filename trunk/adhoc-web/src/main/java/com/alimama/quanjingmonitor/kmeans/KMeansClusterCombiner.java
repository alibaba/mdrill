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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class KMeansClusterCombiner extends Reducer<Text, Text, Text, Text> {

	  private final Collection<Cluster> clusters = new ArrayList<Cluster>();

	  private Map<String, Cluster> clusterMap;
	  int rep=2;

	  private void setClusterMap(Collection<Cluster> clusters) {
		    clusterMap = new HashMap<String, Cluster>();
		    for (Cluster cluster : clusters) {
		      clusterMap.put(String.valueOf(cluster.getId()), cluster);
		    }
		    clusters.clear();
		  }
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			this.clusters.clear();

			Configuration conf = context.getConfiguration();
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
		
		Comparator<Text> cmp=new Comparator<Text>() {

			@Override
			public int compare(Text o1, Text o2) {
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
	  int limit=1;
	  if(clu!=null)
	  {
		  limit=Math.min(clu.getNumselect()*this.rep*10, 50000);

	  }
	  if(limit<1000)
	  {
		  limit=1000;
	  }
	  
	  PriorityQueue<Text> res= new PriorityQueue<Text>(limit,Collections.reverseOrder(cmp));
    for (Text value : values) {

    	if (res.size() < limit) {
			res.add(new Text(value.toString()));
		} else if (cmp.compare(res.peek(), new Text(value.toString())) > 0) {
			res.add(new Text(value.toString()));
			res.poll();
		}
    	
    }
    for(Text s:res)
    {
        context.write(key, s);

    }
  }

}
