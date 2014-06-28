package org.apache.lucene.index;

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


import java.io.Closeable;
import java.io.IOException;
import java.util.zip.CRC32;

import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.UnicodeUtil;
import org.apache.lucene.util.ArrayUtil;
import org.apache.solr.request.mdrill.MdrillUtils;
import org.apache.solr.request.uninverted.TermIndex;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.FieldDatatype;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TermInfosWriter implements Closeable {
	  public static Logger LOG = LoggerFactory.getLogger(TermInfosWriter.class);

	private static IndexSchema schema=null;
	private static boolean notUseQuick=false;
	public static boolean isNotUseQuick() {
		return notUseQuick;
	}

	public static void setNotUseQuick(boolean notUseQuick) {
		TermInfosWriter.notUseQuick = notUseQuick;
	}

	public static void setSchema(IndexSchema schema)
	{
		TermInfosWriter.schema=schema;
	}

	public static final int QUICK_TII = -1210;
	public static final int FORMAT = -3;
	public static final int FORMAT_VERSION_UTF8_LENGTH_IN_BYTES = -4;
	public static final int FORMAT_CURRENT = FORMAT_VERSION_UTF8_LENGTH_IN_BYTES;

  private FieldInfos fieldInfos;
  private IndexOutput output;
  private IndexOutput outputSize;
  private IndexOutput outputQuickTii=null;
  

  private boolean isquickTis=false;
  private IndexSchema schemainfo=TermInfosWriter.schema;
  private TermInfo lastTi = new TermInfo();
  private long size;

  int indexInterval = 128;

  private static int SKIP_INTERVAL=16;//Integer.MAX_VALUE;
  public static void setSkipInterVal(int i)
  {
	  //如果是全文检索模式，为了提升跳跃的效率，该值不宜设置的太大，其他模式设置的DataOutput.BLOGK_SIZE_COMPRESS,能有比较好的压缩比
	  SKIP_INTERVAL=i;
  }
  int skipInterval = 16; 
  
  int maxSkipLevels = 10;

  private boolean isIndex;
  private byte[] lastTermBytes = new byte[10];
  private int lastTermBytesLength = 0;
  private int lastFieldNumber = -1;

  private TermInfosWriter other;
  private UnicodeUtil.UTF8Result utf8Result = new UnicodeUtil.UTF8Result();

  TermInfosWriter(Directory directory, String segment, FieldInfos fis,
                  int interval)
       throws IOException {
	  
    initialize(directory, segment, fis, interval, false);
    boolean success = false;
    try {
      other = new TermInfosWriter(directory, segment, fis, interval, true);
      other.other = this;
      success = true;
    } finally {
      if (!success) {
        IOUtils.closeWhileHandlingException(output,outputSize, other,outputQuickTii);
        docValues.close();
      }
    }
  }

  private TermInfosWriter(Directory directory, String segment, FieldInfos fis,
                          int interval, boolean isIndex) throws IOException {
    initialize(directory, segment, fis, interval, isIndex);
  }

  private void initialize(Directory directory, String segment, FieldInfos fis,
                          int interval, boolean isi) throws IOException {
	skipInterval=SKIP_INTERVAL<(Integer.MAX_VALUE-1000)?SKIP_INTERVAL:Integer.MAX_VALUE;

	docValues=new DocValuesWriteEmpty();
    indexInterval = interval;
    fieldInfos = fis;
    isIndex = isi;
    output = directory.createOutput(segment + (isIndex ? ".tii" : ".tis"));
    outputQuickTii=isIndex?directory.createOutput(segment+"." +IndexFileNames.TERMS_INDEX_EXTENSION_QUICK):null;
    IndexSchema schema=directory.getSchema();
    if(schema!=null)
    {
    	this.schemainfo=schema;
    }
    if(this.schemainfo!=null&&!isNotUseQuick()&&!(directory instanceof RAMDirectory))
    {
    	if(!isIndex)
    	{
    		DocValuesWriteImpl impl=new DocValuesWriteImpl();
    		impl.outputQuickTis=directory.createOutput(segment+"." +IndexFileNames.TERMS_EXTENSION_QUICK);
    		impl.outputQuickTisTxt=directory.createOutput(segment+"." +IndexFileNames.TERMS_EXTENSION_QUICK_TXT);
    		impl.outputQuickTisVal=directory.createOutput(segment+"." +IndexFileNames.TERMS_EXTENSION_QUICK_VAL);
    		docValues=impl;

    	}
    	this.isquickTis=true;
    }

    outputSize = directory.createOutput(segment + (isIndex ? "."+IndexFileNames.TERMS_INDEX_EXTENSION_SIZE : "."+IndexFileNames.TERMS_EXTENSION_SIZE));
    boolean success = false;
    try {
      output.writeInt(FORMAT_CURRENT);              // write format
      output.writeLong(QUICK_TII);                          // leave space for size
      output.writeInt(indexInterval);               // write indexInterval
      output.writeInt(skipInterval);                // write skipInterval
      output.writeInt(maxSkipLevels);               // write maxSkipLevels
      assert initUTF16Results();
      success = true;
    } finally {
      if (!success) {
        IOUtils.closeWhileHandlingException(output,outputSize,outputQuickTii);
        docValues.close();
      }
    }
  }

  void add(Term term, TermInfo ti) throws IOException {
    UnicodeUtil.UTF16toUTF8(term.text, 0, term.text.length(), utf8Result);
    add(term,fieldInfos.fieldNumber(term.field), utf8Result.result, utf8Result.length, ti);
  }

  // Currently used only by assert statements
  UnicodeUtil.UTF16Result utf16Result1;
  UnicodeUtil.UTF16Result utf16Result2;

  // Currently used only by assert statements
  private boolean initUTF16Results() {
    utf16Result1 = new UnicodeUtil.UTF16Result();
    utf16Result2 = new UnicodeUtil.UTF16Result();
    return true;
  }

  // Currently used only by assert statement
  private int compareToLastTerm(int fieldNumber, byte[] termBytes, int termBytesLength) {

    if (lastFieldNumber != fieldNumber) {
      final int cmp = fieldInfos.fieldName(lastFieldNumber).compareTo(fieldInfos.fieldName(fieldNumber));
      // If there is a field named "" (empty string) then we
      // will get 0 on this comparison, yet, it's "OK".  But
      // it's not OK if two different field numbers map to
      // the same name.
      if (cmp != 0 || lastFieldNumber != -1)
        return cmp;
    }

    UnicodeUtil.UTF8toUTF16(lastTermBytes, 0, lastTermBytesLength, utf16Result1);
    UnicodeUtil.UTF8toUTF16(termBytes, 0, termBytesLength, utf16Result2);
    final int len;
    if (utf16Result1.length < utf16Result2.length)
      len = utf16Result1.length;
    else
      len = utf16Result2.length;

    for(int i=0;i<len;i++) {
      final char ch1 = utf16Result1.result[i];
      final char ch2 = utf16Result2.result[i];
      if (ch1 != ch2)
        return ch1-ch2;
    }
    if (utf16Result1.length == 0 && lastFieldNumber == -1) {
      // If there is a field named "" (empty string) with a term text of "" (empty string) then we
      // will get 0 on this comparison, yet, it's "OK". 
      return -1;
    }
    return utf16Result1.length - utf16Result2.length;
  }
  


  

  DocValuesWriter docValues;
  void collect(int docid)
  {
		synchronized (lock) {

	  if(!this.isIndex)
	  {
		  docValues.collectDoc(docid,this.termNum);
	  }
		}
  }
  
 
  
  void add(Term term,int fieldNumber, byte[] termBytes, int termBytesLength, TermInfo ti)
    throws IOException {
	  
    assert compareToLastTerm(fieldNumber, termBytes, termBytesLength) < 0 ||
      (isIndex && termBytesLength == 0 && lastTermBytesLength == 0) :
      "Terms are out of order: field=" + fieldInfos.fieldName(fieldNumber) + " (number " + fieldNumber + ")" +
        " lastField=" + fieldInfos.fieldName(lastFieldNumber) + " (number " + lastFieldNumber + ")" +
        " text=" + new String(termBytes, 0, termBytesLength, "UTF-8") + " lastText=" + new String(lastTermBytes, 0, lastTermBytesLength, "UTF-8");

    assert ti.freqPointer >= lastTi.freqPointer: "freqPointer out of order (" + ti.freqPointer + " < " + lastTi.freqPointer + ")";
    assert ti.proxPointer >= lastTi.proxPointer: "proxPointer out of order (" + ti.proxPointer + " < " + lastTi.proxPointer + ")";

    if(this.isIndex)
	  {
		  this.addtii(fieldNumber, termBytes, termBytesLength, ti);
		  return ;
	  }
    if ( size % indexInterval == 0)
    {
      other.add(term,lastFieldNumber, lastTermBytes, lastTermBytesLength, lastTi);                      // add an index term
    }

    writeTerm(fieldNumber, termBytes, termBytesLength);                        // write term
    output.writeVInt(ti.docFreq);                       // write doc freq
    output.writeVLong(ti.freqPointer - lastTi.freqPointer); // write pointers
    output.writeVLong(ti.proxPointer - lastTi.proxPointer);

    if (ti.docFreq >= skipInterval) {
      output.writeVInt(ti.skipOffset);
    }
    
   

    lastFieldNumber = fieldNumber;
    lastTi.set(ti);
    size++;
  }
  
   static int unIntfieldnum=-9999;
  int lastquickfieldNumber=unIntfieldnum;
  FieldDatatype dataType;
  FieldType ft;
  int termNum=0;


  Object lock=new Object();
	public void startTerm(Term term, int fieldNumber) throws IOException {
		synchronized (lock) {
		if (this.lastquickfieldNumber != fieldNumber) {
			if (this.lastquickfieldNumber != unIntfieldnum) {
				this.docValues.flushFieldDoc(this.termNum);
			}
			this.docValues.start(fieldNumber,term.field);
			this.ft = this.schemainfo.getField(term.field).getType();
			this.dataType = UnInvertedFieldUtils.getDataType(ft);
			this.lastquickfieldNumber = fieldNumber;
			this.termNum = 0;

		}
		}
	}
  
  
  void addTm(Term term,int fieldNumber) throws IOException
  {
	  if(!this.isquickTis)
	  {
		  return ;
	  }
		synchronized (lock) {
 
      if(this.ft.isMultiValued())
      {
    	  return ;
      }
      
      long tmValue=0;
      if (dataType == FieldDatatype.d_long){
    	  tmValue=Long.parseLong(ft.indexedToReadable(term.text()));
		}else if (dataType == FieldDatatype.d_double) {
			
			Double val=MdrillUtils.ParseDouble(ft.indexedToReadable(term.text()));
			tmValue=Double.doubleToLongBits(val);
		}else{
			CRC32 crc32 = new CRC32();
			crc32.update(new String(ft.indexedToReadable(term.text())).getBytes());
			tmValue=crc32.getValue();
		}
      

      docValues.collectTm(tmValue);
      if ((this.termNum & TermIndex.intervalMask)==0){
    	  docValues.collectTmIndex(term.text());
      }
      
      this.termNum++;
		}
  }
  

  
  void addtii(int fieldNumber, byte[] termBytes, int termBytesLength, TermInfo ti)
  throws IOException {
  output.writeInt(ti.docFreq);                       // write doc freq
  output.writeLong(ti.freqPointer); // write pointers
  output.writeLong(ti.proxPointer);
  output.writeInt(ti.skipOffset);
  output.writeLong(other.output.getFilePointer());
  output.writeLong(this.outputQuickTii.getFilePointer());
  this.writeTermTii(fieldNumber, termBytes, termBytesLength);
  size++;
}

  private void writeTerm(int fieldNumber, byte[] termBytes, int termBytesLength)
       throws IOException {

    // TODO: UTF16toUTF8 could tell us this prefix
    // Compute prefix in common with last term:
    int start = 0;
    final int limit = termBytesLength < lastTermBytesLength ? termBytesLength : lastTermBytesLength;
    while(start < limit) {
      if (termBytes[start] != lastTermBytes[start])
        break;
      start++;
    }

    final int length = termBytesLength - start;
    output.writeVInt(start);                     // write shared prefix length
    output.writeVInt(length);                  // write delta length
    output.writeBytes(termBytes, start, length);  // write delta bytes
    output.writeVInt(fieldNumber); // write field num
    if (lastTermBytes.length < termBytesLength) {
      lastTermBytes = ArrayUtil.grow(lastTermBytes, termBytesLength);
    }
    System.arraycopy(termBytes, start, lastTermBytes, start, length);
    lastTermBytesLength = termBytesLength;
  }
  

  
	private void writeTermTii(int fieldNumber, byte[] termBytes,
			int termBytesLength) throws IOException {
		this.outputQuickTii.writeVInt(termBytesLength); // write delta bytes
		this.outputQuickTii.writeBytes(termBytes, 0, termBytesLength); // write delta bytes
		this.outputQuickTii.writeVInt(fieldNumber); // write field num
	}

  /** Called to complete TermInfos creation. */
  public void close() throws IOException {
	 
  	outputSize.writeLong(size);

	  
    try {
		synchronized (lock) {

	  	this.docValues.flushFieldDoc(this.termNum);
    	this.docValues.flushPosTo(outputSize);
    	this.docValues.free();
	  	this.docValues.close();
		}

	  	 if(outputQuickTii!=null)
		  {
			  outputQuickTii.close();
		  }
    } finally {
      try {
    	  outputSize.close();
    	  output.close();
      } finally {
        if (!isIndex) {
          other.close();
        }
      }
    }

  }

}
