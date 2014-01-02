package com.alimama.mdrill.editlog;

import static com.alimama.mdrill.editlog.defined.FSEditLogOpCodes.OP_ADD;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.JavaBinCodec;

import com.alimama.mdrill.editlog.read.FSEditLogOp;


public class AddOp extends  FSEditLogOp {
	SolrInputDocument doc=new SolrInputDocument();
    public SolrInputDocument getDoc() {
		return doc;
	}


	public void setDoc(SolrInputDocument doc) {
		this.doc = doc;
	}


	public AddOp() {
        super(OP_ADD);
      }
   

      @Override
      public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AddOp ");
        return builder.toString();
      }

      
    @Override
	public void readFields(DataInputStream in, int logVersion) throws IOException {
    	JavaBinCodec code=new JavaBinCodec();
    	this.doc=(SolrInputDocument)code.unmarshal(in);
	}

	@Override
	public void writeFields(DataOutputStream out) throws IOException {
		JavaBinCodec code=new JavaBinCodec();
    	code.marshal(doc, out);
	}
    }
