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
			rs = historyDBProc.queryTotalHistory();
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
					boolean userKeyActive = Integer.parseInt(rs.getString(DBTableColName.USER_CERT.ACTIVE)) == 1;
					boolean userKeyRevoked = Integer.parseInt(rs.getString(DBTableColName.USER_CERT.REVOKED)) == 1;

					if (isRootView) {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.USER_CERT.ID),
										Integer.toString(index++),
										Enum2String.getInstance().getActiveState(userKeyActive),
										Enum2String.getInstance().getRevokedState(userKeyRevoked),
										rs.getString(DBTableColName.USER_CERT.CREATED_DATE),
										rs.getString(DBTableColName.ACCOUNT.NAME),
										rs.getString(DBTableColName.GROUP.NAME),
										rs.getString(DBTableColName.USER.NAME))));
					} else {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.USER_CERT.ID),
										Integer.toString(index++),
										Enum2String.getInstance().getActiveState(userKeyActive),
										Enum2String.getInstance().getRevokedState(userKeyRevoked),
										rs.getString(DBTableColName.USER_CERT.CREATED_DATE),
										rs.getString(DBTableColName.GROUP.NAME),
										rs.getString(DBTableColName.USER.NAME))));
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
	private static final String[] TABLE_COL_TITLE_CERT_ACTIVE = { "Active", "密钥状态" };
	private static final String[] TABLE_COL_TITLE_CERT_REVOKED = { "Revoked", "是否撤销"};
	private static final String[] TABLE_COL_TITLE_CERT_CREATED_DATE = {"Created Date", "创建时间" };
	private static final String[] TABLE_COL_TITLE_ACCOUNT_NAME = {"Account", "账户" };
	private static final String[] TABLE_COL_TITLE_GROUP_NAME = { "Group", "组" };
	private static final String[] TABLE_COL_TITLE_NAME = {"ID", "用户"};

	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%",TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_ACTIVE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_REVOKED[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_CREATED_DATE[1], true, "25%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_GROUP_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false));

	private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_ACTIVE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_REVOKED[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_CREATED_DATE[1], true, "30%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_GROUP_NAME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NAME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false));

}
