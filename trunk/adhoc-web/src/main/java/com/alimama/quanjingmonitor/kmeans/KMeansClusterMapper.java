/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Mapper;


public class KMeansClusterMapper extends Mapper<WritableComparable<?>, Text, Text, Text> {


  
  ParseVector parse=new ParseVector();

  @Override
  protected void map(WritableComparable<?> key, Text point, Context context)
    throws IOException, InterruptedException {

		Cluster nearestCluster = null;
		double nearestDistance = Integer.MAX_VALUE;
		Vector pointv=parse.parseVector(point.toString());
		if(pointv==null)
		{
			return ;
		}
		pointv.setNumPoints(1);
		for (Cluster cluster : clusters) {
			Vector clusterCenter = cluster.getCenter();
			boolean isDeny=clusterCenter.Deny(pointv);
			if(isDeny)
			{
				continue;
			}
			double distance =clusterCenter.distiance(pointv);
		      context.getCounter("Clustering", "similar").increment(1);

			if (distance <= nearestDistance || nearestCluster == null) {
				nearestCluster = cluster;
				nearestDistance = distance;
			}
		}
		if(nearestCluster!=null)
		{
			context.write(new Text(String.valueOf(nearestCluster.getId())),new Text(String.valueOf(nearestDistance)+"@abtest@"+point.toString()));
		}
	
  }
  
  
  private final Collection<Cluster> clusters = new ArrayList<Cluster>();


	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		this.clusters.clear();

		Configuration conf = context.getConfiguration();
		parse.setup(conf);

		try {

			String clusterPath = conf.get(KMeansDriver.CLUSTER_PATH_KEY);
			if (clusterPath != null && clusterPath.length() > 0) {
				KmeansPublic.configureWithClusterInfo(conf, new Path(clusterPath), clusters);
				if (clusters.isEmpty()) {
					throw new IllegalStateException(
							"No clusters found. Check your -c path.");
				}
			}
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	
}
