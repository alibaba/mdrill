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

import java.io.IOException;
import org.apache.lucene.util.BitVector;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.IndexInput;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.buffer.BlockBufferInput;
import com.alimama.mdrill.buffer.PForDelta;
import com.alimama.mdrill.buffer.BlockBufferInput.KeyInput;
import com.alimama.mdrill.buffer.RepeatCompress;

public class SegmentTermDocs implements TermDocs {
	  public static Logger log = LoggerFactory.getLogger(SolrCore.class);

  protected SegmentReader parent;
  protected IndexInput freqStream;
  protected int count;
  protected int df;
  protected BitVector deletedDocs;
  int doc = 0;
  int freq;

  private int skipInterval;
  private int maxSkipLevels;
  private DefaultSkipListReader skipListReader;
  
  private long freqBasePointer;
  private long proxBasePointer;

  private long skipPointer;
  private boolean haveSkipped;
  
  protected boolean currentFieldStoresPayloads;
  protected IndexOptions indexOptions;
  int isfrqCompress=0;
  
  ReadCompress compress;
  protected SegmentTermDocs(SegmentReader parent,int buffer) {
    this.parent = parent;
    this.freqStream = null;
    if(buffer>10240&&parent.core.freqStream instanceof KeyInput) {
	    	KeyInput kin=(KeyInput)parent.core.freqStream;
	    	this.freqStream=BlockBufferInput.MaybeInstance((IndexInput)kin.input.clone(), kin.d,kin.fname,kin.getP());
	    	log.info("####KeyInput#####="+buffer);
    } else  {
    	if(parent.core.freqStream instanceof KeyInput)
	    {
	    	KeyInput kin=(KeyInput)parent.core.freqStream;
	        this.freqStream = (IndexInput) kin.input.clone();

	    }else{
	    	this.freqStream = (IndexInput) parent.core.freqStream.clone();
	    }
    	
    	if(buffer>0&&parent.core.freqStream instanceof BufferedIndexInput)  {
        	((BufferedIndexInput)this.freqStream).setBufferSize(buffer);
       	 	log.info("####setbigbuffer#####="+buffer);
        }
    }
    
    try {
    	isfrqCompress=this.freqStream.readVInt();
    } catch (IOException e) {
    }
    
    synchronized (parent) {
      this.deletedDocs = parent.deletedDocs;
    }
    this.skipInterval = parent.core.getTermsReader().getSkipInterval();
    this.maxSkipLevels = parent.core.getTermsReader().getMaxSkipLevels();
  }

  public void seek(Term term) throws IOException {
    TermInfo ti = parent.core.getTermsReader().get(term);
    seek(ti, term);
  }

  public void seek(TermEnum termEnum) throws IOException {
    TermInfo ti;
    Term term;
    
    // use comparison of fieldinfos to verify that termEnum belongs to the same segment as this SegmentTermDocs
    if (termEnum instanceof SegmentTermEnum && ((SegmentTermEnum) termEnum).fieldInfos == parent.core.fieldInfos) {        // optimized case
      SegmentTermEnum segmentTermEnum = ((SegmentTermEnum) termEnum);
      term = segmentTermEnum.term();
      ti = segmentTermEnum.termInfo();
    } else  {                                         // punt case
      term = termEnum.term();
      ti = parent.core.getTermsReader().get(term);
    }
    
    seek(ti, term);
  }
  
 

  void seek(TermInfo ti, Term term) throws IOException {
    count = 0;
    FieldInfo fi = parent.core.fieldInfos.fieldInfo(term.field);
    indexOptions = (fi != null) ? fi.indexOptions : IndexOptions.DOCS_AND_FREQS_AND_POSITIONS;
    currentFieldStoresPayloads = (fi != null) ? fi.storePayloads : false;
    if (ti == null) {
      df = 0;
    } else {
      df = ti.docFreq;
      doc = 0;
      freqBasePointer = ti.freqPointer;
      proxBasePointer = ti.proxPointer;
      skipPointer = freqBasePointer + ti.skipOffset;
      freqStream.seek(freqBasePointer);
      this.compress=new ReadCompress(this.freqStream);
  		this.compress.setCompressMode(isfrqCompress);
      this.compress.resetCompressblock();
      haveSkipped = false;
    }
  }
  
	public static class ReadCompress {
		IndexInput stream;

		public ReadCompress(IndexInput freqStream) {
			super();
			this.stream = freqStream;
		}

		protected int[] buffer = new int[0];
		int pos = 0;

		int isfrqCompress = 0;

		public void setCompressMode(int isfrqCompress) {
			this.isfrqCompress = isfrqCompress;
		}

		public void resetCompressblock() {
			this.buffer = new int[0];
			this.pos = 0;
		}


