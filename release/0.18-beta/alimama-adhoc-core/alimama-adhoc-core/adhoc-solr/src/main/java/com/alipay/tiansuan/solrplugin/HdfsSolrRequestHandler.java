package com.alipay.tiansuan.solrplugin;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.handler.ContentStreamHandlerBase;
import org.apache.solr.handler.ContentStreamLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.utils.Utils;

import com.alimama.mdrill.utils.HadoopUtil;


/**
 * 
 * curl http://hdphadoop:8983/solr/update/userimport?istmp=true&hdfsfile=hdfs://hdphadoop.com:9000/data/tiansuan/fileintest --data-binary @/data/testdata/filein -H 'Content-type:text/plain; charset=utf-8'
 * 
 * @author yannian
 * @version $Id: HdfsSolrRequestHandler.java, v 0.1 2012-7-30 下午12:33:01 root Exp $
 */
public class HdfsSolrRequestHandler extends ContentStreamHandlerBase {
	
	private static  Object lock = new Object();
	private static long lasttime = 0l;
    public static Logger log = LoggerFactory.getLogger(HdfsSolrRequestHandler.class);

    @Override
    public String getDescription() {
	return "write userid to  hdfs";
    }

    @Override
    public String getSource() {
	return "$URL: nothing $";
    }

    @Override
    public String getSourceId() {
	return this.getVersion();
    }

    @Override
    public String getVersion() {
	return "version 20120730";
    }


    @Override
    protected ContentStreamLoader newLoader(SolrQueryRequest arg0,
	    UpdateRequestProcessor arg1) {
	return new loader(arg0, arg1);
    }

    public class loader extends ContentStreamLoader {
	SolrQueryRequest req;
	UpdateRequestProcessor process;
	String file = "";
	String filepath = "";
	boolean istmp = false;

	public loader(SolrQueryRequest arg0, UpdateRequestProcessor arg1) {
	    this.req = arg0;
	    this.process = arg1;
	    Path base=new Path(this.req.getParams().get("hdfsdir"));
	    String filename=this.req.getParams().get("hdfsfile");
	    this.file = new Path(new Path(base,"safedir"),filename).toString();
	    this.istmp = this.req.getParams().getBool("istmp", true);

	}
	
	

		public String cleandir(Path path, Configuration conf)
				throws IOException {
			FileSystem fs = FileSystem.get(conf);

			fs.mkdirs(path);
			Date now = new Date();
			synchronized (lock) {
				if (now.getTime() - 1000 * 60 < lasttime) {
					return "skip";
				}
				lasttime = now.getTime();

			}

			StringBuffer buff = new StringBuffer();

			FileStatus[] fsArrays = fs.listStatus(path);
			int index = 0;
			int savetime = 1000 * 600;
			for (FileStatus f : fsArrays) {
				index++;

				String filename = f.getPath().getName();
				long acctime = f.getAccessTime();
				long modtime = f.getModificationTime();
				if (index <= 10) {
					buff.append(">>" + filename + "@" + acctime + "@" + modtime
							+ "@" + now.getTime());
				}
				if (filename.indexOf("grep") >= 0
						&& (acctime + savetime) < now.getTime()
						&& (modtime + savetime) < now.getTime()) {
					if (index <= 10) {
						buff.append("@del");
					}

					fs.delete(f.getPath(), true);
				} else {
					if (index <= 10) {
						buff.append("@save");
					}

				}
			}

			return buff.toString();
		}

		public void makePath(Path path, Configuration conf) throws IOException {
			if(this.istmp)
			{
				cleandir(path.getParent(), conf);
			}
			FileSystem fs = FileSystem.get(conf);
			fs.mkdirs(path.getParent());
		}

	@Override
	public void load(SolrQueryRequest rq, SolrQueryResponse pq,
				ContentStream stream) throws Exception {
			Configuration conf = HadoopUtil.getConf(Utils.readStormConfig());
			Path filepath = new Path(this.file);
			FileSystem fs = filepath.getFileSystem(conf);

			this.makePath(filepath, conf);

			Path randompath = new Path(this.file + "_" + java.util.UUID.randomUUID().toString());
			try {
				FSDataOutputStream writer = fs.create(randompath);

				Reader reader = stream.getReader();
				if (!(reader instanceof BufferedReader)) {
					reader = new BufferedReader(reader);
				}
				BufferedReader br = (BufferedReader) reader;
				String s1 = null;
				while ((s1 = br.readLine()) != null) {
					writer.write(new String(s1.trim().replaceAll("(\r)|(\n)","")+ "\n").getBytes());
				}
				br.close();

				writer.close();
				reader.close();
				fs.delete(filepath, true);
				fs.rename(randompath, filepath);
			} catch (Exception e) {
				fs.delete(randompath, true);
			}
		}
    }

}
