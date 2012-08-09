package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

class DeviceTemplateDBProcWrapper {

	private static final Logger LOG = Logger.getLogger(DeviceTemplateDBProcWrapper.class.getName());

	DBProcWrapper wrapper = DBProcWrapper.Instance();

	ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}

	void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}

	ResultSetWrapper queryAllTemplates(String starttime, String endtime) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.TEMPLATE);
		sb.append(" WHERE 1=1 ");
		
		if (starttime != null) {
			sb.append(" AND ");
			sb.append(DBTableColName.TEMPLATE.STARTTIME).append(" >= ").append("\"").append(starttime).append("\"");
		}
		if (endtime != null) {
			sb.append(" AND ");
			sb.append(DBTableColName.TEMPLATE.STARTTIME).append(" <= ").append("\"").append(endtime).append("\"");
		}
		return doQuery(sb.toString());
	}

	ResultSetWrapper queryTemplate(int template_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBTableName.TEMPLATE);
		sb.append(" WHERE ");
		sb.append(DBTableColName.TEMPLATE.ID);
		sb.append(" = ").append(template_id);
		return doQuery(sb.toString());
	}

	void modifyTemplate(int template_id, String cpu, String mem, String disk, String bw, String image) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(DBTableName.TEMPLATE);
		sb.append(" SET ");

		sb.append(DBTableColName.TEMPLATE.CPU).append(" = ").append("\"").append(cpu).append("\"").append(", ");
		sb.append(DBTableColName.TEMPLATE.MEM).append(" = ").append("\"").append(mem).append("\"").append(", ");
		sb.append(DBTableColName.TEMPLATE.DISK).append(" = ").append("\"").append(disk).append("\"").append(", ");
		sb.append(DBTableColName.TEMPLATE.BW).append(" = ").append("\"").append(bw).append("\"").append(", ");
		sb.append(DBTableColName.TEMPLATE.IMAGE).append(" = ").append("\"").append(image).append("\"");
		
		sb.append(" WHERE ");
		sb.append(DBTableColName.TEMPLATE.ID).append(" = ").append(template_id);
		doUpdate(sb.toString());
	}

	void addTemplate(String mark, String cpu, String mem, String disk, String bw, String image, String starttime)
	        throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(DBTableName.TEMPLATE).append(" (");
		sb.append(DBTableColName.TEMPLATE.MARK).append(", ");
		sb.append(DBTableColName.TEMPLATE.CPU).append(", ");
		sb.append(DBTableColName.TEMPLATE.MEM).append(", ");
		sb.append(DBTableColName.TEMPLATE.DISK).append(", ");
		sb.append(DBTableColName.TEMPLATE.BW).append(", ");
		sb.append(DBTableColName.TEMPLATE.IMAGE).append(", ");
		sb.append(DBTableColName.TEMPLATE.STARTTIME).append(")");
		sb.append(" VALUES ").append("(");
		sb.append("\"").append(mark).append("\", ");
		sb.append("\"").append(cpu).append("\", ");
		sb.append("\"").append(mem).append("\", ");
		sb.append("\"").append(disk).append("\", ");
		sb.append("\"").append(bw).append("\", ");
		sb.append("\"").append(image).append("\", ");
		sb.append("\"").append(starttime).append("\")");
		doUpdate(sb.toString());
	}

	void deleteTemplate(int template_id) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(DBTableName.TEMPLATE);
		sb.append(" WHERE ").append(DBTableColName.TEMPLATE.ID).append(" = ").append(template_id);
		doUpdate(sb.toString());
	}

}

public class DeviceTemplateServiceProcImpl {

