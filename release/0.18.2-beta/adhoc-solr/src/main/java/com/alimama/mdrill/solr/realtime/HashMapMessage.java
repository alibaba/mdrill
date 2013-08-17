package com.alimama.mdrill.solr.realtime;

import java.util.HashMap;
import backtype.storm.utils.Utils;

public class HashMapMessage implements IMessage{

	    private  HashMap<String,String> p=new  HashMap<String,String>();

	    public HashMap<String,String> getMap() {
	        return p;
	    }
	    
	    public void setMap(HashMap<String,String> o)
	    {
	        this.p=o;
	    }
	    

	    @Override
	    public void setData(byte[] data) {
	       this.p=(HashMap<String,String>) Utils.deserialize(data);
	    }


	    @Override
	    public byte[] getData() {
	        return Utils.serialize(this.p);
	    }

}
