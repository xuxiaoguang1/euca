package com.eucalyptus.webui.server.db;

/*******************************************************************************
 *Copyright (c) 2009  
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, only version 3 of the License.
 * 
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 *    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 *    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 *    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 *    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 *    THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 *    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 *    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 *    ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************/
/*
 *
 * Author: Lei Zhang sosilent.lzh@gmail.com
 */

import java.sql.*;

import com.eucalyptus.webui.server.config.DBConfig;

public class DBProcWrapper {
	private final static DBProcWrapper dbProc = new DBProcWrapper();
	private DBProcWrapper() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static DBProcWrapper Instance() {
		return dbProc;
	}
	/* 数据库更新操作：
	 *   包括 插入数据，修改数据
	 */
	public void update(String sql) throws SQLException {
		try {
			Connection conn = DriverManager.getConnection(DBConfig.instance().url(), DBConfig.instance().usr(), DBConfig.instance().pwd());
			
			if (!conn.isClosed()) {
				Statement statement = conn.createStatement();
				statement.executeUpdate(sql);
				
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public int insertAndGetInsertId(String sql) throws SQLException {
		int insertId = 0;
		try {
			Connection conn = DriverManager.getConnection(DBConfig.instance().url(), DBConfig.instance().usr(), DBConfig.instance().pwd());
			
			if (!conn.isClosed()) {
				Statement statement = conn.createStatement();
				statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
				
				
				ResultSet rs = statement.getGeneratedKeys();
				if (rs.next()) {
					insertId = rs.getInt(1);
				}
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		
		return insertId;
	}
	
	/* 数据库查询（只读）
	 */
	public ResultSetWrapper query(String sql) throws SQLException {
		try {
			Connection conn = DriverManager.getConnection(DBConfig.instance().url(), DBConfig.instance().usr(), DBConfig.instance().pwd());
			
			if (!conn.isClosed()) {
				Statement statement = conn.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				
				return new ResultSetWrapper(conn, rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		
		return null;
	}
	
	public static Connection getConnection() throws Exception {
	    return DriverManager.getConnection(DBConfig.instance().url(), DBConfig.instance().usr(), DBConfig.instance().pwd());
	}
	
	public static ResultSet queryResultSet(Connection conn, boolean updatable, String sql) throws Exception {
	    Statement stat;
	    if (updatable) {
	        stat = conn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_UPDATABLE);
	    }
	    else {
	        stat = conn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
	    }
	    return stat.executeQuery(sql);
	}
	
	public static void rollback(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void close(Connection conn) {
	    if (conn != null) {
	        try {
	            conn.close();
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
	
}
