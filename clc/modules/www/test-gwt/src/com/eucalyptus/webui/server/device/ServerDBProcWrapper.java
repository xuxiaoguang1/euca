package com.eucalyptus.webui.server.device;

import java.util.logging.Logger;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableName;

public class ServerDBProcWrapper {
	
	final private static Logger LOG = Logger.getLogger(ServerDBProcWrapper.class.getName());
	
	public ResultSetWrapper queryAllServers(int queryState) {
		DBProcWrapper dbproc = DBProcWrapper.Instance();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableName.SERVER);
		sb.append(".* ");
		sb.append(" FROM ");
		sb.append(DBTableName.SERVER);
		sb.append(" WHERE 1=1");
		
		LOG.info(sb.toString());
		
		try {
			return dbproc.query(sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
