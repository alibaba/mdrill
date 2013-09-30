package com.alimama.mdrill.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import backtype.storm.utils.Utils;

import com.alimama.mdrill.index.utils.DocumentList;
import com.alimama.mdrill.index.utils.DocumentMap;
import com.alimama.mdrill.index.utils.JobIndexPublic;
import com.alimama.mdrill.index.utils.PairWriteable;
import com.alimama.mdrill.utils.HadoopUtil;
import com.alipay.bluewhale.core.utils.StormUtils;



/**
 * 
hadoop fs -rmr /group/taobao/external/p4p/p4padhoc/tmp/clear/* &&./bluewhale  jar ../lib/bluewhale-higo-1.0.2-SNAPSHOT.jar  com.alipay.higo.hadoop.job.MakeIndex /group/taobao/external/p4p/p4padhoc/tablelist/rpt_b2bad_hoc_memb_sum_d/ /group/taobao/external/p4p/p4padhoc/tmp/ myn  /group/taobao/external/p4p/p4padhoc/tmp/clear/out /group/taobao/external/p4p/p4padhoc/tmp/clear/index , "userid_s,pv1_s,pv2_s"
hadoop fs -rmr /group/taobao/external/p4p/p4padhoc/tmp/clear/* &&./bluewhale  jar ../lib/bluewhale-higo-1.0.2-SNAPSHOT.jar  com.alipay.higo.hadoop.job.MakeIndex /group/taobao/external/p4p/p4padhoc/tablelist/rpt_b2bad_hoc_memb_sum_d/ /group/taobao/external/p4p/p4padhoc/tmp/ myncount  /group/taobao/external/p4p/p4padhoc/tmp/clear/out /group/taobao/external/p4p/p4padhoc/tmp/clear/index , "cnt_s,num_s"
hadoop fs -rmr /group/taobao/external/p4p/p4padhoc/tmp/clear/* &&./bluewhale  jar ../lib/bluewhale-higo-1.0.2-SNAPSHOT.jar  com.alipay.higo.hadoop.job.MakeIndex /group/taobao/external/p4p/p4padhoc/tablelist/r_rpt_cps_luna_item/ /group/taobao/external/p4p/p4padhoc/tmp/myn/ rpitem  /group/taobao/external/p4p/p4padhoc/tmp/clear/out /group/taobao/external/p4p/p4padhoc/tmp/clear/index , "thedate_s,userid_s,c1_s,c2_s,c3_s"

 *
 * 
 */
public class MakeIndex {
	public static void main(String[] args) throws IOException, Exception {
		Map stormconf = Utils.readStormConfig();

		Configuration conf=getConf(stormconf);
		
		HashSet<String> inputs=new HashSet<String>();
		inputs.add(args[2]);//inputs
		 MakeIndex.make(FileSystem.get(conf), args[0]//solrhome
		                                           , conf,"txt", args[1],//inputbase
		                                           inputs,//
		                                           "*", 
		                                           args[3],//output
		                                           new Path(args[4]),//smallindex
		                                           1, args[5]//split
		                                                   ,false,
		                                                   args[6]//custFields
		                                                        ,null,10
		                                                               
		 );

	}
	
	public static String parseSplit(String str)
	{
		return SplitStrEncode.parseSplit(str);
	}
	
	public static interface updateStatus{
		public void update(int statge,Job job);
		public boolean dump(Job job);
		public void finish();
	}
	
	public static int make(
			FileSystem fs,
			String solrHome,
			Configuration jconf ,
			String filetype,
			
			String inputBase,
			HashSet<String> inputs,
			String inputmatch,
			String output,
			Path smallindex,
			int shards,
			String split,
			boolean usedthedate,
			String custFields,
			updateStatus update,Integer parallel 
			) throws Exception
	{
		return make(fs, solrHome, jconf, filetype, inputBase, inputs, inputmatch, output, smallindex, shards, split, usedthedate, custFields, update,"",parallel);
	}
	
	
	public static HashSet<FileStatus> getInputList(
			FileSystem fs,
			String inputBase,
			HashSet<String> inputs,
			String inputmatch
			) throws IOException
	{
		
		HashSet<FileStatus> rtn=new HashSet<FileStatus>();
		for (String input : inputs) {
			Path p = new Path(inputBase, "*" + input + "*/"+inputmatch+"");
			FileStatus[] list=fs.globStatus(p);
			if(list==null)
			{
				continue;
			}
			for(FileStatus f:list)
			{
				rtn.add(f);
			}
		}
		return rtn;
	}
   
