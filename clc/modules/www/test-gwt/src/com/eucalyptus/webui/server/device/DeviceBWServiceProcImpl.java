package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

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

class DeviceBWDBProcWrapper {

	private static final Logger LOG = Logger.getLogger(DeviceBWDBProcWrapper.class.getName());

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
	
	ResultSetWrapper listIPsByUser(String account, String user) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.IP).append(" LEFT JOIN ").append(DBTableName.IP_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.IP_ID);
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
		sb.append(" LEFT JOIN ").append(DBTableName.BW_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(DBTableName.BW_SERVICE).append(".").append(DBTableColName.BW_SERVICE.IP_ID);
		sb.append(" WHERE ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.NAME);
		sb.append(" = ").append("\"").append(user).append("\"");
		sb.append(" AND ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME);
		sb.append(" = ").append("\"").append(account).append("\"");
		sb.append(" AND ").append(DBTableName.BW_SERVICE).append(".").append(DBTableColName.BW_SERVICE.IP_ID);
		sb.append(" IS NULL");
		return doQuery(sb.toString());
	}

	/* *
	 * select * from bw_service left join ip on bw_service.ip_id = ip.ip_id
	 * left join user on bw_service.user_id=user.user_id left join account on
	 * account.account_id=user.account_id where 1=1 and user.id=$user_id
	 */
	ResultSetWrapper queryAllBWs(int account_id, int user_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.BW_SERVICE).append(" LEFT JOIN ").append(DBTableName.IP);
		sb.append(" ON ");
		sb.append(DBTableName.BW_SERVICE).append(".").append(DBTableColName.BW_SERVICE.IP_ID);
		sb.append(" = ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" LEFT JOIN ").append(DBTableName.USER);
		sb.append(" ON ");
		sb.append(DBTableName.BW_SERVICE).append(".").append(DBTableColName.BW_SERVICE.USER_ID);
		sb.append(" = ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ID);
		sb.append(" LEFT JOIN ").append(DBTableName.ACCOUNT);
		sb.append(" ON ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID);
		sb.append(" = ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID);

		sb.append(" WHERE 1=1");
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
	 * select * from cpu_service where cs_id=$cs_id
	 */
	ResultSetWrapper queryService(int cs_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.BW_SERVICE);
		sb.append(" WHERE ");
		sb.append(DBTableColName.BW_SERVICE.ID);
		sb.append(" = ").append(cs_id);
		return doQuery(sb.toString());
	}

	void modifyService(int bs_id, String endtime) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.BW_SERVICE);
		sb.append(" SET ");

		sb.append(DBTableColName.BW_SERVICE.LIFE).append(" = ");
		sb.append("(SELECT DATEDIFF((SELECT IF(");
		sb.append("\"").append(endtime).append("\"");
		sb.append(" > ").append(DBTableColName.BW_SERVICE.STARTTIME).append(", ");
		sb.append("\"").append(endtime).append("\"").append(", ");
		sb.append(DBTableColName.BW_SERVICE.STARTTIME).append(")), ");
		sb.append(DBTableColName.BW_SERVICE.STARTTIME).append("))");
		sb.append(" WHERE ");
		sb.append(DBTableColName.BW_SERVICE.ID).append(" = ").append(bs_id);
		doUpdate(sb.toString());
	}

	void addService(String account, String user, String bs_starttime, int bs_life, String ip, long bandwidth)
	        throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(", ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.ID);
		sb.append(" FROM ");
		sb.append(DBTableName.IP).append(" LEFT JOIN ").append(DBTableName.IP_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(DBTableName.IP_SERVICE).append(".").append(DBTableColName.IP_SERVICE.IP_ID);
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
		sb.append(" LEFT JOIN ").append(DBTableName.BW_SERVICE);
		sb.append(" ON ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ID);
		sb.append(" = ");
		sb.append(DBTableName.BW_SERVICE).append(".").append(DBTableColName.BW_SERVICE.IP_ID);
		sb.append(" WHERE ");
		sb.append(DBTableName.USER).append(".").append(DBTableColName.USER.NAME);
		sb.append(" = ").append("\"").append(user).append("\"");
		sb.append(" AND ");
		sb.append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME);
		sb.append(" = ").append("\"").append(account).append("\"");
		sb.append(" AND ");
		sb.append(DBTableName.IP).append(".").append(DBTableColName.IP.ADDR);
		sb.append(" = ").append("\"").append(ip).append("\"");
		sb.append(" AND ").append(DBTableName.BW_SERVICE).append(".").append(DBTableColName.BW_SERVICE.IP_ID);
		sb.append(" IS NULL");
		
		ResultSetWrapper rsw = null;
		int ip_id = -1;
		int user_id = -1;
		try {
			rsw = doQuery(sb.toString());
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				ip_id = rs.getInt(1);
				user_id = rs.getInt(2);
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
		assert (ip_id != -1 && user_id != -1);
		
		sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.BW_SERVICE);
		sb.append("(");
		sb.append(DBTableColName.BW_SERVICE.STARTTIME).append(", ");
		sb.append(DBTableColName.BW_SERVICE.LIFE).append(", ");
		sb.append(DBTableColName.BW_SERVICE.IP_ID).append(", ");
		sb.append(DBTableColName.BW_SERVICE.USER_ID).append(", ");
		sb.append(DBTableColName.BW_SERVICE.BANDWIDTH).append(") ");
		sb.append(" VALUES ");
		sb.append("(").append("\"").append(bs_starttime).append("\"").append(", ");
		sb.append(bs_life).append(", ");
		sb.append(ip_id).append(", ");
		sb.append(user_id).append(", ");
		sb.append(bandwidth).append(")");
		doUpdate(sb.toString());
	}

	/* *
	 * delete from bw_service where bs_id=$bs_id
	 */
	void deleteService(int bs_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(DBTableName.BW_SERVICE);
		sb.append(" WHERE ").append(DBTableColName.BW_SERVICE.ID).append(" = ").append(bs_id);
		doUpdate(sb.toString());
	}

}

public class DeviceBWServiceProcImpl {

	private DeviceBWDBProcWrapper dbproc = new DeviceBWDBProcWrapper();

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
	
	public List<String> listIPsByUser(Session session, String account, String user) {
		try {
			if (!isEmpty(account) || !isEmpty(user)) {
				if (getUser(session).isSystemAdmin()) {
					return listIPsByUser(account, user);
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
	
	private List<String> listIPsByUser(String account, String user) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.listIPsByUser(account, user);
			List<String> list = new ArrayList<String>();
			ResultSet rs = rsw.getResultSet();
			while (rs.next()) {
				String ip = rs.getString(DBTableColName.IP.ADDR);
				if (ip != null) {
					list.add(ip);
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

	public SearchResult lookupBW(Session session, String search, SearchRange range) {
		ResultSetWrapper rsw = null;
		try {
			LoginUserProfile user = getUser(session);
			int account_id = user.getAccountId();
			int user_id = user.getUserId();

			List<SearchResultRow> rows;
			if (user.isSystemAdmin()) {
				rsw = dbproc.queryAllBWs(-1, -1);
			}
			else if (user.isAccountAdmin()) {
				rsw = dbproc.queryAllBWs(account_id, -1);
			}
			else {
				rsw = dbproc.queryAllBWs(account_id, user_id);
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
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public SearchResultRow modifyService(Session session, SearchResultRow row, String sendtime) {
		try {
			if (row == null || isEmpty(sendtime)) {
				return null;
			}
			LoginUserProfile user = getUser(session);
			if (!user.isSystemAdmin()) {
				return null;
			}
			int bs_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_BS_ID));
			modifyService(bs_id, formatter.format(formatter.parse(sendtime)));
			return lookupService(row, bs_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private SearchResultRow lookupService(SearchResultRow row, int cs_id) throws Exception {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.queryService(cs_id);
			ResultSet rs = rsw.getResultSet();
			if (!rs.next()) {
				return null;
			}
			Date starttime = rs.getDate(DBTableColName.BW_SERVICE.STARTTIME);
			int life = rs.getInt(DBTableColName.BW_SERVICE.LIFE);
			sstarttime = formatter.format(starttime);
			slife = Integer.toString(life);
			sremains = Integer.toString(calcRemainingDays(starttime, life));
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
		return row;
	}

	public List<SearchResultRow> deleteService(Session session, List<SearchResultRow> list) {
		try {
			if (!getUser(session).isSystemAdmin() || list == null) {
				return null;
			}
			List<SearchResultRow> result = new ArrayList<SearchResultRow>();
			for (SearchResultRow row : list) {
				int bs_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_BS_ID));
				if (!deleteService(bs_id)) {
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

	public boolean addService(Session session, String account, String user,
	        String sstarttime, int life, String ip, long bandwidth) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return false;
			}
			if (isEmpty(account) || isEmpty(user) || isEmpty(ip)) {
				return false;
			}
			if (isEmpty(sstarttime) || !(life >= 0)) {
				return false;
			}
			addService(account, user, sstarttime, life, ip, bandwidth);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean deleteService(int bs_id) {
		try {
			dbproc.deleteService(bs_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void modifyService(int bs_id, String endtime) throws Exception {
		dbproc.modifyService(bs_id, endtime);
	}

	private void addService(String account, String user, String starttime, int life, String ip, long bandwidth)
	        throws Exception {
		dbproc.addService(account, user, starttime, life, ip, bandwidth);
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
		String account = null;
		String user = null;
		try {
			Date starttime = rs.getDate(DBTableColName.BW_SERVICE.STARTTIME);
			account = rs.getString(DBTableColName.ACCOUNT.NAME);
			user = rs.getString(DBTableColName.USER.NAME);
			int life = rs.getInt(DBTableColName.BW_SERVICE.LIFE);
			sstarttime = formatter.format(starttime);
			slife = Integer.toString(life);
			sremains = Integer.toString(calcRemainingDays(starttime, life));
		}
		catch (Exception e) {
		}
		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.BW_SERVICE.ID), "", Integer.toString(index),
		        account, user, rs.getString(DBTableColName.BW_SERVICE.BANDWIDTH), "SPEED",
		        rs.getString(DBTableColName.IP.ADDR), sstarttime, slife, sremains));
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
	
	public static final int TABLE_COL_INDEX_BS_ID = 0;
	public static final int TABLE_COL_INDEX_CHECKBOX = 1;
	public static final int TABLE_COL_INDEX_NO = 2;
	public static final int TABLE_COL_INDEX_ACCOUNT = 3;
	public static final int TABLE_COL_INDEX_USER = 4;
	public static final int TABLE_COL_INDEX_STARTTIME = 8;
	public static final int TABLE_COL_INDEX_LIFE = 9;
	public static final int TABLE_COL_INDEX_REMAINS = 10;

	private static final String[] TABLE_COL_TITLE_CHECKBOX = {"", ""};
	private static final String[] TABLE_COL_TITLE_NO = {"", "序号"};
	private static final String[] TABLE_COL_TITLE_BW = {"", "带宽(KB)"};
	private static final String[] TABLE_COL_TITLE_BWSPEED = {"", "速率/S"};
	private static final String[] TABLE_COL_TITLE_IPADDR = {"", "IP地址"};
	private static final String[] TABLE_COL_TITLE_STARTTIME = {"", "开始时间"};
	private static final String[] TABLE_COL_TITLE_LIFE = {"", "服务期限"};
	private static final String[] TABLE_COL_TITLE_REMAINS = {"", "剩余时间"};
	private static final String[] TABLE_COL_TITLE_ACCOUNT = {"", "账户"};
	private static final String[] TABLE_COL_TITLE_USER = {"", "用户"};

	private static final List<SearchResultFieldDesc> FIELDS = Arrays.asList(
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKBOX[LAN_SELECT], "4%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_USER[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_BW[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_BWSPEED[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_IPADDR[LAN_SELECT], false, "14%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "14%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_LIFE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_REMAINS[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));

}
