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

public class ImportSpoutLocal implements IRichSpout{
	private static Logger LOG = Logger.getLogger(ImportSpoutLocal.class);

    private static final long serialVersionUID = 1L;
    private ImportReader reader=null;
    private DataParser parse=null;
    private String confPrefix;

    public ImportSpoutLocal(String confPrefix)
    {
    	this.confPrefix=confPrefix;
    }
	private ArrayList<SolrInputDocument> doclist=null;
	private Object doclistLock=new Object();


    int buffersize=5000;
    @Override
    public void open(Map conf, TopologyContext context,SpoutOutputCollector collector) {
    	try {
			parse=(DataParser) Class.forName(String.valueOf(conf.get(this.confPrefix+"-parse"))).newInstance();
			parse.init(true, conf, context);
		} catch (Throwable e1) {
			LOG.error(this.confPrefix+ " DataParser",e1);
		}

		int timeout=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-timeoutSpout")));
		this.buffersize=Integer.parseInt(String.valueOf(conf.get(confPrefix+"-spoutbuffer")));

		Object chkIntervel=conf.get(confPrefix+"-spoutIntervel");
		if(chkIntervel!=null)
		{
			this.checkIntervel=Integer.parseInt(String.valueOf(chkIntervel));
		}
		
    	this.timeoutCheck=new TimeOutCheck(timeout*1000l);
    	this.status=new SpoutStatus();
    	
    	
	 	doclist=new ArrayList<SolrInputDocument>(this.buffersize);

	 	int readerCount=context.getComponentTasks(context.getThisComponentId()).size();
    	int readerIndex=context.getThisTaskIndex();
    	try {
			this.reader=new ImportReader(conf, confPrefix, parse, readerIndex, readerCount);
		} catch (IOException e) {
			LOG.error("TTReader",e);
		}
    }
    

	
	private SpoutStatus status=null;
	private TimeOutCheck timeoutCheck=null;
	

    int checkIntervel=1000;


    
    
	private boolean putdata(DataParser.DataIter log)
	{	
    	long ts=log.getTs();
		BoltStatKey key=new BoltStatKey(log.getGroup());
		BoltStatVal val=new BoltStatVal(log.getSum(),ts);
		
		this.status.ttInput++;
		this.status.groupCreate++;
		
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
		
		synchronized (doclistLock) {
			doclist.add(doc);
		}

    	if((this.status.ttInput%checkIntervel!=0))
    	{
    		return false;
    	}
    	

    	this.commit();
	    
	    return true;
	}
	
	
	public synchronized void commit() {
		
		synchronized (doclistLock) {
	    	if(!this.timeoutCheck.istimeout()&&doclist.size()<buffersize)
			{
	    		return ;
			}
		}
    	
    	this.timeoutCheck.reset();
		
		try{
		ArrayList<SolrInputDocument> buffer=null;
		synchronized (doclistLock) {
	 		buffer=doclist;
		 	doclist=new ArrayList<SolrInputDocument>(300);
		}

    	if(buffer!=null&&buffer.size()>0)
    	{
	    	for(int i=0;i<100;i++)
	    	{
	    		try {
					LOG.info(this.confPrefix+" mdrill request:"+i+"@"+buffer.size());
					MdrillService.insertLocal(this.parse.getTableName(), buffer,null);
					break ;
				} catch (Throwable e) {
					LOG.error(this.confPrefix+" insert", e);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
	    	}
    	}
		}catch(Throwable e)
		{
			LOG.info("commit ",e);
		}
	}
	
	
	   @Override
	    public synchronized void nextTuple()  {
			try {
				List ttdata = this.reader.read();
				if(ttdata==null)
				{
					return ;
				}
				for(Object o:ttdata)
				{
					this.status.ttInput++;
					DataParser.DataIter pv=(DataParser.DataIter)o;
					while(true)
					{
						this.status.groupInput++;
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

	    @Override
	    public void close() {
		
	    }
	        @Override
	    public void ack(Object msgId) {
	        	this.status.ackCnt++;
	    }

	    @Override
	    public void fail(Object msgId) {
	    	this.status.failCnt++;
	    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("key","value"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> rtnmap = new HashMap<String, Object>();
        return rtnmap;
    }

}
