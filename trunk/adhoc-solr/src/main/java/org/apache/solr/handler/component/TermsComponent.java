package org.apache.solr.handler.component;
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

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.StringHelper;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.*;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.StrField;
import org.apache.solr.request.SimpleFacets.CountPair;
import org.apache.solr.util.BoundedTreeSet;

import org.apache.solr.client.solrj.response.TermsResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Return TermEnum information, useful for things like auto suggest.
 * 
 * <pre class="prettyprint">
 * &lt;searchComponent name="termsComponent" class="solr.TermsComponent"/&gt;
 * 
 * &lt;requestHandler name="/terms" class="solr.SearchHandler"&gt;
 *   &lt;lst name="defaults"&gt;
 *     &lt;bool name="terms"&gt;true&lt;/bool&gt;
 *   &lt;/lst&gt;
 *   &lt;arr name="components"&gt;
 *     &lt;str&gt;termsComponent&lt;/str&gt;
 *   &lt;/arr&gt;
 * &lt;/requestHandler&gt;</pre>
 *
 * @see org.apache.solr.common.params.TermsParams
 *      See Lucene's TermEnum class
 * @version $Id: TermsComponent.java 1067552 2011-02-05 23:52:42Z koji $
 */
public class TermsComponent extends SearchComponent {
  public static final int UNLIMITED_MAX_COUNT = -1;
  public static final String COMPONENT_NAME = "terms";

  @Override
  public void prepare(ResponseBuilder rb) throws IOException {

  }

  @Override
  public void process(ResponseBuilder rb) throws IOException {
}

  int resolveRegexpFlags(SolrParams params) {
      String[] flagParams = params.getParams(TermsParams.TERMS_REGEXP_FLAG);
      if (flagParams == null) {
          return 0;
      }
      int flags = 0;
      for (String flagParam : flagParams) {
          try {
            flags |= TermsParams.TermsRegexpFlag.valueOf(flagParam.toUpperCase(Locale.ENGLISH)).getValue();
          } catch (IllegalArgumentException iae) {
              throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unknown terms regex flag '" + flagParam + "'");
          }
      }
      return flags;
  }

