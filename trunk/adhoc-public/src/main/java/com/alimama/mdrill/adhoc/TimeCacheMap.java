package com.alimama.mdrill.adhoc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.apache.log4j.Logger;





public class TimeCacheMap<K, V> implements Serializable{
	private static final long serialVersionUID = 1L;
    private static final int DEFAULT_NUM_BUCKETS = 3;
	private static Logger LOG = Logger.getLogger(TimeCacheMap.class);

    public static interface ExpiredCallback<K, V> {
        public void expire(K key, V val);
        public void commit();
    }

    private LinkedList<HashMap<K, V>> _buckets;

    private final Object _lock = new Object();
    private Thread _cleaner=null;
    TimerTask task=null;
    Timer timer=null;
    
    
    private ExpiredCallback<K, V> _callback;
    
    
    private int numBuckets;
	private long lasttime=System.currentTimeMillis();
	private long localMergerDelay=20*1000l;
	
	public static class CleanExecute<K, V>
	{
	    public void executeClean(TimeCacheMap<K, V> t){
	    	t.clean();
	    }
	}
	
	public static class CleanExecuteWithLock<K, V> extends CleanExecute<K, V>
	{
		Object lock;
	    public CleanExecuteWithLock(Object lock) {
			this.lock = lock;
		}
		public void executeClean(TimeCacheMap<K, V> t){
			synchronized (this.lock) {
				super.executeClean(t);
			}
	    }
	}
	

	
	CleanExecute<K, V> _cleanlock=new CleanExecute<K, V>();

    
    public TimeCacheMap(Timer timer,int expirationSecs, int numBuckets, ExpiredCallback<K, V> callback,CleanExecute<K, V> cleanlock) {
    	if(cleanlock!=null)
    	{
    		this._cleanlock=cleanlock;
    	}
    	
    	this.numBuckets=numBuckets;
        if(numBuckets<2) {
            throw new IllegalArgumentException("numBuckets must be >= 2");
        }
        _buckets = new LinkedList<HashMap<K, V>>();
        for(int i=0; i<numBuckets; i++) {
            _buckets.add(new HashMap<K, V>());
        }

        _callback = callback;
        
        final long expirationMillis = expirationSecs * 1000L;
        final long sleepTime = expirationMillis / (numBuckets-1);
        this.localMergerDelay=sleepTime-100;
        if(timer==null)
        {
        _cleaner = new Thread(new Runnable() {
            public void run() {
					while (true) {
						LOG.info("_cleaner start");
						try {
							Thread.currentThread().sleep(sleepTime);
							TimeCacheMap.this.maybeClean();
						} catch (Throwable ex) {
							LOG.error("_cleaner", ex);
						}
					}

            }
        });
        _cleaner.setDaemon(true);
        _cleaner.start();
        
        }else{
        	task=new TimerTask() {
				@Override
				public void run() {
					try {
						TimeCacheMap.this.maybeClean();
					} catch (Throwable ex) {
						LOG.error("_cleaner", ex);
					}
				}
			};
        	timer.schedule(task, sleepTime,sleepTime);
        	this.timer=timer;

        }
    }
    
    
     
    private  void timerReset()
	{
		lasttime=System.currentTimeMillis();
	}
    
	private boolean isTimeout()
	{
		long time=System.currentTimeMillis();
    	if((lasttime+localMergerDelay)<=time)
		{
			return true ;
		}
    	
    	return false;
	}
    
    public void maybeClean()
    {
        synchronized(_lock) {
	    	if(!this.isTimeout())
	    	{
	    		return ;
	    	}
	    	this.timerReset();
        }
		this._cleanlock.executeClean(this);
    }
    
    private void clean()
    {
    	try {
            Map<K, V> dead = null;
            synchronized(_lock) {
                dead = _buckets.removeLast();
                _buckets.addFirst(new HashMap<K, V>());
            
            }
            if(_callback!=null) {
             	synchronized(_callback) {
	                for(Entry<K, V> entry: dead.entrySet()) {
	                    _callback.expire(entry.getKey(), entry.getValue());
	                }
	                _callback.commit();
             	}
            }
            
           
        
        } catch (Throwable ex) {
				LOG.error("_cleaner maybeClean", ex);
        }	
    }
    
    
    public void fourceClean()
    {
        synchronized(_lock) {
	    	this.timerReset();
        }
        this.clean();					
	
    }
    
