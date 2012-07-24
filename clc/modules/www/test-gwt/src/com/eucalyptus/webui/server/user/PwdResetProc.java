package com.eucalyptus.webui.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.server.dictionary.EucalyptusServiceExceptionMsg;

public class PwdResetProc {
	
	  public void requestPasswordRecovery( String userName, String accountName, String email ) throws EucalyptusServiceException {
	    // TODO Auto-generated method stub
	    //firstly, check whether the user is a valid user
		UserDBProcWrapper userDBProc = new UserDBProcWrapper();
		UserInfoAndState userAndState = null;
		try {
			userAndState = userDBProc.lookupUserAndState(accountName, userName);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.USER_NOT_EXISTED[1]);
		}
		  
		//secondly, create the confirmationCode for resetting pwd
		String confirmationCode = RandomPwdCreator.genRandomNum(16);
		
		//thirdly, store the confirmationCod in DB for changing pwd
		StringBuilder sql = new StringBuilder("INSERT INTO ").
								append(DBTableName.USER_RESET_PWD).
								append(" ( ").
								append(DBTableColName.USER_RESET_PWD.ID).
								append(", ").
								append(DBTableColName.USER_RESET_PWD.CODE).
								append(", ").
								append(DBTableColName.USER_RESET_PWD.USER_ID).
								append(") VALUES (").
								append("null, '").
								append(confirmationCode).
								append("', ").
								append(userAndState.getUserInfo().getId()).
								append(")");
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.USER_PWD_RESET_FAILED[1]);
		}
		
		//at last, send the confirmation code to user by email
	  }

	  public void resetPassword( String confirmationCode, String password ) throws EucalyptusServiceException {
	    // TODO Auto-generated method stub
	    //firstly, get the user id by confirmationCode
		StringBuilder sql = new StringBuilder("SELECT * FROM ").
								append(DBTableName.USER_RESET_PWD).
								append(" WHERE ").
								append(DBTableColName.USER_RESET_PWD.CODE).
								append(" = '").
								append(confirmationCode).
								append("'");
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			ResultSetWrapper rsw = dbProc.query(sql.toString());
			ResultSet rs = rsw.getResultSet();
			
			int userId = Integer.valueOf(rs.getString(DBTableColName.USER_RESET_PWD.USER_ID));
			
			UserDBProcWrapper userDBProc = new UserDBProcWrapper();
			userDBProc.changePwd(userId, password);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.USER_PWD_RESET_FAILED[1]);
		}
	  }
}
