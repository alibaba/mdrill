/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.core;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.SimpleMapCache;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.CommonParams.EchoParamStyle;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrResourceLoader.PartionKey;
import org.apache.solr.handler.component.*;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.*;
import org.apache.solr.request.mdrill.FacetComponent;
import org.apache.solr.response.*;
import org.apache.solr.response.BinaryResponseWriter;
import org.apache.solr.response.JSONResponseWriter;

import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.RawResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SolrFieldCacheMBean;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.ValueSourceParser;
import org.apache.solr.update.DirectUpdateHandler2;
import org.apache.solr.update.UpdateHandler;
import org.apache.solr.update.processor.LogUpdateProcessorFactory;
import org.apache.solr.update.processor.RunUpdateProcessorFactory;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.RefCounted;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
import org.apache.solr.util.plugin.PluginInfoInitialized;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.solr.realtime.MdrillDirectory;
import com.alimama.mdrill.solr.realtime.ReadOnlyDirectory;
import com.alimama.mdrill.solr.realtime.RealTimeDirectory;
import com.alimama.mdrill.solr.realtime.ShardPartion;
import com.alimama.mdrill.utils.HadoopUtil;
import com.esotericsoftware.minlog.Log;

import java.net.URL;
import java.lang.reflect.Constructor;


/**
 * @version $Id: SolrCore.java 1190108 2011-10-28 01:13:25Z yonik $
 */
public final class SolrCore implements SolrInfoMBean {
  public static final String version="1.0";  

  public static Logger log = LoggerFactory.getLogger(SolrCore.class);

  private String name;
  private String logid; // used to show what name is set
  private final CoreDescriptor coreDescriptor;

  private final SolrConfig solrConfig;
  private final SolrResourceLoader resourceLoader;
  private final IndexSchema schema;
  private final String dataDir;
  private final UpdateHandler updateHandler;
  private final long startTime;
  private final RequestHandlers reqHandlers;
  private final Map<String,SearchComponent> searchComponents;
  private final Map<String,UpdateRequestProcessorChain> updateProcessorChains;
  private final Map<String, SolrInfoMBean> infoRegistry;
  private IndexDeletionPolicyWrapper solrDelPolicy;

  public long getStartTime() { return startTime; }

  static int boolean_query_max_clause_count = Integer.MIN_VALUE;
  // only change the BooleanQuery maxClauseCount once for ALL cores...
  void booleanQueryMaxClauseCount()  {
    synchronized(SolrCore.class) {
      if (boolean_query_max_clause_count == Integer.MIN_VALUE) {
        boolean_query_max_clause_count = solrConfig.booleanQueryMaxClauseCount;
        BooleanQuery.setMaxClauseCount(boolean_query_max_clause_count);
      } else if (boolean_query_max_clause_count != solrConfig.booleanQueryMaxClauseCount ) {
        log.debug("BooleanQuery.maxClauseCount= " +boolean_query_max_clause_count+ ", ignoring " +solrConfig.booleanQueryMaxClauseCount);
      }
    }
  }

  

  public SolrResourceLoader getResourceLoader() {
    return resourceLoader;
  }

 
  public String getConfigResource() {
    return solrConfig.getResourceName();
  }
  
  @Deprecated
  public String getConfigFile() {
    return solrConfig.getResourceName();
  }


  public SolrConfig getSolrConfig() {
    return solrConfig;
  }
  

  public String getSchemaResource() {
    return schema.getResourceName();
  }

  @Deprecated
  public String getSchemaFile() {
    return schema.getResourceName();
  }
  

  public IndexSchema getSchema() { 
    return schema;
  }
  
  public String getDataDir() {
    return dataDir;
  }
  
  public String getIndexDir(String partion) {
    synchronized (searcherLock) {
        return this.getDataDir()+"/"+partion;

    }
  }
  
  public String getRealTimeDir(String partion) {
	    synchronized (searcherLock) {
	        return this.getIndexDir(partion)+"/realtime";

	    }
	  }

  

  
  public String getName() {
    return name;
  }

  public void setName(String v) {
    this.name = v;
    this.logid = (v==null)?"":("["+v+"] ");
  }
  


  public Map<String, SolrInfoMBean> getInfoRegistry() {
    return infoRegistry;
  }

   private void initDeletionPolicy() {
     PluginInfo info = solrConfig.getPluginInfo(IndexDeletionPolicy.class.getName());
     IndexDeletionPolicy delPolicy = null;
     if(info != null){
       delPolicy = createInstance(info.className,IndexDeletionPolicy.class,"Deletion Policy for SOLR");
       if (delPolicy instanceof NamedListInitializedPlugin) {
         ((NamedListInitializedPlugin) delPolicy).init(info.initArgs);
       }
     } else {
       delPolicy = new SolrDeletionPolicy();
     }     
     solrDelPolicy = new IndexDeletionPolicyWrapper(delPolicy);
   }

  private void initListeners() {
    final Class<SolrEventListener> clazz = SolrEventListener.class;
    final String label = "Event Listener";
    for (PluginInfo info : solrConfig.getPluginInfos(SolrEventListener.class.getName())) {
      String event = info.attributes.get("event");
      if("firstSearcher".equals(event) ){
        SolrEventListener obj = createInitInstance(info,clazz,label,null);
        firstSearcherListeners.add(obj);
        log.info(logid + "Added SolrEventListener for firstSearcher: " + obj);
      } else if("newSearcher".equals(event) ){
        SolrEventListener obj = createInitInstance(info,clazz,label,null);
        newSearcherListeners.add(obj);
        log.info(logid + "Added SolrEventListener for newSearcher: " + obj);
      }
    }
  }

  final List<SolrEventListener> firstSearcherListeners = new ArrayList<SolrEventListener>();
  final List<SolrEventListener> newSearcherListeners = new ArrayList<SolrEventListener>();

 
  public void registerFirstSearcherListener( SolrEventListener listener )
  {
    firstSearcherListeners.add( listener );
  }

  public void registerNewSearcherListener( SolrEventListener listener )
  {
    newSearcherListeners.add( listener );
  }

  public void registerResponseWriter( String name, QueryResponseWriter responseWriter ){
    responseWriters.put(name, responseWriter);
  }

  

