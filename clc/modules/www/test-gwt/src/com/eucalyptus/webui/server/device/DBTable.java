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
    public static final DBTableMemory MEMORY = new DBTableMemory();
    public static final DBTableMemoryService MEMORY_SERVICE = new DBTableMemoryService();
    public static final DBTableDisk DISK = new DBTableDisk();
    public static final DBTableDiskService DISK_SERVICE = new DBTableDiskService();
    public static final DBTableIPService IP_SERVICE = new DBTableIPService();
    public static final DBTableBWService BW_SERVICE = new DBTableBWService();
    
    public static final DBTableTemplate TEMPLATE = new DBTableTemplate();
    
    public static final DBTableDevicePrice DEVICE_PRICE = new DBTableDevicePrice();
    public static final DBTableTemplatePrice TEMPLATE_PRICE = new DBTableTemplatePrice();
    
    public static final DBTableUserApp USER_APP = new DBTableUserApp();
    
    public final DBTableColumn ANY = new DBTableColumn(this, "*"); 
    
}

class DBTableColumn {
    
    private DBTable table;
    private String name;
    
    public DBTableColumn(String name) {
        this(null, name);
    }
    
    public DBTableColumn(DBTable table, String name) {
        this.table = table;
        this.name = name;
    }
    
    @Override
    public String toString() {
        if (table == null) {
            return name;
        }
        return table + "." + name;
    }
    
    public boolean belongsTo(DBTable table) {
        return this.table == table;
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
    public final DBTableColumn SERVER_EUCA = new DBTableColumn(this, "server_euca_zone");
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
    public final DBTableColumn CPU_DESC = new DBTableColumn(this, "cpu_desc");
    public final DBTableColumn CPU_TOTAL = new DBTableColumn(this, "cpu_total");
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
    public final DBTableColumn CPU_SERVICE_USED = new DBTableColumn(this, "cs_used");
    public final DBTableColumn CPU_SERVICE_STARTTIME = new DBTableColumn(null, "cs_starttime");
    public final DBTableColumn CPU_SERVICE_ENDTIME = new DBTableColumn(null, "cs_endtime");
    public final DBTableColumn CPU_SERVICE_LIFE = new DBTableColumn(null, "cs_life");
    public final DBTableColumn CPU_SERVICE_STATE = new DBTableColumn(this, "cs_state");
    public final DBTableColumn CPU_SERVICE_CREATIONTIME = new DBTableColumn(this, "cs_creationtime");
    public final DBTableColumn CPU_SERVICE_MODIFIEDTIME = new DBTableColumn(this, "cs_modifiedtime");
    public final DBTableColumn CPU_ID = new DBTableColumn(this, "cpu_id");
    public final DBTableColumn USER_ID = new DBTableColumn(this, "user_id");
    
}

class DBTableMemory extends DBTable {
    
    public DBTableMemory() {
        super("memory");
    }
    
    public final DBTableColumn MEMORY_ID = new DBTableColumn(this, "mem_id");
    public final DBTableColumn MEMORY_DESC = new DBTableColumn(this, "mem_desc");
    public final DBTableColumn MEMORY_TOTAL = new DBTableColumn(this, "mem_total");
    public final DBTableColumn MEMORY_CREATIONTIME = new DBTableColumn(this, "mem_creationtime");
    public final DBTableColumn MEMORY_MODIFIEDTIME = new DBTableColumn(this, "mem_modifiedtime");
    public final DBTableColumn SERVER_ID = new DBTableColumn(this, "server_id");
    
}

class DBTableMemoryService extends DBTable {
    
    public DBTableMemoryService() {
        super("mem_service");
    }
    
