package com.eucalyptus.webui.client.service;

import com.eucalyptus.webui.client.session.Session;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CmdServiceAsync {

	void run(Session session, String[] cmd, AsyncCallback<String> callback);

	void sshRun(Session session, String[] cmd, AsyncCallback<String> callback);


}
