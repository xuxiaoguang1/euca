package com.eucalyptus.webui.server.device;

public class DBTable {
	
	private String name;
	
	DBTable(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static final DBTableAccount ACCOUNT = new DBTableAccount();
	public static final DBTableUser USER = new DBTableUser();
	
	public static final DBTableArea AREA = new DBTableArea();
	public static final DBTableRoom ROOM = new DBTableRoom();
	public static final DBTableCabinet CABINET = new DBTableCabinet();
	public static final DBTableServer SERVER = new DBTableServer();
	
	public static final DBTableCPU CPU = new DBTableCPU();
	public static final DBTableCPUService CPU_SERVICE = new DBTableCPUService();
	
	public static final DBTableTemplate TEMPLATE = new DBTableTemplate();
	
	public static final DBTableCPUPrice CPU_PRICE = new DBTableCPUPrice();
	public static final DBTableOthersPrice OTHERS_PRICE = new DBTableOthersPrice();
	public static final DBTableTemplatePrice TEMPLATE_PRICE = new DBTableTemplatePrice();
	
	public static DBTableAlias getDBTableAlias(String table) {
	    return new DBTableAlias(table);
	}
	
	public final DBTableColumn ANY = new DBTableColumn(this, "*"); 
	
}

class DBTableColumn {
	
	private DBTable table;
	private String name;
	
	public DBTableColumn(DBTable table, String name) {
		this.table = table;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return table + "." + getName();
	}
	
	public String getName() {
		return name;
	}
				
}

class DBTableAlias extends DBTable {
    
    public DBTableAlias(String table) {
        super(table);
    }
    
    public DBTableColumn getColumn(DBTableColumn column) {
        return new DBTableColumn(this, column.getName());
    }
    
}

class DBTableAccount extends DBTable {
    
    public DBTableAccount() {
        super("account");
    }
    
    public final DBTableColumn ACCOUNT_ID = new DBTableColumn(this, "account_id");
    public final DBTableColumn ACCOUNT_NAME = new DBTableColumn(this, "account_name");
    
}

class DBTableUser extends DBTable {
    
    public DBTableUser() {
        super("user");
    }
    
    public final DBTableColumn USER_ID = new DBTableColumn(this, "user_id");
    public final DBTableColumn USER_NAME = new DBTableColumn(this, "user_name");
    public final DBTableColumn ACCOUNT_ID = new DBTableColumn(this, "account_id");
    
}

class DBTableArea extends DBTable {
	
	public DBTableArea() {
		super("area");
	}
	
	public final DBTableColumn AREA_ID = new DBTableColumn(this, "area_id");
	public final DBTableColumn AREA_NAME = new DBTableColumn(this, "area_name");
	public final DBTableColumn AREA_DESC = new DBTableColumn(this, "area_desc");
	public final DBTableColumn AREA_CREATIONTIME = new DBTableColumn(this, "area_creationtime");
	public final DBTableColumn AREA_MODIFIEDTIME = new DBTableColumn(this, "area_modifiedtime");
	
}

class DBTableRoom extends DBTable {
	
	public DBTableRoom() {
		super("room");
	}
	
	public final DBTableColumn ROOM_ID = new DBTableColumn(this, "room_id");
	public final DBTableColumn ROOM_NAME = new DBTableColumn(this, "room_name");
	public final DBTableColumn ROOM_DESC = new DBTableColumn(this, "room_desc");
	public final DBTableColumn ROOM_CREATIONTIME = new DBTableColumn(this, "room_creationtime");
	public final DBTableColumn ROOM_MODIFIEDTIME = new DBTableColumn(this, "room_modifiedtime");
	public final DBTableColumn AREA_ID = new DBTableColumn(this, "area_id");
	
}
 
class DBTableCabinet extends DBTable {
	
	public DBTableCabinet() {
		super("cabinet");
	}
	
	public final DBTableColumn CABINET_ID = new DBTableColumn(this, "cabinet_id");
	public final DBTableColumn CABINET_NAME = new DBTableColumn(this, "cabinet_name");
	public final DBTableColumn CABINET_DESC = new DBTableColumn(this, "cabinet_desc");
	public final DBTableColumn CABINET_CREATIONTIME = new DBTableColumn(this, "cabinet_creationtime");
	public final DBTableColumn CABINET_MODIFIEDTIME = new DBTableColumn(this, "cabinet_modifiedtime");
	public final DBTableColumn ROOM_ID = new DBTableColumn(this, "room_id");
	
}

class DBTableServer extends DBTable {
	
	public DBTableServer() {
		super("server");
	}
	
	public final DBTableColumn SERVER_ID = new DBTableColumn(this, "server_id");
	public final DBTableColumn SERVER_NAME = new DBTableColumn(this, "server_name");
	public final DBTableColumn SERVER_DESC = new DBTableColumn(this, "server_desc");
	public final DBTableColumn SERVER_IP = new DBTableColumn(this, "server_ip");
	public final DBTableColumn SERVER_BW = new DBTableColumn(this, "server_bw");
	public final DBTableColumn SERVER_STATE = new DBTableColumn(this, "server_state");
	public final DBTableColumn SERVER_CREATIONTIME = new DBTableColumn(this, "server_creationtime");
	public final DBTableColumn SERVER_MODIFIEDTIME = new DBTableColumn(this, "server_modifiedtime");
	public final DBTableColumn CABINET_ID = new DBTableColumn(this, "cabinet_id");
	
}

class DBTableCPU extends DBTable {
	
	public DBTableCPU() {
		super("cpu");
	}
	
