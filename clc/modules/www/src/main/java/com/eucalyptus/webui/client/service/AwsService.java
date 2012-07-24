package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.session.Session;
import com.amazonaws.services.ec2.AmazonEC2;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("awsBackend")
public interface AwsService extends RemoteService {
	public SearchResult lookupInstance(Session session, String search, SearchRange range);
	public ArrayList<String> stopInstances(Session session, List<String> ids);
	public ArrayList<String> startInstances(Session session, List<String> ids);
	public ArrayList<String> terminateInstances(Session session, List<String> ids);
	public SearchResult lookupImage(Session session, String search, SearchRange range);

}
