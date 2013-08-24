package com.alimama.mdrill.topology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Map;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

import com.alimama.mdrill.index.JobIndexerPartion;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alipay.bluewhale.core.cluster.Cluster;
import com.alipay.bluewhale.core.cluster.ClusterState;
import com.alipay.bluewhale.core.cluster.StormClusterState;
import com.alipay.bluewhale.core.command.KillTopology;
import com.alipay.bluewhale.core.custom.CustomAssignment;
import com.alipay.bluewhale.core.utils.StormUtils;

public class MdrillMain {
	public static void main(String[] args) throws Exception {
		String type = args[0];
		if (type.equals("table")) {
			runTable(args);
			return;
		}
		if (type.equals("create")) {
			createtable(args);
			return;
		}
		if (type.equals("drop")) {
			cleartable(args);
			return;
		}
		if (type.equals("index")) {
			makeIndex(args);
			return;
		}
		
		

	}
	
	
	private static void createtable(String[] args) throws Exception {
		String tableConfig = args[1];
		String stormhome = System.getProperty("storm.home");
		if (stormhome == null) {
			stormhome=".";
		}
				
		
		Map stormconf = Utils.readStormConfig();
		String hdfsSolrDir = (String) stormconf.get("higo.table.path");
		Configuration conf = getConf(stormconf);

		FileSystem fs=FileSystem.get(conf);
		FileSystem lfs=FileSystem.getLocal(conf);
		String topschema=getFileContent(lfs,new Path(stormhome,"solr/conf/schema.top.txt"));
		String downschema=getFileContent(lfs,new Path(stormhome,"solr/conf/schema.down.txt"));
		String fieldschema=getFileContent(lfs,new Path(tableConfig));
		CCJSqlParserManager parserManager = new CCJSqlParserManager();
		CreateTable createTable = (CreateTable) parserManager.parse(new StringReader(fieldschema));
		StringBuffer buffer=new StringBuffer();
		String tableName=createTable.getTable().getName();
		buffer.append("\r\n");

		for(int i=0;i<createTable.getColumnDefinitions().size();i++)
		{
			ColumnDefinition col=(ColumnDefinition)createTable.getColumnDefinitions().get(i);
			String colname=col.getColumnName();
			String type=col.getColDataType().getDataType();
			if(type.equals("long")||type.equals("int")||type.equals("tint")||type.equals("bigint"))
			{
				type="tlong";
			}
			
			
			if(type.equals("float")||type.equals("double")||type.equals("tfloat"))
			{
				type="tdouble";
			}
			if(type.indexOf("char")>0||type.indexOf("string")>0)
			{
				type="string";
			}
			
			buffer.append("<field name=\""+colname+"\" type=\""+type+"\" indexed=\"true\" stored=\"false\"/>");
			buffer.append("\r\n");
		}
		
		Path tablepath=new Path(hdfsSolrDir,tableName);
		Path solrpath=new Path(tablepath,"solr");
		if(!fs.exists(tablepath))
		{
			fs.mkdirs(tablepath);
		}
		if(fs.exists(solrpath))
		{
			fs.delete(solrpath,true);
		}
		
		fs.copyFromLocalFile(new Path(stormhome,"solr"), solrpath);
		
		if(fs.exists(new Path(solrpath,"conf/schema.xml")))
		{
			fs.delete(new Path(solrpath,"conf/schema.xml"),true);
		}
		
		
		
		FSDataOutputStream out=fs.create(new Path(solrpath,"conf/schema.xml"));
		java.io.BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(out));
		writer.append(topschema);
		writer.append(buffer.toString());
		writer.append(downschema);
		writer.close();
		out.close();
		
		System.out.println(buffer.toString());
		System.out.println("create succ at "+tablepath.toString());

