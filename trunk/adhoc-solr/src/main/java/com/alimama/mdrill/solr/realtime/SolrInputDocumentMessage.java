package com.alimama.mdrill.solr.realtime;


import org.apache.solr.common.SolrInputDocument;

import backtype.storm.utils.Utils;

public class SolrInputDocumentMessage implements IMessage{

	    private  SolrInputDocument p=new  SolrInputDocument();

	    public SolrInputDocument getMap() {
	        return p;
	    }
	    
	    public void setMap(SolrInputDocument o)
	    {
	        this.p=o;
	    }
	    

	    @Override
	    public void setData(byte[] data) {
	       this.p=(SolrInputDocument) Utils.deserialize(data);
	    }


	    @Override
	    public byte[] getData() {
	        return Utils.serialize(this.p);
	    }

}
