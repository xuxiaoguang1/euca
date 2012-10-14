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
import com.eucalyptus.webui.shared.resource.device.CPUInfo;
import com.eucalyptus.webui.shared.resource.device.CPUServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
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
            new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
            new SearchResultFieldDesc(false, "3EM", new ClientMessage("", "序号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "CPU名称"),
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
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "总数量"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "占用数量"),
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
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "生产厂家"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "产品型号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "主频(GHz)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "缓存(MB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "硬件添加时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "0%", new ClientMessage("", "硬件修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.CPU.ACCOUNT_NAME: return DBTable.ACCOUNT.ACCOUNT_NAME;
        case CellTableColumns.CPU.USER_NAME: return DBTable.USER.USER_NAME;
        case CellTableColumns.CPU.SERVER_NAME: return DBTable.SERVER.SERVER_NAME;
        case CellTableColumns.CPU.CPU_NAME: return DBTable.CPU.CPU_NAME;
        case CellTableColumns.CPU.CPU_TOTAL: return DBTable.CPU.CPU_TOTAL;
        case CellTableColumns.CPU.CPU_SERVICE_USED: return DBTable.CPU_SERVICE.CPU_SERVICE_USED;
        case CellTableColumns.CPU.CPU_VENDOR: return DBTable.CPU.CPU_VENDOR;
        case CellTableColumns.CPU.CPU_MODEL: return DBTable.CPU.CPU_MODEL;
        case CellTableColumns.CPU.CPU_GHZ: return DBTable.CPU.CPU_GHZ;
        case CellTableColumns.CPU.CPU_CACHE: return DBTable.CPU.CPU_CACHE;
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
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
            	if (start <= index && index < end) {
	            	int cpu_id = DBData.getInt(rs, CPU.CPU_ID);
	            	int cs_id = DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_ID);
	            	String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
	            	String cpu_desc = DBData.getString(rs, CPU.CPU_DESC);
	            	int cpu_total = DBData.getInt(rs, CPU.CPU_TOTAL);
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
	            	int cs_used = DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_USED);
	            	Date cs_starttime = null;
	            	Date cs_endtime = null;
	            	String cs_life = null;
	            	Date cs_creationtime = null;
	            	Date cs_modifiedtime = null;
	            	if (cpu_state != CPUState.RESERVED) {
	            		account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME.getName());
	            		user_name = DBData.getString(rs, USER.USER_NAME.getName());
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
	            	row.setColumn(CellTableColumns.CPU.CPU_SERVICE_STATE, cpu_state.toString());
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
	            	row.setColumn(CellTableColumns.CPU.CPU_VENDOR, cpu_vendor);
	            	row.setColumn(CellTableColumns.CPU.CPU_MODEL, cpu_model);
	            	row.setColumn(CellTableColumns.CPU.CPU_GHZ, cpu_ghz);
	            	row.setColumn(CellTableColumns.CPU.CPU_CACHE, cpu_cache);
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
    
    public synchronized Map<Integer, Integer> lookupCPUCountsGroupByState(Session session) throws EucalyptusServiceException {
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
    
    public synchronized void addCPU(Session session, String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model,
            double cpu_ghz, double cpu_cache, String server_name) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (isEmpty(cpu_name)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU名称"));
    	}
    	if (cpu_total <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU数量"));
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
    		dbproc.createCPU(cpu_name, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_name);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "创建CPU失败"));
    	}
    }
    
    public synchronized void addCPUService(Session session, String cs_desc, int cs_size, Date cs_starttime, Date cs_endtime, int cpu_id, String account_name, String user_name) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (cs_starttime == null || cs_endtime == null || DBData.calcLife(cs_endtime, cs_starttime) <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
    	}
    	if (cs_size <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU数量"));
    	}
    	if (isEmpty(account_name) || isEmpty(user_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的账户名称"));
        }
    	if (cs_desc == null) {
    		cs_desc = "";
    	}
    	try {
    		dbproc.createCPUService(cs_desc, cs_size, cs_starttime, cs_endtime, cpu_id, account_name, user_name);
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
    
    public synchronized void modifyCPU(Session session, int cpu_id, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model,
    		double cpu_ghz, double cpu_cache) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (cpu_total <= 0) {
    	    throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU数量"));
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
    	ResultSetWrapper rsw = null;
    	int total = 0;
    	int reserved = 0;
    	try {
    		rsw = dbproc.getCPUResizeInfo(cpu_id);
    		ResultSet rs = rsw.getResultSet();
    		rs.next();
    		total = rs.getInt(1);
    		reserved = rs.getInt(2);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "获取CPU数量失败"));
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
    	if (cpu_total < total - reserved) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU数量：已使用 " + (total - reserved)));
    	}
    	try {
    		dbproc.modifyCPU(cpu_id, cpu_desc, cpu_total - total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "修改CPU失败"));
    	}
    }
    
    public synchronized void modifyCPUService(Session session, int cs_id, String cs_desc,
    		Date cs_starttime, Date cs_endtime) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (cs_starttime == null || cs_endtime == null || DBData.calcLife(cs_endtime, cs_starttime) <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
    	}
    	if (cs_desc == null) {
    		cs_desc = "";
    	}
    	try {
    		dbproc.modifyCPUService(cs_id, cs_desc, cs_starttime, cs_endtime);
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
	    	int cpu_total = DBData.getInt(rs, CPU.CPU_TOTAL);
	    	String cpu_vendor = DBData.getString(rs, CPU.CPU_VENDOR);
	    	String cpu_model = DBData.getString(rs, CPU.CPU_MODEL);
	    	double cpu_ghz = DBData.getDouble(rs, CPU.CPU_GHZ);
	    	double cpu_cache = DBData.getDouble(rs, CPU.CPU_CACHE);
	    	Date cpu_creationtime = DBData.getDate(rs, CPU.CPU_CREATIONTIME);
	    	Date cpu_modifiedtime = DBData.getDate(rs, CPU.CPU_MODIFIEDTIME);
	    	int server_id = DBData.getInt(rs, CPU.SERVER_ID);
	    	return new CPUInfo(cpu_id, cpu_name, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, cpu_creationtime, cpu_modifiedtime, server_id);
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
	
    private void doCleanup() throws Exception {
    	DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("DELETE FROM ").append(CPU_SERVICE).append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_USED).append(" = ").append(0);
        sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" != ").append(CPUState.RESERVED.getValue());
        doUpdate(sb.toString());
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
	
	public ResultSetWrapper lookupCPUByDate(CPUState cpu_state, Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending, int account_id, int user_id) throws Exception {
		DBTableAccount ACCOUNT = DBTable.ACCOUNT;
		DBTableUser USER = DBTable.USER;
		DBTableServer SERVER = DBTable.SERVER;
		DBTableCPU CPU = DBTable.CPU;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(CPU.ANY).append(", ").append(CPU_SERVICE.ANY).append(", ").append(SERVER.SERVER_NAME).append(", ").append(ACCOUNT.ACCOUNT_NAME).append(", ").append(USER.USER_NAME).append(", ");
		appendServiceLife(sb, CPU_SERVICE.CPU_SERVICE_STARTTIME, CPU_SERVICE.CPU_SERVICE_ENDTIME, CPU_SERVICE.CPU_SERVICE_LIFE).append(" FROM "); 
		sb.append(CPU_SERVICE).append(" LEFT JOIN ").append(USER).append(" ON ").append(CPU_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
		sb.append(" LEFT JOIN ").append(CPU).append(" ON ").append(CPU_SERVICE.CPU_ID).append(" = ").append(CPU.CPU_ID);
		sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(CPU.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
		sb.append(" WHERE ").append(CPU_SERVICE.CPU_SERVICE_USED).append(" != ").append(0);
		if (cpu_state != null) {
			sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(cpu_state.getValue());
		}
		if (user_id >= 0) {
		    sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
		}
		if (account_id >= 0) {
		    sb.append(" AND ").append(ACCOUNT.ACCOUNT_ID).append(" = ").append(account_id);
		}
		if (dateBegin != null || dateEnd != null) {
			sb.append(" AND (");
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_STARTTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_ENDTIME, dateBegin, dateEnd);
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
	
	public ResultSetWrapper lookupCPUCountsGroupByState(int account_id, int user_id) throws Exception {
		DBTableUser USER = DBTable.USER;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(", sum(").append(CPU_SERVICE.CPU_SERVICE_USED).append(") FROM ").append(CPU_SERVICE);
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
	
	public void createCPU(String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) throws Exception {
	    DBTableServer SERVER = DBTable.SERVER;
        DBTableCPU CPU = DBTable.CPU;
        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(CPU).append(" (");
        sb.append(CPU.CPU_NAME).append(", ");
        sb.append(CPU.CPU_DESC).append(", ");
        sb.append(CPU.CPU_TOTAL).append(", ");
        sb.append(CPU.CPU_VENDOR).append(", ");
        sb.append(CPU.CPU_MODEL).append(", ");
        sb.append(CPU.CPU_GHZ).append(", ");
        sb.append(CPU.CPU_CACHE).append(", ");
        sb.append(CPU.SERVER_ID).append(", ");
        sb.append(CPU.CPU_CREATIONTIME);
        sb.append(") VALUES (");
        sb.appendString(cpu_name).append(", ");
        sb.appendString(cpu_desc).append(", ");
        sb.append(cpu_total).append(", ");
        sb.appendString(cpu_vendor).append(", ");
        sb.appendString(cpu_model).append(", ");
        sb.append(cpu_ghz).append(", ");
        sb.append(cpu_cache).append(", ");
        sb.append("(SELECT ").append(SERVER.SERVER_ID).append(" FROM ").append(SERVER).append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name).append("), ");
        sb.appendDate(new Date()).append(")");
        doUpdate(sb.toString());
        
        sb = new DBStringBuilder();
        sb.append("INSERT INTO ").append(CPU_SERVICE).append(" (");
        sb.append(CPU_SERVICE.CPU_SERVICE_DESC).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_USED).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_STARTTIME).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_ENDTIME).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_CREATIONTIME).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME).append(", ");
        sb.append(CPU_SERVICE.CPU_ID).append(", ");
        sb.append(CPU_SERVICE.USER_ID);
        sb.append(") VALUES (");
        sb.appendString(null).append(", ");
        sb.append(cpu_total).append(", ");
        sb.appendNull().append(", ");
        sb.appendNull().append(", ");
        sb.append(CPUState.RESERVED.getValue()).append(", ");
        sb.appendNull().append(", ");
        sb.appendNull().append(", ");
        sb.append("(SELECT MAX(").append(CPU.CPU_ID).append(") FROM ").append(CPU).append(" WHERE 1=1)").append(", ");
        sb.appendNull().append(")");
        doUpdate(sb.toString());
        
        doCleanup();
	}
	
	public void createCPUService(String cs_desc, int cs_used, Date cs_starttime, Date cs_endtime, int cpu_id, String account_name, String user_name) throws Exception {
        DBTableAccount ACCOUNT = DBTable.ACCOUNT;
        DBTableUser USER = DBTable.USER;
	    DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
	    DBStringBuilder sb = new DBStringBuilder();
        
        sb.append("INSERT INTO ").append(CPU_SERVICE).append(" (");
        sb.append(CPU_SERVICE.CPU_SERVICE_DESC).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_USED).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_STARTTIME).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_ENDTIME).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_STATE).append(", ");
        sb.append(CPU_SERVICE.CPU_SERVICE_CREATIONTIME).append(", ");
        sb.append(CPU_SERVICE.USER_ID).append(", ");
        sb.append(CPU_SERVICE.CPU_ID);
        sb.append(") VALUES (");
        sb.appendString(cs_desc).append(", ");
        sb.append(cs_used).append(", ");
        sb.appendDate(cs_starttime).append(", ");
        sb.appendDate(cs_endtime).append(", ");
        sb.append(CPUState.STOP.getValue()).append(", ");
        sb.appendDate(new Date()).append(", ");
        sb.append("(SELECT ").append(USER.USER_ID).append(" FROM ").append(USER).append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID).append(" WHERE ");
        sb.append(USER.USER_NAME).append(" = ").appendString(user_name).append(" AND ").append(ACCOUNT.ACCOUNT_NAME).append(" = ").appendString(account_name).append(")").append(", ");
        sb.append(cpu_id).append(")");
        doUpdate(sb.toString());
        
        int cs_id = getMaxCPUServiceID();
        
        DBTableAlias A = DBTable.getDBTableAlias("A");
        DBTableAlias B = DBTable.getDBTableAlias("B");
        DBTableColumn A_USED = A.getColumn(CPU_SERVICE.CPU_SERVICE_USED);
        DBTableColumn B_USED = B.getColumn(CPU_SERVICE.CPU_SERVICE_USED);
        sb = new DBStringBuilder();
        sb.append("UPDATE ").append(CPU_SERVICE).append(" ").append(A).append(", ").append(CPU_SERVICE).append(" ").append(B).append(" SET ");
        sb.append(A_USED).append(" = ").append(cs_used).append(", ");
        sb.append(B_USED).append(" = ").append("(").append(B_USED).append(" - ").append(cs_used).append(")");
        sb.append(" WHERE ").append(A.getColumn(CPU_SERVICE.CPU_SERVICE_ID)).append(" = ").append(cs_id);
        sb.append(" AND ").append(A.getColumn(CPU_SERVICE.CPU_ID)).append(" = ").append(B.getColumn(CPU_SERVICE.CPU_ID));
        sb.append(" AND ").append(B.getColumn(CPU_SERVICE.CPU_SERVICE_STATE)).append(" = ").append(CPUState.RESERVED.getValue());
        sb.append(" AND ").append(B.getColumn(CPU_SERVICE.CPU_SERVICE_USED)).append(" >= ").append(cs_used);
        doUpdate(sb.toString());
        
        doCleanup();
	}
	
	private int getMaxCPUServiceID() throws Exception {
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT MAX(").append(CPU_SERVICE.CPU_SERVICE_ID).append(") FROM ").append(CPU_SERVICE).append(" WHERE 1=1");
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
	
	public ResultSetWrapper getCPUResizeInfo(int cpu_id) throws Exception {
		DBTableCPU CPU = DBTable.CPU;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT ").append(CPU.CPU_TOTAL).append(", ").append(CPU_SERVICE.CPU_SERVICE_USED).append(" FROM ").append(CPU_SERVICE);
	    sb.append(" LEFT JOIN ").append(CPU).append(" ON ").append(CPU_SERVICE.CPU_ID).append(" = ").append(CPU.CPU_ID);
	    sb.append(" WHERE ").append(CPU_SERVICE.CPU_ID).append(" = ").append(cpu_id);
	    sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(CPUState.RESERVED.getValue());
	    return doQuery(sb.toString());
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
		
        sb.append("DELETE ").append(CPU_SERVICE).append(" FROM ").append(CPU_SERVICE).append(" LEFT JOIN ").append(CPU);
        sb.append(" ON ").append(CPU.CPU_ID).append(" = ").append(CPU_SERVICE.CPU_ID);
        sb.append(" WHERE ").append(CPU_SERVICE.CPU_ID).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(CPUState.RESERVED.getValue());
        sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_USED).append(" = ").append(CPU.CPU_TOTAL);
        doUpdate(sb.toString());
        
        sb = new DBStringBuilder();
        sb.append("DELETE FROM ").append(CPU).append(" WHERE ").append(CPU.CPU_ID).append(" IN (").append(ids.toString()).append(")");
        doUpdate(sb.toString());
        
        doCleanup();
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

        DBTableAlias A = DBTable.getDBTableAlias("A");
        DBTableAlias B = DBTable.getDBTableAlias("B");
        DBTableColumn A_USED = A.getColumn(CPU_SERVICE.CPU_SERVICE_USED);
        DBTableColumn B_USED = B.getColumn(CPU_SERVICE.CPU_SERVICE_USED);
        sb.append("UPDATE ").append(CPU_SERVICE).append(" ").append(A).append(", ").append(CPU_SERVICE).append(" ").append(B).append(" SET ");
        sb.append(A_USED).append(" = ").append(0).append(", ");
        sb.append(B_USED).append(" = ").append("(").append(B_USED).append(" + ").append(A_USED).append(")");
        sb.append(" WHERE ").append(A.getColumn(CPU_SERVICE.CPU_SERVICE_ID)).append(" IN (").append(ids.toString()).append(")");
        sb.append(" AND ").append(A.getColumn(CPU_SERVICE.CPU_ID)).append(" = ").append(B.getColumn(CPU_SERVICE.CPU_ID));
        sb.append(" AND ").append(B.getColumn(CPU_SERVICE.CPU_SERVICE_STATE)).append(" = ").append(CPUState.RESERVED.getValue());
        doUpdate(sb.toString());
        
        doCleanup();
	}
	
	public void modifyCPU(int cpu_id, String cpu_desc, int cpu_resize, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache) throws Exception {
		DBTableCPU CPU = DBTable.CPU;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(CPU).append(" LEFT JOIN ").append(CPU_SERVICE).append(" ON ").append(CPU.CPU_ID).append(" = ").append(CPU_SERVICE.CPU_ID).append(" SET ");
		sb.append(CPU.CPU_DESC).append(" = ").appendString(cpu_desc).append(", ");
		sb.append(CPU.CPU_TOTAL).append(" = (").append(CPU.CPU_TOTAL).append(" + ").append(cpu_resize).append("), ");
		sb.append(CPU.CPU_VENDOR).append(" = ").appendString(cpu_vendor).append(", ");
		sb.append(CPU.CPU_MODEL).append(" = ").appendString(cpu_model).append(", ");
		sb.append(CPU.CPU_GHZ).append(" = ").append(cpu_ghz).append(", ");
		sb.append(CPU.CPU_CACHE).append(" = ").append(cpu_cache).append(", ");
		sb.append(CPU.CPU_MODIFIEDTIME).append(" = ").appendDate(new Date()).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_USED).append(" = (").append(CPU_SERVICE.CPU_SERVICE_USED).append(" + ").append(cpu_resize).append(")");
		sb.append(" WHERE ").append(CPU.CPU_ID).append(" = ").append(cpu_id);
		sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(CPUState.RESERVED.getValue());
		sb.append(" AND ").append("(").append(CPU_SERVICE.CPU_SERVICE_USED).append(" + ").append(cpu_resize).append(")").append(" >= 0");
		doUpdate(sb.toString());
	}
	
	public void modifyCPUService(int cs_id, String cs_desc, Date cs_starttime, Date cs_endtime) throws Exception {
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(CPU_SERVICE).append(" SET ");
		sb.append(CPU_SERVICE.CPU_SERVICE_DESC).append(" = ").appendString(cs_desc).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_STARTTIME).append(" = ").appendDate(cs_starttime).append(", ");
		sb.append(CPU_SERVICE.CPU_SERVICE_ENDTIME).append(" = ").appendDate(cs_endtime).append(", ");
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
	    sb.append(" ORDER BY ").append(CPU.CPU_NAME);
	    return doQuery(sb.toString());
	}
	
}
