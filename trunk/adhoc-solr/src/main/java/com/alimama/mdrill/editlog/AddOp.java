package com.alimama.mdrill.editlog;

import static com.alimama.mdrill.editlog.defined.FSEditLogOpCodes.OP_ADD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.alimama.mdrill.editlog.read.FSEditLogOp;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;

public class AddOp extends  FSEditLogOp {
	public static Logger LOG = LoggerFactory.getLogger(AddOp.class);

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

      
      
      public static void main(String[] args) {
    	  
    	  for(int k=0;k<1000;k++)
    	  {
    	  SolrInputDocument dd=new SolrInputDocument();
    	  dd.addField("a", 1);
    	  dd.addField("b", 1.0);
    	  dd.addField("c", "1123");
    	  dd.addField("d", 1l);
    	  dd.addField("c", "333");
    	  dd.addField("c", "1111000000000000000000001111");

    	  dd.addField("e", "4");

    	  dd.addField("d"+k, k);


    	  byte[] bytes=ser(dd);

    	  System.out.println(serialize(dd).length+"=="+bytes.length);
	    	SolrInputDocument aaa=dec(bytes);

	    	System.out.println(aaa.toString());
	    	
	    	System.out.println("#######################");
    	  }
	}
      
      /**
       * 序列化
       * 
       * @param object
       * @return
       */
      public static byte[] serialize(Object object) {
          ObjectOutputStream oos = null;
          ByteArrayOutputStream baos = null;
          try {
              // 序列化
              baos = new ByteArrayOutputStream();
              oos = new ObjectOutputStream(baos);
              oos.writeObject(object);
              byte[] bytes = baos.toByteArray();
              return bytes;
          } catch (Exception e) {

          }
          return null;
      }

      /**
       * 反序列化
       * 
       * @param bytes
       * @return
       */
      public static Object unserialize(byte[] bytes) {
          ByteArrayInputStream bais = null;
          try {
              // 反序列化
              bais = new ByteArrayInputStream(bytes);
              ObjectInputStream ois = new ObjectInputStream(bais);
              return ois.readObject();
          } catch (Exception e) {

          }
          return null;
      }
      
      public static byte[] ser(SolrInputDocument doc)
      {
    	  synchronized (buffer) {
	    	  ObjectBuffer buffer=new ObjectBuffer(KROY);
	    	  return buffer.writeClassAndObject(doc);
    	  }
      }
      
      public static SolrInputDocument dec(byte[] buff)
      {
    	  synchronized (buffer) {
        	  return (SolrInputDocument) buffer.readClassAndObject(buff);
		}
      }
 
    
    private static Kryo KROY=new Kryo();
    private static ObjectBuffer buffer=null;

    static{
    	KROY.register(byte[].class);
    	KROY.register(String[].class);
    	KROY.register(ArrayList.class);
    	KROY.register(HashMap.class);
    	KROY.register(LinkedHashMap.class);
    	KROY.register(SolrInputField.class);
    	KROY.register(SolrInputDocument.class);
    	KROY.register(Collection.class);
    	KROY.register(NamedList.class);
    	KROY.register(SolrDocumentList.class);
    	KROY.register(SolrDocument.class);
    	KROY.register(Map.class);
    	KROY.register(Iterator.class);
    	KROY.register(Iterable.class);
    	KROY.register(Date.class);
    	KROY.register(ByteBuffer.class);
    	buffer=new ObjectBuffer(KROY);
    }
    
    
	@Override
	public void writeFields(DataOutputStream out) throws IOException {
		byte[] data = null;
		try {
			data = ser(doc);
		} catch (Throwable e) {
			data = null;
		}

		if (data == null || data.length == 0) {
			out.writeInt(0);
		} else {
			out.writeInt(data.length);
			out.write(data, 0, data.length);
		}
	}

	@Override
	public void readFields(DataInputStream in, int logVersion)
			throws IOException {
		int len = in.readInt();
		if (len <= 0) {
			this.doc = null;
			return;
		}

		byte[] b = new byte[len];
		in.read(b, 0, len);
		try {
			this.doc = dec(b);
		} catch (Throwable e) {
			LOG.error("dec", e);
			this.doc = null;
		}
	}
}
