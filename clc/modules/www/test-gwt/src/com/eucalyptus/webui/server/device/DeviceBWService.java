package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
import com.eucalyptus.webui.shared.resource.device.BWServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceBWService {
	
	private DeviceBWDBProcWrapper dbproc = new DeviceBWDBProcWrapper();
	
    private DeviceBWService() {
    }
    
    private static DeviceBWService instance = new DeviceBWService();
    
    public static DeviceBWService getInstance() {
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
            new SearchResultFieldDesc("4%", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "IP地址"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "类型"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "12%", new ClientMessage("", "带宽上限(KB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "账户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "用户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "开始时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "结束时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "剩余(天)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "带宽(KB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.BW.IP_ADDR: return DBTable.IP.IP_ADDR;
        case CellTableColumns.BW.IP_TYPE: return DBTable.IP.IP_TYPE;
        case CellTableColumns.BW.BW_SERVICE_BW_MAX: return DBTable.BW_SERVICE.BW_SERVICE_BW_MAX;
        case CellTableColumns.BW.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.BW.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.BW.BW_SERVICE_STARTTIME: return DBTable.BW_SERVICE.BW_SERVICE_STARTTIME;
        case CellTableColumns.BW.BW_SERVICE_ENDTIME: return DBTable.BW_SERVICE.BW_SERVICE_ENDTIME;
        case CellTableColumns.BW.BW_SERVICE_BW: return DBTable.BW_SERVICE.BW_SERVICE_BW;
        case CellTableColumns.BW.BW_SERVICE_CREATIONTIME: return DBTable.BW_SERVICE.BW_SERVICE_CREATIONTIME;
        case CellTableColumns.BW.BW_SERVICE_MODIFIEDTIME: return DBTable.BW_SERVICE.BW_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    private int getLife(Date starttime, Date endtime) {
    	final long div = 1000L * 24 * 3600;
    	long start = starttime.getTime() / div, end = endtime.getTime() / div;
    	return start <= end ? (int)(start - end) + 1 : 0;
    }
	
    public synchronized SearchResult lookupBWServiceByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupBWServiceByDate(dateBegin, dateEnd, getSortColumn(range), range.isAscending(), -1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupBWServiceByDate(dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupBWServiceByDate(dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableIP IP = DBTable.IP;
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            Date today = new Date();
            for (int index = 1; rs.next(); index ++) {
            	int bs_id = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_ID);
            	int ip_id = DBData.getInt(rs, IP.IP_ID);
                String ip_addr = DBData.getString(rs, IP.IP_ADDR);
                IPType ip_type = IPType.getIPType(DBData.getInt(rs, IP.IP_TYPE));
                String bs_desc = DBData.getString(rs, BW_SERVICE.BW_SERVICE_DESC);
                int bs_bw_max = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW_MAX);
                String account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                String user_name = DBData.getString(rs, USER.USER_NAME);
                Date bs_starttime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_STARTTIME);
                Date bs_endtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_ENDTIME);
                String bs_life = Integer.toString(Math.min(getLife(bs_starttime, bs_endtime), getLife(today, bs_endtime)));
                int bs_bw = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW);
                Date bs_creationtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_CREATIONTIME);
                Date bs_modifiedtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_MODIFIEDTIME);
                List<String> list = Arrays.asList(Integer.toString(bs_id), Integer.toString(ip_id), "", Integer.toString(index ++),
                		ip_addr, ip_type.toString(), bs_desc, Integer.toString(bs_bw_max), account_name, user_name,
                        DBData.format(bs_starttime), DBData.format(bs_endtime), bs_life, Integer.toString(bs_bw),
                        DBData.format(bs_creationtime), DBData.format(bs_modifiedtime));
                rows.add(new SearchResultRow(list));
            }
            final int col_life = CellTableColumns.BW.BW_SERVICE_LIFE;
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
            throw new EucalyptusServiceException(new ClientMessage("", "获取带宽服务列表失败"));
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
    
    public synchronized void addBWService(Session session, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, String ip_addr) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (bs_starttime == null || bs_endtime == null) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (getLife(bs_starttime, bs_endtime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
        }
        if (isEmpty(ip_addr)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的IP地址"));
        }
        if (bs_desc == null) {
            bs_desc = "";
        }
        try {
        	dbproc.createBWService(bs_desc, bs_bw_max, bs_starttime, bs_endtime, ip_addr);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "创建带宽服务失败"));
        }
    }
    
    public synchronized void deleteBWService(Session session, List<Integer> bs_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!bs_ids.isEmpty()) {
                dbproc.deleteBWService(bs_ids);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "删除带宽服务失败"));
        }
    }
    
    public synchronized void modifyBWService(Session session, int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (bs_bw_max < 0) {
        	throw new EucalyptusServiceException(new ClientMessage("", "无效的带宽上限"));
        }
        if (bs_starttime == null || bs_endtime == null) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (getLife(bs_starttime, bs_endtime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
        }
        if (bs_desc == null) {
        	bs_desc = "";
        }
        try {
        	dbproc.modifyBWService(bs_id, bs_desc, bs_bw_max, bs_starttime, bs_endtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "修改带宽服务失败"));
        }
    }
    
    public synchronized void updateBWServiceBandwidth(int bs_id, int bs_bw) throws EucalyptusServiceException {
        if (bs_bw < 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的带宽"));
        }
        try {
        	dbproc.updateBWServiceBandwidth(bs_id, bs_bw);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "更新带宽服务状态失败"));
        }
    }
    
    public synchronized List<String> lookupUnusedIPAddr(Session session, String account_name, String user_name) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (isEmpty(account_name)) {
        	user_name = account_name = null;
        }
        else if (isEmpty(user_name)) {
        	user_name = null;
        }
        ResultSetWrapper rsw = null;
        try {
        	rsw = dbproc.lookupUnusedIPAddr(account_name, user_name);
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
    
    public synchronized BWServiceInfo lookupBWServiceInfoByID(int bs_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
        	rsw = dbproc.lookupBWServiceByID(bs_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            String bs_desc = DBData.getString(rs, BW_SERVICE.BW_SERVICE_DESC);
            Date bs_starttime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_STARTTIME);
            Date bs_endtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_ENDTIME);
            int bs_bw = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW);
            int bs_bw_max = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW_MAX);;
            Date bs_creationtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_CREATIONTIME);
            Date bs_modifiedtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_MODIFIEDTIME);
            int ip_id = DBData.getInt(rs, BW_SERVICE.IP_ID);
            return new BWServiceInfo(bs_id, bs_desc, bs_starttime, bs_endtime, bs_bw, bs_bw_max, bs_creationtime, bs_modifiedtime, ip_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取带宽服务信息失败"));
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

class DeviceBWDBProcWrapper {
	
    private static final Logger LOG = Logger.getLogger(DeviceBWDBProcWrapper.class.getName());

    private DBProcWrapper wrapper = DBProcWrapper.Instance();

    private ResultSetWrapper doQuery(String request) throws Exception {
        LOG.info(request);
        return wrapper.query(request);
    }

    private void doUpdate(String request) throws Exception {
        LOG.info(request);
        wrapper.update(request);
    }

    public ResultSetWrapper lookupBWServiceByID(int bs_id) throws Exception {
    	DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
    	DBStringBuilder sb = new DBStringBuilder();
    	sb.append("SELECT * FROM ").append(BW_SERVICE).append(" WHERE ").append(BW_SERVICE.BW_SERVICE_ID).append(" = ").append(bs_id);
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
    
    public ResultSetWrapper lookupBWServiceByDate(Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableIP IP = DBTable.IP;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(BW_SERVICE.ANY).append(", ").append(IP.IP_ADDR).append(", ").append(IP.IP_TYPE).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(" FROM ");
		sb.append(BW_SERVICE).append(" LEFT JOIN ").append(IP).append(" ON ").append(BW_SERVICE.IP_ID).append(" = ").append(IP.IP_ID);
		sb.append(" LEFT JOIN ").append(IP_SERVICE).append(" ON ").append(BW_SERVICE.IP_ID).append(IP_SERVICE.IP_ID);
		sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
		sb.append(" WHERE 1=1");
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
    
    public void createBWService(String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, String ip_addr) throws Exception {
        DBTableIP IP = DBTable.IP;
        DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(BW_SERVICE).append(" (");
        sb.append(BW_SERVICE.BW_SERVICE_DESC).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_STARTTIME).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_ENDTIME).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_BW_MAX).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_CREATIONTIME).append(", ");
        sb.append(BW_SERVICE.IP_ID);
        sb.append(") VALUES (");
        sb.appendString(bs_desc).append(", ");
        sb.appendDate(bs_starttime).append(", ");
        sb.appendDate(bs_endtime).append(", ");
        sb.append(bs_bw_max).append(", ");
        sb.appendDate(new Date()).append(", ");
        sb.append("(SELECT ").append(IP.IP_ID).append(" FROM ").append(IP).append(" WHERE ").append(IP.IP_ADDR).append(" = ").appendString(ip_addr).append(")").append(")");
        doUpdate(sb.toString());
    }
    
    public void deleteBWService(List<Integer> bs_ids) throws Exception {
    	DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int bs_id : bs_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(bs_id);
        }
        
        sb.append("DELETE FROM ").append(BW_SERVICE).append(" WHERE ").append(BW_SERVICE.BW_SERVICE_ID).append(" IN (").append(ids.toString()).append(")");
        doUpdate(sb.toString());
    }
    
    public void modifyBWService(int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) throws Exception {
    	DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(BW_SERVICE).append(" SET ");
        sb.append(BW_SERVICE.BW_SERVICE_DESC).append(" = ").appendString(bs_desc).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_BW_MAX).append(" = ").append(bs_bw_max).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_STARTTIME).append(" = ").appendDate(bs_starttime).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_ENDTIME).append(" = ").appendDate(bs_endtime).append(", ");
        sb.append(BW_SERVICE.BW_SERVICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
        sb.append(" WHERE ").append(BW_SERVICE.BW_SERVICE_ID).append(" = ").append(bs_id);
        doUpdate(sb.toString());
    }
    
    public void updateBWServiceBandwidth(int bs_id, int bs_bw) throws Exception {
    	DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(BW_SERVICE).append(" SET ");
        sb.append(BW_SERVICE.BW_SERVICE_BW).append(" = ").append(bs_bw);
        sb.append(" WHERE ").append(BW_SERVICE.BW_SERVICE_ID).append(" = ").append(bs_id);
        doUpdate(sb.toString());
    }
    
    public ResultSetWrapper lookupUnusedIPAddr(String account_name, String user_name) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
    	DBTableIP IP = DBTable.IP;
    	DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
    	DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
    	DBStringBuilder sb = new DBStringBuilder();
    	sb.append("SELECT ").append(IP.IP_ADDR).append(" FROM ").append(" IP ");
    	if (account_name != null) {
        	sb.append(" LEFT JOIN ").append(IP_SERVICE).append(" ON ").append(IP.IP_ID).append(" = ").append(IP_SERVICE.IP_ID);
    		sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
    		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
    	}
    	sb.append(" WHERE ").append(IP.IP_ID).append(" NOT IN ");
    	sb.append("(SELECT").append(BW_SERVICE.IP_ID).append(" FROM ").append(BW_SERVICE).append(")");
    	if (account_name != null) {
    		sb.append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name);
    		if (user_name != null) {
    			sb.append(" AND ").append(USER.USER_NAME).append(" = ").appendString(user_name);
    		}
    	}
    	sb.append(" ORDER BY ").append(IP.IP_TYPE).append(", ").append(IP.IP_ADDR);
        return doQuery(sb.toString());
    }
    
}
