package org.apache.solr.handler;

import java.io.IOException;
import java.net.URL;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.MergeIndexesCommand;
import org.apache.solr.update.RollbackUpdateCommand;
import org.apache.solr.update.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class NonUpdateHandler extends UpdateHandler{
	public static Logger LOG = LoggerFactory.getLogger(NonUpdateHandler.class);

	SolrCore core;
	public NonUpdateHandler(SolrCore core) {
		super(core);
		this.core=core;
	}

	@Override
	public String getName() {
		return NonUpdateHandler.class.getName();
	}

	@Override
	public String getVersion() {
	    return SolrCore.version;
	}

	@Override
	public String getDescription() {
	    return "Update handler that efficiently directly updates the on-disk main lucene index";
	}

	public Category getCategory() {
	    return Category.UPDATEHANDLER;
	  }

	  public String getSourceId() {
	    return "$Id: DirectUpdateHandler2.java 1203770 2011-11-18 17:55:52Z mikemccand $";
	  }

	  public String getSource() {
	    return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_5/solr/core/src/java/org/apache/solr/update/DirectUpdateHandler2.java $";
	  }

	  public URL[] getDocs() {
	    return null;
	  }

	  public NamedList getStatistics() {
		    NamedList lst = new SimpleOrderedMap();
		    return lst;
		  }


	@Override
	public int addDoc(AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc=cmd.getSolrInputDocument();
		
		String partion=String.valueOf(doc.getFieldValue("mdrillPartion"));

		String cmdstr=String.valueOf(doc.getFieldValue("mdrillCmd"));
		if(cmdstr.equals("sync"))
		{
			this.core.getRealTime(partion,true).syncLocal();
		}else if(cmdstr.equals("syncHdfs")){
			this.core.getRealTime(partion,true).syncHdfs();
		}
		this.core.getRealTime(partion,true).addDocument(doc);
		return 1;
	}

	@Override
	public void delete(DeleteUpdateCommand cmd) throws IOException {
	
	}

	@Override
	public void deleteByQuery(DeleteUpdateCommand cmd) throws IOException {
	
	}

	@Override
	public int mergeIndexes(MergeIndexesCommand cmd) throws IOException {
		return 0;
	}

	@Override
	public void commit(CommitUpdateCommand cmd) throws IOException {
		
	}

	@Override
	public void rollback(RollbackUpdateCommand cmd) throws IOException {
		
	}

	@Override
	public void close() throws IOException {
	
	}

}
