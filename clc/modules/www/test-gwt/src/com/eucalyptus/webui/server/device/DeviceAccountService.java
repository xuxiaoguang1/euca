package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceAccountService {
    
    private DeviceAccountDBProcWrapper dbproc = new DeviceAccountDBProcWrapper();
    
    private LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    private DeviceAccountService() {
    }
    
    private static DeviceAccountService instance = new DeviceAccountService();
    
    public static DeviceAccountService getInstance() {
        return instance;
    }
    
    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
    
    public synchronized List<String> lookupAccountNames(Session session) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupAccountNames();
            ResultSet rs = rsw.getResultSet();
            List<String> account_name_list = new LinkedList<String>();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            while (rs.next()) {
                String account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                account_name_list.add(account_name);
            }
            return account_name_list;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException();
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public synchronized List<String> lookupUserNamesByAccountName(Session session, String account_name) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (isEmpty(account_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的账户名称"));
        }
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupUserNamesByAccountName(account_name);
            ResultSet rs = rsw.getResultSet();
            List<String> user_name_list = new LinkedList<String>();
            DBTableUser USER = DBTable.USER;
            while (rs.next()) {
                String user_name = DBData.getString(rs, USER.USER_NAME);
                user_name_list.add(user_name);
            }
            return user_name_list;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException();
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}

class DeviceAccountDBProcWrapper {
    
    private static final Logger LOG = Logger.getLogger(DeviceAccountDBProcWrapper.class.getName());
    
    private DBProcWrapper wrapper = DBProcWrapper.Instance();
    
    private ResultSetWrapper doQuery(String request) throws Exception {
        LOG.info(request);
        return wrapper.query(request);
    }

    public ResultSetWrapper lookupAccountNames() throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT DISTINCT(").append(ACCOUNT.ACCOUNT_NAME).append(") FROM ").append(ACCOUNT).append(" WHERE 1=1");
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupUserNamesByAccountName(String account_name) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT DISTINCT(").append(USER.USER_NAME).append(") FROM ");
        sb.append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
        sb.append(" WHERE ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name);
        return doQuery(sb.toString());
    }
    
}
