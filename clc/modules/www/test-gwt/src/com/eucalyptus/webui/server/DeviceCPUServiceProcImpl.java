package com.eucalyptus.webui.server;

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

import com.eucalyptus.webui.client.activity.DeviceCPUActivity.CPUState;
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
import com.eucalyptus.webui.shared.user.LoginUserProfile;

class DeviceCPUDBProcWrapper {

	private static final Logger LOG = Logger.getLogger(DeviceCPUDBProcWrapper.class.getName());

	DBProcWrapper wrapper = DBProcWrapper.Instance();

	ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}

	void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}

	/* *
	 * select * from account;
	 */
	ResultSetWrapper listAccounts() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(DBTableName.ACCOUNT);
		sb.append(" WHERE 1=1");
		return doQuery(sb.toString());
	}

	/* *
	 * select * from account left join user on
	 * account.account_id=user.account_id where account.account_name=$account
	 */
	ResultSetWrapper listUsersByAccount(String account) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.USER).append(" LEFT JOIN ").append(DBTableName.ACCOUNT);
		sb.append(" ON ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID);
		sb.append(" = ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID);
		sb.append(" WHERE ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME);
		sb.append(" = ").append("\"").append(account).append("\"");
		return doQuery(sb.toString());
	}

	/* *
	 * select cs_state, count(*) from cpu_service where user_id=$user_id group
	 * by cs_state;
	 */
	ResultSetWrapper getCountsByState(int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableColName.CPU_SERVICE.STATE);
		sb.append(", count(*) FROM ");
		sb.append(DBTableName.CPU_SERVICE);

		if (user_id >= 0) {
			sb.append(" WHERE ");
			sb.append(DBTableColName.CPU_SERVICE.USER_ID);
			sb.append(" = ").append(user_id);
		}

		sb.append(" GROUP BY ");
		sb.append(DBTableColName.CPU_SERVICE.STATE);
		return doQuery(sb.toString());
	}

	/* *
	 * select * from cpu left join server on cpu.server_id = server.server_id
	 * left join cpu_service on cpu.cpu_id=cpu_service.cpu_id left join user on
	 * cpu_service.user_id=user.user_id left join account on
	 * user.account_id=account.id where 1=1 and cpu_service.cs_state=$queryState
	 * and user.id=$user_id
	 */
	ResultSetWrapper queryAllCPUs(int queryState, int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.CPU).append(" LEFT JOIN ").append(DBTableName.SERVER);
		sb.append(" ON ");
		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.SERVER_ID);
		sb.append(" = ");
		sb.append(DBTableName.SERVER).append(".").append(DBTableColName.SERVER.ID);
		sb.append(" LEFT JOIN ").append(DBTableName.CPU_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID);
		sb.append(" = ");
		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.CPU_ID);
		sb.append(" LEFT JOIN ").append(DBTableName.USER);
		sb.append(" ON ");
		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.USER_ID);
		sb.append(" = ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ID);
		sb.append(" LEFT JOIN ").append(DBTableName.ACCOUNT);
		sb.append(" ON ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID);
		sb.append(" = ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID);

		sb.append(" WHERE 1=1");

		if (queryState >= 0) {
			sb.append(" AND ");
			sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.STATE);
			sb.append(" = ").append(queryState);
		}
		if (user_id >= 0) {
			sb.append(" AND ");
			sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ID);
			sb.append(" = ").append(user_id);
		}
		return doQuery(sb.toString());
	}

	/* *
	 * select * from cpu_service where cs_id=$cs_id
	 */
	ResultSetWrapper queryService(int cs_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.CPU_SERVICE);
		sb.append(" WHERE ");
		sb.append(DBTableColName.CPU_SERVICE.ID);
		sb.append(" = ").append(cs_id);
		return doQuery(sb.toString());
	}

	/* *
	 * select distinct entry from cpu;
	 */
	ResultSetWrapper listColumnValues(String entry) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ").append(entry).append(" FROM ").append(DBTableName.CPU);
		return doQuery(sb.toString());
	}

	/* *
	 * select server_name, server_mark from server;
	 */
	ResultSetWrapper listServers() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableColName.SERVER.NAME).append(", ").append(DBTableColName.SERVER.MARK);
		sb.append(" FROM ").append(DBTableName.SERVER);
		return doQuery(sb.toString());
	}

	/* *
	 * update cpu_service set cs_life=(select datediff((select if($endtime >
	 * cs_starttime, $endtime, cs_starttime)), cs_starttime)),
	 * cs_state=$cs_state where cs_id=$cs_id and user_id=$user_id
	 */
	void modifyService(int cs_id, int user_id, String endtime, int cs_state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.CPU_SERVICE);
		sb.append(" SET ");

		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ");
		sb.append("(SELECT DATEDIFF((SELECT IF(");
		sb.append("\"").append(endtime).append("\"");
		sb.append(" > ").append(DBTableColName.CPU_SERVICE.STARTTIME).append(", ");
		sb.append("\"").append(endtime).append("\"").append(", ");
		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(")), ");
		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(")), ");

		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(cs_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
		if (user_id >= 0) {
			sb.append(" AND ");
			sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = ").append(user_id);
		}
		doUpdate(sb.toString());
	}

	/* *
	 * update cpu_service set user_id=(select user.user_id from account left
	 * join user on account.account_id=user.account_id where
	 * account.account_name=$account and user.user_name=$user),
	 * cs_starttime=$cs_starttime, cs_life=$cs_life, cs_state=$cs_state where
	 * cs_id=$cs_id
	 */
	void addService(int cs_id, String account, String user, String cs_starttime, int cs_life, int cs_state)
	        throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.CPU_SERVICE);
		sb.append(" SET ");

		sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = ").append("(");
		sb.append("SELECT ").append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).append(" FROM ");
		sb.append(DBTableName.USER).append(" LEFT JOIN ").append(DBTableName.ACCOUNT);
		sb.append(" ON ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID);
		sb.append(" = ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID);
		sb.append(" WHERE ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME);
		sb.append(" = ").append("\"").append(account).append("\"");
		sb.append(" AND ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.NAME);
		sb.append(" = ").append("\"").append(user).append("\"");
		sb.append("), ");

		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(" = ").append("\"").append(cs_starttime).append("\", ");
		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ").append(cs_life).append(", ");
		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(cs_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
		doUpdate(sb.toString());
	}

	/* *
	 * update cpu_service set cs_starttime="0000-00-00",
	 * cs_state=CPUState.RESERVED, cs_life=0, user_id=NULL where
	 * cs_id=$cs_id
	 */
	void deleteService(int cs_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.CPU_SERVICE);
		sb.append(" SET ");
		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(" = \"0000-00-00\"");
		sb.append(", ");
		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(CPUState.RESERVED.getValue());
		sb.append(", ");
		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ").append(0);
		sb.append(", ");
		sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = NULL");
		sb.append(" WHERE ");
		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
		doUpdate(sb.toString());
	}

	/* *
	 * delete cpu, cpu_service from cpu left join cpu_service on cpu.cpu_id=cpu_service.cpu_id
	 * where cpu_service.cs_state=CPUState.RESERVED and cpu.cpu_id=$cpu_id;
	 */
	void deleteDevice(int cpu_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ").append(DBTableName.CPU).append(", ").append(DBTableName.CPU_SERVICE);
		sb.append(" FROM ").append(DBTableName.CPU).append(" LEFT JOIN ").append(DBTableName.CPU_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID);
		sb.append(" = ");
		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.CPU_ID);
		sb.append(" WHERE ");
		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.STATE);
		sb.append(" = ");
		sb.append(CPUState.RESERVED.getValue());
		sb.append(" AND ");
		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID);
		sb.append(" = ");
		sb.append(cpu_id);
		doUpdate(sb.toString());
	}

	/* *
	 * insert into cpu (cpu_name, cpu_vendor, cpu_model, cpu_ghz, cpu_cache,
	 * server_id) values ("$name", "$vendor", "$model", ghz, cache, (select
	 * server_id from server where server_mark="$server_mark"))
	 * 
	 * insert into cpu_service (cs_starttime, cs_state, cs_life, cpu_id,
	 * user_id) values ("0000-00-00", CPUState.RESERVED, 0, (select
	 * last_insert_id()), NULL)
	 */
	void addDevice(String serverMark, String name, String vendor, String model, double ghz, double cache)
	        throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.CPU);
		sb.append(" (");
		sb.append(DBTableColName.CPU.NAME).append(", ");
		sb.append(DBTableColName.CPU.VENDOR).append(", ");
		sb.append(DBTableColName.CPU.MODEL).append(", ");
		sb.append(DBTableColName.CPU.GHZ).append(", ");
		sb.append(DBTableColName.CPU.CACHE).append(", ");
		sb.append(DBTableColName.CPU.SERVER_ID);
		sb.append(")");
		sb.append(" VALUES ");
		sb.append("(");
		sb.append("\"").append(name).append("\", ");
		sb.append("\"").append(vendor).append("\", ");
		sb.append("\"").append(model).append("\", ");
		sb.append(ghz).append(", ").append(cache).append(", ");
		sb.append("(SELECT ").append(DBTableColName.SERVER.ID).append(" FROM ");
		sb.append(DBTableName.SERVER).append(" WHERE ").append(DBTableColName.SERVER.MARK).append(" = ");
		sb.append("\"").append(serverMark).append("\"").append(")");
		sb.append(")");
		doUpdate(sb.toString());

		sb = new StringBuilder();
		sb.append("SELECT MAX(").append(DBTableColName.CPU.ID).append(") FROM ").append(DBTableName.CPU);

		ResultSetWrapper rsw = null;
		int cpu_id = -1;
		try {
			rsw = doQuery(sb.toString());
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				cpu_id = rs.getInt(1);
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
		assert (cpu_id != -1);

		sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.CPU_SERVICE);
		sb.append(" (");
		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(", ");
		sb.append(DBTableColName.CPU_SERVICE.STATE).append(", ");
		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(", ");
		sb.append(DBTableColName.CPU_SERVICE.CPU_ID).append(", ");
		sb.append(DBTableColName.CPU_SERVICE.USER_ID);
		sb.append(")");
		sb.append(" VALUES ");
		sb.append("(");
		sb.append("\"0000-00-00\", ").append(CPUState.RESERVED.getValue()).append(", ").append(0).append(", ");
		sb.append(cpu_id).append(", NULL");
		sb.append(")");
		doUpdate(sb.toString());
	}

}

