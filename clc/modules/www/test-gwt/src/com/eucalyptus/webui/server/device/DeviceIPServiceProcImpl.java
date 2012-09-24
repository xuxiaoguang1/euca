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
import com.eucalyptus.webui.client.activity.device.DeviceIPActivity.IPState;
import com.eucalyptus.webui.client.activity.device.DeviceIPActivity.IPType;
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
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

class DeviceIPDBProcWrapper {

	private static final Logger LOG = Logger.getLogger(DeviceIPDBProcWrapper.class.getName());

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
	 * select * from vm left join vm_service on vm.vm_id=vm_service.vm_id
	 * left join user on vm_service.user_id=user.user_id
	 * left join account on user.account_id=account.account_id
	 * where account.account_name=$account and user.user_name=$user 
	 */
	ResultSetWrapper listVMsByUser(String account, String user) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.VM).append(" LEFT JOIN ").append(DBTableName.VM_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.VM).append(".").append(DBTableColName.VM.ID);
		sb.append(" = ");
		sb.append(DBTableName.VM_SERVICE).append(".").append(DBTableColName.VM_SERVICE.VM_ID);
		sb.append(" LEFT JOIN ").append(DBTableName.USER);
		sb.append(" ON ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ID);
		sb.append(" = ");
		sb.append(DBTableName.VM_SERVICE).append(".").append(DBTableColName.VM_SERVICE.USER_ID);
		sb.append(" LEFT JOIN ").append(DBTableName.ACCOUNT);
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
		return doQuery(sb.toString());
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
	 * select is_state, count(*) from ip left join ip_service on ip.ip_id=ip_service.ip_id
	 * where user_id=$user_id and ip.ip_type=$type group by is_state;
	 */
	ResultSetWrapper getCountsByState(int queryType, int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableColName.IP_SERVICE.STATE);
		sb.append(", count(*) FROM ");
		sb.append(DBTableName.IP).append(" LEFT JOIN ").append(DBTableName.IP_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.IP_ID);
		
		sb.append(" WHERE 1=1");

		if (user_id >= 0) {
			sb.append(" AND ");
			sb.append(DBTableColName.IP_SERVICE.USER_ID);
			sb.append(" = ").append(user_id);
		}
		if (queryType >= 0) {
			sb.append(" AND ");
			sb.append(DBTableColName.IP.TYPE);
			sb.append(" = ").append(queryType);
		}

		sb.append(" GROUP BY ");
		sb.append(DBTableColName.IP_SERVICE.STATE);
		return doQuery(sb.toString());
	}

	/* *
	 * select * from ip left join ip_service on ip.ip_id=ip_service.ip_id
	 * left join vm on ip_service.vm_id=vm.vm_id left join user on
	 * ip_service.user_id=user.user_id left join account on
	 * user.account_id=account.id where 1=1 and ip_service.is_state=$queryState
	 * and user.id=$user_id
	 */
	ResultSetWrapper queryAllIPs(int queryState, int queryType, int account_id, int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.IP).append(" LEFT JOIN ").append(DBTableName.IP_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.IP_ID);
		sb.append(" LEFT JOIN ").append(DBTableName.VM);
		sb.append(" ON ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.VM_ID);
		sb.append(" = ");
		sb.append(DBTableName.VM).append(".").append(DBTableColName.VM.ID);
		sb.append(" LEFT JOIN ").append(DBTableName.USER);
		sb.append(" ON ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.USER_ID);
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
			sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.STATE);
			sb.append(" = ").append(queryState);
		}
		if (queryType >= 0) {
			sb.append(" AND ");
			sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.TYPE);
			sb.append(" = ").append(queryType);
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
	 * select * from ip_service where is_id=$is_id
	 */
	ResultSetWrapper queryService(int is_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.IP_SERVICE);
		sb.append(" WHERE ");
		sb.append(DBTableColName.IP_SERVICE.ID);
		sb.append(" = ").append(is_id);
		return doQuery(sb.toString());
	}

	/* *
	 * select distinct entry from ip;
	 */
	ResultSetWrapper listColumnValues(String entry) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ").append(entry).append(" FROM ").append(DBTableName.IP);
		return doQuery(sb.toString());
	}

	void modifyService(int is_id, String endtime, int is_state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.IP_SERVICE);
		sb.append(" SET ");

		sb.append(DBTableColName.IP_SERVICE.LIFE).append(" = ");
		sb.append("(SELECT DATEDIFF((SELECT IF(");
		sb.append("\"").append(endtime).append("\"");
		sb.append(" > ").append(DBTableColName.IP_SERVICE.STARTTIME).append(", ");
		sb.append("\"").append(endtime).append("\"").append(", ");
		sb.append(DBTableColName.IP_SERVICE.STARTTIME).append(")), ");
		sb.append(DBTableColName.IP_SERVICE.STARTTIME).append(")), ");

		sb.append(DBTableColName.IP_SERVICE.STATE).append(" = ").append(is_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.IP_SERVICE.ID).append(" = ").append(is_id);
		doUpdate(sb.toString());
	}
	
	void updateServiceState(int is_id, int is_state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.IP_SERVICE);
		sb.append(" SET ");

		sb.append(DBTableColName.IP_SERVICE.STATE).append(" = ").append(is_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.IP_SERVICE.ID).append(" = ").append(is_id);
		doUpdate(sb.toString());
	}

	/* *
	 * update ip_service set vm_id=(select vm.vm_id from vm where vm.mark=$vmMark), 
	 * user_id=(select user.user_id from account left
	 * join user on account.account_id=user.account_id where
	 * account.account_name=$account and user.user_name=$user),
	 * is_starttime=$is_starttime, is_life=$is_life, is_state=$is_state where
	 * is_id=$is_id
	 */
	void addService(int is_id, String account, String user, String vmMark, String is_starttime, int is_life, int is_state)
	        throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.IP_SERVICE);
		sb.append(" SET ");
		
		sb.append(DBTableColName.IP_SERVICE.VM_ID).append(" = ").append("(");
		sb.append("SELECT ").append(DBTableName.VM).append(".").append(DBTableColName.VM.ID);
		sb.append(" FROM ").append(DBTableName.VM).append(" WHERE ");
		sb.append(DBTableName.VM).append(".").append(DBTableColName.VM.MARK);
		sb.append(" = ").append("\"").append(vmMark).append("\"").append("), ");

		sb.append(DBTableColName.IP_SERVICE.USER_ID).append(" = ").append("(");
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

		sb.append(DBTableColName.IP_SERVICE.STARTTIME).append(" = ").append("\"").append(is_starttime).append("\", ");
		sb.append(DBTableColName.IP_SERVICE.LIFE).append(" = ").append(is_life).append(", ");
		sb.append(DBTableColName.IP_SERVICE.STATE).append(" = ").append(is_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.IP_SERVICE.ID).append(" = ").append(is_id);
		doUpdate(sb.toString());
	}

	/* *
	 * update ip_service set is_starttime="0000-00-00",
	 * is_state=IPState.RESERVED, is_life=0, user_id=NULL vm_id=NULL where
	 * is_id=$is_id
	 */
	void deleteService(int is_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.IP_SERVICE);
		sb.append(" SET ");
		sb.append(DBTableColName.IP_SERVICE.STARTTIME).append(" = \"0000-00-00\"");
		sb.append(", ");
		sb.append(DBTableColName.IP_SERVICE.STATE).append(" = ").append(IPState.RESERVED.getValue());
		sb.append(", ");
		sb.append(DBTableColName.IP_SERVICE.LIFE).append(" = ").append(0);
		sb.append(", ");
		sb.append(DBTableColName.IP_SERVICE.USER_ID).append(" = NULL");
		sb.append(", ");
		sb.append(DBTableColName.IP_SERVICE.VM_ID).append(" = NULL");
		sb.append(" WHERE ");
		sb.append(DBTableColName.IP_SERVICE.ID).append(" = ").append(is_id);
		doUpdate(sb.toString());
	}

	/* *
	 * delete ip, ip_service from ip, ip_service where ip.ip_id=ip_service.ip_id
	 * ip_service.is_state=IPState.RESERVED and ip.ip_id=$ip_id;
	 */
	void deleteDevice(int ip_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ").append(DBTableName.IP).append(", ").append(DBTableName.IP_SERVICE);
		sb.append(" FROM ").append(DBTableName.IP).append(", ").append(DBTableName.IP_SERVICE);
		sb.append(" WHERE ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.IP_ID);
		sb.append(" AND ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.STATE);
		sb.append(" = ");
		sb.append(IPState.RESERVED.getValue());
		sb.append(" AND ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(ip_id);
		doUpdate(sb.toString());
	}

	/* *
	 * insert into ip (ip_addr, ip_type) values ($ip, $type)
	 * 
	 * insert into ip_service (is_starttime, is_state, is_life, ip_id, vm_id,
	 * user_id) values ("0000-00-00", IPState.RESERVED, 0, (select
	 * last_insert_id()), NULL, NULL)
	 */
	void addDevice(String ip, int type) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.IP);
		sb.append(" (");
		sb.append(DBTableColName.IP.ADDR).append(", ");
		sb.append(DBTableColName.IP.TYPE);
		sb.append(")");
		sb.append(" VALUES ");
		sb.append("(");
		sb.append("\"").append(ip).append("\", ");
		sb.append(type).append(")");
		doUpdate(sb.toString());

		sb = new StringBuilder();
		sb.append("SELECT MAX(").append(DBTableColName.IP.ID).append(") FROM ").append(DBTableName.IP);

		ResultSetWrapper rsw = null;
		int ip_id = -1;
		try {
			rsw = doQuery(sb.toString());
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				ip_id = rs.getInt(1);
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
		assert (ip_id != -1);

		sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.IP_SERVICE);
		sb.append(" (");
		sb.append(DBTableColName.IP_SERVICE.STARTTIME).append(", ");
		sb.append(DBTableColName.IP_SERVICE.STATE).append(", ");
		sb.append(DBTableColName.IP_SERVICE.LIFE).append(", ");
		sb.append(DBTableColName.IP_SERVICE.IP_ID).append(", ");
		sb.append(DBTableColName.IP_SERVICE.VM_ID).append(", ");
		sb.append(DBTableColName.IP_SERVICE.USER_ID);
		sb.append(")");
		sb.append(" VALUES ");
		sb.append("(");
		sb.append("\"0000-00-00\", ").append(IPState.RESERVED.getValue()).append(", ").append(0).append(", ");
		sb.append(ip_id).append(", NULL, NULL");
		sb.append(")");
		doUpdate(sb.toString());
	}

}

