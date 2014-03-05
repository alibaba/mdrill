package com.alimama.mdrillImport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import com.alimama.mdrill.ui.service.MdrillService;


import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public class ImportSpoutLocalForParseTest{
	private static Logger LOG = Logger.getLogger(ImportSpoutLocalForParseTest.class);

    private static final long serialVersionUID = 1L;
    private ImportReader reader=null;
    private DataParser parse=null;
    private String confPrefix;

    public ImportSpoutLocalForParseTest(String confPrefix)
    {
    	this.confPrefix=confPrefix;
    }


    public void open(Map conf, TopologyContext context) {
    	try {
			parse=(DataParser) Class.forName(String.valueOf(conf.get(this.confPrefix+"-parse"))).newInstance();
			parse.init(true, conf, context);
		} catch (Throwable e1) {
			LOG.error(this.confPrefix+ " DataParser",e1);
		}


    	
    	
    	try {
			this.reader=new ImportReader(conf, confPrefix, parse, 0, 1);
		} catch (IOException e) {
			LOG.error("TTReader",e);
		}
    }
    


    
	private boolean putdata(DataParser.DataIter log)
	{	
    	long ts=log.getTs();
		BoltStatKey key=new BoltStatKey(log.getGroup());
		BoltStatVal val=new BoltStatVal(log.getSum(),ts);
		
		
		SolrInputDocument doc=new SolrInputDocument();

		String[] groupnames=parse.getGroupName();
		for(int i=0;i<groupnames.length&&i<key.list.length;i++)
		{
		    doc.addField(groupnames[i], key.list[i]);
		}
		
		String[] statNames=parse.getSumName();

		for(int i=0;i<statNames.length&&i<val.list.length;i++)
		{
		    doc.addField(statNames[i], val.list[i]);
		}
		
		System.out.println(doc.toString());
	    return true;
	}
	
	
	   
	    public synchronized void nextTuple()  {
			try {
				List ttdata = this.reader.read();
				if(ttdata==null)
				{
					return ;
				}
				for(Object o:ttdata)
				{
					DataParser.DataIter pv=(DataParser.DataIter)o;
					while(true)
					{
						this.putdata(pv);
						if(!pv.next())
						{
							break;
						}
					}
				}
				
			} catch (Throwable e) {
				this.sleep(100);
			}
	    }
	    
	    private void sleep(int i)
	    {
	    	try {
				Thread.sleep(i);
			} catch (InterruptedException e1) {
			}
	    }

	  


 

}
