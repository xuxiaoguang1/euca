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
import com.eucalyptus.webui.shared.resource.device.BWServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceBWService {
    
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Max BW", "带宽上限(KB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("BW", "带宽(KB)"),
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
        case CellTableColumns.BW.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.BW.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.BW.IP_ADDR: return DBTable.IP_SERVICE.IP_ADDR;
        case CellTableColumns.BW.IP_TYPE: return DBTable.IP_SERVICE.IP_TYPE;
        case CellTableColumns.BW.BW_SERVICE_BW_MAX: return DBTable.BW_SERVICE.BW_SERVICE_BW_MAX;
        case CellTableColumns.BW.BW_SERVICE_BW: return DBTable.BW_SERVICE.BW_SERVICE_BW;
        case CellTableColumns.BW.BW_SERVICE_DESC: return DBTable.BW_SERVICE.BW_SERVICE_DESC;
        case CellTableColumns.BW.BW_SERVICE_STARTTIME: return DBTable.BW_SERVICE.BW_SERVICE_STARTTIME;
        case CellTableColumns.BW.BW_SERVICE_ENDTIME: return DBTable.BW_SERVICE.BW_SERVICE_ENDTIME;
        case CellTableColumns.BW.BW_SERVICE_LIFE: return DBTable.BW_SERVICE.BW_SERVICE_LIFE;
        case CellTableColumns.BW.BW_SERVICE_CREATIONTIME: return DBTable.BW_SERVICE.BW_SERVICE_CREATIONTIME;
        case CellTableColumns.BW.BW_SERVICE_MODIFIEDTIME: return DBTable.BW_SERVICE.BW_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupBWService(Session session, SearchRange range) throws EucalyptusServiceException {
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
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            ResultSet rs = DeviceBWDBProcWrapper.lookupBWService(conn, getSortColumn(range), range.isAscending(), account_id, user_id);
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int bs_id = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_ID);
                    String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
                    IPType ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
                    String bs_desc = DBData.getString(rs, BW_SERVICE.BW_SERVICE_DESC);
                    int bs_bw_max = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW_MAX);
                    String account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                    String user_name = DBData.getString(rs, USER.USER_NAME);
                    Date bs_starttime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_STARTTIME);
                    Date bs_endtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_ENDTIME);
                    String bs_life = DBData.getString(rs, BW_SERVICE.BW_SERVICE_LIFE);
                    if (bs_life != null) {
                        bs_life = Integer.toString(Math.max(0, Integer.parseInt(bs_life) + 1));
                    }
                    int bs_bw = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW);
                    Date bs_creationtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_CREATIONTIME);
                    Date bs_modifiedtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_MODIFIEDTIME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.BW.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_ID, bs_id);
                    row.setColumn(CellTableColumns.BW.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.BW.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.BW.IP_ADDR, ip_addr);
                    row.setColumn(CellTableColumns.BW.IP_TYPE, ip_type.toString());
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_BW_MAX, bs_bw_max);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_BW, bs_bw);
                    row.setColumn(CellTableColumns.BW.ACCOUNT_NAME, account_name);
                    row.setColumn(CellTableColumns.BW.USER_NAME, user_name);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_DESC, bs_desc);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_STARTTIME, bs_starttime);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_ENDTIME, bs_endtime);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_LIFE, bs_life);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_CREATIONTIME, bs_creationtime);
                    row.setColumn(CellTableColumns.BW.BW_SERVICE_MODIFIEDTIME, bs_modifiedtime);
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
    
    public static Map<String, Integer> lookupIPsWithoutBWService(boolean force, Session session, IPType ip_type, int account_id, int user_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceBWDBProcWrapper.lookupIPsWithoutBWService(conn, ip_type, account_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static BWServiceInfo lookupBWServiceInfoByID(int bs_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            ResultSet rs = DeviceBWDBProcWrapper.lookupBWServiceByID(conn, false, bs_id);
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            String bs_desc = DBData.getString(rs, BW_SERVICE.BW_SERVICE_DESC);
            Date bs_starttime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_STARTTIME);
            Date bs_endtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_ENDTIME);
            int bs_bw = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW);
            int bs_bw_max = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW_MAX);
            Date bs_creationtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_CREATIONTIME);
            Date bs_modifiedtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_MODIFIEDTIME);
            int ip_id = DBData.getInt(rs, BW_SERVICE.IP_ID);
            return new BWServiceInfo(bs_id, bs_desc, bs_starttime, bs_endtime, bs_bw, bs_bw_max, bs_creationtime, bs_modifiedtime, ip_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    static int createBWService(Connection conn, String bs_desc, int bs_bw_max, int ip_id) throws Exception {
        return DeviceBWDBProcWrapper.createBWService(conn, bs_desc, bs_bw_max, null, null, ip_id);
    }
    
    static void deleteBWService(Connection conn, int bs_id) throws Exception {
        DeviceBWDBProcWrapper.lookupBWServiceByID(conn, true, bs_id).deleteRow();
    }
    
    public static void createBWService(boolean force, Session session, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, int ip_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        bs_bw_max = Math.max(0, bs_bw_max);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceBWDBProcWrapper.createBWService(conn, bs_desc, bs_bw_max, bs_starttime, bs_endtime, ip_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void deleteBWService(boolean force, Session session, List<Integer> bs_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (bs_ids != null && !bs_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                for (int bs_id : bs_ids) {
                    DeviceBWDBProcWrapper.lookupBWServiceByID(conn, true, bs_id).deleteRow();
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
    
    public static void modifyBWService(boolean force, Session session, int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (bs_starttime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Start Time", "开始日期"));
        }
        if (bs_endtime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("End Time", "结束日期"));
        }
        if (DBData.calcLife(bs_endtime, bs_starttime) <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Date Value", "服务日期"));
        }
        if (bs_desc == null) {
            bs_desc = "";
        }
        bs_bw_max = Math.max(0, bs_bw_max);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            ResultSet rs = DeviceBWDBProcWrapper.lookupBWServiceByID(conn, true, bs_id);
            rs.updateString(BW_SERVICE.BW_SERVICE_DESC.toString(), bs_desc);
            rs.updateInt(BW_SERVICE.BW_SERVICE_BW_MAX.toString(), bs_bw_max);
            rs.updateString(BW_SERVICE.BW_SERVICE_STARTTIME.toString(), DBStringBuilder.getDate(bs_starttime));
            rs.updateString(BW_SERVICE.BW_SERVICE_ENDTIME.toString(), DBStringBuilder.getDate(bs_endtime));
            rs.updateString(BW_SERVICE.BW_SERVICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
    public static void updateBWServiceBandwidth(int bs_id, int bs_bw) throws EucalyptusServiceException {
        bs_bw = Math.max(0, bs_bw);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            ResultSet rs = DeviceBWDBProcWrapper.lookupBWServiceByID(conn, true, bs_id);
            rs.updateInt(BW_SERVICE.BW_SERVICE_BW.toString(), bs_bw);
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
    
    static class DeviceBWDBProcWrapper {

        private static final Logger log = Logger.getLogger(DeviceBWDBProcWrapper.class.getName());
        
        public static ResultSet lookupBWServiceByID(Connection conn, boolean updatable, int bs_id) throws Exception {
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(BW_SERVICE);
            sb.append(" WHERE ").append(BW_SERVICE.BW_SERVICE_ID).append(" = ").append(bs_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static Map<String, Integer> lookupIPsWithoutBWService(Connection conn, IPType type, int account_id, int user_id) throws Exception {
            DBTableUser USER = DBTable.USER;
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(IP_SERVICE.IP_ADDR).append(", ").append(IP_SERVICE.IP_ID).append(" FROM "); {
                sb.append(IP_SERVICE);
                sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
            }
            sb.append(" WHERE ");
            sb.append(IP_SERVICE.IP_ID).append(" NOT IN ").append("("); {
                sb.append("SELECT ").append(BW_SERVICE.IP_ID).append(" FROM ").append(BW_SERVICE).append(" WHERE 1=1");
            }
            sb.append(")");
            if (type != null) {
                sb.append(" AND ").append(IP_SERVICE.IP_TYPE).append(" = ").append(type.getValue());
            }
            if (user_id >= 0) {
                sb.append(" AND ").append(IP_SERVICE.USER_ID).append(" = ").append(user_id);
            }
            if (account_id >= 0) {
                sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
            }
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        }
        
        public static ResultSet lookupBWService(Connection conn, DBTableColumn sorted, boolean isAscending, int account_id, int user_id) throws Exception {
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(BW_SERVICE.ANY).append(", ").append(IP_SERVICE.IP_ADDR).append(", ").append(IP_SERVICE.IP_TYPE).append(", ");
            sb.append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
            sb.appendDateLifeRemains(BW_SERVICE.BW_SERVICE_STARTTIME, BW_SERVICE.BW_SERVICE_ENDTIME, BW_SERVICE.BW_SERVICE_LIFE).append(" FROM "); {
                sb.append(BW_SERVICE);
                sb.append(" LEFT JOIN ").append(IP_SERVICE).append(" ON ").append(BW_SERVICE.IP_ID).append(" = ").append(IP_SERVICE.IP_ID);
                sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
                sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
            }
            sb.append(" WHERE 1=1");
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
        
        public static int createBWService(Connection conn, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, int ip_id) throws Exception {
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(BW_SERVICE).append(" ("); {
                sb.append(BW_SERVICE.BW_SERVICE_DESC).append(", ");
                sb.append(BW_SERVICE.BW_SERVICE_STARTTIME).append(", ");
                sb.append(BW_SERVICE.BW_SERVICE_ENDTIME).append(", ");
                sb.append(BW_SERVICE.BW_SERVICE_BW).append(", ");
                sb.append(BW_SERVICE.BW_SERVICE_BW_MAX).append(", ");
                sb.append(BW_SERVICE.BW_SERVICE_CREATIONTIME).append(", ");
                sb.append(BW_SERVICE.BW_SERVICE_MODIFIEDTIME).append(", ");
                sb.append(BW_SERVICE.IP_ID);
            }
            sb.append(") VALUES ("); {
                sb.appendString(bs_desc).append(", ");
                sb.appendDate(bs_starttime).append(", ");
                sb.appendDate(bs_endtime).append(", ");
                sb.append(0).append(", ");
                sb.append(bs_bw_max).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull().append(", ");
                sb.append(ip_id);
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{BW_SERVICE.BW_SERVICE_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
    }
    
}
