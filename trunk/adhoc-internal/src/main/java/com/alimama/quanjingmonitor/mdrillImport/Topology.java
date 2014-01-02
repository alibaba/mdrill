package com.alimama.quanjingmonitor.mdrillImport;

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
		conf.setMaxSpoutPending(5000);
		conf.setMessageTimeoutSecs(60);
		
		String topologyName=args[0];
		String[] prefixlist=args[1].split(",");
		String tttime=args[2];
		int workescount=Integer.parseInt(args[3]);
		int mb=Integer.parseInt(args[4]);
		conf.put("topology.worker.childopts", "-Xms"+mb+"m -Xmx"+mb+"m -Xmn"+(mb/2)+"m -XX:SurvivorRatio=3 -XX:PermSize=96m -XX:MaxPermSize=256m -XX:+UseParallelGC -XX:ParallelGCThreads=16 -XX:+UseAdaptiveSizePolicy -XX:+PrintGCDetails -XX:+PrintGCTimeStamps  -Xloggc:%storm.home%/logs/gc-import-%port%.log ");
		conf.setNumWorkers(workescount);
		conf.setNumAckers(Math.max(workescount/4, 4));
		TopologyBuilder builder = new TopologyBuilder();
		for(String prefix:prefixlist)
		{
			conf.put(prefix+"-spout-start-time", tttime);
			
		}
		for(String prefix:prefixlist)
		{
			String mode=String.valueOf(conf.get(prefix+"-mode"));
			if(mode.equals("local"))
			{
				builder.setSpout("map_"+prefix, new ImportSpoutLocal(prefix), workescount*2);
			}else{
				builder.setSpout("map_"+prefix, new ImportSpout(prefix), workescount*2);
				builder.setBolt("reduce_"+prefix, new ImportBolt(prefix), workescount).fieldsGrouping("map_"+prefix, new Fields("key") );
			}
		}
		
		StormSubmitter.submitTopology(topologyName, conf,builder.createTopology());

	}
}
