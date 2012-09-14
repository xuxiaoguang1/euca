package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.resource.Template;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;

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
  public String runVM(Session session, int userID, Template template, String keypair, String group, String image) throws EucalyptusServiceException {
    //real code about template won't be in old repo
    return aws.runInstance(session, userID, image, keypair, "m1.small", group);
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
  
}
