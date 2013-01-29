package com.eucalyptus.webui.server.device;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceAccountService {
    
    private static DeviceAccountService instance = new DeviceAccountService();
    
    public static DeviceAccountService getInstance() {
        return instance;
    }
    
    private DeviceAccountService() {
        /* do nothing */
    }
    
    private LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    public Map<Integer, String> lookupAccountNames(Session session) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
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
    
    public Map<Integer, String> lookupUserNamesByAccountName(Session session, int user_id) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceAccountDBProcWrapper.lookupUserNamesByAccountID(conn, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
}

class DeviceAccountDBProcWrapper {
    
    private static final Logger log = Logger.getLogger(DeviceAccountDBProcWrapper.class.getName());
    
    public static Map<Integer, String> lookupAccountNames(Connection conn) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(ACCOUNT.ACCOUNT_ID).append(", ").append(ACCOUNT.ACCOUNT_NAME);
        sb.append(" FROM ").append(ACCOUNT);
        sb.append(" WHERE 1=1");
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<Integer, String> result = new HashMap<Integer, String>();
        while (rs.next()) {
            result.put(rs.getInt(ACCOUNT.ACCOUNT_ID.toString()), rs.getString(ACCOUNT.ACCOUNT_NAME.toString()));
        }
        return result;
    }
    
    public static Map<Integer, String> lookupUserNamesByAccountID(Connection conn, int user_id) throws Exception {
        DBTableUser USER = DBTable.USER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(USER.USER_ID).append(", ").append(USER.USER_NAME);
        sb.append(" FROM ").append(USER);
        sb.append(" WHERE ").append(USER.ACCOUNT_ID).append(" = ").append(user_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<Integer, String> result = new HashMap<Integer, String>();
        while (rs.next()) {
            result.put(rs.getInt(USER.USER_ID.toString()), rs.getString(USER.USER_NAME.toString()));
        }
        return result;
    }
    
}
