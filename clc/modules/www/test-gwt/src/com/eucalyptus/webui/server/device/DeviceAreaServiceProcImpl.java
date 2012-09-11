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
import com.eucalyptus.webui.shared.resource.device.AreaInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceAreaServiceProcImpl {
	
	private DeviceAreaDBProcWrapper dbproc = new DeviceAreaDBProcWrapper();
	
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
	
	public synchronized SearchResult lookupAreaByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
					throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupAreaByDate(creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			DBTableArea AREA = DBTable.AREA;
			for (int index = 1; rs.next(); index ++) {
				int area_id = DBData.getInt(rs, AREA.AREA_ID);
				String area_name = DBData.getString(rs, AREA.AREA_NAME);
				String area_desc = DBData.getString(rs, AREA.AREA_DESC);
				Date area_creationtime = DBData.getDate(rs, AREA.AREA_CREATIONTIME);
				Date area_modifiedtime = DBData.getDate(rs, AREA.AREA_MODIFIEDTIME);
				List<String> list = Arrays.asList(Integer.toString(area_id), "", Integer.toString(index),
						area_name, area_desc, DBData.format(area_creationtime), DBData.format(area_modifiedtime));
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
			throw new EucalyptusServiceException();
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
	
	public synchronized void addArea(Session session, String area_name, String area_desc) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (isEmpty(area_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的区域名称 = '%s'", area_name)));
		}
		if (area_desc == null) {
			area_desc = "";
		}
		try {
			dbproc.createArea(area_name, area_desc);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "创建区域失败"));
		}
	}
	
	public synchronized void deleteArea(Session session, Collection<Integer> area_ids) throws EucalyptusServiceException {
		if (area_ids != null && !area_ids.isEmpty()) {
			if (!getUser(session).isSystemAdmin()) {
				throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
			}
			try {
				for (int area_id : area_ids) {
					dbproc.deleteArea(area_id);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new EucalyptusServiceException(new ClientMessage("", "删除区域失败"));
			}
		}
	}
	
	public synchronized void modifyArea(Session session, int area_id, String area_desc) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (area_desc == null) {
			area_desc = "";
		}
		try {
			dbproc.modifyArea(area_id, area_desc);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "修改区域失败"));
		}
	}
	
	public synchronized AreaInfo lookupAreaByID(Session session, int area_id) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
	        throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
	    }
	    ResultSetWrapper rsw = null;
	    try {
	        rsw = dbproc.lookupAreaByID(area_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
            DBTableArea AREA = DBTable.AREA;
            String area_name = DBData.getString(rs, AREA.AREA_NAME);
            String area_desc = DBData.getString(rs, AREA.AREA_DESC);
            Date area_creationtime = DBData.getDate(rs, AREA.AREA_CREATIONTIME);
            Date area_modifiedtime = DBData.getDate(rs, AREA.AREA_MODIFIEDTIME);
            return new AreaInfo(area_id, area_name, area_desc, area_creationtime, area_modifiedtime);
	    }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException();
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
	
	public synchronized List<String> lookupAreaNames(Session session) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupAreaNames();
			ResultSet rs = rsw.getResultSet();
			List<String> area_name_list = new LinkedList<String>();
			DBTableArea AREA = DBTable.AREA;
			while (rs.next()) {
				String area_name = DBData.getString(rs, AREA.AREA_NAME);
				area_name_list.add(area_name);
			}
			return area_name_list;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException();
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

class DeviceAreaDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceAreaDBProcWrapper.class.getName());
	
	private DBProcWrapper wrapper = DBProcWrapper.Instance();
		
	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}
	
	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
	public ResultSetWrapper lookupAreaByID(int area_id) throws Exception {
	    DBTableArea AREA = DBTable.AREA;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT * FROM ").append(AREA).append(" WHERE ").append(AREA.AREA_ID).append(" = ").append(area_id);
	    return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupAreaByDate(Date creationtimeBegin, Date creationtimeEnd,
			Date modifiedtimeBegin, Date modifiedtimeEnd) throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(AREA).append(" WHERE 1=1");
		if (creationtimeBegin != null) {
			sb.append(" AND ").append(AREA.AREA_CREATIONTIME.getName()).append(" >= ").appendDate(creationtimeBegin);
		}
		if (creationtimeEnd != null) {
			sb.append(" AND ").append(AREA.AREA_CREATIONTIME.getName()).append(" <= ").appendDate(creationtimeEnd);
		}
		if (modifiedtimeBegin != null) {
			sb.append(" AND ").append(AREA.AREA_MODIFIEDTIME.getName()).append(" >= ").appendDate(modifiedtimeBegin);
		}
		if (modifiedtimeEnd != null) {
			sb.append(" AND ").append(AREA.AREA_MODIFIEDTIME.getName()).append(" <= ").appendDate(modifiedtimeEnd);
		}
		if (modifiedtimeBegin != null || modifiedtimeEnd != null) {
			sb.append(" AND ").append(AREA.AREA_MODIFIEDTIME.getName()).append(" != ").appendString("0000-00-00");
		}
		return doQuery(sb.toString());
	}
	
	public void createArea(String area_name, String area_desc) throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("INSERT INTO ").append(AREA).append(" (");
		sb.append(AREA.AREA_NAME).append(", ");
		sb.append(AREA.AREA_DESC).append(", ");
		sb.append(AREA.AREA_CREATIONTIME);
		sb.append(") VALUES (");
		sb.appendString(area_name).append(", ");
		sb.appendString(area_desc).append(", ");
		sb.appendDate(new Date()).append(")");
		doUpdate(sb.toString());
	}
	
	public void deleteArea(int area_id) throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("DELETE FROM ").append(AREA).append(" WHERE ");
		sb.append(AREA.AREA_ID).append(" = ").append(area_id);
		doUpdate(sb.toString());
	}
	
	public void modifyArea(int area_id, String area_desc) throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(AREA).append(" SET ");
		sb.append(AREA.AREA_DESC).append(" = ").appendString(area_desc).append(", ");
		sb.append(AREA.AREA_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ");
		sb.append(AREA.AREA_ID).append(" = ").append(area_id);
		doUpdate(sb.toString());
	}
	
	public ResultSetWrapper lookupAreaNames() throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(AREA.AREA_NAME).append(" FROM ").append(AREA).append(" WHERE 1=1");
		return doQuery(sb.toString());
	}
	
}
