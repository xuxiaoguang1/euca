package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
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
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "IP地址"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "地址类型"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "带宽上限(KB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
    		new SearchResultFieldDesc(true, "8%", new ClientMessage("", "带宽(KB)"),
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
            		TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.BW.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.BW.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.BW.IP_ADDR: return DBTable.IP_SERVICE.IP_ADDR;
        case CellTableColumns.BW.IP_TYPE: return DBTable.IP_SERVICE.IP_TYPE;
    	case CellTableColumns.BW.BW_SERVICE_BW_MAX: return DBTable.BW_SERVICE.BW_SERVICE_BW_MAX;
    	case CellTableColumns.BW.BW_SERVICE_BW: return DBTable.BW_SERVICE.BW_SERVICE_BW;
    	case CellTableColumns.BW.BW_SERVICE_DESC: return DBTable.BW_SERVICE.BW_SERVICE_DESC;
    	case CellTableColumns.BW.BW_SERVICE_STARTTIME: return DBTable.BW_SERVICE.BW_SERVICE_STARTTIME;
    	case CellTableColumns.BW.BW_SERVICE_ENDTIME: return DBTable.BW_SERVICE.BW_SERVICE_ENDTIME;
    	case CellTableColumns.BW.BW_SERVICE_LIFE: return DBTable.BW_SERVICE.BW_SERVICE_LIFE;
    	case CellTableColumns.BW.BW_SERVICE_CREATIONTIME: return DBTable.BW_SERVICE.BW_SERVICE_CREATIONTIME;
    	case CellTableColumns.BW.BW_SERVICE_MODIFIEDTIME: return DBTable.BW_SERVICE.BW_SERVICE_MODIFIEDTIME;
        }
        return null;
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
            DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
            DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
            	if (start <= index && index < end) {
	            	int bs_id = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_ID);
	                String ip_addr = DBData.getString(rs, IP_SERVICE.IP_ADDR);
	                IPType ip_type = IPType.getIPType(DBData.getInt(rs, IP_SERVICE.IP_TYPE));
	                String bs_desc = DBData.getString(rs, BW_SERVICE.BW_SERVICE_DESC);
	                int bs_bw_max = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW_MAX);
	                String account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
	                String user_name = DBData.getString(rs, USER.USER_NAME);
	                Date bs_starttime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_STARTTIME);
	                Date bs_endtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_ENDTIME);
	                String bs_life = DBData.getString(rs, BW_SERVICE.BW_SERVICE_LIFE);
                    if (bs_life != null) {
                    	bs_life = Integer.toString(Math.max(0, Integer.parseInt(bs_life) + 1));
                    }
	                int bs_bw = DBData.getInt(rs, BW_SERVICE.BW_SERVICE_BW);
	                Date bs_creationtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_CREATIONTIME);
	                Date bs_modifiedtime = DBData.getDate(rs, BW_SERVICE.BW_SERVICE_MODIFIEDTIME);
	                CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.IP.COLUMN_SIZE);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_ID, bs_id);
	            	row.setColumn(CellTableColumns.BW.RESERVED_CHECKBOX, "");
	            	row.setColumn(CellTableColumns.BW.RESERVED_INDEX, index + 1);
	            	row.setColumn(CellTableColumns.BW.IP_ADDR, ip_addr);
	            	row.setColumn(CellTableColumns.BW.IP_TYPE, ip_type.toString());
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_BW_MAX, bs_bw_max);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_BW, bs_bw);
	            	row.setColumn(CellTableColumns.BW.ACCOUNT_NAME, account_name);
	            	row.setColumn(CellTableColumns.BW.USER_NAME, user_name);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_DESC, bs_desc);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_STARTTIME, bs_starttime);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_ENDTIME, bs_endtime);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_LIFE, bs_life);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_CREATIONTIME, bs_creationtime);
	            	row.setColumn(CellTableColumns.BW.BW_SERVICE_MODIFIEDTIME, bs_modifiedtime);
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
        if (bs_starttime == null || bs_endtime == null || DBData.calcLife(bs_endtime, bs_starttime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
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
        if (bs_starttime == null || bs_endtime == null || DBData.calcLife(bs_endtime, bs_starttime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
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
    
    public ResultSetWrapper lookupBWServiceByDate(Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
        DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(BW_SERVICE.ANY).append(", ").append(IP_SERVICE.IP_ADDR).append(", ").append(IP_SERVICE.IP_TYPE).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
        appendServiceLife(sb, BW_SERVICE.BW_SERVICE_STARTTIME, BW_SERVICE.BW_SERVICE_ENDTIME, BW_SERVICE.BW_SERVICE_LIFE).append(" FROM ");
		sb.append(BW_SERVICE).append(" LEFT JOIN ").append(IP_SERVICE).append(" ON ").append(BW_SERVICE.IP_ID).append(" = ").append(IP_SERVICE.IP_ID);
		sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
		sb.append(" WHERE 1=1");
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
    
    public void createBWService(String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, String ip_addr) throws Exception {
    	DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
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
        sb.append("(SELECT ").append(IP_SERVICE.IP_ID).append(" FROM ").append(IP_SERVICE).append(" WHERE ").append(IP_SERVICE.IP_ADDR).append(" = ").appendString(ip_addr).append(")").append(")");
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
        DBTableIPService IP_SERVICE = DBTable.IP_SERVICE;
    	DBTableBWService BW_SERVICE = DBTable.BW_SERVICE;
    	DBStringBuilder sb = new DBStringBuilder();
    	sb.append("SELECT ").append(IP_SERVICE.IP_ADDR).append(" FROM ").append(IP_SERVICE);
    	if (account_name != null) {
    		sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(IP_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
    		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
    	}
    	sb.append(" WHERE ").append(IP_SERVICE.IP_ID).append(" NOT IN ");
    	sb.append("(SELECT ").append(BW_SERVICE.IP_ID).append(" FROM ").append(BW_SERVICE).append(")");
    	if (account_name != null) {
    		sb.append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name);
    		if (user_name != null) {
    			sb.append(" AND ").append(USER.USER_NAME).append(" = ").appendString(user_name);
    		}
    	}
    	sb.append(" ORDER BY ").append(IP_SERVICE.IP_TYPE).append(", ").append(IP_SERVICE.IP_ADDR);
        return doQuery(sb.toString());
    }
    
}
