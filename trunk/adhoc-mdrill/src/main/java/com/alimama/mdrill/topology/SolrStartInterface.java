package com.alimama.mdrill.topology;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public interface SolrStartInterface {

    public void setExecute(ThreadPoolExecutor EXECUTE) ;

    public void setRealTime(boolean isRealTime) ;

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
