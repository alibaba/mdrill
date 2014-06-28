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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.DoubleBarrelLRUCache;
import org.apache.lucene.util.CloseableThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.buffer.BlockBufferInput;
import com.alimama.mdrill.buffer.SmallBufferedInput;
import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;


/** This stores a monotonically increasing set of <Term, TermInfo> pairs in a
 * Directory.  Pairs are accessed either by Term or by ordinal position the
 * set.  */

public class TermInfosReader implements Closeable {
	  public static Logger log = LoggerFactory.getLogger(TermInfosReader.class);

  private final Directory directory;
  private final String segment;
  private final FieldInfos fieldInfos;

  private final CloseableThreadLocal<ThreadResources> threadResources = new CloseableThreadLocal<ThreadResources>();
  private final SegmentTermEnum origEnum;
  private final long size;

  private final TermInfoReaderIndexInterface index;
  private final int indexLength;
  
  private final int totalIndexInterval;

  private final static int DEFAULT_CACHE_SIZE = 1024;

  // Just adds term's ord to TermInfo
  private final static class TermInfoAndOrd extends TermInfo {
    final long termOrd;
    public TermInfoAndOrd(TermInfo ti, long termOrd) {
      super(ti);
      assert termOrd >= 0;
      this.termOrd = termOrd;
    }
  }

  private static class CloneableTerm extends DoubleBarrelLRUCache.CloneableKey {
    private final Term term;

    public CloneableTerm(Term t) {
      this.term = new Term(t.field(), t.text());
    }

    @Override
    public Object clone() {
      return new CloneableTerm(term);
    }

    @Override
    public boolean equals(Object _other) {
      CloneableTerm other = (CloneableTerm) _other;
      return term.equals(other.term);
    }

    @Override
    public int hashCode() {
      return term.hashCode();
    }
  }

  private final DoubleBarrelLRUCache<CloneableTerm,TermInfoAndOrd> termsCache = new DoubleBarrelLRUCache<CloneableTerm,TermInfoAndOrd>(DEFAULT_CACHE_SIZE);
  
  /**
   * Per-thread resources managed by ThreadLocal
   */
  private static final class ThreadResources {
    SegmentTermEnum termEnum;
  }
  IndexInput tisInput=null;
  IndexInput tiiInput=null;
  public IndexInput tiiInputquick=null;
  AtomicBoolean isQuickMode=new AtomicBoolean(false);
  
  public AtomicBoolean supportquick=new AtomicBoolean(false);

  
  DocValuesReader docValues=null;
  
  public DocValuesReader getDocValues() throws CloneNotSupportedException
  {
	  return this.docValues;
  }
  
