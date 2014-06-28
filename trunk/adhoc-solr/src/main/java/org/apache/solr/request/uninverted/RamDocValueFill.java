package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.DocValuesReader;
import org.apache.solr.request.BlockBufferPool.BlockArray;
import org.apache.solr.request.uninverted.TermIndex.IndexSearch;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.FieldDatatype;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocIterator;

public class RamDocValueFill {
//	public static Logger log = LoggerFactory.getLogger(RamDocValueFill.class);

	public static int Fill(UnInvertedField inv, int maxdoc, boolean isinit,
			TermIndex ti, DocValuesReader quicktisInput, int fieldNumber,
			boolean isReadDouble, BitDocSet baseAdvanceDocs)
			throws IOException, CloneNotSupportedException {

		DocValuesReader docValues = (DocValuesReader) quicktisInput.clone();
		synchronized (docValues.getLock()) {
			docValues.seekTo(fieldNumber, isinit);
			int doc = -1;
			int tm = 0;
	
			if (inv.fieldDataType == FieldDatatype.d_double) {
				if (baseAdvanceDocs != null) {
					DocIterator iter = baseAdvanceDocs.iterator();
					while (iter.hasNext()) {
						doc = iter.nextDoc();
						tm = docValues.readTm(doc);
						inv.markDocTm(doc, tm, isinit);
						inv.bits.add(doc);
						if (isReadDouble) {
							inv.setTmValueDouble(tm,RamTermNumValue.EMPTY_FOR_MARK);
						}
					}
				} else {
					for (doc = 0; doc < maxdoc; doc++) {
						tm = docValues.readTm(doc);
						inv.markDocTm(doc, tm, isinit);
						inv.bits.add(doc);
						if (isReadDouble) {
							inv.setTmValueDouble(tm,RamTermNumValue.EMPTY_FOR_MARK);
						}
					}
				}
			} else {
				if (baseAdvanceDocs != null) {
					DocIterator iter = baseAdvanceDocs.iterator();
					while (iter.hasNext()) {
						doc = iter.nextDoc();
						tm = docValues.readTm(doc);
						inv.markDocTm(doc, tm, isinit);
						inv.bits.add(doc);
						if (isReadDouble) {
							inv.setTmValueLong(tm,	(long) RamTermNumValue.EMPTY_FOR_MARK);
						}
					}
				} else {
					for (doc = 0; doc < maxdoc; doc++) {
						tm = docValues.readTm(doc);
						inv.markDocTm(doc, tm, isinit);
						inv.bits.add(doc);
						if (isReadDouble) {
							inv.setTmValueLong(tm,	(long) RamTermNumValue.EMPTY_FOR_MARK);
						}
					}
				}
			}
	
			if (isReadDouble) {
				if (inv.fieldDataType == FieldDatatype.d_double) {
					BlockArray<Double> list = inv.getTmValueDouble();
					for (int i = 0; i < list.getSize(); i++) {
						if (list.get(i) <= RamTermNumValue.EMPTY_FOR_MARK_FORCMP) {
							long val = docValues.readTmValue(i);
							list.set(i, Double.longBitsToDouble(val));
						}
					}
				} else {
					BlockArray<Long> list = inv.getTmValueLong();
					for (int i = 0; i < list.getSize(); i++) {
						if (list.get(i) <= RamTermNumValue.EMPTY_FOR_MARK_FORCMP) {
							long val = docValues.readTmValue(i);
							list.set(i, val);
						}
					}
				}
	
			}
			if (isinit) {
				ArrayList<String> lst = docValues.lst;
				ti.nTerms = docValues.maxtm;
				ti.sizeOfStrings = docValues.sizeOfStrings;
				ti.index = new IndexSearch();
				ti.index.index = lst != null ? lst.toArray(new String[lst.size()]) : new String[0];
	
			}
	
			return docValues.maxtm;
		
		}

	}

}
