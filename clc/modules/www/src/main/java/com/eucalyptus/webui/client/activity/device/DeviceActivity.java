package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.activity.AbstractSearchActivity;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.google.gwt.user.client.Window;

abstract public class DeviceActivity extends AbstractSearchActivity {
    
    protected DeviceActivity(SearchPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }
    
    @Override
    final public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
        /* do nothing */
    }

    protected EucalyptusServiceAsync getBackendService() {
        return clientFactory.getBackendService();
    }

    protected FooterView getFooterView() {
        return clientFactory.getShellView().getFooterView();
    }

    protected LogView getLogView() {
        return clientFactory.getShellView().getLogView();
    }

    protected Session getSession() {
        return clientFactory.getLocalSession().getSession();
    }
    
    protected void showStatus(ClientMessage msg) {
        getFooterView().showStatus(StatusType.NONE, msg.toString(), FooterView.CLEAR_DELAY_SECOND * 3);
        getLogView().log(LogType.INFO, msg.toString());
    }
    
    protected void onFrontendServiceFailure(Throwable caught) {
        Window.alert(new ClientMessage("Runtime Error in Frontend", "前端服务运行错误").toString());
        getLogView().log(LogType.ERROR, caught.toString());
    }
    
    protected void onBackendServiceFailure(Throwable caught) {
        if (caught instanceof EucalyptusServiceException) {
            EucalyptusServiceException exception = (EucalyptusServiceException)caught;
            ClientMessage msg = exception.getFrontendMessage();
            if (msg == null) {
                msg = new ClientMessage("Runtime Error in Backend", "后代服务运行错误");
            }
            Window.alert(msg.toString());
            getLogView().log(LogType.ERROR, msg.toString() + " : " + caught.toString());
        }
        else {
            getLogView().log(LogType.ERROR, caught.toString());
        }
    }
    
    protected void onBackendServiceFinished() {
        showStatus(new ClientMessage("Operation Finished Successfully.", "操作完成"));
    }
    
    protected void onBackendServiceFinished(ClientMessage msg) {
        showStatus(msg);
    }
    
}
