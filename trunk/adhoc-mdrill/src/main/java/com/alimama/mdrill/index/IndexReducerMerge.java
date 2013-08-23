package com.alimama.mdrill.index;
 
import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;
import com.alimama.mdrill.index.utils.DocumentConverter;
import com.alimama.mdrill.index.utils.HeartBeater;
import com.alimama.mdrill.index.utils.JobIndexPublic;
import com.alimama.mdrill.index.utils.ShardWriter;

public class IndexReducerMerge extends
		Reducer<Text, Text, Text, Text> {
	private HeartBeater heartBeater = null;
	private ShardWriter shardWriter = null;
	private String indexHdfsPath = null;
	private String tmpath = null;
	public DocumentConverter documentConverter;
	protected void setup(Context context) throws java.io.IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();

		String fieldStrs = conf.get("higo.index.fields");
		String[] fieldslist = fieldStrs.split(",");
		this.documentConverter = new DocumentConverter(fieldslist,"solrconfig.xml", "schema.xml");
		heartBeater = new HeartBeater(context);
		heartBeater.needHeartBeat();

		shardWriter = this.initShardWriter(context);
		shardWriter.getDir().setAllowLinks(true);
	}

	protected void cleanup(Context context) throws IOException,
			InterruptedException {
		try {
			shardWriter.addEmptyDoc();
			shardWriter.optimize();
			shardWriter.close();
			Configuration conf = context.getConfiguration();
			FileSystem fs = FileSystem.get(conf);
			if (!fs.exists(new Path(indexHdfsPath))) {
				fs.rename(new Path(tmpath), new Path(indexHdfsPath));
			}

		} catch (Exception e) {
		}

		heartBeater.cancelHeartBeat();
		heartBeater.interrupt();

	}

	private ShardWriter initShardWriter(Context context) throws IOException {
		String part_xxxxx = JobIndexPublic.getOutFileName(context, "part");
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);

		String outputdir = conf.get("mapred.output.dir");
		indexHdfsPath = new Path(outputdir, part_xxxxx).toString();
		tmpath = new Path(outputdir + "/_tmpindex", part_xxxxx + "_"
				+ java.util.UUID.randomUUID().toString()).toString();
		ShardWriter shardWriter = new ShardWriter(fs, tmpath, conf);
		return shardWriter;
	}

	protected void reduce(Text key, Iterable<Text> values,
			Context context) throws java.io.IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);
		Iterator<Text> iterator = values.iterator();
		while (iterator.hasNext()) {
			Text path = iterator.next();
			shardWriter.process(new FileSystemDirectory(fs, new Path(path
					.toString()), false, conf));
			context.write(key, path);
		}

	}
}
