package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
import com.eucalyptus.webui.shared.resource.device.RoomInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceRoomService {
	
	private DeviceRoomDBProcWrapper dbproc = new DeviceRoomDBProcWrapper();
	
	private DeviceRoomService() {
	}
	
	private static DeviceRoomService instance = new DeviceRoomService();
	
	public static DeviceRoomService getInstance() {
		return instance;
	}
	
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	private List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("4%", false, new ClientMessage("", "")),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "序号"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "描述"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "所在区域"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case 3: return DBTable.ROOM.ROOM_NAME;
        case 4: return DBTable.ROOM.ROOM_DESC;
        case 5: return DBTable.AREA.AREA_NAME;
        case 6: return DBTable.ROOM.ROOM_CREATIONTIME;
        case 7: return DBTable.ROOM.ROOM_MODIFIEDTIME;
        }
        return null;
    }
	
	public synchronized SearchResult lookupRoomByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupRoomByDate(dateBegin, dateEnd, getSortColumn(range), range.isAscending());
			ResultSet rs = rsw.getResultSet();
			ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
			DBTableArea AREA = DBTable.AREA;
			DBTableRoom ROOM = DBTable.ROOM;
			for (int index = 1; rs.next(); index ++) {
				int room_id = DBData.getInt(rs, ROOM.ROOM_ID);
				String room_name = DBData.getString(rs, ROOM.ROOM_NAME);
				String room_desc = DBData.getString(rs, ROOM.ROOM_DESC);
				Date room_creation = DBData.getDate(rs, ROOM.ROOM_CREATIONTIME);
				Date room_modified = DBData.getDate(rs, ROOM.ROOM_MODIFIEDTIME);
				String area_name = DBData.getString(rs, AREA.AREA_NAME);
				List<String> list = Arrays.asList(Integer.toString(room_id), "", Integer.toString(index),
						room_name, room_desc, area_name, DBData.format(room_creation), DBData.format(room_modified));
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
			throw new EucalyptusServiceException(new ClientMessage("", "获取机房列表失败"));
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
	
	public synchronized void addRoom(Session session, String room_name, String room_desc, String area_name) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (isEmpty(room_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的机房名称"));
		}
		if (isEmpty(area_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的区域名称"));
		}
		if (room_desc == null) {
			room_desc = "";
		}
		try {
			dbproc.createRoom(room_name, room_desc, area_name);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "创建机房失败"));
		}
	}
	
	public synchronized void deleteRoom(Session session, List<Integer> room_ids) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		try {
			if (!room_ids.isEmpty()) {
				dbproc.deleteRoom(room_ids);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除机房失败"));
		}
	}
	
	public synchronized void modifyRoom(Session session, int room_id, String room_desc) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (room_desc == null) {
			room_desc = "";
		}
		try {
			dbproc.modifyRoom(room_id, room_desc);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "修改机房失败"));
		}
	}
	
	public synchronized List<String> lookupRoomNamesByAreaName(Session session, String area_name) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		if (isEmpty(area_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的区域名称"));
		}
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupRoomNamesByAreaName(area_name);
			ResultSet rs = rsw.getResultSet();
			List<String> room_name_list = new LinkedList<String>();
			DBTableRoom ROOM = DBTable.ROOM;
			while (rs.next()) {
				String room_name = DBData.getString(rs, ROOM.ROOM_NAME);
				room_name_list.add(room_name);
			}
			return room_name_list;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "获取机房名称列表失败"));
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
	
	private RoomInfo getRoomInfo(ResultSet rs) throws Exception {
	    DBTableRoom ROOM = DBTable.ROOM;
	    int room_id = DBData.getInt(rs, ROOM.ROOM_ID);
        String room_name = DBData.getString(rs, ROOM.ROOM_NAME);
        String room_desc = DBData.getString(rs, ROOM.ROOM_DESC);
        Date room_creationtime = DBData.getDate(rs, ROOM.ROOM_CREATIONTIME);
        Date room_modifiedtime = DBData.getDate(rs, ROOM.ROOM_MODIFIEDTIME);
        int area_id = DBData.getInt(rs, ROOM.AREA_ID);
        return new RoomInfo(room_id, room_name, room_desc, room_creationtime, room_modifiedtime, area_id);
	}
	
	public synchronized RoomInfo lookupRoomInfoByID(int room_id) throws EucalyptusServiceException {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupRoomByID(room_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        return getRoomInfo(rs);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取机房信息失败"));
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
	
	public synchronized RoomInfo lookupRoomInfoByName(String room_name) throws EucalyptusServiceException {
		if (isEmpty(room_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", "无效的机房名称"));
		}
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupRoomByName(room_name);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        return getRoomInfo(rs);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取机房信息失败"));
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

class DeviceRoomDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceRoomDBProcWrapper.class.getName());
	
	private DBProcWrapper wrapper = DBProcWrapper.Instance();
		
	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}
	
	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
    public ResultSetWrapper lookupRoomByID(int room_id) throws Exception {
        DBTableRoom ROOM = DBTable.ROOM;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(ROOM).append(" WHERE ").append(ROOM.ROOM_ID).append(" = ").append(room_id);
        return doQuery(sb.toString());
    }
    
    public ResultSetWrapper lookupRoomByName(String room_name) throws Exception {
        DBTableRoom ROOM = DBTable.ROOM;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(ROOM).append(" WHERE ").append(ROOM.ROOM_NAME).append(" = ").appendString(room_name);
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
	
	public ResultSetWrapper lookupRoomByDate(Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending) throws Exception {
	    DBTableArea AREA = DBTable.AREA;
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(ROOM.ANY).append(", ").append(AREA.AREA_NAME).append(" FROM ");
		sb.append(ROOM).append(" LEFT JOIN ").append(AREA);
		sb.append(" ON ").append(ROOM.AREA_ID).append(" = ").append(AREA.AREA_ID).append(" WHERE 1=1");
		if (dateBegin != null || dateEnd != null) {
		    sb.append(" AND (");
		    appendBoundedDate(sb, ROOM.ROOM_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
		    appendBoundedDate(sb, ROOM.ROOM_MODIFIEDTIME, dateBegin, dateEnd);
		    sb.append(")");
		}
		if (sort != null) {
		    sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
		}
		return doQuery(sb.toString());
	}
	
	private int lookupAreaIDByName(String area_name) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			DBTableArea AREA = DBTable.AREA;
			DBStringBuilder sb = new DBStringBuilder();
			sb.append("SELECT ").append(AREA.AREA_ID).append(" FROM ").append(AREA);
			sb.append(" WHERE ").append(AREA.AREA_NAME).append(" = ").appendString(area_name);
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
	
	private void createRoom(String room_name, String room_desc, int area_id) throws Exception {
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("INSERT INTO ").append(ROOM).append(" (");
		sb.append(ROOM.ROOM_NAME).append(", ");
		sb.append(ROOM.ROOM_DESC).append(", ");
		sb.append(ROOM.AREA_ID).append(", ");
		sb.append(ROOM.ROOM_CREATIONTIME);
		sb.append(") VALUES (");
		sb.appendString(room_name).append(", ");
		sb.appendString(room_desc).append(", ");
		sb.append(area_id).append(", ");
		sb.appendDate(new Date()).append(")");
		doUpdate(sb.toString());
	}
	
	public void createRoom(String room_name, String room_desc, String area_name) throws Exception {
		createRoom(room_name, room_desc, lookupAreaIDByName(area_name));
	}
	
	public void deleteRoom(List<Integer> room_ids) throws Exception {
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		
		StringBuilder ids = new StringBuilder();
		int total = 0;
		for (int room_id : room_ids) {
			if (total ++ != 0) {
				ids.append(", ");
			}
			ids.append(room_id);
		}
		
		sb.append("DELETE FROM ").append(ROOM).append(" WHERE ");
		sb.append(ROOM.ROOM_ID).append(" IN (").append(ids.toString()).append(")");
		doUpdate(sb.toString());
	}
	
	public void modifyRoom(int room_id, String room_desc) throws Exception {
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(ROOM).append(" SET ");
		sb.append(ROOM.ROOM_DESC).append(" = ").appendString(room_desc).append(", ");
		sb.append(ROOM.ROOM_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ");
		sb.append(ROOM.ROOM_ID).append(" = ").append(room_id);
		doUpdate(sb.toString());
	}
	
	public ResultSetWrapper lookupRoomNamesByAreaName(String area_name) throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT DISTINCT(").append(ROOM.ROOM_NAME).append(") FROM ");
		sb.append(ROOM).append(" LEFT JOIN ").append(AREA).append(" ON ").append(ROOM.AREA_ID).append(" = ").append(AREA.AREA_ID);
		sb.append(" WHERE ").append(AREA.AREA_NAME).append(" = ").appendString(area_name);
		sb.append(" ORDER BY ").append(ROOM.ROOM_NAME);
		return doQuery(sb.toString());
	}
	
}
