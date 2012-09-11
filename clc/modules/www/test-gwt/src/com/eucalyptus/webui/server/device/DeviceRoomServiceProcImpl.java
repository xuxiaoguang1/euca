package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
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
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceRoomServiceProcImpl {
	
	private DeviceRoomDBProcWrapper dbproc = new DeviceRoomDBProcWrapper();
	
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
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "所在区域"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
	public synchronized SearchResult lookupRoomByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
					throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupRoomByDate(creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
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
			throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的机房名称 = '%s'", room_name)));
		}
		if (isEmpty(area_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的区域名称 = '%s'", area_name)));
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
	
	public synchronized void deleteRoom(Session session, Collection<Integer> room_ids) throws EucalyptusServiceException {
		if (room_ids != null && !room_ids.isEmpty()) {
			if (!getUser(session).isSystemAdmin()) {
				throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
			}
			try {
				for (int room_id : room_ids) {
					dbproc.deleteRoom(room_id);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new EucalyptusServiceException(new ClientMessage("", "删除机房失败"));
			}
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
		ResultSetWrapper rsw = null;
		try {
			if (isEmpty(area_name)) {
				throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的区域名称 = '%s'", area_name)));
			}
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
	
	public ResultSetWrapper lookupRoomByDate(Date creationtimeBegin, Date creationtimeEnd,
			Date modifiedtimeBegin, Date modifiedtimeEnd) throws Exception {
	    DBTableArea AREA = DBTable.AREA;
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(ROOM).append(" LEFT JOIN ").append(AREA);
		sb.append(" ON ").append(ROOM.AREA_ID).append(" = ").append(AREA.AREA_ID);
		sb.append(" WHERE 1=1");
		if (creationtimeBegin != null) {
			sb.append(" AND ").append(ROOM.ROOM_CREATIONTIME.getName()).append(" >= ").appendDate(creationtimeBegin);
		}
		if (creationtimeEnd != null) {
			sb.append(" AND ").append(ROOM.ROOM_CREATIONTIME.getName()).append(" <= ").appendDate(creationtimeEnd);
		}
		if (modifiedtimeBegin != null) {
			sb.append(" AND ").append(ROOM.ROOM_MODIFIEDTIME.getName()).append(" >= ").appendDate(modifiedtimeBegin);
		}
		if (modifiedtimeEnd != null) {
			sb.append(" AND ").append(ROOM.ROOM_MODIFIEDTIME.getName()).append(" <= ").appendDate(modifiedtimeEnd);
		}
		if (modifiedtimeBegin != null || modifiedtimeEnd != null) {
			sb.append(" AND ").append(ROOM.ROOM_MODIFIEDTIME.getName()).append(" != ").appendString("0000-00-00");
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
	
	public void deleteRoom(int room_id) throws Exception {
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("DELETE FROM ").append(ROOM).append(" WHERE ");
		sb.append(ROOM.ROOM_ID).append(" = ").append(room_id);
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
	
	private ResultSetWrapper lookupRoomNamesByAreaID(int area_id) throws Exception {
		DBTableRoom ROOM = DBTable.ROOM;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(ROOM.ROOM_NAME).append(" FROM ").append(ROOM);
		sb.append(" WHERE ").append(ROOM.AREA_ID).append(" = ").append(area_id);
		return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupRoomNamesByAreaName(String area_name) throws Exception {
		return lookupRoomNamesByAreaID(lookupAreaIDByName(area_name));
	}
	
}
