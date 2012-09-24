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
import com.eucalyptus.webui.client.activity.device.DeviceMemoryActivity.MemoryState;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceMemoryDeviceAddView.DataCache;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

class DeviceMemoryDBProcWrapper {

	private static final Logger LOG = Logger.getLogger(DeviceMemoryDBProcWrapper.class.getName());

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
	 * select ms_state, sum(ms_used) from mem_service where user_id=$user_id group
	 * by ms_state;
	 */
	ResultSetWrapper getCountsByState(int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableColName.MEM_SERVICE.STATE);
		sb.append(", sum(").append(DBTableColName.MEM_SERVICE.USED).append(") FROM ");
		sb.append(DBTableName.MEM_SERVICE);

		if (user_id >= 0) {
			sb.append(" WHERE ");
			sb.append(DBTableColName.MEM_SERVICE.USER_ID);
			sb.append(" = ").append(user_id);
		}

		sb.append(" GROUP BY ");
		sb.append(DBTableColName.MEM_SERVICE.STATE);
		return doQuery(sb.toString());
	}

	ResultSetWrapper queryAllMemorys(int queryState, int account_id, int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.MEMORY).append(" LEFT JOIN ").append(DBTableName.SERVER);
		sb.append(" ON ");
		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.SERVER_ID);
		sb.append(" = ");
		sb.append(DBTableName.SERVER).append(".").append(DBTableColName.SERVER.ID);
		sb.append(" LEFT JOIN ").append(DBTableName.MEM_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.ID);
		sb.append(" = ");
		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.MEM_ID);
		sb.append(" LEFT JOIN ").append(DBTableName.USER);
		sb.append(" ON ");
		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.USER_ID);
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
			sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.STATE);
			sb.append(" = ").append(queryState);
		}
		if (account_id >= 0) {
			sb.append(" AND ");
			sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID);
			sb.append(" = ").append(account_id);
		}
		if (user_id >= 0) {
			sb.append(" AND ");
			sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ID);
			sb.append(" = ").append(user_id);
		}
		return doQuery(sb.toString());
	}

	/* *
	 * select * from mem_service where mem_id=$mem_id
	 */
	ResultSetWrapper queryService(int mem_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.MEM_SERVICE);
		sb.append(" WHERE ");
		sb.append(DBTableColName.MEM_SERVICE.ID);
		sb.append(" = ").append(mem_id);
		return doQuery(sb.toString());
	}

	/* *
	 * select distinct entry from memory;
	 */
	ResultSetWrapper listColumnValues(String entry) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ").append(entry).append(" FROM ").append(DBTableName.MEMORY);
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

	void modifyService(int ms_id, String endtime, int ms_state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.MEM_SERVICE);
		sb.append(" SET ");

		sb.append(DBTableColName.MEM_SERVICE.LIFE).append(" = ");
		sb.append("(SELECT DATEDIFF((SELECT IF(");
		sb.append("\"").append(endtime).append("\"");
		sb.append(" > ").append(DBTableColName.MEM_SERVICE.STARTTIME).append(", ");
		sb.append("\"").append(endtime).append("\"").append(", ");
		sb.append(DBTableColName.MEM_SERVICE.STARTTIME).append(")), ");
		sb.append(DBTableColName.MEM_SERVICE.STARTTIME).append(")), ");

		sb.append(DBTableColName.MEM_SERVICE.STATE).append(" = ").append(ms_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.MEM_SERVICE.ID).append(" = ").append(ms_id);
		doUpdate(sb.toString());
	}
	
	void updateServiceState(int ms_id, int ms_state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.MEM_SERVICE);
		sb.append(" SET ");

		sb.append(DBTableColName.MEM_SERVICE.STATE).append(" = ").append(ms_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.MEM_SERVICE.ID).append(" = ").append(ms_id);
		doUpdate(sb.toString());
	}

	/* *
	 * insert into mem_service (ms_used, ms_starttime, ms_life, ms_state, user_id, mem_id)
	 * values (0, $ms_starttime, $ms_life, $ms_state, (select user.user_id from account
	 * left join user on account.account_id=user.account_id where account.account_name=$account and
	 * user.user_name=$user), $mem_id) 
	 * 
	 * update mem_service A, mem_service B set A.ms_used=$ms_used, B.ms_used=(B.ms_used-$ms_used)
	 * where A.ms_id=$new_ms_id and B.ms_id=$ms_id and B.ms_used>=$ms_used;
	 */
	void addService(int ms_id, int mem_id, String account, String user, long ms_used, String ms_starttime,
			int ms_life, int ms_state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.MEM_SERVICE).append(" (");
		sb.append(DBTableColName.MEM_SERVICE.USED).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.STARTTIME).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.LIFE).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.STATE).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.USER_ID).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.MEM_ID).append(") VALUES (");
		sb.append(0).append(", ");
		sb.append("\"").append(ms_starttime).append("\", ");
		sb.append(ms_life).append(", ");
		sb.append(ms_state).append(", ");
		sb.append("(SELECT ").append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).append(" FROM ");
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
		sb.append(" = ").append("\"").append(user).append("\"), ");
		sb.append(mem_id).append(")");
		doUpdate(sb.toString());
		
		sb = new StringBuilder();
		sb.append("SELECT MAX(").append(DBTableColName.MEM_SERVICE.ID).append(") FROM ").append(DBTableName.MEM_SERVICE);
		
		ResultSetWrapper rsw = null;
		int new_ms_id = -1;
		try {
			rsw = doQuery(sb.toString());
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				new_ms_id = rs.getInt(1);
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
		assert (new_ms_id != -1);
		
		sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.MEM_SERVICE).append(" A, ");
		sb.append(DBTableName.MEM_SERVICE).append(" B");
		sb.append(" SET ");
		sb.append("A.").append(DBTableColName.MEM_SERVICE.USED).append(" = ").append(ms_used).append(", ");
		sb.append("B.").append(DBTableColName.MEM_SERVICE.USED).append(" = ");
		sb.append("(B.").append(DBTableColName.MEM_SERVICE.USED).append(" - ").append(ms_used).append(")");
		sb.append(" WHERE ");
		sb.append("A.").append(DBTableColName.MEM_SERVICE.ID).append(" = ").append(new_ms_id);
		sb.append(" AND ");
		sb.append("B.").append(DBTableColName.MEM_SERVICE.ID).append(" = ").append(ms_id);
		sb.append(" AND ");
		sb.append("A.").append(DBTableColName.MEM_SERVICE.MEM_ID).append(" = ");
		sb.append("B.").append(DBTableColName.MEM_SERVICE.MEM_ID);
		sb.append(" AND ");
		sb.append("B.").append(DBTableColName.MEM_SERVICE.USED).append(" >= ").append(ms_used);
		doUpdate(sb.toString());
		
		cleanupService();
	}
	
	/* *
	 * delete from mem_service where ms_used=0 and ms_state != MemoryState.RESERVED
	 */
	private void cleanupService() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(DBTableName.MEM_SERVICE);
		sb.append(" WHERE ");
		sb.append(DBTableColName.MEM_SERVICE.USED).append(" = ").append(0);
		sb.append(" AND ");
		sb.append(DBTableColName.MEM_SERVICE.STATE).append(" != ").append(MemoryState.RESERVED.getValue());
		doUpdate(sb.toString());
	}
	
	/* *
	 * update mem_service A, mem_service B set A.ms_used=(A.ms_used + B.ms_used), B.ms_used=0
	 * where A.mem_id=B.mem_id and A.ms_state=MemoryState.RESERVED and B.ms_id=$ms_id
	 * 
	 * delete from mem_service where ms_used=0
	 */
	void deleteService(int ms_id, int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.MEM_SERVICE).append(" A").append(", ");
		sb.append(DBTableName.MEM_SERVICE).append(" B");
		sb.append(" SET ");
		sb.append("A.").append(DBTableColName.MEM_SERVICE.USED).append(" = ");
		sb.append("(").append("A.").append(DBTableColName.MEM_SERVICE.USED).append(" + B.").append(DBTableColName.MEM_SERVICE.USED).append("),");
		sb.append("B.").append(DBTableColName.MEM_SERVICE.USED).append(" = 0");
		sb.append(" WHERE ");
		sb.append("A.").append(DBTableColName.MEM_SERVICE.MEM_ID);
		sb.append(" = ");
		sb.append("B.").append(DBTableColName.MEM_SERVICE.MEM_ID);
		sb.append(" AND ");
		sb.append("A.").append(DBTableColName.MEM_SERVICE.STATE);
		sb.append(" = ").append(MemoryState.RESERVED.getValue());
		sb.append(" AND ");
		sb.append("B.").append(DBTableColName.MEM_SERVICE.ID).append(" = ").append(ms_id);
		doUpdate(sb.toString());
		
		cleanupService();
	}

	/* *
	 * delete memory, mem_service from memory left join mem_service on memory.mem_id=mem_service.mem_id where
	 * memory.mem_total=mem_service.ms_used and mem_service.ms_state=MemoryState.RESERVED and memory.mem_id=$mem_id;
	 */
	void deleteDevice(int mem_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ").append(DBTableName.MEMORY).append(", ").append(DBTableName.MEM_SERVICE);
		sb.append(" FROM ").append(DBTableName.MEMORY).append(" LEFT JOIN ").append(DBTableName.MEM_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.ID);
		sb.append(" = ");
		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.MEM_ID);
		sb.append(" WHERE ");
		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.TOTAL);
		sb.append(" = ");
		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.USED);
		sb.append(" AND ");
		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.STATE);
		sb.append(" = ");
		sb.append(MemoryState.RESERVED.getValue());
		sb.append(" AND ");
		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.ID);
		sb.append(" = ");
		sb.append(mem_id);
		doUpdate(sb.toString());
	}

	/* *
	 * insert into memory (mem_name, mem_total, server_id) values ("$name",$total,
	 * (select server_id from server where server_mark="$server_mark"))
	 * 
	 * insert into mem_service (ms_used, ms_starttime, ms_state, ms_life, mem_id,
	 * user_id) values ($total, "0000-00-00", MemoryState.RESERVED, 0, (select
	 * last_insert_id()), NULL)
	 */
	void addDevice(String serverMark, String name, long total) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.MEMORY);
		sb.append(" (");
		sb.append(DBTableColName.MEMORY.NAME).append(", ");
		sb.append(DBTableColName.MEMORY.TOTAL).append(", ");
		sb.append(DBTableColName.MEMORY.SERVER_ID);
		sb.append(")");
		sb.append(" VALUES ");
		sb.append("(");
		sb.append("\"").append(name).append("\", ").append(total).append(", ");
		sb.append("(SELECT ").append(DBTableColName.SERVER.ID).append(" FROM ");
		sb.append(DBTableName.SERVER).append(" WHERE ").append(DBTableColName.SERVER.MARK).append(" = ");
		sb.append("\"").append(serverMark).append("\"").append(")");
		sb.append(")");
		doUpdate(sb.toString());

		sb = new StringBuilder();
		sb.append("SELECT MAX(").append(DBTableColName.MEMORY.ID).append(") FROM ").append(DBTableName.MEMORY);

		ResultSetWrapper rsw = null;
		int mem_id = -1;
		try {
			rsw = doQuery(sb.toString());
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				mem_id = rs.getInt(1);
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
		assert (mem_id != -1);

		sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.MEM_SERVICE);
		sb.append(" (");
		sb.append(DBTableColName.MEM_SERVICE.USED).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.STARTTIME).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.STATE).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.LIFE).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.MEM_ID).append(", ");
		sb.append(DBTableColName.MEM_SERVICE.USER_ID);
		sb.append(")");
		sb.append(" VALUES ");
		sb.append("(").append(total).append(", ");
		sb.append("\"0000-00-00\", ").append(MemoryState.RESERVED.getValue()).append(", ").append(0).append(", ");
		sb.append(mem_id).append(", NULL");
		sb.append(")");
		doUpdate(sb.toString());
	}

}

