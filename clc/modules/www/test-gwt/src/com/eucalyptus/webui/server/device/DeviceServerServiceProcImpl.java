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
import com.eucalyptus.webui.client.activity.device.DeviceServerActivity.ServerState;
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
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

class DeviceServerDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceServerDBProcWrapper.class.getName());
	
	DBProcWrapper wrapper = DBProcWrapper.Instance();
	
	ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}
	
	void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
	ResultSetWrapper getCountsByState() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(DBTableColName.SERVER.STATE);
		sb.append(", count(*) FROM ");
		sb.append(DBTableName.SERVER);
		sb.append(" GROUP BY ");
		sb.append(DBTableColName.SERVER.STATE);
		return doQuery(sb.toString());
	}
	
	ResultSetWrapper queryAllServers(int queryState) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.SERVER);
		sb.append(" WHERE 1=1");
		
		if (queryState >= 0) {
			sb.append(" AND ");
			sb.append(DBTableColName.SERVER.STATE);
			sb.append(" = ").append(queryState);
		}
		return doQuery(sb.toString());
	}
	
	ResultSetWrapper queryServer(int server_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.SERVER);
		sb.append(" WHERE ");
		sb.append(DBTableColName.SERVER.ID);
		sb.append(" = ").append(server_id);
		return doQuery(sb.toString());
	}
	
	void modifyServerState(int server_id, int server_state) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.SERVER);
		sb.append(" SET ");
		sb.append(DBTableColName.SERVER.STATE).append(" = ").append(server_state);
		sb.append(" WHERE ");
		sb.append(DBTableColName.SERVER.ID).append(" = ").append(server_id);
		doUpdate(sb.toString());
	}
	
	void deleteDevice(int server_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ");
		sb.append(" FROM ").append(DBTableName.SERVER);
		sb.append(" WHERE ");
		sb.append(DBTableColName.SERVER.ID).append(" = ").append(server_id);
		doUpdate(sb.toString());
	}
	
	void addDevice(String mark, String name, String conf, String ip, int bw, int state, String room, String starttime)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.SERVER);
		sb.append(" (");
		sb.append(DBTableColName.SERVER.MARK).append(", ");
		sb.append(DBTableColName.SERVER.NAME).append(", ");
		sb.append(DBTableColName.SERVER.CONF).append(", ");
		sb.append(DBTableColName.SERVER.IP).append(", ");
		sb.append(DBTableColName.SERVER.BW).append(", ");
		sb.append(DBTableColName.SERVER.STATE).append(", ");
		sb.append(DBTableColName.SERVER.ROOM).append(", ");
		sb.append(DBTableColName.SERVER.STARTTIME).append(") ");
		sb.append(" VALUES ");
		sb.append("(");
		sb.append("\"").append(mark).append("\", ");
		sb.append("\"").append(name).append("\", ");
		sb.append("\"").append(conf).append("\", ");
		sb.append("\"").append(ip).append("\", ");
		sb.append(bw).append(", ");
		sb.append(state).append(", ");
		sb.append("\"").append(room).append("\", ");
		sb.append("\"").append(starttime).append("\")");
		doUpdate(sb.toString());
	}
	
	public void createServer(String mark, String name, String conf, String ip, int bw, int state, String cabinet_name) throws Exception {
	    DBTableCabinet CABINET = DBTable.CABINET;
	    DBTableServer SERVER = DBTable.SERVER;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("INSERT INTO ").append(DBTableName.SERVER);
        sb.append(" (");
        sb.append(SERVER.SERVER_MARK).append(", ");
        sb.append(SERVER.SERVER_NAME).append(", ");
        sb.append(SERVER.SERVER_CONF).append(", ");
        sb.append(SERVER.SERVER_IP).append(", ");
        sb.append(SERVER.SERVER_BW).append(", ");
        sb.append(SERVER.SERVER_STATE).append(", ");
        sb.append(SERVER.SERVER_STARTTIME).append(", ");
        sb.append(SERVER.CABINET_ID);
        sb.append(") VALUES (");
        sb.appendString(mark).append(", ");
        sb.appendString(name).append(", ");
        sb.appendString(conf).append(", ");
        sb.appendString(ip).append(", ");
        sb.append(bw).append(", ");
        sb.append(state).append(", ");
        sb.appendString(DBData.format(new Date())).append(", ");
        sb.append("(SELECT ").append(CABINET.CABINET_ID).append(" FROM ").append(CABINET).append(" WHERE ").append(CABINET.CABINET_NAME).append(" = ").appendString(cabinet_name).append(")");
        sb.append(")");
        doUpdate(sb.toString());
	}
	
}

public class DeviceServerServiceProcImpl {
	
