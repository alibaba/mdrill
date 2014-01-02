package com.alimama.quanjingmonitor.topology;

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
		conf.put("topology.worker.childopts", "-Xms4g -Xmx4g -Xmn2g -XX:SurvivorRatio=3 -XX:PermSize=96m -XX:MaxPermSize=256m -XX:+UseParallelGC -XX:ParallelGCThreads=16 -XX:+UseAdaptiveSizePolicy -XX:+PrintGCDetails -XX:+PrintGCTimeStamps  -Xloggc:%storm.home%/logs/gc-%port%.log ");
		conf.setMaxSpoutPending(10000);
		int workescount=12;
		conf.setMessageTimeoutSecs(60);
		conf.setNumWorkers(workescount);
		conf.setNumAckers(4);
		
		if(args.length>0)
		{
			conf.put("pv-spout-start-time", args[0]);
			conf.put("click-spout-start-time", args[0]);
			conf.put("access-spout-start-time", args[0]);

		}
		
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("pvmap", new PvSpout("pv-spout"), 48);
		builder.setBolt("reduce", new SumReduceBolt("pv"), 16).fieldsGrouping("pvmap", new Fields("key") );
		//builder.setSpout("accessmap", new AccessSpout("access-spout"), 24);
		//builder.setBolt("accessreduce", new AccessReduceBolt("access"), 4).fieldsGrouping("accessmap", new Fields("hashkey") );

		builder.setSpout("clickmap", new ClickSpout("click-spout"), 4);
		builder.setBolt("clickreduce", new ClickReduceBolt("click"), 4).fieldsGrouping("clickmap", new Fields("hashkey") );
		StormSubmitter.submitTopology("quanjingmointor", conf,builder.createTopology());

	}
}
