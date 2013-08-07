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

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.DOMUtil;
import org.apache.solr.schema.IndexSchema;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;


/**
 * @version $Id: CoreContainer.java 1095128 2011-04-19 16:26:01Z markrmiller $
 * @since solr 1.3
 */
public class CoreContainer 
{
  private static final String DEFAULT_DEFAULT_CORE_NAME = "collection1";
  protected static Logger log = LoggerFactory.getLogger(CoreContainer.class);
  protected final Map<String, SolrCore> cores = new LinkedHashMap<String, SolrCore>();
  protected SolrResourceLoader loader = null;
  protected Properties containerProperties;
  protected String solrHome;
  private String defaultCoreName = "";
  private boolean defaultAbortOnConfigError = false;
  private int numCoresAbortOnConfigError = 0;
  
  private static AtomicBoolean isException=new AtomicBoolean(false);
  
  public static boolean getIsException() {
	return isException.get();
}
  
  public static  void setException()
  {
	  isException.set(true);
  }

public CoreContainer() {
    solrHome = SolrResourceLoader.locateSolrHome();
    log.info("New CoreContainer: solrHome=" + solrHome + " instance="+System.identityHashCode(this));
  }

  public Properties getContainerProperties() {
    return containerProperties;
  }

  public static class Initializer {
    protected boolean abortOnConfigurationError = true;
    public boolean isAbortOnConfigurationError() {
      return abortOnConfigurationError;
    }
    
    public void setAbortOnConfigurationError(boolean abortOnConfigurationError) {
      this.abortOnConfigurationError = abortOnConfigurationError;
    }

    public CoreContainer initialize() throws IOException,
        ParserConfigurationException, SAXException {
      CoreContainer cores = null;
      String solrHome = SolrResourceLoader.locateSolrHome();
      File fconf = new File(solrHome, "solr.xml");
      log.info("looking for solr.xml: " + fconf.getAbsolutePath());
      cores = new CoreContainer();
      if (fconf.exists()) {
        cores.defaultAbortOnConfigError = false;
        cores.load(solrHome, new InputSource(fconf.toURI().toASCIIString()));
      } else {
        cores.defaultAbortOnConfigError = abortOnConfigurationError;
        cores.load(solrHome, new InputSource(new ByteArrayInputStream(DEF_SOLR_XML.getBytes("UTF-8"))));
      }
      setAbortOnConfigurationError(0 < cores.numCoresAbortOnConfigError);      
      return cores;
    }
  }
  
  public void load(String dir, InputSource cfgis)
      throws ParserConfigurationException, IOException, SAXException {
    this.loader = new SolrResourceLoader(dir);
    this.solrHome = loader.getInstanceDir();
    Config cfg = new Config(loader, null, cfgis, null);

    try {
      containerProperties = readProperties(cfg, ((NodeList) cfg.evaluate("solr", XPathConstants.NODESET)).item(0));
    } catch (Throwable e) {
      SolrConfig.severeErrors.add(e);
      SolrException.logOnce(log,null,e);
    }
  }
  
  private Properties readProperties(Config cfg, Node node) throws XPathExpressionException {
	    XPath xpath = cfg.getXPath();
	    NodeList props = (NodeList) xpath.evaluate("property", node, XPathConstants.NODESET);
	    Properties properties = new Properties();
	    for (int i=0; i<props.getLength(); i++) {
	      Node prop = props.item(i);
	      properties.setProperty(DOMUtil.getAttr(prop, "name"), DOMUtil.getAttr(prop, "value"));
	    }
	    return properties;
	  }



