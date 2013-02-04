package com.eucalyptus.webui.server.device;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
import com.eucalyptus.webui.shared.resource.device.CPUPriceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns.CellTableColumnsRow;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceCPUPriceService {
    
    private static DeviceCPUPriceService instance = new DeviceCPUPriceService();
    
    public static DeviceCPUPriceService getInstance() {
        return instance;
    }
    
    private DeviceCPUPriceService() {
        /* do nothing */
    }
    
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private List<SearchResultFieldDesc> FIELDS_DESC = Arrays.asList(
			new SearchResultFieldDesc("0%", false, null),
			new SearchResultFieldDesc("2EM", false, new ClientMessage("", "")),
			new SearchResultFieldDesc(false, "3EM", new ClientMessage("Index", "序号"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("CPU", "CPU名称"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Desc", "描述"),
                    TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Price(Y/D)", "单价(元/天)"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Create", "创建时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(false, "8%", new ClientMessage("Modify", "修改时间"),
					TableDisplay.MANDATORY, Type.TEXT, false, false));
	
    private DBTableColumn getSortColumn(SearchRange range) {
        switch (range.getSortField()) {
        case CellTableColumns.CPU_PRICE.CPU_NAME: return DBTable.CPU_PRICE.CPU_NAME;
        case CellTableColumns.CPU_PRICE.CPU_PRICE_DESC: return DBTable.CPU_PRICE.CPU_PRICE_DESC;
        case CellTableColumns.CPU_PRICE.CPU_PRICE: return DBTable.CPU_PRICE.CPU_PRICE;
        case CellTableColumns.CPU_PRICE.CPU_PRICE_CREATIONTIME: return DBTable.CPU_PRICE.CPU_PRICE_CREATIONTIME;
        case CellTableColumns.CPU_PRICE.CPU_PRICE_MODIFIEDTIME: return DBTable.CPU_PRICE.CPU_PRICE_MODIFIEDTIME;
        }
        return null;
    }
	
	public SearchResult lookupCPUPriceByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
            ResultSet rs = DeviceCPUPriceDBProcWrapper.lookupCPUPriceByDate(conn, dateBegin, dateEnd, getSortColumn(range), range.isAscending());
            ArrayList<SearchResultRow> rows = new ArrayList<SearchResultRow>();
            int index, start = range.getStart(), end = start + range.getLength();
            for (index = 0; rs.next(); index ++) {
                if (start <= index && index < end) {
                    int cp_id = DBData.getInt(rs, CPU_PRICE.CPU_PRICE_ID);
                    String cpu_name = DBData.getString(rs, CPU_PRICE.CPU_NAME);
                    String cp_desc = DBData.getString(rs, CPU_PRICE.CPU_PRICE_DESC);
                    double cp_price = DBData.getDouble(rs, CPU_PRICE.CPU_PRICE);
                    Date cp_creationtime = DBData.getDate(rs, CPU_PRICE.CPU_PRICE_CREATIONTIME);
                    Date cp_modifiedtime = DBData.getDate(rs, CPU_PRICE.CPU_PRICE_MODIFIEDTIME);
                    CellTableColumnsRow row = new CellTableColumnsRow(CellTableColumns.CPU_PRICE.COLUMN_SIZE);
                    row.setColumn(CellTableColumns.CPU_PRICE.CPU_PRICE_ID, cp_id);
                    row.setColumn(CellTableColumns.CPU_PRICE.RESERVED_CHECKBOX, "");
                    row.setColumn(CellTableColumns.CPU_PRICE.RESERVED_INDEX, index + 1);
                    row.setColumn(CellTableColumns.CPU_PRICE.CPU_NAME, cpu_name);
                    row.setColumn(CellTableColumns.CPU_PRICE.CPU_PRICE_DESC, cp_desc);
                    row.setColumn(CellTableColumns.CPU_PRICE.CPU_PRICE, cp_price);
                    row.setColumn(CellTableColumns.CPU_PRICE.CPU_PRICE_CREATIONTIME, cp_creationtime);
                    row.setColumn(CellTableColumns.CPU_PRICE.CPU_PRICE_MODIFIEDTIME, cp_modifiedtime);
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
	
	public void createCPUPrice(Session session, String cpu_name, String cp_desc, double cp_price) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
	    if (cpu_name == null || cpu_name.isEmpty()) {
	        throw new EucalyptusServiceException(ClientMessage.invalidValue("CPU Name", "CPU名称"));
	    }
	    cp_price = Math.max(0, cp_price);
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DeviceCPUPriceDBProcWrapper.createCPUPrice(conn, cpu_name, cp_desc, cp_price);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public void deleteCPUPrice(Session session, List<Integer> cp_ids) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
	    if (cp_ids != null && !cp_ids.isEmpty()) {
	        Connection conn = null;
	        try {
	            conn = DBProcWrapper.getConnection();
                conn.setAutoCommit(false);
	            for (int cp_id : cp_ids) {
	                DeviceCPUPriceDBProcWrapper.lookupCPUPriceByID(conn, true, cp_id).deleteRow();
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
	
	public void modifyCPUPrice(Session session, int cp_id, String cp_desc, double cp_price) throws EucalyptusServiceException {
        if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
		if (cp_desc == null) {
		    cp_desc = "";
		}
		cp_price = Math.max(0, cp_price);
		Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
            ResultSet rs = DeviceCPUPriceDBProcWrapper.lookupCPUPriceByID(conn, true, cp_id);
            rs.updateString(CPU_PRICE.CPU_PRICE_DESC.toString(), cp_desc);
            rs.updateDouble(CPU_PRICE.CPU_PRICE.toString(), cp_price);
            rs.updateString(CPU_PRICE.CPU_PRICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
	
	public CPUPriceInfo lookupCPUPriceByID(int cp_id) throws EucalyptusServiceException {
	    Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
            ResultSet rs = DeviceCPUPriceDBProcWrapper.lookupCPUPriceByID(conn, false, cp_id);
            String cpu_name = DBData.getString(rs, CPU_PRICE.CPU_NAME);
            String cp_desc = DBData.getString(rs, CPU_PRICE.CPU_PRICE_DESC);
            double cp_price = DBData.getDouble(rs, CPU_PRICE.CPU_PRICE);
            Date cp_creationtime = DBData.getDate(rs, CPU_PRICE.CPU_PRICE_CREATIONTIME);
            Date cp_modifiedtime = DBData.getDate(rs, CPU_PRICE.CPU_PRICE_MODIFIEDTIME);
            return new CPUPriceInfo(cp_id, cpu_name, cp_desc, cp_price, cp_creationtime, cp_modifiedtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public List<String> lookupCPUsWithoutPrice() throws EucalyptusServiceException {
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            return DeviceCPUPriceDBProcWrapper.lookupCPUsWithoutPrice(conn);
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

class DeviceCPUPriceDBProcWrapper {
    
	private static final Logger log = Logger.getLogger(DeviceCPUPriceDBProcWrapper.class.getName());
	
	public static ResultSet lookupCPUPriceByID(Connection conn, boolean updatable, int cp_id) throws Exception {
	    DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(CPU_PRICE);
        sb.append(" WHERE ").append(CPU_PRICE.CPU_PRICE_ID).append(" = ").append(cp_id);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
	}
	
	public static List<String> lookupCPUsWithoutPrice(Connection conn) throws Exception {
        DBTableCPU CPU = DBTable.CPU;
        DBTableCPUPrice CPUPrice = DBTable.CPU_PRICE;
        DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT DISTINCT (").append(CPU.CPU_NAME).append(")");
        sb.append(" FROM ").append(CPU);
        sb.append(" WHERE ").append(CPU.CPU_NAME).append(" NOT IN ").append("("); {
            sb.append("SELECT DISTINCT(").append(CPUPrice.CPU_NAME).append(")");
            sb.append(" FROM ").append(CPUPrice);
            sb.append(" WHERE 1=1");
        }
        sb.append(")");
        ResultSet rs = DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
        List<String> result = new LinkedList<String>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
	}
	
	public static ResultSet lookupCPUPriceByDate(Connection conn, Date beg, Date end, DBTableColumn sorted, boolean isAscending) throws Exception {
		DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
		DBStringBuilder sb = new DBStringBuilder();
		sb.append("SELECT * FROM ").append(CPU_PRICE).append(" WHERE 1=1");
		if (beg != null || end != null) {
            sb.append(" AND ("); {
                sb.appendDateBound(CPU_PRICE.CPU_PRICE_CREATIONTIME, beg, end);
                sb.append(" OR ");
                sb.appendDateBound(CPU_PRICE.CPU_PRICE_MODIFIEDTIME, beg, end);
            }
            sb.append(")");
        }
        if (sorted != null) {
            sb.append(" ORDER BY ").append(sorted).append(isAscending ? " ASC" : " DESC");
        }
        return DBProcWrapper.queryResultSet(conn, false, sb.toSql(log));
	}
	
	public static int createCPUPrice(Connection conn, String cpu_name, String cp_desc, double price) throws Exception {
	    DBTableCPUPrice CPU_PRICE = DBTable.CPU_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("INSERT INTO ").append(CPU_PRICE).append(" ("); {
            sb.append(CPU_PRICE.CPU_NAME).append(", ");
            sb.append(CPU_PRICE.CPU_PRICE_DESC).append(", ");
            sb.append(CPU_PRICE.CPU_PRICE).append(", ");
            sb.append(CPU_PRICE.CPU_PRICE_CREATIONTIME).append(", ");
            sb.append(CPU_PRICE.CPU_PRICE_MODIFIEDTIME);
        }
        sb.append(") VALUES ("); {
            sb.appendString(cpu_name).append(", ");
            sb.appendString(cp_desc).append(", ");
            sb.append(price).append(", ");
            sb.appendDate().append(", ");
            sb.appendNull();
        }
        sb.append(")");
        
        Statement stat = conn.createStatement();
        stat.executeUpdate(sb.toSql(log), new String[]{CPU_PRICE.CPU_PRICE_ID.toString()});
        ResultSet rs = stat.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
	}
	
}
