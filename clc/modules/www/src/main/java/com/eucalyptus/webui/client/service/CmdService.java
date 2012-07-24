package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.session.Session;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("cmdBackend")
public interface CmdService extends RemoteService {
	
	public String run(Session session, String[] cmd);
	public String sshRun(Session session, String[] cmd);
	
	public SearchResult lookupNodeCtrl(Session session, String search, SearchRange range);
	public SearchResult lookupWalrusCtrl(Session session, String search, SearchRange range);
	public SearchResult lookupClusterCtrl(Session session, String search, SearchRange range);
	public SearchResult lookupStorageCtrl(Session session, String search, SearchRange range);
	
}
