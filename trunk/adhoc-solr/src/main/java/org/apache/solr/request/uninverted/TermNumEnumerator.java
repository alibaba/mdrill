package org.apache.solr.request.uninverted;

import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

public  class TermNumEnumerator extends TermEnum {
//	  public static Logger log = LoggerFactory.getLogger(TermNumEnumerator.class);

	  protected final IndexReader reader;
	  protected final TermIndex tindex;
	  protected TermEnum tenum;
	  protected int pos=-1;
	  protected Term t;
	  protected TermDocs termDocs;


	  TermNumEnumerator(IndexReader reader, TermIndex tindex) throws IOException {
	    this.reader = reader;
	    this.tindex = tindex;
	  }


	  TermNumEnumerator(IndexReader reader, TermIndex tindex, String termValue, int pos) throws IOException {
	    this.reader = reader;
	    this.tindex = tindex;
	    this.pos = pos;
	    tenum = reader.terms(tindex.createTerm(termValue));
	    setTerm();
	  }

	  public TermDocs getTermDocs() throws IOException {
	    if (termDocs==null) termDocs = reader.termDocs(t,102400);
	    else termDocs.seek(t);
	    return termDocs;
	  }

	  protected boolean setTerm() {
	    t = tenum.term();
	    if (t==null
	            || t.field() != tindex.fterm.field()  // intern'd compare
	            || (tindex.prefix != null && !t.text().startsWith(tindex.prefix,0)) )
	    {
	      t = null;
	      return false;
	    }
	    return true;
	  }


	  @Override
	  public boolean next() throws IOException {
	    pos++;
	    boolean b = tenum.next();
	    if (!b) {
	      t = null;
	      return false;
	    }
	    return setTerm();  // this is extra work if we know we are in bounds...
	  }

	  @Override
	  public Term term() {
	    return t;
	  }

	  @Override
	  public int docFreq() {
	    return tenum.docFreq();
	  }

	  @Override
	  public void close() throws IOException {
	    if (tenum!=null) tenum.close();
	  }

	  public boolean skipTo(String target) throws IOException {
	    return skipTo(tindex.fterm.createTerm(target));
	  }

	  public boolean skipTo(Term target) throws IOException {
	    // already here
	    if (t != null && t.equals(target)) return true;

	    int startIdx = tindex.index.search(target.text());

	    if (startIdx >= 0) {
	      // we hit the term exactly... lucky us!
	      if (tenum != null) tenum.close();
	      tenum = reader.terms(target);
	      pos = startIdx << tindex.intervalBits;
	      return setTerm();
	    }

	    // we didn't hit the term exactly
	    startIdx=-startIdx-1;

	    if (startIdx == 0) {
	      // our target occurs *before* the first term
	      if (tenum != null) tenum.close();
	      tenum = reader.terms(target);
	      pos = 0;
	      return setTerm();
	    }

	    // back up to the start of the block
	    startIdx--;

	    if ((pos >> tindex.intervalBits) == startIdx && t != null && t.text().compareTo(target.text())<=0) {
	      // we are already in the right block and the current term is before the term we want,
	      // so we don't need to seek.
	    } else {
	      // seek to the right block
	      if (tenum != null) tenum.close();            
	      tenum = reader.terms(target.createTerm(tindex.index.get(startIdx)));
	      pos = startIdx << tindex.intervalBits;
	      setTerm();  // should be true since it's in the index
	    }


	    while (t != null && t.text().compareTo(target.text()) < 0) {
	      next();
	    }

	    return t != null;
	  }


	  public boolean skipTo(int termNumber) throws IOException {
	    int delta = termNumber - pos;
	    if (delta < 0 || delta > tindex.interval || tenum==null) {
	      int idx = termNumber >>> tindex.intervalBits;
	      String base = tindex.index.get(idx);
	      pos = idx << tindex.intervalBits;
	      delta = termNumber - pos;
	      if (tenum != null) {tenum.close();}
	      tenum = reader.terms(tindex.createTerm(base));
	    }

	    while (--delta >= 0) {
	      boolean b = tenum.next();
	      if (b==false) {
	        t = null;
	        return false;
	      }
	      ++pos;
	    }
	    
	    return setTerm();
	  }

	  /** The current term number, starting at 0.
	   * Only valid if the previous call to next() or skipTo() returned true.
	   */
	  public int getTermNumber() {
	    return pos;
	  }
	}
