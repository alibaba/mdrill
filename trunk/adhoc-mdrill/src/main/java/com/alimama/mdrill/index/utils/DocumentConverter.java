package com.alimama.mdrill.index.utils;

import java.io.IOException;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.document.Document;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.schema.*;
import org.apache.solr.update.DocumentBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.lucene.index.*;

public class DocumentConverter {
	private IndexSchema schema=null;
	
	private void initSchema(String config,String schema) throws ParserConfigurationException, IOException, SAXException
	{
	    SolrConfig solrConfig = new SolrConfig(config);
	    InputSource is = new InputSource(solrConfig.getResourceLoader().openSchema(schema));
	    this.schema =new IndexSchema(solrConfig, "solrconfig",is);
	    TermInfosWriter.setSchema(this.schema);
	}
	
	public DocumentConverter(String[] fields,String config,String schema) {
	    try {
	        this.initSchema(config, schema);
            } catch (ParserConfigurationException e) {
            	 e.printStackTrace();
            } catch (IOException e) {
            	 e.printStackTrace();
            } catch (SAXException e) {
            	 e.printStackTrace();
            }
	}
	
    public Document convert(java.util.Map<String, String> value) {

	    SolrInputDocument doc = new SolrInputDocument();
	    for (Entry<String, String> e : value.entrySet()) {
		String fieldName = e.getKey();
		String fieldValue = e.getValue();
		doc.addField(fieldName, fieldValue);
	    }

	    return DocumentBuilder.toDocument(doc, this.schema);
	
    }
}
