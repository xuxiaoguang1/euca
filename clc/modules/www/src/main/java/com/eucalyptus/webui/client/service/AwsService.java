package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.session.Session;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("awsBackend")
public interface AwsService extends RemoteService {
	public SearchResult lookupInstance(Session session, String search, SearchRange range);
	public SearchResult lookupImage(Session session, String search, SearchRange range);
	public SearchResult lookupKeypair(Session session, String search, SearchRange range);
	
	public ArrayList<String> stopInstances(Session session, List<String> ids);
	public ArrayList<String> startInstances(Session session, List<String> ids);
	public ArrayList<String> terminateInstances(Session session, List<String> ids);
		
	public String runInstance(Session session, String image, String key);
	public String addKeypair(Session session, String name);
	public void importKeypair(Session session, String name, String key);
	public void deleteKeypairs(Session session, List<String> keys);
	
	public String createSecurityGroup(Session session, String name, String desc);
	public void deleteSecurityGroups(Session session, List<String> names);
	public SearchResult lookupSecurityGroup(Session session, String search, SearchRange range);
	public SearchResult lookupSecurityRule(Session session, String search, SearchRange range);
	public void addSecurityRule(Session session, String group, String fromPort, String toPort, String proto, String ipRange);
	public void delSecurityRules(Session session, List<String> groups, List<String> fromPorts, List<String> toPorts, List<String> protos, List<String> ipRanges);
	

}
