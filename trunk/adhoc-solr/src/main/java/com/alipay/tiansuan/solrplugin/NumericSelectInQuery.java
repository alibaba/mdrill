package com.alipay.tiansuan.solrplugin;



import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.apache.lucene.document.NumericField.DataType;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import com.alipay.tiansuan.solrplugin.HdfsToSet.TransType;

public final class NumericSelectInQuery<T extends Number> extends
        MultiTermQuery {

    private static final long serialVersionUID = 1L;

    private String field;
    private final int precisionStep;
    private final DataType dataType;
    private String file;
    private TransType<T> trans;
	private final Term termTemplate ;

    public NumericSelectInQuery(final String field, int precisionStep,
	    final DataType dataType,TransType<T> trans, String file) {
    	if(precisionStep==0)
    	{
    		precisionStep=Integer.MAX_VALUE;
    	}
	if (precisionStep < 1)
	    throw new IllegalArgumentException("precisionStep must be >=1");
	this.field = StringHelper.intern(field);
	termTemplate = new Term(field);
	this.precisionStep = precisionStep;
	this.dataType = dataType;
	this.file = file;
	this.trans=trans;

	    switch (dataType) {
	      case LONG:
	      case DOUBLE:
	        setRewriteMethod( (precisionStep > 6) ?
	          CONSTANT_SCORE_FILTER_REWRITE : 
	          CONSTANT_SCORE_AUTO_REWRITE_DEFAULT
	        );
	        break;
	      case INT:
	      case FLOAT:
	        setRewriteMethod( (precisionStep > 8) ?
	          CONSTANT_SCORE_FILTER_REWRITE : 
	          CONSTANT_SCORE_AUTO_REWRITE_DEFAULT
	        );
	        break;
	      default:
	        // should never happen
	        throw new IllegalArgumentException("Invalid numeric DataType");
	    }
    }
    

    @Override
    protected FilteredTermEnum getEnum(final IndexReader reader)
	    throws IOException {
	return new NumericRangeTermEnum(reader);
    }

    /** Returns the field name for this query */
    public String getField() {
	return field;
    }

    /** Returns the precision step. */
    public int getPrecisionStep() {
	return precisionStep;
    }

    private void readObject(java.io.ObjectInputStream in)
	    throws java.io.IOException, ClassNotFoundException {
	in.defaultReadObject();
	field = StringHelper.intern(field);
    }

    public final class Bounds{
	HashSet<String> bound=new HashSet<String>();
	Term max=null;
	
	public void setmax(String s)
	{
	    Term t=termTemplate.createTerm(s);

	    if(max==null||max.compareTo(t)<=0)
	    {
		this.max=t;
	    }
	}
    }
    
    private final class NumericRangeTermEnum extends FilteredTermEnum {

	private final IndexReader reader;
	private final java.util.LinkedHashMap<String, Bounds> rangeBounds = new LinkedHashMap<String,Bounds>();

	private final java.util.LinkedList<String> lowBounds = new LinkedList<String>();

	Set<T> list;

	private void loopDouble() {
	    for (T t : list) {
		long bound;
		if (dataType == DataType.LONG) {
		    bound = t.longValue();
		} else {
		    assert dataType == DataType.DOUBLE;
		    bound = NumericUtils.doubleToSortableLong(t.doubleValue());
		}

		NumericUtils.splitLongRange(
		        new NumericUtils.LongRangeBuilder() {
			    @Override
			    public final void addRange(String minPrefixCoded,
			            String maxPrefixCoded) {
				Bounds maxset = rangeBounds.get(minPrefixCoded);
			        if (maxset == null) {
				    maxset = new Bounds();
				    rangeBounds.put(minPrefixCoded, maxset);
				    lowBounds.add(minPrefixCoded);
			        }

			        maxset.bound.add(maxPrefixCoded);
			        maxset.setmax(maxPrefixCoded);
			    }
		        }, precisionStep, bound, bound);
	    }
	}

	private void loopfloat() {
	    for (T t : list) {
		// lower
		int bound;
		if (dataType == DataType.INT) {
		    bound = t.intValue();
		} else {
		    assert dataType == DataType.FLOAT;
		    bound = NumericUtils.floatToSortableInt(t.floatValue());
		}
		
		 NumericUtils.splitIntRange(new NumericUtils.IntRangeBuilder() {
		            @Override
		            public final void addRange(String minPrefixCoded, String maxPrefixCoded) {

		        	Bounds maxset = rangeBounds.get(minPrefixCoded);
			        if (maxset == null) {
				    maxset = new Bounds();
				    rangeBounds.put(minPrefixCoded, maxset);
				    lowBounds.add(minPrefixCoded);
			        }
			        maxset.bound.add(maxPrefixCoded);
			        maxset.setmax(maxPrefixCoded);

			    
		          }}, precisionStep, bound, bound);
	    }
	}

	NumericRangeTermEnum(final IndexReader reader) throws IOException {
	    this.reader = reader;
	    this.init();
	}
	
	boolean isinit=false;
	public void init() throws IOException
	{
	    if(this.isinit)
	    {
		return ;
	    }
	    
	    isinit=true;
	    HdfsToSet<T> toset = new HdfsToSet<T>();
	    this.list = toset.toset(file, trans);

	    switch (dataType) {
	    case LONG:
	    case DOUBLE: {
		this.loopDouble();
		break;
	    }

	    case INT:
	    case FLOAT: {
		this.loopfloat();
		break;
	    }

	    default:
		// should never happen
		throw new IllegalArgumentException("Invalid numeric DataType");
	    }

	    System.out.println("##############"+rangeBounds.size());
	    this.next();
	}

	@Override
	public float difference() {
	    return 1.0f;
	}

	/** this is a dummy, it is not used by this class. */
	@Override
	protected boolean endEnum() {
	    throw new UnsupportedOperationException("not implemented");
	}

	/** this is a dummy, it is not used by this class. */
	@Override
	protected void setEnum(TermEnum tenum) {
	    throw new UnsupportedOperationException("not implemented");
	}

	/**
	 * Compares if current upper bound is reached. In contrast to
	 * {@link FilteredTermEnum}, a return value of <code>false</code> ends
	 * iterating the current enum and forwards to the next sub-range.
	 */
	@Override
	protected boolean termCompare(Term term) {
	    if (!term.field().equals(field)) {
		return false;
	    }
	   return isContainsTerm(term);
	}
	
	private boolean isContainsTerm(Term term)
	{
	    String t = term.text();
	    if (maxset != null && maxset.bound.contains(t)) {
		return true;
	    }
	    return false;
	}
	
	
	
	private boolean isEndTerm(Term term) {
	    if (!term.field().equals(field)) {
		return true;
	    }
	    if (maxset != null &&term.compareTo(maxset.max)>0) {
		return true;
	    }
	    return false;
	}

	Bounds maxset;

	@Override
	public Term term() {
	    return currentTerm;
	}
	
	@Override
	public boolean next() throws IOException {
	    if (this.currentTerm != null && this.getterms()) {
		return true;
	    }

	    currentTerm = null;
	    while (lowBounds.size() >= 1) {
		if (actualEnum != null) {
		    actualEnum.close();
		    actualEnum = null;
		}
		String lowerBound = this.lowBounds.removeFirst();
		maxset = rangeBounds.get(lowerBound);
		// 
		actualEnum = reader.terms(termTemplate.createTerm(lowerBound));
		currentTerm = actualEnum.term();

		if (currentTerm != null
		        && (termCompare(currentTerm) || this.getterms())) {
		    return true;
		}
		// 
		currentTerm = null;
	    }

	    return false;
	}

	public boolean getterms() throws IOException {
	    while (actualEnum.next()) {
		currentTerm = actualEnum.term();
		if (isEndTerm(currentTerm)) {
		    break;
		}
		if (isContainsTerm(currentTerm)) {
		    return true;
		}
	    }

	    return false;
	}

	/** Closes the enumeration to further activity, freeing resources. */
	@Override
	public void close() throws IOException {
	    rangeBounds.clear();
	    lowBounds.clear();
	    super.close();
	}

    }

    @Override
    public String toString(String field) {
	return toString() + ",field=" + field;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result
	        + ((dataType == null) ? 0 : dataType.hashCode());
	result = prime * result + ((field == null) ? 0 : field.hashCode());
	result = prime * result + ((file == null) ? 0 : file.hashCode());
	result = prime * result + precisionStep;
	result = prime * result + ((trans == null) ? 0 : trans.hashCode());
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
	NumericSelectInQuery other = (NumericSelectInQuery) obj;
	if (dataType != other.dataType)
	    return false;
	if (field == null) {
	    if (other.field != null)
		return false;
	} else if (!field.equals(other.field))
	    return false;
	if (file == null) {
	    if (other.file != null)
		return false;
	} else if (!file.equals(other.file))
	    return false;
	if (precisionStep != other.precisionStep)
	    return false;
	if (trans == null) {
	    if (other.trans != null)
		return false;
	} else if (!trans.equals(other.trans))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "NumericSelectInQuery [field=" + field + ", precisionStep="
	        + precisionStep + ", dataType=" + dataType + ", file=" + file
	        + ", trans=" + trans + "]";
    }

}
