package com.eucalyptus.webui.server.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.shared.auth.AccessKey;

public class AccessKeyDBProcWrapper {

	public void addAccessKey(AccessKey key) throws AccessKeySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addAccessKeySQL(key);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new AccessKeySyncException("Database fails");
		}
	}

	public void deleteAccessKey(int key_id) throws AccessKeySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = deleteAccessKeySQL(key_id);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new AccessKeySyncException("Database fails");
		}
	}
	
	public List<AccessKey> getAccessKeys(String userId) throws AccessKeySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = getAccessKeysSQL(userId);

		ResultSet res;
		try {
			res = dbProc.query(sql.toString()).getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AccessKeySyncException("Database fails");
		}
		
		List<AccessKey> list = null;
		if(res != null){
			list = new ArrayList<AccessKey>();
			DateFormat df = new SimpleDateFormat(AccessKey.DATE_PATTERN);
			try {
				while(res.next()){
					AccessKey key = null;
					try {
						key = new AccessKey(
								Integer.valueOf(res.getString(DBTableColName.USER_KEY.ID)),
								res.getString(DBTableColName.USER_KEY.AKEY), 
								res.getString(DBTableColName.USER_KEY.SKEY),
								Boolean.parseBoolean(res.getString(DBTableColName.USER_KEY.ACTIVE)),
								df.parse(res.getString(DBTableColName.USER_KEY.CREATED_DATE)),
								res.getString(DBTableColName.USER_KEY.USER_ID)
								);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(key != null){
						list.add(key);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<AccessKey> listAccessKeys() throws AccessKeySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = listAccessKeysSQL();

		ResultSet res;
		try {
			res = dbProc.query(sql.toString()).getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AccessKeySyncException("Database fails");
		}
		
		List<AccessKey> list = null;
		if(res != null){
			list = new ArrayList<AccessKey>();
			DateFormat df = new SimpleDateFormat(AccessKey.DATE_PATTERN);
			try {
				while(res.next()){
					AccessKey key = null;
					try {
						key = new AccessKey(
								Integer.valueOf(res.getString(DBTableColName.USER_KEY.ID)),
								res.getString(DBTableColName.USER_KEY.AKEY), 
								res.getString(DBTableColName.USER_KEY.SKEY),
								Boolean.parseBoolean(res.getString(DBTableColName.USER_KEY.ACTIVE)),
								df.parse(res.getString(DBTableColName.USER_KEY.CREATED_DATE)),
								res.getString(DBTableColName.USER_KEY.USER_ID)
								);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(key != null){
						list.add(key);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	private String addAccessKeySQL(AccessKey key) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").append(DBTableName.USER_KEY).append(" ( ")
				.append(DBTableColName.USER_KEY.ID).append(", ")
				.append(DBTableColName.USER_KEY.AKEY).append(", ")
				.append(DBTableColName.USER_KEY.SKEY).append(", ")
				.append(DBTableColName.USER_KEY.ACTIVE).append(", ")
				.append(DBTableColName.USER_KEY.CREATED_DATE).append(", ")
				.append(DBTableColName.USER_KEY.USER_ID).append(") VALUES (null, ");

		str.append("'");
		str.append(key.getAccessKey());
		str.append("', '");

		str.append(key.getSecretKey());
		str.append("', '");

		str.append(key.isActive());
		str.append("', '");

		
		DateFormat df = new SimpleDateFormat(AccessKey.DATE_PATTERN);
		str.append(df.format(key.getCreatedDate()));
		str.append("', '");

		str.append(key.getUserId());
		str.append("')");

		return str.toString();
	}

	private String deleteAccessKeySQL(int key_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ");
		sb.append(" FROM ").append(DBTableName.USER_KEY);
		sb.append(" WHERE ");
		sb.append(DBTableColName.USER_KEY.ID).append(" = ").append(key_id);
		return sb.toString();
	}
	
	private String getAccessKeysSQL(String userId) {
		StringBuilder sql = new StringBuilder().
				append("SELECT * FROM ").append(DBTableName.USER_KEY)
				.append(" WHERE ").append(DBTableColName.USER_KEY.USER_ID).append(" = ")
				.append(userId);
		return sql.toString();
	}

	private String listAccessKeysSQL() {
		StringBuilder sql = new StringBuilder("SELECT * FROM ").append(
				DBTableName.USER_KEY).append(" WHERE 1=1 ");
		return sql.toString();
	}

}
