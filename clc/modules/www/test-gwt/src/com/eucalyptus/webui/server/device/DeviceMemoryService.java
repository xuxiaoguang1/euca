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
import com.eucalyptus.webui.shared.resource.device.MemoryInfo;
import com.eucalyptus.webui.shared.resource.device.MemoryServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceMemoryService {
    
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
        case CellTableColumns.MEMORY.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.MEMORY.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.MEMORY.SERVER_NAME: return DBTable.SERVER.SERVER_NAME;
        case CellTableColumns.MEMORY.MEMORY_TOTAL: return DBTable.MEMORY.MEMORY_TOTAL;
        case CellTableColumns.MEMORY.MEMORY_SERVICE_USED: return DBTable.MEMORY_SERVICE.MEMORY_SERVICE_USED;
        case CellTableColumns.MEMORY.MEMORY_CREATIONTIME: return DBTable.MEMORY.MEMORY_CREATIONTIME;
        case CellTableColumns.MEMORY.MEMORY_MODIFIEDTIME: return DBTable.MEMORY.MEMORY_MODIFIEDTIME;
        case CellTableColumns.MEMORY.MEMORY_SERVICE_STARTTIME: return DBTable.MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME;
        case CellTableColumns.MEMORY.MEMORY_SERVICE_ENDTIME: return DBTable.MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME;
        case CellTableColumns.MEMORY.MEMORY_SERVICE_LIFE: return DBTable.MEMORY_SERVICE.MEMORY_SERVICE_LIFE;
        case CellTableColumns.MEMORY.MEMORY_SERVICE_STATE: return DBTable.MEMORY_SERVICE.MEMORY_SERVICE_STATE;
        case CellTableColumns.MEMORY.MEMORY_SERVICE_CREATIONTIME: return DBTable.MEMORY_SERVICE.MEMORY_SERVICE_CREATIONTIME;
        case CellTableColumns.MEMORY.MEMORY_SERVICE_MODIFIEDTIME: return DBTable.MEMORY_SERVICE.MEMORY_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupMemory(Session session, SearchRange range, MemoryState ms_state) throws EucalyptusServiceException {
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
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemory(conn, ms_state, getSortColumn(range), range.isAscending(), account_id, user_id);
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int mem_id = DBData.getInt(rs, MEMORY.MEMORY_ID);
                    int ms_id = DBData.getInt(rs, MEMORY_SERVICE.MEMORY_SERVICE_ID);
                    String mem_desc = DBData.getString(rs, MEMORY.MEMORY_DESC);
                    String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
                    long mem_total = DBData.getLong(rs, MEMORY.MEMORY_TOTAL);
                    Date mem_creationtime = DBData.getDate(rs, MEMORY.MEMORY_CREATIONTIME);
                    Date mem_modifiedtime = DBData.getDate(rs, MEMORY.MEMORY_MODIFIEDTIME);
                    ms_state = MemoryState.getMemoryState(DBData.getInt(rs, MEMORY_SERVICE.MEMORY_SERVICE_STATE));
                    String account_name = null;
                    String user_name = null;
                    String ms_desc = null;
                    long ms_used = DBData.getLong(rs, MEMORY_SERVICE.MEMORY_SERVICE_USED);
                    Date ms_starttime = null;
                    Date ms_endtime = null;
                    String ms_life = null;
                    Date ms_creationtime = null;
                    Date ms_modifiedtime = null;
                    if (ms_state != MemoryState.RESERVED) {
                        account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                        user_name = DBData.getString(rs, USER.USER_NAME);
                        ms_desc = DBData.getString(rs, MEMORY_SERVICE.MEMORY_SERVICE_DESC);
                        ms_starttime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME);
                        ms_endtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME);
                        ms_life = DBData.getString(rs, MEMORY_SERVICE.MEMORY_SERVICE_LIFE);
                        if (ms_life != null) {
                            ms_life = Integer.toString(Math.max(0, Integer.parseInt(ms_life) + 1));
                        }
                        ms_creationtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_CREATIONTIME);
                        ms_modifiedtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_MODIFIEDTIME);
                    }
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.MEMORY.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_ID, ms_id);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_ID, mem_id);
                    row.setColumn(CellTableColumns.MEMORY.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.MEMORY.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.MEMORY.SERVER_NAME, server_name);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE, ms_state.toString());
                    row.setColumn(CellTableColumns.MEMORY.ACCOUNT_NAME, account_name);
                    row.setColumn(CellTableColumns.MEMORY.USER_NAME, user_name);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_DESC, ms_desc);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_TOTAL, mem_total);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_USED, ms_used);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_STARTTIME, ms_starttime);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_ENDTIME, ms_endtime);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_LIFE, ms_life);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_CREATIONTIME, ms_creationtime);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_MODIFIEDTIME, ms_modifiedtime);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_DESC, mem_desc);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_CREATIONTIME, mem_creationtime);
                    row.setColumn(CellTableColumns.MEMORY.MEMORY_MODIFIEDTIME, mem_modifiedtime);
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
    
    public static Map<Integer, Long> lookupMemoryCounts(Session session) throws EucalyptusServiceException {
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
            return DeviceMemoryDBProcWrapper.lookupMemoryCounts(conn, account_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static MemoryInfo lookupMemoryInfoByID(int mem_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryByID(conn, false, mem_id);
            String mem_desc = DBData.getString(rs, MEMORY.MEMORY_DESC);
            long mem_total = DBData.getLong(rs, MEMORY.MEMORY_TOTAL);
            Date mem_creationtime = DBData.getDate(rs, MEMORY.MEMORY_CREATIONTIME);
            Date mem_modifiedtime = DBData.getDate(rs, MEMORY.MEMORY_MODIFIEDTIME);
            int server_id = DBData.getInt(rs, MEMORY.SERVER_ID);
            rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceReservedByID(conn, false, mem_id);
            long ms_reserved = DBData.getLong(rs, MEMORY_SERVICE.MEMORY_SERVICE_USED);
            return new MemoryInfo(mem_id, mem_desc, mem_total, ms_reserved, mem_creationtime, mem_modifiedtime, server_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static MemoryServiceInfo lookupMemoryServiceInfoByID(int ms_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceByID(conn, false, ms_id);
            String ms_desc = DBData.getString(rs, MEMORY_SERVICE.MEMORY_SERVICE_DESC);
            long ms_used = DBData.getLong(rs, MEMORY_SERVICE.MEMORY_SERVICE_USED);
            MemoryState ms_state = MemoryState.getMemoryState(DBData.getInt(rs, MEMORY_SERVICE.MEMORY_SERVICE_STATE));
            Date ms_creationtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_CREATIONTIME);
            Date ms_modifiedtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_MODIFIEDTIME);
            int mem_id = DBData.getInt(rs, MEMORY_SERVICE.MEMORY_ID);
            int user_id = DBData.getInt(rs, MEMORY_SERVICE.USER_ID);
            return new MemoryServiceInfo(ms_id, ms_desc, ms_used, ms_state, ms_creationtime, ms_modifiedtime, mem_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void incMemoryTotal(boolean force, Session session, String mem_desc, long mem_total, int server_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (mem_desc == null) {
            mem_desc = "";
        }
        long resize = Math.max(0, mem_total);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            int mem_id = DeviceMemoryDBProcWrapper.lookupMemoryIDByServerID(conn, server_id);
            ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryByID(conn, true, mem_id);
            if (resize != 0) {
                rs.updateLong(MEMORY.MEMORY_TOTAL.toString(), rs.getLong(MEMORY.MEMORY_TOTAL.toString()) + resize);
            }
            rs.updateString(MEMORY.MEMORY_DESC.toString(), mem_desc);
            rs.updateString(MEMORY.MEMORY_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            if (resize != 0) {
                rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceReservedByID(conn, true, mem_id);
                rs.updateLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString(), rs.getLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString()) + resize);
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
    
    public static void decMemoryTotal(boolean force, Session session, List<Integer> mem_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (mem_ids == null || mem_ids.isEmpty()) {
            return;
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            for (int mem_id : mem_ids) {
                ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceReservedByID(conn, true, mem_id);
                long resize = rs.getLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString());
                if (resize > 0) {
                    rs.updateLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString(), 0);
                    rs.updateRow();
                    rs = DeviceMemoryDBProcWrapper.lookupMemoryByID(conn, true, mem_id);
                    rs.updateLong(MEMORY.MEMORY_TOTAL.toString(), rs.getLong(MEMORY.MEMORY_TOTAL.toString()) - resize);
                    rs.updateString(MEMORY.MEMORY_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
    public static void modifyMemory(boolean force, Session session, int mem_id, String mem_desc, long mem_total) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (mem_desc == null) {
            mem_desc = "";
        }
        mem_total = Math.max(0, mem_total);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryByID(conn, true, mem_id);
            long resize = mem_total - rs.getLong(MEMORY.MEMORY_TOTAL.toString());
            rs.updateLong(MEMORY.MEMORY_TOTAL.toString(), mem_total);
            rs.updateString(MEMORY.MEMORY_DESC.toString(), mem_desc);
            rs.updateString(MEMORY.MEMORY_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            if (resize != 0) {
                rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceReservedByID(conn, true, mem_id);
                long reserved = rs.getLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString()) + resize;
                if (reserved >= 0) {
                    rs.updateLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString(), reserved);
                    rs.updateRow();
                }
                else {
                    throw new EucalyptusServiceException(ClientMessage.invalidValue("Memory Size", "内存大小"));
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
    
    static void createMemoryByServerID(Connection conn, String server_desc, int server_id) throws Exception {
        int mem_id = DeviceMemoryDBProcWrapper.createMemory(conn, server_desc, 0, server_id);
        DeviceMemoryDBProcWrapper.createMemoryService(conn, null, 0, MemoryState.RESERVED, mem_id, -1);
    }
    
    static void deleteMemoryByServerID(Connection conn, int server_id) throws Exception {
        int mem_id = DeviceMemoryDBProcWrapper.lookupMemoryIDByServerID(conn, server_id);
        DeviceMemoryDBProcWrapper.lookupMemoryServiceReservedByID(conn, true, mem_id).deleteRow();
        DeviceMemoryDBProcWrapper.lookupMemoryByID(conn, true, mem_id).deleteRow();
    }
    
    static int createMemoryService(Connection conn, String ms_desc, long ms_used, MemoryState ms_state, int user_id, int server_id) throws Exception {
        if (ms_used <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Memory Size", "内存大小"));
        }
        if (ms_state == null || ms_state == MemoryState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Memory Service State", "内存服务状态"));
        }
        if (user_id == -1) {
            throw new EucalyptusServiceException(new ClientMessage("User Name", "用户名称"));
        }
        int mem_id = DeviceMemoryDBProcWrapper.lookupMemoryIDByServerID(conn, server_id);
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceReservedByID(conn, true, mem_id);
        long reserved = rs.getLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString());
        if (reserved >= ms_used) {
            rs.updateLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString(), reserved - ms_used);
            rs.updateRow();
        }
        else {
            rs.updateLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString(), 0);
            rs.updateRow();
            rs = DeviceMemoryDBProcWrapper.lookupMemoryByID(conn, true, mem_id);
            rs.updateLong(MEMORY.MEMORY_TOTAL.toString(), rs.getLong(MEMORY.MEMORY_TOTAL.toString()) + ms_used - reserved);
            rs.updateRow();
        }
        return DeviceMemoryDBProcWrapper.createMemoryService(conn, ms_desc, ms_used, ms_state, mem_id, user_id);
    }
    
    static void deleteMemoryService(Connection conn, int ms_id) throws Exception {
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceByID(conn, true, ms_id);
        int mem_id = rs.getInt(MEMORY_SERVICE.MEMORY_ID.toString());
        long resize = rs.getLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString());
        rs.deleteRow();
        rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceReservedByID(conn, true, mem_id);
        rs.updateLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString(), rs.getLong(MEMORY_SERVICE.MEMORY_SERVICE_USED.toString()) + resize);
        rs.updateRow();
    }
    
    static void stopMemoryServiceByServerID(Connection conn, int server_id) throws Exception {
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        int mem_id = DeviceMemoryDBProcWrapper.lookupMemoryIDByServerID(conn, server_id);
        for (int ms_id : DeviceMemoryDBProcWrapper.lookupMemoryServiceIDsByMemoryID(conn, mem_id)) {
            ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceByID(conn, true, ms_id);
            rs.updateInt(MEMORY_SERVICE.MEMORY_SERVICE_STATE.toString(), MemoryState.STOP.getValue());
            rs.updateRow();
        }
    }
    
    static void modifyMemoryServiceState(Connection conn, int ms_id, MemoryState ms_state) throws Exception {
        if (ms_state == null || ms_state == MemoryState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Memory Service State", "内存服务状态"));
        }
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        ResultSet rs = DeviceMemoryDBProcWrapper.lookupMemoryServiceByID(conn, true, ms_id);
        rs.updateInt(MEMORY_SERVICE.MEMORY_SERVICE_STATE.toString(), ms_state.getValue());
        rs.updateRow();
    }

    static class DeviceMemoryDBProcWrapper {
        
        private static final Logger log = Logger.getLogger(DeviceMemoryDBProcWrapper.class.getName());
        
        public static int lookupMemoryIDByServerID(Connection conn, int server_id) throws Exception {
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(MEMORY.MEMORY_ID).append(" FROM ").append(MEMORY);
            sb.append(" WHERE ").append(MEMORY.SERVER_ID).append(" = ").append(server_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            rs.next();
            return rs.getInt(1);
        }
        
        public static List<Integer> lookupMemoryServiceIDsByMemoryID(Connection conn, int mem_id) throws Exception {
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(MEMORY_SERVICE.MEMORY_SERVICE_ID).append(" FROM ").append(MEMORY_SERVICE);
            sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_ID).append(" = ").append(mem_id);
            sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" != ").append(MemoryState.RESERVED.getValue());
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            List<Integer> result = new LinkedList<Integer>();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;
        }
        
        public static ResultSet lookupMemoryByID(Connection conn, boolean updatable, int mem_id) throws Exception {
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(MEMORY);
            sb.append(" WHERE ").append(MEMORY.MEMORY_ID).append(" = ").append(mem_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupMemoryServiceByID(Connection conn, boolean updatable, int ms_id) throws Exception {
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(MEMORY_SERVICE);
            sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_SERVICE_ID).append(" = ").append(ms_id);
            sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" != ").append(MemoryState.RESERVED.getValue());
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupMemoryServiceReservedByID(Connection conn, boolean updatable, int mem_id) throws Exception {
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(MEMORY_SERVICE);
            sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_ID).append(" = ").append(mem_id);
            sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" = ").append(MemoryState.RESERVED.getValue());
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupMemory(Connection conn, MemoryState state, DBTableColumn sorted, boolean isAscending, int account_id, int user_id) throws Exception {
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableServer SERVER = DBTable.SERVER;
            DBTableUserApp USER_APP = DBTable.USER_APP;
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(MEMORY.ANY).append(", ").append(MEMORY_SERVICE.ANY).append(", ");
            sb.append(SERVER.SERVER_NAME).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
            sb.append(USER_APP.SERVICE_STARTTIME).append(" AS ").append(MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME).append(", ");
            sb.append(USER_APP.SERVICE_ENDTIME).append(" AS ").append(MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME).append(", ");
            sb.appendDateLifeRemains(USER_APP.SERVICE_STARTTIME, USER_APP.SERVICE_ENDTIME, MEMORY_SERVICE.MEMORY_SERVICE_LIFE).append(" FROM "); {
                sb.append(MEMORY_SERVICE);
                sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(MEMORY_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
                sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
                sb.append(" LEFT JOIN ").append(MEMORY).append(" ON ").append(MEMORY_SERVICE.MEMORY_ID).append(" = ").append(MEMORY.MEMORY_ID);
                sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(MEMORY.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
                sb.append(" LEFT JOIN ").append(USER_APP).append(" ON ").append(MEMORY_SERVICE.MEMORY_SERVICE_ID).append(" = ").append(USER_APP.MEMORY_SERVICE_ID);
            }
            sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" != ").append(0);
            if (state != null) {
                sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" = ").append(state.getValue());
            }
            if (user_id >= 0) {
                sb.append(" AND ").append(MEMORY_SERVICE.USER_ID).append(" = ").append(user_id);
            }
            if (account_id >= 0) {
                sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
            }
            if (sorted != null) {
                sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
            }
            return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        }
        
        public static int createMemory(Connection conn, String mem_desc, long mem_total, int server_id) throws Exception {
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(MEMORY).append(" ("); {
                sb.append(MEMORY.MEMORY_DESC).append(", ");
                sb.append(MEMORY.MEMORY_TOTAL).append(", ");
                sb.append(MEMORY.SERVER_ID).append(", ");
                sb.append(MEMORY.MEMORY_CREATIONTIME).append(", ");
                sb.append(MEMORY.MEMORY_MODIFIEDTIME);
            }
            sb.append(") VALUES ("); {
                sb.appendString(mem_desc).append(", ");
                sb.append(mem_total).append(", ");
                sb.append(server_id).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull();
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{MEMORY.MEMORY_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
        public static int createMemoryService(Connection conn, String ms_desc, long ms_used, MemoryState state, int mem_id, int user_id) throws Exception {
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(MEMORY_SERVICE).append(" ("); {
                sb.append(MEMORY_SERVICE.MEMORY_SERVICE_DESC).append(", ");
                sb.append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(", ");
                sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(", ");
                sb.append(MEMORY_SERVICE.MEMORY_SERVICE_CREATIONTIME).append(", ");
                sb.append(MEMORY_SERVICE.MEMORY_SERVICE_MODIFIEDTIME).append(", ");
                sb.append(MEMORY_SERVICE.MEMORY_ID).append(", ");
                sb.append(MEMORY_SERVICE.USER_ID);
            }
            sb.append(") VALUES ("); {
                sb.appendString(ms_desc).append(", ");
                sb.append(ms_used).append(", ");
                sb.append(state.getValue()).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull().append(", ");
                sb.append(mem_id).append(", ");
                if (user_id == -1) {
                    sb.appendNull();
                }
                else {
                    sb.append(user_id);
                }
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{MEMORY_SERVICE.MEMORY_SERVICE_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
        public static Map<Integer, Long> lookupMemoryCounts(Connection conn, int account_id, int user_id) throws Exception {
            DBTableUser USER = DBTable.USER;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(", sum(").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(")").append(" FROM ");
            sb.append(MEMORY_SERVICE);
            if (account_id >= 0) {
                sb.append(" LEFT JOIN ").append(USER).append(" ON ");
                sb.append(MEMORY_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
            }
            sb.append(" WHERE 1=1");
            if (user_id >= 0) {
                sb.append(" AND ").append(MEMORY_SERVICE.USER_ID).append(" = ").append(user_id);
            }
            if (account_id >= 0) {
                sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
            }
            sb.append(" GROUP BY ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE);
            
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
