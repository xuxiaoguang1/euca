package com.eucalyptus.webui.server.device;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.server.db.DBProcWrapper;

public class DeviceAccountService {
    
    public static Map<String, Integer> lookupAccountNames() throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceAccountDBProcWrapper.lookupAccountNames(conn);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static Map<String, Integer> lookupUserNamesByAccountID(int account_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceAccountDBProcWrapper.lookupUserNamesByAccountID(conn, account_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    static class DeviceAccountDBProcWrapper {
        
        private static final Logger log = Logger.getLogger(DeviceAccountDBProcWrapper.class.getName());
        
        public static Map<String, Integer> lookupAccountNames(Connection conn) throws Exception {
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(ACCOUNT.ACCOUNT_NAME).append(", ").append(ACCOUNT.ACCOUNT_ID);
            sb.append(" FROM ").append(ACCOUNT);
            sb.append(" WHERE 1=1");
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        }
        
        public static Map<String, Integer> lookupUserNamesByAccountID(Connection conn, int account_id) throws Exception {
            DBTableUser USER = DBTable.USER;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(USER.USER_NAME).append(", ").append(USER.USER_ID);
            sb.append(" FROM ").append(USER);
            sb.append(" WHERE ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        }
        
    }
    
}
