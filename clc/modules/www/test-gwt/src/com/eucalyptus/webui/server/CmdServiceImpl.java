package com.eucalyptus.webui.server;

import com.eucalyptus.webui.client.service.CmdService;
import com.eucalyptus.webui.client.session.Session;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CmdServiceImpl extends RemoteServiceServlet implements CmdService {
	static final String EC2_ACCESS_KEY="5VPWK0CGBEORB4ITOOMLL";
	static final String EC2_SECRET_KEY="xHj6hTmtKgGzCEIOAtOc6iUCkuyFBXBQhWOdiSZU";
	static final String EC2_URL="http://192.168.0.50:8773/services/Eucalyptus";
	static final String SSH_HOST="root@166.111.134.30";
	//FIXME !! howto get certs? 
	static final String EC2_CERT="/home/eucalyptus/admin/euca2-admin-bf8e80b9-cert.pem";
	static final String EC2_PRIVATE_KEY="/home/eucalyptus/admin/euca2-admin-bf8e80b9-pk.pem";
	static final String EC2_USER_ID="491317658036";
	static final String EUCALYPTUS_CERT="/home/eucalyptus/admin/cloud-cert.pem";
	static final String AWS_CREDENTIAL_FILE="/home/eucalyptus/admin/iamrc";
	static final String IMAGE_PATH = "/home/images/";
  @Override
  public String run(Session session, String[] cmd) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public String sshRun(Session session, String[] cmd) {
    // TODO Auto-generated method stub
    return null;
  }
	

}
