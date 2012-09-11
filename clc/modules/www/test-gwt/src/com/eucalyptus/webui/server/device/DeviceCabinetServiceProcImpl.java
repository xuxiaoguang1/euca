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

public class DeviceCabinetServiceProcImpl {
	
	private DeviceCabinetDBProcWrapper dbproc = new DeviceCabinetDBProcWrapper();
	
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
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
	public synchronized SearchResult lookupCabinetByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
					throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupCabinetByDate(creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			DBTableCabinet CABINET = DBTable.CABINET;
			for (int index = 1; rs.next(); index ++) {
				int cabinet_id = DBData.getInt(rs, CABINET.CABINET_ID);
				String cabinet_name = DBData.getString(rs, CABINET.CABINET_NAME);
				String cabinet_desc = DBData.getString(rs, CABINET.CABINET_DESC);
				Date cabinet_creation = DBData.getDate(rs, CABINET.CABINET_CREATIONTIME);
				Date cabinet_modified = DBData.getDate(rs, CABINET.CABINET_MODIFIEDTIME);
				List<String> list = Arrays.asList(Integer.toString(cabinet_id), "", Integer.toString(index),
						cabinet_name, cabinet_desc, DBData.format(cabinet_creation), DBData.format(cabinet_modified));
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
			throw new EucalyptusServiceException(new ClientMessage("", "获取机柜列表失败"));
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
	
	public synchronized void addCabinet(Session session, String cabinet_name, String cabinet_desc, String room_name) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (isEmpty(cabinet_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的机柜名称 = '%s'", cabinet_name)));
		}
		if (isEmpty(room_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的机房名称 = '%s'", room_name)));
		}
		if (cabinet_desc == null) {
			cabinet_desc = "";
		}
		try {
			dbproc.createCabinet(cabinet_name, cabinet_desc, room_name);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "创建机柜失败"));
		}
	}
	
	public synchronized void deleteCabinet(Session session, Collection<Integer> cabinet_ids) throws EucalyptusServiceException {
		if (cabinet_ids != null && !cabinet_ids.isEmpty()) {
			if (!getUser(session).isSystemAdmin()) {
				throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
			}
			try {
				for (int cabinet_id : cabinet_ids) {
					dbproc.deleteCabinet(cabinet_id);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new EucalyptusServiceException(new ClientMessage("", "删除机柜失败"));
			}
		}
	}
	
	public synchronized void modifyCabinet(Session session, int cabinet_id, String cabinet_desc) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (cabinet_desc == null) {
			cabinet_desc = "";
		}
		try {
			dbproc.modifyCabinet(cabinet_id, cabinet_desc);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "修改机柜失败"));
		}
	}
	
}

class DeviceCabinetDBProcWrapper {
	
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
	
	public ResultSetWrapper lookupCabinetByDate(Date creationtimeBegin, Date creationtimeEnd,
			Date modifiedtimeBegin, Date modifiedtimeEnd) throws Exception {
		DBTableCabinet CABINET = DBTable.CABINET;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(CABINET).append(" WHERE 1=1");
		if (creationtimeBegin != null) {
			sb.append(" AND ").append(CABINET.CABINET_CREATIONTIME.getName()).append(" >= ").appendDate(creationtimeBegin);
		}
		if (creationtimeEnd != null) {
			sb.append(" AND ").append(CABINET.CABINET_CREATIONTIME.getName()).append(" <= ").appendDate(creationtimeEnd);
		}
		if (modifiedtimeBegin != null) {
			sb.append(" AND ").append(CABINET.CABINET_MODIFIEDTIME.getName()).append(" >= ").appendDate(modifiedtimeBegin);
		}
		if (modifiedtimeEnd != null) {
			sb.append(" AND ").append(CABINET.CABINET_MODIFIEDTIME.getName()).append(" <= ").appendDate(modifiedtimeEnd);
		}
		if (modifiedtimeBegin != null || modifiedtimeEnd != null) {
			sb.append(" AND ").append(CABINET.CABINET_MODIFIEDTIME.getName()).append(" != ").appendString("0000-00-00");
		}
		return doQuery(sb.toString());
	}
	
	private int lookupRoomIDByName(String room_name) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			DBTableRoom ROOM = DBTable.ROOM;
			DBStringBuilder sb = new DBStringBuilder();
			sb.append("SELECT ").append(ROOM.ROOM_ID).append(" FROM ").append(ROOM);
			sb.append(" WHERE ").append(ROOM.ROOM_NAME).append(" = ").appendString(room_name);
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
	
	private void createCabinet(String cabinet_name, String cabinet_desc, int room_id) throws Exception {
		DBTableCabinet CABINET = DBTable.CABINET;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("INSERT INTO ").append(CABINET).append(" (");
		sb.append(CABINET.CABINET_NAME).append(", ");
		sb.append(CABINET.CABINET_DESC).append(", ");
		sb.append(CABINET.ROOM_ID).append(", ");
		sb.append(CABINET.CABINET_CREATIONTIME);
		sb.append(") VALUES (");
		sb.appendString(cabinet_name).append(", ");
		sb.appendString(cabinet_desc).append(", ");
		sb.append(room_id).append(", ");
		sb.appendDate(new Date()).append(")");
		doUpdate(sb.toString());
	}
	
	public void createCabinet(String cabinet_name, String cabinet_desc, String room_name) throws Exception {
		createCabinet(cabinet_name, cabinet_desc, lookupRoomIDByName(room_name));
	}
	
	public void deleteCabinet(int cabinet_id) throws Exception {
		DBTableCabinet CABINET = DBTable.CABINET;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("DELETE FROM ").append(CABINET).append(" WHERE ");
		sb.append(CABINET.CABINET_ID).append(" = ").append(cabinet_id);
		doUpdate(sb.toString());
	}
	
	public void modifyCabinet(int cabinet_id, String cabinet_desc) throws Exception {
		DBTableCabinet CABINET = DBTable.CABINET;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(CABINET).append(" SET ");
		sb.append(CABINET.CABINET_DESC).append(" = ").appendString(cabinet_desc).append(", ");
		sb.append(CABINET.CABINET_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ");
		sb.append(CABINET.CABINET_ID).append(" = ").append(cabinet_id);
		doUpdate(sb.toString());
	}
	
}
