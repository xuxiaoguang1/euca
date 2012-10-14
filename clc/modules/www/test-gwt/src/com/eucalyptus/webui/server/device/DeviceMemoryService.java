package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.MemoryInfo;
import com.eucalyptus.webui.shared.resource.device.MemoryServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceMemoryService {
    
    private DeviceMemoryDBProcWrapper dbproc = new DeviceMemoryDBProcWrapper();
    
    private DeviceMemoryService() {
    }
    
    private static DeviceMemoryService instance = new DeviceMemoryService();
    
    public static DeviceMemoryService getInstance() {
        return instance;
    }
    
    private LoginUserProfile getUser(Session session) {
        return LoginUserProfileStorer.instance().get(session.getId());
    }
    
    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
    
    private static final List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "内存名称"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "所属服务器"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "服务状态"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "账户名称"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "用户名称"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(false, "0%", new ClientMessage("", "服务描述"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "总数量(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "占用数量(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "开始时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "结束时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "剩余(天)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "添加时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "修改时间"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "0%", new ClientMessage("", "硬件描述"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "硬件添加时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "硬件修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.MEMORY.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.MEMORY.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.MEMORY.SERVER_NAME: return DBTable.SERVER.SERVER_NAME;
        case CellTableColumns.MEMORY.MEMORY_NAME: return DBTable.MEMORY.MEMORY_NAME;
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
    
    private static final long MEMORY_UNIT = 1024 * 1024;
    
    public synchronized SearchResult lookupMemoryByDate(Session session, SearchRange range, MemoryState memory_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupMemoryByDate(memory_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), -1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupMemoryByDate(memory_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupMemoryByDate(memory_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableServer SERVER = DBTable.SERVER;
            DBTableMemory MEMORY = DBTable.MEMORY;
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
            	if (start <= index && index < end) {
	                int memory_id = DBData.getInt(rs, MEMORY.MEMORY_ID);
	                int ms_id = DBData.getInt(rs, MEMORY_SERVICE.MEMORY_SERVICE_ID);
	                String memory_name = DBData.getString(rs, MEMORY.MEMORY_NAME);
	                String memory_desc = DBData.getString(rs, MEMORY.MEMORY_DESC);
	                String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
	                long memory_total = DBData.getLong(rs, MEMORY.MEMORY_TOTAL) / MEMORY_UNIT;
	                Date memory_creationtime = DBData.getDate(rs, MEMORY.MEMORY_CREATIONTIME);
	                Date memory_modifiedtime = DBData.getDate(rs, MEMORY.MEMORY_MODIFIEDTIME);
	                memory_state = MemoryState.getMemoryState(DBData.getInt(rs, MEMORY_SERVICE.MEMORY_SERVICE_STATE));
	                String account_name = null;
	                String user_name = null;
	                String ms_desc = null;
	                long ms_used = DBData.getLong(rs, MEMORY_SERVICE.MEMORY_SERVICE_USED) / MEMORY_UNIT;
	                Date ms_starttime = null;
	                Date ms_endtime = null;
	                String ms_life = null;
	                Date ms_creationtime = null;
	                Date ms_modifiedtime = null;
	                if (memory_state != MemoryState.RESERVED) {
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
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_ID, memory_id);
	            	row.setColumn(CellTableColumns.MEMORY.RESERVED_CHECKBOX, "");
	            	row.setColumn(CellTableColumns.MEMORY.RESERVED_INDEX, index + 1);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_NAME, memory_name);
	            	row.setColumn(CellTableColumns.MEMORY.SERVER_NAME, server_name);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE, memory_state.toString());
	            	row.setColumn(CellTableColumns.MEMORY.ACCOUNT_NAME, account_name);
	            	row.setColumn(CellTableColumns.MEMORY.USER_NAME, user_name);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_DESC, ms_desc);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_TOTAL, memory_total);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_USED, ms_used);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_STARTTIME, ms_starttime);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_ENDTIME, ms_endtime);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_LIFE, ms_life);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_CREATIONTIME, ms_creationtime);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_SERVICE_MODIFIEDTIME, ms_modifiedtime);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_DESC, memory_desc);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_CREATIONTIME, memory_creationtime);
	            	row.setColumn(CellTableColumns.MEMORY.MEMORY_MODIFIEDTIME, memory_modifiedtime);
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
            throw new EucalyptusServiceException(new ClientMessage("", "获取内存列表失败"));
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public synchronized Map<Integer, Long> lookupMemoryCountsGroupByState(Session session) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupMemoryCountsGroupByState(-1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupMemoryCountsGroupByState(user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupMemoryCountsGroupByState(user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            Map<Integer, Long> map = new HashMap<Integer, Long>();
            long sum = 0;
            while (rs.next()) {
                int memory_state = rs.getInt(1);
                long memory_total = rs.getLong(2);
                sum += memory_total;
                map.put(memory_state, memory_total / MEMORY_UNIT);
            }
            map.put(-1, sum / MEMORY_UNIT);
            return map;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取内存计数失败"));
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public synchronized void addMemory(Session session, String memory_name, String memory_desc, long memory_size, String server_name) throws EucalyptusServiceException { 
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (isEmpty(memory_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的内存名称"));
        }
        if (memory_size <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的内存大小"));
        }
        if (isEmpty(server_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
        }
        if (memory_desc == null) {
            memory_desc = "";
        }
        long memory_total = memory_size * MEMORY_UNIT;
        try {
            dbproc.createMemory(memory_name, memory_desc, memory_total, server_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "创建内存失败"));
        }
    }
    
    public synchronized void addMemoryService(Session session, String ms_desc, long ms_size, Date ms_starttime, Date ms_endtime, int memory_id, String account_name, String user_name) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (ms_starttime == null || ms_endtime == null || DBData.calcLife(ms_endtime, ms_starttime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (ms_size <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的内存大小"));
        }
        if (isEmpty(account_name) || isEmpty(user_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的账户名称"));
        }
        if (ms_desc == null) {
            ms_desc = "";
        }
        long memory_used = ms_size * MEMORY_UNIT;
        try {
            dbproc.createMemoryService(ms_desc, memory_used, ms_starttime, ms_endtime, memory_id, account_name, user_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "创建内存服务失败"));
        }
    }
    
    public synchronized void deleteMemory(Session session, List<Integer> memory_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!memory_ids.isEmpty()) {
                dbproc.deleteMemory(memory_ids);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "删除内存失败"));
        }
    }
    
    public synchronized void deleteMemoryService(Session session, List<Integer> ms_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!ms_ids.isEmpty()) {
                dbproc.deleteMemoryService(ms_ids);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "删除内存服务失败"));
        }
    }
    
    public synchronized void modifyMemory(Session session, int memory_id, String memory_desc, long memory_size) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (memory_size <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的内存大小"));
        }
        if (memory_desc == null) {
            memory_desc = "";
        }
        long memory_total = memory_size * MEMORY_UNIT;
        ResultSetWrapper rsw = null;
        long total = 0;
        long reserved = 0;
        try {
            rsw = dbproc.getMemoryResizeInfo(memory_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            total = rs.getLong(1);
            reserved = rs.getLong(2);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取内存大小失败"));
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (memory_total < total - reserved) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的内存大小：已使用 " + (total - reserved) / MEMORY_UNIT));
        }
        try {
            dbproc.modifyMemory(memory_id, memory_desc, memory_total - total);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "修改内存失败"));
        }
    }
    
    public synchronized void modifyMemoryService(Session session, int ms_id, String ms_desc,
            Date ms_starttime, Date ms_endtime) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (ms_starttime == null || ms_endtime == null || DBData.calcLife(ms_endtime, ms_starttime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (ms_desc == null) {
            ms_desc = "";
        }
        try {
            dbproc.modifyMemoryService(ms_id, ms_desc, ms_starttime, ms_endtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "修改内存服务失败"));
        }
    }
    
    public synchronized void updateMemoryServiceState(int ms_id, MemoryState memory_state) throws EucalyptusServiceException {
        if (memory_state != MemoryState.INUSE && memory_state != MemoryState.STOP) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
        }
        try {
            dbproc.updateMemoryServiceState(ms_id, memory_state);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "更新内存服务状态失败"));
        }
    }
    
    public synchronized List<String> lookupMemoryNamesByServerName(Session session, String server_name) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (isEmpty(server_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
        }
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupMemoryNamesByServerName(server_name);
            DBTableMemory MEMORY = DBTable.MEMORY;
            ResultSet rs = rsw.getResultSet();
            List<String> memory_name_list = new LinkedList<String>();
            while (rs.next()) {
                String memory_name = DBData.getString(rs, MEMORY.MEMORY_NAME);
                memory_name_list.add(memory_name);
            }
            return memory_name_list;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取内存列表失败"));
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public synchronized MemoryInfo lookupMemoryInfoByID(int memory_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupMemoryByID(memory_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableMemory MEMORY = DBTable.MEMORY;
            String memory_name = DBData.getString(rs, MEMORY.MEMORY_NAME);
            String memory_desc = DBData.getString(rs, MEMORY.MEMORY_DESC);
            long memory_total = DBData.getLong(rs, MEMORY.MEMORY_TOTAL) / MEMORY_UNIT;
            Date memory_creationtime = DBData.getDate(rs, MEMORY.MEMORY_CREATIONTIME);
            Date memory_modifiedtime = DBData.getDate(rs, MEMORY.MEMORY_MODIFIEDTIME);
            int server_id = DBData.getInt(rs, MEMORY.SERVER_ID);
            return new MemoryInfo(memory_id, memory_name, memory_desc, memory_total, memory_creationtime, memory_modifiedtime, server_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取内存信息失败"));
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public synchronized MemoryServiceInfo lookupMemoryServiceInfoByID(int ms_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupMemoryServiceByID(ms_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
            String ms_desc = DBData.getString(rs, MEMORY_SERVICE.MEMORY_SERVICE_DESC);
            long ms_used = DBData.getLong(rs, MEMORY_SERVICE.MEMORY_SERVICE_USED) / MEMORY_UNIT;
            Date ms_starttime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME);
            Date ms_endtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME);
            MemoryState memory_state = MemoryState.getMemoryState(DBData.getInt(rs, MEMORY_SERVICE.MEMORY_SERVICE_STATE));
            Date ms_creationtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_CREATIONTIME);
            Date ms_modifiedtime = DBData.getDate(rs, MEMORY_SERVICE.MEMORY_SERVICE_MODIFIEDTIME);
            int memory_id = DBData.getInt(rs, MEMORY_SERVICE.MEMORY_ID);
            int user_id = DBData.getInt(rs, MEMORY_SERVICE.USER_ID);
            return new MemoryServiceInfo(ms_id, ms_desc, ms_used, ms_starttime, ms_endtime, memory_state, ms_creationtime, ms_modifiedtime, memory_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取内存服务信息失败"));
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

class DeviceMemoryDBProcWrapper {
    
    private static final Logger LOG = Logger.getLogger(DeviceMemoryDBProcWrapper.class.getName());

    private DBProcWrapper wrapper = DBProcWrapper.Instance();

    private ResultSetWrapper doQuery(String request) throws Exception {
        LOG.info(request);
        return wrapper.query(request);
    }

    private void doUpdate(String request) throws Exception {
        LOG.info(request);
        wrapper.update(request);
    }
    
    private void doCleanup() throws Exception {
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("DELETE FROM ").append(MEMORY_SERVICE).append(" WHERE ").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" = ").append(0);
        sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" != ").append(MemoryState.RESERVED.getValue());
        doUpdate(sb.toString());
    }
    
    public ResultSetWrapper lookupMemoryByID(int memory_id) throws Exception {
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(MEMORY).append(" WHERE ").append(MEMORY.MEMORY_ID).append(" = ").append(memory_id);
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupMemoryServiceByID(int ms_id) throws Exception {
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(MEMORY_SERVICE).append(" WHERE ").append(MEMORY_SERVICE.MEMORY_SERVICE_ID).append(" = ").append(ms_id);
        return doQuery(sb.toString());
    }
    
    private DBStringBuilder appendBoundedDate(DBStringBuilder sb, DBTableColumn column, Date dateBegin, Date dateEnd) {
        sb.append("(");
        sb.append(column).append(" != ").appendString("0000-00-00");
        if (dateBegin != null) {
            sb.append(" AND ").append(column).append(" >= ").appendDate(dateBegin);
        }
        if (dateEnd != null) {
            sb.append(" AND ").append(column).append(" <= ").appendDate(dateEnd);
        }
        sb.append(")");
        return sb;
    }
    
	private DBStringBuilder appendServiceLife(DBStringBuilder sb, DBTableColumn start, DBTableColumn end, DBTableColumn alias) {
		sb.append(" IF (");
		sb.append("DATEDIFF(").append(end).append(", ").append(start).append(")");
		sb.append(" < ");
		sb.append("DATEDIFF(").append(end).append(", now())");
		sb.append(", ");
		sb.append("DATEDIFF(").append(end).append(", ").append(start).append(")");
		sb.append(", ");
		sb.append("DATEDIFF(").append(end).append(", now())");
		sb.append(") AS ").append(alias);
		return sb;
	}
    
    public ResultSetWrapper lookupMemoryByDate(MemoryState memory_state, Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableServer SERVER = DBTable.SERVER;
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(MEMORY.ANY).append(", ").append(MEMORY_SERVICE.ANY).append(", ").append(SERVER.SERVER_NAME).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
		appendServiceLife(sb, MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME, MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME, MEMORY_SERVICE.MEMORY_SERVICE_LIFE).append(" FROM ");
        sb.append(MEMORY_SERVICE).append(" LEFT JOIN ").append(USER).append(" ON ").append(MEMORY_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
        sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
        sb.append(" LEFT JOIN ").append(MEMORY).append(" ON ").append(MEMORY_SERVICE.MEMORY_ID).append(" = ").append(MEMORY.MEMORY_ID);
        sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(MEMORY.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
        sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" != ").append(0);
        if (memory_state != null) {
            sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" = ").append(memory_state.getValue());
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
        }
        if (account_id >= 0) {
            sb.append(" AND ").append(ACCOUNT.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (dateBegin != null || dateEnd != null) {
            sb.append(" AND (");
            appendBoundedDate(sb, MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME, dateBegin, dateEnd);
            sb.append(")");
        }
        if (sort != null) {
        	if (sort.belongsTo(ACCOUNT) || sort.belongsTo(USER)) {
				sb.append(" ORDER BY ").append(sort.getName()).append(isAscending ? " ASC" : " DESC");
			}
			else {
				sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
			}
        }
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupMemoryCountsGroupByState(int account_id, int user_id) throws Exception {
        DBTableUser USER = DBTable.USER;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(", sum(").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(") FROM ").append(MEMORY_SERVICE);
        if (account_id >= 0 || user_id >= 0) {
            sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(MEMORY_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
        }
        sb.append(" WHERE 1=1");
        if (account_id >= 0) {
            sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
        }
        sb.append(" GROUP BY ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE);
        return doQuery(sb.toString());
    }
    
    public void createMemory(String mem_name, String mem_desc, long mem_total, String server_name) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(MEMORY).append(" (");
        sb.append(MEMORY.MEMORY_NAME).append(", ");
        sb.append(MEMORY.MEMORY_DESC).append(", ");
        sb.append(MEMORY.MEMORY_TOTAL).append(", ");
        sb.append(MEMORY.SERVER_ID).append(", ");
        sb.append(MEMORY.MEMORY_CREATIONTIME);
        sb.append(") VALUES (");
        sb.appendString(mem_name).append(", ");
        sb.appendString(mem_desc).append(", ");
        sb.append(mem_total).append(", ");
        sb.append("(SELECT ").append(SERVER.SERVER_ID).append(" FROM ").append(SERVER).append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name).append("), ");
        sb.appendDate(new Date()).append(")");
        doUpdate(sb.toString());
        
        sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(MEMORY_SERVICE).append(" (");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_DESC).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_CREATIONTIME).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_MODIFIEDTIME).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_ID).append(", ");
        sb.append(MEMORY_SERVICE.USER_ID);
        sb.append(") VALUES (");
        sb.appendString(null).append(", ");
        sb.append(mem_total).append(", ");
        sb.appendNull().append(", ");
        sb.appendNull().append(", ");
        sb.append(MemoryState.RESERVED.getValue()).append(", ");
        sb.appendNull().append(", ");
        sb.appendNull().append(", ");
        sb.append("(SELECT MAX(").append(MEMORY.MEMORY_ID).append(") FROM ").append(MEMORY).append(" WHERE 1=1)").append(", ");
        sb.appendNull().append(")");
        doUpdate(sb.toString());
        
        doCleanup();
    }
    
    public void createMemoryService(String ms_desc, long ms_used, Date ms_starttime, Date ms_endtime, int mem_id, String account_name, String user_name) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        sb.append("INSERT INTO ").append(MEMORY_SERVICE).append(" (");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_DESC).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_CREATIONTIME).append(", ");
        sb.append(MEMORY_SERVICE.USER_ID).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_ID);
        sb.append(") VALUES (");
        sb.appendString(ms_desc).append(", ");
        sb.append(ms_used).append(", ");
        sb.appendDate(ms_starttime).append(", ");
        sb.appendDate(ms_endtime).append(", ");
        sb.append(MemoryState.STOP.getValue()).append(", ");
        sb.appendDate(new Date()).append(", ");
        sb.append("(SELECT ").append(USER.USER_ID).append(" FROM ").append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE ");
        sb.append(USER.USER_NAME).append(" = ").appendString(user_name).append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name).append(")").append(", ");
        sb.append(mem_id).append(")");
        doUpdate(sb.toString());
        
        int ms_id = getMaxMemoryServiceID();
        
        DBTableAlias A = DBTable.getDBTableAlias("A");
        DBTableAlias B = DBTable.getDBTableAlias("B");
        DBTableColumn A_USED = A.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_USED);
        DBTableColumn B_USED = B.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_USED);
        sb = new DBStringBuilder();
        sb.append("UPDATE ").append(MEMORY_SERVICE).append(" ").append(A).append(", ").append(MEMORY_SERVICE).append(" ").append(B).append(" SET ");
        sb.append(A_USED).append(" = ").append(ms_used).append(", ");
        sb.append(B_USED).append(" = ").append("(").append(B_USED).append(" - ").append(ms_used).append(")");
        sb.append(" WHERE ").append(A.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_ID)).append(" = ").append(ms_id);
        sb.append(" AND ").append(A.getColumn(MEMORY_SERVICE.MEMORY_ID)).append(" = ").append(B.getColumn(MEMORY_SERVICE.MEMORY_ID));
        sb.append(" AND ").append(B.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_STATE)).append(" = ").append(MemoryState.RESERVED.getValue());
        sb.append(" AND ").append(B.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_USED)).append(" >= ").append(ms_used);
        doUpdate(sb.toString());
        
        doCleanup();
    }
    
    private int getMaxMemoryServiceID() throws Exception {
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT MAX(").append(MEMORY_SERVICE.MEMORY_SERVICE_ID).append(") FROM ").append(MEMORY_SERVICE).append(" WHERE 1=1");
        ResultSetWrapper rsw = null;
        try {
            rsw = doQuery(sb.toString());
            ResultSet rs = rsw.getResultSet();
            rs.next();
            return rs.getInt(1);
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public ResultSetWrapper getMemoryResizeInfo(int memory_id) throws Exception {
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(MEMORY.MEMORY_TOTAL).append(", ").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" FROM ").append(MEMORY_SERVICE);
        sb.append(" LEFT JOIN ").append(MEMORY).append(" ON ").append(MEMORY_SERVICE.MEMORY_ID).append(" = ").append(MEMORY.MEMORY_ID);
        sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_ID).append(" = ").append(memory_id);
        sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" = ").append(MemoryState.RESERVED.getValue());
        return doQuery(sb.toString());
    }
    
    public void deleteMemory(List<Integer> mem_ids) throws Exception {
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int mem_id : mem_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(mem_id);
        }
        
        sb.append("DELETE ").append(MEMORY_SERVICE).append(" FROM ").append(MEMORY_SERVICE).append(" LEFT JOIN ").append(MEMORY);
        sb.append(" ON ").append(MEMORY.MEMORY_ID).append(" = ").append(MEMORY_SERVICE.MEMORY_ID);
        sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_ID).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" = ").append(MemoryState.RESERVED.getValue());
        sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" = ").append(MEMORY.MEMORY_TOTAL);
        doUpdate(sb.toString());
        
        sb = new DBStringBuilder();
        sb.append("DELETE FROM ").append(MEMORY).append(" WHERE ").append(MEMORY.MEMORY_ID).append(" IN (").append(ids.toString()).append(")");
        doUpdate(sb.toString());
        
        doCleanup();
    }
    
    public void deleteMemoryService(List<Integer> ms_ids) throws Exception {
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int ms_id : ms_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(ms_id);
        }
        
        DBTableAlias A = DBTable.getDBTableAlias("A");
        DBTableAlias B = DBTable.getDBTableAlias("B");
        DBTableColumn A_USED = A.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_USED);
        DBTableColumn B_USED = B.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_USED);
        sb.append("UPDATE ").append(MEMORY_SERVICE).append(" ").append(A).append(", ").append(MEMORY_SERVICE).append(" ").append(B).append(" SET ");
        sb.append(A_USED).append(" = ").append(0).append(", ");
        sb.append(B_USED).append(" = ").append("(").append(B_USED).append(" + ").append(A_USED).append(")");
        sb.append(" WHERE ").append(A.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_ID)).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(A.getColumn(MEMORY_SERVICE.MEMORY_ID)).append(" = ").append(B.getColumn(MEMORY_SERVICE.MEMORY_ID));
        sb.append(" AND ").append(B.getColumn(MEMORY_SERVICE.MEMORY_SERVICE_STATE)).append(" = ").append(MemoryState.RESERVED.getValue());
        doUpdate(sb.toString());
        
        doCleanup();
    }
    
    public void modifyMemory(int mem_id, String mem_desc, long mem_resize) throws Exception {
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(MEMORY).append(" LEFT JOIN ").append(MEMORY_SERVICE).append(" ON ").append(MEMORY.MEMORY_ID).append(" = ").append(MEMORY_SERVICE.MEMORY_ID).append(" SET ");
        sb.append(MEMORY.MEMORY_DESC).append(" = ").appendString(mem_desc).append(", ");
        sb.append(MEMORY.MEMORY_TOTAL).append(" = (").append(MEMORY.MEMORY_TOTAL).append(" + ").append(mem_resize).append("), ");
        sb.append(MEMORY.MEMORY_MODIFIEDTIME).append(" = ").appendDate(new Date()).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" = (").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" + ").append(mem_resize).append(")");
        sb.append(" WHERE ").append(MEMORY.MEMORY_ID).append(" = ").append(mem_id);
        sb.append(" AND ").append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" = ").append(MemoryState.RESERVED.getValue());
        sb.append(" AND ").append("(").append(MEMORY_SERVICE.MEMORY_SERVICE_USED).append(" + ").append(mem_resize).append(")").append(" >= 0");
        doUpdate(sb.toString());
    }
    
    public void modifyMemoryService(int ms_id, String ms_desc, Date ms_starttime, Date ms_endtime) throws Exception {
    	DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(MEMORY_SERVICE).append(" SET ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_DESC).append(" = ").appendString(ms_desc).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STARTTIME).append(" = ").appendDate(ms_starttime).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_ENDTIME).append(" = ").appendDate(ms_endtime).append(", ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
        sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_SERVICE_ID).append(" = ").append(ms_id);
        doUpdate(sb.toString());
    }
    
    public void updateMemoryServiceState(int ms_id, MemoryState memory_state) throws Exception {
    	DBTableMemoryService MEMORY_SERVICE = DBTable.MEMORY_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(MEMORY_SERVICE).append(" SET ");
        sb.append(MEMORY_SERVICE.MEMORY_SERVICE_STATE).append(" = ").append(memory_state.getValue());
        sb.append(" WHERE ").append(MEMORY_SERVICE.MEMORY_SERVICE_ID).append(" = ").append(ms_id);
        doUpdate(sb.toString());
    }
    
    public ResultSetWrapper lookupMemoryNamesByServerName(String server_name) throws Exception {
    	DBTableServer SERVER = DBTable.SERVER;
        DBTableMemory MEMORY = DBTable.MEMORY;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT DISTINCT(").append(MEMORY.MEMORY_NAME).append(") FROM");
        sb.append(MEMORY).append(" LEFT JOIN ").append(SERVER).append(" ON ").append(MEMORY.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
        sb.append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name);
        sb.append(" ORDER BY ").append(MEMORY.MEMORY_NAME);
        return doQuery(sb.toString());
    }
    
}
