package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import com.eucalyptus.webui.shared.resource.device.IPInfo;
import com.eucalyptus.webui.shared.resource.device.IPServiceInfo;
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
            new SearchResultFieldDesc(null, "0%",false),
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "IP地址"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "类型"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "账户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "用户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "开始时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "结束时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "剩余(天)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "状态"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.IP.IP_ADDR: return DBTable.IP.IP_ADDR;
        case CellTableColumns.IP.IP_TYPE: return DBTable.IP.IP_TYPE;
        case CellTableColumns.IP.IP_CREATIONTIME: return DBTable.IP.IP_CREATIONTIME;
        case CellTableColumns.IP.IP_MODIFIEDTIME: return DBTable.IP.IP_MODIFIEDTIME;
        case CellTableColumns.IP.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.IP.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.IP.IP_SERVICE_STARTTIME: return DBTable.IP_SERVICE.IP_SERVICE_STARTTIME;
        case CellTableColumns.IP.IP_SERVICE_ENDTIME: return DBTable.IP_SERVICE.IP_SERVICE_ENDTIME;
        case CellTableColumns.IP.IP_SERVICE_STATE: return DBTable.IP_SERVICE.IP_SERVICE_STATE;
        case CellTableColumns.IP.IP_SERVICE_CREATIONTIME: return DBTable.IP_SERVICE.IP_SERVICE_CREATIONTIME;
        case CellTableColumns.IP.IP_SERVICE_MODIFIEDTIME: return DBTable.IP_SERVICE.IP_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    private int getLife(Date starttime, Date endtime) {
    	final long div = 1000L * 24 * 3600;
    	long start = starttime.getTime() / div, end = endtime.getTime() / div;
    	return start <= end ? (int)(end - start) + 1 : 0;
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
            DBTableIP IP = DBTable.IP;
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            Date today = new Date();
            for (int index = 1; rs.next(); index ++) {
            	int ip_id = DBData.getInt(rs, IP.IP_ID);
                String ip_addr = DBData.getString(rs, IP.IP_ADDR);
                ip_type = IPType.getIPType(DBData.getInt(rs, IP.IP_TYPE));
                String ip_desc = DBData.getString(rs, IP.IP_DESC);
                Date ip_creationtime = DBData.getDate(rs, IP.IP_CREATIONTIME);
                Date ip_modifiedtime = DBData.getDate(rs, IP.IP_MODIFIEDTIME);
                String account_name = null;
                String user_name = null;
                String is_desc = null;
                ip_state = IPState.RESERVED;
                Date is_starttime = null;
                Date is_endtime = null;
                String is_life = null;
                Date is_creationtime = null;
                Date is_modifiedtime = null;
                String is_id = DBData.getString(rs, IP_SERVICE.IP_SERVICE_ID);
                if (!isEmpty(is_id)) {
                	ip_state = IPState.getIPState(DBData.getInt(rs, IP_SERVICE.IP_SERVICE_STATE));
                	account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                    user_name = DBData.getString(rs, USER.USER_NAME);
                    is_desc = DBData.getString(rs, IP_SERVICE.IP_SERVICE_DESC);
                    is_starttime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_STARTTIME);
                    is_endtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_ENDTIME);
                    is_life = Integer.toString(Math.min(getLife(is_starttime, is_endtime), getLife(today, is_endtime)));
                    is_creationtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_CREATIONTIME);
                    is_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_MODIFIEDTIME);
                }
                List<String> list = Arrays.asList(Integer.toString(ip_id), is_id, "", Integer.toString(index ++),
                		ip_addr, ip_type.toString(), ip_desc, DBData.format(ip_creationtime), DBData.format(ip_modifiedtime), account_name, user_name, is_desc,
                        DBData.format(is_starttime), DBData.format(is_endtime), is_life, ip_state.toString(),
                        DBData.format(is_creationtime), DBData.format(is_modifiedtime));
                rows.add(new SearchResultRow(list));
            }
            final int col_life = CellTableColumns.IP.IP_SERVICE_LIFE;
            if (range.getSortField() == col_life) {
                final boolean isAscending = range.isAscending();
                Collections.sort(rows, new Comparator<SearchResultRow>() {

                    @Override
                    public int compare(SearchResultRow arg0, SearchResultRow arg1) {
                        String life0 = arg0.getField(col_life), life1 = arg1.getField(col_life);
                        int result;
                        if (life0.length() == 0) {
                            result = life1.length() == 0 ? 0 : -1;
                        }
                        else {
                            result = life1.length() == 0 ? 1 : Integer.parseInt(life0) - Integer.parseInt(life1);
                        }
                        if (!isAscending) {
                            result = -result;
                        }
                        return result;
                    }
                    
                });
                int index = 1;
                for (SearchResultRow row : rows) {
                    row.setField(3, Integer.toString(index ++));
                }
            }
            SearchResult result = new SearchResult(rows.size(), range, FIELDS_DESC);
            int size = Math.min(range.getLength(), rows.size() - range.getStart());
            int from = range.getStart(), to = range.getStart() + size;
            if (from < to) {
                result.setRows(rows.subList(from, to));
            }
            for (SearchResultRow row : result.getRows()) {
                System.out.println(row);
            }
            return result;
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
        if (is_starttime == null || is_endtime == null) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (getLife(is_starttime, is_endtime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
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
    
    public synchronized void deleteIPService(Session session, List<Integer> is_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!is_ids.isEmpty()) {
                dbproc.deleteIPService(is_ids);
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
    
    public synchronized void modifyIPService(Session session, int is_id, String is_desc, Date is_starttime, Date is_endtime) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (is_starttime == null || is_endtime == null) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (getLife(is_starttime, is_endtime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
        }
        if (is_desc == null) {
        	is_desc = "";
        }
        try {
        	dbproc.modifyIPService(is_id, is_desc, is_starttime, is_endtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "修改IP地址服务失败"));
        }
    }
    
    public synchronized void updateIPServiceState(int is_id, IPState ip_state) throws EucalyptusServiceException {
        if (ip_state != IPState.INUSE && ip_state != IPState.STOP) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
        }
        try {
        	dbproc.updateIPServiceState(is_id, ip_state);
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
            DBTableIP IP = DBTable.IP;
            ResultSet rs = rsw.getResultSet();
            List<String> ip_addr_list = new LinkedList<String>();
            while (rs.next()) {
                String ip_addr = DBData.getString(rs, IP.IP_ADDR);
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
    
    public synchronized IPInfo lookupIPInfoByID(int ip_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupIPByID(ip_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableIP IP = DBTable.IP;
            String ip_addr = DBData.getString(rs, IP.IP_ADDR);
            String ip_desc = DBData.getString(rs, IP.IP_DESC);
            IPType ip_type = IPType.getIPType(DBData.getInt(rs, IP.IP_TYPE));
            Date ip_creationtime = DBData.getDate(rs, IP.IP_CREATIONTIME);
            Date ip_modifiedtime = DBData.getDate(rs, IP.IP_MODIFIEDTIME);
            return new IPInfo(ip_id, ip_addr, ip_desc, ip_type, ip_creationtime, ip_modifiedtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取IP地址信息失败"));
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
    
    public synchronized IPServiceInfo lookupIPServiceInfoByID(int is_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupIPServiceByID(is_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            String is_desc = DBData.getString(rs, IP_SERVICE.IP_SERVICE_DESC);
            Date is_starttime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_STARTTIME);
            Date is_endtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_ENDTIME);
            IPState ip_state = IPState.getIPState(DBData.getInt(rs, IP_SERVICE.IP_SERVICE_STATE));
            Date is_creationtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_CREATIONTIME);
            Date is_modifiedtime = DBData.getDate(rs, IP_SERVICE.IP_SERVICE_MODIFIEDTIME);
            int ip_id = DBData.getInt(rs, IP_SERVICE.IP_ID);
            int user_id = DBData.getInt(rs, IP_SERVICE.USER_ID);
            return new IPServiceInfo(is_id, is_desc, is_starttime, is_endtime, ip_state, is_creationtime, is_modifiedtime, ip_id, user_id);
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
    	DBTableIP IP = DBTable.IP;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(IP).append(" WHERE ").append(IP.IP_ID).append(" = ").append(ip_id);
        return doQuery(sb.toString());
    }

    public ResultSetWrapper lookupIPServiceByID(int is_id) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(IP_SERVICE).append(" WHERE ").append(IP_SERVICE.IP_SERVICE_ID).append(" = ").append(is_id);
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
    
    public ResultSetWrapper lookupIPByDate(IPType ip_type, IPState ip_state, Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableIP IP = DBTable.IP;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP.ANY).append(", ").append(IP_SERVICE.ANY).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(" FROM ");
		sb.append(IP_SERVICE).append(" LEFT JOIN ").append(IP).append(" ON ").append(IP_SERVICE.IP_ID).append(" = ").append(IP.IP_ID);
		sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
		sb.append(" WHERE 1=1");
        if (ip_state != null) {
            sb.append(" AND ").append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(ip_state.getValue());
        }
        if (ip_type != null) {
        	sb.append(" AND ").append(IP.IP_TYPE).append(" = ").append(ip_type.getValue());
        }
        if (user_id != 0) {
            sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
        }
        if (account_id != 0) {
            sb.append(" AND ").append(ACCOUNT.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (dateBegin != null || dateEnd != null) {
            sb.append(" AND (");
            appendBoundedDate(sb, IP.IP_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, IP.IP_MODIFIEDTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, IP_SERVICE.IP_SERVICE_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, IP_SERVICE.IP_SERVICE_MODIFIEDTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, IP_SERVICE.IP_SERVICE_STARTTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, IP_SERVICE.IP_SERVICE_ENDTIME, dateBegin, dateEnd);
            sb.append(")");
        }
        if (sort != null) {
            sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
        }
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupIPCountsGroupByState(IPType ip_type, int account_id, int user_id) throws Exception {
        DBTableUser USER = DBTable.USER;
        DBTableIP IP = DBTable.IP;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP_SERVICE.IP_SERVICE_STATE).append(", count(").append(IP_SERVICE.IP_SERVICE_STATE).append(") FROM ").append(IP_SERVICE);
        if (ip_type != null) {
        	sb.append(" LEFT JOIN ").append(IP).append(" ON ").append(IP_SERVICE.IP_ID).append(" = ").append(IP.IP_ID);
        }
        if (account_id >= 0 || user_id >= 0) {
            sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
        }
        sb.append(" WHERE 1=1");
        if (ip_type != null) {
        	sb.append(" AND ").append(IP.IP_TYPE).append(" = ").append(ip_type.getValue());
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
        DBTableIP IP = DBTable.IP;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(IP).append(" (");
        sb.append(IP.IP_ADDR).append(", ");
        sb.append(IP.IP_DESC).append(", ");
        sb.append(IP.IP_TYPE).append(", ");
        sb.append(IP.IP_CREATIONTIME);
        sb.append(") VALUES (");
        sb.appendString(ip_addr).append(", ");
        sb.appendString(ip_desc).append(", ");
        sb.append(ip_type.getValue()).append(", ");
        sb.appendDate(new Date()).append(")");
        doUpdate(sb.toString());
    }
    
    public void createIPService(String is_desc, Date is_starttime, Date is_endtime, int ip_id, String account_name, String user_name) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(IP_SERVICE).append(" (");
        sb.append(IP_SERVICE.IP_SERVICE_DESC).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_STARTTIME).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_ENDTIME).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_STATE).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_CREATIONTIME).append(", ");
        sb.append(IP_SERVICE.USER_ID).append(", ");
        sb.append(IP_SERVICE.IP_ID);
        sb.append(") VALUES (");
        sb.appendString(is_desc).append(", ");
        sb.appendDate(is_starttime).append(", ");
        sb.appendDate(is_endtime).append(", ");
        sb.append(IPState.STOP.getValue()).append(", ");
        sb.appendDate(new Date()).append(", ");
        sb.append("(SELECT ").append(USER.USER_ID).append(" FROM ").append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE ");
        sb.append(USER.USER_NAME).append(" = ").appendString(user_name).append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name).append(")").append(", ");
        sb.append(ip_id).append(")");
        doUpdate(sb.toString());
    }
    
    public void deleteIP(List<Integer> ip_ids) throws Exception {
        DBTableIP IP = DBTable.IP;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int ip_id : ip_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(ip_id);
        }
        
        sb.append("DELETE FROM ").append(IP).append(" WHERE ").append(IP.IP_ID).append(" IN (").append(ids.toString()).append(")");
        doUpdate(sb.toString());
    }
    
    public void deleteIPService(List<Integer> is_ids) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int is_id : is_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(is_id);
        }
        
        sb.append("DELETE FROM ").append(IP_SERVICE).append(" WHERE ").append(IP_SERVICE.IP_ID).append(" IN (").append(ids.toString()).append(")");
        doUpdate(sb.toString());
    }
    
    public void modifyIP(int ip_id, String ip_desc, IPType ip_type) throws Exception {
        DBTableIP IP = DBTable.IP;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(IP).append(" SET ");
        sb.append(IP.IP_DESC).append(" = ").appendString(ip_desc).append(", ");
        if (ip_type != null) {
        	sb.append(IP.IP_TYPE).append(" = ").append(ip_type.getValue()).append(", ");
        }
        sb.append(IP.IP_MODIFIEDTIME).append(" = ").appendDate(new Date());
        sb.append(" WHERE ").append(IP.IP_ID).append(" = ").append(ip_id);
        doUpdate(sb.toString());
    }
    
    public void modifyIPService(int is_id, String is_desc, Date is_starttime, Date is_endtime) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(IP_SERVICE).append(" SET ");
        sb.append(IP_SERVICE.IP_SERVICE_DESC).append(" = ").appendString(is_desc).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_STARTTIME).append(" = ").appendDate(is_starttime).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_ENDTIME).append(" = ").appendDate(is_endtime).append(", ");
        sb.append(IP_SERVICE.IP_SERVICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
        sb.append(" WHERE ").append(IP_SERVICE.IP_SERVICE_ID).append(" = ").append(is_id);
        doUpdate(sb.toString());
    }
    
    public void updateIPServiceState(int is_id, IPState ip_state) throws Exception {
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(IP_SERVICE).append(" SET ");
        sb.append(IP_SERVICE.IP_SERVICE_STATE).append(" = ").append(ip_state.getValue());
        sb.append(" WHERE ").append(IP_SERVICE.IP_SERVICE_ID).append(" = ").append(is_id);
        doUpdate(sb.toString());
    }
    
    public ResultSetWrapper lookupUnusedIPAddrByIPType(IPType ip_type) throws Exception {
        DBTableIP IP = DBTable.IP;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(IP.IP_ADDR).append(" FROM ").append(IP).append(" LEFT JOIN ").append(IP_SERVICE);
        sb.append(" ON ").append(IP.IP_ID).append(" = ").append(IP_SERVICE.IP_ID).append(" WHERE ").append(IP_SERVICE.IP_ID).append(" IS NULL ");
        if (ip_type != null) {
        	sb.append(" AND ").append(IP.IP_TYPE).append(" = ").append(ip_type.getValue());
        }
        sb.append(" ORDER BY ").append(IP.IP_TYPE).append(", ").append(IP.IP_ADDR);
        return doQuery(sb.toString());
    }
    
}
