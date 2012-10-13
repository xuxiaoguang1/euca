package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
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
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceTemplatePriceService {
	
	private DeviceTemplatePriceDBProcWrapper dbproc = new DeviceTemplatePriceDBProcWrapper();
	
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private DeviceTemplatePriceService() {
	}
	
	private static DeviceTemplatePriceService instance = new DeviceTemplatePriceService();
	
	public static DeviceTemplatePriceService getInstance() {
		return instance;
	}
	
	private List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "序号"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "单价"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("0%", false, null),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "CPU单价(元/天)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(false, "8%", new ClientMessage("", "内存单价(元/天/MB)"),
	        		TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("", "硬盘单价(元/天/MB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
    		new SearchResultFieldDesc(false, "8%", new ClientMessage("", "带宽单价(元/天/KB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
	private static final long DIV_BW = 1024;
	private static final long DIV_MEM = 1024 * 1024;
	private static final long DIV_DISK = 1000 * 1000;
	
	public synchronized double lookupTemplatePriceByPriceID(int template_price_id) throws EucalyptusServiceException {
	    ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupTemplatePriceByPriceID(template_price_id);
            ResultSet rs = rsw.getResultSet();
            if (!rs.next()) {
                throw new EucalyptusServiceException(new ClientMessage("", "查询模板定价失败"));
            }
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
            double template_mem = (double)DBData.getLong(rs, TEMPLATE.TEMPLATE_MEM) / DIV_MEM;
            double template_disk = (double)DBData.getLong(rs, TEMPLATE.TEMPLATE_DISK) / DIV_DISK;
            double template_bw = (double)DBData.getLong(rs, TEMPLATE.TEMPLATE_BW) / DIV_BW;
            int template_ncpus = DBData.getInt(rs, TEMPLATE.TEMPLATE_NCPUS);
            double template_price_cpu = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_CPU);
            double template_price_mem = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_MEM);
            double template_price_disk = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_DISK);
            double template_price_bw = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_BW);
            double total_price = template_price_cpu * template_ncpus + template_mem * template_price_mem + template_disk * template_price_disk + template_bw * template_price_bw;
            return Double.parseDouble(DBData.format(total_price));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取模板定价失败"));
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
	
	public synchronized SearchResult lookupTemplatePriceByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
					throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupTemplatePriceByDate(creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
			DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
			for (int index = 1; rs.next(); index ++) {
				int template_price_id = DBData.getInt(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_ID);
				String template_name = DBData.getString(rs, TEMPLATE.TEMPLATE_NAME);
				String template_price_desc = DBData.getString(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_DESC);
				String template_cpu = DBData.getString(rs, TEMPLATE.TEMPLATE_CPU);
				double template_mem = (double)DBData.getLong(rs, TEMPLATE.TEMPLATE_MEM) / DIV_MEM;
				double template_disk = (double)DBData.getLong(rs, TEMPLATE.TEMPLATE_DISK) / DIV_DISK;
				double template_bw = (double)DBData.getLong(rs, TEMPLATE.TEMPLATE_BW) / DIV_BW;
				int template_ncpus = DBData.getInt(rs, TEMPLATE.TEMPLATE_NCPUS);
				double template_price_cpu = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_CPU);
				double template_price_mem = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_MEM);
				double template_price_disk = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_DISK);
				double template_price_bw = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_BW);
				double total_price = template_price_cpu * template_ncpus + template_mem * template_price_mem + template_disk * template_price_disk + template_bw * template_price_bw;
				total_price = (double)(int)(total_price * 100) / 100;
				Date template_price_creation = DBData.getDate(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME);
				Date template_price_modified = DBData.getDate(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME);
				List<String> list = Arrays.asList(Integer.toString(template_price_id), "", Integer.toString(index),
						template_name, DBData.format(total_price), template_price_desc, template_cpu, Integer.toString(template_ncpus),
						DBData.format(template_mem),  DBData.format(template_disk), DBData.format(template_bw),
						DBData.format(template_price_cpu), DBData.format(template_price_mem), DBData.format(template_price_disk), DBData.format(template_price_bw),
						DBData.format(template_price_creation), DBData.format(template_price_modified));
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
			throw new EucalyptusServiceException(new ClientMessage("", "获取模板定价列表失败"));
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
	
	public synchronized void deleteTemplatePrice(Session session, List<Integer> template_price_ids) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		try {
			for (int template_price_id : template_price_ids) {
				dbproc.deleteTemplatePrice(template_price_id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除模板定价失败"));
		}
	}
	
	public synchronized List<String> lookupTemplateUnpriced(Session session) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupTemplateUnpriced();
            ResultSet rs = rsw.getResultSet();
            List<String> template_name_list = new LinkedList<String>();
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            while (rs.next()) {
                String template_name = DBData.getString(rs, TEMPLATE.TEMPLATE_NAME);
                template_name_list.add(template_name);
            }
            return template_name_list;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取未定价模板列表失败"));
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
	
	public synchronized void modifyTemplatePrice(Session session, int template_price_id, String template_price_desc,
            double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
	    if (template_price_desc == null) {
	        template_price_desc = "";
	    }
	    try {
	        dbproc.modifyTemplatePrice(template_price_id, template_price_desc, template_price_cpu, template_price_mem, template_price_disk, template_price_bw);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        throw new EucalyptusServiceException(new ClientMessage("", "编辑模板定价失败"));
	    }
	}
	
	public synchronized void createTemplatePriceByID(Session session, int template_id, String template_price_desc,
			double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
	    if (template_price_desc == null) {
	        template_price_desc = "";
	    }
	    try {
	    	dbproc.createTemplatePriceByID(template_id, template_price_desc, template_price_cpu, template_price_mem, template_price_disk, template_price_bw);
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	throw new EucalyptusServiceException(new ClientMessage("", "添加模板定价失败"));
	    }
	}
	
}

class DeviceTemplatePriceDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceTemplatePriceDBProcWrapper.class.getName());
	
	private DBProcWrapper wrapper = DBProcWrapper.Instance();
		
	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}
	
	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
	public ResultSetWrapper lookupTemplatePriceByDate(Date creationtimeBegin, Date creationtimeEnd,
			Date modifiedtimeBegin, Date modifiedtimeEnd) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(TEMPLATE_PRICE).append(" LEFT JOIN ").append(TEMPLATE);
		sb.append(" ON ").append(TEMPLATE_PRICE.TEMPLATE_ID).append(" = ").append(TEMPLATE.TEMPLATE_ID);
		sb.append(" WHERE 1=1");
		if (creationtimeBegin != null) {
			sb.append(" AND ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME.getName());
			sb.append(" >= ").appendDate(creationtimeBegin);
		}
		if (creationtimeEnd != null) {
			sb.append(" AND ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME.getName());
			sb.append(" <= ").appendDate(creationtimeEnd);
		}
		if (modifiedtimeBegin != null) {
			sb.append(" AND ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME.getName());
			sb.append(" >= ").appendDate(modifiedtimeBegin);
		}
		if (modifiedtimeEnd != null) {
			sb.append(" AND ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME.getName());
			sb.append(" <= ").appendDate(modifiedtimeEnd);
		}
		if (modifiedtimeBegin != null || modifiedtimeEnd != null) {
			sb.append(" AND ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME.getName());
			sb.append(" != ").appendString("0000-00-00");
		}
		sb.append(" ORDER BY ").append(TEMPLATE.TEMPLATE_NAME);
		return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupTemplatePriceByTemplateName(String template_name) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(TEMPLATE_PRICE).append(" LEFT JOIN ").append(TEMPLATE);
		sb.append(" ON ").append(TEMPLATE_PRICE.TEMPLATE_ID).append(" = ").append(TEMPLATE.TEMPLATE_ID);
		sb.append(" WHERE ").append(TEMPLATE.TEMPLATE_NAME).append(" = ").appendString(template_name);
		return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupTemplatePriceByTemplateID(int template_id) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(TEMPLATE_PRICE).append(" LEFT JOIN ").append(TEMPLATE);
		sb.append(" ON ").append(TEMPLATE_PRICE.TEMPLATE_ID).append(" = ").append(TEMPLATE.TEMPLATE_ID);
		sb.append(" WHERE ").append(TEMPLATE.TEMPLATE_ID).append(" = ").append(template_id);
		return doQuery(sb.toString());
	}
	
	public ResultSetWrapper lookupTemplatePriceByPriceID(int template_price_id) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(TEMPLATE_PRICE).append(" LEFT JOIN ").append(TEMPLATE);
		sb.append(" ON ").append(TEMPLATE_PRICE.TEMPLATE_ID).append(" = ").append(TEMPLATE.TEMPLATE_ID);
		sb.append(" WHERE ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_ID).append(" = ").append(template_price_id);
		return doQuery(sb.toString());
	}
	
	public void deleteTemplatePrice(int template_price_id) throws Exception {
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("DELETE FROM ").append(TEMPLATE_PRICE).append(" WHERE ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_ID).append(" = ").append(template_price_id);
		doUpdate(sb.toString());
	}
	
	public void modifyTemplatePrice(int template_price_id, String template_price_desc,
			double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw) throws Exception {
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(TEMPLATE_PRICE).append(" SET ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_DESC).append(" = ").appendString(template_price_desc).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_CPU).append(" = ").append(template_price_cpu).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_MEM).append(" = ").append(template_price_mem).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_DISK).append(" = ").append(template_price_disk).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_BW).append(" = ").append(template_price_bw).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_ID).append(" = ").append(template_price_id);
		doUpdate(sb.toString());
	}
	
	public ResultSetWrapper lookupTemplateUnpriced() throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(TEMPLATE.TEMPLATE_NAME).append(" FROM ");
		sb.append(TEMPLATE).append(" LEFT JOIN ").append(TEMPLATE_PRICE);
		sb.append(" ON ").append(TEMPLATE.TEMPLATE_ID).append(" = ").append(TEMPLATE_PRICE.TEMPLATE_ID);
		sb.append(" WHERE ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_ID).append(" IS NULL");
		return doQuery(sb.toString());
	}
	
	public void createTemplatePriceByID(int template_id, String template_price_desc,
			double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw) throws Exception {
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("INSERT INTO ").append(TEMPLATE_PRICE).append(" (");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_DESC).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_CPU).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_MEM).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_DISK).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_BW).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_ID).append(", ");
		sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME);
		sb.append(") VALUES (");
		sb.appendString(template_price_desc).append(", ");
		sb.append(template_price_cpu).append(", ");
		sb.append(template_price_mem).append(", ");
		sb.append(template_price_disk).append(", ");
		sb.append(template_price_bw).append(", ");
		sb.append(template_id).append(", ");
		sb.appendDate(new Date()).append(")");
		doUpdate(sb.toString());
	}
	
}