    public void fourceTimeout(Timeout<K, V> fetch,Update<K, V> d)
    {
    	LinkedList<HashMap<K, V>> lastdata=null;
    	synchronized(_lock) {
    		lastdata=_buckets;
    		_buckets = new LinkedList<HashMap<K, V>>();
    		 for(int i=0; i<numBuckets; i++) {
    	            _buckets.add(new HashMap<K, V>());
    	     }
    	}

         if(_callback!=null&&lastdata!=null) {
          	synchronized(_callback) {

	        	 HashMap<K, V> needupdate=new HashMap<K, V>();
	        	 for(HashMap<K, V> dead:lastdata)
	        	 {
		             for(Entry<K, V> entry: dead.entrySet()) {
		            	 K key=entry.getKey();
		            	 V val=entry.getValue();
		            	 if(fetch.timeout(key,val))
		            	 {
		            		 _callback.expire(key, val);
		            	 }else{
		            		 needupdate.put(key, val);
		            	 }
		             }
	        	 }
	        	 
	        	 _callback.commit();
	        	 
	
	        	 if(needupdate.size()>0)
	        	 {
	        		 this.updateAll(needupdate, d);
	        	 }
          	}
        	 
         }
    }
    
    public void fourceTimeout()
    {
    	LinkedList<HashMap<K, V>> lastdata=null;
    	synchronized(_lock) {
    		lastdata=_buckets;
    		_buckets = new LinkedList<HashMap<K, V>>();
    		 for(int i=0; i<numBuckets; i++) {
    	            _buckets.add(new HashMap<K, V>());
    	        }
    	}

         if(_callback!=null&&lastdata!=null) {
         	synchronized(_callback) {

        	 for(HashMap<K, V> dead:lastdata)
        	 {
	             for(Entry<K, V> entry: dead.entrySet()) {
	                 _callback.expire(entry.getKey(), entry.getValue());
	             }
        	 }
        	 
        	 _callback.commit();
         	}
         }
    }

    public TimeCacheMap(int expirationSecs, ExpiredCallback<K, V> callback) {
        this(null,expirationSecs, DEFAULT_NUM_BUCKETS, callback,null);
    }
    
    public TimeCacheMap( Timer timer,int expirationSecs, ExpiredCallback<K, V> callback) {
        this(timer,expirationSecs, DEFAULT_NUM_BUCKETS, callback,null);
    }
    
    public TimeCacheMap( Timer timer,int expirationSecs, ExpiredCallback<K, V> callback,CleanExecute<K, V> cleanlock) {
        this(timer,expirationSecs, DEFAULT_NUM_BUCKETS, callback,null);
    }
 
    

    public TimeCacheMap(int expirationSecs) {
        this(expirationSecs, DEFAULT_NUM_BUCKETS);
    }

    public TimeCacheMap(int expirationSecs, int numBuckets) {
        this(null,expirationSecs, numBuckets, null,null);
    }


    public boolean containsKey(K key) {
        synchronized(_lock) {
            for(HashMap<K, V> bucket: _buckets) {
                if(bucket.containsKey(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    public V get(K key) {
        synchronized(_lock) {
            return this.getNolock(key);
        }
    }
    
    
    private V getNolock(K key)
    {
    	 for(HashMap<K, V> bucket: _buckets) {
             if(bucket.containsKey(key)) {
                 return bucket.get(key);
             }
         }
         return null;
    }
    
    private void putNolock(K key, V value) {
            Iterator<HashMap<K, V>> it = _buckets.iterator();
            HashMap<K, V> bucket = it.next();
            bucket.put(key, value);
            while(it.hasNext()) {
                bucket = it.next();
                bucket.remove(key);
            }
    }

    public void put(K key, V value) {
        synchronized(_lock) {
            this.putNolock(key, value);
        }
    }
    
    public static interface Update<K, V>{
    	public V update(K key,V old,V newval);
    }
    
    public static interface Timeout<K, V>{
    	public boolean timeout(K key,V val);
    }
    
    public void updateAll(Map<K, V> bucket,Update<K, V> d)
    {
    	synchronized(_lock) {
    		for(Entry<K, V> e:bucket.entrySet())
    		{
    			K key=e.getKey();
    			V old=this.getNolock(key);
    			V newval=e.getValue();
    			V finalVal=d.update(key, old, newval);
    			this.putNolock(key, finalVal);
    		}
    	}
    }
    
    public void update(K key, V newval,Update<K, V> d)
    {
    	synchronized(_lock) {
    		V old=this.getNolock(key);
			V finalVal=d.update(key, old, newval);
			this.putNolock(key, finalVal);
    	}
    }
    
    
    public V remove(K key) {
        synchronized(_lock) {
            for(HashMap<K, V> bucket: _buckets) {
                if(bucket.containsKey(key)) {
                    return bucket.remove(key);
                }
            }
            return null;
        }
    }

    public int size() {
        synchronized(_lock) {
            int size = 0;
            for(HashMap<K, V> bucket: _buckets) {
                size+=bucket.size();
            }
            return size;
        }
    }
    @Override
    protected void finalize() throws Throwable {
        try {
        	if(_cleaner!=null)
        	{
        		_cleaner.interrupt();
        	}
        	
        	if(this.timer!=null)
        	{
        		this.task.cancel();
        		this.timer.purge();
        	}
        } finally {
            super.finalize();
        }
    }

    
}

