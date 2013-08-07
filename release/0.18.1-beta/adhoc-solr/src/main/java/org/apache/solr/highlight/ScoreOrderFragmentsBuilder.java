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

package org.apache.solr.highlight;

import org.apache.lucene.search.vectorhighlight.BoundaryScanner;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.solr.common.params.SolrParams;

public class ScoreOrderFragmentsBuilder extends SolrFragmentsBuilder {

  @Override
  protected FragmentsBuilder getFragmentsBuilder( SolrParams params,
      String[] preTags, String[] postTags, BoundaryScanner bs ) {
    org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder sofb =
      new org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder( preTags, postTags, bs );
    sofb.setMultiValuedSeparator( getMultiValuedSeparatorChar( params ) );
    return sofb;
  }

  ///////////////////////////////////////////////////////////////////////
  //////////////////////// SolrInfoMBeans methods ///////////////////////
  ///////////////////////////////////////////////////////////////////////

  @Override
  public String getDescription() {
    return "ScoreOrderFragmentsBuilder";
  }

  @Override
  public String getSource() {
    return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_5/solr/core/src/java/org/apache/solr/highlight/ScoreOrderFragmentsBuilder.java $";
  }

  @Override
  public String getSourceId() {
    return "$Id: ScoreOrderFragmentsBuilder.java 1170620 2011-09-14 13:46:38Z koji $";
  }

  @Override
  public String getVersion() {
    return "$Revision: 1170620 $";
  }
}
