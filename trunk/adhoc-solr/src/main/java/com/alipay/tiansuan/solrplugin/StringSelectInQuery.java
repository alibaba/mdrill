package com.alipay.tiansuan.solrplugin;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.ToStringUtils;

import com.alipay.tiansuan.solrplugin.HdfsToSet.TransType;


import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StringSelectInQuery <A> extends MultiTermQuery {
    private static final long serialVersionUID = 1L;
    private String field;
    private String file;
    private TransType<A> trans;
    

    public StringSelectInQuery(String file, String field,TransType<A> trans) {
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


	boolean isempty=false;
	public ListTermEnum(IndexReader reader, Set<A> list, String field,TransType<A> trans)
	        throws IOException {
		this.trans=trans;
		if(list==null||list.size()<=0)
		{
			isempty=true;
		}
		this.maxmincmp = this.TermMaxMin(field,list,this.trans);

	    this.list = list;
	    this.field=field;
	    Term start=this.maxmincmp.getMin();
	    this.maxTerm=this.maxmincmp.getMax();
	    this.setEnum(reader.terms(start));
	    
	    
	}

	@Override
	protected boolean termCompare(Term term) {
		
		if(isempty)
		{
		    endEnum = true;
			return false;
		}
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
	        Collection<? extends T> coll,TransType<T> t) {
		
		if(isempty)
		{
		    return new maxminPair<Term>(new Term(fields), new Term(fields));
		}
		
	    Iterator<? extends T> i = coll.iterator();
	    
	    Term first=new Term(fields,t.transBack(i.next()));
	    Term max=first;
	    Term min = first;
	    while (i.hasNext()) {
		   Term next=new Term(fields,t.transBack(i.next()));
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
	HdfsToSet<A> toset=new HdfsToSet<A>();
	return new ListTermEnum<A>(reader, toset.toset(file, trans), this.field, this.trans);
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
	StringSelectInQuery other = (StringSelectInQuery) obj;
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
    

}
