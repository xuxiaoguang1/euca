package com.eucalyptus.webui.server.device;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.IPServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceIPService {
    
    private static DeviceIPService instance = new DeviceIPService();
    
    public static DeviceIPService getInstance() {
        return instance;
    }
    
    private DeviceIPService() {
        /* do nothing */
    }
    
    private LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    private static final List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("Index", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("IP Addr", "IP地址"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("IP Type", "地址类型"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("State", "服务状态"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("Account", "账户名称"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("User", "用户名称"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(false, "0%", new ClientMessage("Desc", "服务描述"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Start Time", "开始时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("End Time", "结束时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Remains", "剩余(天)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("Create", "添加时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("Modify", "修改时间"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "0%", new ClientMessage("Desc(IP)", "地址描述"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("Create(IP)", "地址添加时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("Modify(IP)", "地址修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.IP.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.IP.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.IP.IP_ADDR: return DBTable.IP_SERVICE.IP_ADDR;
        case CellTableColumns.IP.IP_TYPE: return DBTable.IP_SERVICE.IP_TYPE;
        case CellTableColumns.IP.IP_CREATIONTIME: return DBTable.IP_SERVICE.IP_CREATIONTIME;
        case CellTableColumns.IP.IP_MODIFIEDTIME: return DBTable.IP_SERVICE.IP_MODIFIEDTIME;
        case CellTableColumns.IP.IP_SERVICE_STARTTIME: return DBTable.IP_SERVICE.IP_SERVICE_STARTTIME;
        case CellTableColumns.IP.IP_SERVICE_ENDTIME: return DBTable.IP_SERVICE.IP_SERVICE_ENDTIME;
        case CellTableColumns.IP.IP_SERVICE_LIFE: return DBTable.IP_SERVICE.IP_SERVICE_LIFE;
        case CellTableColumns.IP.IP_SERVICE_STATE: return DBTable.IP_SERVICE.IP_SERVICE_STATE;
        case CellTableColumns.IP.IP_SERVICE_CREATIONTIME: return DBTable.IP_SERVICE.IP_SERVICE_CREATIONTIME;
        case CellTableColumns.IP.IP_SERVICE_MODIFIEDTIME: return DBTable.IP_SERVICE.IP_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    public SearchResult lookupIPByDate(Session session, SearchRange range, IPType ip_type, IPState is_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            LoginUserProfile user = getUser(session);
            int account_id;
            int user_id;
            if (user.isSystemAdmin()) {
                account_id = -1;
                user_id = -1;
            }
            else if (user.isAccountAdmin()) {
                account_id = user.getAccountId();
                user_id = -1;
            }
            else {
                account_id = user.getAccountId();
                user_id = user.getUserId();
            }
            conn = DBProcWrapper.getConnection();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPServiceByDate(conn, ip_type, is_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), account_id, user_id);
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int ip_id = DBData.getInt(rs, IP_SERVICE.IP_ID);
                    String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
                    ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
                    String ip_desc = DBData.getString(rs, IP_SERVICE.IP_DESC);
                    Date ip_creationtime = DBData.getDate(rs, IP_SERVICE.IP_CREATIONTIME);
                    Date ip_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_MODIFIEDTIME);
                    String account_name = null;
                    String user_name = null;
                    String is_desc = null;
                    is_state = IPState.getIPState(DBData.getInt(rs, IP_SERVICE.IP_SERVICE_STATE));
                    Date is_starttime = null;
                    Date is_endtime = null;
                    String is_life = null;
                    Date is_creationtime = null;
                    Date is_modifiedtime = null;
                    if (is_state != IPState.RESERVED) {
                        account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                        user_name = DBData.getString(rs, USER.USER_NAME);
                        is_desc = DBData.getString(rs, IP_SERVICE.IP_SERVICE_DESC);
                        is_starttime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_STARTTIME);
                        is_endtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_ENDTIME);
                        is_life = DBData.getString(rs, IP_SERVICE.IP_SERVICE_LIFE);
                        if (is_life != null) {
                            is_life = Integer.toString(Math.max(0, Integer.parseInt(is_life) + 1));
                        }
                        is_creationtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_CREATIONTIME);
                        is_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_MODIFIEDTIME);
                    }
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.IP.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.IP.IP_ID, ip_id);
                    row.setColumn(CellTableColumns.IP.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.IP.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.IP.IP_ADDR, ip_addr);
                    row.setColumn(CellTableColumns.IP.IP_TYPE, ip_type.toString());
                    row.setColumn(CellTableColumns.IP.IP_SERVICE_STATE, is_state.toString());
                    row.setColumn(CellTableColumns.IP.ACCOUNT_NAME, account_name);
                    row.setColumn(CellTableColumns.IP.USER_NAME, user_name);
                    row.setColumn(CellTableColumns.IP.IP_SERVICE_DESC, is_desc);
                    row.setColumn(CellTableColumns.IP.IP_SERVICE_STARTTIME, is_starttime);
                    row.setColumn(CellTableColumns.IP.IP_SERVICE_ENDTIME, is_endtime);
                    row.setColumn(CellTableColumns.IP.IP_SERVICE_LIFE, is_life);
                    row.setColumn(CellTableColumns.IP.IP_SERVICE_CREATIONTIME, is_creationtime);
                    row.setColumn(CellTableColumns.IP.IP_SERVICE_MODIFIEDTIME, is_modifiedtime);
                    row.setColumn(CellTableColumns.IP.IP_DESC, ip_desc);
                    row.setColumn(CellTableColumns.IP.IP_CREATIONTIME, ip_creationtime);
                    row.setColumn(CellTableColumns.IP.IP_MODIFIEDTIME, ip_modifiedtime);
                    rows.add(new SearchResultRow(row.toList()));
                }
            }
            for (SearchResultRow row : rows) {
                System.out.println(row);
            }
            range.setLength(rows.size());
            return new SearchResult(index, range, FIELDS_DESC, rows);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public Map<String, Integer> lookupIPReservedByIPType(Session session, IPType ip_type) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceIPDBProcWrapper.lookupIPsReservedByType(conn, ip_type);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public Map<Integer, Integer> lookupIPCountsByState(Session session, IPType ip_type) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            LoginUserProfile user = getUser(session);
            int account_id;
            int user_id;
            if (user.isSystemAdmin()) {
                account_id = -1;
                user_id = -1;
            }
            else if (user.isAccountAdmin()) {
                account_id = user.getAccountId();
                user_id = -1;
            }
            else {
                account_id = user.getAccountId();
                user_id = user.getUserId();
            }
            conn = DBProcWrapper.getConnection();
            return DeviceIPDBProcWrapper.lookupIPCountsByState(conn, ip_type, account_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public IPServiceInfo lookupIPServiceInfoByID(int ip_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, false, ip_id);
            String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
            String ip_desc = DBData.getString(rs, IP_SERVICE.IP_DESC);
            IPType ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
            Date ip_creationtime = DBData.getDate(rs, IP_SERVICE.IP_CREATIONTIME);
            Date ip_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_MODIFIEDTIME);
            IPState is_state = IPState.getIPState(DBData.getInt(rs, IP_SERVICE.IP_SERVICE_STATE));
            String is_desc = null;
            Date is_starttime = null;
            Date is_endtime = null;
            Date is_creationtime = null;
            Date is_modifiedtime = null;
            int user_id = -1;
            if (is_state != IPState.RESERVED) {
                is_desc = DBData.getString(rs, IP_SERVICE.IP_SERVICE_DESC);
                is_starttime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_STARTTIME);
                is_endtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_ENDTIME);
                is_creationtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_CREATIONTIME);
                is_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_MODIFIEDTIME);
                user_id = DBData.getInt(rs, IP_SERVICE.USER_ID);
            }
            return new IPServiceInfo(ip_id, ip_addr, ip_desc, ip_type, ip_creationtime, ip_modifiedtime, is_desc, is_starttime, is_endtime, is_state, is_creationtime, is_modifiedtime, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void createIP(boolean force, Session session, String ip_addr, String ip_desc, IPType ip_type) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ip_addr == null || ip_addr.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address", "IP地址"));
        }
        if (ip_type == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address Type", "IP地址类型"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceIPDBProcWrapper.createIP(conn, ip_addr, ip_desc, ip_type);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    protected int createIPService(Connection conn, String ip_addr, String ip_desc, IPType ip_type, String is_desc, IPState is_state, Date is_starttime, Date is_endtime, int user_id) throws Exception {
        if (ip_addr == null || ip_addr.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address", "IP地址"));
        }
        if (ip_type == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address Type", "IP地址类型"));
        }
        int ip_id = DeviceIPDBProcWrapper.createIP(conn, ip_addr, ip_desc, ip_type);
        createIPService(conn, is_desc, is_state, is_starttime, is_endtime, ip_id, user_id);
        return ip_id;
    }
    
    private void createIPService(Connection conn, String is_desc, IPState is_state, Date is_starttime, Date is_endtime, int ip_id, int user_id) throws Exception {
        if (is_state == null || is_state == IPState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
        }
        if (is_starttime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Start Time", "开始日期"));
        }
        if (is_endtime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("End Time", "结束日期"));
        }
        if (DBData.calcLife(is_endtime, is_starttime) <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Date Value", "服务日期"));
        }
        if (user_id == -1) {
            throw new EucalyptusServiceException(new ClientMessage("User Name", "用户名称"));
        }
        if (is_desc == null) {
            is_desc = "";
        }
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
        if (rs.getInt(IP_SERVICE.IP_SERVICE_STATE.toString()) != IPState.RESERVED.getValue()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
        }
        rs.updateString(IP_SERVICE.IP_SERVICE_DESC.toString(), is_desc);
        rs.updateString(IP_SERVICE.IP_SERVICE_STARTTIME.toString(), DBStringBuilder.getDate(is_starttime));
        rs.updateString(IP_SERVICE.IP_SERVICE_ENDTIME.toString(), DBStringBuilder.getDate(is_endtime));
        rs.updateInt(IP_SERVICE.IP_SERVICE_STATE.toString(), is_state.getValue());
        rs.updateString(IP_SERVICE.IP_SERVICE_CREATIONTIME.toString(), DBStringBuilder.getDate());
        rs.updateNull(IP_SERVICE.IP_SERVICE_MODIFIEDTIME.toString());
        rs.updateInt(IP_SERVICE.USER_ID.toString(), user_id);
        rs.updateRow();
    }
    
    public void createIPService(boolean force, Session session, String is_desc, IPState is_state, Date is_starttime, Date is_endtime, int ip_id, int user_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            createIPService(conn, is_desc, is_state, is_starttime, is_endtime, ip_id, user_id);
            conn.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            DBProcWrapper.rollback(conn);
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void deleteIP(boolean force, Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ip_ids != null && !ip_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
                for (int ip_id : ip_ids) {
                    ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
                    if (rs.getInt(IP_SERVICE.IP_SERVICE_STATE.toString()) == IPState.RESERVED.getValue()) {
                        rs.deleteRow();
                    }
                    else {
                        throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
                    }
                }
                conn.commit();
            }
            catch (Exception e) {
                e.printStackTrace();
                DBProcWrapper.rollback(conn);
                throw new EucalyptusServiceException(e);
            }
            finally {
                DBProcWrapper.close(conn);
            }
        }
    }
    
    public void deleteIPService(boolean force, Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ip_ids != null && !ip_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
                for (int ip_id : ip_ids) {
                    ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
                    if (rs.getInt(IP_SERVICE.IP_SERVICE_STATE.toString()) != IPState.RESERVED.getValue()) {
                        rs.updateString(IP_SERVICE.IP_SERVICE_DESC.toString(), "");
                        rs.updateNull(IP_SERVICE.IP_SERVICE_STARTTIME.toString());
                        rs.updateNull(IP_SERVICE.IP_SERVICE_ENDTIME.toString());
                        rs.updateInt(IP_SERVICE.IP_SERVICE_STATE.toString(), IPState.RESERVED.getValue());
                        rs.updateNull(IP_SERVICE.IP_SERVICE_CREATIONTIME.toString());
                        rs.updateNull(IP_SERVICE.IP_SERVICE_MODIFIEDTIME.toString());
                        rs.updateNull(IP_SERVICE.USER_ID.toString());
                        rs.updateRow();
                    }
                    else {
                        throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
                    }
                }
                conn.commit();
            }
            catch (Exception e) {
                e.printStackTrace();
                DBProcWrapper.rollback(conn);
                throw new EucalyptusServiceException(e);
            }
            finally {
                DBProcWrapper.close(conn);
            }
        }
    }
    
    public void modifyIP(boolean force, Session session, int ip_id, String ip_desc, IPType ip_type) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ip_type == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address Type", "IP地址类型"));
        }
        if (ip_desc == null) {
            ip_desc = "";
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
            rs.updateString(IP_SERVICE.IP_DESC.toString(), ip_desc);
            rs.updateInt(IP_SERVICE.IP_TYPE.toString(), ip_type.getValue());
            rs.updateString(IP_SERVICE.IP_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            conn.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            DBProcWrapper.rollback(conn);
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void modifyIPService(boolean force, Session session, int ip_id, String is_desc, Date is_starttime, Date is_endtime) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (is_starttime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Start Time", "开始日期"));
        }
        if (is_endtime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("End Time", "结束日期"));
        }
        if (DBData.calcLife(is_endtime, is_starttime) <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Date Value", "服务日期"));
        }
        if (is_desc == null) {
            is_desc = "";
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
            if (rs.getInt(IP_SERVICE.IP_SERVICE_STATE.toString()) == IPState.RESERVED.getValue()) {
                throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
            }
            rs.updateString(IP_SERVICE.IP_SERVICE_DESC.toString(), is_desc);
            rs.updateString(IP_SERVICE.IP_SERVICE_STARTTIME.toString(), DBStringBuilder.getDate(is_starttime));
            rs.updateString(IP_SERVICE.IP_SERVICE_ENDTIME.toString(), DBStringBuilder.getDate(is_endtime));
            rs.updateString(IP_SERVICE.IP_SERVICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            conn.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            DBProcWrapper.rollback(conn);
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void stopIPServiceByID(boolean force, Session session, int ip_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
            if (rs.getInt(IP_SERVICE.IP_SERVICE_STATE.toString()) == IPState.RESERVED.getValue()) {
                throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
            }
            rs.updateInt(IP_SERVICE.IP_SERVICE_STATE.toString(), IPState.STOP.getValue());
            rs.updateRow();
            conn.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            DBProcWrapper.rollback(conn);
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void modifyIPServiceState(boolean force, Session session, int ip_id, IPState is_state) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (is_state == null || is_state == IPState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
            if (rs.getInt(IP_SERVICE.IP_SERVICE_STATE.toString()) == IPState.RESERVED.getValue()) {
                throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
            }
            rs.updateInt(IP_SERVICE.IP_SERVICE_STATE.toString(), is_state.getValue());
            rs.updateRow();
            conn.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            DBProcWrapper.rollback(conn);
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
}

class DeviceIPDBProcWrapper {
    
    private static final Logger log = Logger.getLogger(DeviceIPDBProcWrapper.class.getName());
    
    public static ResultSet lookupIPByID(Connection conn, boolean updatable, int ip_id) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(IP_SERVICE);
        sb.append(" WHERE ").append(IP_SERVICE.IP_ID).append(" = ").append(ip_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
    }
    
    public static Map<String, Integer> lookupIPsReservedByType(Connection conn, IPType type) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP_SERVICE.IP_ADDR).append(", ").append(IP_SERVICE.IP_ID);
        sb.append(" FROM ").append(IP_SERVICE);
        sb.append(" WHERE ").append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(IPState.RESERVED.getValue());
        if (type != null) {
            sb.append(" AND ").append(IP_SERVICE.IP_TYPE).append(" = ").append(type.getValue());
        }
        
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<String, Integer> result = new HashMap<String, Integer>();
        while (rs.next()) {
            result.put(rs.getString(1), rs.getInt(2));
        }
        return result;
    }
    
    public static ResultSet lookupIPServiceByDate(Connection conn, IPType type, IPState state, Date beg, Date end, DBTableColumn sorted, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP_SERVICE.ANY).append(", ");
        sb.append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
        sb.appendDateLifeRemains(IP_SERVICE.IP_SERVICE_STARTTIME, IP_SERVICE.IP_SERVICE_ENDTIME, IP_SERVICE.IP_SERVICE_LIFE).append(" FROM "); {
            sb.append(IP_SERVICE);
            sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
            sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
        }
        sb.append(" WHERE 1=1");
        if (type != null) {
            sb.append(" AND ").append(IP_SERVICE.IP_TYPE).append(" = ").append(type.getValue());
        }
        if (state != null) {
            sb.append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(state.getValue());
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(IP_SERVICE.USER_ID).append(" = ").append(user_id);
        }
        if (account_id >= 0) {
            sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (beg != null || end != null) {
            sb.append(" AND ("); {
                sb.appendDateBound(IP_SERVICE.IP_SERVICE_CREATIONTIME, beg, end);
                sb.append(" OR ");
                sb.appendDateBound(IP_SERVICE.IP_SERVICE_MODIFIEDTIME, beg, end);
            }
            sb.append(")");
        }
        if (sorted != null) {
            sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
        }
        return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
    }
    
    public static int createIP(Connection conn, String ip_addr, String ip_desc, IPType type) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(IP_SERVICE).append(" ("); {
            sb.append(IP_SERVICE.IP_ADDR).append(", ");
            sb.append(IP_SERVICE.IP_DESC).append(", ");
            sb.append(IP_SERVICE.IP_TYPE).append(", ");
            sb.append(IP_SERVICE.IP_CREATIONTIME).append(", ");
            sb.append(IP_SERVICE.IP_MODIFIEDTIME).append(", ");
            sb.append(IP_SERVICE.IP_SERVICE_DESC).append(", ");
            sb.append(IP_SERVICE.IP_SERVICE_STARTTIME).append(", ");
            sb.append(IP_SERVICE.IP_SERVICE_ENDTIME).append(", ");
            sb.append(IP_SERVICE.IP_SERVICE_STATE).append(", ");
            sb.append(IP_SERVICE.IP_SERVICE_CREATIONTIME).append(", ");
            sb.append(IP_SERVICE.IP_SERVICE_MODIFIEDTIME).append(", ");
            sb.append(IP_SERVICE.USER_ID);
        }
        sb.append(") VALUES ("); {
            sb.appendString(ip_addr).append(", ");
            sb.appendString(ip_desc).append(", ");
            sb.append(type.getValue()).append(", ");
            sb.appendDate().append(", ");
            sb.appendNull().append(", ");
            sb.appendString("").append(", ");
            sb.appendNull().append(", ");
            sb.appendNull().append(", ");
            sb.append(IPState.RESERVED.getValue()).append(", ");
            sb.appendNull().append(", ");
            sb.appendNull().append(", ");
            sb.appendNull();
        }
        sb.append(")");
        Statement stat = conn.createStatement();
        stat.executeUpdate(sb.toSql(log), new String[]{IP_SERVICE.IP_ID.toString()});
        ResultSet rs = stat.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }
    
    public static Map<Integer, Integer> lookupIPCountsByState(Connection conn, IPType type, int account_id, int user_id) throws Exception {
        DBTableUser USER = DBTable.USER;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(IP_SERVICE.IP_SERVICE_STATE).append(", count(").append(IP_SERVICE.IP_SERVICE_STATE).append(")").append(" FROM ");
        sb.append(IP_SERVICE);
        if (account_id >= 0) {
            sb.append(" LEFT JOIN ").append(USER).append(" ON ");
            sb.append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
        }
        sb.append(" WHERE 1=1");
        if (type != null) {
            sb.append(" AND ").append(IP_SERVICE.IP_TYPE).append(" = ").append(type.getValue());
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(IP_SERVICE.USER_ID).append(" = ").append(user_id);
        }
        if (account_id >= 0) {
            sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
        }
        sb.append(" GROUP BY ").append(IP_SERVICE.IP_SERVICE_STATE);
        
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        int sum = 0;
        while (rs.next()) {
            int state = rs.getInt(1);
            int count = rs.getInt(2);
            sum += count;
            result.put(state, count);
        }
        result.put(-1, sum);
        return result;
    }
    
}
