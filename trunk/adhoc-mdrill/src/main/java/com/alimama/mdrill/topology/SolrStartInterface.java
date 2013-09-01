package com.alimama.mdrill.topology;

public interface SolrStartInterface {

    public void setRealTime(boolean isRealTime) ;

    public void setMergeServer(boolean isMergeServer) ;
    
    public void setConfigDir(String dir);
        
      
    public void start() throws Exception ;

    
    
    public void stop() throws Exception ;
    

    public Boolean isTimeout();
    public void heartbeat() throws Exception;
    
  
    
    public void unregister();
    
    

    public boolean isStop() ;
}
