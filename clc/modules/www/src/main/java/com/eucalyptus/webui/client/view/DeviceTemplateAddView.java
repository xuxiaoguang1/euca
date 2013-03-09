package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateAddView extends IsWidget {
    
    public void setPresenter(Presenter presenter);
    
    public void popup();
    
    public interface Presenter {
        
        public boolean onOK(String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw);
        
    }
    
}
