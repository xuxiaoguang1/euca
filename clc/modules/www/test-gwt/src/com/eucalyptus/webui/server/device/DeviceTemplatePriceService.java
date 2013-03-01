package com.eucalyptus.webui.server.device;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.resource.device.TemplatePriceInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceTemplatePriceService {

	private static DeviceTemplatePriceService instance = new DeviceTemplatePriceService();
	
	public static DeviceTemplatePriceService getInstance() {
		return instance;
	}
	
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
			new SearchResultFieldDesc(false, "3EM", new ClientMessage("Index", "序号"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Name", "名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Desc", "描述"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Total(Y/D)", "单价"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
    		new SearchResultFieldDesc(false, "8%", new ClientMessage("CPU Total", "CPU数量"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("CPU Price(Y/D)", "CPU单价(元/天)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("Memory Total(MB)", "内存数量(MB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(false, "8%", new ClientMessage("Memory(Y/D/MB)", "内存单价(元/天/MB)"),
	        		TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("Disk Total(MB)", "硬盘数量(MB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("Disk(Y/D/MB)", "硬盘单价(元/天/MB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(false, "8%", new ClientMessage("BW Total(KB)", "带宽数量(KB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
    		new SearchResultFieldDesc(false, "8%", new ClientMessage("BW(Y/D/KB)", "带宽单价(元/天/KB)"),
            		TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Create", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Modify", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
	private DBTableColumn getSortColumn(SearchRange range) {
		switch (range.getSortField()) {
		case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_NAME: return DBTable.TEMPLATE.TEMPLATE_NAME;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_DESC: return DBTable.TEMPLATE_PRICE.TEMPLATE_PRICE_DESC;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_CPU_NCPUS: return DBTable.TEMPLATE.TEMPLATE_NCPUS;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_CPU_PRICE: return DBTable.TEMPLATE_PRICE.TEMPLATE_PRICE_CPU;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_MEM_TOTAL: return DBTable.TEMPLATE.TEMPLATE_MEM;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_MEM_PRICE: return DBTable.TEMPLATE_PRICE.TEMPLATE_PRICE_MEM;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_DISK_TOTAL: return DBTable.TEMPLATE.TEMPLATE_DISK;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_DISK_PRICE: return DBTable.TEMPLATE_PRICE.TEMPLATE_PRICE_DISK;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_BW_TOTAL: return DBTable.TEMPLATE.TEMPLATE_BW;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_BW_PRICE: return DBTable.TEMPLATE_PRICE.TEMPLATE_PRICE_BW;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME: return DBTable.TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME;
        case CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME: return DBTable.TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME;
		}
		return null;
		
	}
	
	public SearchResult lookupTemplatePriceByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
            DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
            ResultSet rs = DeviceTemplatePriceDBProcWrapper.lookupTemplatePriceByDate(conn, dateBegin, dateEnd, getSortColumn(range), range.isAscending());
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                	int tp_id = DBData.getInt(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_ID);
    				String template_name = DBData.getString(rs, TEMPLATE.TEMPLATE_NAME);
    				String tp_desc = DBData.getString(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_DESC);
    				int template_ncpus = DBData.getInt(rs, TEMPLATE.TEMPLATE_NCPUS);
    				double tp_cpu = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_CPU);
    				long template_mem = DBData.getLong(rs, TEMPLATE.TEMPLATE_MEM);
    				double tp_mem = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_MEM);
    				long template_disk = DBData.getLong(rs, TEMPLATE.TEMPLATE_DISK);
    				double tp_disk = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_DISK);
    				int template_bw = DBData.getInt(rs, TEMPLATE.TEMPLATE_BW);
    				double tp_bw = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_BW);
    				double tp_price = template_ncpus * tp_cpu + template_mem * tp_mem + template_disk * tp_disk + template_bw * tp_bw;
    				tp_price = (double)(int)(tp_price * 100) / 100;
    				Date tp_creationtime = DBData.getDate(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME);
    				Date tp_modifiedtime = DBData.getDate(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.TEMPLATE_PRICE.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_ID, tp_id);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_NAME, template_name);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_DESC, tp_desc);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_CPU_NCPUS, template_ncpus);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_CPU_PRICE, tp_cpu);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_MEM_TOTAL, template_mem);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_MEM_PRICE, tp_mem);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_DISK_TOTAL, template_disk);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_DISK_PRICE, tp_disk);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_BW_TOTAL, template_bw);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_BW_PRICE, tp_bw);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_TOTAL, tp_price);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME, tp_creationtime);
                    row.setColumn(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME, tp_modifiedtime);
                    rows.add(new SearchResultRow(row.toList()));
                }
            }
            for (SearchResultRow row : rows) {
                System.out.println(row);
            }
            range.setLength(rows.size());
            return new SearchResult(index, range, FIELDS_DESC, rows);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
    }
	
	public void createTemplatePriceByID(Session session, int template_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (tp_desc == null) {
			tp_desc = "";
		}
		tp_cpu = Math.max(0, tp_cpu);
		tp_mem = Math.max(0, tp_mem);
		tp_disk = Math.max(0, tp_disk);
		tp_bw = Math.max(0, tp_bw);
		Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceTemplatePriceDBProcWrapper.createTemplatePrice(conn, tp_desc, tp_cpu, tp_mem, tp_disk, tp_bw, template_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public void deleteTemplatePrice(Session session, List<Integer> tp_ids) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
			throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
		}
		if (tp_ids != null && !tp_ids.isEmpty()) {
	        Connection conn = null;
	        try {
	            conn = DBProcWrapper.getConnection();
	            conn.setAutoCommit(false);
	            for (int tp_id : tp_ids) {
	            	DeviceTemplatePriceDBProcWrapper.lookupTemplatePriceByID(conn, true, tp_id).deleteRow();
	            }
	            conn.commit();
	        }
	        catch (Exception e) {
                e.printStackTrace();
                DBProcWrapper.rollback(conn);
                throw new EucalyptusServiceException(e);
            }
            finally {
                DBProcWrapper.close(conn);
            }
		}
	}
	
	public void modifyTemplatePrice(Session session, int tp_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) throws EucalyptusServiceException {
		if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
		if (tp_desc == null) {
			tp_desc = "";
		}
		tp_cpu = Math.max(0, tp_cpu);
		tp_mem = Math.max(0, tp_mem);
		tp_disk = Math.max(0, tp_disk);
		tp_bw = Math.max(0, tp_bw);
		Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
            ResultSet rs = DeviceTemplatePriceDBProcWrapper.lookupTemplatePriceByID(conn, true, tp_id);
            rs.updateString(TEMPLATE_PRICE.TEMPLATE_PRICE_DESC.toString(), tp_desc);
            rs.updateDouble(TEMPLATE_PRICE.TEMPLATE_PRICE_CPU.toString(), tp_cpu);
            rs.updateDouble(TEMPLATE_PRICE.TEMPLATE_PRICE_MEM.toString(), tp_mem);
            rs.updateDouble(TEMPLATE_PRICE.TEMPLATE_PRICE_DISK.toString(), tp_disk);
            rs.updateDouble(TEMPLATE_PRICE.TEMPLATE_PRICE_BW.toString(), tp_bw);
            rs.updateString(TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            conn.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            DBProcWrapper.rollback(conn);
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public TemplatePriceInfo lookupTemplatePriceByID(int tp_id) throws EucalyptusServiceException {
		Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
            ResultSet rs = DeviceTemplatePriceDBProcWrapper.lookupTemplatePriceByID(conn, false, tp_id);
            int template_id = DBData.getInt(rs, TEMPLATE_PRICE.TEMPLATE_ID);
			String tp_desc = DBData.getString(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_DESC);
			double tp_cpu = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_CPU);
			double tp_mem = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_MEM);
			double tp_disk = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_DISK);
			double tp_bw = DBData.getDouble(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_BW);
			Date tp_creationtime = DBData.getDate(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME);
			Date tp_modifiedtime = DBData.getDate(rs, TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME);
			return new TemplatePriceInfo(tp_id, template_id, tp_desc, tp_cpu, tp_mem, tp_disk, tp_bw, tp_creationtime, tp_modifiedtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public Map<String, Integer> lookupTemplatesWithoutPrice(Session session) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceTemplatePriceDBProcWrapper.lookupTemplatesWithoutPrice(conn);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
}

