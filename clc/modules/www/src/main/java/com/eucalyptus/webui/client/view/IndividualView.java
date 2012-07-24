package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.gwt.user.client.ui.IsWidget;

public interface IndividualView extends IsWidget {
    
  void setPresenter( Presenter presenter );
  
  void setLoginUserProfile(LoginUserProfile profile);
  
  void clearPwd();
  
  public interface Presenter {
	void onUpdateInfo(String title, String mobile, String email);
    void onChangePwd(String oldPwd, String newPwd, String newPwdAgain);
  }
}
