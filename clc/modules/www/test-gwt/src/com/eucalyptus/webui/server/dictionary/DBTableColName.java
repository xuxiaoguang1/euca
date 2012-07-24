package com.eucalyptus.webui.server.dictionary;

public class DBTableColName {
	public interface USER {
		public final String ID = "user_id";
		public final String NAME = "user_name";
		public final String PWD = "user_pwd";
		public final String TITLE = "user_title";
		public final String MOBILE = "user_mobile";
		public final String EMAIL = "user_email";
		public final String TYPE = "user_type";
		public final String STATE = "user_state";
		public final String GROUP_ID = "group_id";
		public final String ACCOUNT_ID = "account_id";
	}
	
	public interface ACCOUNT {
		public final String ID = "account_id";
		public final String NAME = "account_name";
		public final String EMAIL = "account_email";
		public final String DES = "account_descrip";
		public final String STATE = "account_state";
	}
	
	public interface GROUP {
		public final String ID = "group_id";
		public final String NAME = "group_name";
		public final String DESCRIPTION = "group_descrip";
		public final String STATE = "group_state";
		public final String ACCOUNT_ID = "account_id";
	}
	
	public interface USER_RESET_PWD {
		public final String ID = "user_reset_pwd_id";
		public final String CODE = "user_reset_pwd_code";
		public final String USER_ID = "user_id";
	}
	
	
	public interface SERVER {
		public final String ID = "server_id";
		public final String NAME = "server_name";
		public final String MARK = "server_mark";
		public final String IMAGE = "server_image";
		public final String CONF = "server_conf";
		public final String IP = "server_ip";
		public final String BW = "server_bw";
		public final String STATE = "server_state";
	}
	
	public interface CPU {
		public final String ID = "cpu_id";
		public final String NAME = "cpu_name";
		public final String VENDOR = "cpu_vendor";
		public final String MODEL = "cpu_model";
		public final String GHZ = "cpu_ghz";
		public final String CACHE = "cpu_cache";
		public final String SERVER_ID = "server_id";
	}
	
	public interface CPU_SERVICE {
		public final String ID = "cs_id";
		public final String STARTTIME = "cs_starttime";
		public final String LIFE = "cs_life";
		public final String STATE = "cs_state";
		public final String CPU_ID = "cpu_id";
		public final String USER_ID = "user_id";
	}
}