class DeviceTemplatePriceDBProcWrapper {
    
	private static final Logger log = Logger.getLogger(DeviceTemplatePriceDBProcWrapper.class.getName());
	
	public static ResultSet lookupTemplatePriceByID(Connection conn, boolean updatable, int tp_id) throws Exception {
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(TEMPLATE_PRICE);
        sb.append(" WHERE ").append(TEMPLATE_PRICE.TEMPLATE_PRICE_ID).append(" = ").append(tp_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
	}
	
	public static Map<String, Integer> lookupTemplatesWithoutPrice(Connection conn) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT ").append(TEMPLATE.TEMPLATE_NAME).append(", ").append(TEMPLATE.TEMPLATE_ID);
        sb.append(" FROM ").append(TEMPLATE);
        sb.append(" WHERE ").append(TEMPLATE.TEMPLATE_ID).append(" NOT IN ").append("("); {
            sb.append("SELECT DISTINCT(").append(TEMPLATE_PRICE.TEMPLATE_ID).append(")");
            sb.append(" FROM ").append(TEMPLATE_PRICE);
            sb.append(" WHERE 1=1");
        }
        sb.append(")");
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        Map<String, Integer> result = new HashMap<String, Integer>();
        while (rs.next()) {
        	result.put(rs.getString(1), rs.getInt(2));
        }
        return result;
	}
	
