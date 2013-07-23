package com.alimama.mdrill.adhoc;
import java.sql.*;

public class MySqlConn {

	String m_url = "jdbc:mysql://tiansuan1.kgb.cm4:3306/adhoc_download";

	String m_username = "adhoc";
	String m_passwd = "adhoc";
	Connection m_conn = null;
	DatabaseMetaData m_dbmd = null;
	Statement m_sql = null;

	public MySqlConn(String m_url, String m_username, String m_passwd) {
		this.m_url = m_url;
		this.m_username = m_username;
		this.m_passwd = m_passwd;
	}

	public void close() throws SQLException {
		if (m_sql != null) {
			m_sql.close();
			m_sql = null;
		}

		m_dbmd = null;

		if (m_conn != null) {
			m_conn.close();
			m_conn = null;
		}

	}

	public Connection getConn() throws SQLException {

		if (m_conn == null) {
			try {
				Class.forName("org.gjt.mm.mysql.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			m_conn = DriverManager.getConnection(m_url, m_username, m_passwd);
		}
		return m_conn;
	}

	public DatabaseMetaData getDbm() throws SQLException {

		if (m_dbmd == null) {

			m_dbmd = getConn().getMetaData();
		}
		return m_dbmd;
	}

	public Statement getState() throws SQLException {
		if (m_sql == null) {

			m_sql = getConn().createStatement();
		}
		return m_sql;
	}

}
