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
import com.eucalyptus.webui.shared.resource.device.IPServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceIPService {
	
	private DeviceIPDBProcWrapper dbproc = new DeviceIPDBProcWrapper();
	
    private DeviceIPService() {
    }
    
    private static DeviceIPService instance = new DeviceIPService();
    
    public static DeviceIPService getInstance() {
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
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "IP地址"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "地址类型"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "服务状态"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "账户名称"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "用户名称"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(false, "0%", new ClientMessage("", "服务描述"),
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
			new SearchResultFieldDesc(false, "0%", new ClientMessage("", "地址描述"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "地址添加时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "地址修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.IP.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.IP.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.IP.IP_ADDR: return DBTable.IP_SERVICE.IP_ADDR;
        case CellTableColumns.IP.IP_TYPE: return DBTable.IP_SERVICE.IP_TYPE;
        case CellTableColumns.IP.IP_CREATIONTIME: return DBTable.IP_SERVICE.IP_CREATIONTIME;
        case CellTableColumns.IP.IP_MODIFIEDTIME: return DBTable.IP_SERVICE.IP_MODIFIEDTIME;
        case CellTableColumns.IP.IP_SERVICE_STARTTIME: return DBTable.IP_SERVICE.IP_SERVICE_STARTTIME;
        case CellTableColumns.IP.IP_SERVICE_ENDTIME: return DBTable.IP_SERVICE.IP_SERVICE_ENDTIME;
        case CellTableColumns.IP.IP_SERVICE_LIFE: return DBTable.IP_SERVICE.IP_SERVICE_LIFE;
        case CellTableColumns.IP.IP_SERVICE_STATE: return DBTable.IP_SERVICE.IP_SERVICE_STATE;
        case CellTableColumns.IP.IP_SERVICE_CREATIONTIME: return DBTable.IP_SERVICE.IP_SERVICE_CREATIONTIME;
        case CellTableColumns.IP.IP_SERVICE_MODIFIEDTIME: return DBTable.IP_SERVICE.IP_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    public synchronized SearchResult lookupIPByDate(Session session, SearchRange range, IPType ip_type, IPState ip_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupIPByDate(ip_type, ip_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), -1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupIPByDate(ip_type, ip_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupIPByDate(ip_type, ip_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
            	if (start <= index && index < end) {
	            	int ip_id = DBData.getInt(rs, IP_SERVICE.IP_ID);
	                String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
	                ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
	                String ip_desc = DBData.getString(rs, IP_SERVICE.IP_DESC);
	                Date ip_creationtime = DBData.getDate(rs, IP_SERVICE.IP_CREATIONTIME);
	                Date ip_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_MODIFIEDTIME);
	                String account_name = null;
	                String user_name = null;
	                String is_desc = null;
	                ip_state = IPState.getIPState(DBData.getInt(rs, IP_SERVICE.IP_SERVICE_STATE));
	                Date is_starttime = null;
	                Date is_endtime = null;
	                String is_life = null;
	                Date is_creationtime = null;
	                Date is_modifiedtime = null;
	                if (ip_state != IPState.RESERVED) {
	                	account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
	                    user_name = DBData.getString(rs, USER.USER_NAME);
	                    is_desc = DBData.getString(rs, IP_SERVICE.IP_SERVICE_DESC);
	                    is_starttime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_STARTTIME);
	                    is_endtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_ENDTIME);
	                    is_life = DBData.getString(rs, IP_SERVICE.IP_SERVICE_LIFE);
	                    if (is_life != null) {
	                    	is_life = Integer.toString(Math.max(0, Integer.parseInt(is_life) + 1));
	                    }
	                    is_creationtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_CREATIONTIME);
	                    is_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_MODIFIEDTIME);
	                }
	            	CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.IP.COLUMN_SIZE);
	            	row.setColumn(CellTableColumns.IP.IP_ID, ip_id);
	            	row.setColumn(CellTableColumns.IP.RESERVED_CHECKBOX, "");
	            	row.setColumn(CellTableColumns.IP.RESERVED_INDEX, index + 1);
	            	row.setColumn(CellTableColumns.IP.IP_ADDR, ip_addr);
	            	row.setColumn(CellTableColumns.IP.IP_TYPE, ip_type.toString());
	            	row.setColumn(CellTableColumns.IP.IP_SERVICE_STATE, ip_state.toString());
	            	row.setColumn(CellTableColumns.IP.ACCOUNT_NAME, account_name);
	            	row.setColumn(CellTableColumns.IP.USER_NAME, user_name);
	            	row.setColumn(CellTableColumns.IP.IP_SERVICE_DESC, is_desc);
	            	row.setColumn(CellTableColumns.IP.IP_SERVICE_STARTTIME, is_starttime);
	            	row.setColumn(CellTableColumns.IP.IP_SERVICE_ENDTIME, is_endtime);
	            	row.setColumn(CellTableColumns.IP.IP_SERVICE_LIFE, is_life);
	            	row.setColumn(CellTableColumns.IP.IP_SERVICE_CREATIONTIME, is_creationtime);
	            	row.setColumn(CellTableColumns.IP.IP_SERVICE_MODIFIEDTIME, is_modifiedtime);
	            	row.setColumn(CellTableColumns.IP.IP_DESC, ip_desc);
	            	row.setColumn(CellTableColumns.IP.IP_CREATIONTIME, ip_creationtime);
	            	row.setColumn(CellTableColumns.IP.IP_MODIFIEDTIME, ip_modifiedtime);
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
            throw new EucalyptusServiceException(new ClientMessage("", "获取IP地址列表失败"));
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
    
    public synchronized Map<Integer, Integer> lookupIPCountsGroupByState(Session session, IPType ip_type) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupIPCountsGroupByState(ip_type, -1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupIPCountsGroupByState(ip_type, user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupIPCountsGroupByState(ip_type, user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            int sum = 0;
            while (rs.next()) {
                int ip_state = rs.getInt(1);
                int ip_count = rs.getInt(2);
                sum += ip_count;
                map.put(ip_state, ip_count);
            }
            map.put(-1, sum);
            return map;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取IP地址计数失败"));
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
    
    public synchronized void addIP(Session session, String ip_addr, String ip_desc, IPType ip_type) throws EucalyptusServiceException { 
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (isEmpty(ip_addr)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的IP地址"));
        }
        if (ip_type == null) {
        	throw new EucalyptusServiceException(new ClientMessage("", "无效的IP地址类型"));
        }
        if (ip_desc == null) {
        	ip_desc = "";
        }
        try {
        	dbproc.createIP(ip_addr, ip_desc, ip_type);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "创建IP地址失败"));
        }
    }
    
    public synchronized void addIPService(Session session, String is_desc, Date is_starttime, Date is_endtime, int ip_id, String account_name, String user_name) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (is_starttime == null || is_endtime == null || DBData.calcLife(is_endtime, is_starttime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (isEmpty(account_name) || isEmpty(user_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的账户名称"));
        }
        if (is_desc == null) {
            is_desc = "";
        }
        try {
        	dbproc.createIPService(is_desc, is_starttime, is_endtime, ip_id, account_name, user_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "创建IP地址服务失败"));
        }
    }
    
    public synchronized void deleteIP(Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!ip_ids.isEmpty()) {
                dbproc.deleteIP(ip_ids);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "删除IP地址失败"));
        }
    }
    
    public synchronized void deleteIPService(Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!ip_ids.isEmpty()) {
                dbproc.deleteIPService(ip_ids);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "删除IP地址服务失败"));
        }
    }
    
    public synchronized void modifyIP(Session session, int ip_id, String ip_desc, IPType ip_type) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (ip_desc == null) {
            ip_desc = "";
        }
        try {
            dbproc.modifyIP(ip_id, ip_desc, ip_type);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "修改IP地址失败"));
        }
    }
    
    public synchronized void modifyIPService(Session session, int ip_id, String is_desc, Date is_starttime, Date is_endtime) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (is_starttime == null || is_endtime == null || DBData.calcLife(is_endtime, is_starttime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (is_desc == null) {
        	is_desc = "";
        }
        try {
        	dbproc.modifyIPService(ip_id, is_desc, is_starttime, is_endtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "修改IP地址服务失败"));
        }
    }
    
    public synchronized void updateIPServiceState(int ip_id, IPState ip_state) throws EucalyptusServiceException {
        if (ip_state != IPState.INUSE && ip_state != IPState.STOP) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
        }
        try {
        	dbproc.updateIPServiceState(ip_id, ip_state);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "更新IP地址服务状态失败"));
        }
    }
    
    public synchronized List<String> lookupUnusedIPAddrByIPType(Session session, IPType ip_type) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        ResultSetWrapper rsw = null;
        try {
        	rsw = dbproc.lookupUnusedIPAddrByIPType(ip_type);
        	DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            ResultSet rs = rsw.getResultSet();
            List<String> ip_addr_list = new LinkedList<String>();
            while (rs.next()) {
                String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
                ip_addr_list.add(ip_addr);
            }
            return ip_addr_list;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取IP地址列表失败"));
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
    
    public synchronized IPServiceInfo lookupIPInfoByID(int ip_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupIPByID(ip_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
            String ip_desc = DBData.getString(rs, IP_SERVICE.IP_DESC);
            IPType ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
            Date ip_creationtime = DBData.getDate(rs, IP_SERVICE.IP_CREATIONTIME);
            Date ip_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_MODIFIEDTIME);
            IPState ip_state = IPState.getIPState(DBData.getInt(rs, IP_SERVICE.IP_SERVICE_STATE));
            String is_desc = null;
            Date is_starttime = null;
            Date is_endtime = null;
            Date is_creationtime = null;
            Date is_modifiedtime = null;
            int user_id = -1;
            if (ip_state != IPState.RESERVED) {
            	is_desc = DBData.getString(rs, IP_SERVICE.IP_SERVICE_DESC);
                is_starttime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_STARTTIME);
                is_endtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_ENDTIME);
                is_creationtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_CREATIONTIME);
                is_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_MODIFIEDTIME);
                user_id = DBData.getInt(rs, IP_SERVICE.USER_ID);
            }
            return new IPServiceInfo(ip_id, ip_addr, ip_desc, ip_type, ip_creationtime, ip_modifiedtime, is_desc, is_starttime, is_endtime, ip_state, is_creationtime, is_modifiedtime, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取IP地址服务信息失败"));
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

class DeviceIPDBProcWrapper {
	
    private static final Logger LOG = Logger.getLogger(DeviceIPDBProcWrapper.class.getName());

    private DBProcWrapper wrapper = DBProcWrapper.Instance();

    private ResultSetWrapper doQuery(String request) throws Exception {
        LOG.info(request);
        return wrapper.query(request);
    }

    private void doUpdate(String request) throws Exception {
        LOG.info(request);
        wrapper.update(request);
    }

    public ResultSetWrapper lookupIPByID(int ip_id) throws Exception {
    	DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(IP_SERVICE).append(" WHERE ").append(IP_SERVICE.IP_ID).append(" = ").append(ip_id);
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
    
    public ResultSetWrapper lookupIPByDate(IPType ip_type, IPState ip_state, Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP_SERVICE.ANY).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
		appendServiceLife(sb, IP_SERVICE.IP_SERVICE_STARTTIME, IP_SERVICE.IP_SERVICE_ENDTIME, IP_SERVICE.IP_SERVICE_LIFE).append(" FROM ");
		sb.append(IP_SERVICE).append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
		sb.append(" WHERE 1=1");
        if (ip_state != null) {
            sb.append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(ip_state.getValue());
        }
        if (ip_type != null) {
        	sb.append(" AND ").append(IP_SERVICE.IP_TYPE).append(" = ").append(ip_type.getValue());
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
        }
        if (account_id >= 0) {
            sb.append(" AND ").append(ACCOUNT.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (dateBegin != null || dateEnd != null) {
            sb.append(" AND (");
            appendBoundedDate(sb, IP_SERVICE.IP_SERVICE_STARTTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, IP_SERVICE.IP_SERVICE_ENDTIME, dateBegin, dateEnd);
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
    
    public ResultSetWrapper lookupIPCountsGroupByState(IPType ip_type, int account_id, int user_id) throws Exception {
        DBTableUser USER = DBTable.USER;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP_SERVICE.IP_SERVICE_STATE).append(", count(").append(IP_SERVICE.IP_SERVICE_STATE).append(") FROM ").append(IP_SERVICE);
        if (account_id >= 0 || user_id >= 0) {
            sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
        }
        sb.append(" WHERE 1=1");
        if (ip_type != null) {
        	sb.append(" AND ").append(IP_SERVICE.IP_TYPE).append(" = ").append(ip_type.getValue());
        }
        if (account_id >= 0) {
            sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
        }
        sb.append(" GROUP BY ").append(IP_SERVICE.IP_SERVICE_STATE);
        return doQuery(sb.toString());
    }
    
    public void createIP(String ip_addr, String ip_desc, IPType ip_type) throws Exception {
    	DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(IP_SERVICE).append(" (");
        sb.append(IP_SERVICE.IP_ADDR).append(", ");
        sb.append(IP_SERVICE.IP_DESC).append(", ");
        sb.append(IP_SERVICE.IP_TYPE).append(", ");
        sb.append(IP_SERVICE.IP_CREATIONTIME).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_STATE);
        sb.append(") VALUES (");
        sb.appendString(ip_addr).append(", ");
        sb.appendString(ip_desc).append(", ");
        sb.append(ip_type.getValue()).append(", ");
        sb.appendDate(new Date()).append(", ");
        sb.append(IPState.RESERVED.getValue()).append(")");
        doUpdate(sb.toString());
    }
    
    public void createIPService(String is_desc, Date is_starttime, Date is_endtime, int ip_id, String account_name, String user_name) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(IP_SERVICE).append(" SET ");
        sb.append(IP_SERVICE.IP_SERVICE_DESC).append(" = ").appendString(is_desc).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_STARTTIME).append(" = ").appendDate(is_starttime).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_ENDTIME).append(" = ").appendDate(is_endtime).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(IPState.STOP.getValue()).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_CREATIONTIME).append(" = ").appendDate(new Date()).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_MODIFIEDTIME).append(" = ").appendNull().append(", ");
        sb.append(IP_SERVICE.USER_ID).append(" = ");
        sb.append("(SELECT ").append(USER.USER_ID).append(" FROM ").append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE ");
        sb.append(USER.USER_NAME).append(" = ").appendString(user_name).append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name).append(")");
        sb.append(" WHERE ").append(IP_SERVICE.IP_ID).append(" = ").append(ip_id).append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(IPState.RESERVED.getValue());
        doUpdate(sb.toString());
    }
    
    public void deleteIP(List<Integer> ip_ids) throws Exception {
    	DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int ip_id : ip_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(ip_id);
        }
        
        sb.append("DELETE FROM ").append(IP_SERVICE).append(" WHERE ").append(IP_SERVICE.IP_ID).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(IPState.RESERVED.getValue());
        doUpdate(sb.toString());
    }
    
    public void deleteIPService(List<Integer> ip_ids) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int ip_id : ip_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(ip_id);
        }
        
        sb.append("UPDATE ").append(IP_SERVICE).append(" SET ");
        sb.append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(IPState.RESERVED.getValue()).append(", ");
        sb.append(IP_SERVICE.USER_ID).append(" = ").appendNull();
        sb.append(" WHERE ").append(IP_SERVICE.IP_ID).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" != ").append(IPState.RESERVED.getValue());
        doUpdate(sb.toString());
    }
    
    public void modifyIP(int ip_id, String ip_desc, IPType ip_type) throws Exception {
    	DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(IP_SERVICE).append(" SET ");
        sb.append(IP_SERVICE.IP_DESC).append(" = ").appendString(ip_desc).append(", ");
        if (ip_type != null) {
        	sb.append(IP_SERVICE.IP_TYPE).append(" = ").append(ip_type.getValue()).append(", ");
        }
        sb.append(IP_SERVICE.IP_MODIFIEDTIME).append(" = ").appendDate(new Date());
        sb.append(" WHERE ").append(IP_SERVICE.IP_ID).append(" = ").append(ip_id);
        doUpdate(sb.toString());
    }
    
    public void modifyIPService(int ip_id, String is_desc, Date is_starttime, Date is_endtime) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(IP_SERVICE).append(" SET ");
        sb.append(IP_SERVICE.IP_SERVICE_DESC).append(" = ").appendString(is_desc).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_STARTTIME).append(" = ").appendDate(is_starttime).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_ENDTIME).append(" = ").appendDate(is_endtime).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
        sb.append(" WHERE ").append(IP_SERVICE.IP_ID).append(" = ").append(ip_id);
        sb.append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" != ").append(IPState.RESERVED.getValue());
        doUpdate(sb.toString());
    }
    
    public void updateIPServiceState(int ip_id, IPState ip_state) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(IP_SERVICE).append(" SET ");
        sb.append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(ip_state.getValue());
        sb.append(" WHERE ").append(IP_SERVICE.IP_ID).append(" = ").append(ip_id);
        sb.append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" != ").append(IPState.RESERVED.getValue());
        doUpdate(sb.toString());
    }
    
    public ResultSetWrapper lookupUnusedIPAddrByIPType(IPType ip_type) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP_SERVICE.IP_ADDR).append(" FROM ").append(IP_SERVICE);
        sb.append(" WHERE ").append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(IPState.RESERVED.getValue());
        if (ip_type != null) {
        	sb.append(" AND ").append(IP_SERVICE.IP_TYPE).append(" = ").append(ip_type.getValue());
        }
        sb.append(" ORDER BY ").append(IP_SERVICE.IP_TYPE).append(", ").append(IP_SERVICE.IP_ADDR);
        return doQuery(sb.toString());
    }
    
}
