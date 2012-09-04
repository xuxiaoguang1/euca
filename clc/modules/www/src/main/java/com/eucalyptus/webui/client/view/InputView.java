package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import com.google.gwt.user.client.ui.IsWidget;

public interface InputView extends IsWidget {
  
  void display( String caption, String subject, ArrayList<InputField> fields );
  
  void setState(int id, String text, boolean enabled);
  
  void setPresenter( Presenter presenter );
  
  public interface Presenter {

    void process( String subject, ArrayList<String> values );

    void cancel(String subject );
    
  }
  
}
