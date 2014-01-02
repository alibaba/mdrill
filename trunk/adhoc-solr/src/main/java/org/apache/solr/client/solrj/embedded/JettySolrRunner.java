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

package org.apache.solr.client.solrj.embedded;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.log.Logger;
import org.mortbay.thread.QueuedThreadPool;


public class JettySolrRunner 
{

  Server server;
  FilterHolder dispatchFilter;
  String context;
  

  public JettySolrRunner( String context, int port )
  {
    this.init( context, port );
  }
  
  private void init( String context, int port )
  {
    System.setProperty("org.mortbay.jetty.Request.maxFormContentSize", "9000000");
    System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "9000000");

    this.context = context;
    server = new Server(  );    
   
    QueuedThreadPool threads=new QueuedThreadPool(512);
    threads.setDaemon(true);

    SelectChannelConnector conn=new SelectChannelConnector();
    conn.setPort(port);
    conn.setLowResourcesConnections(10240);
    conn.setMaxIdleTime(3600000);
    conn.setLowResourceMaxIdleTime(600000);
    server.setThreadPool(threads);
    server.setConnectors(new Connector[] { conn });
    server.setStopAtShutdown( true );
    
    // Initialize the servlets
    Context root = new Context( server, context, Context.SESSIONS );
    
    root.addServlet( Servlet404.class, "/*" );
    dispatchFilter = root.addFilter( SolrDispatchFilter.class, "*", Handler.REQUEST );
  }

  //------------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------------
  
  public void start() throws Exception
  {
    start(true);
  }

  public void start(boolean waitForSolr) throws Exception
  {
    if(!server.isRunning() ) {
      server.start();
    }
    if (waitForSolr) waitForSolr(context);
  }


  public void stop() throws Exception
  {
    if( server.isRunning() ) {
      server.stop();
      server.join();
    }
  }

  /** Waits until a ping query to the solr server succeeds,
   * retrying every 200 milliseconds up to 2 minutes.
   */
  public void waitForSolr(String context) throws Exception
  {
    int port = getLocalPort();

    // A raw term query type doesn't check the schema
    URL url = new URL("http://localhost:"+port+context+"/select?q={!raw+f=junit_test_query}ping");

    Exception ex = null;
    // Wait for a total of 20 seconds: 100 tries, 200 milliseconds each
    for (int i=0; i<600; i++) {
      try {
        InputStream stream = url.openStream();
        stream.close();
      } catch (IOException e) {
        // e.printStackTrace();
        ex = e;
        Thread.sleep(200);
        continue;
      }

      return;
    }

    throw new RuntimeException("Jetty/Solr unresponsive",ex);
  }
  
	public void checksolr(String context) throws Exception {
		int port = getLocalPort();
		URL url = new URL("http://localhost:" + port + context
				+ "/select?q={!raw+f=junit_test_query}ping");
		try {
			InputStream stream = url.openStream();
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException("Jetty/Solr unresponsive", e);
		}

	}
	
	public static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");

	public Long checkSolrRecord(String context, String newestPartion,String day)
			throws MalformedURLException, SolrServerException {
		
		SolrServerException error = new SolrServerException("checkSolrRecord");
		for (int i = 0; i < 3; i++) {
			try {
				int port = getLocalPort();
				CommonsHttpSolrServer server = new CommonsHttpSolrServer(
						"http://localhost:" + port + context);
				server.setConnectionManagerTimeout(60000000l);
				server.setSoTimeout(60000000);
			    server.setConnectionTimeout(100000);
			    server.setDefaultMaxConnectionsPerHost(100);
			    server.setMaxTotalConnections(100);
			    server.setFollowRedirects(false);
			    server.setAllowCompression(true);
			    server.setMaxRetries(1);
				server.setRequestWriter(new BinaryRequestWriter());
				SolrQuery query = new SolrQuery();
				query.setParam("start", "0");
				if (newestPartion != null && !newestPartion.isEmpty()) {
					query.setParam(CommonParams.PARTION, newestPartion);
				}

				
				if(day!=null&&!day.isEmpty()&&!day.equals(newestPartion))
				{
					query.addFilterQuery("thedate:"+day);
				}
				query.setParam(CommonParams.HIGOHB,true);
				query.setParam("mlogtime", fmt.format(new Date()));

				query.setParam("rows", "0");
				query.setQuery("*:*");
				QueryResponse qr3 = server.query(query);
				SolrDocumentList result3 = qr3.getResults();
				if(result3!=null)
				{
					return result3.getNumFound();
				}else{
					SolrCore.log.info("checkSolrRecord result is null "+
						"http://localhost:" + port + context+"?"+query.toString());
				}
			} catch (SolrServerException e) {
				error = e;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		throw error;
	}

  /**
   * Returns the Local Port of the first Connector found for the jetty Server.
   * @exception RuntimeException if there is no Connector
   */
  public int getLocalPort() {
    Connector[] conns = server.getConnectors();
    if (0 == conns.length) {
      throw new RuntimeException("Jetty Server has no Connectors");
    }
    return conns[0].getLocalPort();
  }

  //--------------------------------------------------------------
  //--------------------------------------------------------------
    
  /** 
   * This is a stupid hack to give jetty something to attach to
   */
  public static class Servlet404 extends HttpServlet
  {
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res ) throws IOException
    {
      res.sendError( 404, "Can not find: "+req.getRequestURI() );
    }
  }
  
  /**
   * A main class that starts jetty+solr 
   * This is useful for debugging
   */
  public static void main( String[] args )
  {
    try {
      JettySolrRunner jetty = new JettySolrRunner( "/solr", 3456 );
      jetty.start();
//      jetty.stop();
      Thread.sleep(5000);
    }
    catch( Exception ex ) {
      ex.printStackTrace();
    }
  }
}


class NoLog implements Logger
{    
  private static boolean debug = System.getProperty("DEBUG",null)!=null;
  private final String name;
      
  public NoLog()
  {
    this(null);
  }
  
  public NoLog(String name)
  {    
    this.name=name==null?"":name;
  }
  
  public boolean isDebugEnabled()
  {
    return debug;
  }
  
  public void setDebugEnabled(boolean enabled)
  {
    debug=enabled;
  }
  
  public void info(String msg,Object arg0, Object arg1) {}
  public void debug(String msg,Throwable th){}
  public void debug(String msg,Object arg0, Object arg1){}
  public void warn(String msg,Object arg0, Object arg1){}
  public void warn(String msg, Throwable th){}

  public Logger getLogger(String name)
  {
    if ((name==null && this.name==null) ||
      (name!=null && name.equals(this.name)))
      return this;
    return new NoLog(name);
  }
  
  @Override
  public String toString()
  {
    return "NOLOG["+name+"]";
  }
}
