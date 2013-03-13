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
import com.eucalyptus.webui.shared.resource.device.AreaInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceAreaService {
    
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Create", "创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Modify", "修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private static DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.AREA.AREA_NAME: return DBTable.AREA.AREA_NAME;
        case CellTableColumns.AREA.AREA_DESC: return DBTable.AREA.AREA_DESC;
        case CellTableColumns.AREA.AREA_CREATIONTIME: return DBTable.AREA.AREA_CREATIONTIME;
        case CellTableColumns.AREA.AREA_MODIFIEDTIME: return DBTable.AREA.AREA_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupArea(Session session, SearchRange range)  throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableArea AREA = DBTable.AREA;
            ResultSet rs = DeviceAreaDBProcWrapper.lookupArea(conn, getSortColumn(range), range.isAscending());
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int area_id = DBData.getInt(rs, AREA.AREA_ID);
                    String area_name = DBData.getString(rs, AREA.AREA_NAME);
                    String area_desc = DBData.getString(rs, AREA.AREA_DESC);
                    Date area_creationtime = DBData.getDate(rs, AREA.AREA_CREATIONTIME);
                    Date area_modifiedtime = DBData.getDate(rs, AREA.AREA_MODIFIEDTIME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.AREA.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.AREA.AREA_ID, area_id);
                    row.setColumn(CellTableColumns.AREA.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.AREA.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.AREA.AREA_NAME, area_name);
                    row.setColumn(CellTableColumns.AREA.AREA_DESC, area_desc);
                    row.setColumn(CellTableColumns.AREA.AREA_CREATIONTIME, area_creationtime);
                    row.setColumn(CellTableColumns.AREA.AREA_MODIFIEDTIME, area_modifiedtime);
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
    
    public static Map<String, Integer> lookupAreaNames() throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceAreaDBProcWrapper.lookupAreaNames(conn);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }

    public static AreaInfo lookupAreaByID(int area_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableArea AREA = DBTable.AREA;
            ResultSet rs = DeviceAreaDBProcWrapper.lookupAreaByID(conn, false, area_id);
            String area_name = DBData.getString(rs, AREA.AREA_NAME);
            String area_desc = DBData.getString(rs, AREA.AREA_DESC);
            Date area_creationtime = DBData.getDate(rs, AREA.AREA_CREATIONTIME);
            Date area_modifiedtime = DBData.getDate(rs, AREA.AREA_MODIFIEDTIME);
            return new AreaInfo(area_id, area_name, area_desc, area_creationtime, area_modifiedtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void createArea(boolean force, Session session, String area_name, String area_desc) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (area_name == null || area_name.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Area Name", "区域名称"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceAreaDBProcWrapper.createArea(conn, area_name, area_desc);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void deleteArea(boolean force, Session session, List<Integer> area_ids) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (area_ids != null && !area_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                for (int area_id : area_ids) {
                    DeviceAreaDBProcWrapper.lookupAreaByID(conn, true, area_id).deleteRow();
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
    
    public static void modifyArea(boolean force, Session session, int area_id, String area_desc) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableArea AREA = DBTable.AREA;
            ResultSet rs = DeviceAreaDBProcWrapper.lookupAreaByID(conn, true, area_id);
            rs.updateString(AREA.AREA_DESC.toString(), area_desc);
            rs.updateString(AREA.AREA_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
    static class DeviceAreaDBProcWrapper {
        
        private static final Logger log = Logger.getLogger(DeviceAreaDBProcWrapper.class.getName());
        
        public static Map<String, Integer> lookupAreaNames(Connection conn) throws Exception {
            DBTableArea AREA = DBTable.AREA;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(AREA.AREA_NAME).append(", ").append(AREA.AREA_ID);
            sb.append(" FROM ").append(AREA).append(" WHERE 1=1");
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        }
        
        public static ResultSet lookupAreaByID(Connection conn, boolean updatable, int area_id) throws Exception {
            DBTableArea AREA = DBTable.AREA;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(AREA);
            sb.append(" WHERE ").append(AREA.AREA_ID).append(" = ").append(area_id);
            ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
            rs.next();
            return rs;
        }
        
        public static ResultSet lookupArea(Connection conn, DBTableColumn sorted, boolean isAscending) throws Exception {
            DBTableArea AREA = DBTable.AREA;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(AREA).append(" WHERE 1=1");
            if (sorted != null) {
                sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
            }
            return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        }
        
        public static int createArea(Connection conn, String area_name, String area_desc) throws Exception {
            DBTableArea AREA = DBTable.AREA;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(AREA).append(" ("); {
                sb.append(AREA.AREA_NAME).append(", ");
                sb.append(AREA.AREA_DESC).append(", ");
                sb.append(AREA.AREA_CREATIONTIME).append(", ");
                sb.append(AREA.AREA_MODIFIEDTIME);
            }
            sb.append(") VALUES ("); {
                sb.appendString(area_name).append(", ");
                sb.appendString(area_desc).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull();
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{AREA.AREA_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }

    }

}
