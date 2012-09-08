package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.stat.HistoryDBProcWrapper;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class HistoryServiceProcImpl {

	private HistoryDBProcWrapper historyDBProc;
	
	public HistoryServiceProcImpl() {
		historyDBProc = new HistoryDBProcWrapper();
	}

	public SearchResult lookupHistory(LoginUserProfile curUser, String search, SearchRange range) throws EucalyptusServiceException {
		boolean isRootAdmin = curUser.isSystemAdmin();

		ResultSetWrapper rs;
		try {
			if (isRootAdmin) {
				rs = historyDBProc.queryTotalHistory();
			} else {
				rs = historyDBProc.queryHistoryBy(curUser.getAccountId(), curUser.getUserId(), curUser.getUserType());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Fail to query certificates");
		}
		if (rs == null)
			return null;

		return getSearchResult(isRootAdmin, rs, range);
	}

	private SearchResult getSearchResult(boolean isRootAdmin, ResultSetWrapper rs, SearchRange range) {
		assert (range != null);

		List<SearchResultFieldDesc> FIELDS;

		if (isRootAdmin) {
			FIELDS = FIELDS_ROOT;
		} else {
			FIELDS = FIELDS_NONROOT;
		}

		final int sortField = range.getSortField();

		DATA = resultSet2List(isRootAdmin, rs);

		int resultLength = Math.min(range.getLength(),
				DATA.size() - range.getStart());
		SearchResult result = new SearchResult(DATA.size(), range);
		result.setDescs(FIELDS);
		result.setRows(DATA.subList(range.getStart(), range.getStart()
				+ resultLength));

		for (SearchResultRow row : result.getRows()) {
			System.out.println("Row: " + row);
		}

		return result;
	}

	private List<SearchResultRow> resultSet2List(boolean isRootView,
			ResultSetWrapper rsWrapper) {
		ResultSet rs = rsWrapper.getResultSet();
		int index = 1;
		List<SearchResultRow> result = null;
		try {
			if (rs != null) {
				result = new ArrayList<SearchResultRow>();

				while (rs.next()) {
					boolean start = Integer.parseInt(rs.getString(DBTableColName.HISTORY.ACTION)) == 1;
					if (isRootView) {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.HISTORY.ID),
										Integer.toString(index++),
										Enum2String.getInstance().getVMAction(start),
										rs.getString(DBTableColName.HISTORY.REASON),
										rs.getString(DBTableColName.HISTORY.DATE),
										rs.getString(DBTableColName.HISTORY.USER_ID),
										rs.getString(DBTableColName.HISTORY.VM_ID))));
					} else {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.HISTORY.ID),
										Integer.toString(index++),
										Enum2String.getInstance().getVMAction(start),
										rs.getString(DBTableColName.HISTORY.REASON),
										rs.getString(DBTableColName.HISTORY.DATE),
										rs.getString(DBTableColName.HISTORY.USER_ID),
										rs.getString(DBTableColName.HISTORY.VM_ID))));
					}
				}
			}
			rsWrapper.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}


	private static List<SearchResultRow> DATA = null;

	private static final String[] TABLE_COL_TITLE_CHECKALL = { "Check All", "全选" };
	private static final String[] TABLE_COL_TITLE_NO = { "No.", "序号" };
	private static final String[] TABLE_COL_TITLE_HISTORY_ACTION = { "Action", "操作" };
	private static final String[] TABLE_COL_TITLE_HISTORY_REASON = { "Reason", "原因"};
	private static final String[] TABLE_COL_TITLE_HISTORY_DATE = {"Date", "时间" };
	private static final String[] TABLE_COL_TITLE_USER_ID = {"User ID", "用户ID" };
	private static final String[] TABLE_COL_TITLE_VM_ID = { "VM ID", "虚拟机ID" };

	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%",TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_HISTORY_ACTION[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_HISTORY_REASON[1], true, "25%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_HISTORY_DATE[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_USER_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_VM_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false));

	private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%",TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_HISTORY_ACTION[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_HISTORY_REASON[1], true, "25%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_HISTORY_DATE[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_USER_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_VM_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false));

}
