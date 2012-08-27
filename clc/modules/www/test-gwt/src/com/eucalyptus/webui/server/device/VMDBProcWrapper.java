package com.eucalyptus.webui.server.device;

import java.sql.SQLException;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableName;

public class VMDBProcWrapper {
	public ResultSetWrapper queryVMImageType() throws DeviceSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = queryVMImageTypeSql();
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DeviceSyncException("Fail to query VM image type");
		}
	}
	
	private StringBuilder queryVMImageTypeSql() {
		StringBuilder sql = new StringBuilder("SELECT ").
				append(DBTableName.VM_IMAGE_TYPE).append(".*").
				
				append(" FROM ").
				append(DBTableName.VM_IMAGE_TYPE);
		
		return sql;
	}
}
