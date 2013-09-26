package com.alimama.mdrill.index;
 
import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.lucene.analysis.Analyzer;

import com.alimama.mdrill.index.utils.DocumentConverter;
import com.alimama.mdrill.index.utils.DocumentList;
import com.alimama.mdrill.index.utils.HeartBeater;
import com.alimama.mdrill.index.utils.JobIndexPublic;
import com.alimama.mdrill.index.utils.RamWriter;
import com.alimama.mdrill.index.utils.ShardWriter;


public class IndexReducer extends  Reducer<Text, DocumentList, Text, Text> {
    private HeartBeater heartBeater = null;
    private ShardWriter shardWriter = null;
    private String tmpath = null;
    private String localtmpath = null;
    private String indexHdfsPath = null;

    private Analyzer analyzer;
    private DocumentConverter documentConverter = null;

	protected void setup(Context context) throws java.io.IOException,
			InterruptedException {
		super.setup(context);
		context.getCounter("higo", "dumpcount").increment(0);

		Configuration conf = context.getConfiguration();

		heartBeater = new HeartBeater(context);
		heartBeater.needHeartBeat();

		String fieldStrs = conf.get("higo.index.fields");
		String[] fieldslist = fieldStrs.split(",");
		this.documentConverter = new DocumentConverter(fieldslist,"solrconfig.xml", "schema.xml");
		shardWriter = this.initShardWriter(context);

		try {
			this.analyzer = JobIndexPublic.setAnalyzer(conf);
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

    protected void cleanup(Context context) throws IOException,	InterruptedException {
		try {
			
			shardWriter.optimize();
			shardWriter.close();
			Configuration conf = context.getConfiguration();
			FileSystem fs = FileSystem.get(conf);
			fs.copyFromLocalFile(new Path(localtmpath), new Path(tmpath));

			if (!fs.exists(new Path(indexHdfsPath))) {
				fs.rename(new Path(tmpath), new Path(indexHdfsPath));
			}
			if (shardWriter.getNumDocs() > 0 && lastkey != null) {
				context.write(lastkey,new Text(indexHdfsPath));
			}
			FileSystem lfs = FileSystem.getLocal(conf);
			if(lfs.exists(new Path(localtmpath)))
			{
				lfs.delete(new Path(localtmpath),true);
			}

		} catch (Exception e) {}

		heartBeater.cancelHeartBeat();
		heartBeater.interrupt();
	}

	private ShardWriter initShardWriter(Context context) throws IOException {
		String part_xxxxx = JobIndexPublic.getOutFileName(context, "part");
		Configuration conf = context.getConfiguration();
		FileSystem lfs = FileSystem.getLocal(conf);

		String outputdir = conf.get("mapred.output.dir");
		indexHdfsPath = new Path(outputdir, part_xxxxx).toString();

		tmpath = new Path(outputdir + "/_tmpindex", part_xxxxx + "_" + java.util.UUID.randomUUID().toString()).toString();
		localtmpath = new Path("./_tmpindex", part_xxxxx + "_" + java.util.UUID.randomUUID().toString()).toString();
		
		ShardWriter shardWriter = new ShardWriter(lfs, localtmpath, conf);
		return shardWriter;
	}

    private Text lastkey = null;
    private int debuglines=0;

	protected void reduce(Text key, Iterable<DocumentList> values,
			Context context) throws java.io.IOException, InterruptedException {
		if(key.toString().startsWith("uniq_"))
		{
			int dumps=0;
			Iterator<DocumentList> iterator = values.iterator();
			while (iterator.hasNext()) {
				DocumentList doclist = iterator.next();
				dumps++;
			}
			if(dumps>1)
			{
				context.getCounter("higo", "dumpcount").increment(1);;
				if(debuglines<100)
	    		{
	    			debuglines++;
	        		System.out.println("dumpcount: " + key.toString()   + "");
	    		}
			}
			return ;
		}
		
		lastkey = key;
		RamWriter ramMerger = null;
		Iterator<DocumentList> iterator = values.iterator();
		while (iterator.hasNext()) {
			if(doccount>maxDocCount)
			{
				break ;
			}
			ramMerger = this.maybeCreate(ramMerger);
			DocumentList doclist = iterator.next();
			RamWriter ram = doclist.toRamWriter(documentConverter, analyzer,context);
			ramMerger.process(ram);
			doclist.clean();
			if (this.maybeFlush(ramMerger,context,false)) {
				ramMerger = null;
			}
		}
		if (this.maybeFlush(ramMerger,context,true)) {
			ramMerger = null;
		}
		
	}
    
    private RamWriter maybeCreate(RamWriter form) throws IOException
    {
    	if(form!=null)
    	{
    		return form;
    	}
    	
    	return  new RamWriter();
    }
    
    private long minsize=1024l*1024*32;
    private static long maxDocCount=10000l*1000*10;
    long doccount=0;
	private boolean maybeFlush(RamWriter form,
			Context context,boolean fource)
			throws IOException {
		if (form == null) {
			return false;
		}
		long formSize = form.totalSizeInBytes();
		Integer docs=form.getNumDocs();
		if ((docs>=1000&&formSize>minsize)||fource) {
			context.getCounter("higo", "docCount").increment(docs);;
			doccount+=docs;
			form.closeWriter();
			shardWriter.process(form);
			form.closeDir();
			return true;
		}

		return false;
	}
}
