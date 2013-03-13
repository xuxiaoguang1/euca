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

import org.apache.log4j.Logger;

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
import com.eucalyptus.webui.shared.resource.device.DiskInfo;
import com.eucalyptus.webui.shared.resource.device.DiskServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceDiskService {
    
    private static LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    private static final List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("Index", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Server", "所属服务器"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("State", "服务状态"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Account", "账户名称"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("User", "用户名称"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "0%", new ClientMessage("Desc", "服务描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Total(MB)", "总数量(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Used(MB)", "占用数量(MB)"),
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
            new SearchResultFieldDesc(false, "0%", new ClientMessage("Desc(HW)", "硬件描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("Create(HW)", "硬件添加时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("Modify(HW)", "硬件修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private static DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.DISK.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.DISK.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.DISK.SERVER_NAME: return DBTable.SERVER.SERVER_NAME;
        case CellTableColumns.DISK.DISK_TOTAL: return DBTable.DISK.DISK_TOTAL;
        case CellTableColumns.DISK.DISK_SERVICE_USED: return DBTable.DISK_SERVICE.DISK_SERVICE_USED;
        case CellTableColumns.DISK.DISK_CREATIONTIME: return DBTable.DISK.DISK_CREATIONTIME;
        case CellTableColumns.DISK.DISK_MODIFIEDTIME: return DBTable.DISK.DISK_MODIFIEDTIME;
        case CellTableColumns.DISK.DISK_SERVICE_STARTTIME: return DBTable.DISK_SERVICE.DISK_SERVICE_STARTTIME;
        case CellTableColumns.DISK.DISK_SERVICE_ENDTIME: return DBTable.DISK_SERVICE.DISK_SERVICE_ENDTIME;
        case CellTableColumns.DISK.DISK_SERVICE_LIFE: return DBTable.DISK_SERVICE.DISK_SERVICE_LIFE;
        case CellTableColumns.DISK.DISK_SERVICE_STATE: return DBTable.DISK_SERVICE.DISK_SERVICE_STATE;
        case CellTableColumns.DISK.DISK_SERVICE_CREATIONTIME: return DBTable.DISK_SERVICE.DISK_SERVICE_CREATIONTIME;
        case CellTableColumns.DISK.DISK_SERVICE_MODIFIEDTIME: return DBTable.DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupDisk(Session session, SearchRange range, DiskState ds_state) throws EucalyptusServiceException {
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
            DBTableServer SERVER = DBTable.SERVER;
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDisk(conn, ds_state, getSortColumn(range), range.isAscending(), account_id, user_id);
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int disk_id = DBData.getInt(rs, DISK.DISK_ID);
                    int ds_id = DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_ID);
                    String disk_desc = DBData.getString(rs, DISK.DISK_DESC);
                    String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
                    long disk_total = DBData.getLong(rs, DISK.DISK_TOTAL);
                    Date disk_creationtime = DBData.getDate(rs, DISK.DISK_CREATIONTIME);
                    Date disk_modifiedtime = DBData.getDate(rs, DISK.DISK_MODIFIEDTIME);
                    ds_state = DiskState.getDiskState(DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_STATE));
                    String account_name = null;
                    String user_name = null;
                    String ds_desc = null;
                    long ds_used = DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED);
                    Date ds_starttime = null;
                    Date ds_endtime = null;
                    String ds_life = null;
                    Date ds_creationtime = null;
                    Date ds_modifiedtime = null;
                    if (ds_state != DiskState.RESERVED) {
                        account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                        user_name = DBData.getString(rs, USER.USER_NAME);
                        ds_desc = DBData.getString(rs, DISK_SERVICE.DISK_SERVICE_DESC);
                        ds_starttime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_STARTTIME);
                        ds_endtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_ENDTIME);
                        ds_life = DBData.getString(rs, DISK_SERVICE.DISK_SERVICE_LIFE);
                        if (ds_life != null) {
                            ds_life = Integer.toString(Math.max(0, Integer.parseInt(ds_life) + 1));
                        }
                        ds_creationtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_CREATIONTIME);
                        ds_modifiedtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME);
                    }
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.DISK.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_ID, ds_id);
                    row.setColumn(CellTableColumns.DISK.DISK_ID, disk_id);
                    row.setColumn(CellTableColumns.DISK.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.DISK.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.DISK.SERVER_NAME, server_name);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_STATE, ds_state.toString());
                    row.setColumn(CellTableColumns.DISK.ACCOUNT_NAME, account_name);
                    row.setColumn(CellTableColumns.DISK.USER_NAME, user_name);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_DESC, ds_desc);
                    row.setColumn(CellTableColumns.DISK.DISK_TOTAL, disk_total);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_USED, ds_used);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_STARTTIME, ds_starttime);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_ENDTIME, ds_endtime);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_LIFE, ds_life);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_CREATIONTIME, ds_creationtime);
                    row.setColumn(CellTableColumns.DISK.DISK_SERVICE_MODIFIEDTIME, ds_modifiedtime);
                    row.setColumn(CellTableColumns.DISK.DISK_DESC, disk_desc);
                    row.setColumn(CellTableColumns.DISK.DISK_CREATIONTIME, disk_creationtime);
                    row.setColumn(CellTableColumns.DISK.DISK_MODIFIEDTIME, disk_modifiedtime);
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
    
    public static Map<Integer, Long> lookupDiskCounts(Session session) throws EucalyptusServiceException {
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
            return DeviceDiskDBProcWrapper.lookupDiskCounts(conn, account_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static DiskInfo lookupDiskInfoByID(int disk_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, false, disk_id);
            String disk_desc = DBData.getString(rs, DISK.DISK_DESC);
            long disk_total = DBData.getLong(rs, DISK.DISK_TOTAL);
            Date disk_creationtime = DBData.getDate(rs, DISK.DISK_CREATIONTIME);
            Date disk_modifiedtime = DBData.getDate(rs, DISK.DISK_MODIFIEDTIME);
            int server_id = DBData.getInt(rs, DISK.SERVER_ID);
            rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, false, disk_id);
            long ds_reserved = DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED);
            return new DiskInfo(disk_id, disk_desc, disk_total, ds_reserved, disk_creationtime, disk_modifiedtime, server_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static DiskServiceInfo lookupDiskServiceInfoByID(int ds_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, false, ds_id);
            String ds_desc = DBData.getString(rs, DISK_SERVICE.DISK_SERVICE_DESC);
            long ds_used = DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED);
            DiskState disk_state = DiskState.getDiskState(DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_STATE));
            Date ds_creationtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_CREATIONTIME);
            Date ds_modifiedtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME);
            int disk_id = DBData.getInt(rs, DISK_SERVICE.DISK_ID);
            int user_id = DBData.getInt(rs, DISK_SERVICE.USER_ID);
            return new DiskServiceInfo(ds_id, ds_desc, ds_used, disk_state, ds_creationtime, ds_modifiedtime, disk_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void incDiskTotal(boolean force, Session session, String disk_desc, long disk_total, int server_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (disk_desc == null) {
            disk_desc = "";
        }
        long resize = Math.max(0, disk_total);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            int disk_id = DeviceDiskDBProcWrapper.lookupDiskIDByServerID(conn, server_id);
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id);
            if (resize != 0) {
                rs.updateLong(DISK.DISK_TOTAL.toString(), rs.getLong(DISK.DISK_TOTAL.toString()) + resize);
            }
            rs.updateString(DISK.DISK_DESC.toString(), disk_desc);
            rs.updateString(DISK.DISK_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            if (resize != 0) {
                rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
                rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString()) + resize);
                rs.updateRow();
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
    
    public static void decDiskTotal(boolean force, Session session, List<Integer> disk_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (disk_ids == null || disk_ids.isEmpty()) {
            return;
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            for (int disk_id : disk_ids) {
                ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
                long resize = rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString());
                if (resize > 0) {
                    rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), 0);
                    rs.updateRow();
                    rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id);
                    rs.updateLong(DISK.DISK_TOTAL.toString(), rs.getLong(DISK.DISK_TOTAL.toString()) - resize);
                    rs.updateString(DISK.DISK_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
                    rs.updateRow();
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
    
    public static void modifyDisk(boolean force, Session session, int disk_id, String disk_desc, long disk_total) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (disk_desc == null) {
            disk_desc = "";
        }
        disk_total = Math.max(0, disk_total);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id);
            long resize = disk_total - rs.getLong(DISK.DISK_TOTAL.toString());
            rs.updateLong(DISK.DISK_TOTAL.toString(), disk_total);
            rs.updateString(DISK.DISK_DESC.toString(), disk_desc);
            rs.updateString(DISK.DISK_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            if (resize != 0) {
                rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
                long reserved = rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString()) + resize;
                if (reserved >= 0) {
                    rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), reserved);
                    rs.updateRow();
                }
                else {
                    throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Size", "硬盘大小"));
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
    
    static void createDiskByServerID(Connection conn, String server_desc, int server_id) throws Exception {
        int disk_id = DeviceDiskDBProcWrapper.createDisk(conn, server_desc, 0, server_id);
        DeviceDiskDBProcWrapper.createDiskService(conn, null, 0, DiskState.RESERVED, disk_id, -1);
    }
    
    static void deleteDiskByServerID(Connection conn, int server_id) throws Exception {
        int disk_id = DeviceDiskDBProcWrapper.lookupDiskIDByServerID(conn, server_id);
        DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id).deleteRow();
        DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id).deleteRow();
    }
    
    static int createDiskService(Connection conn, String ds_desc, long ds_used, DiskState ds_state, int user_id, int server_id) throws Exception {
        if (ds_used <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Size", "硬盘大小"));
        }
        if (ds_state == null || ds_state == DiskState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Service State", "硬盘服务状态"));
        }
        if (user_id == -1) {
            throw new EucalyptusServiceException(new ClientMessage("User Name", "用户名称"));
        }
        int disk_id = DeviceDiskDBProcWrapper.lookupDiskIDByServerID(conn, server_id);
        DBTableDisk DISK = DBTableDisk.DISK;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
        long reserved = rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString());
        if (reserved >= ds_used) {
            rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), reserved - ds_used);
            rs.updateRow();
        }
        else {
            rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), 0);
            rs.updateRow();
            rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id);
            rs.updateLong(DISK.DISK_TOTAL.toString(), rs.getLong(DISK.DISK_TOTAL.toString()) + ds_used - reserved);
            rs.updateRow();
        }
        return DeviceDiskDBProcWrapper.createDiskService(conn, ds_desc, ds_used, ds_state, disk_id, user_id);
    }
    
    static void deleteDiskService(Connection conn, int ds_id) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
        int disk_id = rs.getInt(DISK_SERVICE.DISK_ID.toString());
        long resize = rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString());
        rs.deleteRow();
        rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
        rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString()) + resize);
        rs.updateRow();
    }
    
    static void stopDiskServiceByServerID(Connection conn, int server_id) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        int disk_id = DeviceDiskDBProcWrapper.lookupDiskIDByServerID(conn, server_id);
        for (int ds_id : DeviceDiskDBProcWrapper.lookupDiskServiceIDsByDiskID(conn, disk_id)) {
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
            rs.updateInt(DISK_SERVICE.DISK_SERVICE_STATE.toString(), DiskState.STOP.getValue());
            rs.updateRow();
        }
    }
    
    static void modifyDiskServiceState(Connection conn, int ds_id, DiskState ds_state) throws Exception {
        if (ds_state == null || ds_state == DiskState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Service State", "硬盘服务状态"));
        }
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
        rs.updateInt(DISK_SERVICE.DISK_SERVICE_STATE.toString(), ds_state.getValue());
        rs.updateRow();
    }
    
    static class DeviceDiskDBProcWrapper {
        
        private static final Logger log = Logger.getLogger(DeviceDiskDBProcWrapper.class.getName());
        
        public static int lookupDiskIDByServerID(Connection conn, int server_id) throws Exception {
            DBTableDisk DISK = DBTable.DISK;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(DISK.DISK_ID).append(" FROM ").append(DISK);
            sb.append(" WHERE ").append(DISK.SERVER_ID).append(" = ").append(server_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            rs.next();
            return rs.getInt(1);
        }
        
        public static List<Integer> lookupDiskServiceIDsByDiskID(Connection conn, int disk_id) throws Exception {
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(DISK_SERVICE.DISK_SERVICE_ID).append(" FROM ").append(DISK_SERVICE);
            sb.append(" WHERE ").append(DISK_SERVICE.DISK_ID).append(" = ").append(disk_id);
            sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(" != ").append(DiskState.RESERVED.getValue());
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            List<Integer> result = new LinkedList<Integer>();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;
        }
        
        public static ResultSet lookupDiskByID(Connection conn, boolean updatable, int disk_id) throws Exception {
            DBTableDisk DISK = DBTable.DISK;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(DISK);
            sb.append(" WHERE ").append(DISK.DISK_ID).append(" = ").append(disk_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupDiskServiceByID(Connection conn, boolean updatable, int ds_id) throws Exception {
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(DISK_SERVICE);
            sb.append(" WHERE ").append(DISK_SERVICE.DISK_SERVICE_ID).append(" = ").append(ds_id);
            sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(" != ").append(DiskState.RESERVED.getValue());
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupDiskServiceReservedByID(Connection conn, boolean updatable, int disk_id) throws Exception {
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(DISK_SERVICE);
            sb.append(" WHERE ").append(DISK_SERVICE.DISK_ID).append(" = ").append(disk_id);
            sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(" = ").append(DiskState.RESERVED.getValue());
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupDisk(Connection conn, DiskState state, DBTableColumn sorted, boolean isAscending, int account_id, int user_id) throws Exception {
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableServer SERVER = DBTable.SERVER;
            DBTableUserApp USER_APP = DBTable.USER_APP;
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(DISK.ANY).append(", ").append(DISK_SERVICE.ANY).append(", ");
            sb.append(SERVER.SERVER_NAME).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
            sb.append(USER_APP.SERVICE_STARTTIME).append(" AS ").append(DISK_SERVICE.DISK_SERVICE_STARTTIME).append(", ");
            sb.append(USER_APP.SERVICE_ENDTIME).append(" AS ").append(DISK_SERVICE.DISK_SERVICE_ENDTIME).append(", ");
            sb.appendDateLifeRemains(USER_APP.SERVICE_STARTTIME, USER_APP.SERVICE_ENDTIME, DISK_SERVICE.DISK_SERVICE_LIFE).append(" FROM "); {
                sb.append(DISK_SERVICE);
                sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(DISK_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
                sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
                sb.append(" LEFT JOIN ").append(DISK).append(" ON ").append(DISK_SERVICE.DISK_ID).append(" = ").append(DISK.DISK_ID);
                sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(DISK.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
                sb.append(" LEFT JOIN ").append(USER_APP).append(" ON ").append(DISK_SERVICE.DISK_SERVICE_ID).append(" = ").append(USER_APP.DISK_SERVICE_ID);
            }
            sb.append(" WHERE ").append(DISK_SERVICE.DISK_SERVICE_USED).append(" != ").append(0);
            if (state != null) {
                sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(" = ").append(state.getValue());
            }
            if (user_id >= 0) {
                sb.append(" AND ").append(DISK_SERVICE.USER_ID).append(" = ").append(user_id);
            }
            if (account_id >= 0) {
                sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
            }
            if (sorted != null) {
                sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
            }
            return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        }
        
        public static int createDisk(Connection conn, String disk_desc, long disk_total, int server_id) throws Exception {
            DBTableDisk DISK = DBTable.DISK;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(DISK).append(" ("); {
                sb.append(DISK.DISK_DESC).append(", ");
                sb.append(DISK.DISK_TOTAL).append(", ");
                sb.append(DISK.SERVER_ID).append(", ");
                sb.append(DISK.DISK_CREATIONTIME).append(", ");
                sb.append(DISK.DISK_MODIFIEDTIME);
            }
            sb.append(") VALUES ("); {
                sb.appendString(disk_desc).append(", ");
                sb.append(disk_total).append(", ");
                sb.append(server_id).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull();
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{DISK.DISK_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
        public static int createDiskService(Connection conn, String ds_desc, long ds_used, DiskState state, int disk_id, int user_id) throws Exception {
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(DISK_SERVICE).append(" ("); {
                sb.append(DISK_SERVICE.DISK_SERVICE_DESC).append(", ");
                sb.append(DISK_SERVICE.DISK_SERVICE_USED).append(", ");
                sb.append(DISK_SERVICE.DISK_SERVICE_STATE).append(", ");
                sb.append(DISK_SERVICE.DISK_SERVICE_CREATIONTIME).append(", ");
                sb.append(DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME).append(", ");
                sb.append(DISK_SERVICE.DISK_ID).append(", ");
                sb.append(DISK_SERVICE.USER_ID);
            }
            sb.append(") VALUES ("); {
                sb.appendString(ds_desc).append(", ");
                sb.append(ds_used).append(", ");
                sb.append(state.getValue()).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull().append(", ");
                sb.append(disk_id).append(", ");
                if (user_id == -1) {
                    sb.appendNull();
                }
                else {
                    sb.append(user_id);
                }
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{DISK_SERVICE.DISK_SERVICE_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
        public static Map<Integer, Long> lookupDiskCounts(Connection conn, int account_id, int user_id) throws Exception {
            DBTableUser USER = DBTable.USER;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(DISK_SERVICE.DISK_SERVICE_STATE).append(", sum(").append(DISK_SERVICE.DISK_SERVICE_USED).append(")").append(" FROM ");
            sb.append(DISK_SERVICE);
            if (account_id >= 0) {
                sb.append(" LEFT JOIN ").append(USER).append(" ON ");
                sb.append(DISK_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
            }
            sb.append(" WHERE 1=1");
            if (user_id >= 0) {
                sb.append(" AND ").append(DISK_SERVICE.USER_ID).append(" = ").append(user_id);
            }
            if (account_id >= 0) {
                sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
            }
            sb.append(" GROUP BY ").append(DISK_SERVICE.DISK_SERVICE_STATE);
            
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<Integer, Long> result = new HashMap<Integer, Long>();
            long sum = 0;
            while (rs.next()) {
                int state = rs.getInt(1);
                long count = rs.getLong(2);
                sum += count;
                result.put(state, count);
            }
            result.put(-1, sum);
            return result;
        }

    }
    
}
