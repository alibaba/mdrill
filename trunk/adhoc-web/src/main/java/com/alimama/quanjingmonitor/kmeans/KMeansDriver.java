package com.alimama.quanjingmonitor.kmeans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alimama.mdrill.ui.service.AdhocOfflineService;

/**
 * 
 * 
 *
 *
 *
http://quanjing.alimama.com:9999/downloadabtest.jsp?strmatch=rep0&uuid=46ce2a8e-1b74-4fee-bb3a-a9c111e35655
http://quanjing.alimama.com:9999/downloadabtest.jsp?strmatch=rep1&uuid=46ce2a8e-1b74-4fee-bb3a-a9c111e35655
http://adhoc.etao.com:9999/downloadoffline.jsp?uuid=46ce2a8e-1b74-4fee-bb3a-a9c111e35655
http://quanjing.alimama.com:9999/abtestInfo.jsp?uuid=46ce2a8e-1b74-4fee-bb3a-a9c111e35655



http://42.156.210.133:9999/visualization/kmeans.jsp
colls	["prov","seller_star_name"]
colls_important	["main_cat_name"]
jobname	abtest
mailto	yannian.mu@alibaba-inc.com
memo	beiyong
number_important	["(case when (alipay_direct_num+alipay_indirect_num)=0 then 0.00001 else ((alipay_direct_amt+alipay_indirect_amt)/(alipay_direct_num+alipay_indirect_num)) end)"]
numbers	["alipay_indirect_amt","alipay_direct_num","e_gmv_indirect_amt"]
params	{"project":"rpt_p4padhoc_cust","callback":"null","start":"0","rows":"20","q":"[{\"thedate\":{\"operate\":5,\"value\":[\"20140521\",\"20140520\",\"20140519\",\"20140518\",\"20140517\",\"20140516\"]}}]","dist":"null","username":"yannian.mu","fl":"prov,seller_star_name,sum(alipay_direct_amt),sum(alipay_indirect_amt),sum(alipay_indirect_num),sum(alipay_direct_num),sum(e_gmv_direct_cnt),average(e_gmv_direct_cnt),sum(e_gmv_direct_amt),sum(e_gmv_indirect_amt),sum(e_gmv_indirect_cnt)","groupby":"prov,seller_star_name","sort":"null","order":"null","leftjoin":"null","dimvalue":"省, 卖家星级, 求和(直通车直接成交金额), 求和(直通车间接成交金额), 求和(直通车间接成交笔数), 求和(直通车直接成交笔数), 求和(GMV直接成交笔数), 平均值(GMV直接成交笔数), 求和(GMV直接成交金额), 求和(GMV间接成交金额), 求和(GMV间接成交笔数)","jobparam":"过滤条件："}
project	rpt_p4padhoc_cust
q	[{"thedate":{"operate":5,"value":["20140521","20140520","20140519","20140518","20140517","20140516"]}}]
rnd	0.13910470720947243
username	yannian.mu




 *  hadoop fs -cat /group/tbdp-etao-adhoc/p4padhoc/abtest_cust/2001/cluster_abtest/*|sed 's/\x01/\x09/g'|more
 hadoop jar ./kkk.jar  com.alimama.quanjingmonitor.kmeans.KMeansDriver  0 /group/tbdp-etao-adhoc/p4padhoc/abtestforbp /group/tbdp-etao-adhoc/p4padhoc/abtest_out/131  3 200  0.0001 1000 2 10 0,1@2@3,4 1048576
 hadoop jar ./kkk.jar  com.alimama.quanjingmonitor.kmeans.KMeansDriver  0 /group/tbdp-etao-adhoc/p4padhoc/abtest_out/131/cluster_1  xxx  0 10 0.0001 1000 2 10 0,1@2@3,4 0000 |more
 
 
 hadoop jar ./kkk.jar  com.alimama.quanjingmonitor.kmeans.KMeansDriver  0 /group/tbdp-etao-adhoc/p4padhoc/abtestforbp /group/tbdp-etao-adhoc/p4padhoc/abtest_out/500  4 1000  0.0001 1000 2 100 0;1;2;3,4 548576 
 
 
 
 
 hadoop jar ./kkk.jar  com.alimama.quanjingmonitor.kmeans.KMeansDriver  0 /group/tbdp-etao-adhoc/p4padhoc/adhoc_cust_seed /group/tbdp-etao-adhoc/p4padhoc/abtest_cust/001  2 999  0.0001 1500 2 100 "1;2;3,4;5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29" 548576
 
 hadoop jar ./kkk.jar  com.alimama.quanjingmonitor.kmeans.KMeansDriver  0 /group/tbdp-etao-adhoc/p4padhoc/abtest_cust/001/part-InitCenter  xxx  0 10 0.0001 1000 2 10 "1;2;3,4;5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29" 0000 |more
 
 
 
 
  hadoop fs -cat /group/tbdp-etao-adhoc/p4padhoc/abtest_cust/kkk1413/cluster_abtest/*|sed 's/\x01/\x09/g' 
  
  |grep rep0>abtest_1500_rep0.txt
  
  nohup hadoop jar ./kkk_ok.jar  com.alimama.quanjingmonitor.kmeans.KMeansDriver  0 /group/tbdp-etao-adhoc/p4padhoc/adhoc_cust_seed /group/tbdp-etao-adhoc/p4padhoc/abtest_cust/kkk_ok  15  999  0.0001 1500 2 100 "1,3,4;2;3,4;5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29" 548576 >kkk_ok_1500.log &

 
 * @author yannian.mu
 * 
 * 
 *
 */
