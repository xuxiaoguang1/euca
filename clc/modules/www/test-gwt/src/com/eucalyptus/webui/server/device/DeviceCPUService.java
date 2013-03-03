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
import com.eucalyptus.webui.shared.resource.device.CPUInfo;
import com.eucalyptus.webui.shared.resource.device.CPUServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceCPUService {
    
    private static DeviceCPUService instance = new DeviceCPUService();
    
    public static DeviceCPUService getInstance() {
        return instance;
    }
    
    private DeviceCPUService() {
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Name", "CPU名称"),
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Total", "总数量"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("Used", "占用数量"),
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
        case CellTableColumns.CPU.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.CPU.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.CPU.SERVER_NAME: return DBTable.SERVER.SERVER_NAME;
        case CellTableColumns.CPU.CPU_NAME: return DBTable.CPU.CPU_NAME;
        case CellTableColumns.CPU.CPU_TOTAL: return DBTable.CPU.CPU_TOTAL;
        case CellTableColumns.CPU.CPU_SERVICE_USED: return DBTable.CPU_SERVICE.CPU_SERVICE_USED;
        case CellTableColumns.CPU.CPU_CREATIONTIME: return DBTable.CPU.CPU_CREATIONTIME;
        case CellTableColumns.CPU.CPU_MODIFIEDTIME: return DBTable.CPU.CPU_MODIFIEDTIME;
        case CellTableColumns.CPU.CPU_SERVICE_STARTTIME: return DBTable.CPU_SERVICE.CPU_SERVICE_STARTTIME;
        case CellTableColumns.CPU.CPU_SERVICE_ENDTIME: return DBTable.CPU_SERVICE.CPU_SERVICE_ENDTIME;
        case CellTableColumns.CPU.CPU_SERVICE_LIFE: return DBTable.CPU_SERVICE.CPU_SERVICE_LIFE;
        case CellTableColumns.CPU.CPU_SERVICE_STATE: return DBTable.CPU_SERVICE.CPU_SERVICE_STATE;
        case CellTableColumns.CPU.CPU_SERVICE_CREATIONTIME: return DBTable.CPU_SERVICE.CPU_SERVICE_CREATIONTIME;
        case CellTableColumns.CPU.CPU_SERVICE_MODIFIEDTIME: return DBTable.CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    public SearchResult lookupCPUByDate(Session session, SearchRange range, CPUState cs_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
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
            DBTableCPU CPU = DBTable.CPU;
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUByDate(conn, cs_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), account_id, user_id);
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int cpu_id = DBData.getInt(rs, CPU.CPU_ID);
                    int cs_id = DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_ID);
                    String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
                    String cpu_desc = DBData.getString(rs, CPU.CPU_DESC);
                    int cpu_total = DBData.getInt(rs, CPU.CPU_TOTAL);
                    String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
                    Date cpu_creationtime = DBData.getDate(rs, CPU.CPU_CREATIONTIME);
                    Date cpu_modifiedtime = DBData.getDate(rs, CPU.CPU_MODIFIEDTIME);
                    cs_state = CPUState.getCPUState(DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_STATE));
                    String account_name = null;
                    String user_name = null;
                    String cs_desc = null;
                    int cs_used = DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_USED);
                    Date cs_starttime = null;
                    Date cs_endtime = null;
                    String cs_life = null;
                    Date cs_creationtime = null;
                    Date cs_modifiedtime = null;
                    if (cs_state != CPUState.RESERVED) {
                        account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME.toString());
                        user_name = DBData.getString(rs, USER.USER_NAME.toString());
                        cs_desc = DBData.getString(rs, CPU_SERVICE.CPU_SERVICE_DESC);
                        cs_starttime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_STARTTIME);
                        cs_endtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_ENDTIME);
                        cs_life = DBData.getString(rs, CPU_SERVICE.CPU_SERVICE_LIFE);
                        if (cs_life != null) {
                            cs_life = Integer.toString(Math.max(0, Integer.parseInt(cs_life) + 1));
                        }
                        cs_creationtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_CREATIONTIME);
                        cs_modifiedtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME);
                    }
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.CPU.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_ID, cs_id);
                    row.setColumn(CellTableColumns.CPU.CPU_ID, cpu_id);
                    row.setColumn(CellTableColumns.CPU.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.CPU.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.CPU.CPU_NAME, cpu_name);
                    row.setColumn(CellTableColumns.CPU.SERVER_NAME, server_name);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_STATE, cs_state.toString());
                    row.setColumn(CellTableColumns.CPU.ACCOUNT_NAME, account_name);
                    row.setColumn(CellTableColumns.CPU.USER_NAME, user_name);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_DESC, cs_desc);
                    row.setColumn(CellTableColumns.CPU.CPU_TOTAL, cpu_total);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_USED, cs_used);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_STARTTIME, cs_starttime);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_ENDTIME, cs_endtime);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_LIFE, cs_life);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_CREATIONTIME, cs_creationtime);
                    row.setColumn(CellTableColumns.CPU.CPU_SERVICE_MODIFIEDTIME, cs_modifiedtime);
                    row.setColumn(CellTableColumns.CPU.CPU_DESC, cpu_desc);
                    row.setColumn(CellTableColumns.CPU.CPU_CREATIONTIME, cpu_creationtime);
                    row.setColumn(CellTableColumns.CPU.CPU_MODIFIEDTIME, cpu_modifiedtime);
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
    
    public Map<Integer, Integer> lookupCPUCounts(Session session) throws EucalyptusServiceException {
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
            return DeviceCPUDBProcWrapper.lookupCPUCounts(conn, account_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public CPUInfo lookupCPUInfoByID(int cpu_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableCPU CPU = DBTable.CPU;
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUByID(conn, false, cpu_id);
            String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
            String cpu_desc = DBData.getString(rs, CPU.CPU_DESC);
            int cpu_total = DBData.getInt(rs, CPU.CPU_TOTAL);
            Date cpu_creationtime = DBData.getDate(rs, CPU.CPU_CREATIONTIME);
            Date cpu_modifiedtime = DBData.getDate(rs, CPU.CPU_MODIFIEDTIME);
            int server_id = DBData.getInt(rs, CPU.SERVER_ID);
            rs = DeviceCPUDBProcWrapper.lookupCPUServiceReservedByID(conn, false, cpu_id);
            int cs_reserved = rs.getInt(CPU_SERVICE.CPU_SERVICE_USED.toString());
            return new CPUInfo(cpu_id, cpu_name, cpu_desc, cpu_total, cs_reserved, cpu_creationtime, cpu_modifiedtime, server_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public CPUServiceInfo lookupCPUServiceInfoByID(int cs_id) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUServiceByID(conn, false, cs_id);
            String cs_desc = DBData.getString(rs, CPU_SERVICE.CPU_SERVICE_DESC);
            int cs_used = DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_USED);
            Date cs_starttime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_STARTTIME);
            Date cs_endtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_ENDTIME);
            CPUState cpu_state = CPUState.getCPUState(DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_STATE));
            Date cs_creationtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_CREATIONTIME);
            Date cs_modifiedtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME);
            int cpu_id = DBData.getInt(rs, CPU_SERVICE.CPU_ID);
            int user_id = DBData.getInt(rs, CPU_SERVICE.USER_ID);
            return new CPUServiceInfo(cs_id, cs_desc, cs_used, cs_starttime, cs_endtime, cpu_state, cs_creationtime, cs_modifiedtime, cpu_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
    
    public void createCPU(boolean force, Session session, String cpu_name, String cpu_desc, int cpu_total, int server_id) throws EucalyptusServiceException {
        if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (cpu_name == null || cpu_name.isEmpty()) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Name", "CPU名称"));
        }
        cpu_total = Math.max(0, cpu_total);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            int cpu_id = DeviceCPUDBProcWrapper.createCPU(conn, cpu_name, cpu_desc, cpu_total, server_id);
            DeviceCPUDBProcWrapper.createCPUService(conn, null, cpu_total, null, null, CPUState.RESERVED, cpu_id, -1);
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
    
    public void createCPUService(boolean force, Session session, String cs_desc, int cs_size, CPUState cs_state, Date cs_starttime, Date cs_endtime, int cpu_id, int user_id) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            createCPUService(force, conn, cs_desc, cs_size, cs_state, cs_starttime, cs_endtime, cpu_id, user_id);
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
    
    protected int createCPUService(boolean force, Connection conn, String cs_desc, int cs_size, CPUState cs_state, Date cs_starttime, Date cs_endtime, int cpu_id, int user_id) throws Exception {
        if (cs_size <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Size", "CPU大小"));
        }
        if (cs_state == null || cs_state == CPUState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Service State", "CPU服务状态"));
        }
        if (cs_starttime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Start Time", "开始日期"));
        }
        if (cs_endtime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("End Time", "结束日期"));
        }
        if (DBData.calcLife(cs_endtime, cs_starttime) <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Date Value", "服务日期"));
        }
        if (user_id == -1) {
            throw new EucalyptusServiceException(new ClientMessage("User Name", "用户名称"));
        }
        int cs_used = cs_size;
        DBTableCPU CPU = DBTable.CPU;
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUServiceReservedByID(conn, true, cpu_id);
        int reserved = rs.getInt(CPU_SERVICE.CPU_SERVICE_USED.toString());
        if (reserved >= cs_used) {
            rs.updateInt(CPU_SERVICE.CPU_SERVICE_USED.toString(), reserved - cs_used);
            rs.updateRow();
        }
        else if (force) {
            rs.updateLong(CPU_SERVICE.CPU_SERVICE_USED.toString(), 0);
            rs.updateRow();
            rs = DeviceCPUDBProcWrapper.lookupCPUByID(conn, true, cpu_id);
            rs.updateLong(CPU.CPU_TOTAL.toString(), rs.getLong(CPU.CPU_TOTAL.toString()) + cs_used - reserved);
            rs.updateRow();
        }
        else {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Size", "CPU大小"));
        }
        return DeviceCPUDBProcWrapper.createCPUService(conn, cs_desc, cs_used, cs_starttime, cs_endtime, cs_state, cpu_id, user_id);
    }
    
    public void deleteCPU(boolean force, Session session, List<Integer> cpu_ids) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
    	if (cpu_ids != null && !cpu_ids.isEmpty()) {
    		Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
                for (int cpu_id : cpu_ids) {
                	DeviceCPUDBProcWrapper.lookupCPUServiceReservedByID(conn, true, cpu_id).deleteRow();
                	DeviceCPUDBProcWrapper.lookupCPUByID(conn, true, cpu_id).deleteRow();
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
    
    public void deleteCPUService(boolean force, Session session, List<Integer> cs_ids) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
    	if (cs_ids != null && !cs_ids.isEmpty()) {
    		Connection conn = null;
            try {
                conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
    			DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
    			for (int cs_id : cs_ids) {
                	ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUServiceByID(conn, true, cs_id);
                	int cpu_id = rs.getInt(CPU_SERVICE.CPU_ID.toString());
                	int cs_used = rs.getInt(CPU_SERVICE.CPU_SERVICE_USED.toString());
                	rs.deleteRow();
                	rs = DeviceCPUDBProcWrapper.lookupCPUServiceReservedByID(conn, true, cpu_id);
                	rs.updateInt(CPU_SERVICE.CPU_SERVICE_USED.toString(), rs.getInt(CPU_SERVICE.CPU_SERVICE_USED.toString()) + cs_used);  
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
    
    public void modifyCPU(boolean force, Session session, int cpu_id, String cpu_desc, int cpu_total) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
    	if (cpu_desc == null) {
    		cpu_desc = "";
    	}
    	cpu_total = Math.max(0, cpu_total);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableCPU CPU = DBTable.CPU;
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUByID(conn, true, cpu_id);
            int resize = cpu_total - rs.getInt(CPU.CPU_TOTAL.toString());
            rs.updateInt(CPU.CPU_TOTAL.toString(), cpu_total);
            rs.updateString(CPU.CPU_DESC.toString(), cpu_desc);
            rs.updateString(CPU.CPU_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            if (resize != 0) {
            	rs = DeviceCPUDBProcWrapper.lookupCPUServiceReservedByID(conn, true, cpu_id);
            	int reserved = rs.getInt(CPU_SERVICE.CPU_SERVICE_USED.toString()) + resize;
            	if (reserved >= 0) {
            		rs.updateInt(CPU_SERVICE.CPU_SERVICE_USED.toString(), reserved);
                    rs.updateRow();
            	}
            	else {
            		throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Size", "CPU大小"));
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
    
    public void modifyCPUService(boolean force, Session session, int cs_id, String cs_desc, int cs_size, Date cs_starttime, Date cs_endtime) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
    	if (cs_starttime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Start Time", "开始日期"));
        }
        if (cs_endtime == null) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("End Time", "结束日期"));
        }
        if (DBData.calcLife(cs_endtime, cs_starttime) <= 0) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("Date Value", "服务日期"));
        }
        if (cs_desc == null) {
    		cs_desc = "";
    	}
        int cs_used = Math.max(0, cs_size);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableCPU CPU = DBTable.CPU;
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUServiceByID(conn, true, cs_id);
            int resize = cs_used - rs.getInt(CPU_SERVICE.CPU_SERVICE_USED.toString());
            rs.updateInt(CPU_SERVICE.CPU_SERVICE_USED.toString(), cs_used);
            rs.updateString(CPU_SERVICE.CPU_SERVICE_DESC.toString(), cs_desc);
            rs.updateString(CPU_SERVICE.CPU_SERVICE_STARTTIME.toString(), DBStringBuilder.getDate(cs_starttime));
            rs.updateString(CPU_SERVICE.CPU_SERVICE_ENDTIME.toString(), DBStringBuilder.getDate(cs_endtime));
            rs.updateString(CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            if (resize != 0) {
            	int cpu_id = rs.getInt(CPU_SERVICE.CPU_ID.toString());
            	rs = DeviceCPUDBProcWrapper.lookupCPUServiceReservedByID(conn, true, cpu_id);
            	int reserved = rs.getInt(CPU_SERVICE.CPU_SERVICE_USED.toString()) - resize;
            	if (reserved >= 0) {
            		rs.updateInt(CPU_SERVICE.CPU_SERVICE_USED.toString(), reserved);
                    rs.updateRow();
            	}
            	else if (force) {
            		rs.updateInt(CPU_SERVICE.CPU_SERVICE_USED.toString(), 0);
                    rs.updateRow();
            		rs = DeviceCPUDBProcWrapper.lookupCPUByID(conn, true, cpu_id);
            		rs.updateInt(CPU.CPU_TOTAL.toString(), rs.getInt(CPU.CPU_TOTAL.toString()) - reserved);
                    rs.updateRow();
            	}
            	else {
	                throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Size", "CPU大小"));
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
    
    public void stopCPUServiceByServerID(boolean force, Session session, int server_id) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            for (Map.Entry<String, Integer> entry : DeviceCPUDBProcWrapper.lookupCPUNamesByServerID(conn, server_id).entrySet()) {
            	DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            	for (int cs_id : DeviceCPUDBProcWrapper.lookupCPUServiceIDsByCPUID(conn, entry.getValue())) {
            		ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUServiceByID(conn, true, cs_id);
            		rs.updateInt(CPU_SERVICE.CPU_SERVICE_STATE.toString(), CPUState.STOP.getValue());
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
    
    public void modifyCPUState(boolean force, Session session, int cpu_id, CPUState cs_state) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
    	if (cs_state == null || cs_state == CPUState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Service State", "CPU服务状态"));
        }
    	Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            for (int cs_id : DeviceCPUDBProcWrapper.lookupCPUServiceIDsByCPUID(conn, cpu_id)) {
            	ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUServiceByID(conn, true, cs_id);
            	rs.updateInt(CPU_SERVICE.CPU_SERVICE_STATE.toString(), cs_state.getValue());
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
    
    public void modifyCPUServiceState(boolean force, Session session, int cs_id, CPUState cs_state) throws EucalyptusServiceException {
    	if (!force && !getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        if (cs_state == null || cs_state == CPUState.RESERVED) {
            throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Service State", "CPU服务状态"));
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            ResultSet rs = DeviceCPUDBProcWrapper.lookupCPUServiceByID(conn, true, cs_id);
            rs.updateInt(CPU_SERVICE.CPU_SERVICE_STATE.toString(), cs_state.getValue());
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

class DeviceCPUDBProcWrapper {
    
    private static final Logger log = Logger.getLogger(DeviceCPUDBProcWrapper.class.getName());
    
    public static Map<String, Integer> lookupCPUNamesByServerID(Connection conn, int server_id) throws Exception {
        DBTableCPU CPU = DBTable.CPU;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(CPU.CPU_NAME).append(", ").append(CPU.CPU_ID);
        sb.append(" FROM ").append(CPU);
        sb.append(" WHERE ").append(CPU.SERVER_ID).append(" = ").append(server_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<String, Integer> result = new HashMap<String, Integer>();
        while (rs.next()) {
        	result.put(rs.getString(1), rs.getInt(2));
        }
        return result;
    }
    
    public static List<Integer> lookupCPUServiceIDsByCPUID(Connection conn, int cpu_id) throws Exception {
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(CPU_SERVICE.CPU_SERVICE_ID);
        sb.append(" FROM ").append(CPU_SERVICE);
        sb.append(" WHERE ").append(CPU_SERVICE.CPU_ID).append(" = ").append(cpu_id);
        sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" != ").append(CPUState.RESERVED.getValue());
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        List<Integer> result = new LinkedList<Integer>();
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        return result;
    }
    
    public static ResultSet lookupCPUByID(Connection conn, boolean updatable, int cpu_id) throws Exception {
        DBTableCPU CPU = DBTable.CPU;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(CPU);
        sb.append(" WHERE ").append(CPU.CPU_ID).append(" = ").append(cpu_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
    }
    
    public static ResultSet lookupCPUServiceByID(Connection conn, boolean updatable, int cs_id) throws Exception {
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(CPU_SERVICE);
        sb.append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_ID).append(" = ").append(cs_id);
        sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" != ").append(CPUState.RESERVED.getValue());
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
    }
    
    public static ResultSet lookupCPUServiceReservedByID(Connection conn, boolean updatable, int cpu_id) throws Exception {
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(CPU_SERVICE);
        sb.append(" WHERE ").append(CPU_SERVICE.CPU_ID).append(" = ").append(cpu_id);
        sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(CPUState.RESERVED.getValue());
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
    }
    
    public static ResultSet lookupCPUByDate(Connection conn, CPUState state, Date beg, Date end, DBTableColumn sorted, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableServer SERVER = DBTable.SERVER;
        DBTableCPU CPU = DBTable.CPU;
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(CPU.ANY).append(", ").append(CPU_SERVICE.ANY).append(", ");
        sb.append(SERVER.SERVER_NAME).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
        sb.appendDateLifeRemains(CPU_SERVICE.CPU_SERVICE_STARTTIME, CPU_SERVICE.CPU_SERVICE_ENDTIME, CPU_SERVICE.CPU_SERVICE_LIFE).append(" FROM "); {
            sb.append(CPU_SERVICE);
            sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(CPU_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
            sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
            sb.append(" LEFT JOIN ").append(CPU).append(" ON ").append(CPU_SERVICE.CPU_ID).append(" = ").append(CPU.CPU_ID);
            sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(CPU.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
        }
        sb.append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_USED).append(" != ").append(0);
        if (state != null) {
            sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(state.getValue());
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(CPU_SERVICE.USER_ID).append(" = ").append(user_id);
        }
        if (account_id >= 0) {
            sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (beg != null || end != null) {
            sb.append(" AND ("); {
                sb.appendDateBound(CPU_SERVICE.CPU_SERVICE_CREATIONTIME, beg, end);
                sb.append(" OR ");
                sb.appendDateBound(CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME, beg, end);
            }
            sb.append(")");
        }
        if (sorted != null) {
            sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
        }
        return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
    }
    
    public static int createCPU(Connection conn, String cpu_name, String cpu_desc, int cpu_total, int server_id) throws Exception {
        DBTableCPU CPU = DBTable.CPU;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(CPU).append(" ("); {
            sb.append(CPU.CPU_NAME).append(", ");
            sb.append(CPU.CPU_DESC).append(", ");
            sb.append(CPU.CPU_TOTAL).append(", ");
            sb.append(CPU.SERVER_ID).append(", ");
            sb.append(CPU.CPU_CREATIONTIME).append(", ");
            sb.append(CPU.CPU_MODIFIEDTIME);
        }
        sb.append(") VALUES ("); {
            sb.appendString(cpu_name).append(", ");
            sb.appendString(cpu_desc).append(", ");
            sb.append(cpu_total).append(", ");
            sb.append(server_id).append(", ");
            sb.appendDate().append(", ");
            sb.appendNull();
        }
        sb.append(")");
        Statement stat = conn.createStatement();
        stat.executeUpdate(sb.toSql(log), new String[]{CPU.CPU_ID.toString()});
        ResultSet rs = stat.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }
    
    public static int createCPUService(Connection conn, String cs_desc, int cs_used, Date cs_starttime, Date cs_endtime, CPUState state, int cpu_id, int user_id) throws Exception {
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(CPU_SERVICE).append(" ("); {
            sb.append(CPU_SERVICE.CPU_SERVICE_DESC).append(", ");
            sb.append(CPU_SERVICE.CPU_SERVICE_USED).append(", ");
            sb.append(CPU_SERVICE.CPU_SERVICE_STARTTIME).append(", ");
            sb.append(CPU_SERVICE.CPU_SERVICE_ENDTIME).append(", ");
            sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(", ");
            sb.append(CPU_SERVICE.CPU_SERVICE_CREATIONTIME).append(", ");
            sb.append(CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME).append(", ");
            sb.append(CPU_SERVICE.CPU_ID).append(", ");
            sb.append(CPU_SERVICE.USER_ID);
        }
        sb.append(") VALUES ("); {
            sb.appendString(cs_desc).append(", ");
            sb.append(cs_used).append(", ");
            sb.appendDate(cs_starttime).append(", ");
            sb.appendDate(cs_endtime).append(", ");
            sb.append(state.getValue()).append(", ");
            sb.appendDate().append(", ");
            sb.appendNull().append(", ");
            sb.append(cpu_id).append(", ");
            if (user_id == -1) {
                sb.appendNull();
            }
            else {
                sb.append(user_id);
            }
        }
        sb.append(")");
        Statement stat = conn.createStatement();
        stat.executeUpdate(sb.toSql(log), new String[]{CPU_SERVICE.CPU_SERVICE_ID.toString()});
        ResultSet rs = stat.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }
    
    public static Map<Integer, Integer> lookupCPUCounts(Connection conn, int account_id, int user_id) throws Exception {
        DBTableUser USER = DBTable.USER;
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ");
        sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(", sum(").append(CPU_SERVICE.CPU_SERVICE_USED).append(")").append(" FROM ");
        sb.append(CPU_SERVICE);
        if (account_id >= 0) {
            sb.append(" LEFT JOIN ").append(USER).append(" ON ");
            sb.append(CPU_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
        }
        sb.append(" WHERE 1=1");
        if (user_id >= 0) {
            sb.append(" AND ").append(CPU_SERVICE.USER_ID).append(" = ").append(user_id);
        }
        if (account_id >= 0) {
            sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
        }
        sb.append(" GROUP BY ").append(CPU_SERVICE.CPU_SERVICE_STATE);
        
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