public class DeviceCPUServiceProcImpl {

	private DeviceCPUDBProcWrapper dbproc = new DeviceCPUDBProcWrapper();

	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}

	public List<String> listAccounts(Session session) {
		try {
			LoginUserProfile user = getUser(session);
			if (!user.isSystemAdmin()) {
				return null;
			}
			return listAccounts();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<String> listUsersByAccount(Session session, String account) {
		try {
			if (account != null) {
				LoginUserProfile user = getUser(session);
				if (user.isSystemAdmin()) {
					return listUsersByAccount(account);
				}
			}
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<String> listAccounts() throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.listAccounts();
			List<String> list = new ArrayList<String>();
			ResultSet rs = rsw.getResultSet();
			while (rs.next()) {
				String account = rs.getString(DBTableColName.ACCOUNT.NAME);
				if (account != null) {
					list.add(account);
				}
			}
			return list;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
	}

	private List<String> listUsersByAccount(String account) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.listUsersByAccount(account);
			List<String> list = new ArrayList<String>();
			ResultSet rs = rsw.getResultSet();
			while (rs.next()) {
				String user = rs.getString(DBTableColName.USER.NAME);
				if (user != null) {
					list.add(user);
				}
			}
			return list;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
	}

	public SearchResult lookupCPU(Session session, String search, SearchRange range, int queryState) {
		ResultSetWrapper rsw = null;
		try {
			LoginUserProfile user = getUser(session);
			boolean isRoot = user.isSystemAdmin();

			List<SearchResultRow> rows;
			if (isRoot) {
				rsw = dbproc.queryAllCPUs(queryState, -1);
			}
			else {
				rsw = dbproc.queryAllCPUs(queryState, getUser(session).getUserId());
			}
			rows = convertResults(isRoot, rsw);

			if (rows != null) {
				int length = Math.min(range.getLength(), rows.size() - range.getStart());
				SearchResult result = new SearchResult(rows.size(), range);
				if (isRoot) {
					result.setDescs(FIELDS_ROOT);
				}
				else {
					result.setDescs(FIELDS_USER);
				}
				result.setRows(rows.subList(range.getStart(), range.getStart() + length));
				for (SearchResultRow row : result.getRows()) {
					System.out.println(row);
				}
				return result;
			}
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Map<Integer, Integer> getCPUCounts(Session session) {
		try {
			LoginUserProfile user = getUser(session);
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			if (user.isSystemAdmin()) {
				queryAllCPUCounts(map, -1);
			}
			else {
				queryAllCPUCounts(map, user.getUserId());
			}
			return map;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public SearchResultRow modifyService(Session session, SearchResultRow row, String sendtime, int state) {
		try {
			if (sendtime == null) {
				return null;
			}
			if (CPUState.getCPUState(state) == null || CPUState.getCPUState(state) == CPUState.RESERVED) {
				return null;
			}
			LoginUserProfile user = getUser(session);
			boolean isRoot = user.isSystemAdmin();
			int user_id = isRoot ? -1 : user.getUserId();
			int cs_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_CS_ID));
			modifyService(cs_id, user_id, formatter.format(formatter.parse(sendtime)), state);
			return lookupService(row, isRoot, cs_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private SearchResultRow lookupService(SearchResultRow row, boolean isRoot, int cs_id) throws Exception {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		String sstate = null;
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.queryService(cs_id);
			ResultSet rs = rsw.getResultSet();
			if (!rs.next()) {
				return null;
			}
			
			CPUState state = CPUState.getCPUState(rs.getInt(DBTableColName.CPU_SERVICE.STATE));
			if (state != null) {
				sstate = state.toString();
				if (state != CPUState.RESERVED) {
				Date starttime = rs.getDate(DBTableColName.CPU_SERVICE.STARTTIME);
				int life = rs.getInt(DBTableColName.CPU_SERVICE.LIFE);
				sstarttime = formatter.format(starttime);
				slife = Integer.toString(life);
				sremains = Integer.toString(calcRemainingDays(starttime, life));
				}
			}
		}
		catch (Exception e) {
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
		if (isRoot) {
			row.setField(TABLE_COL_INDEX_ROOT_STARTTIME, sstarttime);
			row.setField(TABLE_COL_INDEX_ROOT_LIFE, slife);
			row.setField(TABLE_COL_INDEX_ROOT_REMAINS, sremains);
			row.setField(TABLE_COL_INDEX_ROOT_STATE, sstate);
		}
		else {
			row.setField(TABLE_COL_INDEX_USER_STARTTIME, sstarttime);
			row.setField(TABLE_COL_INDEX_USER_LIFE, slife);
			row.setField(TABLE_COL_INDEX_USER_REMAINS, sremains);
			row.setField(TABLE_COL_INDEX_USER_STATE, sstate);
		}
		return row;
	}

	public List<SearchResultRow> deleteService(Session session, List<SearchResultRow> list) {
		try {
			if (list == null) {
				return null;
			}
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			List<SearchResultRow> result = new ArrayList<SearchResultRow>();
			for (SearchResultRow row : list) {
				int cs_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_CS_ID));
				if (!deleteService(cs_id)) {
					break;
				}
				result.add(row);
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<SearchResultRow> deleteDevice(Session session, List<SearchResultRow> list) {
		try {
			if (list != null) {
				LoginUserProfile user = getUser(session);
				if (!user.isSystemAdmin()) {
					return null;
				}
				List<SearchResultRow> result = new ArrayList<SearchResultRow>();
				for (SearchResultRow row : list) {
					int cpu_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_CPU_ID));
					if (!deleteDevice(cpu_id)) {
						break;
					}
					result.add(row);
				}
				return result;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public SearchResultRow addService(Session session, SearchResultRow row, String account, String user,
	        String sstarttime, int life, int state) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			if (row == null || account == null || user == null) {
				return null;
			}
			if (sstarttime == null || !(life >= 0)) {
				return null;
			}
			if (CPUState.getCPUState(state) == null || CPUState.getCPUState(state) == CPUState.RESERVED) {
				return null;
			}
			Date starttime = formatter.parse(sstarttime);
			int cs_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_CS_ID));
			addService(cs_id, account, user, sstarttime, life, state);
			int remains = calcRemainingDays(starttime, life);
			row.setField(TABLE_COL_INDEX_ROOT_ACCOUNT, account);
			row.setField(TABLE_COL_INDEX_ROOT_USER, user);
			row.setField(TABLE_COL_INDEX_ROOT_STARTTIME, sstarttime);
			row.setField(TABLE_COL_INDEX_ROOT_LIFE, Integer.toString(life));
			row.setField(TABLE_COL_INDEX_ROOT_REMAINS, Integer.toString(remains));
			row.setField(TABLE_COL_INDEX_ROOT_STATE, CPUState.getCPUState(state).toString());
			return row;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean addDevice(Session session, String serverMark, String name, String vendor, String model, double ghz,
	        double cache, int num) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return false;
			}
			if (serverMark == null || name == null || num <= 0) {
				return false;
			}
			if (vendor == null) {
				vendor = "";
			}
			if (model == null) {
				model = "";
			}
			addDevice(serverMark, name, vendor, model, ghz, cache, num);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public DataCache lookupDeviceInfo(Session session) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			DataCache cache = new DataCache();
			cache.cpuNameList = listColumnValues(DBTableColName.CPU.NAME);
			cache.cpuVendorList = listColumnValues(DBTableColName.CPU.VENDOR);
			cache.cpuModelList = listColumnValues(DBTableColName.CPU.MODEL);
			return listServers(cache);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private DataCache listServers(DataCache cache) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.listServers();
			ResultSet rs = rsw.getResultSet();
			List<String> nameList = new ArrayList<String>();
			List<String> markList = new ArrayList<String>();
			while (rs.next()) {
				nameList.add(rs.getString(DBTableColName.SERVER.NAME));
				markList.add(rs.getString(DBTableColName.SERVER.MARK));
			}
			cache.serverNameList = nameList;
			cache.serverMarkList = markList;
			return cache;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
	}

	private List<String> listColumnValues(String entry) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.listColumnValues(entry);
			ResultSet rs = rsw.getResultSet();
			List<String> list = new ArrayList<String>();
			while (rs.next()) {
				list.add(rs.getString(entry));
			}
			return list;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
	}

	private boolean deleteService(int cs_id) {
		try {
			dbproc.deleteService(cs_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void modifyService(int cs_id, int user_id, String endtime, int state) throws Exception {
		dbproc.modifyService(cs_id, user_id, endtime, state);
	}

	private void addService(int cs_id, String account, String user, String starttime, int life, int state)
	        throws Exception {
		dbproc.addService(cs_id, account, user, starttime, life, state);
	}

	private boolean deleteDevice(int cpu_id) {
		try {
			dbproc.deleteDevice(cpu_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void addDevice(String serverMark, String name, String vendor, String model, double ghz, double cache, int num) throws Exception {
		for (int i = 0; i < num; i ++) {
			dbproc.addDevice(serverMark, name, vendor, model, ghz, cache);
		}
	}

	private List<SearchResultRow> convertResults(boolean isRoot, ResultSetWrapper rsw) {
		if (rsw != null) {
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			try {
				int index = 1;
				while (rs.next()) {
					if (isRoot) {
						rows.add(convertRootResultRow(rs, index));
					}
					else {
						rows.add(convertUserResultRow(rs, index));
					}
					index ++;
				}
				return rows;
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				try {
					rsw.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	private SearchResultRow convertRootResultRow(ResultSet rs, int index) throws SQLException {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		String sstate = null;
		String account = null;
		String user = null;
		try {
			CPUState state = CPUState.getCPUState(rs.getInt(DBTableColName.CPU_SERVICE.STATE));
			if (state != null) {
				sstate = state.toString();
				if (state != CPUState.RESERVED) {
					Date starttime = rs.getDate(DBTableColName.CPU_SERVICE.STARTTIME);
					account = rs.getString(DBTableColName.ACCOUNT.NAME);
					user = rs.getString(DBTableColName.USER.NAME);
					int life = rs.getInt(DBTableColName.CPU_SERVICE.LIFE);
					sstarttime = formatter.format(starttime);
					slife = Integer.toString(life);
					sremains = Integer.toString(calcRemainingDays(starttime, life));
				}
			}
		}
		catch (Exception e) {
		}
		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.CPU_SERVICE.ID),
		        rs.getString(DBTableColName.CPU.ID), "", Integer.toString(index),
		        rs.getString(DBTableColName.SERVER.NAME), rs.getString(DBTableColName.CPU.VENDOR),
		        rs.getString(DBTableColName.CPU.MODEL), rs.getString(DBTableColName.CPU.GHZ),
		        rs.getString(DBTableColName.CPU.CACHE), account, user, sstarttime, slife, sremains, sstate));
	}

	private SearchResultRow convertUserResultRow(ResultSet rs, int index) throws SQLException {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		String sstate = null;
		try {
			CPUState state = CPUState.getCPUState(rs.getInt(DBTableColName.CPU_SERVICE.STATE));
			if (state != null) {
				sstate = state.toString();
				if (state != CPUState.RESERVED) {
					Date starttime = rs.getDate(DBTableColName.CPU_SERVICE.STARTTIME);
					int life = rs.getInt(DBTableColName.CPU_SERVICE.LIFE);
					sstarttime = formatter.format(starttime);
					slife = Integer.toString(life);
					sremains = Integer.toString(calcRemainingDays(starttime, life));
				}
			}
		}
		catch (Exception e) {
		}
		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.CPU_SERVICE.ID),
		        rs.getString(DBTableColName.CPU.ID), "", Integer.toString(index),
		        rs.getString(DBTableColName.SERVER.NAME), rs.getString(DBTableColName.CPU.VENDOR),
		        rs.getString(DBTableColName.CPU.MODEL), rs.getString(DBTableColName.CPU.GHZ),
		        rs.getString(DBTableColName.CPU.CACHE), sstarttime, slife, sremains, sstate));
	}

	private void queryAllCPUCounts(Map<Integer, Integer> map, int user_id) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.getCountsByState(user_id);
			ResultSet rs = rsw.getResultSet();
			int sum = 0;
			while (rs.next()) {
				int value = rs.getInt(2);
				sum += value;
				map.put(rs.getInt(1), value);
			}
			map.put(-1, sum);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
	}

	private int getNumDays(Date endtime, Date starttime) {
		return (int)((endtime.getTime() - starttime.getTime()) / (1000 * 60 * 60 * 24));
	}

	private int calcRemainingDays(Date starttime, int life) {
		int used = getNumDays(new Date(), starttime);
		int remains;
		if (used <= 0) {
			remains = life;
		}
		else {
			remains = life - used;
		}
		if (remains < 0) {
			remains = 0;
		}
		return remains;
	}

	private static final int LAN_SELECT = 1;

	public static final int TABLE_COL_INDEX_CS_ID = 0;
	public static final int TABLE_COL_INDEX_CPU_ID = 1;
	public static final int TABLE_COL_INDEX_CHECKBOX = 2;
	public static final int TABLE_COL_INDEX_NO = 3;
	public static final int TABLE_COL_INDEX_ROOT_ACCOUNT = 9;
	public static final int TABLE_COL_INDEX_ROOT_USER = 10;
	public static final int TABLE_COL_INDEX_ROOT_STARTTIME = 11;
	public static final int TABLE_COL_INDEX_ROOT_LIFE = 12;
	public static final int TABLE_COL_INDEX_ROOT_REMAINS = 13;
	public static final int TABLE_COL_INDEX_ROOT_STATE = 14;
	public static final int TABLE_COL_INDEX_USER_STARTTIME = 9;
	public static final int TABLE_COL_INDEX_USER_LIFE = 10;
	public static final int TABLE_COL_INDEX_USER_REMAINS = 11;
	public static final int TABLE_COL_INDEX_USER_STATE = 12;

	private static final String[] TABLE_COL_TITLE_CHECKBOX = {"", ""};
	private static final String[] TABLE_COL_TITLE_NO = {"", "序号"};
	private static final String[] TABLE_COL_TITLE_SERVER = {"", "服务器"};
	private static final String[] TABLE_COL_TITLE_VENDOR = {"", "厂家"};
	private static final String[] TABLE_COL_TITLE_MODEL = {"", "型号"};
	private static final String[] TABLE_COL_TITLE_GHZ = {"", "主频"};
	private static final String[] TABLE_COL_TITLE_CACHE = {"", "缓存"};
	private static final String[] TABLE_COL_TITLE_STARTTIME = {"", "开始时间"};
	private static final String[] TABLE_COL_TITLE_LIFE = {"", "服务期限"};
	private static final String[] TABLE_COL_TITLE_REMAINS = {"", "剩余时间"};
	private static final String[] TABLE_COL_TITLE_STATE = {"", "状态"};
	private static final String[] TABLE_COL_TITLE_ACCOUNT = {"", "账户"};
	private static final String[] TABLE_COL_TITLE_USER = {"", "用户"};

	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKBOX[LAN_SELECT], "4%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_SERVER[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_VENDOR[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_MODEL[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_GHZ[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_CACHE[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_USER[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "14%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_LIFE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_REMAINS[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STATE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));

	private static final List<SearchResultFieldDesc> FIELDS_USER = Arrays.asList(
			new SearchResultFieldDesc(null, "0%", false), new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKBOX[LAN_SELECT], "4%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_SERVER[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
            new SearchResultFieldDesc(TABLE_COL_TITLE_VENDOR[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_MODEL[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_GHZ[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_CACHE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "14%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_LIFE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_REMAINS[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STATE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));

}
