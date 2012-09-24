package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.server.config.ViewSearchTableServerConfig;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.user.AccountDBProcWrapper;
import com.eucalyptus.webui.server.user.AccountSyncException;
import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.LanguageSelection;
import com.eucalyptus.webui.shared.config.SearchTableCol;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserRegStatus;
import com.eucalyptus.webui.shared.user.EnumUserType;
import com.eucalyptus.webui.shared.user.UserInfo;

public class AccountServiceProcImpl {
	
	public void createAccount(AccountInfo account, boolean skipRegistration)
	  		throws EucalyptusServiceException {
		
		if ( account == null ) {
		      throw new EucalyptusServiceException( "Empty account para" );
		  }
		  
		try {
			int newAccountId = accountDBProc.addAccount(account);
			
			// if creating account is successful, add corresponding admin user in user table
			if (newAccountId > 0) {
				UserServiceProcImpl userServiceProc = new UserServiceProcImpl();
				
				UserInfo user = new UserInfo();
				user.setAccountId(newAccountId);
				user.setName("admin");
				user.setPwd("admin");
				user.setEmail(account.getEmail());
				user.setState(EnumState.NORMAL);
				user.setType(EnumUserType.ADMIN);
				
				if (skipRegistration)
					user.setRegStatus(EnumUserRegStatus.APPROVED);
				else
					user.setRegStatus(EnumUserRegStatus.REGISTERED);
				
				userServiceProc.createUser(newAccountId, user);
			}
		} catch (AccountSyncException e) {
			// TODO Auto-generated catch blockion
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create account");
		}
	}
	
	public void modifyAccount(AccountInfo account)
	  		throws EucalyptusServiceException {
		
		if ( account == null ) {
		      throw new EucalyptusServiceException( "Empty account para" );
		  }
		  
		try {
			accountDBProc.modifyAccount(account);
		} catch (AccountSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to modify account");
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
			  rs = accountDBProc.queryTotalAccounts(range);
		  } catch (AccountSyncException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  throw new EucalyptusServiceException("Failed to query accounts");
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
			  rsw = accountDBProc.queryTotalAccounts(null);
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
		  
		  List<SearchResultRow> DATA = resultSet2List(rs);
		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
		  SearchResult result = new SearchResult( DATA.size( ), range );
		  result.setDescs( ViewSearchTableServerConfig.instance().getConfig(EnumService.ACCOUNT_SRV, LanguageSelection.instance().getCurLanguage()));
		  
		  if (DATA.size() > 0)
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
				  
				 ArrayList<SearchTableCol> tableCols = ViewSearchTableServerConfig.instance().getConfig(EnumService.ACCOUNT_SRV);
				 String[] dbFields = new String[tableCols.size()];
				  
				 for (int i=0; i<dbFields.length; i++)
					 dbFields[i] = tableCols.get(i).getDbField();
				  
				 while (rs.next()) {
					 ArrayList<String> rowValue = new ArrayList<String>();
					 
					 for (int i=0; i<dbFields.length; i++) {
						  String value;
						  
						  if (!dbFields[i].equalsIgnoreCase("null")) {
							  if (dbFields[i].equalsIgnoreCase(DBTableColName.ACCOUNT.STATE))
								  value = Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.ACCOUNT.STATE));
							  else
								  value = rs.getString(dbFields[i]);
						  }
						  else
							  value = Integer.toString(index++);
					  			 
						  rowValue.add(value);
					 }
					 
					 result.add(new SearchResultRow(rowValue));
				 }
			  }
			  
			  rsWrapper.close();			
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }		
	  
	  private SorterProxy sorterProxy = new SorterProxy(EnumService.ACCOUNT_SRV);
	  private AccountDBProcWrapper accountDBProc = new AccountDBProcWrapper(sorterProxy);
	  
	  ServletUtils servletUtils = new ServletUtils();
}
