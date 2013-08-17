package org.apache.solr.request.uninverted;

import java.io.IOException;
import java.util.HashSet;

import org.apache.solr.request.mdrill.MdrillPorcessUtils;
import org.apache.solr.request.uninverted.UnInvertedFieldUtils.BlockArrayReadInt;
import org.apache.solr.schema.FieldType;

public class UnInvertedFieldTermNumRead {
	public static interface TermNumReadInterface {
		public void setUni(UnInvertedField uni);

		public double quickToDouble(int doc, FieldType ft, NumberedTermEnum te)
				throws IOException;

		public Integer termNum(int doc, Integer def) throws IOException;

		public String tNumToString(int tnum, FieldType ft, NumberedTermEnum te,
				String def) throws IOException;
	}

	public static class TermNumReadNull implements TermNumReadInterface {
		HashSet<Integer> rtn = new HashSet<Integer>();

		public void setUni(UnInvertedField uni) {
		}

		@Override
		public double quickToDouble(int doc, FieldType ft, NumberedTermEnum te)
				throws IOException {
			return UnInvertedField.MINVALUE_FILL;
		}

		public Integer termNum(int doc, Integer def) throws IOException {
			return def;
		}

		@Override
		public String tNumToString(int tnum, FieldType ft, NumberedTermEnum te,
				String def) throws IOException {
			return def;
		}
	}

	public static class TermNumReadSingleNotNull implements
			TermNumReadInterface {
		private UnInvertedField uni;
		private UnInvertedFieldQuickDouble.termDoubleValue td;
		private BlockArrayReadInt tm = null;

		public void setUni(UnInvertedField uni) {
			this.uni = uni;
			this.td = UnInvertedFieldQuickDouble.termDoubleValue.INSTANCE(uni);
			this.tm = UnInvertedFieldUtils.getBlockArrayRead(uni);

		}

		@Override
		public double quickToDouble(int doc, FieldType ft, NumberedTermEnum te)
				throws IOException {
			return this.td.doc(doc, ft, te);
		}

		public Integer termNum(int doc, Integer def) throws IOException {
			int tnum = this.tm.get(doc);
			if (tnum == this.uni.nullTermNum) {
				return def;
			}
			return tnum;
		}

		public String tNumToString(int tnum, FieldType ft, NumberedTermEnum te,
				String def) throws IOException {
			if (tnum == this.uni.nullTermNum) {
				return def;
			}
			String termText = this.uni.getTermText(te, tnum);
			if (termText != null) {
				return ft.indexedToReadable(termText);
			}
			return def;
		}

	}

	public static class TermNumReadMultyNotNull implements TermNumReadInterface {
		UnInvertedField uni;
		BlockArrayReadInt tm;

		public void setUni(UnInvertedField uni) {
			this.uni = uni;
			this.tm = UnInvertedFieldUtils.getBlockArrayRead(uni);
		}

		@Override
		public double quickToDouble(int doc, FieldType ft, NumberedTermEnum te)
				throws IOException {
			int termnum = this.termNum(doc, this.uni.nullTermNum);
			if (termnum == this.uni.nullTermNum) {
				return UnInvertedField.MINVALUE_FILL;
			}

			String termText = this.uni.getTermText(te, termnum);
			if (termText != null) {
				return MdrillPorcessUtils.ParseDouble(ft
						.indexedToReadable(termText));
			}
			return UnInvertedField.MINVALUE_FILL;
		}

		public String tNumToString(int tnum, FieldType ft, NumberedTermEnum te,
				String def) throws IOException {
			if (tnum == this.uni.nullTermNum) {
				return def;
			}
			String termText = this.uni.getTermText(te, tnum);
			if (termText != null) {
				return ft.indexedToReadable(termText);
			}
			return def;
		}

		public Integer termNum(int doc, Integer def) throws IOException {
			int code = this.tm.get(doc);
			if (code == this.uni.nullTermNum) {
				return def;
			}
			if ((code & 0xff) == 1) {
				int pos = code >>> 8;
				int whichArray = (doc >>> 16) & 0xff;
				byte[] arr = uni.tnums[whichArray];
				int tnum = 0;
				for (;;) {
					int delta = 0;
					for (;;) {
						byte b = arr[pos++];
						delta = (delta << 7) | (b & 0x7f);
						if ((b & 0x80) == 0)
							break;
					}
					if (delta == 0)
						break;
					tnum += delta - UnInvertedField.TNUM_OFFSET;
					return tnum;
				}
			} else {
				int tnum = 0;
				int delta = 0;
				for (;;) {
					delta = (delta << 7) | (code & 0x7f);
					if ((code & 0x80) == 0) {
						if (delta == 0)
							break;
						tnum += delta - UnInvertedField.TNUM_OFFSET;
						delta = 0;
						return tnum;
					}
					code >>>= 8;
				}
			}
			return def;
		}
	}

	public static class TermNumReadSingle implements TermNumReadInterface {
		TermNumReadInterface inter;

		public void setUni(UnInvertedField uni) {
			if (uni.index == null && uni.indexbyte == null
					&& uni.indexshort == null) {
				this.inter = new TermNumReadNull();
			} else {
				this.inter = new TermNumReadSingleNotNull();
			}
			this.inter.setUni(uni);
		}

		@Override
		public double quickToDouble(int doc, FieldType ft, NumberedTermEnum te)
				throws IOException {
			return this.inter.quickToDouble(doc, ft, te);
		}

		@Override
		public Integer termNum(int doc, Integer def) throws IOException {
			return this.inter.termNum(doc, def);
		}

		@Override
		public String tNumToString(int tnum, FieldType ft, NumberedTermEnum te,
				String def) throws IOException {
			return this.inter.tNumToString(tnum, ft, te, def);
		}
	}

	public static class TermNumReadMulty implements TermNumReadInterface {
		TermNumReadInterface inter;

		public void setUni(UnInvertedField uni) {
			if (uni.index == null && uni.indexbyte == null
					&& uni.indexshort == null) {
				this.inter = new TermNumReadNull();
			} else {
				this.inter = new TermNumReadMultyNotNull();
			}
			this.inter.setUni(uni);
		}

		@Override
		public double quickToDouble(int doc, FieldType ft, NumberedTermEnum te)
				throws IOException {
			return this.inter.quickToDouble(doc, ft, te);
		}

		@Override
		public Integer termNum(int doc, Integer def) throws IOException {
			return this.inter.termNum(doc, def);
		}

		@Override
		public String tNumToString(int tnum, FieldType ft, NumberedTermEnum te,
				String def) throws IOException {
			return this.inter.tNumToString(tnum, ft, te, def);
		}
	}
}
