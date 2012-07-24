package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.eucalyptus.webui.client.activity.DeviceServerActivity.ServerState;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.device.ServerDBProcWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceServerServiceProcImpl {
	
	private ServerDBProcWrapper dbproc = new ServerDBProcWrapper();
	
	public SearchResult lookupServer(Session session, String search, SearchRange range, int queryState) throws EucalyptusServiceException {
		LoginUserProfile user = LoginUserProfileStorer.instance().get(session.getId());
		if (user.isSystemAdmin()) {
			List<SearchResultRow> rows = convertResults(true, dbproc.queryAllServers(queryState));
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
		}
		return null;
	}
	
	private List<SearchResultRow> convertResults(boolean isRootAdmin, ResultSetWrapper rsw) {
		if (rsw != null) {
			ResultSet rs = rsw.getResultSet();
			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
			try {
				int index = 1;
				while (rs.next()) {
					if (isRootAdmin) {
						rows.add(convertRootAdminResultRow(rs, index));
					}
					index ++;
				}
				rsw.close();
				return rows;
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private String getServerState(String state) {
		if (state != null) {
			try {
				return ServerState.getServerState(Integer.parseInt(state)).toString();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private SearchResultRow convertRootAdminResultRow(ResultSet rs, int index) throws SQLException {
		return new SearchResultRow(Arrays.asList(
				rs.getString(DBTableColName.SERVER.ID),
				"", Integer.toString(index),
				rs.getString(DBTableColName.SERVER.NAME),
				rs.getString(DBTableColName.SERVER.MARK),
				rs.getString(DBTableColName.SERVER.IMAGE),
				rs.getString(DBTableColName.SERVER.CONF),
				rs.getString(DBTableColName.SERVER.IP),
				rs.getString(DBTableColName.SERVER.BW),
				getServerState(rs.getString(DBTableColName.SERVER.STATE))));
	}
	
	final private static int LAN_SELECT = 1;
	
	final private static String[] TABLE_COL_TITLE_CHECKALL = {"", "全选"};
	final private static String[] TABLE_COL_TITLE_NO = {"", "序号"};
	final private static String[] TABLE_COL_TITLE_NAME = {"", "名称"};
	final private static String[] TABLE_COL_TITLE_MARK = {"", "标识"};
	final private static String[] TABLE_COL_TITLE_IMAGE = {"", "镜像"};
	final private static String[] TABLE_COL_TITLE_CONF = {"", "配置"};
	final private static String[] TABLE_COL_TITLE_IP = {"", "IP地址"};
	final private static String[] TABLE_COL_TITLE_BW = {"", "带宽"};
	final private static String[] TABLE_COL_TITLE_STATE = {"", "状态"};
	
	final private static List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
			new SearchResultFieldDesc(null, "0%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[LAN_SELECT], "8%", false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
			new SearchResultFieldDesc(TABLE_COL_TITLE_NAME[LAN_SELECT], true, "13%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_MARK[LAN_SELECT], true, "13%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_IMAGE[LAN_SELECT], true, "12%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_CONF[LAN_SELECT], true, "11%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_IP[LAN_SELECT], true, "17%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_BW[LAN_SELECT], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
			new SearchResultFieldDesc(TABLE_COL_TITLE_STATE[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false));
	
}
