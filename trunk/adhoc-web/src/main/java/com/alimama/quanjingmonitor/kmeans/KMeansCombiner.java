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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class KMeansCombiner extends Reducer<Text, Vector, Text, Vector> {

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
        context.write(key, cluster_default);

    }else{
    	context.write(key, cluster);
    }
  }

}
