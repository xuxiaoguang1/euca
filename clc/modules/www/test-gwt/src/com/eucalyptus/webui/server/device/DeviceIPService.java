package com.eucalyptus.webui.server.device;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
    
    private static LoginUserProfile getUser(Session session) {
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
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    
    private static DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.IP.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.IP.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.IP.IP_ADDR: return DBTable.IP_SERVICE.IP_ADDR;
        case CellTableColumns.IP.IP_TYPE: return DBTable.IP_SERVICE.IP_TYPE;
        case CellTableColumns.IP.IP_SERVICE_STARTTIME: return DBTable.IP_SERVICE.IP_SERVICE_STARTTIME;
        case CellTableColumns.IP.IP_SERVICE_ENDTIME: return DBTable.IP_SERVICE.IP_SERVICE_ENDTIME;
        case CellTableColumns.IP.IP_SERVICE_LIFE: return DBTable.IP_SERVICE.IP_SERVICE_LIFE;
        case CellTableColumns.IP.IP_SERVICE_STATE: return DBTable.IP_SERVICE.IP_SERVICE_STATE;
        case CellTableColumns.IP.IP_SERVICE_CREATIONTIME: return DBTable.IP_SERVICE.IP_SERVICE_CREATIONTIME;
        case CellTableColumns.IP.IP_SERVICE_MODIFIEDTIME: return DBTable.IP_SERVICE.IP_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupIP(Session session, SearchRange range, IPType ip_type, IPState is_state) throws EucalyptusServiceException {
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
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPService(conn, ip_type, is_state, getSortColumn(range), range.isAscending(), account_id, user_id);
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int ip_id = DBData.getInt(rs, IP_SERVICE.IP_ID);
                    String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
                    ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
                    String account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                    String user_name = DBData.getString(rs, USER.USER_NAME);
                    String is_desc = DBData.getString(rs, IP_SERVICE.IP_SERVICE_DESC);
                    is_state = IPState.getIPState(DBData.getInt(rs, IP_SERVICE.IP_SERVICE_STATE));
                    Date is_starttime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_STARTTIME);
                    Date is_endtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_ENDTIME);
                    String is_life = DBData.getString(rs, IP_SERVICE.IP_SERVICE_LIFE);
                    if (is_life != null) {
                        is_life = Integer.toString(Math.max(0, Integer.parseInt(is_life) + 1));
                    }
                    Date is_creationtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_CREATIONTIME);
                    Date is_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_MODIFIEDTIME);
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
    
    public static Map<Integer, Integer> lookupIPCountsByType(Session session, IPType ip_type) throws EucalyptusServiceException {
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
            return DeviceIPDBProcWrapper.lookupIPCountsByType(conn, ip_type, account_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static IPServiceInfo lookupIPServiceInfoByID(int ip_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, false, ip_id);
            String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
            IPType ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
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
            return new IPServiceInfo(ip_id, ip_addr, ip_type, is_desc, is_starttime, is_endtime, is_state, is_creationtime, is_modifiedtime, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    private static int ip = 0;
    private static List<String> allocateAddress(int user_id, IPType ip_type, int count) {
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < count; i ++) {
            list.add("166.111." + user_id + "." + ip++);
        }
        // EucaServiceWrapper.getInstance().allocateAddress(user_id, type, count)
        return list;
    }
    
    static int createIPService(Connection conn, String is_desc, IPType ip_type, IPState is_state, int user_id) throws Exception {
        if (ip_type == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address Type", "IP地址类型"));
        }
        if (user_id == -1) {
            throw new EucalyptusServiceException(new ClientMessage("User Name", "用户名称"));
        }
        if (is_desc == null) {
            is_desc = "";
        }
        String ip_addr = allocateAddress(user_id, ip_type, 1).get(0);
        return DeviceIPDBProcWrapper.createIPService(conn, ip_addr, ip_type, is_state, is_desc, null, null, user_id);
    }
    
    
    public static void createIPService(boolean force, Session session, IPType ip_type, String is_desc, Date is_starttime, Date is_endtime, int count, int user_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ip_type == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address Type", "IP地址类型"));
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
        if (count <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Address Count", "IP地址数量"));
        }
        if (user_id == -1) {
            throw new EucalyptusServiceException(new ClientMessage("User Name", "用户名称"));
        }
        if (is_desc == null) {
            is_desc = "";
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            for (String ip_addr : allocateAddress(user_id, ip_type, count)) {
                DeviceIPDBProcWrapper.createIPService(conn, ip_addr, ip_type, IPState.RESERVED, is_desc, is_starttime, is_endtime, user_id);
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
    
    static void deleteIPService(Connection conn, int ip_id) throws Exception {
        DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id).deleteRow();
    }
    
    public static void deleteIPService(boolean force, Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
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
    
    static void modifyIPServiceState(Connection conn, int ip_id, IPState is_state) throws Exception {
        if (is_state == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("IP Service State", "IP服务状态"));
        }
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        ResultSet rs = DeviceIPDBProcWrapper.lookupIPByID(conn, true, ip_id);
        rs.updateInt(IP_SERVICE.IP_SERVICE_STATE.toString(), is_state.getValue());
        rs.updateRow();
    }
    
    static class DeviceIPDBProcWrapper {
        
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
        
        public static ResultSet lookupIPService(Connection conn, IPType type, IPState state, DBTableColumn sorted, boolean isAscending, int account_id, int user_id) throws Exception {
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
            if (sorted != null) {
                sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
            }
            return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        }
        
        public static int createIPService(Connection conn, String ip_addr, IPType type, IPState state, String is_desc, Date is_starttime, Date is_endtime, int user_id) throws Exception {
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(IP_SERVICE).append(" ("); {
                sb.append(IP_SERVICE.IP_ADDR).append(", ");
                sb.append(IP_SERVICE.IP_TYPE).append(", ");
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
                sb.append(type.getValue()).append(", ");
                sb.appendString(is_desc).append(", ");
                sb.appendDate(is_starttime).append(", ");
                sb.appendDate(is_endtime).append(", ");
                sb.append(state.getValue()).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull().append(", ");
                sb.append(user_id);
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{IP_SERVICE.IP_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
        public static Map<Integer, Integer> lookupIPCountsByType(Connection conn, IPType type, int account_id, int user_id) throws Exception {
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
    
}
