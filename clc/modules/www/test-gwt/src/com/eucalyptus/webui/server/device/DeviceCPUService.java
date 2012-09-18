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
import com.eucalyptus.webui.shared.resource.device.CPUInfo;
import com.eucalyptus.webui.shared.resource.device.CPUServiceInfo;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceCPUService {
    
    private DeviceCPUDBProcWrapper dbproc = new DeviceCPUDBProcWrapper();
    
    private DeviceCPUService() {
    }
    
    private static DeviceCPUService instance = new DeviceCPUService();
    
    public static DeviceCPUService getInstance() {
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "厂家"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "型号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "主频"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "缓存"),
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
    
    private final int SERVICE_LIFE_COLUMN = 17;
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case 4: return DBTable.CPU.CPU_NAME;
        case 5: return DBTable.SERVER.SERVER_NAME;
        case 6: return DBTable.CPU.CPU_VENDOR;
        case 7: return DBTable.CPU.CPU_MODEL;
        case 8: return DBTable.CPU.CPU_GHZ;
        case 9: return DBTable.CPU.CPU_CACHE;
        case 10: return DBTable.CPU.CPU_CREATIONTIME;
        case 11: return DBTable.CPU.CPU_MODIFIEDTIME;
        case 12: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case 13: return DBTable.USER.USER_NAME;
        case 14: return DBTable.CPU_SERVICE.CPU_SERVICE_DESC;
        case 15: return DBTable.CPU_SERVICE.CPU_SERVICE_STARTTIME;
        case 16: return DBTable.CPU_SERVICE.CPU_SERVICE_ENDTIME;
        case 18: return DBTable.CPU_SERVICE.CPU_SERVICE_STATE;
        case 19: return DBTable.CPU_SERVICE.CPU_SERVICE_CREATIONTIME;
        case 20: return DBTable.CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME;
        }
        return null;
    }
    
    private int getLife(Date starttime, Date endtime) {
    	return (int)((endtime.getTime() - starttime.getTime()) / (1000L *24 *3600));
    }
    
    public synchronized SearchResult lookupCPUByDate(Session session, SearchRange range, CPUState cpu_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
    	ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupCPUByDate(cpu_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), -1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupCPUByDate(cpu_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupCPUByDate(cpu_state, dateBegin, dateEnd, getSortColumn(range), range.isAscending(), user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableServer SERVER = DBTable.SERVER;
            DBTableCPU CPU = DBTable.CPU;
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            for (int index = 1; rs.next(); index ++) {
            	int cpu_id = DBData.getInt(rs, CPU.CPU_ID);
            	int cs_id = DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_ID);
            	String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
            	String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
            	String cpu_vendor = DBData.getString(rs, CPU.CPU_VENDOR);
            	String cpu_model = DBData.getString(rs, CPU.CPU_MODEL);
            	double cpu_ghz = DBData.getDouble(rs, CPU.CPU_GHZ);
            	double cpu_cache = DBData.getDouble(rs, CPU.CPU_CACHE);
            	Date cpu_creationtime = DBData.getDate(rs, CPU.CPU_CREATIONTIME);
            	Date cpu_modifiedtime = DBData.getDate(rs, CPU.CPU_MODIFIEDTIME);
            	cpu_state = CPUState.getCPUState(DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_STATE));
            	String account_name = null;
            	String user_name = null;
            	String cs_desc = null;
            	Date cs_starttime = null;
            	Date cs_endtime = null;
            	String cs_life = null;
            	Date cs_creationtime = null;
            	Date cs_modifiedtime = null;
            	if (cpu_state != CPUState.RESERVED) {
            		account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
            		user_name = DBData.getString(rs, USER.USER_NAME);
            		cs_desc = DBData.getString(rs, CPU_SERVICE.CPU_SERVICE_DESC);
            		cs_starttime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_STARTTIME);
            		cs_endtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_ENDTIME);
            		cs_life = Integer.toString(getLife(cs_starttime, cs_endtime));
            		cs_creationtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_CREATIONTIME);
            		cs_modifiedtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME);
            	}
            	List<String> list = Arrays.asList(Integer.toString(cpu_id), Integer.toString(cs_id), "", Integer.toString(index ++),
            			cpu_name, server_name, cpu_vendor, cpu_model, Double.toString(cpu_ghz), Double.toString(cpu_cache),
            			DBData.format(cpu_creationtime), DBData.format(cpu_modifiedtime), account_name, user_name, cs_desc,
            			DBData.format(cs_starttime), DBData.format(cs_endtime), cs_life, cpu_state.toString(),
            			DBData.format(cs_creationtime), DBData.format(cs_modifiedtime));
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
            throw new EucalyptusServiceException(new ClientMessage("", "获取CPU列表失败"));
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
    
    public synchronized Map<Integer, Integer> lookupCPUCountsGroupByState(Session session, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		ResultSetWrapper rsw = null;
		try {
	    	LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupCPUCountsGroupByState(-1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupCPUCountsGroupByState(user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupCPUCountsGroupByState(user.getAccountId(), user.getUserId());
            }
			ResultSet rs = rsw.getResultSet();
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			int sum = 0;
			while (rs.next()) {
				int cpu_state = rs.getInt(1);
				int cpu_count = rs.getInt(2);
				sum += cpu_count;
				map.put(cpu_state, cpu_count);
			}
			map.put(-1, sum);
			return map;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "获取CPU计数失败"));
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
    
    public synchronized void addCPU(Session session, String cpu_name, String cpu_desc, String cpu_vendor, String cpu_model,
            double cpu_ghz, double cpu_cache, int num, String server_name) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (isEmpty(cpu_name)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU名称"));
    	}
    	if (isEmpty(server_name)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
    	}
    	if (cpu_desc == null) {
    		cpu_desc = "";
    	}
    	if (cpu_vendor == null) {
    		cpu_vendor = "";
    	}
    	if (cpu_model == null) {
    		cpu_model = "";
    	}
    	try {
    	    for (int i = 0; i < num; i ++) {
    	        dbproc.createCPU(cpu_name, cpu_desc, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_name);
    	    }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "创建CPU失败"));
    	}
    }
    
    public synchronized void addCPUService(Session session, String cs_desc, Date cs_starttime, Date cs_endtime, 
            CPUState cpu_state, int cpu_id, String account_name, String user_name) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (cs_starttime == null || cs_endtime == null) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
    	}
    	if (getLife(cs_starttime, cs_endtime) <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
    	}
    	if (cpu_state != CPUState.INUSE && cpu_state != CPUState.STOP) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
    	}
    	if (isEmpty(account_name) || isEmpty(user_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的账户名称"));
        }
    	if (cs_desc == null) {
    		cs_desc = "";
    	}
    	try {
    		dbproc.createCPUService(cs_desc, cs_starttime, cs_endtime, cpu_state, cpu_id, account_name, user_name);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "创建CPU服务失败"));
    	}
    }
    
    public synchronized void deleteCPU(Session session, List<Integer> cpu_ids) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		try {
			if (!cpu_ids.isEmpty()) {
				dbproc.deleteCPU(cpu_ids);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除CPU失败"));
		}
    }
    
    public synchronized void deleteCPUService(Session session, List<Integer> cs_ids) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	try {
    		if (!cs_ids.isEmpty()) {
    			dbproc.deleteCPUService(cs_ids);
    		}
    	}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除CPU服务失败"));
		}
    }
    
    public synchronized void modifyCPU(Session session, int cpu_id, String cpu_desc, String cpu_vendor, String cpu_model,
    		double cpu_ghz, double cpu_cache) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (cpu_desc == null) {
    		cpu_desc = "";
    	}
    	if (cpu_vendor == null) {
    		cpu_vendor = "";
    	}
    	if (cpu_model == null) {
    		cpu_model = "";
    	}
    	try {
    		dbproc.modifyCPU(cpu_id, cpu_desc, cpu_vendor, cpu_model, cpu_ghz, cpu_cache);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "修改CPU失败"));
    	}
    }
    
    public synchronized void modifyCPUService(Session session, int cs_id, String cs_desc,
    		Date cs_starttime, Date cs_endtime, CPUState cpu_state) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
       	if (cs_starttime == null || cs_endtime == null) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
    	}
    	if (getLife(cs_starttime, cs_endtime) <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
    	}
    	if (cpu_state != CPUState.INUSE && cpu_state != CPUState.STOP) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
    	}
    	if (cs_desc == null) {
    		cs_desc = "";
    	}
    	try {
    		dbproc.modifyCPUService(cs_id, cs_desc, cs_starttime, cs_endtime, cpu_state);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "修改CPU服务失败"));
    	}
	}
    
    public synchronized void updateCPUServiceState(int cs_id, CPUState cpu_state) throws EucalyptusServiceException {
        if (cpu_state != CPUState.INUSE && cpu_state != CPUState.STOP) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
    	}
    	try {
    		dbproc.updateCPUServiceState(cs_id, cpu_state);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "更新CPU服务状态失败"));
    	}
    }
    
    public synchronized List<String> lookupCPUNamesByServerName(Session session, String server_name) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (isEmpty(server_name)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
    	}
    	ResultSetWrapper rsw = null;
    	try {
    		rsw = dbproc.lookupCPUNamesByServerName(server_name);
    		DBTableCPU CPU = DBTable.CPU;
    		ResultSet rs = rsw.getResultSet();
    		List<String> cpu_name_list = new LinkedList<String>();
    		while (rs.next()) {
    			String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
    			cpu_name_list.add(cpu_name);
    		}
    		return cpu_name_list;
    	}
    	catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取CPU列表失败"));
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
    
    public synchronized CPUInfo lookupCPUInfoByID(int cpu_id) throws EucalyptusServiceException {
    	ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupCPUByID(cpu_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        DBTableCPU CPU = DBTable.CPU;
	    	String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
	    	String cpu_desc = DBData.getString(rs, CPU.CPU_DESC);
	    	String cpu_vendor = DBData.getString(rs, CPU.CPU_VENDOR);
	    	String cpu_model = DBData.getString(rs, CPU.CPU_MODEL);
	    	double cpu_ghz = DBData.getDouble(rs, CPU.CPU_GHZ);
	    	double cpu_cache = DBData.getDouble(rs, CPU.CPU_CACHE);
	    	Date cpu_creationtime = DBData.getDate(rs, CPU.CPU_CREATIONTIME);
	    	Date cpu_modifiedtime = DBData.getDate(rs, CPU.CPU_MODIFIEDTIME);
	    	int server_id = DBData.getInt(rs, CPU.SERVER_ID);
	    	return new CPUInfo(cpu_id, cpu_name, cpu_desc, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, cpu_creationtime, cpu_modifiedtime, server_id);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取CPU信息失败"));
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
    
    public synchronized CPUServiceInfo lookupCPUServiceInfoByID(int cs_id) throws EucalyptusServiceException {
    	ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupCPUServiceByID(cs_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
	    	String cs_desc = DBData.getString(rs, CPU_SERVICE.CPU_SERVICE_DESC);
	    	Date cs_starttime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_STARTTIME);
	    	Date cs_endtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_ENDTIME);
	    	CPUState cpu_state = CPUState.getCPUState(DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_STATE));
	    	Date cs_creationtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_CREATIONTIME);
	    	Date cs_modifiedtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME);
	    	int cpu_id = DBData.getInt(rs, CPU_SERVICE.CPU_ID);
	    	int user_id = DBData.getInt(rs, CPU_SERVICE.USER_ID);
	    	return new CPUServiceInfo(cs_id, cs_desc, cs_starttime, cs_endtime, cpu_state, cs_creationtime, cs_modifiedtime, cpu_id, user_id);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取CPU服务信息失败"));
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