  @Override
  public int distributedProcess(ResponseBuilder rb) throws IOException {
      return ResponseBuilder.STAGE_DONE;

  }

  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
}

  @Override
  public void finishStage(ResponseBuilder rb) {
}

  private ShardRequest createShardQuery(SolrParams params) {
    ShardRequest sreq = new ShardRequest();
    sreq.purpose = ShardRequest.PURPOSE_GET_TERMS;

    // base shard request on original parameters
    sreq.params = new ModifiableSolrParams(params);

    // don't pass through the shards param
    sreq.params.remove(ShardParams.SHARDS);

    // remove any limits for shards, we want them to return all possible
    // responses
    // we want this so we can calculate the correct counts
    // dont sort by count to avoid that unnecessary overhead on the shards
    sreq.params.remove(TermsParams.TERMS_MAXCOUNT);
    sreq.params.remove(TermsParams.TERMS_MINCOUNT);
    sreq.params.set(TermsParams.TERMS_LIMIT, -1);
    sreq.params.set(TermsParams.TERMS_SORT, TermsParams.TERMS_SORT_INDEX);

    // TODO: is there a better way to handle this?
    String qt = params.get(CommonParams.QT);
    if (qt != null) {
      sreq.params.add(CommonParams.QT, qt);
    }
    return sreq;
  }

  public class TermsHelper {
    // map to store returned terms
    private HashMap<String, HashMap<String, TermsResponse.Term>> fieldmap;
    private SolrParams params;

    public TermsHelper() {
      fieldmap = new HashMap<String, HashMap<String, TermsResponse.Term>>(5);
    }

    public void init(SolrParams params) {
      this.params = params;
      String[] fields = params.getParams(TermsParams.TERMS_FIELD);
      if (fields != null) {
        for (String field : fields) {
          // TODO: not sure 128 is the best starting size
          // It use it because that is what is used for facets
          fieldmap.put(field, new HashMap<String, TermsResponse.Term>(128));
        }
      }
    }

    public void parse(NamedList terms) {
      // exit if there is no terms
      if (terms == null) {
        return;
      }

      TermsResponse termsResponse = new TermsResponse(terms);
      
      // loop though each field and add each term+freq to map
      for (String key : fieldmap.keySet()) {
        HashMap<String, TermsResponse.Term> termmap = fieldmap.get(key);
        List<TermsResponse.Term> termlist = termsResponse.getTerms(key); 

        // skip this field if there are no terms
        if (termlist == null) {
          continue;
        }

        // loop though each term
        for (TermsResponse.Term tc : termlist) {
          String term = tc.getTerm();
          if (termmap.containsKey(term)) {
            TermsResponse.Term oldtc = termmap.get(term);
            oldtc.addFrequency(tc.getFrequency());
            termmap.put(term, oldtc);
          } else {
            termmap.put(term, tc);
          }
        }
      }
    }

    public NamedList buildResponse() {
      NamedList response = new SimpleOrderedMap();

      // determine if we are going index or count sort
      boolean sort = !TermsParams.TERMS_SORT_INDEX.equals(params.get(
          TermsParams.TERMS_SORT, TermsParams.TERMS_SORT_COUNT));

      // init minimum frequency
      long freqmin = 1;
      String s = params.get(TermsParams.TERMS_MINCOUNT);
      if (s != null)  freqmin = Long.parseLong(s);

      // init maximum frequency, default to max int
      long freqmax = -1;
      s = params.get(TermsParams.TERMS_MAXCOUNT);
      if (s != null)  freqmax = Long.parseLong(s);
      if (freqmax < 0) {
        freqmax = Long.MAX_VALUE;
      }

      // init limit, default to max int
      long limit = 10;
      s = params.get(TermsParams.TERMS_LIMIT);
      if (s != null)  limit = Long.parseLong(s);
      if (limit < 0) {
        limit = Long.MAX_VALUE;
      }

      // loop though each field we want terms from
      for (String key : fieldmap.keySet()) {
        NamedList fieldterms = new SimpleOrderedMap();
        TermsResponse.Term[] data = null;
        if (sort) {
          data = getCountSorted(fieldmap.get(key));
        } else {
          data = getLexSorted(fieldmap.get(key));
        }

        // loop though each term until we hit limit
        int cnt = 0;
        for (TermsResponse.Term tc : data) {
          if (tc.getFrequency() >= freqmin && tc.getFrequency() <= freqmax) {
            fieldterms.add(tc.getTerm(), num(tc.getFrequency()));
            cnt++;
          }

          if (cnt >= limit) {
            break;
          }
        }

        response.add(key, fieldterms);
      }

      return response;
    }

    // use <int> tags for smaller facet counts (better back compatibility)
    private Number num(long val) {
      if (val < Integer.MAX_VALUE) return (int) val;
      else return val;
    }

    // based on code from facets
    public TermsResponse.Term[] getLexSorted(HashMap<String, TermsResponse.Term> data) {
      TermsResponse.Term[] arr = data.values().toArray(new TermsResponse.Term[data.size()]);

      Arrays.sort(arr, new Comparator<TermsResponse.Term>() {
        public int compare(TermsResponse.Term o1, TermsResponse.Term o2) {
          return o1.getTerm().compareTo(o2.getTerm());
        }
      });

      return arr;
    }

    // based on code from facets
    public TermsResponse.Term[] getCountSorted(HashMap<String, TermsResponse.Term> data) {
      TermsResponse.Term[] arr = data.values().toArray(new TermsResponse.Term[data.size()]);

      Arrays.sort(arr, new Comparator<TermsResponse.Term>() {
        public int compare(TermsResponse.Term o1, TermsResponse.Term o2) {
          long freq1 = o1.getFrequency();
          long freq2 = o2.getFrequency();
          
          if (freq2 < freq1) {
            return -1;
          } else if (freq1 < freq2) {
            return 1;
          } else {
            return o1.getTerm().compareTo(o2.getTerm());
          }
        }
      });

      return arr;
    }
  }

  @Override
  public String getVersion() {
    return "$Revision: 1067552 $";
  }

  @Override
  public String getSourceId() {
    return "$Id: TermsComponent.java 1067552 2011-02-05 23:52:42Z koji $";
  }

  @Override
  public String getSource() {
    return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_5/solr/core/src/java/org/apache/solr/handler/component/TermsComponent.java $";
  }

  @Override
  public String getDescription() {
    return "A Component for working with Term Enumerators";
  }
}
