package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.aws.ImageType;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AwsServiceAsync {

	void lookupInstance(Session session, int userID, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

	void startInstances(Session session, int userID, List<String> ids,
			AsyncCallback<ArrayList<String>> callback);

	void stopInstances(Session session, int userID, List<String> ids,
			AsyncCallback<ArrayList<String>> callback);

	void terminateInstances(Session session, int userID, List<String> ids,
			AsyncCallback<ArrayList<String>> callback);

	void lookupImage(Session session, int userID, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

  void runInstance(Session session, int userID, String image, String key,
      AsyncCallback<String> callback);

  void lookupKeypair(Session session, int userID, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void addKeypair(Session session, int userID, String name, AsyncCallback<String> callback);

  void importKeypair(Session session, int userID, String name, String key,
      AsyncCallback<Void> callback);

  void deleteKeypairs(Session session, int userID, List<String> keys,
      AsyncCallback<Void> callback);

  void createSecurityGroup(Session session, int userID, String name, String desc,
      AsyncCallback<String> callback);

  void lookupSecurityGroup(Session session, int userID, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void deleteSecurityGroups(Session session, int userID, List<String> names,
      AsyncCallback<Void> callback);

  void lookupSecurityRule(Session session, int userID, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void addSecurityRule(Session session, int userID, String group, String fromPort,
      String toPort, String proto, String ipRange, AsyncCallback<Void> callback);

  void delSecurityRules(Session session, int userID, List<String> groups,
      List<String> fromPorts, List<String> toPorts, List<String> protos,
      List<String> ipRanges, AsyncCallback<Void> callback);

  void bindImage(Session session, int userID, String id, String sysName, String sysVer,
      AsyncCallback<Void> callback);

  void unbindImages(Session session, int userID, List<String> ids, AsyncCallback<Void> callback);
  
  void lookupNodeCtrl(Session session, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void lookupWalrusCtrl(Session session, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void lookupStorageCtrl(Session session, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void lookupClusterCtrl(Session session, String search, SearchRange range,
      AsyncCallback<SearchResult> callback);

  void uploadImage(Session session, int userID, String file, ImageType type, String bucket,
      String name, String kernel, String ramdisk, AsyncCallback<String> callback);

  void runInstance(Session session, int userID, String image, String keypair,
      String vmtype, String group, AsyncCallback<String> callback);

  void registerCluster(Session session, String part, String host, String name,
      AsyncCallback<Void> callback);

  void deregisterCluster(Session session, String part, String name,
      AsyncCallback<Void> callback);

  void deregisterNode(Session session, String host, AsyncCallback<Void> callback);

  void registerWalrus(Session session, String host, String name,
      AsyncCallback<Void> callback);

  void registerStorage(Session session, String part, String host, String name,
      AsyncCallback<Void> callback);

  void deregisterWalrus(Session session, String name,
      AsyncCallback<Void> callback);

  void registerNode(Session session, String host, AsyncCallback<Void> callback);

  void deregisterStorage(Session session, String part, String name,
      AsyncCallback<Void> callback);

  void lookupAvailablityZones(Session session,
      AsyncCallback<ArrayList<String>> callback);

}
