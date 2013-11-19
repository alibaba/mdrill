package com.alimama.mdrill.adhoc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;




public class TimeCacheMap<K, V> implements Serializable{
	private static final long serialVersionUID = 1L;
    private static final int DEFAULT_NUM_BUCKETS = 3;

    public static interface ExpiredCallback<K, V> {
        public void expire(K key, V val);
        public void commit();
    }

    private LinkedList<HashMap<K, V>> _buckets;

    private final Object _lock = new Object();
    private Thread _cleaner;
    private ExpiredCallback _callback;
    
    private int numBuckets;
    public TimeCacheMap(int expirationSecs, int numBuckets, ExpiredCallback<K, V> callback) {
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
        _cleaner = new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        Map<K, V> dead = null;
                        Thread.currentThread().sleep(sleepTime);
                        synchronized(_lock) {
                            dead = _buckets.removeLast();
                            _buckets.addFirst(new HashMap<K, V>());
                        }
                        if(_callback!=null) {
                            for(Entry<K, V> entry: dead.entrySet()) {
                                _callback.expire(entry.getKey(), entry.getValue());
                            }
                            _callback.commit();
                        }
                    }
                } catch (InterruptedException ex) {

                }
            }
        });
        _cleaner.setDaemon(true);
        _cleaner.start();
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
        	 for(HashMap<K, V> dead:lastdata)
        	 {
	             for(Entry<K, V> entry: dead.entrySet()) {
	                 _callback.expire(entry.getKey(), entry.getValue());
	             }
        	 }
        	 
        	 _callback.commit();
         }
    }

    public TimeCacheMap(int expirationSecs, ExpiredCallback<K, V> callback) {
        this(expirationSecs, DEFAULT_NUM_BUCKETS, callback);
    }

    public TimeCacheMap(int expirationSecs) {
        this(expirationSecs, DEFAULT_NUM_BUCKETS);
    }

    public TimeCacheMap(int expirationSecs, int numBuckets) {
        this(expirationSecs, numBuckets, null);
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
            _cleaner.interrupt();
        } finally {
            super.finalize();
        }
    }

    
}