  public SolrCore getCore(String name) {
		name = checkDefault(name);
		 if( name == null ||
	        name.indexOf( '/'  ) >= 0 ||
	        name.indexOf( '\\' ) >= 0 ){
	      throw new RuntimeException( "Invalid core name: "+name );
	    }
		synchronized (cores) {
			SolrCore core = cores.get(name);
			if (core == null) {
				String instanceDir=SolrResourceLoader.locateSolrHome();
				String schema = instanceDir+ "/conf/schema.xml";
				String datadir = instanceDir + "/data/";
				if(!name.isEmpty())
				{
					instanceDir=SolrResourceLoader.GetSchemaHome() + "/" + name	+ "/solr";
					schema = SolrResourceLoader.GetSchemaHome() + "/" + name	+ "/solr/conf/schema.xml";
					datadir = SolrResourceLoader.GetSchemaHome() + "/" + name + "/solr/data/";
				}
				
				log.info("higolog getCore"+schema+","+datadir);
				try {
					this.createCore(name, instanceDir, schema,datadir);
				} catch (Exception e) {
					isException.set(true);
					log.error("getCore",e);
				}
				core = cores.get(name);
			}
			if (core != null) {
				core.open(); // increment the ref count while still synchronized
			}
			return core;
		}
	}
  
  public void createCore(String name,String instanceDir,String schema,String datadir) throws ParserConfigurationException, IOException, SAXException
  {
	  CoreDescriptor p = new CoreDescriptor(this, name, instanceDir);
	  p.setSchemaName(schema);
	  p.setDataDir(datadir);
      p.setCoreProperties( new Properties());
      
      SolrResourceLoader solrLoader = new SolrResourceLoader(p.getInstanceDir(), null, p.getCoreProperties());
      SolrConfig sconfig = new SolrConfig(solrLoader, p.getConfigName(), null);

      if (sconfig.getBool("abortOnConfigurationError",defaultAbortOnConfigError)) {
        numCoresAbortOnConfigError++;
      }
      
      IndexSchema ischema = new IndexSchema(sconfig, p.getSchemaName(), null);
      SolrCore core = new SolrCore(p.getName(), p.getDataDir(), sconfig, ischema, p);
      core.setName(name);
      core.getCoreDescriptor().name = name;
      
      SolrCore old = cores.put(name, core);
      if( old == null || old == core) {
        log.info( "registering core: "+name );
      }else {
        log.info( "replacing core: "+name );
        old.close();
      }
  }


  private boolean isShutDown = false;
  /**
   * Stops all cores.
   */
  public void shutdown() {
    synchronized(cores) {
      try {
        for(SolrCore core : cores.values()) {
          core.close();
        }
        cores.clear();
      } finally {
        isShutDown = true;
      }
    }
  }
  
  @Override
  protected void finalize() throws Throwable {
    try {
      if(!isShutDown){
        log.error("CoreContainer was not shutdown prior to finalize(), indicates a bug -- POSSIBLE RESOURCE LEAK!!!  instance=" + System.identityHashCode(this));
        shutdown();
      }
    } finally {
      super.finalize();
    }
  }



  

	private String checkDefault(String name) {
	    return name.length() == 0  || defaultCoreName.equals(name) || name.trim().length() == 0 ? "" : name;
	} 
  
	public String getSolrHome() {
		return solrHome;
	}
	
