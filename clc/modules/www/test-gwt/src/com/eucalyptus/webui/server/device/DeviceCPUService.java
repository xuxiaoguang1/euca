package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.eucalyptus.webui.client.view.DeviceCPUDeviceAddView.DataCache;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.resource.device.CPUInfo;
import com.eucalyptus.webui.shared.resource.device.CPUServiceInfo;
import com.eucalyptus.webui.shared.resource.device.ServerInfo;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

//class DeviceCPUDBProcWrapper2 {
//
//	ResultSetWrapper listAccounts() throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("SELECT * FROM ").append(DBTableName.ACCOUNT);
//		sb.append(" WHERE 1=1");
//		return doQuery(sb.toString());
//	}
//
//	/* *
//	 * select * from account left join user on
//	 * account.account_id=user.account_id where account.account_name=$account
//	 */
//	ResultSetWrapper listUsersByAccount(String account) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("SELECT * FROM ");
//		sb.append(DBTableName.USER).append(" LEFT JOIN ").append(DBTableName.ACCOUNT);
//		sb.append(" ON ");
//		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID);
//		sb.append(" = ");
//		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID);
//		sb.append(" WHERE ");
//		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME);
//		sb.append(" = ").append("\"").append(account).append("\"");
//		return doQuery(sb.toString());
//	}
//
//	/* *
//	 * select * from cpu_service where cs_id=$cs_id
//	 */
//	ResultSetWrapper queryService(int cs_id) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("SELECT * FROM ");
//		sb.append(DBTableName.CPU_SERVICE);
//		sb.append(" WHERE ");
//		sb.append(DBTableColName.CPU_SERVICE.ID);
//		sb.append(" = ").append(cs_id);
//		return doQuery(sb.toString());
//	}
//
//	/* *
//	 * select distinct entry from cpu;
//	 */
//	ResultSetWrapper listColumnValues(String entry) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("SELECT DISTINCT ").append(entry).append(" FROM ").append(DBTableName.CPU);
//		return doQuery(sb.toString());
//	}
//
//	/* *
//	 * select server_name, server_mark from server;
//	 */
//	ResultSetWrapper listServers() throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("SELECT ");
//		sb.append(DBTableColName.SERVER.NAME).append(", ").append(DBTableColName.SERVER.MARK);
//		sb.append(" FROM ").append(DBTableName.SERVER);
//		return doQuery(sb.toString());
//	}
//
//	void modifyService(int cs_id, String endtime, int cs_state) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("UPDATE ");
//		sb.append(DBTableName.CPU_SERVICE);
//		sb.append(" SET ");
//
//		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ");
//		sb.append("(SELECT DATEDIFF((SELECT IF(");
//		sb.append("\"").append(endtime).append("\"");
//		sb.append(" > ").append(DBTableColName.CPU_SERVICE.STARTTIME).append(", ");
//		sb.append("\"").append(endtime).append("\"").append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(")), ");
//		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(")), ");
//
//		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(cs_state);
//		sb.append(" WHERE ");
//		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
//		doUpdate(sb.toString());
//	}
//	
//	void updateServiceState(int cs_id, int cs_state) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("UPDATE ");
//		sb.append(DBTableName.CPU_SERVICE);
//		sb.append(" SET ");
//		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(cs_state);
//		sb.append(" WHERE ");
//		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
//		doUpdate(sb.toString());
//	}
//	
//	/* *
//	 * update cpu_service set user_id=(select user.user_id from account left
//	 * join user on account.account_id=user.account_id where
//	 * account.account_name=$account and user.user_name=$user),
//	 * cs_starttime=$cs_starttime, cs_life=$cs_life, cs_state=$cs_state where
//	 * cs_id=$cs_id
//	 */
//	void addService(int cs_id, String account, String user, String cs_starttime, int cs_life, int cs_state)
//	        throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("UPDATE ");
//		sb.append(DBTableName.CPU_SERVICE);
//		sb.append(" SET ");
//
//		sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = ").append("(");
//		sb.append("SELECT ").append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).append(" FROM ");
//		sb.append(DBTableName.USER).append(" LEFT JOIN ").append(DBTableName.ACCOUNT);
//		sb.append(" ON ");
//		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID);
//		sb.append(" = ");
//		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID);
//		sb.append(" WHERE ");
//		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME);
//		sb.append(" = ").append("\"").append(account).append("\"");
//		sb.append(" AND ");
//		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.NAME);
//		sb.append(" = ").append("\"").append(user).append("\"");
//		sb.append("), ");
//
//		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(" = ").append("\"").append(cs_starttime).append("\", ");
//		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ").append(cs_life).append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(cs_state);
//		sb.append(" WHERE ");
//		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
//		doUpdate(sb.toString());
//	}
//
//	/* *
//	 * update cpu_service set cs_starttime="0000-00-00",
//	 * cs_state=CPUState.RESERVED, cs_life=0, user_id=NULL where
//	 * cs_id=$cs_id
//	 */
//	void deleteService(int cs_id) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("UPDATE ");
//		sb.append(DBTableName.CPU_SERVICE);
//		sb.append(" SET ");
//		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(" = \"0000-00-00\"");
//		sb.append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(CPUState.RESERVED.getValue());
//		sb.append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ").append(0);
//		sb.append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = NULL");
//		sb.append(" WHERE ");
//		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
//		doUpdate(sb.toString());
//	}
//
//	/* *
//	 * delete cpu, cpu_service from cpu left join cpu_service on cpu.cpu_id=cpu_service.cpu_id
//	 * where cpu_service.cs_state=CPUState.RESERVED and cpu.cpu_id=$cpu_id;
//	 */
//	void deleteDevice(int cpu_id) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("DELETE ").append(DBTableName.CPU).append(", ").append(DBTableName.CPU_SERVICE);
//		sb.append(" FROM ").append(DBTableName.CPU).append(" LEFT JOIN ").append(DBTableName.CPU_SERVICE);
//		sb.append(" ON ");
//		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID);
//		sb.append(" = ");
//		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.CPU_ID);
//		sb.append(" WHERE ");
//		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.STATE);
//		sb.append(" = ");
//		sb.append(CPUState.RESERVED.getValue());
//		sb.append(" AND ");
//		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID);
//		sb.append(" = ");
//		sb.append(cpu_id);
//		doUpdate(sb.toString());
//	}
//
//	/* *
//	 * insert into cpu (cpu_name, cpu_vendor, cpu_model, cpu_ghz, cpu_cache,
//	 * server_id) values ("$name", "$vendor", "$model", ghz, cache, (select
//	 * server_id from server where server_mark="$server_mark"))
//	 * 
//	 * insert into cpu_service (cs_starttime, cs_state, cs_life, cpu_id,
//	 * user_id) values ("0000-00-00", CPUState.RESERVED, 0, (select
//	 * last_insert_id()), NULL)
//	 */
//
//	
//}

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
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "名称"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "服务器"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "厂家"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "型号"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "主频"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "缓存"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "设备创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "设备修改时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "账户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "用户"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "开始时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "结束时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "剩余时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "状态"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "服务创建时间"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "服务修改时间"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private int getLife(Date starttime, Date endtime) {
    	return (int)((endtime.getTime() - starttime.getTime()) / (1000L *24 *3600));
    }
    
    public synchronized SearchResult lookupCPUByDate(Session session, SearchRange range, CPUState state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
    	ResultSetWrapper rsw = null;
        try {
            LoginUserProfile user = getUser(session);
            if (user.isSystemAdmin()) {
                rsw = dbproc.lookupCPUByDate(state, dateBegin, dateEnd, -1, -1);
            }
            else if (user.isAccountAdmin()) {
                rsw = dbproc.lookupCPUByDate(state, dateBegin, dateEnd, user.getAccountId(), -1);
            }
            else {
                rsw = dbproc.lookupCPUByDate(state, dateBegin, dateEnd, user.getAccountId(), user.getUserId());
            }
            ResultSet rs = rsw.getResultSet();
            List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
            DBTableAccount ACCOUNT = DBTable.ACCOUNT;
            DBTableUser USER = DBTable.USER;
            DBTableServer SERVER = DBTable.SERVER;
            DBTableCPU CPU = DBTable.CPU;
            DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
            for (int index = 1; rs.next(); index ++) {
            	int cpu_id = DBData.getInt(rs, CPU.CPU_ID);
            	int cpu_service_id = DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_ID);
            	String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
            	String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
            	String cpu_vendor = DBData.getString(rs, CPU.CPU_VENDOR);
            	String cpu_model = DBData.getString(rs, CPU.CPU_MODEL);
            	double cpu_ghz = DBData.getDouble(rs, CPU.CPU_GHZ);
            	double cpu_cache = DBData.getDouble(rs, CPU.CPU_CACHE);
            	Date cpu_creationtime = DBData.getDate(rs, CPU.CPU_CREATIONTIME);
            	Date cpu_modifiedtime = DBData.getDate(rs, CPU.CPU_MODIFIEDTIME);
            	CPUState cpu_state = CPUState.getCPUState(DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_STATE));
            	String account_name = null;
            	String user_name = null;
            	String cpu_service_desc = null;
            	Date cpu_service_starttime = null;
            	Date cpu_service_endtime = null;
            	String cpu_service_life = null;
            	Date cpu_service_creationtime = null;
            	Date cpu_service_modifiedtime = null;
            	if (cpu_state != CPUState.RESERVED) {
            		account_name = DBData.getString(rs, ACCOUNT.ACCOUNT_NAME);
            		user_name = DBData.getString(rs, USER.USER_NAME);
            		cpu_service_desc = DBData.getString(rs, CPU_SERVICE.CPU_SERVICE_DESC);
            		cpu_service_starttime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_STARTTIME);
            		cpu_service_endtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_ENDTIME);
            		cpu_service_life = Integer.toString(getLife(cpu_service_starttime, cpu_service_endtime));
            		cpu_service_creationtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_CREATIONTIME);
            		cpu_service_modifiedtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME);
            	}
            	List<String> list = Arrays.asList(Integer.toString(cpu_id), Integer.toString(cpu_service_id), "", Integer.toString(index ++),
            			cpu_name, server_name, cpu_vendor, cpu_model, Double.toString(cpu_ghz), Double.toString(cpu_cache),
            			DBData.format(cpu_creationtime), DBData.format(cpu_modifiedtime), account_name, user_name, cpu_service_desc,
            			DBData.format(cpu_service_starttime), DBData.format(cpu_service_endtime), cpu_service_life, cpu_state.toString(),
            			DBData.format(cpu_service_creationtime), DBData.format(cpu_service_modifiedtime));
                rows.add(new SearchResultRow(list));
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
            throw new EucalyptusServiceException(new ClientMessage("", "获取服务器列表失败"));
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
			throw new EucalyptusServiceException(new ClientMessage("", "获取服务器计数失败"));
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
    
    public synchronized void addCPU(Session session, String cpu_name, String cpu_desc,
    		String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) throws EucalyptusServiceException {
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
    		dbproc.createCPU(cpu_name, cpu_desc, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_name);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "创建CPU失败"));
    	}
    }
    
    public synchronized void addCPUService(Session session, String account_name, String user_name, String cpu_service_desc,
    		Date cpu_service_starttime, Date cpu_service_endtime, CPUState cpu_service_state, int cpu_id) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (isEmpty(account_name) || isEmpty(user_name)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的账户名称"));
    	}
    	if (cpu_service_starttime == null || cpu_service_endtime == null) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
    	}
    	if (getLife(cpu_service_starttime, cpu_service_endtime) <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
    	}
    	if (cpu_service_state == null || cpu_service_state == CPUState.RESERVED) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
    	}
    	if (cpu_service_desc == null) {
    		cpu_service_desc = "";
    	}
    	try {
    		dbproc.createCPUService(account_name, user_name, cpu_service_desc, cpu_service_starttime, cpu_service_endtime, cpu_service_state, cpu_id);
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
			for (int cpu_id : cpu_ids) {
				dbproc.deleteCPU(cpu_id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除CPU失败"));
		}
    }
    
    public synchronized void deleteCPUService(Session session, List<Integer> cpu_service_ids) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	try {
    		for (int cpu_service_id : cpu_service_ids) {
    			dbproc.deleteCPUService(cpu_service_id);
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
    
    public synchronized void modifyCPUService(Session session, int cpu_service_id, String cpu_service_desc,
    		Date cpu_service_starttime, Date cpu_service_endtime, CPUState cpu_service_state) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
       	if (cpu_service_starttime == null || cpu_service_endtime == null) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务日期"));
    	}
    	if (getLife(cpu_service_starttime, cpu_service_endtime) <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务期限"));
    	}
    	if (cpu_service_state == null || cpu_service_state == CPUState.RESERVED) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
    	}
    	if (cpu_service_desc == null) {
    		cpu_service_desc = "";
    	}
    	try {
    		dbproc.modifyCPUService(cpu_service_id, cpu_service_desc, cpu_service_starttime, cpu_service_endtime, cpu_service_state);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "修改CPU服务失败"));
    	}
	}
    
    public synchronized void updateCPUServiceState(int cpu_service_id, CPUState cpu_service_state) throws EucalyptusServiceException {
    	if (cpu_service_state == null || cpu_service_state == CPUState.RESERVED) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的服务状态"));
    	}
    	try {
    		dbproc.updateCPUServiceState(cpu_service_id, cpu_service_state);
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
	    	return new CPUInfo(cpu_id, cpu_name, cpu_desc, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, cpu_creationtime, cpu_modifiedtime);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取服务器信息失败"));
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
    
    public synchronized CPUServiceInfo lookupCPUServiceInfoByID(int cpu_service_id) throws EucalyptusServiceException {
    	ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupCPUServiceByID(cpu_service_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
	    	String cpu_service_desc = DBData.getString(rs, CPU_SERVICE.CPU_SERVICE_DESC);
	    	Date cpu_service_starttime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_STARTTIME);
	    	Date cpu_service_endtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_ENDTIME);
	    	CPUState cpu_service_state = CPUState.getCPUState(DBData.getInt(rs, CPU_SERVICE.CPU_SERVICE_STATE));
	    	Date cpu_service_creationtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_CREATIONTIME);
	    	Date cpu_service_modifiedtime = DBData.getDate(rs, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME);
	    	int cpu_id = DBData.getInt(rs, CPU_SERVICE.CPU_ID);
	    	int user_id = DBData.getInt(rs, CPU_SERVICE.USER_ID);
	    	return new CPUServiceInfo(cpu_service_id, cpu_service_desc, cpu_service_starttime, cpu_service_endtime, cpu_service_state, cpu_service_creationtime, cpu_service_endtime, cpu_id, user_id);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取服务器信息失败"));
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
	
	private DBStringBuilder appendBoundedDate(DBStringBuilder sb, DBTableColumn column, Date dateBegin, Date dateEnd) {
		sb.append(" AND (");
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
	
	public ResultSetWrapper lookupCPUByDate(CPUState state, Date dateBegin, Date dateEnd, int account_id, int user_id) throws Exception {
		DBTableAccount ACCOUNT = DBTable.ACCOUNT;
		DBTableUser USER = DBTable.USER;
		DBTableServer SERVER = DBTable.SERVER;
		DBTableCPU CPU = DBTable.CPU;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(CPU).append(" LEFT JOIN ").append(CPU_SERVICE).append(" ON ").append(CPU.CPU_ID).append(" = ").append(CPU_SERVICE.CPU_ID);
		sb.append(" LEFT JOIN ").append(SERVER).append(" ON ").append(CPU.SERVER_ID).append(" = ").append(SERVER.SERVER_ID);
		sb.append(" LEFT JOIN ").append(USER).append(" ON ").append(CPU_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		sb.append(" LEFT JOIN ").append(ACCOUNT).append(" ON ").append(USER.ACCOUNT_ID).append(" = ").append(ACCOUNT.ACCOUNT_ID);
		sb.append(" WHERE 1=1");
		if (state != null) {
			sb.append(" AND ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(" = ").append(state.getValue());
		}
		if (dateBegin != null || dateEnd != null) {
			appendBoundedDate(sb, CPU.CPU_CREATIONTIME, dateBegin, dateEnd);
			appendBoundedDate(sb, CPU.CPU_MODIFIEDTIME, dateBegin, dateEnd);
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_CREATIONTIME, dateBegin, dateEnd);
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_MODIFIEDTIME, dateBegin, dateEnd);
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_STARTTIME, dateBegin, dateEnd);
			appendBoundedDate(sb, CPU_SERVICE.CPU_SERVICE_ENDTIME, dateBegin, dateEnd);
		}
		return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupCPUCountsGroupByState(int account_id, int user_id) throws Exception {
		DBTableUser USER = DBTable.USER;
		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(CPU_SERVICE.CPU_SERVICE_STATE).append(", count(*) FROM ");
		sb.append(CPU_SERVICE).append(" LEFT JOIN ").append(USER).append(" ON ").append(CPU_SERVICE.USER_ID).append(" = ").append(USER.USER_ID);
		sb.append(" WHERE 1=1");
		if (account_id >= 0) {
			sb.append(" AND ").append(USER.ACCOUNT_ID).append(" = ").append(account_id);
		}
		if (user_id >= 0) {
			sb.append(" AND ").append(USER.USER_ID).append(" = ").append(user_id);
		}
		sb.append(" GROUP BY ");
		sb.append(CPU_SERVICE.CPU_SERVICE_STATE);
		return doQuery(sb.toString());
	}
	
	public void createCPU(String cpu_name, String cpu_desc, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) throws Exception {
	}
	
	public void createCPUService(String account_name, String user_name, String cpu_service_desc, Date cpu_service_starttime, Date cpu_service_endtime, CPUState cpu_service_state, int cpu_id) throws Exception {
	}
	
	public void deleteCPU(int cpu_id) throws Exception {
	}
	
	public void deleteCPUService(int cpu_service_id) throws Exception {
	}
	
	public void modifyCPU(int cpu_id, String cpu_desc, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache) throws Exception {
	}
	
	public void modifyCPUService(int cpu_service_id, String cpu_service_desc, Date cpu_service_starttime, Date cpu_service_endtime, CPUState cpu_service_state) throws Exception {
	}
	
	public void updateCPUServiceState(int cpu_service_id, CPUState state) throws Exception {
	}
	
	public ResultSetWrapper lookupCPUNamesByServerName(String server_name) throws Exception {
		return null;
	}
	
	public ResultSetWrapper lookupCPUByID(int cpu_id) throws Exception {
		return null;
	}
	
	public ResultSetWrapper lookupCPUServiceByID(int cpu_service_id) throws Exception {
		return null;
	}
	
//	public void createDevice(String cpu_name, String cpu_desc, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) throws Exception {
//		DBTableServer SERVER = DBTable.SERVER;
//		DBTableCPU CPU = DBTable.CPU;
//		DBTableCPUService CPU_SERVICE = DBTable.CPU_SERVICE;
//		DBStringBuilder sb = new DBStringBuilder();
//		sb.append("INSERT INTO ").append(CPU).append(" (");
//		sb.append(CPU.CPU_NAME).append(", ");
//		sb.append(CPU.CPU_NAME).append(", ");
//		sb.append(CPU.CPU_DESC).append(", ");
//		sb.append(CPU.CPU_VENDOR).append(", ");
//		sb.append(CPU.CPU_MODEL).append(", ");
//		sb.append(CPU.CPU_GHZ).append(", ");
//		sb.append(CPU.CPU_CACHE).append(", ");
//		sb.append(CPU.SERVER_ID).append(", ");
//		sb.append(CPU.CPU_CREATIONTIME).append(", ");
//		sb.append(") VALUES (");
//	}
//	
//	void addDevice(String serverMark, String name, String vendor, String model, double ghz, double cache)
//	        throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("INSERT INTO ").append(DBTableName.CPU);
//		sb.append(" (");
//		sb.append(DBTableColName.CPU.NAME).append(", ");
//		sb.append(DBTableColName.CPU.VENDOR).append(", ");
//		sb.append(DBTableColName.CPU.MODEL).append(", ");
//		sb.append(DBTableColName.CPU.GHZ).append(", ");
//		sb.append(DBTableColName.CPU.CACHE).append(", ");
//		sb.append(DBTableColName.CPU.SERVER_ID);
//		sb.append(")");
//		sb.append(" VALUES ");
//		sb.append("(");
//		sb.append("\"").append(name).append("\", ");
//		sb.append("\"").append(vendor).append("\", ");
//		sb.append("\"").append(model).append("\", ");
//		sb.append(ghz).append(", ").append(cache).append(", ");
//		sb.append("(SELECT ").append(DBTableColName.SERVER.ID).append(" FROM ");
//		sb.append(DBTableName.SERVER).append(" WHERE ").append(DBTableColName.SERVER.MARK).append(" = ");
//		sb.append("\"").append(serverMark).append("\"").append(")");
//		sb.append(")");
//		doUpdate(sb.toString());
//
//		sb = new StringBuilder();
//		sb.append("SELECT MAX(").append(DBTableColName.CPU.ID).append(") FROM ").append(DBTableName.CPU);
//
//		ResultSetWrapper rsw = null;
//		int cpu_id = -1;
//		try {
//			rsw = doQuery(sb.toString());
//			ResultSet rs = rsw.getResultSet();
//			if (rs.next()) {
//				cpu_id = rs.getInt(1);
//			}
//		}
//		catch (Exception e) {
//			throw e;
//		}
//		finally {
//			try {
//				if (rsw != null) {
//					rsw.close();
//				}
//			}
//			catch (Exception e) {
//				throw e;
//			}
//		}
//		assert (cpu_id != -1);
//
//		sb = new StringBuilder();
//		sb.append("INSERT INTO ").append(DBTableName.CPU_SERVICE);
//		sb.append(" (");
//		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.STATE).append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.CPU_ID).append(", ");
//		sb.append(DBTableColName.CPU_SERVICE.USER_ID);
//		sb.append(")");
//		sb.append(" VALUES ");
//		sb.append("(");
//		sb.append("\"0000-00-00\", ").append(CPUState.RESERVED.getValue()).append(", ").append(0).append(", ");
//		sb.append(cpu_id).append(", NULL");
//		sb.append(")");
//		doUpdate(sb.toString());
//	}
	
}
