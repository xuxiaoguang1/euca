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
import com.eucalyptus.webui.shared.resource.device.ServerInfo;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceServerService {
	
	private DeviceServerDBProcWrapper dbproc = new DeviceServerDBProcWrapper();
	
	private static DeviceServerService instance = new DeviceServerService();
	
	public static DeviceServerService getInstance() {
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
			new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "序号"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "描述"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "所在机柜"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(true, "8%", new ClientMessage("", "IP地址"),
	                TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(true, "8%", new ClientMessage("", "带宽(KB)"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "状态"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case 3: return DBTable.SERVER.SERVER_NAME;
        case 4: return DBTable.SERVER.SERVER_DESC;
        case 5: return DBTable.CABINET.CABINET_NAME;
        case 6: return DBTable.SERVER.SERVER_IP;
        case 7: return DBTable.SERVER.SERVER_BW;
        case 8: return DBTable.SERVER.SERVER_STATE;
        case 9: return DBTable.SERVER.SERVER_CREATIONTIME;
        case 10: return DBTable.SERVER.SERVER_MODIFIEDTIME;
        }
        return null;
    }
    
	public synchronized SearchResult lookupServerByDate(Session session, SearchRange range, ServerState state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupServerByDate(state, dateBegin, dateEnd, getSortColumn(range), range.isAscending());
			ResultSet rs = rsw.getResultSet();
			ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
			DBTableCabinet CABINET = DBTable.CABINET;
			DBTableServer SERVER = DBTable.SERVER;
			for (int index = 1; rs.next(); index ++) {
				int server_id = DBData.getInt(rs, SERVER.SERVER_ID);
				String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
				String server_desc = DBData.getString(rs, SERVER.SERVER_DESC);
				String server_ip = DBData.getString(rs, SERVER.SERVER_IP);
				int server_bw = DBData.getInt(rs, SERVER.SERVER_BW);
				int server_state = DBData.getInt(rs, SERVER.SERVER_STATE);
				Date server_creationtime = DBData.getDate(rs, SERVER.SERVER_CREATIONTIME);
				Date server_modifiedtime = DBData.getDate(rs, SERVER.SERVER_MODIFIEDTIME);
				String cabinet_name = DBData.getString(rs, CABINET.CABINET_NAME);
				List<String> list = Arrays.asList(Integer.toString(server_id), "", Integer.toString(index),
						server_name, server_desc, cabinet_name, server_ip, Integer.toString(server_bw),
						ServerState.getServerState(server_state).toString(),
						DBData.format(server_creationtime), DBData.format(server_modifiedtime));
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
	
	public synchronized Map<Integer, Integer> lookupServerCountsGroupByState(Session session) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupServerCountsGroupByState();
			ResultSet rs = rsw.getResultSet();
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			int sum = 0;
			while (rs.next()) {
				int server_state = rs.getInt(1);
				int server_count = rs.getInt(2);
				sum += server_count;
				map.put(server_state, server_count);
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
	
	public synchronized void addServer(Session session, String server_name, String server_desc, String server_ip, int server_bw,
			ServerState server_state, String cabinet_name) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		if (isEmpty(server_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
		}
		if (isEmpty(cabinet_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的机柜名称"));
		}
		if (server_state == null) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器状态"));
		}
		if (server_desc == null) {
			server_desc = "";
		}
		if (server_ip == null) {
			server_ip = "";
		}
		try {
			dbproc.createServer(server_name, server_desc, server_ip, server_bw, server_state, cabinet_name);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "创建服务器失败"));
		}
	}
	
	public synchronized void deleteServer(Session session, List<Integer> server_ids) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		try {
			if (!server_ids.isEmpty()) {
				dbproc.deleteServer(server_ids);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除服务器失败"));
		}
	}
	
	public synchronized void modifyServer(Session session, int server_id, String server_desc, String server_ip, int server_bw,
			ServerState server_state) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (server_state == null) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器状态"));
		}
		if (server_desc == null) {
			server_desc = "";
		}
		if (server_ip == null) {
			server_ip = "";
		}		
		try {
			dbproc.modifyServer(server_id, server_desc, server_ip, server_bw, server_state);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "修改服务器失败"));
		}
	}
	
	public synchronized void modifyServerState(Session session, int server_id, ServerState server_state) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (server_state == null) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器状态"));
		}
		try {
			dbproc.updateServerState(server_id, server_state);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "修改服务器失败"));
		}
	}
	
	public synchronized void updateServerState(int server_id, ServerState server_state) throws EucalyptusServiceException {
		if (server_state == null) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器状态"));
		}
		try {
			dbproc.updateServerState(server_id, server_state);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "修改服务器状态失败"));
		}
	}
	
	public synchronized List<String> lookupServerNamesByCabinetName(Session session, String cabinet_name) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		if (isEmpty(cabinet_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的机柜名称"));
        }
		ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupServerNamesByCabinetName(cabinet_name);
            DBTableServer SERVER = DBTable.SERVER;
            ResultSet rs = rsw.getResultSet();
            List<String> server_name_list = new LinkedList<String>();
            while (rs.next()) {
                String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
                server_name_list.add(server_name);
            }
            return server_name_list;
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
	
	private ServerInfo getServerInfo(ResultSet rs) throws Exception {
	    DBTableServer SERVER = DBTable.SERVER;
	    int server_id = DBData.getInt(rs, SERVER.SERVER_ID);
        String server_name = DBData.getString(rs, SERVER.SERVER_NAME);
        String server_desc = DBData.getString(rs, SERVER.SERVER_DESC);
        String server_ip = DBData.getString(rs, SERVER.SERVER_IP);
        int server_bw = DBData.getInt(rs, SERVER.SERVER_BW);
        ServerState server_state = ServerState.getServerState(DBData.getInt(rs, SERVER.SERVER_STATE));
        Date server_creationtime = DBData.getDate(rs, SERVER.SERVER_CREATIONTIME);
        Date server_modifiedtime = DBData.getDate(rs, SERVER.SERVER_MODIFIEDTIME);
        int cabinet_id = DBData.getInt(rs, SERVER.CABINET_ID);
        return new ServerInfo(server_id, server_name, server_desc, server_ip, server_bw, server_state,
                server_creationtime, server_modifiedtime, cabinet_id);
	}
	
	public synchronized ServerInfo lookupServerInfoByID(int server_id) throws EucalyptusServiceException {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupServerByID(server_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        return getServerInfo(rs);
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
	
	public synchronized ServerInfo lookupServerInfoByName(String server_name) throws EucalyptusServiceException {
		if (isEmpty(server_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的服务器名称"));
		}
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupServerByName(server_name);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        return getServerInfo(rs);
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

class DeviceServerDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceCabinetDBProcWrapper.class.getName());
	
	private DBProcWrapper wrapper = DBProcWrapper.Instance();
		
	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}
	
	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
    public ResultSetWrapper lookupServerByID(int server_id) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(SERVER).append(" WHERE ").append(SERVER.SERVER_ID).append(" = ").append(server_id);
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupServerByName(String server_name) throws Exception {
        DBTableServer SERVER = DBTable.SERVER;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(SERVER).append(" WHERE ").append(SERVER.SERVER_NAME).append(" = ").appendString(server_name);
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
	
	public ResultSetWrapper lookupServerByDate(ServerState state, Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending) throws Exception {
		DBTableCabinet CABINET = DBTable.CABINET;
		DBTableServer SERVER = DBTable.SERVER;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(SERVER.ANY).append(", ").append(CABINET.CABINET_NAME).append(" FROM ");
		sb.append(SERVER).append(" LEFT JOIN ").append(CABINET);
		sb.append(" ON ").append(SERVER.CABINET_ID).append(" = ").append(CABINET.CABINET_ID).append(" WHERE 1=1");
		if (state != null) {
		    sb.append(" AND ").append(SERVER.SERVER_STATE).append(" = ").append(state.getValue());
		}
		if (dateBegin != null || dateEnd != null) {
		    sb.append(" AND (");
		    appendBoundedDate(sb, SERVER.SERVER_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
		    appendBoundedDate(sb, SERVER.SERVER_MODIFIEDTIME, dateBegin, dateEnd);
		    sb.append(")");
		}
		if (sort != null) {
            sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
        }
        return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupServerCountsGroupByState() throws Exception {
		DBTableServer SERVER = DBTable.SERVER;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(SERVER.SERVER_STATE).append(", count(*) FROM ").append(SERVER).append(" WHERE 1=1").append(" GROUP BY ").append(SERVER.SERVER_STATE);
		return doQuery(sb.toString());
	}
	
	public void createServer(String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state, String cabinet_name) throws Exception {
		DBTableCabinet CABINET = DBTable.CABINET;
		DBTableServer SERVER = DBTable.SERVER;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("INSERT INTO ").append(SERVER).append(" (");
		sb.append(SERVER.SERVER_NAME).append(", ");
		sb.append(SERVER.SERVER_DESC).append(", ");
		sb.append(SERVER.SERVER_IP).append(", ");
		sb.append(SERVER.SERVER_BW).append(", ");
		sb.append(SERVER.SERVER_STATE).append(", ");
		sb.append(SERVER.CABINET_ID).append(", ");
		sb.append(SERVER.SERVER_CREATIONTIME);
		sb.append(") VALUES (");
		sb.appendString(server_name).append(", ");
		sb.appendString(server_desc).append(", ");
		sb.appendString(server_ip).append(", ");
		sb.append(server_bw).append(", ");
		sb.append(server_state.getValue()).append(", ");
		sb.append("(SELECT ").append(CABINET.CABINET_ID).append(" FROM ").append(CABINET).append(" WHERE ").append(CABINET.CABINET_NAME).append(" = ").appendString(cabinet_name).append("), ");
		sb.appendDate(new Date()).append(")");
		doUpdate(sb.toString());
	}
	
	public void deleteServer(List<Integer> server_ids) throws Exception {
		DBTableServer SERVER = DBTable.SERVER;
		DBStringBuilder sb = new DBStringBuilder();
		
		StringBuilder ids = new StringBuilder();
		int total = 0;
		for (int server_id : server_ids) {
			if (total ++ != 0) {
				ids.append(", ");
			}
			ids.append(server_id);
		}
		
		sb.append("DELETE FROM ").append(SERVER).append(" WHERE ");
		sb.append(SERVER.SERVER_ID).append(" IN (").append(ids.toString()).append(")");
		doUpdate(sb.toString());
	}
	
	public void modifyServer(int server_id, String server_desc, String server_ip, int server_bw, ServerState server_state) throws Exception {
		DBTableServer SERVER = DBTable.SERVER;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(SERVER).append(" SET ");
		sb.append(SERVER.SERVER_DESC).append(" = ").appendString(server_desc).append(", ");
		sb.append(SERVER.SERVER_IP).append(" = ").appendString(server_ip).append(", ");
		sb.append(SERVER.SERVER_BW).append(" = ").append(server_bw).append(", ");
		sb.append(SERVER.SERVER_STATE).append(" = ").append(server_state.getValue());
		sb.append(" WHERE ");
		sb.append(SERVER.SERVER_ID).append(" = ").append(server_id);
		doUpdate(sb.toString());
	}
	
	public void updateServerState(int server_id, ServerState server_state) throws Exception {
		DBTableServer SERVER = DBTable.SERVER;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(SERVER).append(" SET ");
		sb.append(SERVER.SERVER_STATE).append(" = ").append(server_state.getValue());
		sb.append(" WHERE ");
		sb.append(SERVER.SERVER_ID).append(" = ").append(server_id);
		doUpdate(sb.toString());
	}
	
	public ResultSetWrapper lookupServerNamesByCabinetName(String cabinet_name) throws Exception {
		DBTableCabinet CABINET = DBTable.CABINET;
		DBTableServer SERVER = DBTable.SERVER;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT DISTINCT(").append(SERVER.SERVER_NAME).append(") FROM ");
	    sb.append(SERVER).append(" LEFT JOIN ").append(CABINET).append(" ON ").append(SERVER.CABINET_ID).append(" = ").append(CABINET.CABINET_ID);
	    sb.append(" WHERE ").append(CABINET.CABINET_NAME).append(" = ").appendString(cabinet_name);
	    sb.append(" ORDER BY ").append(SERVER.SERVER_NAME);
	    return doQuery(sb.toString());
	}
	
}
