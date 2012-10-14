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

public class DeviceCPUPriceServiceProcImpl {
	
	private DeviceCPUPriceDBProcWrapper dbproc = new DeviceCPUPriceDBProcWrapper();
	
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
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "CPU名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "单价(元/天)"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
	public synchronized SearchResult lookupCPUPriceByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
					throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.lookupCPUPriceByDate(creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
			for (int index = 1; rs.next(); index ++) {
				int cpu_price_id = DBData.getInt(rs, CPU_PRICE.CPU_PRICE_ID);
				String cpu_name = DBData.getString(rs, CPU_PRICE.CPU_NAME);
				String cpu_price_desc = DBData.getString(rs, CPU_PRICE.CPU_PRICE_DESC);
				double cpu_price = DBData.getDouble(rs, CPU_PRICE.CPU_PRICE);
				Date cpu_price_creation = DBData.getDate(rs, CPU_PRICE.CPU_PRICE_CREATIONTIME);
				Date cpu_price_modified = DBData.getDate(rs, CPU_PRICE.CPU_PRICE_MODIFIEDTIME);
				List<String> list = Arrays.asList(Integer.toString(cpu_price_id), "", Integer.toString(index),
						cpu_name, cpu_price_desc, DBData.format(cpu_price), DBData.format(cpu_price_creation), DBData.format(cpu_price_modified));
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
			throw new EucalyptusServiceException(new ClientMessage("", "获取CPU定价列表失败"));
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
	
	public synchronized void addCPUPrice(Session session, String cpu_name, String cpu_price_desc, double cpu_price) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (isEmpty(cpu_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的CPU名称 = '%s'", cpu_name)));
		}
		if (cpu_price_desc == null) {
			cpu_price_desc = "";
		}
		try {
			dbproc.createCPUPrice(cpu_name, cpu_price_desc, cpu_price);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "添加CPU定价失败"));
		}
	}
	