	public final DBTableColumn CPU_ID = new DBTableColumn(this, "cpu_id");
	public final DBTableColumn CPU_NAME = new DBTableColumn(this, "cpu_name");
	public final DBTableColumn CPU_DESC = new DBTableColumn(this, "cpu_desc");
	public final DBTableColumn CPU_VENDOR = new DBTableColumn(this, "cpu_vendor");
	public final DBTableColumn CPU_MODEL = new DBTableColumn(this, "cpu_model");
	public final DBTableColumn CPU_GHZ = new DBTableColumn(this, "cpu_ghz");
	public final DBTableColumn CPU_CACHE = new DBTableColumn(this, "cpu_cache");
	public final DBTableColumn CPU_CREATIONTIME = new DBTableColumn(this, "cpu_creationtime");
	public final DBTableColumn CPU_MODIFIEDTIME = new DBTableColumn(this, "cpu_modifiedtime");
	public final DBTableColumn SERVER_ID = new DBTableColumn(this, "server_id");
	
}

class DBTableCPUService extends DBTable {
    
    public DBTableCPUService() {
        super("cpu_service");
    }
    
    public final DBTableColumn CPU_SERVICE_ID = new DBTableColumn(this, "cs_id");
    public final DBTableColumn CPU_SERVICE_DESC = new DBTableColumn(this, "cs_desc");
    public final DBTableColumn CPU_SERVICE_STARTTIME = new DBTableColumn(this, "cs_starttime");
    public final DBTableColumn CPU_SERVICE_ENDTIME = new DBTableColumn(this, "cs_endtime");
    public final DBTableColumn CPU_SERVICE_STATE = new DBTableColumn(this, "cs_state");
    public final DBTableColumn CPU_SERVICE_CREATIONTIME = new DBTableColumn(this, "cs_creationtime");
    public final DBTableColumn CPU_SERVICE_MODIFIEDTIME = new DBTableColumn(this, "cs_modifiedtime");
    public final DBTableColumn CPU_ID = new DBTableColumn(this, "cpu_id");
    public final DBTableColumn USER_ID = new DBTableColumn(this, "user_id");
    
}

class DBTableTemplate extends DBTable {
	
	public DBTableTemplate() {
		super("template");
	}
	
	public final DBTableColumn TEMPLATE_ID = new DBTableColumn(this, "template_id");
	public final DBTableColumn TEMPLATE_NAME = new DBTableColumn(this, "template_name");
	public final DBTableColumn TEMPLATE_CPU = new DBTableColumn(this, "template_cpu");
	public final DBTableColumn TEMPLATE_MEM = new DBTableColumn(this, "template_mem");
	public final DBTableColumn TEMPLATE_DISK = new DBTableColumn(this, "template_disk");
	public final DBTableColumn TEMPLATE_BW = new DBTableColumn(this, "template_bw");
	public final DBTableColumn TEMPLATE_NCPUS = new DBTableColumn(this, "template_ncpus");

}

class DBTableCPUPrice extends DBTable {

	public DBTableCPUPrice() {
		super("cpu_price");
	}
	
	public final DBTableColumn CPU_PRICE_ID = new DBTableColumn(this, "cpu_price_id");
	public final DBTableColumn CPU_NAME = new DBTableColumn(this, "cpu_name");
	public final DBTableColumn CPU_PRICE_DESC = new DBTableColumn(this, "cpu_price_desc");
	public final DBTableColumn CPU_PRICE = new DBTableColumn(this, "cpu_price");
	public final DBTableColumn CPU_PRICE_CREATIONTIME = new DBTableColumn(this, "cpu_price_creationtime");
	public final DBTableColumn CPU_PRICE_MODIFIEDTIME = new DBTableColumn(this, "cpu_price_modifiedtime");
	
}

class DBTableOthersPrice extends DBTable {
    
    public DBTableOthersPrice() {
        super("others_price");
    }
    
    public final DBTableColumn OTHERS_PRICE_NAME = new DBTableColumn(this, "others_price_name");
    public final DBTableColumn OTHERS_PRICE = new DBTableColumn(this, "others_price");
    public final DBTableColumn OTHERS_PRICE_DESC = new DBTableColumn(this, "others_price_desc");
    public final DBTableColumn OTHERS_PRICE_MODIFIEDTIME = new DBTableColumn(this, "others_price_modifiedtime");
    
}

class DBTableTemplatePrice extends DBTable {
	
	public DBTableTemplatePrice() {
		super("template_price");
	}
	
	public final DBTableColumn TEMPLATE_PRICE_ID = new DBTableColumn(this, "template_price_id");
	public final DBTableColumn TEMPLATE_PRICE_DESC = new DBTableColumn(this, "template_price_desc");
	public final DBTableColumn TEMPLATE_PRICE_CPU = new DBTableColumn(this, "template_price_cpu");
	public final DBTableColumn TEMPLATE_PRICE_MEM = new DBTableColumn(this, "template_price_mem");
	public final DBTableColumn TEMPLATE_PRICE_DISK = new DBTableColumn(this, "template_price_disk");
	public final DBTableColumn TEMPLATE_PRICE_BW = new DBTableColumn(this, "template_price_bw");
	public final DBTableColumn TEMPLATE_PRICE_CREATIONTIME = new DBTableColumn(this, "template_price_creationtime");
	public final DBTableColumn TEMPLATE_PRICE_MODIFIEDTIME = new DBTableColumn(this, "template_price_modifiedtime");
	public final DBTableColumn TEMPLATE_ID = new DBTableColumn(this, "template_id");

}