		public int readCompressblock(int df) throws IOException {
			if (this.isfrqCompress==0 || df < DataOutput.BLOGK_SIZE_USED_COMPRESS) {
				return stream.readVInt();
			}
			while (pos >= buffer.length) {
				int val=stream.readVInt();
				int size = val>>2;
				int type=val-(size<<2);
				if(type==0)
				{
					this.buffer=new int[size];
					for (int i = 0; i < size; i++) {
						this.buffer[i] = stream.readVInt();
					}
				}else if(type==1)
				{
					int compresssize = size;
					int[] compress = new int[compresssize];
					for (int i = 0; i < compresssize; i++) {
						compress[i] = stream.readVInt();
					}
					this.buffer = RepeatCompress.decompress(compress);
				}else
				{
					
					int compresssize = stream.readVInt();
					int[] compress = new int[compresssize];
					for (int i = 0; i < compresssize; i++) {
						compress[i] = stream.readInt();
					}
					this.buffer = RepeatCompress.decompress(PForDelta.decompressOneBlock(compress, size));
				}

				this.pos = 0;
			}

			return this.buffer[this.pos++];
		}
	}
  
  public void seekDocs(long pos,int docFreq,IndexOptions indexOptions, boolean currentFieldStoresPayloads,int skipOffset,long proxPointer) throws IOException
  {
	    count = 0;
	    this.indexOptions =indexOptions;
	    this.currentFieldStoresPayloads = currentFieldStoresPayloads;
	    df = docFreq;
	    doc = 0;
	    freqBasePointer = pos;
	      proxBasePointer = proxPointer;
	      skipPointer = freqBasePointer + skipOffset;
	    freqStream.seek(freqBasePointer);
	    this.compress=new ReadCompress(this.freqStream);
	  	this.compress.setCompressMode(isfrqCompress);
	    this.compress.resetCompressblock();
	    haveSkipped = false;

  }

  public void close() throws IOException {
    freqStream.close();
    if (skipListReader != null)
      skipListReader.close();
  }

  public final int doc() { return doc; }
  public final int freq() { return freq; }

  protected void skippingDoc() throws IOException {
  }

  
  
  public void debug(StringBuffer buff){
	  buff.append("df=").append(this.df);
	  buff.append("freqBasePointer=").append(this.freqBasePointer);
	}
  public boolean next() throws IOException {
    while (true) {
      if (count == df)
        return false;
	    
	final int docCode =  this.compress.readCompressblock(this.df);
      
      if (indexOptions == IndexOptions.DOCS_ONLY) {
        doc += docCode;
        freq = 1;
      } else {
        doc += docCode >>> 1;       // shift off low bit
        if ((docCode & 1) != 0)       // if low bit is set
          freq = 1;         // freq is one
        else
          freq = this.compress.readCompressblock(this.df);     // else read freq
      }
      
      count++;

      if (deletedDocs == null || !deletedDocs.get(doc))
        break;
      skippingDoc();
    }
    return true;
  }

  /** Optimized implementation. */
  public int read(final int[] docs, final int[] freqs)
          throws IOException {
    final int length = docs.length;
    if (indexOptions == IndexOptions.DOCS_ONLY) {
      return readNoTf(docs, freqs, length);
    } else {
      int i = 0;
      while (i < length && count < df) {
        // manually inlined call to next() for speed
        final int docCode =  this.compress.readCompressblock(this.df);
        doc += docCode >>> 1;       // shift off low bit
        if ((docCode & 1) != 0)       // if low bit is set
          freq = 1;         // freq is one
        else
          freq = this.compress.readCompressblock(this.df);     // else read freq
        count++;

        if (deletedDocs == null || !deletedDocs.get(doc)) {
          docs[i] = doc;
          freqs[i] = freq;
          ++i;
        }
      }
      return i;
    }
  }

  private final int readNoTf(final int[] docs, final int[] freqs, final int length) throws IOException {
    int i = 0;
    while (i < length && count < df) {
      // manually inlined call to next() for speed
      doc +=   this.compress.readCompressblock(this.df);       
      count++;

      if (deletedDocs == null || !deletedDocs.get(doc)) {
        docs[i] = doc;
        // Hardware freq to 1 when term freqs were not
        // stored in the index
        freqs[i] = 1;
        ++i;
      }
    }
    return i;
  }
 
  
  /** Overridden by SegmentTermPositions to skip in prox stream. */
  protected void skipProx(long proxPointer, int payloadLength) throws IOException {}

  /** Optimized implementation. */
  public boolean skipTo(int target) throws IOException {
    if ((target - skipInterval) >= doc && df >= skipInterval) {                      // optimized case
      if (skipListReader == null)
        skipListReader = new DefaultSkipListReader((IndexInput) freqStream.clone(), maxSkipLevels, skipInterval); // lazily clone

      if (!haveSkipped) {                          // lazily initialize skip stream
        skipListReader.init(skipPointer, freqBasePointer, proxBasePointer, df, currentFieldStoresPayloads);
        haveSkipped = true;
      }

      int newCount = skipListReader.skipTo(target); 
      if (newCount > count) {
        freqStream.seek(skipListReader.getFreqPointer());

        skipProx(skipListReader.getProxPointer(), skipListReader.getPayloadLength());


        doc = skipListReader.getDoc();
        count = newCount;
      }      
    }

    // done skipping, now just scan
    do {
      if (!next())
        return false;
    } while (target > doc);
    return true;
  }
}
