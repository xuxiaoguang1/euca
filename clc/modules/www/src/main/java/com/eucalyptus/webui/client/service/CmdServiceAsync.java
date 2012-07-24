package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;

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

}
