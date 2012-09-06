package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.session.Session;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AwsServiceAsync {

	void lookupInstance(Session session, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

	void startInstances(Session session, List<String> ids,
			AsyncCallback<ArrayList<String>> callback);

	void stopInstances(Session session, List<String> ids,
			AsyncCallback<ArrayList<String>> callback);

	void terminateInstances(Session session, List<String> ids,
			AsyncCallback<ArrayList<String>> callback);

	void lookupImage(Session session, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

  void runInstance(Session session, String image, String key,
      AsyncCallback<String> callback);

  void lookupKeypair(Session session, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void addKeypair(Session session, String name, AsyncCallback<String> callback);

  void importKeypair(Session session, String name, String key,
      AsyncCallback<Void> callback);

  void deleteKeypairs(Session session, List<String> keys,
      AsyncCallback<Void> callback);

  void createSecurityGroup(Session session, String name, String desc,
      AsyncCallback<String> callback);

  void lookupSecurityGroup(Session session, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void deleteSecurityGroups(Session session, List<String> names,
      AsyncCallback<Void> callback);

  void lookupSecurityRule(Session session, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void addSecurityRule(Session session, String group, String fromPort,
      String toPort, String proto, String ipRange, AsyncCallback<Void> callback);

  void delSecurityRules(Session session, List<String> groups,
      List<String> fromPorts, List<String> toPorts, List<String> protos,
      List<String> ipRanges, AsyncCallback<Void> callback);


}
