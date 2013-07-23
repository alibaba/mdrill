package com.alipay.tiansuan.solrplugin;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.ToStringUtils;
import java.io.IOException;
import java.util.Arrays;

public class StringContainsQuery extends MultiTermQuery {
	private static final long serialVersionUID = 1L;
	private String field;
	private String[] contains;

	public StringContainsQuery(String[] contains, String field) {
		this.field = field;
		this.contains = contains;
	}

	public static class ListTermEnum extends FilteredTermEnum {
		private boolean endEnum = false;
		private String field;
		private String[] contains;

		public ListTermEnum(IndexReader reader, String field, String[] contains)
				throws IOException {
			this.field = field;
			this.contains = contains;
			this.setEnum(reader.terms(new Term(this.field)));
		}

		@Override
		protected boolean termCompare(Term term) {
			if (!term.field().equals(this.field)) {
				endEnum = true;
				return false;
			}

			String termVal = term.text();
			for (String contain : this.contains) {
				if (termVal.indexOf(contain) >= 0) {
					return true;
				}
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

	}

	@Override
	protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
		return new ListTermEnum(reader, this.field, this.contains);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((this.contains == null) ? 0 : Arrays.hashCode(this.contains));
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
		StringContainsQuery other = (StringContainsQuery) obj;
		if (contains == null) {
			if (other.contains != null)
				return false;
		} else if (!Arrays.equals(contains, other.contains))
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
		return "SelectInListQuery [term=" + field + ", contains="
				+ Arrays.toString(contains) + ", hashcode="
				+ Arrays.hashCode(this.contains) + "]";
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