	private DeviceServerDBProcWrapper dbproc = new DeviceServerDBProcWrapper();
	
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	public SearchResult lookupServer(Session session, String search, SearchRange range, int queryState) {
		ResultSetWrapper rsw = null;
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			rsw = dbproc.queryAllServers(queryState);
			List<SearchResultRow> rows = convertResults(rsw);
			if (rows != null) {
				int length = Math.min(range.getLength(), rows.size() - range.getStart());
				SearchResult result = new SearchResult(rows.size(), range);
				result.setDescs(FIELDS_ROOT);
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
	
	public Map<Integer, Integer> getServerCounts(Session session) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			queryAllServerCounts(map);
			return map;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public SearchResultRow modifyServerState(Session session, SearchResultRow row, int state) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			int server_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_SERVER_ID));
			modifyServerState(server_id, state);
			return lookupServer(row, server_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private SearchResultRow lookupServer(SearchResultRow row, int server_id) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.queryServer(server_id);
			ResultSet rs = rsw.getResultSet();
			if (!rs.next()) {
				return null;
			}
			row.setField(TABLE_COL_INDEX_STATE, ServerState.getServerState(rs.getInt(DBTableColName.SERVER.STATE)).toString());
			return row;
		}
		catch (Exception e) {
			return null;
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
	
	public List<SearchResultRow> deleteDevice(Session session, List<SearchResultRow> list) {
		try {
			if (list == null || !getUser(session).isSystemAdmin()) {
				return null;
			}
			List<SearchResultRow> result = new ArrayList<SearchResultRow>();
			for (SearchResultRow row : list) {
				int server_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_SERVER_ID));
				if (!deleteDevice(server_id)) {
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
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	public boolean addDevice(Session session, String mark, String name, String conf, String ip, int bw, int state, String room) {
		try {
			if (!getUser(session).isSystemAdmin() || isEmpty(name)) {
				return false;
			}
			addDevice(mark, name, conf, ip, bw, state, room);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void createServer(Session session, String mark, String name, String conf, String ip, int bw, int state, String cabinet_name) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
	    if (isEmpty(mark) || isEmpty(name)) {
	        throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称")); 
	    }
	    if (isEmpty(cabinet_name)) {
	        throw new EucalyptusServiceException(new ClientMessage("", "无效的机柜名称"));
	    }
	    try {
	        dbproc.createServer(mark, name, conf, ip, bw, state, cabinet_name);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        throw new EucalyptusServiceException(new ClientMessage("", "创建服务器失败"));
	    }
	}
	
	private void modifyServerState(int server_id, int state) throws Exception {
		dbproc.modifyServerState(server_id, state);
	}
	
	private boolean deleteDevice(int server_id) {
		try {
			dbproc.deleteDevice(server_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void addDevice(String mark, String name, String conf, String ip, int bw, int state, String room) throws Exception {
		dbproc.addDevice(mark, name, conf, ip, bw, state, room, formatter.format(new Date()));
	}
	
	private List<SearchResultRow> convertResults(ResultSetWrapper rsw) {
		if (rsw != null) {
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			try {
				int index = 1;
				while (rs.next()) {
					rows.add(convertRootResultRow(rs, index ++ ));
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
		String state = null;
		try {
			state = ServerState.getServerState(rs.getInt(DBTableColName.SERVER.STATE)).toString();
		}
		catch (Exception e) {
		}
		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.SERVER.ID), "", Integer.toString(index),
				rs.getString(DBTableColName.SERVER.NAME), rs.getString(DBTableColName.SERVER.MARK),
				rs.getString(DBTableColName.SERVER.CONF), rs.getString(DBTableColName.SERVER.IP),
				rs.getString(DBTableColName.SERVER.BW), rs.getString(DBTableColName.SERVER.ROOM),
				rs.getString(DBTableColName.SERVER.STARTTIME), state));
	}
	
	private void queryAllServerCounts(Map<Integer, Integer> map) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.getCountsByState();
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
	
	private static final int LAN_SELECT = 1;
	
	public static final int TABLE_COL_INDEX_SERVER_ID = 0;
	public static final int TABLE_COL_INDEX_CHECKBOX = 1;
	public static final int TABLE_COL_INDEX_NO = 2;
	public static final int TABLE_COL_INDEX_STATE = 10;
	
	private static final String[] TABLE_COL_TITLE_CHECKBOX = {"", ""};
	private static final String[] TABLE_COL_TITLE_NO = {"", "序号"};
	private static final String[] TABLE_COL_TITLE_NAME = {"", "名称"};
	private static final String[] TABLE_COL_TITLE_MARK = {"", "标识"};
	private static final String[] TABLE_COL_TITLE_CONF = {"", "配置"};
	private static final String[] TABLE_COL_TITLE_IP = {"", "IP地址"};
	private static final String[] TABLE_COL_TITLE_BW = {"", "带宽"};
	private static final String[] TABLE_COL_TITLE_ROOM = {"", "位置"};
	private static final String[] TABLE_COL_TITLE_STARTTIME = {"", "开始时间"};
	private static final String[] TABLE_COL_TITLE_STATE = {"", "状态"};
	
	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
			new SearchResultFieldDesc(null, "0%",false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKBOX[LAN_SELECT], "4%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NAME[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_MARK[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CONF[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_IP[LAN_SELECT], false, "14%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_BW[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_ROOM[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_STATE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));
	
}
