package com.alimama.mdrillImport;

import java.util.Map;


import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

public class Topology {
	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
		Map stormconf = Utils.readStormConfig();
		Config conf = new Config();
		conf.putAll(stormconf);
		conf.setMessageTimeoutSecs(10);
		
		String topologyName=args[0];
		String[] prefixlist=args[1].split(",");
		int workescount=Integer.parseInt(args[2]);
		int mb=Integer.parseInt(args[3]);
		conf.put("topology.worker.childopts", "-Xms"+mb+"m -Xmx"+mb+"m -Xmn"+(mb/2)+"m -XX:SurvivorRatio=3 -XX:PermSize=96m -XX:MaxPermSize=256m -XX:+UseParallelGC -XX:ParallelGCThreads=16 -XX:+UseAdaptiveSizePolicy -XX:+PrintGCDetails -XX:+PrintGCTimeStamps  -Xloggc:%storm.home%/logs/gc-import-%port%.log ");
		conf.setNumWorkers(workescount);
		conf.setNumAckers(Math.max(workescount/2, 4));
		String peinding=args[4];
		conf.setMaxSpoutPending(Integer.parseInt(peinding));
		
		TopologyBuilder builder = new TopologyBuilder();
		
		if(args.length>=6)
		{
			String tttime=args[5];
			for(String prefix:prefixlist)
			{
				conf.put(prefix+"-start-time", tttime);
			}
		}
		
		for(String prefix:prefixlist)
		{
			conf.put(prefix+"-validate-time", System.currentTimeMillis());
		}
		
		
		for(String prefix:prefixlist)
		{
			String mode=String.valueOf(conf.get(prefix+"-mode"));
			String threadconfig=conf.get(prefix+"-threads")!=null?String.valueOf(conf.get(prefix+"-threads")):String.valueOf(workescount);
			int threads=workescount;
			try{
				threads=Integer.parseInt(threadconfig);
			}catch(Throwable e)
			{
				threads=workescount;
			}
			int threads_reduce=threads;
			String threadconfig_reduce=conf.get(prefix+"-threads_reduce")!=null?String.valueOf(conf.get(prefix+"-threads_reduce")):String.valueOf(threads_reduce);
			try{
				threads_reduce=Integer.parseInt(threadconfig_reduce);
			}catch(Throwable e)
			{
				threads_reduce=threads;
			}
			
			if(mode.equals("local"))
			{
				builder.setSpout("map_"+prefix, new ImportSpoutLocal(prefix), threads);
			}else{
				builder.setSpout("map_"+prefix, new ImportSpout(prefix), threads);
				builder.setBolt("reduce_"+prefix, new ImportBolt(prefix), threads_reduce).fieldsGrouping("map_"+prefix, new Fields("key") );
			}
		}
		
		StormSubmitter.submitTopology(topologyName, conf,builder.createTopology());

	}
}
