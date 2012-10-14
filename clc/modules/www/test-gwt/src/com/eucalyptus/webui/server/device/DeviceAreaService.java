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
import com.eucalyptus.webui.shared.resource.device.AreaInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceAreaService {
	
	private DeviceAreaDBProcWrapper dbproc = new DeviceAreaDBProcWrapper();
	
	private DeviceAreaService() {
	}
	
	private static DeviceAreaService instance = new DeviceAreaService();
	
	public static DeviceAreaService getInstance() {
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
			new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
			new SearchResultFieldDesc(false, "3EM", new ClientMessage("", "序号"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "描述"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(true, "8%", new ClientMessage("", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
	private DBTableColumn getSortColumn(SearchRange range) {
	    switch (range.getSortField()) {
	    case 3: return DBTable.AREA.AREA_NAME;
	    case 4: return DBTable.AREA.AREA_DESC;
	    case 5: return DBTable.AREA.AREA_CREATIONTIME;
	    case 6: return DBTable.AREA.AREA_MODIFIEDTIME;
	    }
	    return null;
	}
	
	public synchronized SearchResult lookupAreaByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd)  throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupAreaByDate(dateBegin, dateEnd, getSortColumn(range), range.isAscending());
			ResultSet rs = rsw.getResultSet();
			ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
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
			throw new EucalyptusServiceException(new ClientMessage("", "无效的区域名称"));
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
	
	public synchronized void deleteArea(Session session, List<Integer> area_ids) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		try {
			if (!area_ids.isEmpty()) {
				dbproc.deleteArea(area_ids);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除区域失败"));
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
	
    private AreaInfo getAreaInfo(ResultSet rs) throws Exception {
        DBTableArea AREA = DBTable.AREA;
        int area_id = DBData.getInt(rs, AREA.AREA_ID);
        String area_name = DBData.getString(rs, AREA.AREA_NAME);
        String area_desc = DBData.getString(rs, AREA.AREA_DESC);
        Date area_creationtime = DBData.getDate(rs, AREA.AREA_CREATIONTIME);
        Date area_modifiedtime = DBData.getDate(rs, AREA.AREA_MODIFIEDTIME);
        return new AreaInfo(area_id, area_name, area_desc, area_creationtime, area_modifiedtime);
    }
	
	public synchronized AreaInfo lookupAreaInfoByID(int area_id) throws EucalyptusServiceException {
	    ResultSetWrapper rsw = null;
	    try {
	        rsw = dbproc.lookupAreaByID(area_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        return getAreaInfo(rs);
	    }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取区域信息失败"));
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
    
    public synchronized AreaInfo lookupAreaInfoByName(String area_name) throws EucalyptusServiceException {
        if (isEmpty(area_name)) {
            throw new EucalyptusServiceException(new ClientMessage("", "无效的区域名称"));
        }
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupAreaByName(area_name);
            ResultSet rs = rsw.getResultSet();
            rs.next();
            return getAreaInfo(rs);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取区域信息失败"));
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
	
	public ResultSetWrapper lookupAreaByName(String area_name) throws Exception {
	    DBTableArea AREA = DBTable.AREA;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT * FROM ").append(AREA).append(" WHERE ").append(AREA.AREA_NAME).append(" = ").appendString(area_name);
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
	
	public ResultSetWrapper lookupAreaByDate(Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending) throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(AREA).append(" WHERE 1=1");
		if (dateBegin != null || dateEnd != null) {
			sb.append(" AND (");
			appendBoundedDate(sb, AREA.AREA_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, AREA.AREA_MODIFIEDTIME, dateBegin, dateEnd);
			sb.append(")");
		}
		if (sort != null) {
		    sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
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
	
	public void deleteArea(List<Integer> area_ids) throws Exception {
		DBTableArea AREA = DBTable.AREA;
		DBStringBuilder sb = new DBStringBuilder();
		
		StringBuilder ids = new StringBuilder();
		int total = 0;
		for (int area_id : area_ids) {
			if (total ++ != 0) {
				ids.append(", ");
			}
			ids.append(area_id);
		}
		
		sb.append("DELETE FROM ").append(AREA).append(" WHERE ");
		sb.append(AREA.AREA_ID).append(" IN (").append(ids.toString()).append(")");
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
		sb.append("SELECT DISTINCT(").append(AREA.AREA_NAME).append(") FROM ").append(AREA).append(" WHERE 1=1");
		sb.append(" ORDER BY ").append(AREA.AREA_NAME);
		return doQuery(sb.toString());
	}
	
}