	public static int make(
			FileSystem fs,
			String solrHome,
			Configuration jconf ,
			String filetype,
			
			String inputBase,
			HashSet<String> inputs,
			String inputmatch,
			String output,
			Path smallindex,
			int shards,
			String split,
			boolean usedthedate,
			String custFields,
			updateStatus update,String uniqCheckField,Integer parallel 
			) throws Exception
	{
		Job job = new Job(new Configuration(jconf));
		JobIndexPublic.setJars(job.getConfiguration());
		if (filetype.equals("seq")) {
			job.setInputFormatClass(SequenceFileInputFormat.class);
			for (String input : inputs) {
				Path p = new Path(inputBase, "*" + input + "*/"+inputmatch+"");
				System.out.println(p.toString());
				SequenceFileInputFormat.addInputPath(job, p);
			}
		} else {
			for (String input : inputs) {
				Path p = new Path(inputBase, "*" + input + "*/"+inputmatch+"");
				System.out.println(p.toString());
				FileInputFormat.addInputPath(job, p);
			}
		}
		
		Path baseP=new Path(output);
		Path baseParent=new Path(output);
		if(baseP.getParent()!=null&&baseP.getParent().getParent()!=null)
		{
			baseParent=baseP.getParent().getParent();
		}

		String jobnameOutput=new String(baseParent.toString()+"*"+baseP.getName());
		int cutoutlen=50;
		if(jobnameOutput.length()>cutoutlen)
		{
			jobnameOutput="*"+jobnameOutput.substring(jobnameOutput.length()-cutoutlen, jobnameOutput.length());
		}
		
		System.out.println("output:"+output+"@"+jobnameOutput);
		System.out.println("tmp:"+smallindex.toString());
		job.setJobName("mdrill_stage_1@"+jobnameOutput);
		job.setJarByClass(JobIndexerPartion.class);

		fs.delete(new Path(output), true);
		fs.delete(smallindex, true);
		Configuration conf = job.getConfiguration();

		String fields = JobIndexPublic.readFieldsFromSchemaXml(solrHome+ "/solr/conf/schema.xml",fs,conf);
		JobIndexPublic.setDistributecache(new Path(solrHome,"solr/conf"), fs,conf);
		if(!split.isEmpty()&&!split.equals("default")&&!split.equals("\001"))
		{
			conf.set("higo.column.split", split);
		}
		
		conf.set("uniq.check.field", uniqCheckField);
		
		if(split.equals("\t"))
		{
			conf.set("higo.column.split", "tab");
		}
		conf.set("higo.column.custfields", custFields);
		conf.set("higo.input.base", inputBase);
		
		
		
		conf.setBoolean("higo.column.userthedate", usedthedate);
		conf.set("io.sort.mb", "80");
		conf.set("mapred.reduce.slowstart.completed.maps", "0.01");
		conf.set("higo.index.fields", fields);
		job.setPartitionerClass(PairPartion.class) ;

		job.setMapperClass(IndexMapper.class);
		job.setMapOutputKeyClass(PairWriteable.class);
		job.setMapOutputValueClass(DocumentMap.class);
		job.setReducerClass(IndexReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		FileOutputFormat.setOutputPath(job, smallindex);
		job.setNumReduceTasks(shards * parallel);
		if(update!=null)
		{
	        job.submit();
	        while(!job.isComplete())
	        {
	        	update.update(1, job);
	        	Thread.sleep(3000);
	        }
	        if(update.dump(job)){
	        	return -1;
	        }
		}else{
			job.waitForCompletion(true);
		}
        
        


		Job job2 = new Job(new Configuration(jconf));
		JobIndexPublic.setJars(job2.getConfiguration());
		job2.setJobName("mdrill_stage_2@"+jobnameOutput);
		Configuration conf2 = job2.getConfiguration();
		JobIndexPublic.setDistributecache(new Path(solrHome,"solr/conf"), fs,conf2);
		conf2.set("higo.index.fields", fields);
		job2.setJarByClass(JobIndexerPartion.class);
		job2.setInputFormatClass(SequenceFileInputFormat.class);
		SequenceFileInputFormat.addInputPath(job2, new Path(smallindex,"part-r-*"));
		job2.setMapOutputKeyClass(IntWritable.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setPartitionerClass(IntPartion.class) ;
		job2.setReducerClass(IndexReducerMerge.class);
		job2.setOutputKeyClass(IntWritable.class);
		job2.setOutputValueClass(Text.class);
		job2.setOutputFormatClass(SequenceFileOutputFormat.class);
		job2.setNumReduceTasks(shards);
		SequenceFileOutputFormat.setOutputPath(job2, new Path(output));
		int result=0;
		if(update!=null)
		{
			job2.submit();
	        while(!job2.isComplete())
	        {
	        	update.update(2, job2);
	        	Thread.sleep(3000);
	        }
	        
	        update.finish();
		}else{
			result= job2.waitForCompletion(true) ? 0 : -1;

		}
		
		fs.delete(smallindex, true);
		fs.delete(new Path(output,"./_tmpindex"), true);
		
		FileStatus[] list=fs.globStatus(new Path(output,"./part-r-*"));
		if(list!=null)
		{
			for(FileStatus f:list)
			{
				fs.delete(f.getPath(),true);
			}
		}

		return result;
	}
	
	 private static Configuration getConf(Map stormconf)
	    {
		String hadoopConfDir = (String) stormconf.get("hadoop.conf.dir");
		String opts=(String) stormconf.get("hadoop.java.opts");
		Configuration conf=new Configuration();
		conf.set("mapred.child.java.opts", opts);
		HadoopUtil.grabConfiguration(hadoopConfDir, conf);
		return conf;
	    }
}
