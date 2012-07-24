package com.eucalyptus.webui.server.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetWrapper {
	
	public ResultSetWrapper(java.sql.Connection conn, ResultSet rs) {
		this.conn = conn;
		this.rs = rs;
	}

	public ResultSet getResultSet() {
		return this.rs;
	}
	
	public void close() throws SQLException {
		this.rs.close();
		this.conn.close();
	}
	
	private java.sql.Connection conn;
	private ResultSet rs;
}
