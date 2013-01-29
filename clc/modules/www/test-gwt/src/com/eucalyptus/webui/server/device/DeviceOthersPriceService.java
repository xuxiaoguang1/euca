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
import com.eucalyptus.webui.shared.resource.device.OthersPriceInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceOthersPriceService {
    
    private static DeviceOthersPriceService instance = new DeviceOthersPriceService();
    
    public static DeviceOthersPriceService getInstance() {
        return instance;
    }
    
    private DeviceOthersPriceService() {
        /* do nothing */
    }
    
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private static final String OTHERS_PRICE_NAME_MEMORY = "memory";
	private static final String OTHERS_PRICE_DISK = "disk";
	private static final String OTHERS_PRICE_BANDWIDTH = "bandwidth";
	
	private OthersPriceInfo lookupOthersPriceInfoByName(Session session, String op_name) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(ClientMessage.PERMISSION_DENIED);
        }
	    Connection conn = null;
        try {
            conn = DBProcWrapper.getConnection();
            DBTableOthersPrice OTHERS_PRICE = DBTable.OTHERS_PRICE;
            ResultSet rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, false, op_name);
            if (!rs.next()) {
                DeviceOthersPriceDBProcWrapper.createOthersPrice(conn, op_name, "", 0);
                rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, false, op_name);
            }
            String op_desc = DBData.getString(rs, OTHERS_PRICE.OTHERS_PRICE_DESC);
            double op_price = DBData.getDouble(rs, OTHERS_PRICE.OTHERS_PRICE);
            Date op_modifiedtime = DBData.getDate(rs, OTHERS_PRICE.OTHERS_PRICE_MODIFIEDTIME);
            return new OthersPriceInfo(op_name, op_desc, op_price, op_modifiedtime);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EucalyptusServiceException(e);
        }
        finally {
            DBProcWrapper.close(conn);
        }
	}
	
	public OthersPriceInfo lookupOthersPriceMemory(Session session) throws EucalyptusServiceException {
	    return lookupOthersPriceInfoByName(session, OTHERS_PRICE_NAME_MEMORY);
	}
	
	public OthersPriceInfo lookupOthersPriceDisk(Session session) throws EucalyptusServiceException {
	    return lookupOthersPriceInfoByName(session, OTHERS_PRICE_DISK);
	}
	
	public OthersPriceInfo lookupOthersPriceBandwidth(Session session) throws EucalyptusServiceException {
        return lookupOthersPriceInfoByName(session, OTHERS_PRICE_BANDWIDTH);
    }
	
	private void modifyOthersPriceByName(Session session, String op_name, String op_desc, double op_price) throws EucalyptusServiceException {
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
            DBTableOthersPrice OTHERS_PRICE = DBTable.OTHERS_PRICE;
            ResultSet rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, true, op_name);
            if (!rs.next()) {
                DeviceOthersPriceDBProcWrapper.createOthersPrice(conn, op_name, "", 0);
                rs = DeviceOthersPriceDBProcWrapper.lookupOthersPriceByName(conn, true, op_name);
            }
            rs.updateString(OTHERS_PRICE.OTHERS_PRICE_DESC.toString(), op_desc);
            rs.updateDouble(OTHERS_PRICE.OTHERS_PRICE.toString(), op_price);
            rs.updateString(OTHERS_PRICE.OTHERS_PRICE_MODIFIEDTIME.toString(), DBStringBuilder.getDate());
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
    
	public void modifyOthersPriceMemory(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
	    modifyOthersPriceByName(session, OTHERS_PRICE_NAME_MEMORY, op_desc, op_price);
    }
    
    public void modifyOthersPriceDisk(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        modifyOthersPriceByName(session, OTHERS_PRICE_DISK, op_desc, op_price);
    }
    
    public void modifyOthersPriceBandwidth(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        modifyOthersPriceByName(session, OTHERS_PRICE_BANDWIDTH, op_desc, op_price);
    }
    
}

class DeviceOthersPriceDBProcWrapper {
    
	private static final Logger log = Logger.getLogger(DeviceOthersPriceDBProcWrapper.class.getName());
	
	public static ResultSet lookupOthersPriceByName(Connection conn, boolean updatable, String name) throws Exception {
	    DBTableOthersPrice OTHERS_PRICE = DBTable.OTHERS_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
        sb.append("SELECT * FROM ").append(OTHERS_PRICE);
        sb.append(" WHERE ").append(OTHERS_PRICE.OTHERS_PRICE_NAME).append(" = ").appendString(name);
        ResultSet rs = DBProcWrapper.queryResultSet(conn, updatable, sb.toSql(log));
        rs.next();
        return rs;
    }
	
	public static void createOthersPrice(Connection conn, String name, String desc, double price) throws Exception {
	    DBTableOthersPrice OTHERS_PRICE = DBTable.OTHERS_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("INSERT INTO ").append(OTHERS_PRICE).append(" ("); {
            sb.append(OTHERS_PRICE.OTHERS_PRICE_NAME).append(", ");
            sb.append(OTHERS_PRICE.OTHERS_PRICE_DESC).append(", ");
            sb.append(OTHERS_PRICE.OTHERS_PRICE).append(", ");
            sb.append(OTHERS_PRICE.OTHERS_PRICE_MODIFIEDTIME);
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