	  private static final String DEF_SOLR_XML ="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
	  "<solr persistent=\"false\">\n" +
	  "  <cores adminPath=\"/admin/cores\" defaultCoreName=\"" + DEFAULT_DEFAULT_CORE_NAME + "\">\n" +
	  "    <core name=\""+ DEFAULT_DEFAULT_CORE_NAME + "\" instanceDir=\".\" />\n" +
	  "  </cores>\n" +
	  "</solr>";


//private static Properties getCoreProps(String instanceDir, String file, Properties defaults) {
//if(file == null) file = "conf"+File.separator+ "solrcore.properties";
//File corePropsFile = new File(file);
//if(!corePropsFile.isAbsolute()){
//  corePropsFile = new File(instanceDir, file);
//}
//Properties p = defaults;
//if (corePropsFile.exists() && corePropsFile.isFile()) {
//  p = new Properties(defaults);
//  InputStream is = null;
//  try {
//    is = new FileInputStream(corePropsFile);
//    p.load(is);
//  } catch (IOException e) {
//    log.warn("Error loading properties ",e);
//  } finally{
//    IOUtils.closeQuietly(is);        
//  }
//}
//return p;
//}
    
//  /**
//   * @return a Collection of registered SolrCores
//   */
//  public Collection<SolrCore> getCores() {
//    List<SolrCore> lst = new ArrayList<SolrCore>();
//    synchronized (cores) {
//      lst.addAll(this.cores.values());
//    }
//    return lst;
//  }

//  /**
//   * @return a Collection of the names that cores are mapped to
//   */
//  public Collection<String> getCoreNames() {
//    List<String> lst = new ArrayList<String>();
//    synchronized (cores) {
//      lst.addAll(this.cores.keySet());
//    }
//    return lst;
//  }

//  /** This method is currently experimental.
//   * @return a Collection of the names that a specific core is mapped to.
//   */
//  public Collection<String> getCoreNames(SolrCore core) {
//    List<String> lst = new ArrayList<String>();
//    synchronized (cores) {
//      for (Map.Entry<String,SolrCore> entry : cores.entrySet()) {
//        if (core == entry.getValue()) {
//          lst.add(entry.getKey());
//        }
//      }
//    }
//    return lst;
//  }

  


//	public String getManagementPath() {
//		return managementPath;
//	}

//	public File getConfigFile() {
//		return configFile;
//	}


	  
	  
	  
	  

	//  /**
	//   * Initalize CoreContainer directly from the constructor
	//   * 
	//   * @param dir
	//   * @param configFile
	//   * @throws ParserConfigurationException
	//   * @throws IOException
	//   * @throws SAXException
	//   */
	//  public CoreContainer(String dir, File configFile) throws ParserConfigurationException, IOException, SAXException 
	//  {
//	    this.load(dir, configFile);
	//  }
	  
	//  /**
	//   * Minimal CoreContainer constructor. 
	//   * @param loader the CoreContainer resource loader
	//   */
	//  public CoreContainer(SolrResourceLoader loader) {
//	    this.loader = loader;
//	    this.solrHome = loader.getInstanceDir();
	//  }

	//  public CoreContainer(String solrHome) {
//	    this.solrHome = solrHome;
	//  }

	  //-------------------------------------------------------------------
	  // Initialization / Cleanup
	  //-------------------------------------------------------------------
	  

	//---------------- Core name related methods --------------- 
	  /**
	   * Recreates a SolrCore.
	   * While the new core is loading, requests will continue to be dispatched to
	   * and processed by the old core
	   * 
	   * @param name the name of the SolrCore to reload
	   * @throws ParserConfigurationException
	   * @throws IOException
	   * @throws SAXException
	   */

	//  public void reload(String name) throws ParserConfigurationException, IOException, SAXException {
//	    name= checkDefault(name);
//	    SolrCore core;
//	    synchronized(cores) {
//	      core = cores.get(name);
//	    }
//	    if (core == null)
//	      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "No such core: " + name );
	//
//	    SolrCore newCore = create(core.getCoreDescriptor());
//	    register(name, newCore, false);
	//  }

	//  /**
	//   * Swaps two SolrCore descriptors.
	//   * @param n0
	//   * @param n1
	//   */
	//  public void swap(String n0, String n1) {
//	    if( n0 == null || n1 == null ) {
//	      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Can not swap unnamed cores." );
//	    }
//	    n0 = checkDefault(n0);
//	    n1 = checkDefault(n1);
//	    synchronized( cores ) {
//	      SolrCore c0 = cores.get(n0);
//	      SolrCore c1 = cores.get(n1);
//	      if (c0 == null)
//	        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n0 );
//	      if (c1 == null)
//	        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n1 );
//	      cores.put(n0, c1);
//	      cores.put(n1, c0);
	//
//	      c0.setName(n1);
//	      c0.getCoreDescriptor().name = n1;
//	      c1.setName(n0);
//	      c1.getCoreDescriptor().name = n0;
//	    }
	//
	//
//	    log.info("swaped: "+n0 + " with " + n1);
	//  }
	  
