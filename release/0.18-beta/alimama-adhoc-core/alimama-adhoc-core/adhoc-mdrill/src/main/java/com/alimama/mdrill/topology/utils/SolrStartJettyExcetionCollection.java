package com.alimama.mdrill.topology.utils;

import org.apache.solr.core.CoreContainer;

public class SolrStartJettyExcetionCollection {
	   private Boolean isException =false;
	    private Exception lasterror=null;
	    
	    private Object errorLock=new Object();
	    

	    public Boolean isException() {
		synchronized (this.getLockObj()) {
		    return isException;
		}
	    }

	    public void setException(Exception e) {
		synchronized (this.getLockObj()) {
		    this.isException = true;
		    this.lasterror = e;
		}
	    }
	    
	    public Object getLockObj()
	    {
		return errorLock;
	    }
	    
	public void checkException() {
		synchronized (this.getLockObj()) {
			if (this.isException()) {
				throw new RuntimeException(this.lasterror);
			}
		}
		
		if(CoreContainer.getIsException())
		{
			throw new RuntimeException("CoreContainer Die");
		}
	}
}