	public synchronized void deleteCPUPrice(Session session, List<Integer> cpu_price_ids) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		try {
			for (int cpu_price_id : cpu_price_ids) {
				dbproc.deleteCPUPrice(cpu_price_id);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "删除CPU定价失败"));
		}
	}
	
	public synchronized void modifyCPUPrice(Session session, int cpu_price_id, String cpu_price_desc, double cpu_price) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (cpu_price_desc == null) {
			cpu_price_desc = "";
		}
		try {
			dbproc.modifyCPUPrice(cpu_price_id, cpu_price_desc, cpu_price);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "修改CPU定价失败"));
		}
	}
	
	public synchronized double lookupCPUPriceByName(String cpu_name) throws EucalyptusServiceException {
		if (isEmpty(cpu_name)) {
			throw new EucalyptusServiceException(new ClientMessage("", String.format("无效的CPU名称 = '%s'", cpu_name)));
		}
		try {
			return dbproc.lookupCPUPriceByName(cpu_name);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException(new ClientMessage("", "查询CPU定价失败"));
		}
	}
	
    public synchronized List<String> lookupCPUNamesUnpriced(Session session) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
        ResultSetWrapper rsw = null;
        try {
            rsw = dbproc.lookupCPUNamesUnpriced();
            ResultSet rs = rsw.getResultSet();
            List<String> cpu_name_list = new LinkedList<String>();
            DBTableCPU CPU = DBTable.CPU;
            while (rs.next()) {
                String cpu_name = DBData.getString(rs, CPU.CPU_NAME);
                cpu_name_list.add(cpu_name);
            }
            return cpu_name_list;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(new ClientMessage("", "获取未定价CPU列表失败"));
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

class DeviceCPUPriceDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceCPUPriceDBProcWrapper.class.getName());
	
	private DBProcWrapper wrapper = DBProcWrapper.Instance();
		
	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}
	
	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
	public ResultSetWrapper lookupCPUPriceByDate(Date creationtimeBegin, Date creationtimeEnd,
			Date modifiedtimeBegin, Date modifiedtimeEnd) throws Exception {
		DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(CPU_PRICE).append(" WHERE 1=1");
		if (creationtimeBegin != null) {
			sb.append(" AND ").append(CPU_PRICE.CPU_PRICE_CREATIONTIME.getName()).append(" >= ").appendDate(creationtimeBegin);
		}
		if (creationtimeEnd != null) {
			sb.append(" AND ").append(CPU_PRICE.CPU_PRICE_CREATIONTIME.getName()).append(" <= ").appendDate(creationtimeEnd);
		}
		if (modifiedtimeBegin != null) {
			sb.append(" AND ").append(CPU_PRICE.CPU_PRICE_MODIFIEDTIME.getName()).append(" >= ").appendDate(modifiedtimeBegin);
		}
		if (modifiedtimeEnd != null) {
			sb.append(" AND ").append(CPU_PRICE.CPU_PRICE_MODIFIEDTIME.getName()).append(" <= ").appendDate(modifiedtimeEnd);
		}
		if (modifiedtimeBegin != null || modifiedtimeEnd != null) {
			sb.append(" AND ").append(CPU_PRICE.CPU_PRICE_MODIFIEDTIME.getName()).append(" != ").appendString("0000-00-00");
		}
		sb.append(" ORDER BY ").append(CPU_PRICE.CPU_NAME);
		return doQuery(sb.toString());
	}
	
	public void createCPUPrice(String cpu_name, String cpu_price_desc, double cpu_price) throws Exception {
		DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("INSERT INTO ").append(CPU_PRICE).append(" (");
		sb.append(CPU_PRICE.CPU_NAME).append(", ");
		sb.append(CPU_PRICE.CPU_PRICE_DESC).append(", ");
		sb.append(CPU_PRICE.CPU_PRICE).append(", ");
		sb.append(CPU_PRICE.CPU_PRICE_CREATIONTIME);
		sb.append(") VALUES (");
		sb.appendString(cpu_name).append(", ");
		sb.appendString(cpu_price_desc).append(", ");
		sb.append(cpu_price).append(", ");
		sb.appendDate(new Date()).append(")");
		doUpdate(sb.toString());
	}
	
	public void deleteCPUPrice(int cpu_price_id) throws Exception {
		DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("DELETE FROM ").append(CPU_PRICE).append(" WHERE ");
		sb.append(CPU_PRICE.CPU_PRICE_ID).append(" = ").append(cpu_price_id);
		doUpdate(sb.toString());
	}
	
	public void modifyCPUPrice(int cpu_price_id, String cpu_price_desc, double cpu_price) throws Exception {
		DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("UPDATE ").append(CPU_PRICE).append(" SET ");
		sb.append(CPU_PRICE.CPU_PRICE_DESC).append(" = ").appendString(cpu_price_desc).append(", ");
		sb.append(CPU_PRICE.CPU_PRICE).append(" = ").append(cpu_price).append(", ");
		sb.append(CPU_PRICE.CPU_PRICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
		sb.append(" WHERE ");
		sb.append(CPU_PRICE.CPU_PRICE_ID).append(" = ").append(cpu_price_id);
		doUpdate(sb.toString());
	}
	
	public double lookupCPUPriceByName(String cpu_name) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
			DBStringBuilder sb = new DBStringBuilder();
			sb.append("SELECT ").append(CPU_PRICE.CPU_PRICE).append(" FROM ").append(CPU_PRICE);
			sb.append(" WHERE ").append(CPU_PRICE.CPU_NAME).append(" = ").appendString(cpu_name);
			rsw = doQuery(sb.toString());
			ResultSet rs = rsw.getResultSet();
			if (rs.next()) {
				return rs.getDouble(1);
			}
			return 0;
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
	
	public ResultSetWrapper lookupCPUNamesUnpriced() throws Exception {
	    DBTableCPU CPU = DBTable.CPU;
	    DBTableCPUPrice CPUPrice = DBTable.CPU_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT DISTINCT (").append(CPU.CPU_NAME).append(") ").append(" FROM ");
	    sb.append(CPU).append(" LEFT JOIN ").append(CPUPrice).append(" ON ");
	    sb.append(CPU.CPU_NAME).append(" = ").append(CPUPrice.CPU_NAME).append(" WHERE ");
	    sb.append(CPUPrice.CPU_PRICE_ID.getName()).append(" IS NULL");
	    return doQuery(sb.toString());
	}
	
}
