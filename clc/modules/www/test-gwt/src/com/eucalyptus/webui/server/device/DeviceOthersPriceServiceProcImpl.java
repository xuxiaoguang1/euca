package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class DeviceOthersPriceServiceProcImpl {
	
	private DeviceOthersPriceDBProcWrapper dbproc = new DeviceOthersPriceDBProcWrapper();
	
	private LoginUserProfile getUser(Session session) {
		return LoginUserProfileStorer.instance().get(session.getId());
	}
	
	private static final String OTHERS_PRICE_NAME_MEMORY = "memory";
	private static final String OTHERS_PRICE_DISK = "disk";
	private static final String OTHERS_PRICE_BANDWIDTH = "bandwidth";
	
	private synchronized SearchResultRow lookupOthersPriceByName(Session session, String price_name) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
	    ResultSetWrapper rsw = null;
	    try {
    	    rsw = dbproc.lookupOthersPriceByName(price_name);
    	    ResultSet rs = rsw.getResultSet();
    	    rs.next();
    	    DBTableOthersPrice OTHERS_PRICE = DBTable.OTHERS_PRICE;
    	    double others_price = DBData.getDouble(rs, OTHERS_PRICE.OTHERS_PRICE);
            String others_price_desc = DBData.getString(rs, OTHERS_PRICE.OTHERS_PRICE_DESC);
    	    Date others_price_modifedtime = DBData.getDate(rs, OTHERS_PRICE.OTHERS_PRICE_MODIFIEDTIME);
    	    return new SearchResultRow(Arrays.asList(DBData.format(others_price), others_price_desc, DBData.format(others_price_modifedtime)));
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        throw new EucalyptusServiceException(new ClientMessage("", "获取定价失败"));
	    }
        finally {
            if (rsw != null) {
                try {
                    rsw.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	public synchronized SearchResultRow lookupOthersPriceMemory(Session session) throws EucalyptusServiceException {
	    return lookupOthersPriceByName(session, OTHERS_PRICE_NAME_MEMORY);
	}
	
	public synchronized SearchResultRow lookupOthersPriceDisk(Session session) throws EucalyptusServiceException {
	    return lookupOthersPriceByName(session, OTHERS_PRICE_DISK);
	}
	
	public synchronized SearchResultRow lookupOthersPriceBandwidth(Session session) throws EucalyptusServiceException {
        return lookupOthersPriceByName(session, OTHERS_PRICE_BANDWIDTH);
    }
	
	private synchronized void modifyOthersPriceByName(Session session, String others_price_name, String others_price_desc, double others_price) throws EucalyptusServiceException {
	    if (!getUser(session).isSystemAdmin()) {
            throw new EucalyptusServiceException(new ClientMessage("", "权限不足 操作无效"));
        }
	    if (others_price_desc == null) {
	        others_price_desc = "";
	    }
	    try {
	        dbproc.modifyOthersPriceByName(others_price_name, others_price_desc, others_price);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        throw new EucalyptusServiceException(new ClientMessage("", "更新定价失败"));
	    }
	}
    
	public synchronized void modifyOthersPriceMemory(Session session, String others_price_desc, double others_price) throws EucalyptusServiceException {
	    modifyOthersPriceByName(session, OTHERS_PRICE_NAME_MEMORY, others_price_desc, others_price);
    }
    
    public synchronized void modifyOthersPriceDisk(Session session, String others_price_desc, double others_price) throws EucalyptusServiceException {
        modifyOthersPriceByName(session, OTHERS_PRICE_DISK, others_price_desc, others_price);
    }
    
    public synchronized void modifyOthersPriceBandwidth(Session session, String others_price_desc, double others_price) throws EucalyptusServiceException {
        modifyOthersPriceByName(session, OTHERS_PRICE_BANDWIDTH, others_price_desc, others_price);
    }
    
}

class DeviceOthersPriceDBProcWrapper {
	
	private static final Logger LOG = Logger.getLogger(DeviceOthersPriceDBProcWrapper.class.getName());
	
	private DBProcWrapper wrapper = DBProcWrapper.Instance();
		
	private ResultSetWrapper doQuery(String request) throws Exception {
		LOG.info(request);
		return wrapper.query(request);
	}
	
	private void doUpdate(String request) throws Exception {
		LOG.info(request);
		wrapper.update(request);
	}
	
	public ResultSetWrapper lookupOthersPriceByName(String others_price_name) throws Exception {
	    DBTableOthersPrice OTHERS_PRICE = DBTable.OTHERS_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("SELECT * FROM ").append(OTHERS_PRICE).append(" WHERE ");
	    sb.append(OTHERS_PRICE.OTHERS_PRICE_NAME).append(" = ").appendString(others_price_name);
	    return doQuery(sb.toString());
	}
	
	public void modifyOthersPriceByName(String others_price_name, String others_price_desc, double others_price) throws Exception {
	    DBTableOthersPrice OTHERS_PRICE = DBTable.OTHERS_PRICE;
	    DBStringBuilder sb = new DBStringBuilder();
	    sb.append("UPDATE ").append(OTHERS_PRICE).append(" SET ");
	    sb.append(OTHERS_PRICE.OTHERS_PRICE).append(" = ").append(others_price).append(", ");
	    sb.append(OTHERS_PRICE.OTHERS_PRICE_DESC).append(" = ").appendString(others_price_desc).append(", ");
	    sb.append(OTHERS_PRICE.OTHERS_PRICE_MODIFIEDTIME).append(" = ").appendDate(new Date());
	    sb.append(" WHERE ").append(OTHERS_PRICE.OTHERS_PRICE_NAME).append(" = ").appendString(others_price_name);
	    doUpdate(sb.toString());
	}

}
