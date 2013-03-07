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
import com.eucalyptus.webui.shared.resource.device.ServerInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceServerService {
    
    private static DeviceServerService instance = new DeviceServerService();
    
    public static DeviceServerService getInstance() {
        return instance;
    }
    
    private DeviceServerService() {
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
			new SearchResultFieldDesc(true, "8%", new ClientMessage("Name", "名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("Desc", "描述"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("Cabinet", "所在机柜"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("IP", "IP地址"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Bandwidth", "带宽(KB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("State", "状态"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("Create", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("Modify", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));

    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.SERVER.SERVER_NAME: return DBTable.SERVER.SERVER_NAME;
        case CellTableColumns.SERVER.SERVER_DESC: return DBTable.SERVER.SERVER_DESC;
        case CellTableColumns.SERVER.CABINET_NAME: return DBTable.CABINET.CABINET_NAME;
        case CellTableColumns.SERVER.SERVER_IP: return DBTable.SERVER.SERVER_IP;
        case CellTableColumns.SERVER.SERVER_BW: return DBTable.SERVER.SERVER_BW;
        case CellTableColumns.SERVER.SERVER_STATE: return DBTable.SERVER.SERVER_STATE;
        case CellTableColumns.SERVER.SERVER_CREATIONTIME: return DBTable.SERVER.SERVER_CREATIONTIME;
        case CellTableColumns.SERVER.SERVER_MODIFIEDTIME: return DBTable.SERVER.SERVER_MODIFIEDTIME;
        }
        return null;
    }
    
    public SearchResult lookupServerByDate(Session session, SearchRange range, ServerState state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableCabinet CABINET = DBTable.CABINET;
            DBTableServer SERVER = DBTable.SERVER;
            ResultSet rs = DeviceServerDBProcWrapper.lookupServerByDate(conn, state, dateBegin, dateEnd, getSortColumn(range), range.isAscending());
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int server_id = DBData.getInt(rs, SERVER.SERVER_ID);
                    String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
                    String server_desc = DBData.getString(rs, SERVER.SERVER_DESC);
                    String server_ip = DBData.getString(rs, SERVER.SERVER_IP);
                    int server_bw = DBData.getInt(rs, SERVER.SERVER_BW);
                    int server_state = DBData.getInt(rs, SERVER.SERVER_STATE);
                    Date server_creationtime = DBData.getDate(rs, SERVER.SERVER_CREATIONTIME);
                    Date server_modifiedtime = DBData.getDate(rs, SERVER.SERVER_MODIFIEDTIME);
                    String cabinet_name = DBData.getString(rs, CABINET.CABINET_NAME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.SERVER.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.SERVER.SERVER_ID, server_id);
                    row.setColumn(CellTableColumns.SERVER.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.SERVER.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.SERVER.SERVER_NAME, server_name);
                    row.setColumn(CellTableColumns.SERVER.SERVER_DESC, server_desc);
                    row.setColumn(CellTableColumns.SERVER.CABINET_NAME, cabinet_name);
                    row.setColumn(CellTableColumns.SERVER.SERVER_IP, server_ip);
                    row.setColumn(CellTableColumns.SERVER.SERVER_BW, server_bw);
                    row.setColumn(CellTableColumns.SERVER.SERVER_STATE, ServerState.getServerState(server_state).toString());
                    row.setColumn(CellTableColumns.SERVER.SERVER_CREATIONTIME, server_creationtime);
                    row.setColumn(CellTableColumns.SERVER.SERVER_MODIFIEDTIME, server_modifiedtime);
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
    
    public Map<String, Integer> lookupServerNamesByCabinetID(int cabinet_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceServerDBProcWrapper.lookupServerNamesByCabinetID(conn, cabinet_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public Map<Integer, Integer> lookupServerCountsByState(Session session) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceServerDBProcWrapper.lookupServerCountsByState(conn);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public ServerInfo lookupServerInfoByID(int server_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableServer SERVER = DBTable.SERVER;
            ResultSet rs = DeviceServerDBProcWrapper.lookupServerByID(conn, false, server_id);
            String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
            String server_desc = DBData.getString(rs, SERVER.SERVER_DESC);
            String server_ip = DBData.getString(rs, SERVER.SERVER_IP);
            int server_bw = DBData.getInt(rs, SERVER.SERVER_BW);
            ServerState server_state = ServerState.getServerState(DBData.getInt(rs, SERVER.SERVER_STATE));
            Date server_creationtime = DBData.getDate(rs, SERVER.SERVER_CREATIONTIME);
            Date server_modifiedtime = DBData.getDate(rs, SERVER.SERVER_MODIFIEDTIME);
            int cabinet_id = DBData.getInt(rs, SERVER.CABINET_ID);
            return new ServerInfo(server_id, server_name, server_desc, server_ip, server_bw, server_state, server_creationtime, server_modifiedtime, cabinet_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void createServer(boolean force, Session session, String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state, int cabinet_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (server_name == null || server_name.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Server Name", "服务器名称"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            int server_id = DeviceServerDBProcWrapper.createServer(conn, server_name, server_desc, server_ip, server_bw, server_state, cabinet_id);
            DeviceCPUService.createCPUByServerID(conn, server_desc, server_id);
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
    
    public void deleteServer(boolean force, Session session, List<Integer> server_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (server_ids != null && !server_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                for (int server_id : server_ids) {
                    DeviceCPUService.deleteCPUByServerID(conn, server_id);
                    DeviceServerDBProcWrapper.lookupServerByID(conn, true, server_id).deleteRow();
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
    
    public void modifyServer(boolean force, Session session, int server_id, String server_desc, String server_ip, int server_bw) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (server_desc == null) {
            server_desc = "";
        }
        if (server_ip == null) {
            server_ip = "";
        }
        if (server_bw < 0) {
            server_bw = 0;
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableServer SERVER = DBTable.SERVER;
            ResultSet rs = DeviceServerDBProcWrapper.lookupServerByID(conn, true, server_id);
            rs.updateString(SERVER.SERVER_DESC.toString(), server_desc);
            rs.updateString(SERVER.SERVER_IP.toString(), server_ip);
            rs.updateInt(SERVER.SERVER_BW.toString(), server_bw);
            rs.updateString(SERVER.SERVER_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
    public void modifyServerState(boolean force, Session session, int server_id, ServerState server_state) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (server_state == null) {
            throw new EucalyptusServiceException(new ClientMessage("Server State", "服务器状态"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableServer SERVER = DBTable.SERVER;
            ResultSet rs = DeviceServerDBProcWrapper.lookupServerByID(conn, true, server_id);
            rs.updateInt(SERVER.SERVER_STATE.toString(), server_state.getValue());
            rs.updateRow();
            if (server_state != ServerState.INUSE) {
                DeviceMemoryService.getInstance().stopMemoryServiceByServerID(force, session, server_id);
                DeviceDiskService.getInstance().stopDiskServiceByServerID(force, session, server_id);
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

class DeviceServerDBProcWrapper {
    
    private static final Logger log = Logger.getLogger(DeviceCabinetDBProcWrapper.class.getName());
    
    public static Map<String, Integer> lookupServerNamesByCabinetID(Connection conn, int cabinet_id) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(SERVER.SERVER_NAME).append(", ").append(SERVER.SERVER_ID);
        sb.append(" FROM ").append(SERVER);
        sb.append(" WHERE ").append(SERVER.CABINET_ID).append(" = ").append(cabinet_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<String, Integer> result = new HashMap<String, Integer>();
        while (rs.next()) {
            result.put(rs.getString(1), rs.getInt(2));
        }
        return result;
    }
    
    public static ResultSet lookupServerByID(Connection conn, boolean updatable, int server_id) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(SERVER);
        sb.append(" WHERE ").append(SERVER.SERVER_ID).append(" = ").append(server_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
    }
    
    public static ResultSet lookupServerByDate(Connection conn, ServerState state, Date beg, Date end, DBTableColumn sorted, boolean isAscending) throws Exception {
        DBTableCabinet CABINET = DBTable.CABINET;
        DBTableServer SERVER = DBTable.SERVER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(SERVER.ANY).append(", ").append(CABINET.CABINET_NAME).append(" FROM "); {
            sb.append(SERVER).append(" LEFT JOIN ").append(CABINET);
            sb.append(" ON ").append(SERVER.CABINET_ID).append(" = ").append(CABINET.CABINET_ID);
        }
        sb.append(" WHERE 1=1");
        if (state != null) {
            sb.append(" AND ").append(SERVER.SERVER_STATE).append(" = ").append(state.getValue());
        }
        if (beg != null || end != null) {
            sb.append(" AND ("); {
                sb.appendDateBound(SERVER.SERVER_CREATIONTIME, beg, end);
                sb.append(" OR ");
                sb.appendDateBound(SERVER.SERVER_MODIFIEDTIME, beg, end);
            }
            sb.append(")");
        }
        if (sorted != null) {
            sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
        }
        return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
    }
    
    public static int createServer(Connection conn, String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state, int cabinet_id) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(SERVER).append(" ("); {
            sb.append(SERVER.SERVER_NAME).append(", ");
            sb.append(SERVER.SERVER_DESC).append(", ");
            sb.append(SERVER.SERVER_IP).append(", ");
            sb.append(SERVER.SERVER_BW).append(", ");
            sb.append(SERVER.SERVER_STATE).append(", ");
            sb.append(SERVER.CABINET_ID).append(", ");
            sb.append(SERVER.SERVER_CREATIONTIME).append(", ");
            sb.append(SERVER.SERVER_MODIFIEDTIME);
        }
        sb.append(") VALUES ("); {
            sb.appendString(server_name).append(", ");
            sb.appendString(server_desc).append(", ");
            sb.appendString(server_ip).append(", ");
            sb.append(Math.max(0, server_bw)).append(", ");
            sb.append(server_state != null ? server_state.getValue() : ServerState.STOP.getValue()).append(", ");
            sb.append(cabinet_id).append(", ");
            sb.appendDate().append(", ");
            sb.appendNull();
        }
        sb.append(")");
        Statement stat = conn.createStatement();
        stat.executeUpdate(sb.toSql(log), new String[]{SERVER.SERVER_ID.toString()});
        ResultSet rs = stat.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }
        
    public static Map<Integer, Integer> lookupServerCountsByState(Connection conn) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(SERVER.SERVER_STATE).append(", count(*)");
        sb.append(" FROM ").append(SERVER);
        sb.append(" WHERE 1=1").append(" GROUP BY ").append(SERVER.SERVER_STATE);
        
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
