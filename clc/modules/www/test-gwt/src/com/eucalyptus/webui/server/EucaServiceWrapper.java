package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.model.Address;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.DBTableName;
import com.eucalyptus.webui.shared.resource.device.IPServiceInfo;
import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;

public class EucaServiceWrapper {

  static private EucaServiceWrapper instance = null;
  private static final Logger LOG = Logger.getLogger(EucaServiceWrapper.class);
  
  private AwsServiceImpl aws = null;
  
  private EucaServiceWrapper() {
    aws = new AwsServiceImpl();
  }
  
  static public EucaServiceWrapper getInstance() {
    if (instance == null)
      instance = new EucaServiceWrapper();
    return instance;
  }

  /**
   * run a new virtual machine with eucalyptus
   * @param session
   * @param template Template.class
   * @param image DB vm_image_type euca_vit_id
   * @param keypair string
   * @param group string
   * @return euca id of vm
   */
  
  public String runVM(Session session, int userID, TemplateInfo template, String keypair, String group, String image) throws EucalyptusServiceException {
    //real code about template won't be in old repo
    return aws.runInstance(session, userID, image, keypair, "c1.xlarge", group);
  }
  
  
  /**
   * get all keypairs' name owned by user
   * @param session
   * @return
   */
  public List<String> getKeypairs(Session session, int userID) throws EucalyptusServiceException {
    List<SearchResultRow> data = aws.lookupKeypair(session, userID);
    List<String> ret = new ArrayList<String>();
    for (SearchResultRow d: data) {
      ret.add(d.getField(0));
    }
    return ret;
  }
  
  /**
   * get all security groups' name can be used by user
   * @param session
   * @return
   */
  public List<String> getSecurityGroups(Session session, int userID) throws EucalyptusServiceException {
    List<SearchResultRow> data = aws.lookupSecurityGroup(session, userID);
    List<String> ret = new ArrayList<String>();
    for (SearchResultRow d: data) {
      ret.add(d.getField(0));
    }
    return ret;
  }
  
  public List<String> getAvailabilityZones(Session session) throws EucalyptusServiceException {
    //TODO: unused filter 
    return aws.lookupAvailablityZones(session);
  }
  
  public void bindServerWithZone(int serverID, String zone) {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ").append(DBTableName.SERVER)
      .append(" SET ").append(DBTableColName.SERVER.EUCA_ZONE)
      .append(" = '").append(zone).append("' WHERE ")
      .append(DBTableColName.SERVER.ID)
      .append(" = '").append(serverID).append("'");
    try {
      DBProcWrapper.Instance().update(sb.toString());
    } catch (SQLException e) {
      //TODO
    }
  }
  
  public int getServerID(Session session, int userID, String instanceID) throws EucalyptusServiceException {
    String zone = aws.lookupZoneWithInstanceId(session, userID, instanceID);
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ").append(DBTableColName.SERVER.ID)
      .append(" FROM ").append(DBTableName.SERVER)
      .append(" WHERE ").append(DBTableColName.SERVER.EUCA_ZONE)
      .append(" = '").append(zone).append("'");
    try {
      ResultSet r = DBProcWrapper.Instance().query(sb.toString()).getResultSet();
      if (r.first()) {
        return r.getInt(DBTableColName.SERVER.ID);
      }
    } catch (SQLException e) {
      //TODO
    }
    return -1;
  }
  
  public String getServerIp(int userID, String instanceID) throws EucalyptusServiceException {
    return aws.lookupInstanceForIp(userID, instanceID);
  }
    
  public List<IPServiceInfo> getIPServices(int accountID, int userID) throws EucalyptusServiceException {
    List<IPServiceInfo> ret = new ArrayList<IPServiceInfo>();
    List<Address> addrs = aws.lookupPublicAddress(userID);
    for (Address a : addrs) {
      IPServiceInfo e = new IPServiceInfo();
      e.ip_addr = a.getPublicIp();
      String id = a.getInstanceId();
      if (id.startsWith("nobody")) { 
        e.is_state = IPState.STOP;
      } else if (id.startsWith("available")) {
        e.is_state = IPState.RESERVED;
      } else {
        e.is_state = IPState.INUSE;
        IPServiceInfo _e = new IPServiceInfo();
        String _id = id.substring(0, id.indexOf(' '));
        String _ip = getServerIp(userID, _id);
        _e.is_state = IPState.INUSE;
        _e.ip_type = IPType.PRIVATE;
        _e.ip_addr = _ip;
        ret.add(_e);
      }            
      e.ip_type = IPType.PUBLIC;
      ret.add(e);
    }
    return ret;
  }
  
  public List<String> allocateAddress(int userID, IPType type, int count) throws EucalyptusServiceException {
    return aws.allocateAddress(userID, type, count);
  }
  
  public void releaseAddress(int userID, IPType type, String addr) throws EucalyptusServiceException {
    aws.releaseAddress(userID, type, addr);
  }
  
  
  public void acquireResource(int serverID, TemplateInfo t) {
    
  }
  
  public void releaseResource(int serverID, TemplateInfo t) {
    
  }
  
}