class DeviceCPUDBProcWrapper {

	private static final Logger LOG = Logger.getLogger(DeviceCPUDBProcWrapper.class.getName());

	private DBProcWrapper wrapper = DBProcWrapper.Instance();

	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}

	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
    public ResultSetWrapper lookupCPUByID(int cpu_id) throws Exception {
        DBTableCPU CPU = DBTable.CPU;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(CPU).append(" WHERE ").append(CPU.CPU_ID).append(" = ").append(cpu_id);
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupCPUServiceByID(int cs_id) throws Exception {
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(CPU_SERVICE).append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_ID).append(" = ").append(cs_id);
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
	
	public ResultSetWrapper lookupCPUByDate(CPUState cpu_state, Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
		DBTableAccount ACCOUNT = DBTable.ACCOUNT;
		DBTableUser USER = DBTable.USER;
		DBTableServer SERVER = DBTable.SERVER;
		DBTableCPU CPU = DBTable.CPU;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBTableAlias A = DBTable.getDBTableAlias("A");
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(CPU.ANY).append(", ").append(CPU_SERVICE.ANY).append(", ").append(SERVER.SERVER_NAME).append(", ").append(A).append(" FROM ");
		sb.append("(SELECT ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(" FROM ");
		sb.append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE 1=1");
		if (account_id != 0) {
		    sb.append(" AND ").append(ACCOUNT.ACCOUNT_ID).append(" = ").append(account_id);
		}
		if (user_id != 0) {
		    sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
		}
		sb.append(") as ").append(A).append(" RIGHT JOIN ").append(CPU_SERVICE);
		sb.append(" ON ").append(CPU_SERVICE.USER_ID).append(" = ").append(A.getColumn(USER.USER_ID));
		sb.append(" LEFT JOIN ").append(CPU).append(" ON ").append(CPU_SERVICE.CPU_ID).append(" = ").append(CPU.CPU_ID);
		sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(CPU.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
		sb.append(" WHERE 1=1");
		if (cpu_state != null) {
			sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(cpu_state.getValue());
		}
		if (dateBegin != null || dateEnd != null) {
			sb.append(" AND (");
			appendBoundedDate(sb, CPU.CPU_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, CPU.CPU_MODIFIEDTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_STARTTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_ENDTIME, dateBegin, dateEnd);
			sb.append(")");
		}
		if (sort != null) {
            sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
        }
		return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupCPUCountsGroupByState(int account_id, int user_id) throws Exception {
		DBTableUser USER = DBTable.USER;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(", count(*) FROM ").append(CPU_SERVICE);
		if (account_id >= 0 || user_id >= 0) {
		    sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(CPU_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		}
		sb.append(" WHERE 1=1");
		if (account_id >= 0) {
			sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
		}
		if (user_id >= 0) {
			sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
		}
		sb.append(" GROUP BY ").append(CPU_SERVICE.CPU_SERVICE_STATE);
		return doQuery(sb.toString());
	}
	
	public void createCPU(String cpu_name, String cpu_desc, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) throws Exception {
	    DBTableServer SERVER = DBTable.SERVER;
        DBTableCPU CPU = DBTable.CPU;
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(CPU).append(" (");
        sb.append(CPU.CPU_NAME).append(", ");
        sb.append(CPU.CPU_DESC).append(", ");
        sb.append(CPU.CPU_VENDOR).append(", ");
        sb.append(CPU.CPU_MODEL).append(", ");
        sb.append(CPU.CPU_GHZ).append(", ");
        sb.append(CPU.CPU_CACHE).append(", ");
        sb.append(CPU.SERVER_ID).append(", ");
        sb.append(CPU.CPU_CREATIONTIME);
        sb.append(") VALUES (");
        sb.appendString(cpu_name).append(", ");
        sb.appendString(cpu_desc).append(", ");
        sb.appendString(cpu_vendor).append(", ");
        sb.appendString(cpu_model).append(", ");
        sb.append(cpu_ghz).append(", ");
        sb.append(cpu_cache).append(", ");
        sb.append("(SELECT ").append(SERVER.SERVER_ID).append(" FROM ").append(SERVER).append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name).append("), ");
        sb.appendDate(new Date()).append(")");
        doUpdate(sb.toString());
        
        sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(CPU_SERVICE).append(" (");
        sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(", ");
        sb.append(CPU_SERVICE.CPU_ID);
        sb.append(") VALUES (");
        sb.append(CPUState.RESERVED.getValue()).append(", ");
        sb.append("(SELECT MAX(").append(CPU.CPU_ID).append(") FROM ").append(CPU).append(" WHERE 1=1))");
        doUpdate(sb.toString());
	}
	
	public void createCPUService(String cs_desc, Date cs_starttime, Date cs_endtime, CPUState cpu_state, int cpu_id, String account_name, String user_name) throws Exception {
	    DBTableAccount ACCOUNT = DBTable.ACCOUNT;
	    DBTableUser USER = DBTable.USER;
	    DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("UPDATE ").append(CPU_SERVICE).append(" SET ");
        sb.append(CPU_SERVICE.CPU_SERVICE_DESC).append(" = ").appendString(cs_desc).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_STARTTIME).append(" = ").appendDate(cs_starttime).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_ENDTIME).append(" = ").appendDate(cs_endtime).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(cpu_state.getValue()).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_CREATIONTIME).append(" = ").appendDate(new Date()).append(", ");
        sb.append(CPU_SERVICE.USER_ID).append(" = ");
        sb.append("(SELECT ").append(USER.USER_ID).append(" FROM ").append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE ");
        sb.append(USER.USER_NAME).append(" = ").appendString(user_name).append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name).append(")");
        sb.append(" WHERE ").append(CPU_SERVICE.CPU_ID).append(" = ").append(cpu_id);
	}
	
	public void deleteCPU(List<Integer> cpu_ids) throws Exception {
		DBTableCPU CPU = DBTable.CPU;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		
		StringBuilder ids = new StringBuilder();
		int total = 0;
		for (int cpu_id : cpu_ids) {
			if (total ++ != 0) {
				ids.append(", ");
			}
			ids.append(cpu_id);
		}
		
		sb.append("DELETE FROM ").append(CPU_SERVICE).append(" WHERE ").append(CPU_SERVICE.CPU_ID).append(" IN (").append(ids.toString()).append(")");
		sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(CPUState.RESERVED.getValue());
		doUpdate(sb.toString());
		
		sb = new DBStringBuilder();
		sb.append("DELETE FROM ").append(CPU).append(" WHERE ").append(CPU.CPU_ID).append(" IN (").append(ids.toString()).append(")");
		doUpdate(sb.toString());
	}
	
	public void deleteCPUService(List<Integer> cs_ids) throws Exception {
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		
		StringBuilder ids = new StringBuilder();
		int total = 0;
		for (int cs_id : cs_ids) {
			if (total ++ != 0) {
				ids.append(", ");
			}
			ids.append(cs_id);
		}
		
		sb.append("UPDATE ").append(CPU_SERVICE).append(" SET ");
		sb.append(CPU_SERVICE.CPU_SERVICE_DESC).append(" = ").appendString(null).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_STARTTIME).append(" = ").appendDate(null).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_ENDTIME).append(" = ").appendDate(null).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(CPUState.RESERVED.getValue()).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_CREATIONTIME).append(" = ").appendDate(null).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME).append(" = ").appendDate(null).append(", ");
		sb.append(CPU_SERVICE.USER_ID).append(" = ").append(-1);
		sb.append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_ID).append(" IN (").append(ids.toString()).append(")");
		doUpdate(sb.toString());
	}
	
	public void modifyCPU(int cpu_id, String cpu_desc, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache) throws Exception {
		DBTableCPU CPU = DBTable.CPU;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(CPU).append(" SET ");
		sb.append(CPU.CPU_DESC).append(" = ").appendString(cpu_desc).append(", ");
		sb.append(CPU.CPU_VENDOR).append(" = ").appendString(cpu_vendor).append(", ");
		sb.append(CPU.CPU_MODEL).append(" = ").appendString(cpu_model).append(", ");
		sb.append(CPU.CPU_GHZ).append(" = ").append(cpu_ghz).append(", ");
		sb.append(CPU.CPU_CACHE).append(" = ").append(cpu_cache).append(", ");
		sb.append(CPU.CPU_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ").append(CPU.CPU_ID).append(" = ").append(cpu_id);
		doUpdate(sb.toString());
	}
	
	public void modifyCPUService(int cs_id, String cs_desc, Date cs_starttime, Date cs_endtime, CPUState cpu_state) throws Exception {
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(CPU_SERVICE).append(" SET ");
		sb.append(CPU_SERVICE.CPU_SERVICE_DESC).append(" = ").appendString(cs_desc).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_STARTTIME).append(" = ").appendDate(cs_starttime).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_ENDTIME).append(" = ").appendDate(cs_endtime).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(cpu_state.getValue()).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_ID).append(" = ").append(cs_id);
		doUpdate(sb.toString());
	}
	
	public void updateCPUServiceState(int cs_id, CPUState cpu_state) throws Exception {
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(CPU_SERVICE).append(" SET ");
		sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(cpu_state.getValue());
		sb.append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_ID).append(" = ").append(cs_id);
		doUpdate(sb.toString());
	}
	
	public ResultSetWrapper lookupCPUNamesByServerName(String server_name) throws Exception {
	    DBTableServer SERVER = DBTable.SERVER;
	    DBTableCPU CPU = DBTable.CPU;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT DISTINCT(").append(CPU.CPU_NAME).append(") FROM");
	    sb.append(CPU).append(" LEFT JOIN ").append(SERVER).append(" ON ").append(CPU.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
	    sb.append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name);
	    return doQuery(sb.toString());
	}
	
}