  /** Creates an instance by trying a constructor that accepts a SolrCore before
   *  trying the default (no arg) constructor.
   *@param className the instance class to create
   *@param cast the class or interface that the instance should extend or implement
   *@param msg a message helping compose the exception error if any occurs.
   *@return the desired instance
   *@throws SolrException if the object could not be instantiated
   */
  private <T extends Object> T createInstance(String className, Class<T> cast, String msg) {
    Class clazz = null;
    if (msg == null) msg = "SolrCore Object";
    try {
        clazz = getResourceLoader().findClass(className);
        if (cast != null && !cast.isAssignableFrom(clazz))
          throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"Error Instantiating "+msg+", "+className+ " is not a " +cast.getName());
      //most of the classes do not have constructors which takes SolrCore argument. It is recommended to obtain SolrCore by implementing SolrCoreAware.
      // So invariably always it will cause a  NoSuchMethodException. So iterate though the list of available constructors
        Constructor[] cons =  clazz.getConstructors();
        for (Constructor con : cons) {
          Class[] types = con.getParameterTypes();
          if(types.length == 1 && types[0] == SolrCore.class){
            return (T)con.newInstance(this);
          }
        }
        return (T) getResourceLoader().newInstance(className);//use the empty constructor      
    } catch (SolrException e) {
      throw e;
    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"Error Instantiating "+msg+", "+className+ " failed to instantiate " +cast.getName(), e);
    }
  }

  public <T extends Object> T createInitInstance(PluginInfo info,Class<T> cast, String msg, String defClassName){
    if(info == null) return null;
    T o = createInstance(info.className == null ? defClassName : info.className,cast, msg);
    if (o instanceof PluginInfoInitialized) {
      ((PluginInfoInitialized) o).init(info);
    } else if (o instanceof NamedListInitializedPlugin) {
      ((NamedListInitializedPlugin) o).init(info.initArgs);
    }
    return o;
  }

  public SolrEventListener createEventListener(String className) {
    return createInstance(className, SolrEventListener.class, "Event Listener");
  }

  public SolrRequestHandler createRequestHandler(String className) {
    return createInstance(className, SolrRequestHandler.class, "Request Handler");
  }

  private UpdateHandler createUpdateHandler(String className) {
    return createInstance(className, UpdateHandler.class, "Update Handler");
  }
  

  public SolrCore(String name, String dataDir, SolrConfig config, IndexSchema schema, CoreDescriptor cd) {
    coreDescriptor = cd;
    this.setName( name );
    resourceLoader = config.getResourceLoader();
    if (dataDir == null){
      dataDir =  config.getDataDir();
      if(dataDir == null) dataDir = cd.getDataDir();
    }

    dataDir = SolrResourceLoader.normalizeDir(dataDir);

    log.info(logid+"Opening new SolrCore at " + resourceLoader.getInstanceDir() + ", dataDir="+dataDir);

    if (schema==null) {
      schema = new IndexSchema(config, IndexSchema.DEFAULT_SCHEMA_FILE, null);
    }

    //Initialize JMX
//    if (config.jmxConfig.enabled) {
//      infoRegistry = new JmxMonitoredMap<String, SolrInfoMBean>(name, String.valueOf(this.hashCode()), config.jmxConfig);
//    } else  {
      log.info("JMX monitoring not detected for core: " + name);
      infoRegistry = new ConcurrentHashMap<String, SolrInfoMBean>();
//    }

    infoRegistry.put("fieldCache", new SolrFieldCacheMBean());

    this.schema = schema;
    this.dataDir = dataDir;
    this.solrConfig = config;
    this.startTime = System.currentTimeMillis();
//    this.maxWarmingSearchers = config.maxWarmingSearchers;

    booleanQueryMaxClauseCount();
  
    initListeners();

    initDeletionPolicy();


    initWriters();
    initQParsers();
    initValueSourceParsers();

    this.searchComponents = loadSearchComponents();

    // Processors initialized before the handlers
    updateProcessorChains = loadUpdateProcessorChains();
    reqHandlers = new RequestHandlers(this);
    reqHandlers.initHandlersFromConfig( solrConfig );


    // Handle things that should eventually go away
//    initDeprecatedSupport();

    final CountDownLatch latch = new CountDownLatch(1);

    try {
      // cause the executor to stall so firstSearcher events won't fire
      // until after inform() has been called for all components.
      // searchExecutor must be single-threaded for this to work
      searcherExecutor.submit(new Callable() {
        public Object call() throws Exception {
          latch.await();
          return null;
        }
      });

      // Open the searcher *before* the update handler so we don't end up opening
      // one in the middle.
      // With lockless commits in Lucene now, this probably shouldn't be an issue anymore
//      getSearcher(false,false,null);

      String updateHandlerClass = solrConfig.getUpdateHandlerInfo().className;

      updateHandler = createUpdateHandler(updateHandlerClass == null ?  DirectUpdateHandler2.class.getName():updateHandlerClass); 
      infoRegistry.put("updateHandler", updateHandler);

      // Finally tell anyone who wants to know
      resourceLoader.inform( resourceLoader );
      resourceLoader.inform( this );  // last call before the latch is released.
//      instance = this;   // set singleton for backwards compatibility
    } finally {
      // allow firstSearcher events to fire
      latch.countDown();
    }

    infoRegistry.put("core", this);
    
    // register any SolrInfoMBeans SolrResourceLoader initialized
    //
    // this must happen after the latch is released, because a JMX server impl may
    // choose to block on registering until properties can be fetched from an MBean,
    // and a SolrCoreAware MBean may have properties that depend on getting a Searcher
    // from the core.
    resourceLoader.inform(infoRegistry);
  }


  /**
   * Load the request processors
   */
   private Map<String,UpdateRequestProcessorChain> loadUpdateProcessorChains() {
    Map<String, UpdateRequestProcessorChain> map = new HashMap<String, UpdateRequestProcessorChain>();
    UpdateRequestProcessorChain def = initPlugins(map,UpdateRequestProcessorChain.class, UpdateRequestProcessorChain.class.getName());
    if(def == null){
      def = map.get(null);
    } 
    if (def == null) {
      // construct the default chain
      UpdateRequestProcessorFactory[] factories = new UpdateRequestProcessorFactory[]{
              new LogUpdateProcessorFactory(),
              new RunUpdateProcessorFactory()
      };
      def = new UpdateRequestProcessorChain(factories, this);
    }
    map.put(null, def);
    map.put("", def);
    return map;
  }

  /**
   * @return an update processor registered to the given name.  Throw an exception if this chain is undefined
   */    
  public UpdateRequestProcessorChain getUpdateProcessingChain( final String name )
  {
    UpdateRequestProcessorChain chain = updateProcessorChains.get( name );
    if( chain == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
          "unknown UpdateRequestProcessorChain: "+name );
    }
    return chain;
  }
  
  // this core current usage count
  private final AtomicInteger refCount = new AtomicInteger(1);

  final void open() {
    refCount.incrementAndGet();
  }
  
  
  public void close() {
    int count = refCount.decrementAndGet();
    if (count > 0) return; // close is called often, and only actually closes if nothing is using it.
    if (count < 0) {
      log.error("Too many close [count:{}] on {}. Please report this exception to solr-user@lucene.apache.org", count, this );
      return;
    }
    log.info(logid+" CLOSING SolrCore " + this);

//
//    if( closeHooks != null ) {
//       for( CloseHook hook : closeHooks ) {
//         try {
//           hook.preClose( this );
//         } catch (Throwable e) {
//           SolrException.log(log, e);           
//         }
//      }
//    }


    try {
      infoRegistry.clear();
    } catch (Exception e) {
      SolrException.log(log, e);
    }
    try {
      updateHandler.close();
    } catch (Exception e) {
      SolrException.log(log,e);
    }
    try {
      searcherExecutor.shutdown();
      if (!searcherExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
        log.error("Timeout waiting for searchExecutor to terminate");
      }
    } catch (Exception e) {
      SolrException.log(log,e);
    }
 

//    if( closeHooks != null ) {
//       for( CloseHook hook : closeHooks ) {
//         try {
//           hook.postClose( this );
//         } catch (Throwable e) {
//           SolrException.log(log, e);
//         }
//      }
//    }
  }

  /** Current core usage count. */
  public int getOpenCount() {
    return refCount.get();
  }
  
  /** Whether this core is closed. */
  public boolean isClosed() {
      return refCount.get() <= 0;
  }
  
  @Override
  protected void finalize() throws Throwable {
    try {
      if (getOpenCount() != 0) {
        log.error("REFCOUNT ERROR: unreferenced " + this + " (" + getName()
            + ") has a reference count of " + getOpenCount());
      }
    } finally {
      super.finalize();
    }
  }

