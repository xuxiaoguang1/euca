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
import com.eucalyptus.webui.shared.resource.device.CabinetInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceCabinetService {
    
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Room", "所在机房"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Create", "创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Modify", "修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));

    private static DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.CABINET.CABINET_NAME: return DBTable.CABINET.CABINET_NAME;
        case CellTableColumns.CABINET.CABINET_DESC: return DBTable.CABINET.CABINET_DESC;
        case CellTableColumns.CABINET.ROOM_NAME: return DBTable.ROOM.ROOM_NAME;
        case CellTableColumns.CABINET.CABINET_CREATIONTIME: return DBTable.CABINET.CABINET_CREATIONTIME;
        case CellTableColumns.CABINET.CABINET_MODIFIEDTIME: return DBTable.CABINET.CABINET_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupCabinet(Session session, SearchRange range) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableRoom ROOM = DBTable.ROOM;
            DBTableCabinet CABINET = DBTable.CABINET;
            ResultSet rs = DeviceCabinetDBProcWrapper.lookupCabinet(conn, getSortColumn(range), range.isAscending());
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int cabinet_id = DBData.getInt(rs, CABINET.CABINET_ID);
                    String cabinet_name = DBData.getString(rs, CABINET.CABINET_NAME);
                    String cabinet_desc = DBData.getString(rs, CABINET.CABINET_DESC);
                    Date cabinet_creationtime = DBData.getDate(rs, CABINET.CABINET_CREATIONTIME);
                    Date cabinet_modifiedtime = DBData.getDate(rs, CABINET.CABINET_MODIFIEDTIME);
                    String room_name = DBData.getString(rs, ROOM.ROOM_NAME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.CABINET.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.CABINET.CABINET_ID, cabinet_id);
                    row.setColumn(CellTableColumns.CABINET.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.CABINET.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.CABINET.CABINET_NAME, cabinet_name);
                    row.setColumn(CellTableColumns.CABINET.CABINET_DESC, cabinet_desc);
                    row.setColumn(CellTableColumns.CABINET.ROOM_NAME, room_name);
                    row.setColumn(CellTableColumns.CABINET.CABINET_CREATIONTIME, cabinet_creationtime);
                    row.setColumn(CellTableColumns.CABINET.CABINET_MODIFIEDTIME, cabinet_modifiedtime);
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
    
    public static Map<String, Integer> lookupCabinetNamesByRoomID(int room_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceCabinetDBProcWrapper.lookupCabinetNamesByRoomID(conn, room_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static CabinetInfo lookupCabinetByID(int cabinet_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableCabinet CABINET = DBTable.CABINET;
            ResultSet rs = DeviceCabinetDBProcWrapper.lookupCabinetByID(conn, false, cabinet_id);
            String cabinet_name = DBData.getString(rs, CABINET.CABINET_NAME);
            String cabinet_desc = DBData.getString(rs, CABINET.CABINET_DESC);
            Date cabinet_creationtime = DBData.getDate(rs, CABINET.CABINET_CREATIONTIME);
            Date cabinet_modifiedtime = DBData.getDate(rs, CABINET.CABINET_MODIFIEDTIME);
            int room_id = DBData.getInt(rs, CABINET.ROOM_ID);
            return new CabinetInfo(cabinet_id, cabinet_name, cabinet_desc, cabinet_creationtime, cabinet_modifiedtime, room_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void createCabinet(boolean force, Session session, String cabinet_name, String cabinet_desc, int room_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (cabinet_name == null || cabinet_name.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Cabinet Name", "机柜名称"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceCabinetDBProcWrapper.createCabinet(conn, cabinet_name, cabinet_desc, room_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void deleteCabinet(boolean force, Session session, List<Integer> cabinet_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (cabinet_ids != null && !cabinet_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                for (int cabinet_id : cabinet_ids) {
                    DeviceCabinetDBProcWrapper.lookupCabinetByID(conn, true, cabinet_id).deleteRow();
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
    
    public static void modifyCabinet(boolean force, Session session, int cabinet_id, String cabinet_desc) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (cabinet_desc == null) {
            cabinet_desc = "";
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableCabinet CABINET = DBTable.CABINET;
            ResultSet rs = DeviceCabinetDBProcWrapper.lookupCabinetByID(conn, true, cabinet_id);
            rs.updateString(CABINET.CABINET_DESC.toString(), cabinet_desc);
            rs.updateString(CABINET.CABINET_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
    static class DeviceCabinetDBProcWrapper {
        
        private static final Logger log = Logger.getLogger(DeviceCabinetDBProcWrapper.class.getName());
        
        public static Map<String, Integer> lookupCabinetNamesByRoomID(Connection conn, int room_id) throws Exception {
            DBTableCabinet CABINET = DBTable.CABINET;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(CABINET.CABINET_NAME).append(", ").append(CABINET.CABINET_ID);
            sb.append(" FROM ").append(CABINET);
            sb.append(" WHERE ").append(CABINET.ROOM_ID).append(" = ").append(room_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        }
        
        public static ResultSet lookupCabinetByID(Connection conn, boolean updatable, int cabinet_id) throws Exception {
            DBTableCabinet CABINET = DBTable.CABINET;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(CABINET);
            sb.append(" WHERE ").append(CABINET.CABINET_ID).append(" = ").append(cabinet_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupCabinet(Connection conn, DBTableColumn sorted, boolean isAscending) throws Exception {
            DBTableRoom ROOM = DBTable.ROOM;
            DBTableCabinet CABINET = DBTable.CABINET;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ").append(CABINET.ANY).append(", ").append(ROOM.ROOM_NAME).append(" FROM "); {
                sb.append(CABINET).append(" LEFT JOIN ").append(ROOM);
                sb.append(" ON ").append(CABINET.ROOM_ID).append(" = ").append(ROOM.ROOM_ID);
            }
            sb.append(" WHERE 1=1");
            if (sorted != null) {
                sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
            }
            return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        }
        
        public static int createCabinet(Connection conn, String cabinet_name, String cabinet_desc, int room_id) throws Exception {
            DBTableCabinet CABINET = DBTable.CABINET;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(CABINET).append(" ("); {
                sb.append(CABINET.CABINET_NAME).append(", ");
                sb.append(CABINET.CABINET_DESC).append(", ");
                sb.append(CABINET.ROOM_ID).append(", ");
                sb.append(CABINET.CABINET_CREATIONTIME).append(", ");
                sb.append(CABINET.CABINET_MODIFIEDTIME);
            }
            sb.append(") VALUES ("); {
                sb.appendString(cabinet_name).append(", ");
                sb.appendString(cabinet_desc).append(", ");
                sb.append(room_id).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull();
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{CABINET.CABINET_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
    }

}
