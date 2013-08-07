package org.apache.lucene.analysis.extendsQuery;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.ToStringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//http://hdphadoop:8983/solr/select/?q=user_id%3A2*&version=2.2&start=0&rows=100&indent=on&fq={!infile%20f=user_id}/data/testdata/filein
public class SelectInListQuery <A> extends MultiTermQuery {
    private static final long serialVersionUID = 1L;
    private String field;
    private String file;
    private TransType<A> trans;
    
    public static interface TransType<A>{
	public A trans(String str);
    }
    
    public static class TransLong implements TransType<Long>
    {
        public Long trans(String str) {
	    return Long.parseLong(str);
        }
    }
    
    public static class TransString implements TransType<String>
    {
        public String trans(String str) {
	    return str;
        }
    }

    public SelectInListQuery(String file, String field,TransType<A> trans) {
	this.field = field;
	this.file = file;
	this.trans=trans;
    }
    
    public static class ListTermEnum<A> extends FilteredTermEnum {
	private maxminPair<Term> maxmincmp;
	private Set<A> list;
	private boolean endEnum = false;
	private String field;
	private Term maxTerm;
	private TransType<A> trans;


	public ListTermEnum(IndexReader reader, Set<A> list, String field,TransType<A> trans)
	        throws IOException {
		this.trans=trans;

	    this.maxmincmp = this.TermMaxMin(field,list);
	    this.list = list;
	    this.field=field;
	    Term start=this.maxmincmp.getMin();
	    this.maxTerm=this.maxmincmp.getMax();
	    this.setEnum(reader.terms(start));
	    
	    
	}

	@Override
	protected boolean termCompare(Term term) {
	    if (term.field().equals(this.field) &&list.contains(this.trans.trans(term.text())) ) {                                                                              
		 return true;
	    }
	    
	    if(term.compareTo(this.maxTerm)>0)
	    {
		    endEnum = true;
	    }
	    return false;
	}

	@Override
	public float difference() {
	    return 1.0f;
	}

	@Override
	protected boolean endEnum() {
	    return endEnum;
	}

	public static class maxminPair<T> {
	    private T max;
	    private T min;

	    public T getMax() {
		return max;
	    }

	    public T getMin() {
		return min;
	    }

	    public maxminPair(T max, T min) {
		super();
		this.max = max;
		this.min = min;
	    }
	}

	public <T> maxminPair<Term> TermMaxMin(String fields,
	        Collection<? extends T> coll) {
	    Iterator<? extends T> i = coll.iterator();
	    Term first=new Term(fields,String.valueOf(i.next()));
	    Term max=first;
	    Term min = first;
	    while (i.hasNext()) {
		   Term next=new Term(fields,String.valueOf(i.next()));
		if (next.compareTo(max) > 0) {
		    max = next;
		} else if (next.compareTo(min) < 0) {
		    min = next;
		}
	    }
	    return new maxminPair<Term>(max, min);
	}
    }

    @Override
    protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
	
	    Set<A> inlist = new HashSet<A>();
	    FileReader freader;

	    freader = new FileReader(file);

	    BufferedReader br = new BufferedReader(freader);
	    String s1 = null;
	    while ((s1 = br.readLine()) != null) {
		inlist.add(this.trans.trans(s1));
	    }
	    br.close();
	    freader.close();
	
	
	return new ListTermEnum<A>(reader,inlist, this.field,this.trans);
    }



    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((file == null) ? 0 : file.hashCode());
	result = prime * result + ((field == null) ? 0 : field.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	SelectInListQuery other = (SelectInListQuery) obj;
	if (file == null) {
	    if (other.file != null)
		return false;
	} else if (!file.equals(other.file))
	    return false;
	if (field == null) {
	    if (other.field != null)
		return false;
	} else if (!field.equals(other.field))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "SelectInListQuery [term=" + field + ", file=" + file
	        + ", hashcode=" + file.hashCode() + "]";
    }

    @Override
    public String toString(String field) {
	StringBuilder buffer = new StringBuilder();
	if (!this.field.equals(field)) {
	    buffer.append(this.field);
	    buffer.append(":");
	}
	buffer.append(this.toString());
	buffer.append(ToStringUtils.boost(getBoost()));
	return buffer.toString();
    }
    
    
//    public static class ListEnum extends TermEnum {
// 	private List<String> list;
// 	private Term term;
// 	private Integer index = 0;
 //
// 	public ListEnum(List<String> list, Term term) {
// 	    this.list = list;
// 	    this.term = term;
// 	}
 //
// 	@Override
// 	public boolean next() throws IOException {
// 	    this.index++;
// 	    if (this.index >= this.list.size()) {
// 		return false;
// 	    }
// 	    return true;
// 	}
 //
// 	@Override
// 	public Term term() {
// 	    String termstr=list.get(this.index);
// 	    return new Term(this.term.field(), termstr);
// 	}
 //
// 	@Override
// 	public int docFreq() {
// 	    return 1;
// 	}
 //
// 	@Override
// 	public void close() throws IOException {
// 	}
//     }

}