//  private Collection<CloseHook> closeHooks = null;
//
//   /**
//    * Add a close callback hook
//    */
//   public void addCloseHook( CloseHook hook )
//   {
//     if( closeHooks == null ) {
//       closeHooks = new ArrayList<CloseHook>();
//     }
//     closeHooks.add( hook );
//   }

  /**
   * Returns a Request object based on the admin/pingQuery section
   * of the Solr config file.
   * 
   * @deprecated use {@link org.apache.solr.handler.PingRequestHandler} instead
   */
  @Deprecated
  public SolrQueryRequest getPingQueryRequest() {
    return solrConfig.getPingQueryRequest(this);
  }
  ////////////////////////////////////////////////////////////////////////////////
  // Request Handler
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Get the request handler registered to a given name.  
   * 
   * This function is thread safe.
   */
  public SolrRequestHandler getRequestHandler(String handlerName) {
    return reqHandlers.get(handlerName);
  }

  /**
   * Returns an unmodifieable Map containing the registered handlers of the specified type.
   */
  public Map<String,SolrRequestHandler> getRequestHandlers(Class clazz) {
    return reqHandlers.getAll(clazz);
  }
  
  /**
   * Returns an unmodifieable Map containing the registered handlers
   */
  public Map<String,SolrRequestHandler> getRequestHandlers() {
    return reqHandlers.getRequestHandlers();
  }

  /**
   * Get the SolrHighlighter
   */
  @Deprecated
  public SolrHighlighter getHighlighter() {
    HighlightComponent hl = (HighlightComponent) searchComponents.get(HighlightComponent.COMPONENT_NAME);
    return hl==null? null: hl.getHighlighter();
  }

  
  public SolrRequestHandler registerRequestHandler(String handlerName, SolrRequestHandler handler) {
    return reqHandlers.register(handlerName,handler);
  }
  
  /**
   * Register the default search components
   */
  private Map<String, SearchComponent> loadSearchComponents()
  {
    Map<String, SearchComponent> components = new HashMap<String, SearchComponent>();
    initPlugins(components,SearchComponent.class);
    for (Map.Entry<String, SearchComponent> e : components.entrySet()) {
      SearchComponent c = e.getValue();
      if (c instanceof HighlightComponent) {
        HighlightComponent hl = (HighlightComponent) c;
        if(!HighlightComponent.COMPONENT_NAME.equals(e.getKey())){
          components.put(HighlightComponent.COMPONENT_NAME,hl);
        }
        break;
      }
    }
//    addIfNotPresent(components,HighlightComponent.COMPONENT_NAME,HighlightComponent.class);
    addIfNotPresent(components,QueryComponent.COMPONENT_NAME,QueryComponent.class);
    addIfNotPresent(components,FacetComponent.COMPONENT_NAME,FacetComponent.class);
//    addIfNotPresent(components,MoreLikeThisComponent.COMPONENT_NAME,MoreLikeThisComponent.class);
//    addIfNotPresent(components,StatsComponent.COMPONENT_NAME,StatsComponent.class);
//    addIfNotPresent(components,DebugComponent.COMPONENT_NAME,DebugComponent.class);
    return components;
  }
  private <T> void addIfNotPresent(Map<String ,T> registry, String name, Class<? extends  T> c){
    if(!registry.containsKey(name)){
      T searchComp = (T) resourceLoader.newInstance(c.getName());
      registry.put(name, searchComp);
      if (searchComp instanceof SolrInfoMBean){
        infoRegistry.put(((SolrInfoMBean)searchComp).getName(), (SolrInfoMBean)searchComp);
      }
    }
  }
  
  /**
   * @return a Search Component registered to a given name.  Throw an exception if the component is undefined
   */
  public SearchComponent getSearchComponent( String name )
  {
    SearchComponent component = searchComponents.get( name );
    if( component == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
          "Unknown Search Component: "+name );
    }
    return component;
  }

  /**
   * Accessor for all the Search Components
   * @return An unmodifiable Map of Search Components
   */
  public Map<String, SearchComponent> getSearchComponents() {
    return Collections.unmodifiableMap(searchComponents);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Update Handler
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * RequestHandlers need access to the updateHandler so they can all talk to the
   * same RAM indexer.  
   */
  public UpdateHandler getUpdateHandler() {
    return updateHandler;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Searcher Control
  ////////////////////////////////////////////////////////////////////////////////

  // The current searcher used to service queries.
  // Don't access this directly!!!! use getSearcher() to
  // get it (and it will increment the ref count at the same time).
  // This reference is protected by searcherLock.
//  private RefCounted<SolrIndexSearcher> _searcher;

  // All of the open searchers.  Don't access this directly.
  // protected by synchronizing on searcherLock.
//  private final LinkedList<RefCounted<SolrIndexSearcher>> _searchers = new LinkedList<RefCounted<SolrIndexSearcher>>();

  final ExecutorService searcherExecutor = Executors.newSingleThreadExecutor();
//  private int onDeckSearchers;  // number of searchers preparing
  private Object searcherLock = new Object();  // the sync object for the searcher
//  private final int maxWarmingSearchers;  // max number of on-deck searchers allowed

  private static Integer cacheSize = 16;
  private static Integer cacheSizeHb = 4;
  private static final Object listlock=new Object();
  public static Cache<String,RefCounted<SolrIndexSearcher>> searchCache=null;
  public static Cache<String,RefCounted<SolrIndexSearcher>> searchCacheForHb=null;
  
  public static final LinkedList<RefCounted<SolrIndexSearcher>> clearlist=new LinkedList<RefCounted<SolrIndexSearcher>>();


  public static String binglogType="hdfs";
  public static String getBinglogType() {
	return binglogType;
}



public static void setBinglogType(String binglogType) {
	SolrCore.binglogType = binglogType;
}



public static void setSearchCacheSize(int cacheSize) {
		SolrCore.cacheSize = cacheSize;
  }
  
	private void initSearchCache() {
		if (searchCache == null) {
			final float LOADFACTOR = 0.75f;
			SimpleMapCache<String, RefCounted<SolrIndexSearcher>> ccache = (new SimpleMapCache<String, RefCounted<SolrIndexSearcher>>(
					new LinkedHashMap<String, RefCounted<SolrIndexSearcher>>(
							(int) Math.ceil(cacheSize / LOADFACTOR) + 1,
							LOADFACTOR, true) {
						private static final long serialVersionUID = 1L;

						@Override
						protected boolean removeEldestEntry(Map.Entry<String, RefCounted<SolrIndexSearcher>> eldest) {
							boolean rtn= size() > cacheSize;
							if(rtn)
							{
								DropSearch(eldest.getValue());
								log.info("SolrIndexSearcher clear:"	+ eldest.getKey()+","+this.size()+"@"+cacheSize);
							}
							return rtn;
						}
					}));

			searchCache = Cache.synchronizedCache(ccache);
		}
		
		
		if (searchCacheForHb == null) {
			final float LOADFACTOR = 0.75f;
			SimpleMapCache<String, RefCounted<SolrIndexSearcher>> ccache = (new SimpleMapCache<String, RefCounted<SolrIndexSearcher>>(
					new LinkedHashMap<String, RefCounted<SolrIndexSearcher>>(
							(int) Math.ceil(cacheSizeHb / LOADFACTOR) + 1,
							LOADFACTOR, true) {
						private static final long serialVersionUID = 1L;

						@Override
						protected boolean removeEldestEntry(Map.Entry<String, RefCounted<SolrIndexSearcher>> eldest) {
							boolean rtn= size() > cacheSizeHb;
							if(rtn)
							{
								DropSearch(eldest.getValue());
//								log.info("SolrIndexSearcher clear for hb:"	+ eldest.getKey()+","+this.size()+"@"+cacheSizeHb);
							}
							return rtn;
						}
					}));

			searchCacheForHb = Cache.synchronizedCache(ccache);
		}
	}
	
	private void clearPartion() throws IOException {
		long maxwaittime=1000l*60*30;
		synchronized (listlock) {
			long nowtime=System.currentTimeMillis();
			ArrayList<RefCounted<SolrIndexSearcher>> refull = new ArrayList<RefCounted<SolrIndexSearcher>>();

			RefCounted<SolrIndexSearcher> refCount;
			while ((refCount = clearlist.poll()) != null) {
				if (refCount.getRefcount() <= 0||nowtime-refCount.reftime>maxwaittime) {
					try{
					refCount.get().close();
					}catch(Throwable e){
						
					}
				} else {
					refull.add(refCount);
					log.info("ref clearpartion "+refCount.toDebugMsg());
				}
			}
			
			clearlist.addAll(refull);
		}
	}
	
	
	private RefCounted<SolrIndexSearcher> newHolderPartion(
			SolrIndexSearcher newSearcher) {
		RefCounted<SolrIndexSearcher> holder = new RefCounted<SolrIndexSearcher>(newSearcher) {
			@Override
			public void close() {
			}
		};
		holder.incref();
		return holder;
	}
	
	
	
	public void DropSearch(RefCounted<SolrIndexSearcher> search)
	{
		synchronized (listlock) {
			search.reftime=System.currentTimeMillis();
			clearlist.add(search);
		}
		try {
			clearPartion();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public RefCounted<SolrIndexSearcher> getSearcher(String partion,boolean mdirs,boolean isclearCache,boolean ishb) {

		String corename=this.getName();
		if(corename==null)
		{
			corename="";
		}
		
		PartionKey p=new PartionKey(corename, partion);
		
		String partionKey=corename+"@"+partion+"@"+SolrResourceLoader.getCacheFlushKey(p);
		File f = new File(getDataDir(), partion);
		return getSearcherByPath(p,partionKey, f.getAbsolutePath(), mdirs, isclearCache, ishb);
				
		
	}
	
	private static Cache<PartionKey,ReadOnlyDirectory> forReadOnlyDir =(new SimpleMapCache<PartionKey, ReadOnlyDirectory>(
			new LinkedHashMap<PartionKey, ReadOnlyDirectory>(
					(int) Math.ceil(16 / 0.75f) + 1,
					0.75f, true) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<PartionKey, ReadOnlyDirectory> eldest) {
					boolean rtn= size() > 16;
					return rtn;
				}
			}));
	
	private static Cache<PartionKey,RealTimeDirectory> forRealTimeDir =(new SimpleMapCache<PartionKey, RealTimeDirectory>(
			new LinkedHashMap<PartionKey, RealTimeDirectory>(
					(int) Math.ceil(16 / 0.75f) + 1,
					0.75f, true) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<PartionKey, RealTimeDirectory> eldest) {
					boolean rtn= size() > 16;
					if(rtn)
					{
						eldest.getValue().mergerFinal();
						eldest.getValue().syncHdfs();
						eldest.getValue().close();
					}
					return rtn;
				}
			}));
	
	
	private static Cache<PartionKey,RealTimeDirectory> forWriteDir =(new SimpleMapCache<PartionKey, RealTimeDirectory>(
			new LinkedHashMap<PartionKey, RealTimeDirectory>(
					(int) Math.ceil(32 / 0.75f) + 1,
					0.75f, true) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<PartionKey, RealTimeDirectory> eldest) {
					boolean rtn= size() > 32;
					if(rtn)
					{
						eldest.getValue().syncHdfs();
						eldest.getValue().close();
					}
					return rtn;
				}
			}));


	
	public static ConcurrentHashMap<String, String> tablemode=new ConcurrentHashMap<String, String>();
	
	public static String getTablemode(String table) {
		return tablemode.get(table);
	}



	public static void setTablemode(String tablename,String tablemode) {
		SolrCore.tablemode.put(tablename, tablemode);
	}
	
	

	
	private MdrillDirectory getForWrite(String partion, boolean forwrite)
			throws IOException {

		String corename = this.getName();
		if (corename == null) {
			corename = "";
		}
		PartionKey p = new PartionKey(corename, partion);

		boolean isWritePool = false;
		synchronized (forRealTimeDir) {

			RealTimeDirectory rtn = forRealTimeDir.remove(p);
			if (forwrite) {
				isWritePool = true;
				if (rtn == null) {
					rtn = forWriteDir.remove(p);
				}
			} else {
				if (rtn == null) {
					rtn = forWriteDir.remove(p);
					if (rtn != null) {
						isWritePool = true;
					}
				}
			}

			if (rtn == null) {

				File f = new File(getDataDir(), partion);
				try{
				rtn = new RealTimeDirectory(f, HadoopUtil.hadoopConfDir,
						ShardPartion
								.getHdfsRealtimePath(p.tablename, p.partion)
								.toString(),this,p);
				}catch(Throwable e)
				{
					log.error("getForWrite error",e);
					rtn=null;
				}
			}

			if (isWritePool) {
				forWriteDir.put(p, rtn);
			} else {
				forRealTimeDir.put(p, rtn);
			}
			rtn.setCore(this);
			rtn.setPartion(p);

			return rtn;
		}
	}


	public MdrillDirectory getRealTime(String partion,boolean forwrite) throws IOException
	{
		
		String corename=this.getName();
		if(corename==null)
		{
			corename="";
		}
		
		boolean isrealtime=forwrite||String.valueOf(getTablemode(corename)).indexOf("@realtime@")>=0;
		
		PartionKey p=new PartionKey(corename, partion);
		
		if(isrealtime)
		{
			synchronized (forReadOnlyDir) {
				forReadOnlyDir.remove(p);
			}
			return getForWrite(partion,forwrite);
		}
		
		synchronized (forReadOnlyDir) {
			MdrillDirectory rtdir=forReadOnlyDir.get(p);
			if(rtdir!=null)
			{
				rtdir.setCore(this);
				rtdir.setPartion(p);
				return rtdir;
			}
			
			File f = new File(getDataDir(), partion);
			ReadOnlyDirectory ddd=new ReadOnlyDirectory(f , HadoopUtil.hadoopConfDir, ShardPartion.getHdfsRealtimePath(p.tablename,p.partion).toString());
			rtdir=ddd;
			forReadOnlyDir.put(p, ddd);
			rtdir.setCore(this);
			rtdir.setPartion(p);
			return rtdir;
		}
		
	}
	
	public synchronized RefCounted<SolrIndexSearcher> getSearcherByPath(PartionKey p,String partionKey,String path,boolean mdirs,boolean isclearCache,boolean ishb) {
		try {
			this.clearPartion();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.initSearchCache();
	
		
		try {
			RefCounted<SolrIndexSearcher> rtn = searchCache.get(partionKey);
			if(rtn==null)
			{
				rtn=searchCacheForHb.get(partionKey);
			}
			if(isclearCache&&rtn != null)
			{
				this.DropSearch(rtn);
				SolrResourceLoader.SetCacheFlushKey(p,System.currentTimeMillis());
				rtn=null;
				searchCache.remove(partionKey);
				searchCacheForHb.remove(partionKey);
			}
			if (rtn != null) {
				rtn.incref();
				return rtn;
			}else{
				File f = new File(path);
				log.info("getSearcher:" + partionKey + ":" + f.getAbsolutePath());
				if (!f.exists()&&mdirs) {
					f.mkdirs();
				}
				
				if(!f.exists())
				{
					return null;
				}
				
				Directory dir=null;
				IndexReader reader=null;
				if(p==null)
				{
					log.info("#######################getSearcher:" + partionKey + ":" + f.getAbsolutePath()+",p=null");
					dir=LinkFSDirectory.readOnlyOpen(f);;
					dir.setCore(this,p);
					reader=IndexReader.open(dir);
				}else if(p.partion.equals("default"))
				{
					RAMDirectory rd=new RAMDirectory();
					rd.setCore(this,p);
					IndexWriter writer=new IndexWriter(rd, null,new KeepOnlyLastCommitDeletionPolicy(), MaxFieldLength.UNLIMITED);
					writer.setMergeFactor(10);
					writer.setUseCompoundFile(false);
					writer.close();	
					reader=IndexReader.open(rd);
				}else{
					MdrillDirectory rtdir=getRealTime(p.partion,false);
					List<Directory> list=rtdir.getForSearch();
					IndexReader[] r=new IndexReader[list.size()];
					for(int i=0;i<list.size();i++)
					{
						Directory d=list.get(i);
						d.setCore(this,p);
						r[i]=IndexReader.open(d);
					}
					if(r.length==1)
					{
						reader=	r[0];
					}else{
						reader=new MultiReader(r,true);
					}
				}
				
				if(dir==null)
				{
					dir=FSDirectory.open(f);
					dir.setCore(this,p);
				}

				File cachedir = null;
				if(dir instanceof FSDirectory)
				{
					FSDirectory d=(FSDirectory)dir;
					cachedir=new File(d.getDirectory(), "cacheField");
				}else{
					cachedir=new File(f, "cacheField");
				}
				
				
				cachedir.mkdirs();
				Directory fieldcacheDir = FSDirectory.open(cachedir);
				fieldcacheDir.setCore(this,p);

				SolrIndexSearcher newSearcher = new SolrIndexSearcher(this,	schema, "partion_" + partionKey, reader, true);
				newSearcher.setPartionCacheKey(p);
				newSearcher.setFieldcacheDir(fieldcacheDir);
				newSearcher.setPartionKey(partionKey);
				rtn = newHolderPartion(newSearcher);
				if(ishb)
				{
					searchCacheForHb.put(partionKey, rtn);
				}else{
					searchCache.put(partionKey, rtn);
				}
				return rtn;
			}
		} catch (IOException e) {
			SolrCore.log.info(e.toString());
			SolrException.log(log, null, e);
			return null;
		}
	}

//  public RefCounted<SolrIndexSearcher> getSearcher() {
//    try {
//      return getSearcher(false,true,null);
//    } catch (IOException e) {
//      SolrException.log(log,null,e);
//      return null;
//    }
//  }

//  /**
//  * Return the newest {@link RefCounted}&lt;{@link SolrIndexSearcher}&gt; with
//  * the reference count incremented.  It <b>must</b> be decremented when no longer needed.
//  * If no searcher is currently open, then if openNew==true a new searcher will be opened,
//  * or null is returned if openNew==false.
//  */
//  public RefCounted<SolrIndexSearcher> getNewestSearcher(boolean openNew) {
//    synchronized (searcherLock) {
//      if (_searchers.isEmpty()) {
//        if (!openNew) return null;
//        // Not currently implemented since simply calling getSearcher during inform()
//        // can result in a deadlock.  Right now, solr always opens a searcher first
//        // before calling inform() anyway, so this should never happen.
//        throw new UnsupportedOperationException();
//      }
//      RefCounted<SolrIndexSearcher> newest = _searchers.getLast();
//      newest.incref();
//      return newest;
//    }
//  }


  /**
   * Get a {@link SolrIndexSearcher} or start the process of creating a new one.
   * <p>
   * The registered searcher is the default searcher used to service queries.
   * A searcher will normally be registered after all of the warming
   * and event handlers (newSearcher or firstSearcher events) have run.
   * In the case where there is no registered searcher, the newly created searcher will
   * be registered before running the event handlers (a slow searcher is better than no searcher).
   *
   * <p>
   * These searchers contain read-only IndexReaders. To access a non read-only IndexReader,
   * see newSearcher(String name, boolean readOnly).
   *
   * <p>
   * If <tt>forceNew==true</tt> then
   *  A new searcher will be opened and registered regardless of whether there is already
   *    a registered searcher or other searchers in the process of being created.
   * <p>
   * If <tt>forceNew==false</tt> then:<ul>
   *   <li>If a searcher is already registered, that searcher will be returned</li>
   *   <li>If no searcher is currently registered, but at least one is in the process of being created, then
   * this call will block until the first searcher is registered</li>
   *   <li>If no searcher is currently registered, and no searchers in the process of being registered, a new
   * searcher will be created.</li>
   * </ul>
   * <p>
   * If <tt>returnSearcher==true</tt> then a {@link RefCounted}&lt;{@link SolrIndexSearcher}&gt; will be returned with
   * the reference count incremented.  It <b>must</b> be decremented when no longer needed.
   * <p>
   * If <tt>waitSearcher!=null</tt> and a new {@link SolrIndexSearcher} was created,
   * then it is filled in with a Future that will return after the searcher is registered.  The Future may be set to
   * <tt>null</tt> in which case the SolrIndexSearcher created has already been registered at the time
   * this method returned.
   * <p>
   * @param forceNew           if true, force the open of a new index searcher regardless if there is already one open.
   * @param returnSearcher     if true, returns a {@link SolrIndexSearcher} holder with the refcount already incremented.
   * @param waitSearcher       if non-null, will be filled in with a {@link Future} that will return after the new searcher is registered.
   * @throws IOException
   */
//  public RefCounted<SolrIndexSearcher> getSearcher(boolean forceNew, boolean returnSearcher, final Future[] waitSearcher) throws IOException {
//    // it may take some time to open an index.... we may need to make
//    // sure that two threads aren't trying to open one at the same time
//    // if it isn't necessary.
//
//    synchronized (searcherLock) {
//      // see if we can return the current searcher
//      if (_searcher!=null && !forceNew) {
//        if (returnSearcher) {
//          _searcher.incref();
//          return _searcher;
//        } else {
//          return null;
//        }
//      }
//
//      // check to see if we can wait for someone else's searcher to be set
//      if (onDeckSearchers>0 && !forceNew && _searcher==null) {
//        try {
//          searcherLock.wait();
//        } catch (InterruptedException e) {
//          log.info(SolrException.toStr(e));
//        }
//      }
//
//      // check again: see if we can return right now
//      if (_searcher!=null && !forceNew) {
//        if (returnSearcher) {
//          _searcher.incref();
//          return _searcher;
//        } else {
//          return null;
//        }
//      }
//
//      // At this point, we know we need to open a new searcher...
//      // first: increment count to signal other threads that we are
//      //        opening a new searcher.
//      onDeckSearchers++;
//      if (onDeckSearchers < 1) {
//        // should never happen... just a sanity check
//        log.error(logid+"ERROR!!! onDeckSearchers is " + onDeckSearchers);
//        onDeckSearchers=1;  // reset
//      } else if (onDeckSearchers > maxWarmingSearchers) {
//        onDeckSearchers--;
//        String msg="Error opening new searcher. exceeded limit of maxWarmingSearchers="+maxWarmingSearchers + ", try again later.";
//        log.warn(logid+""+ msg);
//        // HTTP 503==service unavailable, or 409==Conflict
//        throw new SolrException(SolrException.ErrorCode.SERVICE_UNAVAILABLE,msg,true);
//      } else if (onDeckSearchers > 1) {
//        log.info(logid+"PERFORMANCE WARNING: Overlapping onDeckSearchers=" + onDeckSearchers);
//      }
//    }
//
//    // open the index synchronously
//    // if this fails, we need to decrement onDeckSearchers again.
//    SolrIndexSearcher tmp;
//    RefCounted<SolrIndexSearcher> newestSearcher = null;
//
//    try {
//      newestSearcher = getNewestSearcher(false);
//      String newIndexDir = getNewIndexDir();
//      File indexDirFile = new File(getIndexDir()).getCanonicalFile();
//      File newIndexDirFile = new File(newIndexDir).getCanonicalFile();
//      
//      if (newestSearcher != null && solrConfig.reopenReaders
//          && indexDirFile.equals(newIndexDirFile)) {
//        IndexReader currentReader = newestSearcher.get().getReader();
//        IndexReader newReader = IndexReader.openIfChanged(currentReader);
//
//        if (newReader == null) {
//          currentReader.incRef();
//          newReader = currentReader;
//        }
//
//        tmp = new SolrIndexSearcher(this, schema, "main", newReader, true, true);
//      } else {
//        IndexReader reader = getIndexReaderFactory().newReader(getDirectoryFactory().open(newIndexDir), true);
//        tmp = new SolrIndexSearcher(this, schema, "main", reader, true, true);
//      }
//    } catch (Throwable th) {
//      synchronized(searcherLock) {
//        onDeckSearchers--;
//        // notify another waiter to continue... it may succeed
//        // and wake any others.
//        searcherLock.notify();
//      }
//      // need to close the searcher here??? we shouldn't have to.
//      throw new RuntimeException(th);
//    } finally {
//      if (newestSearcher != null) {
//        newestSearcher.decref();
//      }
//    }
//    
//    final SolrIndexSearcher newSearcher=tmp;
//
//    RefCounted<SolrIndexSearcher> currSearcherHolder=null;
//    final RefCounted<SolrIndexSearcher> newSearchHolder=newHolder(newSearcher);
//
//    if (returnSearcher) newSearchHolder.incref();
//
//    // a signal to decrement onDeckSearchers if something goes wrong.
//    final boolean[] decrementOnDeckCount=new boolean[1];
//    decrementOnDeckCount[0]=true;
//
//    try {
//
//      boolean alreadyRegistered = false;
//      synchronized (searcherLock) {
//        _searchers.add(newSearchHolder);
//
//        if (_searcher == null) {
//          // if there isn't a current searcher then we may
//          // want to register this one before warming is complete instead of waiting.
//          if (solrConfig.useColdSearcher) {
//            registerSearcher(newSearchHolder);
//            decrementOnDeckCount[0]=false;
//            alreadyRegistered=true;
//          }
//        } else {
//          // get a reference to the current searcher for purposes of autowarming.
//          currSearcherHolder=_searcher;
//          currSearcherHolder.incref();
//        }
//      }
//
//
//      final SolrIndexSearcher currSearcher = currSearcherHolder==null ? null : currSearcherHolder.get();
//
//      //
//      // Note! if we registered the new searcher (but didn't increment it's
//      // reference count because returnSearcher==false, it's possible for
//      // someone else to register another searcher, and thus cause newSearcher
//      // to close while we are warming.
//      //
//      // Should we protect against that by incrementing the reference count?
//      // Maybe we should just let it fail?   After all, if returnSearcher==false
//      // and newSearcher has been de-registered, what's the point of continuing?
//      //
//
//      Future future=null;
//
//      // warm the new searcher based on the current searcher.
//      // should this go before the other event handlers or after?
//      if (currSearcher != null) {
//        future = searcherExecutor.submit(
//                new Callable() {
//                  public Object call() throws Exception {
//                    try {
//                      newSearcher.warm(currSearcher);
//                    } catch (Throwable e) {
//                      SolrException.logOnce(log,null,e);
//                    }
//                    return null;
//                  }
//                }
//        );
//      }
//      
//      if (currSearcher==null && firstSearcherListeners.size() > 0) {
//        future = searcherExecutor.submit(
//                new Callable() {
//                  public Object call() throws Exception {
//                    try {
//                      for (SolrEventListener listener : firstSearcherListeners) {
//                        listener.newSearcher(newSearcher,null);
//                      }
//                    } catch (Throwable e) {
//                      SolrException.logOnce(log,null,e);
//                    }
//                    return null;
//                  }
//                }
//        );
//      }
//
//      if (currSearcher!=null && newSearcherListeners.size() > 0) {
//        future = searcherExecutor.submit(
//                new Callable() {
//                  public Object call() throws Exception {
//                    try {
//                      for (SolrEventListener listener : newSearcherListeners) {
//                        listener.newSearcher(newSearcher, currSearcher);
//                      }
//                    } catch (Throwable e) {
//                      SolrException.logOnce(log,null,e);
//                    }
//                    return null;
//                  }
//                }
//        );
//      }
//
//      // WARNING: this code assumes a single threaded executor (that all tasks
//      // queued will finish first).
//      final RefCounted<SolrIndexSearcher> currSearcherHolderF = currSearcherHolder;
//      if (!alreadyRegistered) {
//        future = searcherExecutor.submit(
//                new Callable() {
//                  public Object call() throws Exception {
//                    try {
//                      // signal that we no longer need to decrement
//                      // the count *before* registering the searcher since
//                      // registerSearcher will decrement even if it errors.
//                      decrementOnDeckCount[0]=false;
//                      registerSearcher(newSearchHolder);
//                    } catch (Throwable e) {
//                      SolrException.logOnce(log,null,e);
//                    } finally {
//                      // we are all done with the old searcher we used
//                      // for warming...
//                      if (currSearcherHolderF!=null) currSearcherHolderF.decref();
//                    }
//                    return null;
//                  }
//                }
//        );
//      }
//
//      if (waitSearcher != null) {
//        waitSearcher[0] = future;
//      }
//
//      // Return the searcher as the warming tasks run in parallel
//      // callers may wait on the waitSearcher future returned.
//      return returnSearcher ? newSearchHolder : null;
//
//    } catch (Exception e) {
//      SolrException.logOnce(log,null,e);
//      if (currSearcherHolder != null) currSearcherHolder.decref();
//
//      synchronized (searcherLock) {
//        if (decrementOnDeckCount[0]) {
//          onDeckSearchers--;
//        }
//        if (onDeckSearchers < 0) {
//          // sanity check... should never happen
//          log.error(logid+"ERROR!!! onDeckSearchers after decrement=" + onDeckSearchers);
//          onDeckSearchers=0; // try and recover
//        }
//        // if we failed, we need to wake up at least one waiter to continue the process
//        searcherLock.notify();
//      }
//
//      // since the indexreader was already opened, assume we can continue on
//      // even though we got an exception.
//      return returnSearcher ? newSearchHolder : null;
//    }
//
//  }


//  private RefCounted<SolrIndexSearcher> newHolder(SolrIndexSearcher newSearcher) {
//    RefCounted<SolrIndexSearcher> holder = new RefCounted<SolrIndexSearcher>(newSearcher) {
//      @Override
//      public void close() {
//        try {
//          synchronized(searcherLock) {
//            // it's possible for someone to get a reference via the _searchers queue
//            // and increment the refcount while RefCounted.close() is being called.
//            // we check the refcount again to see if this has happened and abort the close.
//            // This relies on the RefCounted class allowing close() to be called every
//            // time the counter hits zero.
//            if (refcount.get() > 0) return;
//            _searchers.remove(this);
//          }
//          resource.close();
//        } catch (IOException e) {
//          log.error("Error closing searcher:" + SolrException.toStr(e));
//        }
//      }
//    };
//    holder.incref();  // set ref count to 1 to account for this._searcher
//    return holder;
//  }


  // Take control of newSearcherHolder (which should have a reference count of at
  // least 1 already.  If the caller wishes to use the newSearcherHolder directly
  // after registering it, then they should increment the reference count *before*
  // calling this method.
  //
  // onDeckSearchers will also be decremented (it should have been incremented
  // as a result of opening a new searcher).
//  private void registerSearcher(RefCounted<SolrIndexSearcher> newSearcherHolder) throws IOException {
//    synchronized (searcherLock) {
//      try {
//        if (_searcher != null) {
//          _searcher.decref();   // dec refcount for this._searcher
//          _searcher=null;
//        }
//
//        _searcher = newSearcherHolder;
//        SolrIndexSearcher newSearcher = newSearcherHolder.get();
//
//        /***
//        // a searcher may have been warming asynchronously while the core was being closed.
//        // if this happens, just close the searcher.
//        if (isClosed()) {
//          // NOTE: this should not happen now - see close() for details.
//          // *BUT* if we left it enabled, this could still happen before
//          // close() stopped the executor - so disable this test for now.
//          log.error("Ignoring searcher register on closed core:" + newSearcher);
//          _searcher.decref();
//        }
//        ***/
//
//        newSearcher.register(); // register subitems (caches)
//        log.info(logid+"Registered new searcher " + newSearcher);
//
//      } catch (Throwable e) {
//        log(e);
//      } finally {
//        // wake up anyone waiting for a searcher
//        // even in the face of errors.
//        onDeckSearchers--;
//        searcherLock.notifyAll();
//      }
//    }
//  }





  public void execute(SolrRequestHandler handler, SolrQueryRequest req, SolrQueryResponse rsp) {
    if (handler==null) {
      String msg = "Null Request Handler '" +
        req.getParams().get(CommonParams.QT) + "'";
      
      if (log.isWarnEnabled()) log.warn(logid + msg + ":" + req);
      
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, msg, true);
    }
    // setup response header and handle request
    final NamedList<Object> responseHeader = new SimpleOrderedMap<Object>();
    rsp.add("responseHeader", responseHeader);

   
	    
	    
	    handler.handleRequest(req,rsp);
	    setResponseHeaderValues(handler,req,rsp);
	    
	    if(req.getParams().get("showlog","0").equals("1"))
	    {
	 // toLog is a local ref to the same NamedList used by the request
	    NamedList toLog = rsp.getToLog();
	    // for back compat, we set these now just in case other code
	    // are expecting them during handleRequest
	    toLog.add("webapp", req.getContext().get("webapp"));
	    toLog.add("path", req.getContext().get("path"));
	    SolrParams params=req.getParams();
	    NamedList<Object> p=params.toNamedList();
	    p.remove("mdrill.crc.key.get.crclist");
	    toLog.add("params", "{" + p.toString() + "}");
	    StringBuilder sb = new StringBuilder(logid);
	    for (int i=0; i<toLog.size(); i++) {
	      String name = toLog.getName(i);
	      Object val = toLog.getVal(i);
	      sb.append(name).append("=").append(val).append(" ");
	    }
	    String loginfo=sb.toString();
	    log.info(loginfo.substring(0,Math.min(2048, loginfo.length())));
    }
  

  }

  /**
   * @deprecated Use {@link #execute(SolrRequestHandler, SolrQueryRequest, SolrQueryResponse)} instead. 
   */
  @Deprecated
  public void execute(SolrQueryRequest req, SolrQueryResponse rsp) {
    SolrRequestHandler handler = getRequestHandler(req.getQueryType());
    if (handler==null) {
      log.warn(logid+"Unknown Request Handler '" + req.getQueryType() +"' :" + req);
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Unknown Request Handler '" + req.getQueryType() + "'", true);
    }
    execute(handler, req, rsp);
  }
  
  public static void setResponseHeaderValues(SolrRequestHandler handler, SolrQueryRequest req, SolrQueryResponse rsp) {
    // TODO should check that responseHeader has not been replaced by handler
	NamedList responseHeader = rsp.getResponseHeader();
    final int qtime=(int)(rsp.getEndTime() - req.getStartTime());
    int status = 0;
    Exception exception = rsp.getException();
    if( exception != null ){
      if( exception instanceof SolrException )
        status = ((SolrException)exception).code();
      else
        status = 500;
    }
    responseHeader.add("status",status);
    responseHeader.add("QTime",qtime);
    rsp.getToLog().add("status",status);
    rsp.getToLog().add("QTime",qtime);
    
    SolrParams params = req.getParams();
    if( params.getBool(CommonParams.HEADER_ECHO_HANDLER, false) ) {
      responseHeader.add("handler", handler.getName() );
    }
    
    // Values for echoParams... false/true/all or false/explicit/all ???
    String ep = params.get( CommonParams.HEADER_ECHO_PARAMS, null );
    if( ep != null ) {
      EchoParamStyle echoParams = EchoParamStyle.get( ep );
      if( echoParams == null ) {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Invalid value '" + ep + "' for " + CommonParams.HEADER_ECHO_PARAMS 
            + " parameter, use '" + EchoParamStyle.EXPLICIT + "' or '" + EchoParamStyle.ALL + "'" );
      }
      if( echoParams == EchoParamStyle.EXPLICIT ) {
        responseHeader.add("params", req.getOriginalParams().toNamedList());
      } else if( echoParams == EchoParamStyle.ALL ) {
        responseHeader.add("params", req.getParams().toNamedList());
      }
    }
  }


  final public static void log(Throwable e) {
    SolrException.logOnce(log,null,e);
  }

  
  
  private QueryResponseWriter defaultResponseWriter;
  private final Map<String, QueryResponseWriter> responseWriters = new HashMap<String, QueryResponseWriter>();
  public static final Map<String ,QueryResponseWriter> DEFAULT_RESPONSE_WRITERS ;
  static{
    HashMap<String, QueryResponseWriter> m= new HashMap<String, QueryResponseWriter>();
    m.put("xml", new XMLResponseWriter());
    m.put("standard", m.get("xml"));
    m.put("json", new JSONResponseWriter());
//    m.put("python", new PythonResponseWriter());
//    m.put("php", new PHPResponseWriter());
//    m.put("phps", new PHPSerializedResponseWriter());
//    m.put("ruby", new RubyResponseWriter());
    m.put("raw", new RawResponseWriter());
    m.put("javabin", new BinaryResponseWriter());
    m.put("csv", new CSVResponseWriter());
    DEFAULT_RESPONSE_WRITERS = Collections.unmodifiableMap(m);
  }
  
  /** Configure the query response writers. There will always be a default writer; additional
   * writers may also be configured. */
  private void initWriters() {
    defaultResponseWriter = initPlugins(responseWriters, QueryResponseWriter.class);
    for (Map.Entry<String, QueryResponseWriter> entry : DEFAULT_RESPONSE_WRITERS.entrySet()) {
      if(responseWriters.get(entry.getKey()) == null) responseWriters.put(entry.getKey(), entry.getValue());
    }
    
    // configure the default response writer; this one should never be null
    if (defaultResponseWriter == null) {
      defaultResponseWriter = responseWriters.get("standard");
    }

  }
  
  /** Finds a writer by name, or returns the default writer if not found. */
  public final QueryResponseWriter getQueryResponseWriter(String writerName) {
    if (writerName != null) {
        QueryResponseWriter writer = responseWriters.get(writerName);
        if (writer != null) {
            return writer;
        }
    }
    return defaultResponseWriter;
  }

  /** Returns the appropriate writer for a request. If the request specifies a writer via the
   * 'wt' parameter, attempts to find that one; otherwise return the default writer.
   */
  public final QueryResponseWriter getQueryResponseWriter(SolrQueryRequest request) {
    return getQueryResponseWriter(request.getParams().get(CommonParams.WT)); 
  }

  private final Map<String, QParserPlugin> qParserPlugins = new HashMap<String, QParserPlugin>();

  /** Configure the query parsers. */
  private void initQParsers() {
    initPlugins(qParserPlugins,QParserPlugin.class);
    // default parsers
    for (int i=0; i<QParserPlugin.standardPlugins.length; i+=2) {
     try {
       String name = (String)QParserPlugin.standardPlugins[i];
       if (null == qParserPlugins.get(name)) {
         Class<QParserPlugin> clazz = (Class<QParserPlugin>)QParserPlugin.standardPlugins[i+1];
         QParserPlugin plugin = clazz.newInstance();
         qParserPlugins.put(name, plugin);
         plugin.init(null);
       }
     } catch (Exception e) {
       throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
     }
    }
  }

  public QParserPlugin getQueryPlugin(String parserName) {
    QParserPlugin plugin = qParserPlugins.get(parserName);
    if (plugin != null) return plugin;
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unknown query type '"+parserName+"'");
  }
  
  private final HashMap<String, ValueSourceParser> valueSourceParsers = new HashMap<String, ValueSourceParser>();
  
  /** Configure the ValueSource (function) plugins */
  private void initValueSourceParsers() {
    initPlugins(valueSourceParsers,ValueSourceParser.class);
    // default value source parsers
    for (Map.Entry<String, ValueSourceParser> entry : ValueSourceParser.standardValueSourceParsers.entrySet()) {
      try {
        String name = entry.getKey();
        if (null == valueSourceParsers.get(name)) {
          ValueSourceParser valueSourceParser = entry.getValue();
          valueSourceParsers.put(name, valueSourceParser);
          valueSourceParser.init(null);
        }
      } catch (Exception e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      }
    }
  }

  /**
   * @param registry The map to which the instance should be added to. The key is the name attribute
   * @param type the class or interface that the instance should extend or implement.
   * @param defClassName If PluginInfo does not have a classname, use this as the classname
   * @return The default instance . The one with (default=true)
   */
  public <T> T initPlugins(Map<String ,T> registry, Class<T> type, String defClassName){
    return initPlugins(solrConfig.getPluginInfos(type.getName()), registry, type, defClassName);
  }

  public <T> T initPlugins(List<PluginInfo> pluginInfos, Map<String, T> registry, Class<T> type, String defClassName) {
    T def = null;
    for (PluginInfo info : pluginInfos) {
      T o = createInitInstance(info,type, type.getSimpleName(), defClassName);
      registry.put(info.name, o);
      if(info.isDefault()){
        def = o;
      }
    }
    return def;
  }

  /**For a given List of PluginInfo return the instances as a List
   * @param defClassName The default classname if PluginInfo#className == null
   * @return The instances initialized
   */
  public <T> List<T> initPlugins(List<PluginInfo> pluginInfos, Class<T> type, String defClassName) {
    if(pluginInfos.isEmpty()) return Collections.emptyList();
    List<T> result = new ArrayList<T>();
    for (PluginInfo info : pluginInfos) result.add(createInitInstance(info,type, type.getSimpleName(), defClassName));
    return result;
  }


  public <T> T initPlugins(Map<String, T> registry, Class<T> type) {
    return initPlugins(registry, type, null);
  }

  public ValueSourceParser getValueSourceParser(String parserName) {
    return valueSourceParsers.get(parserName);
  }
  

  public CoreDescriptor getCoreDescriptor() {
    return coreDescriptor;
  }

  public IndexDeletionPolicyWrapper getDeletionPolicy(){
    return solrDelPolicy;
  }

  /////////////////////////////////////////////////////////////////////
  // SolrInfoMBean stuff: Statistics and Module Info
  /////////////////////////////////////////////////////////////////////

  public String getVersion() {
    return SolrCore.version;
  }

  public String getDescription() {
    return "SolrCore";
  }

  public Category getCategory() {
    return Category.CORE;
  }

  public String getSourceId() {
    return "$Id: SolrCore.java 1190108 2011-10-28 01:13:25Z yonik $";
  }

  public String getSource() {
    return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_5/solr/core/src/java/org/apache/solr/core/SolrCore.java $";
  }

  public URL[] getDocs() {
    return null;
  }

  public NamedList getStatistics() {
    NamedList lst = new SimpleOrderedMap();
    lst.add("coreName", name==null ? "(null)" : name);
    lst.add("startTime", new Date(startTime));
    lst.add("refCount", getOpenCount());
//    lst.add("aliases", getCoreDescriptor().getCoreContainer().getCoreNames(this));
    return lst;
  }

}