	//  /** Removes and returns registered core w/o decrementing it's reference count */
	//  public SolrCore remove( String name ) {
//	    name = checkDefault(name);    
//	    synchronized(cores) {
//	      return cores.remove( name );
//	    }
	//  }

//		public String getDefaultCoreName() {
//			return defaultCoreName;
//		}
  
//  // all of the following properties aren't synchronized
//  // but this should be OK since they normally won't be changed rapidly
//  public boolean isPersistent() {
//    return persistent;
//  }
  
//  public void setPersistent(boolean persistent) {
//    this.persistent = persistent;
//  }
  
//  public String getAdminPath() {
//    return adminPath;
//  }
//  
//  public void setAdminPath(String adminPath) {
//      this.adminPath = adminPath;
//  }
  

  
//  /**
//   * Sets the alternate path for multicore handling:
//   * This is used in case there is a registered unnamed core (aka name is "") to
//   * declare an alternate way of accessing named cores.
//   * This can also be used in a pseudo single-core environment so admins can prepare
//   * a new version before swapping.
//   * @param path
//   */
//  public void setManagementPath(String path) {
//    this.managementPath = path;
//  }
 
  
///** Persists the cores config file in cores.xml. */
//  public void persist() {
//    persistFile(null);
//  }

//  /** Persists the cores config file in a user provided file. */
//  public void persistFile(File file) {
//    log.info("Persisting cores config to " + (file==null ? configFile : file));
//
//    File tmpFile = null;
//    try {
//      // write in temp first
//      if (file == null) {
//        file = tmpFile = File.createTempFile("solr", ".xml", configFile.getParentFile());
//      }
//      java.io.FileOutputStream out = new java.io.FileOutputStream(file);
//        Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
//        persist(writer);
//        writer.flush();
//        writer.close();
//        out.close();
//        // rename over origin or copy it this fails
//        if (tmpFile != null) {
//          if (tmpFile.renameTo(configFile))
//            tmpFile = null;
//          else
//            fileCopy(tmpFile, configFile);
//        }
//    } 
//    catch(java.io.FileNotFoundException xnf) {
//      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, xnf);
//    } 
//    catch(java.io.IOException xio) {
//      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, xio);
//    } 
//    finally {
//      if (tmpFile != null) {
//        if (!tmpFile.delete())
//          tmpFile.deleteOnExit();
//      }
//    }
//  }
//  
//  /** Write the cores configuration through a writer.*/
//  void persist(Writer w) throws IOException {
//    w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
//    w.write("<solr");
//    if (this.libDir != null) {
//      writeAttribute(w,"sharedLib",libDir);
//    }
//    writeAttribute(w,"persistent",isPersistent());
//    w.write(">\n");
//
//    if (containerProperties != null && !containerProperties.isEmpty())  {
//      writeProperties(w, containerProperties, "  ");
//    }
//    w.write("  <cores");
//    writeAttribute(w, "adminPath",adminPath);
//    if(adminHandler != null) writeAttribute(w, "adminHandler",adminHandler);
//    if(shareSchema) writeAttribute(w, "shareSchema","true");
//    if(!defaultCoreName.equals("")) writeAttribute(w, "defaultCoreName", defaultCoreName);
//    w.write(">\n");
//
//    synchronized(cores) {
//      for (SolrCore solrCore : cores.values()) {
//        persist(w,solrCore.getCoreDescriptor());
//      }
//    }
//
//    w.write("  </cores>\n");
//    w.write("</solr>\n");
//  }
//
//  private void writeAttribute(Writer w, String name, Object value) throws IOException {
//    if (value == null) return;
//    w.write(" ");
//    w.write(name);
//    w.write("=\"");
//    XML.escapeAttributeValue(value.toString(), w);
//    w.write("\"");
//  }
//  
//  /** Writes the cores configuration node for a given core. */
//  void persist(Writer w, CoreDescriptor dcore) throws IOException {
//    w.write("    <core");
//    writeAttribute(w,"name",dcore.name.equals("") ? defaultCoreName : dcore.name);
//    writeAttribute(w,"instanceDir",dcore.getInstanceDir());
//    //write config (if not default)
//    String opt = dcore.getConfigName();
//    if (opt != null && !opt.equals(dcore.getDefaultConfigName())) {
//      writeAttribute(w, "config",opt);
//    }
//    //write schema (if not default)
//    opt = dcore.getSchemaName();
//    if (opt != null && !opt.equals(dcore.getDefaultSchemaName())) {
//      writeAttribute(w,"schema",opt);
//    }
//    opt = dcore.getPropertiesName();
//    if (opt != null) {
//      writeAttribute(w,"properties",opt);
//    }
//    opt = dcore.dataDir;
//    if (opt != null) writeAttribute(w,"dataDir",opt);
//    if (dcore.getCoreProperties() == null || dcore.getCoreProperties().isEmpty())
//      w.write("/>\n"); // core
//    else  {
//      w.write(">\n");
//      writeProperties(w, dcore.getCoreProperties(), "      ");
//      w.write("    </core>\n");
//    }
//  }
//
//  private void writeProperties(Writer w, Properties props, String indent) throws IOException {
//    for (Map.Entry<Object, Object> entry : props.entrySet()) {
//      w.write(indent + "<property");
//      writeAttribute(w,"name",entry.getKey());
//      writeAttribute(w,"value",entry.getValue());
//      w.write("/>\n");
//    }
//  }
//
//  /** Copies a src file to a dest file:
//   *  used to circumvent the platform discrepancies regarding renaming files.
//   */
//  public static void fileCopy(File src, File dest) throws IOException {
//    IOException xforward = null;
//    FileInputStream fis =  null;
//    FileOutputStream fos = null;
//    FileChannel fcin = null;
//    FileChannel fcout = null;
//    try {
//      fis = new FileInputStream(src);
//      fos = new FileOutputStream(dest);
//      fcin = fis.getChannel();
//      fcout = fos.getChannel();
//      // do the file copy 32Mb at a time
//      final int MB32 = 32*1024*1024;
//      long size = fcin.size();
//      long position = 0;
//      while (position < size) {
//        position += fcin.transferTo(position, MB32, fcout);
//      }
//    } 
//    catch(IOException xio) {
//      xforward = xio;
//    } 
//    finally {
//      if (fis   != null) try { fis.close(); fis = null; } catch(IOException xio) {}
//      if (fos   != null) try { fos.close(); fos = null; } catch(IOException xio) {}
//      if (fcin  != null && fcin.isOpen() ) try { fcin.close();  fcin = null;  } catch(IOException xio) {}
//      if (fcout != null && fcout.isOpen()) try { fcout.close(); fcout = null; } catch(IOException xio) {}
//    }
//    if (xforward != null) {
//      throw xforward;
//    }
//  }


  // ---------------- Multicore self related methods ---------------
  /** 
   * Creates a CoreAdminHandler for this MultiCore.
   * @return a CoreAdminHandler
   */
//  protected CoreAdminHandler createMultiCoreHandler(final String adminHandlerClass) {
//    SolrResourceLoader loader = new SolrResourceLoader(null, libLoader, null);
//    Object obj = loader.newAdminHandlerInstance(CoreContainer.this, adminHandlerClass);
//    if ( !(obj instanceof CoreAdminHandler))
//    {
//      throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
//          "adminHandlerClass is not of type "+ CoreAdminHandler.class );
//      
//    }
//    return (CoreAdminHandler) obj;
//  }

//  public CoreAdminHandler getMultiCoreHandler() {
//    return coreAdminHandler;
//  }
 
}
