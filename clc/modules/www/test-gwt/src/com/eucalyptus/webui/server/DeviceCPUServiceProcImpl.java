package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

class DeviceCPUDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceCPUDBProcWrapper.class.getName());

	DBProcWrapper wrapper = DBProcWrapper.Instance();
	
	ResultSetWrapper doQuery(String request) {
		LOG.info(request);
		try {
			return wrapper.query(request);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void doUpdate(String request) {
		LOG.info(request);
		try {
			wrapper.update(request);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* *
	 * select cpu_service.cs_state, count(*) from cpu_service where cpu_service.user_id=user_id group by cpu_service.cs_state;
	 * */
	ResultSetWrapper queryCountsByState(int user_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.STATE);
		sb.append(", count(*) FROM ");
		sb.append(DBTableName.CPU_SERVICE);
		
		if (user_id >= 0) {
			sb.append(" WHERE ");
			sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.USER_ID);
			sb.append(" = ").append(user_id);
		}
		
		sb.append(" GROUP BY ");
		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.STATE);
		return doQuery(sb.toString());
	}
	
	/* * 
	 * select cpu.*, server.* ,cpu_service.*, user.*, account.*
	 *   from cpu left join server on cpu.server_id = server.server_id
	 *   left join cpu_service on cpu.cpu_id=cpu_service.cpu_id
	 *   left join user on cpu_service.user_id=user.user_id
	 *   left join account on user.account_id=account.id
	 *   where 1=1 and cpu_service.cs_state=queryState;
	 * */
	ResultSetWrapper queryAllCPUs(int queryState, int user_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableName.CPU).append(".*, ");
		sb.append(DBTableName.SERVER).append(".*, ");
		sb.append(DBTableName.CPU_SERVICE).append(".*, ");
		sb.append(DBTableName.USER).append(".*, ");
		sb.append(DBTableName.ACCOUNT).append(".* ");
		sb.append(" FROM ");
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
	 * select * from cpu_service where cpu_service.cs_id=cs_id and cpu_service.user_id=user_id
	 * */
	ResultSetWrapper queryService(int cs_id, int user_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.CPU_SERVICE);
		sb.append(" WHERE ");
		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.ID);
		sb.append(" = ").append(cs_id);
		if (user_id >= 0) {
			sb.append(" AND ");
			sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.USER_ID);
			sb.append(" = ").append(user_id);
		}
		return doQuery(sb.toString());
	}
	
	/* *
	 * update cpu_service set cs_life=life, cs_state=state where cs_id=cs_id and user_id=user_id
	 * */
	void updateService(int cs_id, int user_id, int life, int state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.CPU_SERVICE);
		sb.append(" SET ");
		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ").append(life);
		sb.append(", ");
		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
		
		if (user_id >= 0) {
			sb.append(" AND ");
			sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = ").append(user_id);
		}
		doUpdate(sb.toString());
	}
	
	/* *
	 * update cpu_service set cs_starttime="0000-00-00", cs_state=CPUState.RESERVED, cs_life=0, cs_user=user_id
	 * */
	void deleteService(int cs_id, int user_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.CPU_SERVICE);
		sb.append(" SET ");
		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(" = \"0000-00-00\"");
		sb.append(", ");
		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(CPUState.getValue(CPUState.RESERVED));
		sb.append(", ");
		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ").append(0);
		sb.append(", ");
		sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = ").append(user_id);
		sb.append(" WHERE ");
		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
		doUpdate(sb.toString());
	}

}

public class DeviceCPUServiceProcImpl {
	
	private DeviceCPUDBProcWrapper dbproc = new DeviceCPUDBProcWrapper();
	
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	public SearchResult lookup(Session session, String search, SearchRange range, int queryState) {
		try {
			LoginUserProfile user = getUser(session);
			boolean isRoot = user.isSystemAdmin();
			
			List<SearchResultRow> rows;
			if (isRoot) {
				rows = convertResults(isRoot, dbproc.queryAllCPUs(queryState, -1));
			}
			else {
				rows = convertResults(isRoot, dbproc.queryAllCPUs(queryState, getUser(session).getUserId()));
			}
			
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
	}
	
	public Map<Integer, Integer> queryCounts(Session session) {
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
			if (sendtime == null || CPUState.getCPUState(state) == null) {
				return null;
			}
			LoginUserProfile user = getUser(session);
			boolean isRoot = user.isSystemAdmin();
			int user_id = isRoot ? -1 : user.getUserId();
			int cs_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_CS_ID));
			Date starttime = queryServiceStarttime(cs_id, user_id);
			Date endtime = formatter.parse(sendtime);
			int life = getNumDays(endtime, starttime);
			if (life >= 0) {
				if (updateService(cs_id, user_id, life, state)) {
					int remains = calcRemainingDays(starttime, life);
					if (isRoot) {
						row.setField(TABLE_COL_INDEX_ROOT_LIFE, Integer.toString(life));
						row.setField(TABLE_COL_INDEX_ROOT_REMAINS, Integer.toString(remains));
						row.setField(TABLE_COL_INDEX_ROOT_STATE, CPUState.getCPUState(state).toString());
					}
					else {
						row.setField(TABLE_COL_INDEX_USER_LIFE, Integer.toString(life));
						row.setField(TABLE_COL_INDEX_USER_REMAINS, Integer.toString(remains));
						row.setField(TABLE_COL_INDEX_USER_STATE, CPUState.getCPUState(state).toString());
					}
					return row;
				}
			}
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
	
