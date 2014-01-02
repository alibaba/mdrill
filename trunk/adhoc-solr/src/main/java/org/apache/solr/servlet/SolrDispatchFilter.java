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

package org.apache.solr.servlet;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.FastWriter;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.core.*;
import org.apache.solr.request.*;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.servlet.cache.HttpCacheHeaderUtil;
import org.apache.solr.servlet.cache.Method;



public class SolrDispatchFilter implements Filter
{
  final Logger log = LoggerFactory.getLogger(SolrDispatchFilter.class);

  protected CoreContainer cores;
  protected String abortErrorMessage = null;
  protected final Map<SolrConfig, SolrRequestParsers> parsers = new WeakHashMap<SolrConfig, SolrRequestParsers>();
  
  private static final Charset UTF8 = Charset.forName("UTF-8");


  public void init(FilterConfig config) throws ServletException
  {
    log.info("SolrDispatchFilter.init()");

    boolean abortOnConfigurationError = true;
    CoreContainer.Initializer init = new CoreContainer.Initializer();
    try {

      this.cores = init.initialize();
      abortOnConfigurationError = init.isAbortOnConfigurationError();
    }
    catch( Throwable t ) {
      log.error( "Could not start Solr. Check solr/home property", t);
      SolrConfig.severeErrors.add( t );
      SolrCore.log( t );
      CoreContainer.setException();
      
    }

    if( abortOnConfigurationError && SolrConfig.severeErrors.size() > 0 ) {
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter( sw );
      out.println( "Severe errors in solr configuration.\n" );
      for( Throwable t : SolrConfig.severeErrors ) {
        out.println( "-------------------------------------------------------------" );
        t.printStackTrace( out );
      }
      out.flush();

      abortErrorMessage = sw.toString();
    }

    log.info("SolrDispatchFilter.init() done");
  }

  
  public void destroy() {
    if (cores != null) {
      cores.shutdown();
      cores = null;
    }    
  }
  
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if( abortErrorMessage != null ) {
      ((HttpServletResponse)response).sendError( 500, abortErrorMessage );
      return;
    }
    
    if( request instanceof HttpServletRequest) {
      HttpServletRequest req = (HttpServletRequest)request;
      HttpServletResponse resp = (HttpServletResponse)response;
      SolrRequestHandler handler = null;
      SolrQueryRequest solrReq = null;
      SolrCore core = null;
      String corename = "";
      
      try {
        // put the core container in request attribute
        req.setAttribute("org.apache.solr.CoreContainer", cores);
        String path = req.getServletPath();
        if( req.getPathInfo() != null ) {
          path += req.getPathInfo();
        }
        // unused feature ?
        int idx = path.indexOf( ':' );
        if( idx > 0 ) {
          path = path.substring( 0, idx );
        }

          idx = path.indexOf( "/", 1 );
          if( idx > 1 ) {
            // try to get the corename as a request parameter first
            corename = path.substring( 1, idx );
            core = cores.getCore(corename);
            if (core != null) {
              path = path.substring( idx );
            }
          }
          if (core == null) {
            corename = "";
            core = cores.getCore("");
          }

        // With a valid core...
        if( core != null ) {
          final SolrConfig config = core.getSolrConfig();
          SolrRequestParsers parser = null;
          parser = parsers.get(config);
          if( parser == null ) {
            parser = new SolrRequestParsers(config);
            parsers.put(config, parser );
          }

          if( handler == null && path.length() > 1 ) { // don't match "" or "/" as valid path
            handler = core.getRequestHandler( path );
            if( handler == null && parser.isHandleSelect() ) {
              if( "/select".equals( path ) || "/select/".equals( path ) ) {
                solrReq = parser.parse( core, path, req );
                String qt = solrReq.getParams().get( CommonParams.QT );
                handler = core.getRequestHandler( qt );
                if( handler == null ) {
                  throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "unknown handler: "+qt);
                }
              }
            }
          }

          if( handler != null ) {
            if( solrReq == null ) {
              solrReq = parser.parse( core, path, req );
            }

            final Method reqMethod = Method.getMethod(req.getMethod());
            HttpCacheHeaderUtil.setCacheControlHeader(config, resp, reqMethod);
            if (config.getHttpCachingConfig().isNever304() ||
                !HttpCacheHeaderUtil.doCacheHeaderValidation(solrReq, req, reqMethod, resp)) {
                SolrQueryResponse solrRsp = new SolrQueryResponse();
                this.execute( req, handler, solrReq, solrRsp );
                HttpCacheHeaderUtil.checkHttpCachingVeto(solrRsp, resp, reqMethod);
               QueryResponseWriter responseWriter = core.getQueryResponseWriter(solrReq);
              writeResponse(solrRsp, response, responseWriter, solrReq, reqMethod);
            }
            return;
          }
    
        }
        log.debug("no handler or core retrieved for " + path + ", follow through...");
      } 
      catch (Throwable ex) {
        sendError( (HttpServletResponse)response, ex );
        return;
      } 
      finally {
        if( solrReq != null ) {
          solrReq.close();
        }
        if (core != null) {
          core.close();
        }
        
      }
    }

    // Otherwise let the webapp handle the request
    chain.doFilter(request, response);
  }


  private void writeResponse(SolrQueryResponse solrRsp, ServletResponse response,
                             QueryResponseWriter responseWriter, SolrQueryRequest solrReq, Method reqMethod)
          throws IOException {
    if (solrRsp.getException() != null) {
      sendError((HttpServletResponse) response, solrRsp.getException());
    } else {
      // Now write it out
      final String ct = responseWriter.getContentType(solrReq, solrRsp);
      // don't call setContentType on null
      if (null != ct) response.setContentType(ct); 

      if (Method.HEAD != reqMethod) {
        if (responseWriter instanceof BinaryQueryResponseWriter) {
          BinaryQueryResponseWriter binWriter = (BinaryQueryResponseWriter) responseWriter;
          binWriter.write(response.getOutputStream(), solrReq, solrRsp);
        } else {
          String charset = ContentStreamBase.getCharsetFromContentType(ct);
          Writer out = (charset == null || charset.equalsIgnoreCase("UTF-8"))
            ? new OutputStreamWriter(response.getOutputStream(), UTF8)
            : new OutputStreamWriter(response.getOutputStream(), charset);
          out = new FastWriter(out);
          responseWriter.write(out, solrReq, solrRsp);
          out.flush();
        }
      }
      //else http HEAD request, nothing to write out, waited this long just to get ContentType
    }
  }

  protected void execute( HttpServletRequest req, SolrRequestHandler handler, SolrQueryRequest sreq, SolrQueryResponse rsp) {
    // a custom filter could add more stuff to the request before passing it on.
    // for example: sreq.getContext().put( "HttpServletRequest", req );
    // used for logging query stats in SolrCore.execute()
    sreq.getContext().put( "webapp", req.getContextPath() );
    sreq.getCore().execute( handler, sreq, rsp );
  }

  protected void sendError(HttpServletResponse res, Throwable ex) throws IOException {
    int code=500;
    String trace = "";
    if( ex instanceof SolrException ) {
      code = ((SolrException)ex).code();
    }

    // For any regular code, don't include the stack trace
    if( code == 500 || code < 100 ) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      trace = "\n\n"+sw.toString();

      SolrException.logOnce(log,null,ex );

      // non standard codes have undefined results with various servers
      if( code < 100 ) {
        log.warn( "invalid return code: "+code );
        code = 500;
      }
    }
    res.sendError( code, ex.getMessage() + trace );
  }


}