public class DeviceMemoryServiceProcImpl {

	private DeviceMemoryDBProcWrapper dbproc = new DeviceMemoryDBProcWrapper();

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

	public SearchResult lookupMemory(Session session, String search, SearchRange range, int queryState) {
		ResultSetWrapper rsw = null;
		try {
			LoginUserProfile user = getUser(session);
			int account_id = user.getAccountId();
			int user_id = user.getUserId();

			List<SearchResultRow> rows;
			if (user.isSystemAdmin()) {
				rsw = dbproc.queryAllMemorys(queryState, -1, -1);
			}
			else if (user.isAccountAdmin()) {
				rsw = dbproc.queryAllMemorys(queryState, account_id, -1);
			}
			else {
				rsw = dbproc.queryAllMemorys(queryState, account_id, user_id);
			}
			rows = convertResults(rsw);

			if (rows != null) {
				int length = Math.min(range.getLength(), rows.size() - range.getStart());
				SearchResult result = new SearchResult(rows.size(), range);
				result.setDescs(FIELDS);
				int from = range.getStart(), to = range.getStart() + length;
				if (from < to) {
					result.setRows(rows.subList(from, to));
				}
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

	public Map<Integer, Long> getMemoryCounts(Session session) {
		try {
			LoginUserProfile user = getUser(session);
			Map<Integer, Long> map = new HashMap<Integer, Long>();
			if (user.isSystemAdmin()) {
				queryAllMemoryCounts(map, -1);
			}
			else {
				queryAllMemoryCounts(map, user.getUserId());
			}
			return map;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public SearchResultRow modifyService(Session session, SearchResultRow row, String sendtime, int state) {
		try {
			if (isEmpty(sendtime)) {
				return null;
			}
			if (MemoryState.getMemoryState(state) == null || MemoryState.getMemoryState(state) == MemoryState.RESERVED) {
				return null;
			}
			LoginUserProfile user = getUser(session);
			if (!user.isSystemAdmin()) {
				return null;
			}
			int ms_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_MS_ID));
			modifyService(ms_id, formatter.format(formatter.parse(sendtime)), state);
			return lookupService(row, ms_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateServiceState(int ms_id, int cs_state) throws EucalyptusServiceException {
		try {
			dbproc.updateServiceState(ms_id, cs_state);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "更新Memory服务状态失败"));
		}
	}

	private SearchResultRow lookupService(SearchResultRow row, int ms_id) throws Exception {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		String sused = null;
		String sstate = null;
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.queryService(ms_id);
			ResultSet rs = rsw.getResultSet();
			if (!rs.next()) {
				return null;
			}
			long used = Long.parseLong(rs.getString(DBTableColName.MEM_SERVICE.USED));
			if (used != 0) {
				sused = Long.toString(used);
			}
			MemoryState state = MemoryState.getMemoryState(rs.getInt(DBTableColName.MEM_SERVICE.STATE));
			if (state != null) {
				sstate = state.toString();
				if (state != MemoryState.RESERVED) {
					Date starttime = rs.getDate(DBTableColName.MEM_SERVICE.STARTTIME);
					int life = rs.getInt(DBTableColName.MEM_SERVICE.LIFE);
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
		row.setField(TABLE_COL_INDEX_USED, sused);
		row.setField(TABLE_COL_INDEX_STARTTIME, sstarttime);
		row.setField(TABLE_COL_INDEX_LIFE, slife);
		row.setField(TABLE_COL_INDEX_REMAINS, sremains);
		row.setField(TABLE_COL_INDEX_STATE, sstate);
		return row;
	}

	public List<SearchResultRow> deleteService(Session session, List<SearchResultRow> list) {
		try {
			if (list == null) {
				return null;
			}
			LoginUserProfile user = getUser(session);
			if (!user.isSystemAdmin()) {
				return null;
			}
			List<SearchResultRow> result = new ArrayList<SearchResultRow>();
			for (SearchResultRow row : list) {
				int ms_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_MS_ID));
				if (!deleteService(ms_id, user.getUserId())) {
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
					int mem_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_MEM_ID));
					if (!deleteDevice(mem_id)) {
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
	        long used, String sstarttime, int life, int state) {
		try {
			if (row == null || !getUser(session).isSystemAdmin()) {
				return null;
			}
			if (isEmpty(account) || isEmpty(user)) {
				return null;
			}
			if (isEmpty(sstarttime) || !(life >= 0)) {
				return null;
			}
			if (MemoryState.getMemoryState(state) == null || MemoryState.getMemoryState(state) == MemoryState.RESERVED) {
				return null;
			}
			int ms_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_MS_ID));
			int mem_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_MEM_ID));
			updateService(ms_id, mem_id, account, user, used, sstarttime, life, state);
			return lookupService(row, ms_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean addDevice(Session session, String serverMark, String name, long total, int num) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return false;
			}
			if (isEmpty(serverMark) || isEmpty(name) || total <= 0 || num <= 0) {
				return false;
			}
			addDevice(serverMark, name, total, num);
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
			cache.memoryNameList = listColumnValues(DBTableColName.MEMORY.NAME);
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

	private boolean deleteService(int ms_id, int user_id) {
		try {
			dbproc.deleteService(ms_id, user_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void modifyService(int ms_id, String endtime, int state) throws Exception {
		dbproc.modifyService(ms_id, endtime, state);
	}

	private void updateService(int ms_id, int mem_id, String account, String user, long used, String starttime,
			int life, int state) throws Exception {
		dbproc.addService(ms_id, mem_id, account, user, used, starttime, life, state);
	}

	private boolean deleteDevice(int mem_id) {
		try {
			dbproc.deleteDevice(mem_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void addDevice(String serverMark, String name, long total, int num) throws Exception {
		for (int i = 0; i < num; i ++) {
			dbproc.addDevice(serverMark, name, total);
		}
	}

	private List<SearchResultRow> convertResults(ResultSetWrapper rsw) {
		if (rsw != null) {
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			try {
				int index = 1;
				while (rs.next()) {
					rows.add(convertResultRow(rs, index ++));
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
	
	private SearchResultRow convertResultRow(ResultSet rs, int index) throws SQLException {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		String sstate = null;
		String stotal = null;
		String sused = null;
		String account = null;
		String user = null;
		try {
			long used = Long.parseLong(rs.getString(DBTableColName.MEM_SERVICE.USED));
			if (used == 0) {
				return null;
			}
			sused = Long.toString(used);
			stotal = rs.getString(DBTableColName.MEMORY.TOTAL);
			MemoryState state = MemoryState.getMemoryState(rs.getInt(DBTableColName.MEM_SERVICE.STATE));
			if (state != null) {
				sstate = state.toString();
				if (state != MemoryState.RESERVED) {
					Date starttime = rs.getDate(DBTableColName.MEM_SERVICE.STARTTIME);
					account = rs.getString(DBTableColName.ACCOUNT.NAME);
					user = rs.getString(DBTableColName.USER.NAME);
					int life = rs.getInt(DBTableColName.MEM_SERVICE.LIFE);
					sstarttime = formatter.format(starttime);
					slife = Integer.toString(life);
					sremains = Integer.toString(calcRemainingDays(starttime, life));
				}
			}
		}
		catch (Exception e) {
		}
		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.MEM_SERVICE.ID),
		        rs.getString(DBTableColName.MEMORY.ID), "", Integer.toString(index),
		        rs.getString(DBTableColName.SERVER.NAME), stotal, sused,
		        account, user, sstarttime, slife, sremains, sstate));
	}

	private void queryAllMemoryCounts(Map<Integer, Long> map, int user_id) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.getCountsByState(user_id);
			ResultSet rs = rsw.getResultSet();
			long sum = 0;
			while (rs.next()) {
				long value = rs.getLong(2);
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
	
	private static final int TABLE_COL_INDEX_MS_ID = 0;
	private static final int TABLE_COL_INDEX_MEM_ID = 1;
	private static final int TABLE_COL_INDEX_USED = 6;
	private static final int TABLE_COL_INDEX_STARTTIME = 9;
	private static final int TABLE_COL_INDEX_LIFE = 10;
	private static final int TABLE_COL_INDEX_REMAINS = 11;
	private static final int TABLE_COL_INDEX_STATE = 12;
	
	private static final String[] TABLE_COL_TITLE_CHECKBOX = {"", ""};
	private static final String[] TABLE_COL_TITLE_NO = {"", "序号"};
	private static final String[] TABLE_COL_TITLE_SERVER = {"", "服务器"};
	private static final String[] TABLE_COL_TITLE_TOTAL = {"", "总大小(MB)"};
	private static final String[] TABLE_COL_TITLE_USED = {"", "此项大小(MB)"};
	private static final String[] TABLE_COL_TITLE_STARTTIME = {"", "开始时间"};
	private static final String[] TABLE_COL_TITLE_LIFE = {"", "服务期限"};
	private static final String[] TABLE_COL_TITLE_REMAINS = {"", "剩余时间"};
	private static final String[] TABLE_COL_TITLE_STATE = {"", "状态"};
	private static final String[] TABLE_COL_TITLE_ACCOUNT = {"", "账户"};
	private static final String[] TABLE_COL_TITLE_USER = {"", "用户"};
	
	private static final List<SearchResultFieldDesc> FIELDS = Arrays.asList(
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKBOX[LAN_SELECT], "4%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_SERVER[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_TOTAL[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_USED[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_USER[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "14%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_LIFE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_REMAINS[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STATE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));

}