public class KMeansDriver extends Configured implements Tool {

	MysqlCallbackKmeans callback=null;
	public KMeansDriver(MysqlCallbackKmeans callback) {
		this.callback = callback;
	}


	private static Logger LOG = Logger.getLogger(KMeansDriver.class);

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(), new KMeansDriver(null), args);
	}

	@Override
	public int run(String[] args) throws Exception {
		LOG.info("KMeansDriver start :"+Arrays.toString(args));
		Path input=new Path(args[1]);
		Path output=new Path(args[2]);
		int maxIterations=Integer.parseInt(args[3]);
		int k=Integer.parseInt(args[4]);
		String delta=args[5];
		int count=Integer.parseInt(args[6]);
		int rep=Integer.parseInt(args[7]);
		int reduce= Integer.parseInt(args[8]);
		String config=args[9];
		String splitsize=args[10];
		this.KMeans(input, output, maxIterations, k,delta, count, rep,reduce,config,splitsize);
		return 0;
	}
	

	private ParseVector parse = new ParseVector();
	private int reduce=2;
	public void KMeans(
			Path input, Path output, int maxIterations, int k,String delta, int count, int rep,int reduce,String config
			,String splitsize) throws IOException,
			InterruptedException, ClassNotFoundException {
		this.reduce=reduce;
		Configuration conf = this.getConf();
		conf.set("mapred.max.split.size", splitsize);
		conf.set("abtest.kmeans.config", config);
		FileSystem fs=FileSystem.get(conf);

		if(maxIterations==0)
		{
			ArrayList<Cluster> clusters = getClusters(input, conf, fs);
			
			for(Cluster c:clusters){
				System.out.println(c.asFormatString());
			}

			return ;
		}
		
		

		parse.setup(conf);
//		HadoopUtil.delete(conf, output);

		fs.mkdirs(output);
		if(callback!=null)
		{
			callback.setPercent("Stage-6 map = 100.0%, reduce = 100.0%");
			callback.maybeSync();
		}
		Path clusters_random = this.InitCenter(conf, input, output, k);
//
		Path finalClusters = this.buildClustersMR(conf, input, clusters_random,
				output, maxIterations, delta);

//		Path finalClusters=new Path("/group/tbdp-etao-adhoc/p4padhoc/abtest_cust/kkk_ok_1709/cluster_20");///group/tbdp-etao-adhoc/p4padhoc/abtest_out/130/cluster_41

		Path clustersOut = this.makeFinalCluster(conf, finalClusters, output,delta, count, rep);
		if(callback!=null)
		{
			callback.setPercent("Stage-28 map = 100.0%, reduce = 100.0%");
			callback.maybeSync();
		}
		
		this.clusterDataMR(conf, input, clustersOut, output, delta, rep);
		
	}

	private Path makeFinalCluster(Configuration conf, Path finalClusters,
			Path output,  String delta, int count, int rep)
			throws IOException {
		FileSystem fs = FileSystem.get(finalClusters.toUri(), conf);

		ArrayList<Cluster> clusters = getClusters(finalClusters, conf, fs);

		long total = 0;
		for (Cluster c : clusters) {
			total += c.getCenter().getNumPoints();
		}

		float rate = total / count;

		HashMap<Integer, Integer> hashm = new HashMap<Integer, Integer>();

		long total2 = 0;
		for (Cluster c : clusters) {
			long points = c.getCenter().getNumPoints();
			if(points>=20)
			{
				int allow = (int) (points / rate)+1;
				total2 += allow;
				hashm.put(c.getId(), allow);
			}else{
				hashm.put(c.getId(), 0);

			}
		}

		Collections.sort(clusters, new Comparator<Cluster>() {

			@Override
			public int compare(Cluster o1, Cluster o2) {
				long t1 = o1.getCenter().getNumPoints();
				long t2 = o2.getCenter().getNumPoints();
				return t1 == t2 ? 0 : t1 < t2 ? 1 : -1;
			}
		});

		long left = total2 - count;

		while(true)
		{
			if (left <= 0) {
				break;
			}
			for (Cluster c : clusters) {
				if (left <= 0) {
					break;
				}
				int id = c.getId();
				int num = hashm.get(id);
				if(num>0)
				{
					hashm.put(id, num - 1);
					left--;
				}
			}
		}

		for (int i = 0; i < clusters.size(); i++) {
			Cluster c = clusters.get(i);
			int id = c.getId();
			int num = hashm.get(id);
			c.setNumselect(num);
		}

		Path clustersOut = new Path(output, "cluster_final");

		int suma=0;
		int sumb=0;

		SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf,
				clustersOut, Text.class, Cluster.class);

		for (int i = 0; i < clusters.size(); i++) {
			Cluster c = clusters.get(i);
			int id = c.getId();
			suma+=c.getCenter().getNumPoints();
			sumb+=c.getNumselect();

			if(i<10)
			{
				System.out.println(id +"@"+c.getCenter().getNumPoints()+ "@" + c.getNumselect()+","+c.asFormatString());
			}

			writer.append(new Text(String.valueOf(id)), c);
		}

		writer.close();
		
		System.out.println(suma+"==="+sumb);

		return clustersOut;
	}

	

	private void clusterDataMR(Configuration conf, Path input, Path clustersIn,
			Path output, String convergenceDelta, int rep) throws IOException,
			InterruptedException, ClassNotFoundException {
		FileSystem fs=FileSystem.get(conf);
		conf.set(CLUSTER_PATH_KEY, clustersIn.toString());
		conf.set(CLUSTER_CONVERGENCE_KEY, convergenceDelta);
		conf.setInt(CLUSTER_CONVERGENCE_ABTEST_REP, rep);

		Job job = new Job(conf,
				"KMeans Driver running clusterData over input: " + input);
//		job.setInputFormatClass(FileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(KMeansClusterMapper.class);
		job.setCombinerClass(KMeansClusterCombiner.class);
		job.setReducerClass(KMeansClusterReduce.class);
		job.setNumReduceTasks(this.reduce);

		FileInputFormat.setInputPaths(job, input);
		FileOutputFormat.setOutputPath(job, new Path(output, "cluster_abtest"));

		job.setJarByClass(KMeansDriver.class);

		if (!job.waitForCompletion(true)) {
			throw new InterruptedException(
					"K-Means Clustering failed processing " + clustersIn);
		}
	}

	private Path buildClustersMR(Configuration conf, Path input,
			Path clustersIn, Path output, int maxIterations, String delta)
			throws IOException, InterruptedException, ClassNotFoundException {

		boolean converged = false;
		int iteration = 1;
		while (!converged && iteration <= maxIterations) {
			int stage=7+iteration;
			if(callback!=null)
			{
				callback.setPercent("Stage-"+stage+" map = 100.0%, reduce = 100.0%");
                callback.maybeSync();
			}

			
			Path clustersOut = new Path(output, "cluster_" + iteration);
			converged = runIteration(conf, input, clustersIn, clustersOut,
					delta);
			clustersIn = clustersOut;
			iteration++;
		}
		return clustersIn;
	}

	public static String CLUSTER_CONVERGENCE_ABTEST_REP = "org.apache.mahout.clustering.kmeans.abtest.rep";

	public static String CLUSTER_CONVERGENCE_KEY = "org.apache.mahout.clustering.kmeans.convergence";
	public static String CLUSTER_PATH_KEY = "org.apache.mahout.clustering.kmeans.path";

	
	
	private boolean runIteration(Configuration conf, Path input,
			Path clustersIn, Path clustersOut, String convergenceDelta)
			throws IOException, InterruptedException, ClassNotFoundException {

		conf.set(CLUSTER_PATH_KEY, clustersIn.toString());
		conf.set(CLUSTER_CONVERGENCE_KEY, convergenceDelta);
		FileSystem fs=FileSystem.get(conf);

		Job job = new Job(conf);
		job.setJobName(	"KMeans Driver running runIteration over clustersIn: "
				+ clustersIn);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Vector.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Cluster.class);

