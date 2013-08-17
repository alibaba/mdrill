package com.alimama.mdrill.jdbc;
public class ObjectExpression {

		private String columnname = "";
		private String exp = "";
		private Object value = "";
		public String getColumnname() {
			return columnname;
		}
		public void setColumnname(String columnname) {
			this.columnname = columnname;
		}
		public String getExp() {
			return exp;
		}
		public void setExp(String exp) {
			this.exp = exp;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
	}