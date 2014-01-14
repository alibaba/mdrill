package com.taobao.loganalyzer.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputFormat;
import org.apache.hadoop.util.Progressable;

public  abstract class CommonPVProcessor extends AbstractProcessor {
	public abstract static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, LongWritable> {

		private static final String SEPARATOR = "\u0001";

		public String makeKey(List<String> tokens) {
			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (String token : tokens) {
				if (isFirst)
					isFirst = false;
				else
					sb.append(SEPARATOR);
				sb.append(token);
			}
			return sb.toString();
		}

		public abstract void map(LongWritable key, Text value,
				OutputCollector<Text, LongWritable> output, Reporter reporter)
				throws IOException;

	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, LongWritable, Text, Text> {
		LongWritable longVal = new LongWritable();

		public void reduce(Text key, Iterator<LongWritable> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			long sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			longVal.set(sum);
			String newkey = key.toString()+"\u0001"+String.valueOf(sum);
			try {
			output.collect(new Text(newkey), null);
			}catch(Exception e) {
			return;
			}
		}
	}
	public static class Combiner extends MapReduceBase implements
			Reducer<Text, LongWritable, Text, LongWritable> {
		LongWritable longVal = new LongWritable();

		public void reduce(Text key, Iterator<LongWritable> values,
				OutputCollector<Text, LongWritable> output, Reporter reporter)
				throws IOException {
			long sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			longVal.set(sum);
			// String newkey = key.toString() + "\u0001" + String.valueOf(sum);
			output.collect(key, longVal);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static class ReportOutFormat<K extends WritableComparable, V extends Writable>
	extends MultipleOutputFormat<K, V> {

		private TextOutputFormat<K, V> theTextOutputFormat = null;

		@Override
		protected RecordWriter<K, V> getBaseRecordWriter(FileSystem fs,
				JobConf job, String name, Progressable arg3) throws IOException {
			if (theTextOutputFormat == null) {
				theTextOutputFormat = new TextOutputFormat<K, V>();
			}
			return theTextOutputFormat.getRecordWriter(fs, job, name, arg3);
		}

		@Override
		protected String generateFileNameForKeyValue(K key, V value, String name) {
			return name + "_" + key.toString().split("\u0001")[0];
		}
	}
	
	public Class<Reduce> getReducer() {
		return Reduce.class;
	}
	
	public Class<Combiner> getCombiner() {
		return Combiner.class;
	}
}
