package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.aws.ImageType;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CmdServiceAsync {

	void run(Session session, String[] cmd, AsyncCallback<String> callback);

	void sshRun(Session session, String[] cmd, AsyncCallback<String> callback);

	void lookupNodeCtrl(Session session, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

	void lookupWalrusCtrl(Session session, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

	void lookupStorageCtrl(Session session, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

	void lookupClusterCtrl(Session session, String search, SearchRange range,
			AsyncCallback<SearchResult> callback);

  void uploadImage(Session session, String file, ImageType type, String bucket,
      String name, String kernel, String ramdisk, AsyncCallback<String> callback);

  void runInstance(Session session, String image, String keypair,
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

}
