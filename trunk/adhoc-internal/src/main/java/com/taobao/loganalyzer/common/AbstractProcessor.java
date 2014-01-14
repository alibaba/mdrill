/**
 * 
 */
package com.taobao.loganalyzer.common;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputFormat;
import org.apache.hadoop.util.Progressable;

/**
 * @author huangyue.pt
 * 
 */
public abstract class AbstractProcessor implements Processor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.loganalyzer.common.Processor#run(java.lang.String,
	 * java.lang.String, int, int, boolean, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean run(String inputPath, String outputPath, int numMap,
			int numReduce, boolean isInputSequenceFile,
			Map<String, String> properties) throws Exception {
		// TODO Auto-generated method stub
		JobConf conf = new JobConf(this.getClass());
		System.out.println(">>>" + this.getClass().getCanonicalName());

		conf.setJobName(this.getClass().getSimpleName());

		FileInputFormat.setInputPaths(conf, inputPath);
		FileOutputFormat.setOutputPath(conf, new Path(outputPath));
		if (isInputSequenceFile)
			conf.setInputFormat(SequenceFileInputFormat.class);
		else
			conf.setInputFormat(TextInputFormat.class);
		conf.setMapperClass(getMapper());
		conf.setNumMapTasks(numMap);
		conf.setReducerClass(getReducer());
		conf.setNumReduceTasks(numReduce);

		if ("true".equals(conf.get("map.out.compress"))) {
			SequenceFileOutputFormat.setOutputCompressionType(conf,
					SequenceFile.CompressionType.BLOCK);
			SequenceFileOutputFormat.setOutputCompressorClass(conf,
					GzipCodec.class);
		}
		if (conf.get("mapred.max.track.failures") != null) {
			conf.setMaxTaskFailuresPerTracker(Integer.valueOf(conf
					.get("mapred.max.track.failures")));
		}

		for (String propertyKey : properties.keySet())
			conf.set(propertyKey, properties.get(propertyKey));

		configJob(conf);

		JobClient c = new JobClient(conf);
		RunningJob job = c.runJob(conf);
		return job.isSuccessful();
	}

	protected abstract void configJob(JobConf conf);

	public static class ReportOutFormat<K extends WritableComparable<?>, V extends Writable>
			extends MultipleOutputFormat<K, V> {

		private TextOutputFormat<K, V> theTextOutputFormat = null;

		protected RecordWriter<K, V> getBaseRecordWriter(FileSystem fs,
				JobConf job, String name, Progressable arg3) throws IOException {
			if (theTextOutputFormat == null) {
				theTextOutputFormat = new TextOutputFormat<K, V>();
			}
			return theTextOutputFormat.getRecordWriter(fs, job, name, arg3);
		}

		protected String generateFileNameForKeyValue(K key, V value, String name) {
			String tag = "";
			if (key.toString().split("\u0001").length > 0) {
				tag = key.toString().split("\u0001")[0];
			}
			for (int counter = 0; counter < tag.length(); counter++) {
				char c = tag.charAt(counter);
				if (!(('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')
						|| ('0' <= c && c <= '9') || c == '_'))
					break;
			}
			return name + "_" + tag;
		}
	}

}