	private DeviceTemplateDBProcWrapper dbproc = new DeviceTemplateDBProcWrapper();

	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}

	public SearchResult lookupTemplate(Session session, String search, SearchRange range, Date starttime, Date endtime) {
		ResultSetWrapper rsw = null;
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			
			String sstarttime = null;
			if (starttime != null) {
				sstarttime = formatter.format(starttime);
			}
			String sendtime = null;
			if (endtime != null) {
				sendtime = formatter.format(endtime);
			}
			rsw = dbproc.queryAllTemplates(sstarttime, sendtime);
			List<SearchResultRow> rows = convertResults(rsw);
			if (rows != null) {
				int length = Math.min(range.getLength(), rows.size() - range.getStart());
				SearchResult result = new SearchResult(rows.size(), range);
				result.setDescs(FIELDS_ROOT);
				result.setRows(rows.subList(range.getStart(), range.getStart() + length));
				for (SearchResultRow row : result.getRows()) {
					System.out.println(row);
				}
				return result;
			}
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public SearchResultRow modifyTemplate(Session session, SearchResultRow row, String cpu, String mem, String disk,
			String bw, String image) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			int template_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_TEMPLATE_ID));
			modifyTemplate(template_id, cpu, mem, disk, bw, image);
			return lookupService(row, template_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private SearchResultRow lookupService(SearchResultRow row, int template_id) throws Exception {
		ResultSetWrapper rsw = null;
		try {
			rsw = dbproc.queryTemplate(template_id);
			ResultSet rs = rsw.getResultSet();
			if (!rs.next()) {
				return null;
			}
			row.setField(TABLE_COL_INDEX_CPU, rs.getString(DBTableColName.TEMPLATE.CPU));
			row.setField(TABLE_COL_INDEX_MEM, rs.getString(DBTableColName.TEMPLATE.MEM));
			row.setField(TABLE_COL_INDEX_DISK, rs.getString(DBTableColName.TEMPLATE.DISK));
			row.setField(TABLE_COL_INDEX_BW, rs.getString(DBTableColName.TEMPLATE.BW));
			row.setField(TABLE_COL_INDEX_IMAGE, rs.getString(DBTableColName.TEMPLATE.IMAGE));
			return row;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (rsw != null) {
					rsw.close();
				}
			}
			catch (Exception e) {
				throw e;
			}
		}
	}

	public List<SearchResultRow> deleteTemplate(Session session, List<SearchResultRow> list) {
		try {
			if (list == null) {
				return null;
			}
			if (!getUser(session).isSystemAdmin()) {
				return null;
			}
			List<SearchResultRow> result = new ArrayList<SearchResultRow>();
			for (SearchResultRow row : list) {
				int template_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_TEMPLATE_ID));
				if (!deleteTemplate(template_id)) {
					break;
				}
				result.add(row);
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean addTemplate(Session session, String mark, String cpu, String mem, String disk,
			String bw, String image) {
		try {
			if (!getUser(session).isSystemAdmin()) {
				return false;
			}
			if (mark == null || mark.length() == 0) {
				return false;
			}
			addTemplate(mark, cpu, mem, disk, bw, image);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean deleteTemplate(int template_id) {
		try {
			dbproc.deleteTemplate(template_id);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void modifyTemplate(int template_id, String cpu, String mem, String disk, String bw, String image) throws Exception {
		dbproc.modifyTemplate(template_id, cpu, mem, disk, bw, image);
	}

	private void addTemplate(String mark, String cpu, String mem, String disk, String bw, String image)
	        throws Exception {
		dbproc.addTemplate(mark, cpu, mem, disk, bw, image, formatter.format(new Date()));
	}

	private List<SearchResultRow> convertResults(ResultSetWrapper rsw) {
		if (rsw != null) {
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			try {
				int index = 1;
				while (rs.next()) {
					rows.add(convertRootResultRow(rs, index ++));
				}
				return rows;
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				try {
					rsw.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	private SearchResultRow convertRootResultRow(ResultSet rs, int index) throws SQLException {
		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.TEMPLATE.ID), "", Integer.toString(index),
				rs.getString(DBTableColName.TEMPLATE.MARK),
				rs.getString(DBTableColName.TEMPLATE.CPU),
				rs.getString(DBTableColName.TEMPLATE.MEM),
				rs.getString(DBTableColName.TEMPLATE.DISK),
				rs.getString(DBTableColName.TEMPLATE.BW),
				rs.getString(DBTableColName.TEMPLATE.IMAGE),
				rs.getString(DBTableColName.TEMPLATE.STARTTIME)));
	}

	private static final int LAN_SELECT = 1;
	
	public static final int TABLE_COL_INDEX_TEMPLATE_ID = 0;
	
	public static final int TABLE_COL_INDEX_CHECKBOX = 1;
	public static final int TABLE_COL_INDEX_NO = 2;
	public static final int TABLE_COL_INDEX_MARK = 3;
	public static final int TABLE_COL_INDEX_CPU = 4;
	public static final int TABLE_COL_INDEX_MEM = 5;
	public static final int TABLE_COL_INDEX_DISK = 6;
	public static final int TABLE_COL_INDEX_BW = 7;
	public static final int TABLE_COL_INDEX_IMAGE = 8;

	private static final String[] TABLE_COL_TITLE_CHECKBOX = {"", ""};
	private static final String[] TABLE_COL_TITLE_NO = {"", "序号"};
	private static final String[] TABLE_COL_TITLE_MARK = {"", "标识"};
	private static final String[] TABLE_COL_TITLE_CPU = {"", "CPU"};
	private static final String[] TABLE_COL_TITLE_MEM = {"", "内存"};
	private static final String[] TABLE_COL_TITLE_DISK = {"", "硬盘"};
	private static final String[] TABLE_COL_TITLE_BW = {"", "带宽"};
	private static final String[] TABLE_COL_TITLE_IMAGE = {"", "镜像"};
	private static final String[] TABLE_COL_TITLE_STARTTIME = {"", "开始时间"};

	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKBOX[LAN_SELECT], "4%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_MARK[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_CPU[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_MEM[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_DISK[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_BW[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
	        new SearchResultFieldDesc(TABLE_COL_TITLE_IMAGE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
            new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));
}