    public final DBTableColumn MEMORY_SERVICE_ID = new DBTableColumn(this, "ms_id");
    public final DBTableColumn MEMORY_SERVICE_DESC = new DBTableColumn(this, "ms_desc");
    public final DBTableColumn MEMORY_SERVICE_USED = new DBTableColumn(this, "ms_used");
    public final DBTableColumn MEMORY_SERVICE_STARTTIME = new DBTableColumn(null, "ms_starttime");
    public final DBTableColumn MEMORY_SERVICE_ENDTIME = new DBTableColumn(null, "ms_endtime");
    public final DBTableColumn MEMORY_SERVICE_LIFE = new DBTableColumn(null, "ms_life");
    public final DBTableColumn MEMORY_SERVICE_STATE = new DBTableColumn(this, "ms_state");
    public final DBTableColumn MEMORY_SERVICE_CREATIONTIME = new DBTableColumn(this, "ms_creationtime");
    public final DBTableColumn MEMORY_SERVICE_MODIFIEDTIME = new DBTableColumn(this, "ms_modifiedtime");
    public final DBTableColumn MEMORY_ID = new DBTableColumn(this, "mem_id");
    public final DBTableColumn USER_ID = new DBTableColumn(this, "user_id");
    
}

class DBTableDisk extends DBTable {
    
    public DBTableDisk() {
        super("disk");
    }
    
    public final DBTableColumn DISK_ID = new DBTableColumn(this, "disk_id");
    public final DBTableColumn DISK_DESC = new DBTableColumn(this, "disk_desc");
    public final DBTableColumn DISK_TOTAL = new DBTableColumn(this, "disk_total");
    public final DBTableColumn DISK_CREATIONTIME = new DBTableColumn(this, "disk_creationtime");
    public final DBTableColumn DISK_MODIFIEDTIME = new DBTableColumn(this, "disk_modifiedtime");
    public final DBTableColumn SERVER_ID = new DBTableColumn(this, "server_id");
    
}

class DBTableDiskService extends DBTable {
    
    public DBTableDiskService() {
        super("disk_service");
    }
    
    public final DBTableColumn DISK_SERVICE_ID = new DBTableColumn(this, "ds_id");
    public final DBTableColumn DISK_SERVICE_DESC = new DBTableColumn(this, "ds_desc");
    public final DBTableColumn DISK_SERVICE_USED = new DBTableColumn(this, "ds_used");
    public final DBTableColumn DISK_SERVICE_STARTTIME = new DBTableColumn(null, "ds_starttime");
    public final DBTableColumn DISK_SERVICE_ENDTIME = new DBTableColumn(null, "ds_endtime");
    public final DBTableColumn DISK_SERVICE_LIFE = new DBTableColumn(null, "ds_life");
    public final DBTableColumn DISK_SERVICE_STATE = new DBTableColumn(this, "ds_state");
    public final DBTableColumn DISK_SERVICE_CREATIONTIME = new DBTableColumn(this, "ds_creationtime");
    public final DBTableColumn DISK_SERVICE_MODIFIEDTIME = new DBTableColumn(this, "ds_modifiedtime");
    public final DBTableColumn DISK_ID = new DBTableColumn(this, "disk_id");
    public final DBTableColumn USER_ID = new DBTableColumn(this, "user_id");
    
}

class DBTableIPService extends DBTable {
    
    public DBTableIPService() {
        super("ip_service");
    }
    
    public final DBTableColumn IP_ID = new DBTableColumn(this, "ip_id");
    public final DBTableColumn IP_ADDR = new DBTableColumn(this, "ip_addr");
    public final DBTableColumn IP_TYPE = new DBTableColumn(this, "ip_type");
    public final DBTableColumn IP_SERVICE_DESC = new DBTableColumn(this, "is_desc");
    public final DBTableColumn IP_SERVICE_STARTTIME = new DBTableColumn(null, "is_starttime");
    public final DBTableColumn IP_SERVICE_ENDTIME = new DBTableColumn(null, "is_endtime");
    public final DBTableColumn IP_SERVICE_LIFE = new DBTableColumn(null, "is_life");
    public final DBTableColumn IP_SERVICE_STATE = new DBTableColumn(this, "is_state");
    public final DBTableColumn IP_SERVICE_CREATIONTIME = new DBTableColumn(this, "is_creationtime");
    public final DBTableColumn IP_SERVICE_MODIFIEDTIME = new DBTableColumn(this, "is_modifiedtime");
    public final DBTableColumn USER_ID = new DBTableColumn(this, "user_id");
    
}

class DBTableBWService extends DBTable {
    
