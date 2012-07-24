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
import com.eucalyptus.webui.server.dictionary.Enum2String;
import com.eucalyptus.webui.server.user.AccountDBProcWrapper;
import com.eucalyptus.webui.server.user.AccountSyncException;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserType;
import com.eucalyptus.webui.shared.user.UserInfo;

public class AccountServiceProcImpl {
	
	public void createAccount(String name, String email, String description)
	  		throws EucalyptusServiceException {
		
		if ( name == null || email == null) {
		      throw new EucalyptusServiceException( "Empty account name or email" );
		  }
		  
		try {
			int newAccountId = accountDBProc.addAccount(name, email, description, EnumState.NORMAL);
			
			// if creating account is successful, add corresponding admin user in user table
			if (newAccountId > 0) {
				UserServiceProcImpl userServiceProc = new UserServiceProcImpl();
				
				UserInfo user = new UserInfo();
				user.setAccountId(newAccountId);
				user.setName("admin");
				user.setPwd("123456");
				user.setEmail(email);
				user.setState(EnumState.NORMAL);
				user.setType(EnumUserType.ADMIN);
				
				userServiceProc.createUser(newAccountId, user);
			}
		} catch (AccountSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Create user fails");
		}
	}

	/**
	   * Search total accounts.
	   * 
	   * @param session
	   * @param search
	   * @param range
	   * @return
	   * @throws EucalyptusServiceException
	 * @throws AccountSyncException 
	   */
	  public SearchResult lookupAccount(String search, SearchRange range ) throws EucalyptusServiceException {
		  ResultSetWrapper rs;
		  
		  try {
			  rs = accountDBProc.queryTotalAccounts();
		  } catch (AccountSyncException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  throw new EucalyptusServiceException("query accounts fails");
		  }
		  
		  if (rs == null)
			  return null;
		  
		  return getSearchResult(rs, range);
	  }
	  
	  public void deleteAccounts( ArrayList<String> ids ) throws EucalyptusServiceException {
		  try {
			accountDBProc.delAccounts(ids);
		} catch (AccountSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to delete accounts");
		}
	  }
	  
	  public void modifyAccount( int accountId, String name, String email ) throws EucalyptusServiceException {
		  try {
			accountDBProc.modifyAccount(accountId, name, email);
			
		} catch (AccountSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to modify account");
		}
	  }

	  public void updateAccountState( ArrayList<String> ids, EnumState state ) throws EucalyptusServiceException {
		  try {
			accountDBProc.updateAccountState(ids, state);
			
		} catch (AccountSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to update account state");
		}
	  }
	  
	  public ArrayList<AccountInfo> listAccounts() throws EucalyptusServiceException {
		  ResultSetWrapper rsw;
		  
		  try {
			  rsw = accountDBProc.queryTotalAccounts();
		  } catch (AccountSyncException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  throw new EucalyptusServiceException("query accounts fails");
		  }
		  
		  if (rsw == null)
			  return null;
		  
		  ResultSet rs = rsw.getResultSet();
		  
		  ArrayList<AccountInfo> result = null;
		  try {
			  if (rs != null) {
				  result = new ArrayList<AccountInfo>();
				  
				  while (rs.next()) {
					  	AccountInfo accountInfo = new AccountInfo(Integer.valueOf(rs.getString(DBTableColName.ACCOUNT.ID)),
							  							rs.getString(DBTableColName.ACCOUNT.NAME),
														rs.getString(DBTableColName.ACCOUNT.EMAIL),
														rs.getString(DBTableColName.ACCOUNT.DES),
														EnumState.values()[Integer.valueOf(rs.getString(DBTableColName.ACCOUNT.STATE))]);
					  	result.add(accountInfo);
				  }
			  }
			  
			  rsw.close();
			  
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }
	  
	  private SearchResult getSearchResult(ResultSetWrapper rs, SearchRange range) {
		  
		  List<SearchResultFieldDesc> FIELDS = this.FIELDS;
		  
		  final int sortField = range.getSortField( );
		  
		  DATA = resultSet2List(rs);
		  
		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
		  SearchResult result = new SearchResult( DATA.size( ), range );
		  result.setDescs( FIELDS );
		  result.setRows( DATA.subList( range.getStart( ), range.getStart( ) + resultLength ) );
		
		  for ( SearchResultRow row : result.getRows( ) ) {
			  System.out.println( "Row: " + row );
		  }
			
		  return result;
	  }
	  
	  private List<SearchResultRow> resultSet2List(ResultSetWrapper rsWrapper) {
		  ResultSet rs = rsWrapper.getResultSet();
		  int index = 1;
		  List<SearchResultRow> result = null;
		  try {
			  if (rs != null) {
				  result = new ArrayList<SearchResultRow>();
				  
				  while (rs.next()) {
						  result.add( new SearchResultRow(
							  			Arrays.asList(rs.getString(DBTableColName.ACCOUNT.ID),
							  							Integer.toString(index++),
							  							rs.getString(DBTableColName.ACCOUNT.NAME),
														rs.getString(DBTableColName.ACCOUNT.EMAIL),
														Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.ACCOUNT.STATE))
													)
						  						)
								  	);
				  }
			  }
			  
			  rsWrapper.close();			
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }		
	  
	  private AccountDBProcWrapper accountDBProc = new AccountDBProcWrapper();
	  
	  private static List<SearchResultRow> DATA = null;
	
	  private static final String[] TABLE_COL_TITLE_CHECKALL = {"Check All", "全选"};
	  private static final String[] TABLE_COL_TITLE_NO = {"No.", "序号"};
	  private static final String[] TABLE_COL_ACCOUNT_NAME = {"Account", "账户"};
	  private static final String[] TABLE_COL_TITLE_EMAIL = {"Emial", "邮箱"};
	  private static final String[] TABLE_COL_TITLE_STATE = {"State", "状态"};
	  
	
	  private static final List<SearchResultFieldDesc> FIELDS = Arrays.asList(
				new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "10%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_ACCOUNT_NAME[1], true, "25%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_EMAIL[1], true, "35%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false )
				);
}
