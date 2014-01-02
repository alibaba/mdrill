package com.alimama.mdrill.topology;

import java.util.Map;

public interface SolrStartInterface {

    public void setExecute(ShardThread EXECUTE) ;

    public void setMergeServer(boolean isMergeServer) ;
    
    public void setConfigDir(String dir);
    
    public void setConf(Map stormConf);
        
      
    public void start() throws Exception ;

    
    
    public void stop() throws Exception ;
    

    public Boolean isTimeout();
    public void heartbeat() throws Exception;
    
  
    
    public void unregister();
    
    public void checkError();

    public boolean isStop() ;
}
