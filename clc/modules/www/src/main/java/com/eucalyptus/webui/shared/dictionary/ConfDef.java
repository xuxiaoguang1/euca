package com.eucalyptus.webui.shared.dictionary;

public class ConfDef {
	public static Long WEBSESSION_LIFE_IN_MINUTES = 24 * 60L;// 24 hours in minutes
	
	public static String DB_URL = "jdbc:mysql://127.0.0.1:3306/eucalyptus";
	public static String DB_USR = "root";
	public static String DB_PWD = "root";
	
	public static int DEFAULT_PWD_LEN = 6;
	
	public static int DB_DEL_FIELD_VALID_STATE = 0;
	public static int DB_DEL_FIELD_INVALID_STATE = 1;
	
	public static String ROOT_ACCOUNT = "eucalyptus";
	public static String ACCOUNT_ADMIN_NAME = "admin";
}
