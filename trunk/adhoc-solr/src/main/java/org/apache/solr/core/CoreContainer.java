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


}