    public DBTableBWService() {
        super("bw_service");
    }
    
    public final DBTableColumn BW_SERVICE_ID = new DBTableColumn(this, "bs_id");
    public final DBTableColumn BW_SERVICE_DESC = new DBTableColumn(this, "bs_desc");
    public final DBTableColumn BW_SERVICE_STARTTIME = new DBTableColumn(null, "bs_starttime");
    public final DBTableColumn BW_SERVICE_ENDTIME = new DBTableColumn(null, "bs_endtime");
    public final DBTableColumn BW_SERVICE_LIFE = new DBTableColumn(null, "bs_life");
    public final DBTableColumn BW_SERVICE_BW = new DBTableColumn(this, "bs_bw");
    public final DBTableColumn BW_SERVICE_BW_MAX = new DBTableColumn(this, "bs_bw_max");
    public final DBTableColumn BW_SERVICE_CREATIONTIME = new DBTableColumn(this, "bs_creationtime");
    public final DBTableColumn BW_SERVICE_MODIFIEDTIME = new DBTableColumn(this, "bs_modifiedtime");
    public final DBTableColumn IP_ID = new DBTableColumn(this, "ip_id");
    
}

class DBTableTemplate extends DBTable {
    
    public DBTableTemplate() {
        super("template");
    }
    
    public final DBTableColumn TEMPLATE_ID = new DBTableColumn(this, "template_id");
    public final DBTableColumn TEMPLATE_NAME = new DBTableColumn(this, "template_name");
    public final DBTableColumn TEMPLATE_DESC = new DBTableColumn(this, "template_desc");
    public final DBTableColumn TEMPLATE_NCPUS = new DBTableColumn(this, "template_ncpus");
    public final DBTableColumn TEMPLATE_MEM = new DBTableColumn(this, "template_mem");
    public final DBTableColumn TEMPLATE_DISK = new DBTableColumn(this, "template_disk");
    public final DBTableColumn TEMPLATE_BW = new DBTableColumn(this, "template_bw");
    public final DBTableColumn TEMPLATE_CREATIONTIME = new DBTableColumn(this, "template_creationtime");
    public final DBTableColumn TEMPLATE_MODIFIEDTIME = new DBTableColumn(this, "template_modifiedtime");
    
}

class DBTableDevicePrice extends DBTable {
    
    public DBTableDevicePrice() {
        super("device_price");
    }
    
    public final DBTableColumn DEVICE_PRICE_NAME = new DBTableColumn(this, "device_price_name");
    public final DBTableColumn DEVICE_PRICE = new DBTableColumn(this, "device_price");
    public final DBTableColumn DEVICE_PRICE_DESC = new DBTableColumn(this, "device_price_desc");
    public final DBTableColumn DEVICE_PRICE_MODIFIEDTIME = new DBTableColumn(this, "device_price_modifiedtime");
    
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

class DBTableUserApp extends DBTable {
    
    public DBTableUserApp() {
        super("user_app");
    }
    
    public final DBTableColumn CPU_SERVICE_ID = new DBTableColumn(this, "cs_id");
    public final DBTableColumn MEMORY_SERVICE_ID = new DBTableColumn(this, "ms_id");
    public final DBTableColumn DISK_SERVICE_ID = new DBTableColumn(this, "ds_id");
    public final DBTableColumn PUBLIC_IP_ID = new DBTableColumn(this, "public_is_id");
    public final DBTableColumn PUBLIC_BW_SERVICE_ID = new DBTableColumn(this, "bs_id");
    public final DBTableColumn PRIVATE_IP_ID = new DBTableColumn(this, "private_is_id");
    public final DBTableColumn SERVICE_STARTTIME = new DBTableColumn(this, "ua_srv_startingtime");
    public final DBTableColumn SERVICE_ENDTIME = new DBTableColumn(this, "ua_srv_endingtime");

}