//		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setMapperClass(KMeansMapper.class);
		job.setCombinerClass(KMeansCombiner.class);
		job.setReducerClass(KMeansReducer.class);

		FileInputFormat.addInputPath(job, input);
		SequenceFileOutputFormat.setOutputPath(job, clustersOut);

		job.setNumReduceTasks(this.reduce);
		job.setJarByClass(KMeansDriver.class);
//		HadoopUtil.delete(conf, clustersOut);
		if (!job.waitForCompletion(true)) {
			throw new InterruptedException(
					"K-Means Iteration failed processing " + clustersIn);
		}

		return isConverged(clustersOut, conf, fs);
	}

	private static ArrayList<Cluster> getClusters(Path filePath,
			Configuration conf, FileSystem fs) throws IOException {
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		KmeansPublic.configureWithClusterInfo(conf, filePath, clusters);
		if (clusters.isEmpty()) {
			throw new IllegalStateException(
					"No clusters found. Check your -c path.");
		}

		return clusters;
	}

	private static boolean isConverged(Path filePath, Configuration conf,
			FileSystem fs) throws IOException {

		try {

			Collection<Cluster> clusters = getClusters(filePath, conf, fs);
			for (Cluster c : clusters) {
				if (!c.isConverged()) {
					return false;
				}
			}

		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}

		return true;

	}

	
	private Path InitCenter(Configuration conf, Path input, Path output, int k)
			throws IOException, InterruptedException, ClassNotFoundException {
		FileSystem fs = FileSystem.get(output.toUri(), conf);
		Path outFile = new Path(output, "part-InitCenter");
		Job job = new Job(conf);
		job.setJobName(	"KMeans Driver: "+ outFile);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Cluster.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Cluster.class);

		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setMapperClass(KMeansGroupMapper.class);
		job.setCombinerClass(KMeansGroupCombine.class);
		job.setReducerClass(KMeansGroupReducer.class);

		FileInputFormat.addInputPath(job, input);
		SequenceFileOutputFormat.setOutputPath(job, outFile);

		job.setNumReduceTasks(32);
		job.setJarByClass(KMeansDriver.class);