	public boolean deleteService(Session session, List<Integer> list) {
		try {
			if (list == null) {
				return false;
			}
			LoginUserProfile user = getUser(session);
			if (!user.isSystemAdmin()) {
				return false;
			}
			for (int cs_id : list) {
				if (!deleteService(cs_id, user.getUserId())) {
					return false;
				}
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	    return false;
    }
	
	private boolean deleteService(int cs_id, int user_id) {
		try {
			dbproc.deleteService(cs_id, user_id);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private boolean updateService(int cs_id, int user_id, int life, int state) {
		try {
			dbproc.updateService(cs_id, user_id, life, state);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private Date queryServiceStarttime(int cs_id, int user_id) throws Exception {
		ResultSetWrapper rsw = dbproc.queryService(cs_id, user_id);
		try {
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				return rs.getDate(DBTableColName.CPU_SERVICE.STARTTIME);
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				rsw.close();
			}
			catch (Exception e) {
				throw e;
			}
		}
		return null;
	}
	
	private int queryServiceState(int cs_id, int user_id) throws Exception {
		ResultSetWrapper rsw = dbproc.queryService(cs_id, user_id);
		try {
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				return rs.getInt(DBTableColName.CPU_SERVICE.STATE);
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				rsw.close();
			}
			catch (Exception e) {
				throw e;
			}
		}
		return -1;
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
	
	private String getCPUState(String state) {
		if (state != null) {
			try {
				return CPUState.getCPUState(Integer.parseInt(state)).toString();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	private SearchResultRow convertRootResultRow(ResultSet rs, int index) throws SQLException {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		try {
			Date starttime = rs.getDate(DBTableColName.CPU_SERVICE.STARTTIME);
			int life = rs.getInt(DBTableColName.CPU_SERVICE.LIFE);
			sstarttime = formatter.format(starttime);
			slife = Integer.toString(life);
			sremains = Integer.toString(calcRemainingDays(starttime, life));
		}
		catch (Exception e) {
		}
		String state = getCPUState(rs.getString(DBTableColName.CPU_SERVICE.STATE));
		return new SearchResultRow(Arrays.asList(
				rs.getString(DBTableColName.CPU.ID),
				rs.getString(DBTableColName.CPU_SERVICE.ID),
				"", Integer.toString(index),
				rs.getString(DBTableColName.SERVER.MARK),
				rs.getString(DBTableColName.CPU.VENDOR),
				rs.getString(DBTableColName.CPU.MODEL),
				rs.getString(DBTableColName.CPU.GHZ),
				rs.getString(DBTableColName.CPU.CACHE),
				rs.getString(DBTableColName.ACCOUNT.NAME),
				rs.getString(DBTableColName.USER.NAME),
				sstarttime, slife, sremains, state));
	}
	
	private SearchResultRow convertUserResultRow(ResultSet rs, int index) throws SQLException {
		String sstarttime = null;
		String slife = null;
		String sremains = null;
		try {
			Date starttime = rs.getDate(DBTableColName.CPU_SERVICE.STARTTIME);
			int life = rs.getInt(DBTableColName.CPU_SERVICE.LIFE);
			sstarttime = formatter.format(starttime);
			slife = Integer.toString(life);
			sremains = Integer.toString(calcRemainingDays(starttime, life));
		}
		catch (Exception e) {
		}
		String state = getCPUState(rs.getString(DBTableColName.CPU_SERVICE.STATE));
		return new SearchResultRow(Arrays.asList(
				rs.getString(DBTableColName.CPU.ID),
				rs.getString(DBTableColName.CPU_SERVICE.ID),
				"", Integer.toString(index),
				rs.getString(DBTableColName.SERVER.MARK),
				rs.getString(DBTableColName.CPU.VENDOR),
				rs.getString(DBTableColName.CPU.MODEL),
				rs.getString(DBTableColName.CPU.GHZ),
				rs.getString(DBTableColName.CPU.CACHE),
				sstarttime, slife, sremains, state));
	}
	
	private void queryAllCPUCounts(Map<Integer, Integer> map, int user_id) throws Exception {
		ResultSetWrapper rsw = dbproc.queryCountsByState(user_id);
		try {
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
				rsw.close();
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
	
	public static final int TABLE_COL_INDEX_CPU_ID = 0;
	public static final int TABLE_COL_INDEX_CS_ID = 1;
	public static final int TABLE_COL_INDEX_CHECKBOX = 2;
	public static final int TABLE_COL_INDEX_NO = 3;
	public static final int TABLE_COL_INDEX_ROOT_STARTTIME = 11;
	public static final int TABLE_COL_INDEX_ROOT_LIFE = 12;
	public static final int TABLE_COL_INDEX_ROOT_REMAINS = 13;
	public static final int TABLE_COL_INDEX_ROOT_STATE = 14;
	public static final int TABLE_COL_INDEX_USER_STARTTIME = 9;
	public static final int TABLE_COL_INDEX_USER_LIFE = 9;
	public static final int TABLE_COL_INDEX_USER_REMAINS = 9;
	public static final int TABLE_COL_INDEX_USER_STATE = 12;

	private static final String[] TABLE_COL_TITLE_CHECKALL = {"", ""};
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
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[LAN_SELECT], "4%", false),
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
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[LAN_SELECT], "4%", false),
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