		System.exit(0);

	}
	
	private static String getFileContent(FileSystem fs,Path f) throws IOException
	{
		StringBuffer buffer=new StringBuffer();
		FSDataInputStream in=fs.open(f);
		BufferedReader bf  = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = bf.readLine()) != null) {
			buffer.append(line);
			buffer.append("\r\n");
		}
		bf.close();
		in.close();
		
		return buffer.toString();
	}

	private static void makeIndex(String[] args) throws Exception {
		String tablename=args[1];
		String inputdir = args[2];
		Integer maxday = Integer.parseInt(args[3]);
		String startday = args[4];
		
		String format = "seq";
		if (args.length > 5) {
			format = args[5];
		}
		String split = "";
		if (args.length > 6) {
			split = args[6];
		}
		String submatch = "*";
		if (args.length > 7) {
			submatch = args[7];
		}
		
		Map stormconf = Utils.readStormConfig();
		Integer shards = StormUtils.parseInt(stormconf.get("higo.shards.count"));
		String hdfsSolrDir = (String) stormconf.get("higo.table.path");
		Configuration conf = getConf(stormconf);
		String[] jobValues = new String[]{split,submatch};
		String type = (String) stormconf.get("higo.partion.type");
		String tabletype = (String) stormconf.get("higo.partion.type."+tablename);
		if(tabletype!=null&&!tabletype.isEmpty())
		{
			type=tabletype;
		}


		JobIndexerPartion index = new JobIndexerPartion(conf, shards,
				new Path(hdfsSolrDir,tablename).toString(), inputdir, 3, maxday, startday, format, type);
		ToolRunner.run(conf, index, jobValues);

		System.exit(0);

	}

	private static Configuration getConf(Map stormconf) {
		String hadoopConfDir = (String) stormconf.get("hadoop.conf.dir");
		String opts = (String) stormconf.get("hadoop.java.opts");
		Configuration conf = new Configuration();
		conf.set("mapred.child.java.opts", opts);
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	}

	private static void runTable(String[] args) throws Exception {
		String tableName = args[1];
		Map stormconf = Utils.readStormConfig();
		ClusterState zkClusterstate = Cluster
				.mk_distributed_cluster_state(stormconf);
		StormClusterState zkCluster = Cluster
				.mk_storm_cluster_state(zkClusterstate);
		zkCluster.higo_remove(tableName);
		zkCluster.disconnect();

		Integer shards = StormUtils
				.parseInt(stormconf.get("higo.shards.count"));
		
		Integer fixassign = StormUtils.parseInt(stormconf.containsKey("higo.fixed.shards")?stormconf.get("higo.fixed.shards"):0);
		Integer partions = StormUtils.parseInt(stormconf
				.get("higo.cache.partions"));
		Integer portbase = StormUtils.parseInt(stormconf
				.get("higo.solr.ports.begin"));

		String hdfsSolrDir = (String) stormconf.get("higo.table.path");

		String topologyName = "adhoc";

		String hadoopConfDir = (String) stormconf.get("hadoop.conf.dir");
		String localWorkDirList = (String) stormconf.get("higo.workdir.list");
		Integer msCount = StormUtils.parseInt(stormconf
				.get("higo.mergeServer.count"));
		Integer realtimecount = StormUtils.parseInt(stormconf
				.get("higo.realtime.count"));
		Config conf = new Config();
		conf.setNumWorkers(shards + msCount);
		conf.setNumAckers(1);
		conf.setMaxSpoutPending(100);
		conf.put(CustomAssignment.TOPOLOGY_CUSTOM_ASSIGNMENT,
				MdrillTaskAssignment.class.getName());
		
		conf.put(MdrillTaskAssignment.HIGO_FIX_SHARDS,fixassign);
		for(int i=1;i<=fixassign;i++)
		{
		conf.put(MdrillTaskAssignment.HIGO_FIX_SHARDS+"."+i,(String) stormconf.get("higo.fixed.shards"+"."+i));
		}
		conf.put(MdrillTaskAssignment.MS_PORTS,
				(String) stormconf.get("higo.merge.ports"));
		conf.put(MdrillTaskAssignment.REALTIME_PORTS,
				(String) stormconf.get("higo.realtime.ports"));
		conf.put(MdrillTaskAssignment.MS_NAME, "merge");
		conf.put(MdrillTaskAssignment.SHARD_NAME, "shard");
		conf.put(MdrillTaskAssignment.REALTIME_NAME, "realtime");

		ShardsBolt solr = new ShardsBolt(false, hadoopConfDir, hdfsSolrDir,
				tableName, localWorkDirList, portbase, shards, partions,
				topologyName);
		ShardsBolt ms = new ShardsBolt(false, hadoopConfDir, hdfsSolrDir,
				tableName, localWorkDirList, portbase + shards + 100, 0,
				partions, topologyName);
		ShardsBolt realtime = new ShardsBolt(true, hadoopConfDir, hdfsSolrDir,
				tableName, localWorkDirList, portbase + shards + 200, 0,
				partions, topologyName);

		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("heartbeat", new HeartBeatSpout(), 1);
		builder.setBolt("shard", solr, shards).allGrouping("heartbeat");
		builder.setBolt("merge", ms, msCount).allGrouping("heartbeat");
		if (realtimecount > 0) {
			builder.setBolt("realtime", realtime, msCount).allGrouping(
					"heartbeat");
		}
		StormSubmitter.submitTopology(topologyName, conf,
				builder.createTopology());
		System.out.println("start complete ");
		System.exit(0);
	}
	private static void cleartable(String[] args) throws Exception {
		String tableName = args[1];
		String topologyName = "adhoc";
		String[] killargs = {topologyName};
		try {
			KillTopology.main(killargs);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Map stormconf = Utils.readStormConfig();
		ClusterState zkClusterstate = Cluster
				.mk_distributed_cluster_state(stormconf);
		StormClusterState zkCluster = Cluster
				.mk_storm_cluster_state(zkClusterstate);
		zkCluster.higo_remove(topologyName);
		for (String s : tableName.split(",")) {
			zkCluster.higo_remove(s);
		}
		zkCluster.disconnect();
		System.exit(0);
	}

}
