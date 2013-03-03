package com.eucalyptus.webui.server.device;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.DevicePriceInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceDevicePriceService {
    
    private static DeviceDevicePriceService instance = new DeviceDevicePriceService();
    
    public static DeviceDevicePriceService getInstance() {
        return instance;
    }
    
    private DeviceDevicePriceService() {
        /* do nothing */
    }
    
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private static final String DEVICE_PRICE_CPU = "cpu";
	private static final String DEVICE_PRICE_MEMORY = "memory";
	private static final String DEVICE_PRICE_DISK = "disk";
	private static final String DEVICE_PRICE_BANDWIDTH = "bandwidth";
	
	private DevicePriceInfo lookupDevicePriceInfoByName(String op_name) throws EucalyptusServiceException {
	    Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableDevicePrice DEVICE_PRICE = DBTable.DEVICE_PRICE;
            ResultSet rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, false, op_name);
            if (!rs.next()) {
                DeviceOthersPriceDBProcWrapper.createOthersPrice(conn, op_name, "", 0);
                rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, false, op_name);
                rs.next();
            }
            String op_desc = DBData.getString(rs, DEVICE_PRICE.DEVICE_PRICE_DESC);
            double op_price = DBData.getDouble(rs, DEVICE_PRICE.DEVICE_PRICE);
            Date op_modifiedtime = DBData.getDate(rs, DEVICE_PRICE.DEVICE_PRICE_MODIFIEDTIME);
            return new DevicePriceInfo(op_name, op_desc, op_price, op_modifiedtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public DevicePriceInfo lookupDevicePriceCPU() throws EucalyptusServiceException {
        return lookupDevicePriceInfoByName(DEVICE_PRICE_CPU);
    }
	
	public DevicePriceInfo lookupDevicePriceMemory() throws EucalyptusServiceException {
	    return lookupDevicePriceInfoByName(DEVICE_PRICE_MEMORY);
	}
	
	public DevicePriceInfo lookupDevicePriceDisk() throws EucalyptusServiceException {
	    return lookupDevicePriceInfoByName(DEVICE_PRICE_DISK);
	}
	
	public DevicePriceInfo lookupDevicePriceBandwidth() throws EucalyptusServiceException {
        return lookupDevicePriceInfoByName(DEVICE_PRICE_BANDWIDTH);
    }
	
	private void modifyDevicePriceByName(Session session, String op_name, String op_desc, double op_price) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
	    if (op_name == null || op_name.isEmpty()) {
	        throw new EucalyptusServiceException(ClientMessage.invalidValue("Service Name", "服务名称"));
	    }
        Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            conn.setAutoCommit(false);
            DBTableDevicePrice DEVICE_PRICE = DBTable.DEVICE_PRICE;
            ResultSet rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, true, op_name);
            if (!rs.next()) {
                DeviceOthersPriceDBProcWrapper.createOthersPrice(conn, op_name, "", 0);
                rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, true, op_name);
                rs.next();
            }
            rs.updateString(DEVICE_PRICE.DEVICE_PRICE_DESC.toString(), op_desc);
            rs.updateDouble(DEVICE_PRICE.DEVICE_PRICE.toString(), op_price);
            rs.updateString(DEVICE_PRICE.DEVICE_PRICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
            rs.updateRow();
            conn.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public void modifyDevicePriceCPU(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        modifyDevicePriceByName(session, DEVICE_PRICE_CPU, op_desc, op_price);
    }
    
	public void modifyDevicePriceMemory(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
	    modifyDevicePriceByName(session, DEVICE_PRICE_MEMORY, op_desc, op_price);
    }
    
    public void modifyDevicePriceDisk(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        modifyDevicePriceByName(session, DEVICE_PRICE_DISK, op_desc, op_price);
    }
    
    public void modifyDevicePriceBandwidth(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        modifyDevicePriceByName(session, DEVICE_PRICE_BANDWIDTH, op_desc, op_price);
    }
    
}

class DeviceOthersPriceDBProcWrapper {
    
	private static final Logger log = Logger.getLogger(DeviceOthersPriceDBProcWrapper.class.getName());
	
	public static ResultSet lookupOthersPriceByName(Connection conn, boolean updatable, String name) throws Exception {
	    DBTableDevicePrice DEVICE_PRICE = DBTable.DEVICE_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(DEVICE_PRICE);
        sb.append(" WHERE ").append(DEVICE_PRICE.DEVICE_PRICE_NAME).append(" = ").appendString(name);
        return DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
    }
	
	public static void createOthersPrice(Connection conn, String name, String desc, double price) throws Exception {
	    DBTableDevicePrice DEVICE_PRICE = DBTable.DEVICE_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("INSERT INTO ").append(DEVICE_PRICE).append(" ("); {
            sb.append(DEVICE_PRICE.DEVICE_PRICE_NAME).append(", ");
            sb.append(DEVICE_PRICE.DEVICE_PRICE_DESC).append(", ");
            sb.append(DEVICE_PRICE.DEVICE_PRICE).append(", ");
            sb.append(DEVICE_PRICE.DEVICE_PRICE_MODIFIEDTIME);
        }
        sb.append(") VALUES ("); {
            sb.appendString(name).append(", ");
            sb.appendString(desc).append(", ");
            sb.append(price).append(", ");
            sb.appendDate();
        }
        sb.append(")");
        Statement stat = conn.createStatement();
        stat.executeUpdate(sb.toSql(log));
    }

}