//		HadoopUtil.delete(conf, clustersOut);
		if (!job.waitForCompletion(true)) {
			throw new InterruptedException(
					"K-Means Iteration failed processing " + outFile);
		}
		return outFile;

	}
	public Path buildRandom(Configuration conf, Path input, Path output, int k)
			throws IOException {
		FileSystem fs = FileSystem.get(output.toUri(), conf);
		Path outFile = new Path(output, "part-randomSeed");
		fs.mkdirs(outFile);

		Path inputPathPattern;
		System.out.println(input);
		if (fs.getFileStatus(input).isDir()) {
			inputPathPattern = new Path(input, "*");
		} else {
			inputPathPattern = input;
		}
		
		System.out.println("######"+k);

		FileStatus[] inputFiles = fs.globStatus(inputPathPattern,
				KmeansPublic.FILTER);
		SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf,
				new Path(outFile,"random"), Text.class, Cluster.class);
		List<Cluster> chosenClusters = new ArrayList<Cluster>(k);

		int nextClusterId = 0;
		int fileMaxReadCount = 50000;
		if (fileMaxReadCount <= k) {
			fileMaxReadCount = k;
		}

		if (inputFiles.length > 0) {
			fileMaxReadCount = fileMaxReadCount / inputFiles.length;
		}

		if (fileMaxReadCount <= 10) {
			fileMaxReadCount = 10;
		}
		
		System.out.println("#####"+k+"@"+fileMaxReadCount);


		int number = 0;
		for (FileStatus fileStatus : inputFiles) {
			if (fileStatus.isDir()) {
				continue;
			}

			int filehasread = 0;
			FSDataInputStream in = fs.open(fileStatus.getPath());
			BufferedReader bf = new BufferedReader(
					new InputStreamReader(in));
			String line;
			while ((line = bf.readLine()) != null) {
				Vector vec=parse.parseVector(line);
				if(vec==null)
				{
					continue;
				}
//				System.out.println(filehasread+"@"+fileMaxReadCount+","+vec.toString());
				number++;
				filehasread++;
				
				int currentSize = chosenClusters.size();
				if (currentSize < k) {
					Cluster newCluster = new Cluster(vec,nextClusterId++);
					chosenClusters.add(newCluster);
				} else {
					int randIndex = (int) (Math.random() * currentSize);
					chosenClusters.get(randIndex).getCenter().merger(vec);
				}

				if (filehasread > fileMaxReadCount) {
					break;
				}

			}
			bf.close();
			in.close();

		}

		for (int i = 0; i < k; i++) {
			Cluster closter = chosenClusters.get(i);
			closter.setId(i);
			writer.append(new Text(String.valueOf(i)), closter);
		}
		writer.close();
	

		return outFile;
	}

}
