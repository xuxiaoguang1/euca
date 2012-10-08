package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.eucalyptus.webui.shared.resource.Template;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceTemplateService {
	
	private DeviceTemplateDBProcWrapper dbproc = new DeviceTemplateDBProcWrapper();
	
	private DeviceTemplateService() {
    }
    
    private static DeviceTemplateService instance = new DeviceTemplateService();
    
    public static DeviceTemplateService getInstance() {
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
		    new SearchResultFieldDesc("4%", false, new ClientMessage("", "")),
		    new SearchResultFieldDesc(false, "8%", new ClientMessage("", "序号"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "名称"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "CPU名称"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "CPU数量"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),                    
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "内存容量(MB)"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "硬盘容量(MB)"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "带宽(KB)"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "镜像"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "创建时间"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false),
		    new SearchResultFieldDesc(true, "8%", new ClientMessage("", "修改时间"),
		            TableDisplay.MANDATORY, Type.TEXT, false, false));
    
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.TEMPLATE.TEMPLATE_NAME: return DBTable.TEMPLATE.TEMPLATE_NAME;
        case CellTableColumns.TEMPLATE.TEMPLATE_CPU: return DBTable.TEMPLATE.TEMPLATE_CPU;
        case CellTableColumns.TEMPLATE.TEMPLATE_NCPUS: return DBTable.TEMPLATE.TEMPLATE_NCPUS;
        case CellTableColumns.TEMPLATE.TEMPLATE_MEM: return DBTable.TEMPLATE.TEMPLATE_MEM;
        case CellTableColumns.TEMPLATE.TEMPLATE_DISK: return DBTable.TEMPLATE.TEMPLATE_DISK;
        case CellTableColumns.TEMPLATE.TEMPLATE_BW: return DBTable.TEMPLATE.TEMPLATE_BW;
        case CellTableColumns.TEMPLATE.TEMPLATE_IMAGE: return DBTable.TEMPLATE.TEMPLATE_IMAGE;
        case CellTableColumns.TEMPLATE.TEMPLATE_CREATIONTIME: return DBTable.TEMPLATE.TEMPLATE_CREATIONTIME;
        case CellTableColumns.TEMPLATE.TEMPLATE_MODIFIEDTIME: return DBTable.TEMPLATE.TEMPLATE_MODIFIEDTIME;
        }
        return null;
    }
    
    public synchronized SearchResult lookupTemplateByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
    	ResultSetWrapper rsw = null;
        try {
        	rsw = dbproc.lookupTemplateByDate(dateBegin, dateEnd, getSortColumn(range), range.isAscending());
        	ResultSet rs = rsw.getResultSet();
        	ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
        	DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
        	for (int index = 1; rs.next(); index ++) {
        		int template_id = DBData.getInt(rs, TEMPLATE.TEMPLATE_ID);
        		String template_name = DBData.getString(rs, TEMPLATE.TEMPLATE_NAME);
        		String template_desc = DBData.getString(rs, TEMPLATE.TEMPLATE_DESC);
        		String template_cpu = DBData.getString(rs, TEMPLATE.TEMPLATE_CPU);
        		int template_ncpus = DBData.getInt(rs, TEMPLATE.TEMPLATE_NCPUS);
        		long template_mem = DBData.getLong(rs, TEMPLATE.TEMPLATE_MEM);
        		long template_disk = DBData.getLong(rs, TEMPLATE.TEMPLATE_DISK);
        		int template_bw = DBData.getInt(rs, TEMPLATE.TEMPLATE_BW);
        		String template_image = DBData.getString(rs, TEMPLATE.TEMPLATE_IMAGE);
        		Date template_creationtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_CREATIONTIME);
        		Date template_modifiedtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_MODIFIEDTIME);
            	List<String> list = Arrays.asList(Integer.toString(template_id), "", Integer.toString(index),
            			template_name, template_desc, template_cpu, Integer.toString(template_ncpus),
            			Long.toString(template_mem), Long.toString(template_disk), Integer.toString(template_bw), template_image,
            			DBData.format(template_creationtime), DBData.format(template_modifiedtime));
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
            throw new EucalyptusServiceException(new ClientMessage("", "获取模板列表失败"));
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
    
    public synchronized void addTemplate(Session session, String template_name, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (isEmpty(template_name)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的模板名称"));
    	}
    	if (isEmpty(template_cpu)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU名称"));
    	}
    	if (template_ncpus <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU数量"));
    	}
    	if (template_mem <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的内存数量"));
    	}
    	if (template_disk <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的硬盘数量"));
    	}
    	if (template_bw <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的带宽"));
    	}
    	if (isEmpty(template_image)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的镜像名称"));
    	}
    	if (template_desc == null) {
    		template_desc = "";
    	}
    	try {
    		dbproc.createTempalte(template_name, template_desc, template_cpu, template_ncpus, template_mem, template_disk, template_bw, template_image);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "创建模板失败"));
    	}
    }
    
    public synchronized void deleteTemplate(Session session, List<Integer> template_ids) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		try {
			if (!template_ids.isEmpty()) {
				dbproc.deleteTemplate(template_ids);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除模板失败"));
		}
    }
    
    public synchronized void modifyTempalte(Session session, int template_id, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image) throws EucalyptusServiceException {
    	if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
    	if (isEmpty(template_cpu)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU名称"));
    	}
    	if (template_ncpus <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的CPU数量"));
    	}
    	if (template_mem <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的内存数量"));
    	}
    	if (template_disk <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的硬盘数量"));
    	}
    	if (template_bw <= 0) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的带宽"));
    	}
    	if (isEmpty(template_image)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的镜像名称"));
    	}
    	if (template_desc == null) {
    		template_desc = "";
    	}
    	try {
    		dbproc.modifyTemplate(template_id, template_desc, template_cpu, template_ncpus, template_mem, template_disk, template_bw, template_image);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new EucalyptusServiceException(new ClientMessage("", "修改模板失败"));
    	}
    }
    
    private synchronized TemplateInfo convertToTemplateInfo(ResultSet rs) throws Exception {
    	DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
    	int template_id = DBData.getInt(rs, TEMPLATE.TEMPLATE_ID);
		String template_name = DBData.getString(rs, TEMPLATE.TEMPLATE_NAME);
		String template_desc = DBData.getString(rs, TEMPLATE.TEMPLATE_DESC);
		String template_cpu = DBData.getString(rs, TEMPLATE.TEMPLATE_CPU);
		int template_ncpus = DBData.getInt(rs, TEMPLATE.TEMPLATE_NCPUS);
		long template_mem = DBData.getLong(rs, TEMPLATE.TEMPLATE_MEM);
		long template_disk = DBData.getLong(rs, TEMPLATE.TEMPLATE_DISK);
		int template_bw = DBData.getInt(rs, TEMPLATE.TEMPLATE_BW);
		String template_image = DBData.getString(rs, TEMPLATE.TEMPLATE_IMAGE);
		Date template_creationtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_CREATIONTIME);
		Date template_modifiedtime = DBData.getDate(rs, TEMPLATE.TEMPLATE_MODIFIEDTIME);
		return new TemplateInfo(template_id, template_name, template_desc, template_cpu, template_ncpus, template_mem, template_disk, template_bw, template_image, template_creationtime, template_modifiedtime);
    }
    
    public synchronized TemplateInfo lookupTemplateInfoByID(Session session, int template_id) throws EucalyptusServiceException {
    	ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupTemplateByID(template_id);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        return convertToTemplateInfo(rs);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取模板信息失败"));
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
    
    public synchronized TemplateInfo lookupTemplateInfoByName(Session session, String template_name) throws EucalyptusServiceException {
    	if (isEmpty(template_name)) {
    		throw new EucalyptusServiceException(new ClientMessage("", "无效的模板名称"));
    	}
    	ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupTemplateByName(template_name);
	        ResultSet rs = rsw.getResultSet();
	        rs.next();
	        return convertToTemplateInfo(rs);
		}
		catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取模板信息失败"));
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
    
    public Template lookupTemplateByID(Session session, int template_id) throws EucalyptusServiceException {
    	TemplateInfo info = lookupTemplateInfoByID(session, template_id);
    	Template template = new Template();
    	template.setID(Integer.toString(info.template_id));
    	template.setName(info.template_name);
    	template.setCPU(info.template_cpu);
    	template.setNCPUs(Integer.toString(info.template_ncpus));
    	template.setMem(Long.toString(info.template_mem));
    	template.setDisk(Long.toString(info.template_disk));
    	template.setBw(Integer.toString(info.template_bw));
    	template.setImage(info.template_image);
    	return template;
    }
    
    public synchronized void actionTemplate(Session session, int template_id, int user_id, int life) throws EucalyptusServiceException {
    	throw new EucalyptusServiceException("没实现 ><");
    }
    
}

class DeviceTemplateDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceTemplateDBProcWrapper.class.getName());
	
	private DBProcWrapper wrapper = DBProcWrapper.Instance();
	
	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}

	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
	public ResultSetWrapper lookupTemplateByID(int template_id) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(TEMPLATE).append(" WHERE ").append(TEMPLATE.TEMPLATE_ID).append(" = ").append(template_id);
        return doQuery(sb.toString());
    }
	
	public ResultSetWrapper lookupTemplateByName(String template_name) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(TEMPLATE).append(" WHERE ").append(TEMPLATE.TEMPLATE_NAME).append(" = ").appendString(template_name);
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
	
	public ResultSetWrapper lookupTemplateByDate(Date dateBegin, Date dateEnd, DBTableColumn sort, boolean isAscending) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(TEMPLATE).append(" WHERE 1=1");
		if (dateBegin != null || dateEnd != null) {
			sb.append(" AND (");
			appendBoundedDate(sb, TEMPLATE.TEMPLATE_CREATIONTIME, dateBegin, dateEnd).append(" OR ");
			appendBoundedDate(sb, TEMPLATE.TEMPLATE_MODIFIEDTIME, dateBegin, dateEnd);
			sb.append(")");
		}
		if (sort != null) {
			sb.append(" ORDER BY ").append(sort).append(isAscending ? " ASC" : " DESC");
        }
		return doQuery(sb.toString());
	}
	
	public void createTempalte(String template_name, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("INSERT INTO ").append(TEMPLATE).append(" (");
		sb.append(TEMPLATE.TEMPLATE_NAME).append(", ");
		sb.append(TEMPLATE.TEMPLATE_DESC).append(", ");
		sb.append(TEMPLATE.TEMPLATE_CPU).append(", ");
		sb.append(TEMPLATE.TEMPLATE_NCPUS).append(", ");
		sb.append(TEMPLATE.TEMPLATE_MEM).append(", ");
		sb.append(TEMPLATE.TEMPLATE_DISK).append(", ");
		sb.append(TEMPLATE.TEMPLATE_BW).append(", ");
		sb.append(TEMPLATE.TEMPLATE_IMAGE).append(", ");
		sb.append(TEMPLATE.TEMPLATE_CREATIONTIME);
		sb.append(") VALUES (");
		sb.appendString(template_name).append(", ");
		sb.appendString(template_desc).append(", ");
		sb.appendString(template_cpu).append(", ");
		sb.append(template_ncpus).append(", ");
		sb.append(template_mem).append(", ");
		sb.append(template_disk).append(", ");
		sb.append(template_bw).append(", ");
		sb.appendString(template_image).append(", ");
		sb.appendDate(new Date()).append(")");
		doUpdate(sb.toString());
	}
	
	public void deleteTemplate(List<Integer> template_ids) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBStringBuilder sb = new DBStringBuilder();
		
		StringBuilder ids = new StringBuilder();
		int total = 0;
		for (int template_id : template_ids) {
			if (total ++ != 0) {
				ids.append(", ");
			}
			ids.append(template_id);
		}
		
		sb.append("DELETE FROM ").append(TEMPLATE).append(" WHERE ").append(TEMPLATE.TEMPLATE_ID).append(" IN (").append(ids.toString()).append(")");
        doUpdate(sb.toString());
	}
	
	public void modifyTemplate(int template_id, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(TEMPLATE).append(" SET ");
		sb.append(TEMPLATE.TEMPLATE_DESC).append(" = ").appendString(template_desc).append(", ");
		sb.append(TEMPLATE.TEMPLATE_CPU).append(" = ").appendString(template_cpu).append(", ");
		sb.append(TEMPLATE.TEMPLATE_NCPUS).append(" = ").append(template_ncpus).append(", ");
		sb.append(TEMPLATE.TEMPLATE_MEM).append(" = ").append(template_mem).append(", ");
		sb.append(TEMPLATE.TEMPLATE_DISK).append(" = ").append(template_disk).append(", ");
		sb.append(TEMPLATE.TEMPLATE_BW).append(" = ").append(template_bw).append(", ");
		sb.append(TEMPLATE.TEMPLATE_IMAGE).append(" = ").appendString(template_image).append(", ");
		sb.append(TEMPLATE.TEMPLATE_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ").append(TEMPLATE.TEMPLATE_ID).append(" = ").append(template_id);
		doUpdate(sb.toString());
	}
	
}
