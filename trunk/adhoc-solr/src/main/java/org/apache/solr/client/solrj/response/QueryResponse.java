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

package org.apache.solr.client.solrj.response;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.*;

/**
 * 
 * @version $Id: QueryResponse.java 1180378 2011-10-08 14:14:08Z mvg $
 * @since solr 1.3
 */
@SuppressWarnings("unchecked")
public class QueryResponse extends SolrResponseBase 
{
  // Direct pointers to known types
  private NamedList<Object> _header = null;
  private SolrDocumentList _results = null;
  private NamedList<ArrayList> _sortvalues = null;
  private NamedList<Object> _facetInfo = null;
  private Object _mdrillData = null;
  public Object get_mdrillData() {
	return _mdrillData;
}


private NamedList<Object> _debugInfo = null;
  private NamedList<Object> _highlightingInfo = null;
  private NamedList<Object> _spellInfo = null;
  private NamedList<Object> _statsInfo = null;
  private NamedList<Object> _termsInfo = null;

  // Grouping response
  private NamedList<Object> _groupedInfo = null;
  private GroupResponse _groupResponse = null;

  // Facet stuff
  private Map<String,Integer> _facetQuery = null;
  private List<FacetField> _facetFields = null;
  private List<FacetField> _limitingFacets = null;
  private List<FacetField> _facetDates = null;
  private List<RangeFacet> _facetRanges = null;

  // Highlight Info
  private Map<String,Map<String,List<String>>> _highlighting = null;

  // SpellCheck Response
  private SpellCheckResponse _spellResponse = null;

  // Terms Response
  private TermsResponse _termsResponse = null;
  
  // Field stats Response
  private Map<String,FieldStatsInfo> _fieldStatsInfo = null;
  
  // Debug Info
  private Map<String,Object> _debugMap = null;
  private Map<String,String> _explainMap = null;

  // utility variable used for automatic binding -- it should not be serialized
  private transient final SolrServer solrServer;
  
  public QueryResponse(){
    solrServer = null;
  }
  
  /**
   * Utility constructor to set the solrServer and namedList
   */
  public QueryResponse( NamedList<Object> res , SolrServer solrServer){
    this.setResponse( res );
    this.solrServer = solrServer;
  }

  
  Map<String,String> timetaken=new LinkedHashMap<String,String>();
  public ArrayList<String> getTimetaken(int n) {
	  Map<String,ArrayList<String>> rtn=new HashMap<String,ArrayList<String>>();
	  for(Map.Entry<String,String> e:timetaken.entrySet())
	  {
		  String key=e.getKey();
		  String val=e.getValue();
		  String[] arr=key.split("@");
		  
		  ArrayList<String> list=rtn.get(arr[0]);
		  if(list==null)
		  {
			  list=new ArrayList<String>();
			  rtn.put(arr[0], list);
		  }
		  
		  list.add(key+"@"+val);
		  
	  }
	  
	  ArrayList<String> rtntop=new ArrayList<String>();
	  for(Map.Entry<String,ArrayList<String>> e:rtn.entrySet())
	  {
		  ArrayList<String> list=new ArrayList<String>(e.getValue());
		  Collections.sort(list,new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					String[] arr1=o1.split("@");
					String[] arr2=o2.split("@");
					int cmp1=0;
					if(arr1.length>3&&arr2.length>3)
					{
						cmp1=arr2[3].compareTo(arr1[3]);
					}
					if(cmp1==0)
					{
						cmp1=o2.compareTo(o1);
					}
					return cmp1;
				}
			});
		  
		  rtntop.addAll(list.subList(0, Math.min(list.size(), n)));
		 
		  
	  }
	  
	return rtntop;
}

