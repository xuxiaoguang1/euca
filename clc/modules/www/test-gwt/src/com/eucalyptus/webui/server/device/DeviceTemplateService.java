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
import com.eucalyptus.webui.shared.resource.AppResources;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceTemplateService {
    
    private static LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    private static final List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("Index", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Name", "名称"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("Desc", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("NCPU", "CPU数量"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),                    
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Memory(MB)", "内存容量(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Disk(MB)", "硬盘容量(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Bandwidth(KB)", "带宽(KB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Create", "创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Modify", "修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private static DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.TEMPLATE.TEMPLATE_NAME: return DBTable.TEMPLATE.TEMPLATE_NAME;
        case CellTableColumns.TEMPLATE.TEMPLATE_NCPUS: return DBTable.TEMPLATE.TEMPLATE_NCPUS;
        case CellTableColumns.TEMPLATE.TEMPLATE_MEM: return DBTable.TEMPLATE.TEMPLATE_MEM;
        case CellTableColumns.TEMPLATE.TEMPLATE_DISK: return DBTable.TEMPLATE.TEMPLATE_DISK;
        case CellTableColumns.TEMPLATE.TEMPLATE_BW: return DBTable.TEMPLATE.TEMPLATE_BW;
        case CellTableColumns.TEMPLATE.TEMPLATE_CREATIONTIME: return DBTable.TEMPLATE.TEMPLATE_CREATIONTIME;
        case CellTableColumns.TEMPLATE.TEMPLATE_MODIFIEDTIME: return DBTable.TEMPLATE.TEMPLATE_MODIFIEDTIME;
        }
        return null;
    }
    
    public static SearchResult lookupTemplate(Session session, SearchRange range) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            ResultSet rs = DeviceTemplateDBProcWrapper.lookupTempate(conn, getSortColumn(range), range.isAscending());
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int template_id = DBData.getInt(rs, TEMPLATE.TEMPLATE_ID);
                    String template_name = DBData.getString(rs, TEMPLATE.TEMPLATE_NAME);
                    String template_desc = DBData.getString(rs, TEMPLATE.TEMPLATE_DESC);
                    int template_ncpus = DBData.getInt(rs, TEMPLATE.TEMPLATE_NCPUS);
                    long template_mem = DBData.getLong(rs, TEMPLATE.TEMPLATE_MEM);
                    long template_disk = DBData.getLong(rs, TEMPLATE.TEMPLATE_DISK);
                    int template_bw = DBData.getInt(rs, TEMPLATE.TEMPLATE_BW);
                    Date template_creationtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_CREATIONTIME);
                    Date template_modifiedtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_MODIFIEDTIME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.TEMPLATE.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_ID, template_id);
                    row.setColumn(CellTableColumns.TEMPLATE.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.TEMPLATE.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_NAME, template_name);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_DESC, template_desc);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_NCPUS, template_ncpus);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_MEM, template_mem);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_DISK, template_disk);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_BW, template_bw);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_CREATIONTIME, template_creationtime);
                    row.setColumn(CellTableColumns.TEMPLATE.TEMPLATE_MODIFIEDTIME, template_modifiedtime);
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
    
    public static TemplateInfo lookupTemplateInfoByID(int template_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            ResultSet rs = DeviceTemplateDBProcWrapper.lookupTemplateByID(conn, false, template_id);
            if (rs.next()) {
                DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
                String template_name = DBData.getString(rs, TEMPLATE.TEMPLATE_NAME);
                String template_desc = DBData.getString(rs, TEMPLATE.TEMPLATE_DESC);
                int template_ncpus = DBData.getInt(rs, TEMPLATE.TEMPLATE_NCPUS);
                long template_mem = DBData.getLong(rs, TEMPLATE.TEMPLATE_MEM);
                long template_disk = DBData.getLong(rs, TEMPLATE.TEMPLATE_DISK);
                int template_bw = DBData.getInt(rs, TEMPLATE.TEMPLATE_BW);
                Date template_creationtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_CREATIONTIME);
                Date template_modifiedtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_MODIFIEDTIME);
                return new TemplateInfo(template_id, template_name, template_desc, template_ncpus,
                        template_mem, template_disk, template_bw, template_creationtime, template_modifiedtime);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
        throw new EucalyptusServiceException(new ClientMessage("Cannot find the corresponding Template.", "找不到指定的模板"));
    }
    
    public static void createTemplate(Session session, String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (template_name == null || template_name.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Template Name", "模板名称"));
        }
        if (template_ncpus <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Number of CPU", "CPU数量"));
        }
        if (template_mem <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Capacity of Memory", "内存数量"));
        }
        if (template_disk <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Capacity of Disk", "硬盘数量"));
        }
        if (template_bw < 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Capacity of Bandwidth", "带宽数量"));
        }
        if (template_desc == null) {
            template_desc = "";
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceTemplateDBProcWrapper.createTempate(conn, template_name, template_desc, template_ncpus, template_mem, template_disk, template_bw);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static void deleteTemplate(Session session, List<Integer> template_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (template_ids != null && !template_ids.isEmpty()) {
            Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                DeviceTemplateDBProcWrapper.deleteTemplate(conn, template_ids);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new EucalyptusServiceException(e);
            }
            finally {
                DBProcWrapper.close(conn);
            }
        }
    }
    
    public static void modifyTempalte(Session session, int template_id, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (template_ncpus <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Number of CPU", "CPU数量"));
        }
        if (template_mem <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Memory Size", "内存数量"));
        }
        if (template_disk <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Disk Size", "硬盘数量"));
        }
        if (template_bw < 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Bandwidth Value", "带宽数量"));
        }
        if (template_desc == null) {
            template_desc = "";
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            ResultSet rs = DeviceTemplateDBProcWrapper.lookupTemplateByID(conn, true, template_id);
            if (rs.next()) {
                DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
                rs.updateInt(TEMPLATE.TEMPLATE_NCPUS.toString(), template_ncpus);
                rs.updateLong(TEMPLATE.TEMPLATE_MEM.toString(), template_mem);
                rs.updateLong(TEMPLATE.TEMPLATE_DISK.toString(), template_disk);
                rs.updateInt(TEMPLATE.TEMPLATE_BW.toString(), template_bw);
                rs.updateString(TEMPLATE.TEMPLATE_DESC.toString(), template_desc);
                rs.updateString(TEMPLATE.TEMPLATE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
    public static Map<String, Integer> lookupTemplates(Session session) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceTemplateDBProcWrapper.lookupTemplates(conn);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public static AppResources createApp(String desc, int ncpus, long mem_total, long disk_total, int bw_max, int server_id, int user_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            int cs_id = DeviceCPUService.createCPUService(conn, desc, ncpus, CPUState.STOP, user_id, server_id);
            int ms_id = DeviceMemoryService.createMemoryService(conn, desc, mem_total, MemoryState.STOP, user_id, server_id);
            int ds_id = DeviceDiskService.createDiskService(conn, desc, disk_total, DiskState.STOP, user_id, server_id);
            int ip_id_public = DeviceIPService.createIPService(conn, desc, IPType.PUBLIC, IPState.STOP, user_id);
            int bs_id_public = DeviceBWService.createBWService(conn, desc, bw_max, ip_id_public);
            int ip_id_private = DeviceIPService.createIPService(conn, desc, IPType.PRIVATE, IPState.STOP, user_id);
            conn.commit();
            return new AppResources(cs_id, ms_id, ds_id, ip_id_public, bs_id_public, ip_id_private);
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
    
    public static void startApp(AppResources app) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DeviceCPUService.modifyCPUServiceState(conn, app.cs_id, CPUState.INUSE);
            DeviceMemoryService.modifyMemoryServiceState(conn, app.ms_id, MemoryState.INUSE);
            DeviceDiskService.modifyDiskServiceState(conn, app.ds_id, DiskState.INUSE);
            DeviceIPService.modifyIPServiceState(conn, app.ip_id_public, IPState.INUSE);
            DeviceIPService.modifyIPServiceState(conn, app.ip_id_private, IPState.INUSE);
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
    
    public static void stopApp(AppResources app) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DeviceCPUService.modifyCPUServiceState(conn, app.cs_id, CPUState.STOP);
            DeviceMemoryService.modifyMemoryServiceState(conn, app.ms_id, MemoryState.STOP);
            DeviceDiskService.modifyDiskServiceState(conn, app.ds_id, DiskState.STOP);
            DeviceIPService.modifyIPServiceState(conn, app.ip_id_public, IPState.STOP);
            DeviceIPService.modifyIPServiceState(conn, app.ip_id_private, IPState.STOP);
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
    
    public static void deleteApp(AppResources app) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DeviceCPUService.deleteCPUService(conn, app.cs_id);
            DeviceMemoryService.deleteMemoryService(conn, app.ms_id);
            DeviceDiskService.deleteDiskService(conn, app.ds_id);
            DeviceIPService.deleteIPService(conn, app.ip_id_private);
            DeviceBWService.deleteBWService(conn, app.bs_id_public);
            DeviceIPService.deleteIPService(conn, app.ip_id_public);
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
    
    static class DeviceTemplateDBProcWrapper {
        
        private static final Logger log = Logger.getLogger(DeviceTemplateDBProcWrapper.class.getName());
        
        public static Map<String, Integer> lookupTemplates(Connection conn) throws Exception {
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT ");
            sb.append(TEMPLATE.TEMPLATE_NAME).append(", ").append(TEMPLATE.TEMPLATE_ID);
            sb.append(" FROM ").append(TEMPLATE).append(" WHERE 1=1");
            ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        }
        
        public static ResultSet lookupTemplateByID(Connection conn, boolean updatable, int template_id) throws Exception {
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(TEMPLATE);
            sb.append(" WHERE ").append(TEMPLATE.TEMPLATE_ID).append(" = ").append(template_id);
            return DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        }
        
        public static ResultSet lookupTempate(Connection conn, DBTableColumn sorted, boolean isAscending) throws Exception {
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("SELECT * FROM ").append(TEMPLATE).append(" WHERE 1=1");
            if (sorted != null) {
                sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
            }
            return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        }
        
        public static int createTempate(Connection conn, String template_name, String template_desc, int template_ncpus,
                long template_mem, long template_disk, int template_bw) throws Exception {
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("INSERT INTO ").append(TEMPLATE).append(" ("); {
                sb.append(TEMPLATE.TEMPLATE_NAME).append(", ");
                sb.append(TEMPLATE.TEMPLATE_DESC).append(", ");
                sb.append(TEMPLATE.TEMPLATE_NCPUS).append(", ");
                sb.append(TEMPLATE.TEMPLATE_MEM).append(", ");
                sb.append(TEMPLATE.TEMPLATE_DISK).append(", ");
                sb.append(TEMPLATE.TEMPLATE_BW).append(", ");
                sb.append(TEMPLATE.TEMPLATE_CREATIONTIME).append(", ");
                sb.append(TEMPLATE.TEMPLATE_MODIFIEDTIME);
            }
            sb.append(") VALUES ("); {
                sb.appendString(template_name).append(", ");
                sb.appendString(template_desc).append(", ");
                sb.append(template_ncpus).append(", ");
                sb.append(template_mem).append(", ");
                sb.append(template_disk).append(", ");
                sb.append(template_bw).append(", ");
                sb.appendDate().append(", ");
                sb.appendNull();
            }
            sb.append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log), new String[]{TEMPLATE.TEMPLATE_ID.toString()});
            ResultSet rs = stat.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        
        public static void deleteTemplate(Connection conn, List<Integer> template_ids) throws Exception {
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            DBStringBuilder sb = new DBStringBuilder();
            sb.append("DELETE FROM ").append(TEMPLATE).append(" WHERE ");
            sb.append(TEMPLATE.TEMPLATE_ID).append(" IN (").append(DBStringBuilder.listToString(template_ids)).append(")");
            Statement stat = conn.createStatement();
            stat.executeUpdate(sb.toSql(log));
        }
        
    }
    
}
