package com.alimama.mdrill.solr.hbaserealtime;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader.PartionKey;

public interface MdrillDirectory {
	public void setPartion(PartionKey partion);
	public  List<Directory> getForSearch();
	public  void addDocument(SolrInputDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException;
	public void setCore(SolrCore core);
	void syncLocal();
	void syncHdfs();
}
