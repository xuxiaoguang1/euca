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
import com.eucalyptus.webui.shared.resource.device.RoomInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceRoomService {
    
    private static LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    private static List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
            new SearchResultFieldDesc("0%", false, null),
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("Index", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Name", "名称"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Desc", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Area", "所在区域"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Create", "创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Modify", "修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private static DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.ROOM.ROOM_NAME: return DBTable.ROOM.ROOM_NAME;
        case CellTableColumns.ROOM.ROOM_DESC: return DBTable.ROOM.ROOM_DESC;
        case CellTableColumns.ROOM.AREA_NAME: return DBTable.AREA.AREA_NAME;
        case CellTableColumns.ROOM.ROOM_CREATIONTIME: return DBTable.ROOM.ROOM_CREATIONTIME;
        case CellTableColumns.ROOM.ROOM_MODIFIEDTIME: return DBTable.ROOM.ROOM_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupRoom(Session session, SearchRange range) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableArea AREA = DBTable.AREA;
            DBTableRoom ROOM = DBTable.ROOM;
            ResultSet rs = DeviceRoomDBProcWrapper.lookupRoom(conn, getSortColumn(range), range.isAscending());
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int room_id = DBData.getInt(rs, ROOM.ROOM_ID);
                    String room_name = DBData.getString(rs, ROOM.ROOM_NAME);
                    String room_desc = DBData.getString(rs, ROOM.ROOM_DESC);
                    Date room_creationtime = DBData.getDate(rs, ROOM.ROOM_CREATIONTIME);
                    Date room_modifiedtime = DBData.getDate(rs, ROOM.ROOM_MODIFIEDTIME);
                    String area_name = DBData.getString(rs, AREA.AREA_NAME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.ROOM.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.ROOM.ROOM_ID, room_id);
                    row.setColumn(CellTableColumns.ROOM.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.ROOM.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.ROOM.ROOM_NAME, room_name);
                    row.setColumn(CellTableColumns.ROOM.ROOM_DESC, room_desc);
                    row.setColumn(CellTableColumns.ROOM.AREA_NAME, area_name);
                    row.setColumn(CellTableColumns.ROOM.ROOM_CREATIONTIME, room_creationtime);
                    row.setColumn(CellTableColumns.ROOM.ROOM_MODIFIEDTIME, room_modifiedtime);
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
    
    public static Map<String, Integer> lookupRoomNamesByAreaID(int area_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceRoomDBProcWrapper.lookupRoomNamesByAreaID(conn, area_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static RoomInfo lookupRoomByID(int room_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableRoom ROOM = DBTable.ROOM;
            ResultSet rs = DeviceRoomDBProcWrapper.lookupRoomByID(conn, false, room_id);
            String room_name = DBData.getString(rs, ROOM.ROOM_NAME);
            String room_desc = DBData.getString(rs, ROOM.ROOM_DESC);
            Date room_creationtime = DBData.getDate(rs, ROOM.ROOM_CREATIONTIME);
            Date room_modifiedtime = DBData.getDate(rs, ROOM.ROOM_MODIFIEDTIME);
            int area_id = DBData.getInt(rs, ROOM.AREA_ID);
            return new RoomInfo(room_id, room_name, room_desc, room_creationtime, room_modifiedtime, area_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void createRoom(boolean force, Session session, String room_name, String room_desc, int area_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (room_name == null || room_name.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Room Name", "机房名称"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceRoomDBProcWrapper.createRoom(conn, room_name, room_desc, area_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void deleteRoom(boolean force, Session session, List<Integer> room_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (room_ids != null && !room_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                for (int room_id : room_ids) {
                    DeviceRoomDBProcWrapper.lookupRoomByID(conn, true, room_id).deleteRow();
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
    
    public static void modifyRoom(boolean force, Session session, int room_id, String room_desc) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (room_desc == null) {
            room_desc = "";
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableRoom ROOM = DBTable.ROOM;
            ResultSet rs = DeviceRoomDBProcWrapper.lookupRoomByID(conn, true, room_id);
            rs.updateString(ROOM.ROOM_DESC.toString(), room_desc);
            rs.updateString(ROOM.ROOM_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
    static class DeviceRoomDBProcWrapper {
        
        private static final Logger log = Logger.getLogger(DeviceRoomDBProcWrapper.class.getName());
        
        public static Map<String, Integer> lookupRoomNamesByAreaID(Connection conn, int area_id) throws Exception {
            DBTableRoom ROOM = DBTable.ROOM;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(ROOM.ROOM_NAME).append(", ").append(ROOM.ROOM_ID);
            sb.append(" FROM ").append(ROOM);
            sb.append(" WHERE ").append(ROOM.AREA_ID).append(" = ").append(area_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        }
        
        public static ResultSet lookupRoomByID(Connection conn, boolean updatable, int room_id) throws Exception {
            DBTableRoom ROOM = DBTable.ROOM;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(ROOM);
            sb.append(" WHERE ").append(ROOM.ROOM_ID).append(" = ").append(room_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }

        public static ResultSet lookupRoom(Connection conn, DBTableColumn sorted, boolean isAscending) throws Exception {
            DBTableArea AREA = DBTable.AREA;
            DBTableRoom ROOM = DBTable.ROOM;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(ROOM.ANY).append(", ").append(AREA.AREA_NAME).append(" FROM "); {
                sb.append(ROOM).append(" LEFT JOIN ").append(AREA);
                sb.append(" ON ").append(ROOM.AREA_ID).append(" = ").append(AREA.AREA_ID);
            }
            sb.append(" WHERE 1=1");
            if (sorted != null) {
                sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
            }
            return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        }
        
        public static int createRoom(Connection conn, String room_name, String room_desc, int area_id) throws Exception {
            DBTableRoom ROOM = DBTable.ROOM;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(ROOM).append(" ("); {
                sb.append(ROOM.ROOM_NAME).append(", ");
                sb.append(ROOM.ROOM_DESC).append(", ");
                sb.append(ROOM.AREA_ID).append(", ");
                sb.append(ROOM.ROOM_CREATIONTIME).append(", ");
                sb.append(ROOM.ROOM_MODIFIEDTIME);
            }
            sb.append(") VALUES ("); {
                sb.appendString(room_name).append(", ");
                sb.appendString(room_desc).append(", ");
                sb.append(area_id).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull();
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{ROOM.ROOM_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }

    }
    
}
