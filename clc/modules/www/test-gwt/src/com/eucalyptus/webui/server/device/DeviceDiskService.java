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
import com.eucalyptus.webui.shared.resource.device.DiskInfo;
import com.eucalyptus.webui.shared.resource.device.DiskServiceInfo;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceDiskService {
    
    private DeviceDiskDBProcWrapper dbproc = new DeviceDiskDBProcWrapper();
    
    private DeviceDiskService() {
    }
    
    private static DeviceDiskService instance = new DeviceDiskService();
    
    public static DeviceDiskService getInstance() {
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "名称"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "服务器"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "总大小(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "设备创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "设备修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "账户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "用户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "大小(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "开始时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "结束时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "剩余时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "状态"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "服务创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "服务修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private final int SERVICE_LIFE_COLUMN = 15;
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case 4: return DBTable.DISK.DISK_NAME;
        case 5: return DBTable.SERVER.SERVER_NAME;
        case 6: return DBTable.DISK.DISK_TOTAL;
        case 7: return DBTable.DISK.DISK_CREATIONTIME;
        case 8: return DBTable.DISK.DISK_MODIFIEDTIME;
        case 9: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case 10: return DBTable.USER.USER_NAME;
        case 11: return DBTable.DISK_SERVICE.DISK_SERVICE_DESC;
        case 12: return DBTable.DISK_SERVICE.DISK_SERVICE_USED;
        case 13: return DBTable.DISK_SERVICE.DISK_SERVICE_STARTTIME;
        case 14: return DBTable.DISK_SERVICE.DISK_SERVICE_ENDTIME;
        case 16: return DBTable.DISK_SERVICE.DISK_SERVICE_STATE;
        case 17: return DBTable.DISK_SERVICE.DISK_SERVICE_CREATIONTIME;
        case 18: return DBTable.DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    private int getLife(Date starttime, Date endtime) {
        return (int)((endtime.getTime() - starttime.getTime()) / (1000L *24 *3600));
    }
    
    private static final long DISK_UNIT = 1024 * 1024;
    
    public synchronized SearchResult lookupDiskByDate(Session session, SearchRange range, DiskState disk_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupDiskByDate(disk_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), -1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupDiskByDate(disk_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupDiskByDate(disk_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableServer SERVER = DBTable.SERVER;
            DBTableDisk DISK = DBTable.DISK;
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            for (int index = 1; rs.next(); index ++) {
                int disk_id = DBData.getInt(rs, DISK.DISK_ID);
                int ds_id = DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_ID);
                String disk_name = DBData.getString(rs, DISK.DISK_NAME);
                String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
                long disk_total = DBData.getLong(rs, DISK.DISK_TOTAL) / DISK_UNIT;
                Date disk_creationtime = DBData.getDate(rs, DISK.DISK_CREATIONTIME);
                Date disk_modifiedtime = DBData.getDate(rs, DISK.DISK_MODIFIEDTIME);
                disk_state = DiskState.getDiskState(DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_STATE));
                String account_name = null;
                String user_name = null;
                String ds_desc = null;
                String ds_used = null;
                Date ds_starttime = null;
                Date ds_endtime = null;
                String ds_life = null;
                Date ds_creationtime = null;
                Date ds_modifiedtime = null;
                if (disk_state != DiskState.RESERVED) {
                    account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
                    user_name = DBData.getString(rs, USER.USER_NAME);
                    ds_desc = DBData.getString(rs, DISK_SERVICE.DISK_SERVICE_DESC);
                    ds_used = Long.toString(DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED) / DISK_UNIT);
                    ds_starttime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_STARTTIME);
                    ds_endtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_ENDTIME);
                    ds_life = Integer.toString(getLife(ds_starttime, ds_endtime));
                    ds_creationtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_CREATIONTIME);
                    ds_modifiedtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME);
                }
                List<String> list = Arrays.asList(Integer.toString(disk_id), Integer.toString(ds_id), "", Integer.toString(index ++),
                        disk_name, server_name, Long.toString(disk_total),
                        DBData.format(disk_creationtime), DBData.format(disk_modifiedtime), account_name, user_name, ds_desc, ds_used,
                        DBData.format(ds_starttime), DBData.format(ds_endtime), ds_life, disk_state.toString(),
                        DBData.format(ds_creationtime), DBData.format(ds_modifiedtime));
                rows.add(new SearchResultRow(list));
            }
            if (range.getSortField() == SERVICE_LIFE_COLUMN) {
                final boolean isAscending = range.isAscending();
                Collections.sort(rows, new Comparator<SearchResultRow>() {

                    @Override
                    public int compare(SearchResultRow arg0, SearchResultRow arg1) {
                        String life0 = arg0.getField(SERVICE_LIFE_COLUMN), life1 = arg1.getField(SERVICE_LIFE_COLUMN);
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
            throw new EucalyptusServiceException(new ClientMessage("", "获取硬盘列表失败"));
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
    
    public synchronized Map<Integer, Integer> lookupDiskCountsGroupByState(Session session, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupDiskCountsGroupByState(-1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupDiskCountsGroupByState(user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupDiskCountsGroupByState(user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            long sum = 0;
            while (rs.next()) {
                int disk_state = rs.getInt(1);
                long disk_total = rs.getLong(2);
                sum += disk_total;
                map.put(disk_state, (int)(disk_total / DISK_UNIT));
            }
            map.put(-1, (int)(sum / DISK_UNIT));
            return map;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取硬盘计数失败"));
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
    
    public synchronized void addDisk(Session session, String disk_name, String disk_desc, int disk_size, String server_name) throws Exception { 
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (isEmpty(disk_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的硬盘名称"));
        }
        if (isEmpty(server_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
        }
        if (disk_desc == null) {
            disk_desc = "";
        }
        long disk_total = disk_size * DISK_UNIT;
        try {
            dbproc.createDisk(disk_name, disk_desc, disk_total, server_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "创建硬盘失败"));
        }
    }
    
    public synchronized void addDiskService(Session session, String ds_desc, int ds_size, Date ds_starttime, Date ds_endtime, 
            DiskState disk_state, int disk_id, String account_name, String user_name) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (ds_starttime == null || ds_endtime == null) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
        }
        if (getLife(ds_starttime, ds_endtime) <= 0) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
        }
        if (disk_state != DiskState.INUSE && disk_state != DiskState.STOP) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
        }
        if (isEmpty(account_name) || isEmpty(user_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的账户名称"));
        }
        if (ds_desc == null) {
            ds_desc = "";
        }
        long disk_used = ds_size * DISK_UNIT;
        try {
            dbproc.createDiskService(ds_desc, disk_used, ds_starttime, ds_endtime, disk_state, disk_id, account_name, user_name);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "创建硬盘服务失败"));
        }
    }
    
    public synchronized void deleteDisk(Session session, List<Integer> disk_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!disk_ids.isEmpty()) {
                dbproc.deleteDisk(disk_ids);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "删除硬盘失败"));
        }
    }
    
    
    public synchronized void deleteDiskService(Session session, List<Integer> ds_ids) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        try {
            if (!ds_ids.isEmpty()) {
                dbproc.deleteDiskService(ds_ids);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "删除硬盘服务失败"));
        }
    }
    
    public synchronized void modifyDisk(Session session, int disk_id, String disk_desc) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (disk_desc == null) {
            disk_desc = "";
        }
        try {
            dbproc.modifyDisk(disk_id, disk_desc);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "修改硬盘失败"));
        }
    }
    
    public synchronized void updateDiskServiceState(int ds_id, DiskState disk_state) throws EucalyptusServiceException {
        if (disk_state != DiskState.INUSE && disk_state != DiskState.STOP) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
        }
        try {
            dbproc.updateDiskServiceState(ds_id, disk_state);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "更新硬盘服务状态失败"));
        }
    }
    
    public synchronized List<String> lookupDiskNamesByServerName(Session session, String server_name) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        if (isEmpty(server_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
        }
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupDiskNamesByServerName(server_name);
            DBTableDisk DISK = DBTable.DISK;
            ResultSet rs = rsw.getResultSet();
            List<String> disk_name_list = new LinkedList<String>();
            while (rs.next()) {
                String disk_name = DBData.getString(rs, DISK.DISK_NAME);
                disk_name_list.add(disk_name);
            }
            return disk_name_list;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取硬盘列表失败"));
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
    
    public synchronized DiskInfo lookupDiskInfoByID(int disk_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupDiskByID(disk_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableDisk DISK = DBTable.DISK;
            String disk_name = DBData.getString(rs, DISK.DISK_NAME);
            String disk_desc = DBData.getString(rs, DISK.DISK_DESC);
            long disk_total = DBData.getLong(rs, DISK.DISK_TOTAL);
            Date disk_creationtime = DBData.getDate(rs, DISK.DISK_CREATIONTIME);
            Date disk_modifiedtime = DBData.getDate(rs, DISK.DISK_MODIFIEDTIME);
            int server_id = DBData.getInt(rs, DISK.SERVER_ID);
            return new DiskInfo(disk_id, disk_name, disk_desc, disk_total, disk_creationtime, disk_modifiedtime, server_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取硬盘信息失败"));
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
    
    public synchronized DiskServiceInfo lookupDiskServiceInfoByID(int ds_id) throws EucalyptusServiceException {
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupDiskServiceByID(ds_id);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
            String ds_desc = DBData.getString(rs, DISK_SERVICE.DISK_SERVICE_DESC);
            long ds_used = DBData.getLong(rs, DISK_SERVICE.DISK_SERVICE_USED);
            Date ds_starttime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_STARTTIME);
            Date ds_endtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_ENDTIME);
            DiskState disk_state = DiskState.getDiskState(DBData.getInt(rs, DISK_SERVICE.DISK_SERVICE_STATE));
            Date ds_creationtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_CREATIONTIME);
            Date ds_modifiedtime = DBData.getDate(rs, DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME);
            int disk_id = DBData.getInt(rs, DISK_SERVICE.DISK_ID);
            int user_id = DBData.getInt(rs, DISK_SERVICE.USER_ID);
            return new DiskServiceInfo(ds_id, ds_desc, ds_used, ds_starttime, ds_endtime, disk_state, ds_creationtime, ds_modifiedtime, disk_id, user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取硬盘服务信息失败"));
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

class DeviceDiskDBProcWrapper {
    
    private static final Logger LOG = Logger.getLogger(DeviceDiskDBProcWrapper.class.getName());

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
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("DELETE FROM ").append(DISK_SERVICE).append(" WHERE ").append(DISK_SERVICE.DISK_SERVICE_USED).append(" = ").append(0);
        sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(" != ").append(DiskState.RESERVED.getValue());
        doUpdate(sb.toString());
    }
    
    public ResultSetWrapper lookupDiskByID(int disk_id) throws Exception {
        DBTableDisk DISK = DBTable.DISK;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(DISK).append(" WHERE ").append(DISK.DISK_ID).append(" = ").append(disk_id);
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupDiskServiceByID(int ds_id) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(DISK_SERVICE).append(" WHERE ").append(DISK_SERVICE.DISK_SERVICE_ID).append(" = ").append(ds_id);
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
    
    public ResultSetWrapper lookupDiskByDate(DiskState disk_state, Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableServer SERVER = DBTable.SERVER;
        DBTableDisk DISK = DBTable.DISK;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBTableAlias A = DBTable.getDBTableAlias("A");
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(DISK.ANY).append(", ").append(DISK_SERVICE.ANY).append(", ").append(SERVER.SERVER_NAME).append(", ").append(A).append(" FROM ");
        sb.append("(SELECT ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(" FROM ");
        sb.append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE 1=1");
        if (account_id != 0) {
            sb.append(" AND ").append(ACCOUNT.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (user_id != 0) {
            sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
        }
        sb.append(") as ").append(A).append(" RIGHT JOIN ").append(DISK_SERVICE);
        sb.append(" ON ").append(DISK_SERVICE.USER_ID).append(" = ").append(A.getColumn(USER.USER_ID));
        sb.append(" LEFT JOIN ").append(DISK).append(" ON ").append(DISK_SERVICE.DISK_ID).append(" = ").append(DISK.DISK_ID);
        sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(DISK.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
        sb.append(" WHERE 1=1");
        if (disk_state != null) {
            sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(" = ").append(disk_state.getValue());
        }
        if (dateBegin != null || dateEnd != null) {
            sb.append(" AND (");
            appendBoundedDate(sb, DISK.DISK_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, DISK.DISK_MODIFIEDTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, DISK_SERVICE.DISK_SERVICE_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, DISK_SERVICE.DISK_SERVICE_STARTTIME, dateBegin, dateEnd).append(" OR ");
            appendBoundedDate(sb, DISK_SERVICE.DISK_SERVICE_ENDTIME, dateBegin, dateEnd);
            sb.append(")");
        }
        if (sort != null) {
            sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
        }
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupDiskCountsGroupByState(int account_id, int user_id) throws Exception {
        DBTableUser USER = DBTable.USER;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(", sum(").append(DISK_SERVICE.DISK_SERVICE_USED).append(") FROM ").append(DISK_SERVICE);
        if (account_id >= 0 || user_id >= 0) {
            sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(DISK_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
        }
        sb.append(" WHERE 1=1");
        if (account_id >= 0) {
            sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
        }
        if (user_id >= 0) {
            sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
        }
        sb.append(" GROUP BY ").append(DISK_SERVICE.DISK_SERVICE_STATE);
        return doQuery(sb.toString());
    }
    
    public void createDisk(String disk_name, String disk_desc, long disk_total, String server_name) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBTableDisk DISK = DBTable.DISK;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(DISK).append(" (");
        sb.append(DISK.DISK_NAME).append(", ");
        sb.append(DISK.DISK_DESC).append(", ");
        sb.append(DISK.DISK_TOTAL).append(", ");
        sb.append(DISK.SERVER_ID).append(", ");
        sb.append(DISK.DISK_CREATIONTIME);
        sb.append(") VALUES (");
        sb.appendString(disk_name).append(", ");
        sb.appendString(disk_desc).append(", ");
        sb.append(disk_total).append(", ");
        sb.append("(SELECT ").append(SERVER.SERVER_ID).append(" FROM ").append(SERVER).append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name).append("), ");
        sb.appendDate(new Date()).append(")");
        doUpdate(sb.toString());
        
        sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(DISK_SERVICE).append(" (");
        sb.append(DISK_SERVICE.DISK_SERVICE_STATE).append(", ");
        sb.append(DISK_SERVICE.DISK_ID).append(", ");
        sb.append(DISK_SERVICE.USER_ID);
        sb.append(") VALUES (");
        sb.append(DiskState.RESERVED.getValue()).append(", ");
        sb.append("(SELECT MAX(").append(DISK.DISK_ID).append(") FROM ").append(DISK).append(" WHERE 1=1)").append(", -1").append(")");
        doUpdate(sb.toString());
    }
    
    public void createDiskService(String ds_desc, long ds_used, Date ds_starttime, Date ds_endtime, DiskState disk_state, int disk_id, String account_name, String user_name) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        sb.append("INSERT INTO ").append(DISK_SERVICE).append(" (");
        sb.append(DISK_SERVICE.DISK_SERVICE_DESC).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_USED).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_STARTTIME).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_ENDTIME).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_STATE).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_CREATIONTIME).append(", ");
        sb.append(DISK_SERVICE.USER_ID).append(", ");
        sb.append(DISK_SERVICE.DISK_ID);
        sb.append(") VALUES (");
        sb.appendString(ds_desc).append(", ");
        sb.append(ds_used).append(", ");
        sb.appendDate(ds_starttime).append(", ");
        sb.appendDate(ds_endtime).append(", ");
        sb.append(disk_state.getValue()).append(", ");
        sb.appendDate(new Date()).append(", ");
        sb.append("(SELECT ").append(USER.USER_ID).append(" FROM ").append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE ");
        sb.append(USER.USER_NAME).append(" = ").appendString(user_name).append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name).append(")").append(", ");
        sb.append(disk_id).append(")");
        doUpdate(sb.toString());
        
        DBTableAlias A = DBTable.getDBTableAlias("A");
        DBTableAlias B = DBTable.getDBTableAlias("B");
        DBTableColumn A_USED = A.getColumn(DISK_SERVICE.DISK_SERVICE_USED);
        DBTableColumn B_USED = B.getColumn(DISK_SERVICE.DISK_SERVICE_USED);
        sb = new DBStringBuilder();
        sb.append("UPDATE ").append(DISK_SERVICE).append(" ").append(A).append(", ").append(DISK_SERVICE).append(" ").append(B).append(" SET ");
        sb.append(A_USED).append(" = ").append(ds_used).append(", ");
        sb.append(B_USED).append(" = ").append("(").append(B_USED).append(" - ").append(ds_used).append(")");
        sb.append(" WHERE ").append(A.getColumn(DISK_SERVICE.DISK_SERVICE_ID)).append(" = ");
        sb.append("(SELECT MAX(").append(DISK_SERVICE.DISK_SERVICE_ID).append(") FROM ").append(DISK_SERVICE).append(")");
        sb.append(" AND ").append(A.getColumn(DISK_SERVICE.DISK_ID)).append(" = ").append(B.getColumn(DISK_SERVICE.DISK_ID));
        sb.append(" AND ").append(B.getColumn(DISK_SERVICE.DISK_SERVICE_STATE)).append(" = ").append(DiskState.RESERVED.getValue());
        sb.append(" AND ").append(B.getColumn(DISK_SERVICE.DISK_SERVICE_USED)).append(" >= ").append(ds_used);
        doUpdate(sb.toString());
        
        doCleanup();
    }
    
    public void deleteDisk(List<Integer> disk_ids) throws Exception {
        DBTableDisk DISK = DBTable.DISK;
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int disk_id : disk_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(disk_id);
        }
        
        sb.append("DELETE ").append(DISK_SERVICE).append(" FROM ").append(DISK_SERVICE).append(" LEFT JOIN ").append(DISK);
        sb.append(" ON ").append(DISK.DISK_ID).append(" = ").append(DISK_SERVICE.DISK_ID);
        sb.append(" WHERE ").append(DISK_SERVICE.DISK_ID).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_STATE).append(" = ").append(DiskState.RESERVED.getValue());
        sb.append(" AND ").append(DISK_SERVICE.DISK_SERVICE_USED).append(" = ").append(DISK.DISK_TOTAL);
        doUpdate(sb.toString());
        
        sb = new DBStringBuilder();
        sb.append("DELETE FROM ").append(DISK).append(" WHERE ").append(DISK.DISK_ID).append(" IN (").append(ids.toString()).append(")");
        doUpdate(sb.toString());
        
        doCleanup();
    }
    
    public void deleteDiskService(List<Integer> ds_ids) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        
        StringBuilder ids = new StringBuilder();
        int total = 0;
        for (int ds_id : ds_ids) {
            if (total ++ != 0) {
                ids.append(", ");
            }
            ids.append(ds_id);
        }
        
        DBTableAlias A = DBTable.getDBTableAlias("A");
        DBTableAlias B = DBTable.getDBTableAlias("B");
        DBTableColumn A_USED = A.getColumn(DISK_SERVICE.DISK_SERVICE_USED);
        DBTableColumn B_USED = B.getColumn(DISK_SERVICE.DISK_SERVICE_USED);
        sb.append("UPDATE ").append(DISK_SERVICE).append(" ").append(A).append(", ").append(DISK_SERVICE).append(" ").append(B).append(" SET ");
        sb.append(A_USED).append(" = ").append(0).append(", ");
        sb.append(B_USED).append(" = ").append("(").append(B_USED).append(" + ").append(A_USED).append(")");
        sb.append(" WHERE ").append(A.getColumn(DISK_SERVICE.DISK_SERVICE_ID)).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(A.getColumn(DISK_SERVICE.DISK_ID)).append(" = ").append(B.getColumn(DISK_SERVICE.DISK_ID));
        sb.append(" AND ").append(B.getColumn(DISK_SERVICE.DISK_SERVICE_STATE)).append(" = ").append(DiskState.RESERVED.getValue());
        doUpdate(sb.toString());
        
        doCleanup();
    }
    
    public void modifyDisk(int disk_id, String disk_desc) throws Exception {
        DBTableDisk DISK = DBTable.DISK;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(DISK).append(" SET ");
        sb.append(DISK.DISK_DESC).append(" = ").appendString(disk_desc).append(", ");
        sb.append(DISK.DISK_MODIFIEDTIME).append(" = ").appendDate(new Date()).append(", ");
        sb.append(" WHERE ").append(DISK.DISK_ID).append(" = ").append(disk_id);
        doUpdate(sb.toString());
    }
    
    public void modifyDiskService(int ds_id, String ds_desc, Date ds_starttime, Date ds_endtime, DiskState disk_state) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(DISK_SERVICE).append(" SET ");
        sb.append(DISK_SERVICE.DISK_SERVICE_DESC).append(" = ").appendString(ds_desc).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_STARTTIME).append(" = ").appendDate(ds_starttime).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_ENDTIME).append(" = ").appendDate(ds_endtime).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_STATE).append(" = ").append(disk_state.getValue()).append(", ");
        sb.append(DISK_SERVICE.DISK_SERVICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
        sb.append(" WHERE ").append(DISK_SERVICE.DISK_SERVICE_ID).append(" = ").append(ds_id);
        doUpdate(sb.toString());
    }
    
    public void updateDiskServiceState(int ds_id, DiskState disk_state) throws Exception {
        DBTableDiskService DISK_SERVICE = DBTable.DISK_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("UPDATE ").append(DISK_SERVICE).append(" SET ");
        sb.append(DISK_SERVICE.DISK_SERVICE_STATE).append(" = ").append(disk_state.getValue());
        sb.append(" WHERE ").append(DISK_SERVICE.DISK_SERVICE_ID).append(" = ").append(ds_id);
        doUpdate(sb.toString());
    }
    
    public ResultSetWrapper lookupDiskNamesByServerName(String server_name) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBTableDisk DISK = DBTable.DISK;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT DISTINCT(").append(DISK.DISK_NAME).append(") FROM");
        sb.append(DISK).append(" LEFT JOIN ").append(SERVER).append(" ON ").append(DISK.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
        sb.append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name);
        return doQuery(sb.toString());
    }
    
}
