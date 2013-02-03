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
import com.eucalyptus.webui.shared.resource.device.DiskInfo;
import com.eucalyptus.webui.shared.resource.device.DiskServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceDiskService {
    
    private static DeviceDiskService instance = new DeviceDiskService();
    
    public static DeviceDiskService getInstance() {
        return instance;
    }
    
    private DeviceDiskService() {
        /* do nothing */
    }
    
    
    private LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    private static final List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("Index", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Name", "硬盘名称"),
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
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.DISK.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.DISK.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.DISK.SERVER_NAME: return DBTable.SERVER.SERVER_NAME;
        case CellTableColumns.DISK.DISK_NAME: return DBTable.DISK.DISK_NAME;
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
    
    private static final long DISK_UNIT = 1000 * 1000;
    
    public SearchResult lookupDiskByDate(Session session, SearchRange range, DiskState ds_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
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
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskByDate(conn, ds_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), account_id, user_id);
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int disk_id = DBData.getInt(rs, DISK.DISK_ID);
                    int ds_id = DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_ID);
                    String disk_name = DBData.getString(rs, DISK.DISK_NAME);
                    String disk_desc = DBData.getString(rs, DISK.DISK_DESC);
                    String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
                    long disk_total = DBData.getLong(rs, DISK.DISK_TOTAL) / DISK_UNIT;
                    Date disk_creationtime = DBData.getDate(rs, DISK.DISK_CREATIONTIME);
                    Date disk_modifiedtime = DBData.getDate(rs, DISK.DISK_MODIFIEDTIME);
                    ds_state = DiskState.getDiskState(DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_STATE));
                    String account_name = null;
                    String user_name = null;
                    String ds_desc = null;
                    long ds_used = DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED) / DISK_UNIT;
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
                    row.setColumn(CellTableColumns.DISK.DISK_NAME, disk_name);
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
    
    public Map<String, Integer> lookupDiskNamesByServerID(boolean force, Session session, int server_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceDiskDBProcWrapper.lookupDiskNamesByServerID(conn, server_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public Map<Integer, Long> lookupDiskCounts(Session session) throws EucalyptusServiceException {
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
            Map<Integer, Long> result = DeviceDiskDBProcWrapper.lookupDiskCounts(conn, account_id, user_id);
            for (Map.Entry<Integer, Long> entry : result.entrySet()) {
                entry.setValue(entry.getValue() / DISK_UNIT);
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public DiskInfo lookupDiskInfoByID(int disk_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, false, disk_id);
            String disk_name = DBData.getString(rs, DISK.DISK_NAME);
            String disk_desc = DBData.getString(rs, DISK.DISK_DESC);
            long disk_size = DBData.getLong(rs, DISK.DISK_TOTAL) / DISK_UNIT;
            Date disk_creationtime = DBData.getDate(rs, DISK.DISK_CREATIONTIME);
            Date disk_modifiedtime = DBData.getDate(rs, DISK.DISK_MODIFIEDTIME);
            int server_id = DBData.getInt(rs, DISK.SERVER_ID);
            rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, false, disk_id);
            long ds_reserved = DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED) / DISK_UNIT;
            return new DiskInfo(disk_id, disk_name, disk_desc, disk_size, ds_reserved, disk_creationtime, disk_modifiedtime, server_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public DiskServiceInfo lookupDiskServiceInfoByID(int ds_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, false, ds_id);
            String ds_desc = DBData.getString(rs, DISK_SERVICE.DISK_SERVICE_DESC);
            long ds_used = DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED) / DISK_UNIT;
            Date ds_starttime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_STARTTIME);
            Date ds_endtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_ENDTIME);
            DiskState disk_state = DiskState.getDiskState(DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_STATE));
            Date ds_creationtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_CREATIONTIME);
            Date ds_modifiedtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME);
            int disk_id = DBData.getInt(rs, DISK_SERVICE.DISK_ID);
            int user_id = DBData.getInt(rs, DISK_SERVICE.USER_ID);
            return new DiskServiceInfo(ds_id, ds_desc, ds_used, ds_starttime, ds_endtime, disk_state, ds_creationtime, ds_modifiedtime, disk_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void createDisk(boolean force, Session session, String disk_name, String disk_desc, long disk_size, int server_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (disk_name == null || disk_name.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Name", "硬盘名称"));
        }
        long disk_total = Math.max(0, disk_size) * DISK_UNIT;
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            int disk_id = DeviceDiskDBProcWrapper.createDisk(conn, disk_name, disk_desc, disk_total, server_id);
            DeviceDiskDBProcWrapper.createDiskService(conn, null, disk_total, null, null, DiskState.RESERVED, disk_id, -1);
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
    
    public void createDiskService(boolean force, Session session, String ds_desc, long ds_size, DiskState ds_state, Date ds_starttime, Date ds_endtime, int disk_id, int user_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            createDiskService(force, conn, ds_desc, ds_size, ds_state, ds_starttime, ds_endtime, disk_id, user_id);
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
    
    protected int createDiskService(boolean force, Connection conn, String ds_desc, long ds_size, DiskState ds_state, Date ds_starttime, Date ds_endtime, int disk_id, int user_id) throws Exception {
        if (ds_size <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Size", "硬盘大小"));
        }
        if (ds_state == null || ds_state == DiskState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Service State", "硬盘服务状态"));
        }
        if (ds_starttime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Start Time", "开始日期"));
        }
        if (ds_endtime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("End Time", "结束日期"));
        }
        if (DBData.calcLife(ds_endtime, ds_starttime) <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Date Value", "服务日期"));
        }
        if (user_id == -1) {
            throw new EucalyptusServiceException(new ClientMessage("User Name", "用户名称"));
        }
        long ds_used = Math.max(0, ds_size) * DISK_UNIT;
        DBTableDisk DISK = DBTableDisk.DISK;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
        long reserved = rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString());
        if (reserved >= ds_used) {
            rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), reserved - ds_used);
            rs.updateRow();
        }
        else if (force) {
            rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), 0);
            rs.updateRow();
            rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id);
            rs.updateLong(DISK.DISK_TOTAL.toString(), rs.getLong(DISK.DISK_TOTAL.toString()) + ds_used - reserved);
            rs.updateRow();
        }
        return DeviceDiskDBProcWrapper.createDiskService(conn, ds_desc, ds_used, ds_starttime, ds_endtime, ds_state, disk_id, user_id);
    }
    
    public void deleteDisk(boolean force, Session session, List<Integer> disk_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (disk_ids != null && !disk_ids.isEmpty()) {
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            for (int disk_id : disk_ids) {
                DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id).deleteRow();
                DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id).deleteRow();
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
    
    public void deleteDiskService(boolean force, Session session, List<Integer> ds_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ds_ids != null && !ds_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
                for (int ds_id : ds_ids) {
                    ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
                    int disk_id = rs.getInt(DISK_SERVICE.DISK_ID.toString());
                    long ds_used = rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString());
                    rs.deleteRow();
                    rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
                    rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString()) + ds_used);
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
    }
    
    public void modifyDisk(boolean force, Session session, int disk_id, String disk_desc, long disk_size) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (disk_desc == null) {
            disk_desc = "";
        }
        long disk_total = Math.max(0, disk_size) * DISK_UNIT;
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
    
    public void modifyDiskService(boolean force, Session session, int ds_id, String ds_desc, long ds_size, Date ds_starttime, Date ds_endtime) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ds_starttime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Start Time", "开始日期"));
        }
        if (ds_endtime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("End Time", "结束日期"));
        }
        if (DBData.calcLife(ds_endtime, ds_starttime) <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Date Value", "服务日期"));
        }
        if (ds_desc == null) {
            ds_desc = "";
        }
        long ds_total = Math.max(0, ds_size) * DISK_UNIT;
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
            long resize = ds_total - rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString());
            rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), ds_total);
            rs.updateString(DISK_SERVICE.DISK_SERVICE_DESC.toString(), ds_desc);
            rs.updateString(DISK_SERVICE.DISK_SERVICE_STARTTIME.toString(), DBStringBuilder.getDate(ds_starttime));
            rs.updateString(DISK_SERVICE.DISK_SERVICE_ENDTIME.toString(), DBStringBuilder.getDate(ds_endtime));
            rs.updateString(DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            if (resize != 0) {
                int disk_id = rs.getInt(DISK_SERVICE.DISK_ID.toString());
                rs = DeviceDiskDBProcWrapper.lookupDiskServiceReservedByID(conn, true, disk_id);
                long reserved = rs.getLong(DISK_SERVICE.DISK_SERVICE_USED.toString()) - resize;
                if (reserved >= 0) {
                    rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), reserved);
                    rs.updateRow();
                }
                else if (force) {
                    rs.updateLong(DISK_SERVICE.DISK_SERVICE_USED.toString(), 0);
                    rs.updateRow();
                    rs = DeviceDiskDBProcWrapper.lookupDiskByID(conn, true, disk_id);
                    rs.updateLong(DISK.DISK_TOTAL.toString(), rs.getLong(DISK.DISK_TOTAL.toString()) - reserved);
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
    
    public void stopDiskServiceByServerID(boolean force, Session session, int server_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            for (Map.Entry<String, Integer> entry : DeviceDiskDBProcWrapper.lookupDiskNamesByServerID(conn, server_id).entrySet()) {
                DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
                for (int ds_id : DeviceDiskDBProcWrapper.lookupDiskServiceIDsByDiskID(conn, entry.getValue())) {
                    ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
                    rs.updateInt(DISK_SERVICE.DISK_SERVICE_STATE.toString(), DiskState.STOP.getValue());
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
    
    public void modifyDiskState(boolean force, Session session, int disk_id, DiskState ds_state) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ds_state == null || ds_state == DiskState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Service State", "硬盘服务状态"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            for (int ds_id : DeviceDiskDBProcWrapper.lookupDiskServiceIDsByDiskID(conn, disk_id)) {
                ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
                rs.updateInt(DISK_SERVICE.DISK_SERVICE_STATE.toString(), ds_state.getValue());
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
    
    public void modifyDiskServiceState(boolean force, Session session, int ds_id, DiskState ds_state) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (ds_state == null || ds_state == DiskState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Service State", "硬盘服务状态"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            ResultSet rs = DeviceDiskDBProcWrapper.lookupDiskServiceByID(conn, true, ds_id);
            rs.updateInt(DISK_SERVICE.DISK_SERVICE_STATE.toString(), ds_state.getValue());
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

class DeviceDiskDBProcWrapper {
    
    private static final Logger log = Logger.getLogger(DeviceDiskDBProcWrapper.class.getName());
    
    public static Map<String, Integer> lookupDiskNamesByServerID(Connection conn, int server_id) throws Exception {
        DBTableDisk DISK = DBTable.DISK;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(DISK.DISK_NAME).append(", ").append(DISK.DISK_ID);
        sb.append(" FROM ").append(DISK);
        sb.append(" WHERE ").append(DISK.SERVER_ID).append(" = ").append(server_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<String, Integer> result = new HashMap<String, Integer>();
        while (rs.next()) {
            result.put(rs.getString(1), rs.getInt(2));
        }
        return result;
    }
    
    public static List<Integer> lookupDiskServiceIDsByDiskID(Connection conn, int disk_id) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(DISK_SERVICE.DISK_SERVICE_ID);
        sb.append(" FROM ").append(DISK_SERVICE);
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
    
    public static ResultSet lookupDiskByDate(Connection conn, DiskState state, Date beg, Date end, DBTableColumn sorted, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableServer SERVER = DBTable.SERVER;
        DBTableDisk DISK = DBTable.DISK;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(DISK.ANY).append(", ").append(DISK_SERVICE.ANY).append(", ");
        sb.append(SERVER.SERVER_NAME).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
        sb.appendDateLifeRemains(DISK_SERVICE.DISK_SERVICE_STARTTIME, DISK_SERVICE.DISK_SERVICE_ENDTIME, DISK_SERVICE.DISK_SERVICE_LIFE).append(" FROM "); {
            sb.append(DISK_SERVICE);
            sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(DISK_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
            sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
            sb.append(" LEFT JOIN ").append(DISK).append(" ON ").append(DISK_SERVICE.DISK_ID).append(" = ").append(DISK.DISK_ID);
            sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(DISK.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
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
        if (beg != null || end != null) {
            sb.append(" AND ("); {
                sb.appendDateBound(DISK_SERVICE.DISK_SERVICE_CREATIONTIME, beg, end);
                sb.append(" OR ");
                sb.appendDateBound(DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME, beg, end);
            }
            sb.append(")");
        }
        if (sorted != null) {
            sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
        }
        return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
    }
    
    public static int createDisk(Connection conn, String disk_name, String disk_desc, long disk_total, int server_id) throws Exception {
        DBTableDisk DISK = DBTable.DISK;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(DISK).append(" ("); {
            sb.append(DISK.DISK_NAME).append(", ");
            sb.append(DISK.DISK_DESC).append(", ");
            sb.append(DISK.DISK_TOTAL).append(", ");
            sb.append(DISK.SERVER_ID).append(", ");
            sb.append(DISK.DISK_CREATIONTIME).append(", ");
            sb.append(DISK.DISK_MODIFIEDTIME);
        }
        sb.append(") VALUES ("); {
            sb.appendString(disk_name).append(", ");
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
    
    public static int createDiskService(Connection conn, String ds_desc, long ds_used, Date ds_starttime, Date ds_endtime, DiskState state, int disk_id, int user_id) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(DISK_SERVICE).append(" ("); {
            sb.append(DISK_SERVICE.DISK_SERVICE_DESC).append(", ");
            sb.append(DISK_SERVICE.DISK_SERVICE_USED).append(", ");
            sb.append(DISK_SERVICE.DISK_SERVICE_STARTTIME).append(", ");
            sb.append(DISK_SERVICE.DISK_SERVICE_ENDTIME).append(", ");
            sb.append(DISK_SERVICE.DISK_SERVICE_STATE).append(", ");
            sb.append(DISK_SERVICE.DISK_SERVICE_CREATIONTIME).append(", ");
            sb.append(DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME).append(", ");
            sb.append(DISK_SERVICE.DISK_ID).append(", ");
            sb.append(DISK_SERVICE.USER_ID);
        }
        sb.append(") VALUES ("); {
            sb.appendString(ds_desc).append(", ");
            sb.append(ds_used).append(", ");
            sb.appendDate(ds_starttime).append(", ");
            sb.appendDate(ds_endtime).append(", ");
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
