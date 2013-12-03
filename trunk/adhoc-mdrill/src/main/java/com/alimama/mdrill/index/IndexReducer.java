package com.alimama.mdrill.index;
 
import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.TermInfosWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.index.utils.DocumentConverter;
import com.alimama.mdrill.index.utils.DocumentList;
import com.alimama.mdrill.index.utils.DocumentMap;
import com.alimama.mdrill.index.utils.HeartBeater;
import com.alimama.mdrill.index.utils.JobIndexPublic;
import com.alimama.mdrill.index.utils.PairWriteable;
import com.alimama.mdrill.index.utils.RamWriter;
import com.alimama.mdrill.index.utils.ShardWriter;


public class IndexReducer extends  Reducer<PairWriteable, DocumentMap, IntWritable, Text> {
	  public static Logger LOG = LoggerFactory.getLogger(IndexReducer.class);

    private HeartBeater heartBeater = null;
    private ShardWriter shardWriter = null;
    private String tmpath = null;
    private String localtmpath = null;
    private String indexHdfsPath = null;

    private Analyzer analyzer;
    private DocumentConverter documentConverter = null;
    DocumentList doclistcache=new DocumentList();
    RamWriter ramMerger=null;
	boolean isNotFdtMode=true;

    private String[] fields = null;

	protected void setup(Context context) throws java.io.IOException,
			InterruptedException {
		super.setup(context);

		context.getCounter("higo", "dumpcount").increment(0);

		Configuration conf = context.getConfiguration();
		isNotFdtMode=conf.get("mdrill.table.mode","").indexOf("@hdfs@")<0;
		
		String fieldStrs = context.getConfiguration().get("higo.index.fields");

		String custfields=context.getConfiguration().get("higo.column.custfields","");
		
		if(custfields==null||custfields.isEmpty())
		{
			String[] fieldslist = fieldStrs.split(",");
			this.fields = new String[fieldslist.length];
		
			for (int i = 0; i < fieldslist.length; i++) {
			    String[] fieldSchema = fieldslist[i].split(":");
			    String fieldName = fieldSchema[0].trim().toLowerCase();
			    this.fields[i] = fieldName;
			}
		}else{
			String[] fieldslist = custfields.split(",");
			this.fields = new String[fieldslist.length];
		
			for (int i = 0; i < fieldslist.length; i++) {
			    this.fields[i] = fieldslist[i];
			}
		}

		
		if(!isNotFdtMode)
		{
			TermInfosWriter.setSkipInterVal(16);
		}

		heartBeater = new HeartBeater(context);
		heartBeater.needHeartBeat();
		this.doclistcache=new DocumentList();
		this.ramMerger = new RamWriter();

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
			RamWriter ram = doclistcache.toRamWriter(documentConverter, analyzer,context);
			ramMerger.process(ram);
			if (this.maybeFlush(ramMerger,context,true)) {
				ramMerger = null;
			}
			doclistcache=new DocumentList();
			shardWriter.optimize();
			shardWriter.close();
			Configuration conf = context.getConfiguration();
			FileSystem fs = FileSystem.get(conf);
			fs.copyFromLocalFile(new Path(localtmpath), new Path(tmpath));

			if (!fs.exists(new Path(indexHdfsPath))) {
				fs.rename(new Path(tmpath), new Path(indexHdfsPath));
			}
			if (shardWriter.getNumDocs() > 0 && lastkey != null) {
				TaskID taskId = context.getTaskAttemptID().getTaskID();
				int partition = taskId.getId();
				System.out.println("###########>>>>"+partition);
				context.write(new IntWritable(partition),new Text(indexHdfsPath));
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

    private IntWritable lastkey = null;
    private int debuglines=0;

	protected void reduce(PairWriteable key, Iterable<DocumentMap> values,
			Context context) throws java.io.IOException, InterruptedException {
		if(!key.isNum())
		{
			int dumps=0;
			Iterator<DocumentMap> iterator = values.iterator();
			while (iterator.hasNext()) {
				DocumentMap doclist = iterator.next();
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
		
		lastkey = new IntWritable(key.getIndex());
		
		
		Iterator<DocumentMap> iterator = values.iterator();
		while (iterator.hasNext()) {
			if(doccount>maxDocCount)
			{
				break ;
			}
			doclistcache.add(iterator.next(),this.fields);
			if(!doclistcache.isoversize())
			{
				continue;
			}
			RamWriter ram = doclistcache.toRamWriter(documentConverter, analyzer,context);
			doclistcache=new DocumentList();

			
			ramMerger.process(ram);
			if (this.maybeFlush(ramMerger,context,false)) {
				ramMerger =  new RamWriter();;
			}
		}
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
		if(docs<=0)
		{
			return false;
		}
		if ((docs>=1000&&formSize>minsize)||fource) {
			try{
			context.getCounter("higo", "docCount").increment(docs);;
			doccount+=docs;
			form.closeWriter();
			shardWriter.process(form);
			form.closeDir();
			}catch(Throwable e)
			{
				LOG.error("maybeFlush error",e);
				throw new IOException(e);
			}
			return true;
		}

		return false;
	}
}