  TermInfosReader(Directory dir, String seg, FieldInfos fis, int readBufferSize, int indexDivisor)
       throws CorruptIndexException, IOException {
    boolean success = false;
    docValues=null;
    supportquick.set(false);

    if (indexDivisor < 1 && indexDivisor != -1) {
      throw new IllegalArgumentException("indexDivisor must be -1 (don't load terms index) or greater than 0: got " + indexDivisor);
    }

    try {
      directory = dir;
      segment = seg;
      fieldInfos = fis;

      long tisfilesize=-1;
      String tisFileSize=IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_EXTENSION_SIZE);
      
      
      String quickTis=IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_EXTENSION_QUICK);
      String quickTisTxt=IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_EXTENSION_QUICK_TXT);
      String quickTisVal=IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_EXTENSION_QUICK_VAL);
      if(directory.fileExists(tisFileSize))
      {
		  IndexInput sizebuff=directory.openInput(tisFileSize, readBufferSize);
		  tisfilesize=sizebuff.readLong();
				  
		  if(directory.fileExists(quickTis))
		  {
			 docValues=new DocValuesReader();  
			 
		     docValues.quicktisInput=new SmallBufferedInput(directory.openInput(quickTis, 8),8);
			 docValues.quicktisInputTxt=new SmallBufferedInput(directory.openInput(quickTisTxt,1024),1024);
			 docValues.quicktisInputVal=new SmallBufferedInput(directory.openInput(quickTisVal,8),8);
	    
//			 if(directory instanceof FSDirectory){
//		    	  docValues.quicktisInput=BlockBufferInput.MaybeInstance(directory.openInput(quickTis,readBufferSize),directory,quickTis,directory.getP());
//				  docValues.quicktisInputTxt=BlockBufferInput.MaybeInstance(directory.openInput(quickTisTxt,readBufferSize),directory,quickTisTxt,directory.getP());
//				  docValues.quicktisInputVal=BlockBufferInput.MaybeInstance(directory.openInput(quickTisVal,readBufferSize),directory,quickTisVal,directory.getP());
//		    }else if(directory instanceof FileSystemDirectory){
//		    	  docValues.quicktisInput=BlockBufferInput.MaybeInstance(directory.openInput(quickTis,readBufferSize),directory,quickTis,directory.getP());
//				  docValues.quicktisInputTxt=BlockBufferInput.MaybeInstance(directory.openInput(quickTisTxt,readBufferSize),directory,quickTisTxt,directory.getP());
//				  docValues.quicktisInputVal=BlockBufferInput.MaybeInstance(directory.openInput(quickTisVal,readBufferSize),directory,quickTisVal,directory.getP());
//		    }else{}
			 docValues.readPosForm(sizebuff);
			 supportquick.set(true);
		 }
		  sizebuff.close();
      }
      
      String filename=IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_EXTENSION);
      final String indexFileName = IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_INDEX_EXTENSION);
      final String indexFileNamequick = IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_INDEX_EXTENSION_QUICK);

     
			if (directory instanceof FSDirectory) {
				tisInput = BlockBufferInput.MaybeInstance(
						directory.openInput(filename, readBufferSize),
						directory, filename, directory.getP());
				tiiInput = directory.openInput(indexFileName, readBufferSize);
			} else if (directory instanceof FileSystemDirectory) {
				tisInput = BlockBufferInput.MaybeInstance(
						directory.openInput(filename, readBufferSize),
						directory, filename, directory.getP());
				tiiInput = directory.openInput(indexFileName, readBufferSize);
			} else {
				tisInput = directory.openInput(filename, readBufferSize);
				tiiInput = directory.openInput(indexFileName, readBufferSize);
			}
      
      if(directory.fileExists(indexFileNamequick))
      {
    	  tiiInputquick=directory.openInput(indexFileNamequick, readBufferSize); 
    	  this.isQuickMode.set(true);
      }

      
      origEnum = new SegmentTermEnum(tisInput, fieldInfos, false,tisfilesize);
      size = origEnum.size;


      if (indexDivisor != -1) {
	  
	  long tiifilesize=-1;
          String tiiFileSize=IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_INDEX_EXTENSION_SIZE);
          if(directory.fileExists(tiiFileSize))
          {
    	  	IndexInput sizebuff=directory.openInput(tiiFileSize, readBufferSize);
    	  	tiifilesize=sizebuff.readLong();
    	  	sizebuff.close();
          }
        // Load terms index
        totalIndexInterval = origEnum.indexInterval * indexDivisor;
         SegmentTermEnum indexEnum=null ;
        try {
        	if(this.isQuickMode.get())
        	{
	            index = new TermInfosReaderIndexQuick(tiiInput,tiiInputquick,fieldInfos,tiifilesize, indexDivisor, dir.fileLength(indexFileName), totalIndexInterval);
        	}else{
	            indexEnum = new SegmentTermEnum(tiiInput, fieldInfos, true,tiifilesize);
	            index = new TermInfosReaderIndex(indexEnum, indexDivisor, dir.fileLength(indexFileName), totalIndexInterval);
        	}
          indexLength = index.length();
        } finally {
        	if(indexEnum!=null)
        	{
                indexEnum.close();
        	}
        }
      } else {
        // Do not load terms index:
        totalIndexInterval = -1;
        index = null;
        indexLength = -1;
      }
      success = true;
    } finally {
      // With lock-less commits, it's entirely possible (and
      // fine) to hit a FileNotFound exception above. In
      // this case, we want to explicitly close any subset
      // of things that were opened so that we don't have to
      // wait for a GC to do so.
      if (!success) {
        close();
      }
    }
  }

  public int getSkipInterval() {
    return origEnum.skipInterval;
  }
  
  public int getMaxSkipLevels() {
    return origEnum.maxSkipLevels;
  }

  public final void close() throws IOException {
		  supportquick.set(false);
		  if(this.docValues!=null)
		  {
			  docValues.close();
			  docValues=null;
		  }
		  if(this.isQuickMode.get())
		  {
			  if (tiiInput != null)
			    {
				  tiiInput.close();
			    }
			  if (tiiInputquick != null)
			    {
				  tiiInputquick.close();
			    }
		  }
	    if (origEnum != null)
	    {
	      origEnum.close();
	    }
	    threadResources.close();
    
  }

  /** Returns the number of term/value pairs in the set. */
  final long size() {
    return size;
  }

  private ThreadResources getThreadResources() {
    ThreadResources resources = threadResources.get();
    if (resources == null) {
      resources = new ThreadResources();
      resources.termEnum = terms();
      threadResources.set(resources);
    }
    return resources;
  }

  /** Returns the TermInfo for a Term in the set, or null. */
  TermInfo get(Term term) throws IOException {
    BytesRef termBytesRef = new BytesRef(term.text);
    return get(term, false, termBytesRef);
  }
  
  /** Returns the TermInfo for a Term in the set, or null. */
  private TermInfo get(Term term, boolean mustSeekEnum, BytesRef termBytesRef) throws IOException {
    if (size == 0) return null;

    ensureIndexIsRead();

    final CloneableTerm cacheKey = new CloneableTerm(term);
    TermInfoAndOrd tiOrd = termsCache.get(cacheKey);
    
    if (!mustSeekEnum && tiOrd != null) {
      return tiOrd;
    }
    
    ThreadResources resources = getThreadResources();
    SegmentTermEnum enumerator = resources.termEnum;
    if (enumerator.term() != null                 // term is at or past current
	&& ((enumerator.prev() != null && term.compareTo(enumerator.prev())> 0)
	    || term.compareTo(enumerator.term()) >= 0)) {
      int enumOffset = (int)(enumerator.position/totalIndexInterval)+1;
      if (indexLength == enumOffset    // but before end of block
    || index.compareTo(term,termBytesRef,enumOffset) < 0) {
       // no need to seek

        final TermInfo ti;

        int numScans = enumerator.scanTo(term);
        if (enumerator.term() != null && term.compareTo(enumerator.term()) == 0) {
          ti = enumerator.termInfo();
          if (numScans > 1) {
            if (tiOrd == null) {
              termsCache.put(cacheKey, new TermInfoAndOrd(ti, enumerator.position));
            } else {
              assert sameTermInfo(ti, tiOrd, enumerator);
              assert (int) enumerator.position == tiOrd.termOrd;
            }
          }
        } else {
          ti = null;
        }

        return ti;
      }  
    }

    // random-access: must seek
    final int indexPos;
    if (tiOrd != null) {
      indexPos = (int) (tiOrd.termOrd / totalIndexInterval);
    } else {
      // Must do binary search:
      indexPos = index.getIndexOffset(term,termBytesRef);
    }

    index.seekEnum(enumerator, indexPos);
    enumerator.scanTo(term);
    final TermInfo ti;
    if (enumerator.term() != null && term.compareTo(enumerator.term()) == 0) {
      ti = enumerator.termInfo();
      if (tiOrd == null) {
        termsCache.put(cacheKey, new TermInfoAndOrd(ti, enumerator.position));
      } else {
        assert sameTermInfo(ti, tiOrd, enumerator);
        assert enumerator.position == tiOrd.termOrd;
      }
    } else {
      ti = null;
    }
    return ti;
  }

  // called only from asserts
  private final boolean sameTermInfo(TermInfo ti1, TermInfo ti2, SegmentTermEnum enumerator) {
    if (ti1.docFreq != ti2.docFreq) {
      return false;
    }
    if (ti1.freqPointer != ti2.freqPointer) {
      return false;
    }
    if (ti1.proxPointer != ti2.proxPointer) {
      return false;
    }
    // skipOffset is only valid when docFreq >= skipInterval:
    if (ti1.docFreq >= enumerator.skipInterval &&
        ti1.skipOffset != ti2.skipOffset) {
      return false;
    }
    return true;
  }

  private void ensureIndexIsRead() {
    if (index == null) {
      throw new IllegalStateException("terms index was not loaded when this reader was created");
    }
  }

  /** Returns the position of a Term in the set or -1. */
  final long getPosition(Term term) throws IOException {
    if (size == 0) return -1;

    ensureIndexIsRead();
    BytesRef termBytesRef = new BytesRef(term.text);
    int indexOffset = index.getIndexOffset(term,termBytesRef);
    
    SegmentTermEnum enumerator = getThreadResources().termEnum;
    index.seekEnum(enumerator, indexOffset);

    while(term.compareTo(enumerator.term()) > 0 && enumerator.next()) {}

    if (term.compareTo(enumerator.term()) == 0)
      return enumerator.position;
    else
      return -1;
  }

  /** Returns an enumeration of all the Terms and TermInfos in the set. */
  public SegmentTermEnum terms() {
    return (SegmentTermEnum)origEnum.clone();
  }

  /** Returns an enumeration of terms starting at or after the named term. */
  public SegmentTermEnum terms(Term term) throws IOException {
    BytesRef termBytesRef = new BytesRef(term.text);
    get(term, true, termBytesRef);
    return (SegmentTermEnum)getThreadResources().termEnum.clone();
  }
}