@Override
  public void setResponse( NamedList<Object> res )
  {
    super.setResponse( res );
    
    // Look for known things
    for( int i=0; i<res.size(); i++ ) {
      String n = res.getName( i );
      if( "responseHeader".equals( n ) ) {
        _header = (NamedList<Object>) res.getVal( i );
      }
      else if( "response".equals( n ) ) {
        _results = (SolrDocumentList) res.getVal( i );
      }
      else if( "sort_values".equals( n ) ) {
        _sortvalues = (NamedList<ArrayList>) res.getVal( i );
      }
      else if( "facet_counts".equals( n ) ) {
        _facetInfo = (NamedList<Object>) res.getVal( i );
        // extractFacetInfo inspects _results, so defer calling it
        // in case it hasn't been populated yet.
      }
      else if( "mdrill_data".equals( n ) ) {
    	  _mdrillData =  res.getVal( i );
       }
      
      
      else if( "mdrill_shard_time".equals( n ) ) {
    	  timetaken = (Map<String,String>) res.getVal( i );
        }
      
      else if( "debug".equals( n ) ) {
        _debugInfo = (NamedList<Object>) res.getVal( i );
        extractDebugInfo( _debugInfo );
      }
      else if( "grouped".equals( n ) ) {
        _groupedInfo = (NamedList<Object>) res.getVal( i );
        extractGroupedInfo(_groupedInfo);
      }
      else if( "highlighting".equals( n ) ) {
        _highlightingInfo = (NamedList<Object>) res.getVal( i );
        extractHighlightingInfo(_highlightingInfo);
      }
      else if ( "spellcheck".equals( n ) )  {
        _spellInfo = (NamedList<Object>) res.getVal( i );
        extractSpellCheckInfo(_spellInfo);
      }
      else if ( "stats".equals( n ) )  {
        _statsInfo = (NamedList<Object>) res.getVal( i );
        extractStatsInfo(_statsInfo);
      }
      else if ( "terms".equals( n ) ) {
        _termsInfo = (NamedList<Object>) res.getVal( i );
        extractTermsInfo( _termsInfo );
      }
    }
    if(_facetInfo != null) extractFacetInfo( _facetInfo );
  }

  private void extractSpellCheckInfo(NamedList<Object> spellInfo) {
    _spellResponse = new SpellCheckResponse(spellInfo);
  }

  private void extractTermsInfo(NamedList<Object> termsInfo) {
    _termsResponse = new TermsResponse(termsInfo);
  }
  
  private void extractStatsInfo(NamedList<Object> info) {
    if( info != null ) {
      _fieldStatsInfo = new HashMap<String, FieldStatsInfo>();
      NamedList<NamedList<Object>> ff = (NamedList<NamedList<Object>>) info.get( "stats_fields" );
      if( ff != null ) {
        for( Map.Entry<String,NamedList<Object>> entry : ff ) {
          NamedList<Object> v = entry.getValue();
          if( v != null ) {
            _fieldStatsInfo.put( entry.getKey(), 
                new FieldStatsInfo( v, entry.getKey() ) );
          }
        }
      }
    }
  }

  private void extractDebugInfo( NamedList<Object> debug )
  {
    _debugMap = new LinkedHashMap<String, Object>(); // keep the order
    for( Map.Entry<String, Object> info : debug ) {
      _debugMap.put( info.getKey(), info.getValue() );
    }

    // Parse out interesting bits from the debug info
    _explainMap = new HashMap<String, String>();
    NamedList<String> explain = (NamedList<String>)_debugMap.get( "explain" );
    if( explain != null ) {
      for( Map.Entry<String, String> info : explain ) {
        String key = info.getKey();
        _explainMap.put( key, info.getValue() );
      }
    }
  }

  private void extractGroupedInfo( NamedList<Object> info ) {
    if ( info != null ) {
      _groupResponse = new GroupResponse();
      int size = info.size();
      for (int i=0; i < size; i++) {
        String fieldName = info.getName(i);
        Object fieldGroups =  info.getVal(i);
        SimpleOrderedMap<Object> simpleOrderedMap = (SimpleOrderedMap<Object>) fieldGroups;

        Object oMatches = simpleOrderedMap.get("matches");
        Object oNGroups = simpleOrderedMap.get("ngroups");
        Object oGroups = simpleOrderedMap.get("groups");
        Object queryCommand = simpleOrderedMap.get("doclist");
        if (oMatches == null) {
          continue;
        }

        if (oGroups != null) {
          Integer iMatches = (Integer) oMatches;
          ArrayList<Object> groupsArr = (ArrayList<Object>) oGroups;
          GroupCommand groupedCommand;
          if (oNGroups != null) {
            Integer iNGroups = (Integer) oNGroups;
            groupedCommand = new GroupCommand(fieldName, iMatches, iNGroups);
          } else {
            groupedCommand = new GroupCommand(fieldName, iMatches);
          }

          for (Object oGrp : groupsArr) {
            SimpleOrderedMap grpMap = (SimpleOrderedMap) oGrp;
            Object sGroupValue = grpMap.get( "groupValue");
            SolrDocumentList doclist = (SolrDocumentList) grpMap.get( "doclist");
            Group group = new Group(sGroupValue != null ? sGroupValue.toString() : null, doclist) ;
            groupedCommand.add(group);
          }

          _groupResponse.add(groupedCommand);
        } else if (queryCommand != null) {
          Integer iMatches = (Integer) oMatches;
          GroupCommand groupCommand = new GroupCommand(fieldName, iMatches);
          SolrDocumentList docList = (SolrDocumentList) queryCommand;
          groupCommand.add(new Group(fieldName, docList));
          _groupResponse.add(groupCommand);
        }
      }
    }
  }

  private void extractHighlightingInfo( NamedList<Object> info )
  {
    _highlighting = new HashMap<String,Map<String,List<String>>>();
    for( Map.Entry<String, Object> doc : info ) {
      Map<String,List<String>> fieldMap = new HashMap<String, List<String>>();
      _highlighting.put( doc.getKey(), fieldMap );
      
      NamedList<List<String>> fnl = (NamedList<List<String>>)doc.getValue();
      for( Map.Entry<String, List<String>> field : fnl ) {
        fieldMap.put( field.getKey(), field.getValue() );
      }
    }
  }

  private void extractFacetInfo( NamedList<Object> info )
  {
    // Parse the queries
    _facetQuery = new LinkedHashMap<String, Integer>();
    NamedList<Integer> fq = (NamedList<Integer>) info.get( "facet_queries" );
    if (fq != null) {
      for( Map.Entry<String, Integer> entry : fq ) {
        _facetQuery.put( entry.getKey(), entry.getValue() );
      }
    }
    
    // Parse the facet info into fields
    // TODO?? The list could be <int> or <long>?  If always <long> then we can switch to <Long>
    NamedList<NamedList<Object>> ff = (NamedList<NamedList<Object>>) info.get( "facet_fields" );
    if( ff != null ) {
      _facetFields = new ArrayList<FacetField>( ff.size() );
      _limitingFacets = new ArrayList<FacetField>( ff.size() );
      
      long minsize = _results == null ? Long.MAX_VALUE :_results.getNumFound();
      for( Map.Entry<String,NamedList<Object>> facet : ff ) {//遍历“facet_fields”，如果用了cross这里面只有一个值就是solrCorssFields_s。如果没用cross，就是普通情况
    	
	    FacetField f = new FacetField( facet.getKey() );//写"solrCorssFields_s"或者普通的facet field name到FacetField
	    if(facet.getKey().equals("solrCorssFields_s")){
	    	//cross也有两种情况，一种是计算fl的sum，max，min。另一种是计算fl的进一步分组的组数（distinct count）
	    	int count1=0;
	    	for( Map.Entry<String, Object> entry : facet.getValue() ) {//遍历"solrCorssFields_s"
	        	NamedList<Object> nl = (NamedList<Object>)entry.getValue();//key是每个具体的cross串，value是具体信息的list
	        	
	        	if(count1==0){//entry.getKey().equals("recordcount")){//第一行的名字是recordcount，那么这一行就是分组的组数。
	        		if(nl.get("recordcount") != null){//第一行下面有count（很大的数）和recordcount（真实值）两部分
	        			long c = ((Number)nl.get("recordcount")).longValue();
			        	f.setTotal(c);
	        		}
	        	}else{
	        		int count2=0;
	        		long count=0;
	        		ArrayList<String> ext = new ArrayList<String>();
	        		for( Map.Entry<String, Object> entry2 : nl) {//遍历每个"交叉分组串"的list单元，提取count，以及fl，dist等信息
	        			if(count2==0){//每个组的第一行总是count
	        				count = ((Number)(nl.get("count"))).longValue();//得到count
	        			}else{
	        				//在其他行得到信息
	        				if(entry2.getValue() instanceof Number){//计算distinct的情况
	        					ext.add(entry2.getKey()+","+((Number)(entry2.getValue())).intValue());
	        				}else if(entry2.getValue() instanceof NamedList){//计算fl的情况（sum等）
	        					String name = entry2.getKey();
				        		NamedList<Number> nl3 = (NamedList<Number>)entry2.getValue();
				        		double sum = ((Number)(nl3.get("sum",0d))).doubleValue();
				        		double max = ((Number)(nl3.get("max",0d))).doubleValue();
				        		double min = ((Number)(nl3.get("min",0d))).doubleValue();
				        		double dist = ((Number)(nl3.get("dist",0d))).doubleValue();
				        		double cnt = ((Number)(nl3.get("cnt",0d))).doubleValue();
				        		ext.add(name+","+sum+","+max+","+min+","+dist+","+cnt);
	        				}
			        	}
			        	count2++;
	        		}
		            f.add( entry.getKey(), count, ext);//写入key(cross串)和value
	        	}
	        	count1++;
	        }
	    }else{//不用cross，普通情况
	    	
	    	for( Map.Entry<String, Object> entry : facet.getValue() ) {
	           	Number value2 = (Number)entry.getValue();
	           	f.add( entry.getKey(), value2.longValue() , null);
	        }
	    }
	    _facetFields.add( f );
	    FacetField nl = f.getLimitingFields( minsize );
	    if( nl.getValueCount() > 0 ) {
	      _limitingFacets.add( nl );
	    }
      }
    }
    
    //Parse date facets
    NamedList<NamedList<Object>> df = (NamedList<NamedList<Object>>) info.get("facet_dates");
    if (df != null) {
      // System.out.println(df);
      _facetDates = new ArrayList<FacetField>( df.size() );
      for (Map.Entry<String, NamedList<Object>> facet : df) {
        // System.out.println("Key: " + facet.getKey() + " Value: " + facet.getValue());
        NamedList<Object> values = facet.getValue();
        String gap = (String) values.get("gap");
        Date end = (Date) values.get("end");
        FacetField f = new FacetField(facet.getKey(), gap, end);
        
        for (Map.Entry<String, Object> entry : values)   {
          try {
            f.add(entry.getKey(), Long.parseLong(entry.getValue().toString()), null);
          } catch (NumberFormatException e) {
            //Ignore for non-number responses which are already handled above
          }
        }
        
        _facetDates.add(f);
      }
    }

    //Parse range facets
    NamedList<NamedList<Object>> rf = (NamedList<NamedList<Object>>) info.get("facet_ranges");
    if (rf != null) {
      _facetRanges = new ArrayList<RangeFacet>( rf.size() );
      for (Map.Entry<String, NamedList<Object>> facet : rf) {
        NamedList<Object> values = facet.getValue();
        Object rawGap = values.get("gap");

        RangeFacet rangeFacet;
        if (rawGap instanceof Number) {
          Number gap = (Number) rawGap;
          Number start = (Number) values.get("start");
          Number end = (Number) values.get("end");

          Number before = (Number) values.get("before");
          Number after = (Number) values.get("after");

          rangeFacet = new RangeFacet.Numeric(facet.getKey(), start, end, gap, before, after);
        } else {
          String gap = (String) rawGap;
          Date start = (Date) values.get("start");
          Date end = (Date) values.get("end");

          Number before = (Number) values.get("before");
          Number after = (Number) values.get("after");

          rangeFacet = new RangeFacet.Date(facet.getKey(), start, end, gap, before, after);
        }

        NamedList<Integer> counts = (NamedList<Integer>) values.get("counts");
        for (Map.Entry<String, Integer> entry : counts)   {
          rangeFacet.addCount(entry.getKey(), entry.getValue());
        }

        _facetRanges.add(rangeFacet);
      }
    }
  }

  //------------------------------------------------------
  //------------------------------------------------------

  /**
   * Remove the field facet info
   */
  public void removeFacets() {
    _facetFields = new ArrayList<FacetField>();
  }
  
  //------------------------------------------------------
  //------------------------------------------------------

  public NamedList<Object> getHeader() {
    return _header;
  }

  public SolrDocumentList getResults() {
    return _results;
  }
 
  public NamedList<ArrayList> getSortValues(){
    return _sortvalues;
  }

  public Map<String, Object> getDebugMap() {
    return _debugMap;
  }

  public Map<String, String> getExplainMap() {
    return _explainMap;
  }

  public Map<String,Integer> getFacetQuery() {
    return _facetQuery;
  }

  /**
   * Returns the {@link GroupResponse} containing the group commands.
   * A group command can be the result of one of the following parameters:
   * <ul>
   *   <li>group.field
   *   <li>group.func
   *   <li>group.query
   * </ul>
   *
   * @return the {@link GroupResponse} containing the group commands
   */
  public GroupResponse getGroupResponse() {
    return _groupResponse;
  }

  public Map<String, Map<String, List<String>>> getHighlighting() {
    return _highlighting;
  }

  public SpellCheckResponse getSpellCheckResponse() {
    return _spellResponse;
  }

  public TermsResponse getTermsResponse() {
    return _termsResponse;
  }
  
  /**
   * See also: {@link #getLimitingFacets()}
   */
  public List<FacetField> getFacetFields() {
    return _facetFields;
  }
  
  public List<FacetField> getFacetDates()   {
    return _facetDates;
  }

  public List<RangeFacet> getFacetRanges() {
    return _facetRanges;
  }

  /** get 
   * 
   * @param name the name of the 
   * @return the FacetField by name or null if it does not exist
   */
  public FacetField getFacetField(String name) {
    if (_facetFields==null) return null;
    for (FacetField f : _facetFields) {
      if (f.getName().equals(name)) return f;
    }
    return null;
  }
  
  public FacetField getFacetDate(String name)   {
    if (_facetDates == null)
      return null;
    for (FacetField f : _facetDates)
      if (f.getName().equals(name))
        return f;
    return null;
  }
  
  /**
   * @return a list of FacetFields where the count is less then
   * then #getResults() {@link SolrDocumentList#getNumFound()}
   * 
   * If you want all results exactly as returned by solr, use:
   * {@link #getFacetFields()}
   */
  public List<FacetField> getLimitingFacets() {
    return _limitingFacets;
  }
  
  public <T> List<T> getBeans(Class<T> type){
    return solrServer == null ? 
      new DocumentObjectBinder().getBeans(type,_results):
      solrServer.getBinder().getBeans(type, _results);
  }

  public Map<String, FieldStatsInfo> getFieldStatsInfo() {
    return _fieldStatsInfo;
  }
}



