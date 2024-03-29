package com.eucalyptus.webui.shared.resource.device;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CellTableColumns {
    
    public class AREA {
        
        public static final int AREA_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int AREA_NAME = 3;
        public static final int AREA_DESC = 4;
        public static final int AREA_CREATIONTIME = 5;
        public static final int AREA_MODIFIEDTIME = 6;
        
        public static final int COLUMN_SIZE = 7;
        
    }
    
    public class ROOM {
        
        public static final int ROOM_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int ROOM_NAME = 3;
        public static final int ROOM_DESC = 4;
        public static final int AREA_NAME = 5;
        public static final int ROOM_CREATIONTIME = 6;
        public static final int ROOM_MODIFIEDTIME = 7;
        
        public static final int COLUMN_SIZE = 8;
        
    }
    
    public class CABINET {
        
        public static final int CABINET_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int CABINET_NAME = 3;
        public static final int CABINET_DESC = 4;
        public static final int ROOM_NAME = 5;
        public static final int CABINET_CREATIONTIME = 6;
        public static final int CABINET_MODIFIEDTIME = 7;
        
        public static final int COLUMN_SIZE = 8;
        
    }
    
    public class SERVER {
        
        public static final int SERVER_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int SERVER_NAME = 3;
        public static final int SERVER_DESC = 4;
        public static final int CABINET_NAME = 5;
        public static final int SERVER_IP = 6;
        public static final int SERVER_BW = 7;
        public static final int SERVER_STATE = 8;
        public static final int SERVER_CREATIONTIME = 9;
        public static final int SERVER_MODIFIEDTIME = 10;
        
        public static final int COLUMN_SIZE = 11;
        
    }
    
    public class CPU {

        public static final int CPU_SERVICE_ID = 0;
        public static final int CPU_ID = 1;
        public static final int RESERVED_CHECKBOX = 2;
        public static final int RESERVED_INDEX = 3;
        public static final int SERVER_NAME = 4;
        public static final int CPU_SERVICE_STATE = 5;
        public static final int ACCOUNT_NAME = 6;
        public static final int USER_NAME = 7;
        public static final int CPU_SERVICE_DESC = 8;
        public static final int CPU_TOTAL = 9;
        public static final int CPU_SERVICE_USED = 10;
        public static final int CPU_SERVICE_STARTTIME = 11;
        public static final int CPU_SERVICE_ENDTIME = 12;
        public static final int CPU_SERVICE_LIFE = 13;
        public static final int CPU_SERVICE_CREATIONTIME = 14;
        public static final int CPU_SERVICE_MODIFIEDTIME = 15;
        public static final int CPU_DESC = 16;
        public static final int CPU_CREATIONTIME = 17;
        public static final int CPU_MODIFIEDTIME = 18;
        
        public static final int COLUMN_SIZE = 19;
        
    }
    
    public class MEMORY {
        
        public static final int MEMORY_SERVICE_ID = 0;
        public static final int MEMORY_ID = 1;
        public static final int RESERVED_CHECKBOX = 2;
        public static final int RESERVED_INDEX = 3;
        public static final int SERVER_NAME = 4;
        public static final int MEMORY_SERVICE_STATE = 5;
        public static final int ACCOUNT_NAME = 6;
        public static final int USER_NAME = 7;
        public static final int MEMORY_SERVICE_DESC = 8;
        public static final int MEMORY_TOTAL = 9;
        public static final int MEMORY_SERVICE_USED = 10;
        public static final int MEMORY_SERVICE_STARTTIME = 11;
        public static final int MEMORY_SERVICE_ENDTIME = 12;
        public static final int MEMORY_SERVICE_LIFE = 13;
        public static final int MEMORY_SERVICE_CREATIONTIME = 14;
        public static final int MEMORY_SERVICE_MODIFIEDTIME = 15;
        public static final int MEMORY_DESC = 16;
        public static final int MEMORY_CREATIONTIME = 17;
        public static final int MEMORY_MODIFIEDTIME = 18;
        
        public static final int COLUMN_SIZE = 19;
        
    }
    
    public class DISK {
        
        public static final int DISK_SERVICE_ID = 0;
        public static final int DISK_ID = 1;
        public static final int RESERVED_CHECKBOX = 2;
        public static final int RESERVED_INDEX = 3;
        public static final int SERVER_NAME = 4;
        public static final int DISK_SERVICE_STATE = 5;
        public static final int ACCOUNT_NAME = 6;
        public static final int USER_NAME = 7;
        public static final int DISK_SERVICE_DESC = 8;
        public static final int DISK_TOTAL = 9;
        public static final int DISK_SERVICE_USED = 10;
        public static final int DISK_SERVICE_STARTTIME = 11;
        public static final int DISK_SERVICE_ENDTIME = 12;
        public static final int DISK_SERVICE_LIFE = 13;
        public static final int DISK_SERVICE_CREATIONTIME = 14;
        public static final int DISK_SERVICE_MODIFIEDTIME = 15;
        public static final int DISK_DESC = 16;
        public static final int DISK_CREATIONTIME = 17;
        public static final int DISK_MODIFIEDTIME = 18;
        
        public static final int COLUMN_SIZE = 19;
        
    }
    
    public class IP {
        
        public static final int IP_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int IP_ADDR = 3;
        public static final int IP_TYPE = 4;
        public static final int IP_SERVICE_STATE = 5;
        public static final int ACCOUNT_NAME = 6;
        public static final int USER_NAME = 7;
        public static final int IP_SERVICE_DESC = 8;
        public static final int IP_SERVICE_STARTTIME = 9;
        public static final int IP_SERVICE_ENDTIME = 10;
        public static final int IP_SERVICE_LIFE = 11;
        public static final int IP_SERVICE_CREATIONTIME = 12;
        public static final int IP_SERVICE_MODIFIEDTIME = 13;
        
        public static final int COLUMN_SIZE = 14;
        
    }
    
    public class BW {
        
        public static final int BW_SERVICE_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int IP_ADDR = 3;
        public static final int IP_TYPE = 4;
        public static final int BW_SERVICE_BW_MAX = 5;
        public static final int BW_SERVICE_BW = 6;
        public static final int ACCOUNT_NAME = 7;
        public static final int USER_NAME = 8;
        public static final int BW_SERVICE_DESC = 9;
        public static final int BW_SERVICE_STARTTIME = 10;
        public static final int BW_SERVICE_ENDTIME = 11;
        public static final int BW_SERVICE_LIFE = 12;
        public static final int BW_SERVICE_CREATIONTIME = 13;
        public static final int BW_SERVICE_MODIFIEDTIME = 14;
        
        public static final int COLUMN_SIZE = 15;
        
    }
    
    public class TEMPLATE {
        
        public static final int TEMPLATE_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int TEMPLATE_NAME = 3;
        public static final int TEMPLATE_DESC = 4;
        public static final int TEMPLATE_NCPUS = 5;
        public static final int TEMPLATE_MEM = 6;
        public static final int TEMPLATE_DISK = 7;
        public static final int TEMPLATE_BW = 8;
        public static final int TEMPLATE_CREATIONTIME = 9;
        public static final int TEMPLATE_MODIFIEDTIME = 10;
        
        public static final int COLUMN_SIZE = 11;
        
    }
    
    public class CPU_PRICE {
        
        public static final int CPU_PRICE_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int CPU_NAME = 3;
        public static final int CPU_PRICE_DESC = 4;
        public static final int CPU_PRICE = 5;
        public static final int CPU_PRICE_CREATIONTIME = 6;
        public static final int CPU_PRICE_MODIFIEDTIME = 7;
        
        public static final int COLUMN_SIZE = 8;
        
    }
    
    public class TEMPLATE_PRICE {
        
        public static final int TEMPLATE_PRICE_ID = 0;
        public static final int RESERVED_CHECKBOX = 1;
        public static final int RESERVED_INDEX = 2;
        public static final int TEMPLATE_NAME = 3;
        public static final int TEMPLATE_PRICE_DESC = 4;
        public static final int TEMPLATE_PRICE_TOTAL = 5;
        public static final int TEMPLATE_CPU_NCPUS = 6;
        public static final int TEMPLATE_CPU_PRICE = 7; 
        public static final int TEMPLATE_MEM_TOTAL = 8;
        public static final int TEMPLATE_MEM_PRICE = 9;
        public static final int TEMPLATE_DISK_TOTAL = 10;
        public static final int TEMPLATE_DISK_PRICE = 11;
        public static final int TEMPLATE_BW_TOTAL = 12;
        public static final int TEMPLATE_BW_PRICE = 13;
        public static final int TEMPLATE_PRICE_CREATIONTIME = 14;
        public static final int TEMPLATE_PRICE_MODIFIEDTIME = 15;
        
        public static final int COLUMN_SIZE = 16;
        
    }
    
    public static class CellTableColumnsRow {
        
        private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
        private String[] array;
        
        public CellTableColumnsRow(int size) {
            array = new String[size];
        }
        
        public void setColumn(int index, Integer value) {
            if (value != null) {
                array[index] = Integer.toString(value);
            }
            else {
                array[index] = "";
            }
        }
        
        public void setColumn(int index, Long value) {
            if (value != null) {
                array[index] = Long.toString(value);
            }
            else {
                array[index] = "";
            }
        }
        
        public void setColumn(int index, Double value) {
            if (value != null) {
                array[index] = Double.toString(value);
            }
            else {
                array[index] = "";
            }
        }
        
        public void setColumn(int index, String value) {
            if (value != null) {
                array[index] = value;
            }
            else {
                array[index] = "";
            }
        }
        
        public void setColumn(int index, Date date) {
            if (date != null) {
                array[index] = formatter.format(date);
            }
            else {
                array[index] = "";
            }
        }
        
        public List<String> toList() {
            return Arrays.asList(array);
        }
        
    }
    
}