public class DeviceIPServiceProcImpl {

	private DeviceIPDBProcWrapper dbproc = new DeviceIPDBProcWrapper();

	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	public List<String> listVMsByUser(Session session, String account, String user) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			if (isEmpty(account) || isEmpty(user)) {
				return null;
			}
			return listVMsByUser(account, user);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<String> listAccounts(Session session) {
		try {
			if (!getUser(session).isSystemAdmin()) {
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
			if (!isEmpty(account)) {
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
	
	private List<String> listVMsByUser(String account, String user) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.listVMsByUser(account, user);
			List<String> list = new ArrayList<String>();
			ResultSet rs = rsw.getResultSet();
			while (rs.next()) {
				String mark = rs.getString(DBTableColName.VM.MARK);
				if (mark != null) {
					list.add(mark);
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

	public SearchResult lookupIP(Session session, String search, SearchRange range, int queryState, int queryType) {
		ResultSetWrapper rsw = null;
		try {
			LoginUserProfile user = getUser(session);
			int account_id = user.getAccountId();
			int user_id = user.getUserId();

			List<SearchResultRow> rows;
			if (user.isSystemAdmin()) {
				rsw = dbproc.queryAllIPs(queryState, queryType, -1, -1);
			}
			else if (user.isAccountAdmin()) {
				rsw = dbproc.queryAllIPs(queryState, queryType, account_id, -1);
			}
			else {
				rsw = dbproc.queryAllIPs(queryState, queryType, account_id, user_id);
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

	public Map<Integer, Integer> getIPCounts(Session session, int queryType) {
		try {
			LoginUserProfile user = getUser(session);
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			if (user.isSystemAdmin()) {
				queryAllIPCounts(queryType, map, -1);
			}
			else {
				queryAllIPCounts(queryType, map, user.getUserId());
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
			if (isEmpty(sendtime)) {
				return null;
			}
			if (IPState.getIPState(state) == null || IPState.getIPState(state) == IPState.RESERVED) {
				return null;
			}
			LoginUserProfile user = getUser(session);
			if (!user.isSystemAdmin()) {
				return null;
			}
			int is_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_IS_ID));
			modifyService(is_id, formatter.format(formatter.parse(sendtime)), state);
			return lookupService(row, is_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateServiceState(int is_id, int cs_state) throws EucalyptusServiceException {
		try {
			dbproc.updateServiceState(is_id, cs_state);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "更新IP状态失败"));
		}
	}

	private SearchResultRow lookupService(SearchResultRow row, int is_id) throws Exception {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		String sstate = null;
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.queryService(is_id);
			ResultSet rs = rsw.getResultSet();
			if (!rs.next()) {
				return null;
			}
			
			IPState state = IPState.getIPState(rs.getInt(DBTableColName.IP_SERVICE.STATE));
			if (state != null) {
				sstate = state.toString();
				if (state != IPState.RESERVED) {
				Date starttime = rs.getDate(DBTableColName.IP_SERVICE.STARTTIME);
				int life = rs.getInt(DBTableColName.IP_SERVICE.LIFE);
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
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			List<SearchResultRow> result = new ArrayList<SearchResultRow>();
			for (SearchResultRow row : list) {
				int is_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_IS_ID));
				if (!deleteService(is_id)) {
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
					int ip_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_IP_ID));
					if (!deleteDevice(ip_id)) {
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
	        String vmMark, String sstarttime, int life, int state) {
		try {
			if (!getUser(session).isSystemAdmin() || row == null) {
				return null;
			}
			if (isEmpty(account) || isEmpty(user) || isEmpty(vmMark)) {
				return null;
			}
			if (isEmpty(sstarttime) || !(life >= 0)) {
				return null;
			}
			if (IPState.getIPState(state) == null || IPState.getIPState(state) == IPState.RESERVED) {
				return null;
			}
			Date starttime = formatter.parse(sstarttime);
			int is_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_IS_ID));
			addService(is_id, account, user, vmMark, sstarttime, life, state);
			int remains = calcRemainingDays(starttime, life);
			row.setField(TABLE_COL_INDEX_VM, vmMark);
			row.setField(TABLE_COL_INDEX_ACCOUNT, account);
			row.setField(TABLE_COL_INDEX_USER, user);
			row.setField(TABLE_COL_INDEX_STARTTIME, sstarttime);
			row.setField(TABLE_COL_INDEX_LIFE, Integer.toString(life));
			row.setField(TABLE_COL_INDEX_REMAINS, Integer.toString(remains));
			row.setField(TABLE_COL_INDEX_STATE, IPState.getIPState(state).toString());
			return row;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean addDevice(Session session, List<String> publicList, List<String> privateList) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return false;
			}
			if (publicList == null && privateList == null) {
				return false;
			}
			if (publicList != null) {
				addDevice(publicList, IPType.PUBLIC.getValue());
			}
			if (privateList != null) {
				addDevice(privateList, IPType.PRIVATE.getValue());
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean deleteService(int is_id) {
		try {
			dbproc.deleteService(is_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void modifyService(int is_id, String endtime, int state) throws Exception {
		dbproc.modifyService(is_id, endtime, state);
	}

	private void addService(int is_id, String account, String user, String vmMark,
			String starttime, int life, int state) throws Exception {
		dbproc.addService(is_id, account, user, vmMark, starttime, life, state);
	}

	private boolean deleteDevice(int ip_id) {
		try {
			dbproc.deleteDevice(ip_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void addDevice(List<String> ipList, int type) throws Exception {
		for (String ip : ipList) {
			if (!isEmpty(ip)) {
				dbproc.addDevice(ip, type);
			}
		}
	}

	private List<SearchResultRow> convertResults(ResultSetWrapper rsw) {
		if (rsw != null) {
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			try {
				int index = 1;
				while (rs.next()) {
					rows.add(convertRootResultRow(rs, index ++));
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
		String stype = null;
		try {
			stype = IPType.getIPType(rs.getInt(DBTableColName.IP.TYPE)).toString();
			IPState state = IPState.getIPState(rs.getInt(DBTableColName.IP_SERVICE.STATE));
			if (state != null) {
				sstate = state.toString();
				if (state != IPState.RESERVED) {
					Date starttime = rs.getDate(DBTableColName.IP_SERVICE.STARTTIME);
					account = rs.getString(DBTableColName.ACCOUNT.NAME);
					user = rs.getString(DBTableColName.USER.NAME);
					int life = rs.getInt(DBTableColName.IP_SERVICE.LIFE);
					sstarttime = formatter.format(starttime);
					slife = Integer.toString(life);
					sremains = Integer.toString(calcRemainingDays(starttime, life));
				}
			}
		}
		catch (Exception e) {
		}
		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.IP_SERVICE.ID),
		        rs.getString(DBTableColName.IP.ID), "", Integer.toString(index),
		        rs.getString(DBTableColName.IP.ADDR), stype,
		        rs.getString(DBTableColName.VM.MARK), account, user, sstarttime, slife, sremains, sstate));
	}

	private void queryAllIPCounts(int queryType, Map<Integer, Integer> map, int user_id) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.getCountsByState(queryType, user_id);
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

	public static final int TABLE_COL_INDEX_IS_ID = 0;
	public static final int TABLE_COL_INDEX_IP_ID = 1;
	public static final int TABLE_COL_INDEX_CHECKBOX = 2;
	public static final int TABLE_COL_INDEX_NO = 3;
	public static final int TABLE_COL_INDEX_VM = 6;
	public static final int TABLE_COL_INDEX_ACCOUNT = 7;
	public static final int TABLE_COL_INDEX_USER = 8;
	public static final int TABLE_COL_INDEX_STARTTIME = 9;
	public static final int TABLE_COL_INDEX_LIFE = 10;
	public static final int TABLE_COL_INDEX_REMAINS = 11;
	public static final int TABLE_COL_INDEX_STATE = 12;

	private static final String[] TABLE_COL_TITLE_CHECKBOX = {"", ""};
	private static final String[] TABLE_COL_TITLE_NO = {"", "序号"};
	private static final String[] TABLE_COL_TITLE_IP = {"", "IP"};
	private static final String[] TABLE_COL_TITLE_TYPE = {"", "类型"};
	private static final String[] TABLE_COL_TITLE_VM = {"", "虚拟机"};
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
	        new SearchResultFieldDesc(TABLE_COL_TITLE_IP[LAN_SELECT], false, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_TYPE[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_VM[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_USER[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "14%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_LIFE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_REMAINS[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STATE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));

}