	public static ResultSet lookupTemplatePriceByDate(Connection conn, Date beg, Date end, DBTableColumn sorted, boolean isAscending) throws Exception {
		DBTableTemplate TEMPLATE = DBTable.TEMPLATE;
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT ").append(TEMPLATE_PRICE.ANY).append(", ").append(TEMPLATE.ANY).append(" FROM "); {
			sb.append(TEMPLATE_PRICE).append(" LEFT JOIN ").append(TEMPLATE);
			sb.append(" ON ").append(TEMPLATE_PRICE.TEMPLATE_ID).append(" = ").append(TEMPLATE.TEMPLATE_ID);
		}
		sb.append(" WHERE 1=1");
		if (beg != null || end != null) {
            sb.append(" AND ("); {
                sb.appendDateBound(TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME, beg, end);
                sb.append(" OR ");
                sb.appendDateBound(TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME, beg, end);
            }
            sb.append(")");
        }
        if (sorted != null) {
            sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
        }
        return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
	}
	
	public static int createTemplatePrice(Connection conn, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw, int template_id) throws Exception {
		DBTableTemplatePrice TEMPLATE_PRICE = DBTable.TEMPLATE_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
	    sb.append("INSERT INTO ").append(TEMPLATE_PRICE).append(" ("); {
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_DESC).append(", ");
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_CPU).append(", ");
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_MEM).append(", ");
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_DISK).append(", ");
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_BW).append(", ");
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_CREATIONTIME).append(", ");
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_PRICE_MODIFIEDTIME).append(", ");
	    	sb.append(TEMPLATE_PRICE.TEMPLATE_ID);
        }
        sb.append(") VALUES ("); {
        	sb.appendString(tp_desc).append(", ");
        	sb.append(tp_cpu).append(", ");
        	sb.append(tp_mem).append(", ");
        	sb.append(tp_disk).append(", ");
        	sb.append(tp_bw).append(", ");
        	sb.appendDate().append(", ");
        	sb.appendNull().append(", ");
        	sb.append(template_id);
        }
        sb.append(")");
        
        Statement stat = conn.createStatement();
        stat.executeUpdate(sb.toSql(log), new String[]{TEMPLATE_PRICE.TEMPLATE_PRICE_ID.toString()});
        ResultSet rs = stat.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
	}
	
}
