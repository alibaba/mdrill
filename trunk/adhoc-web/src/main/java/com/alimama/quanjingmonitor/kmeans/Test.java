package com.alimama.quanjingmonitor.kmeans;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;

import com.alimama.quanjingmonitor.kmeans.KMeansGroupReducer.Clusterlist;

public class Test {
	static Comparator<Integer>  cmp=new Comparator<Integer>() {

		@Override
		public int compare(Integer t1, Integer t2) {
			return t1 == t2 ? 0 : t1 < t2 ? 1 : -1;


		}
	};
	public static void main(String[] args) {
		int limit=10;
		  PriorityQueue<Integer> res= new PriorityQueue<Integer>(limit,Collections.reverseOrder(cmp));
			ArrayList<Integer> clusters_list=new ArrayList<Integer>();

		  for(int i=100;i>1;i--)
		  {
			  clusters_list.add(i);

		    	if (res.size() < limit) {
					res.add(i);
				} else if (cmp.compare(res.peek(), i) > 0) {
					res.add(i);
				}
		    	
		    
		  }
		  
		  for(Integer i:res)
		  {
			  System.out.println(i);
		  }
		  
			Collections.sort(clusters_list,cmp);
			System.out.println(clusters_list);


	}
}
