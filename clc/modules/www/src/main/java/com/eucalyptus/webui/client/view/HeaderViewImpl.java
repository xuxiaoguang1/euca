package com.eucalyptus.webui.client.view;

import java.util.logging.Logger;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.event.dom.client.ClickEvent;

public class HeaderViewImpl extends Composite implements HeaderView {
  
  private static Logger LOG = Logger.getLogger( HeaderViewImpl.class.getName( ) );
  
  private static HeaderViewImplUiBinder uiBinder = GWT.create( HeaderViewImplUiBinder.class );
  interface HeaderViewImplUiBinder extends UiBinder<Widget, HeaderViewImpl> {}
  
  @UiField
  SearchBox searchBox;
  @UiField Label userLable;
  @UiField Label userNameLable;
  
  private UserSettingViewImpl settingPopup;
  
  private Presenter presenter;
    
  public HeaderViewImpl( ) {
    initWidget( uiBinder.createAndBindUi( this ) );
    settingPopup = new UserSettingViewImpl( );
    settingPopup.setAutoHideEnabled( true );
  }
	@UiHandler("modifyIndividualInfo")
	void onModifyIndividualInfoClick(ClickEvent event) {
		this.presenter.modifyIndividualInfo();
	}
	@UiHandler("logoutLink")
	void onLogoutLinkClick(ClickEvent event) {
		this.presenter.logout();
	}
	
	@UiHandler( "searchBox" )
	void handleSearchBoxKeyPressed( KeyPressEvent e ) {
		if ( KeyCodes.KEY_ENTER == e.getNativeEvent( ).getKeyCode( ) ) {
			presenter.runManualSearch( searchBox.getInput( ) );
		}
	}
  
	@Override
	public void setUser( String user ) {
		this.userNameLable.setText(user);
	}

	@Override
	public UserSettingView getUserSetting( ) {
		return this.settingPopup;
	}

	@Override
	public void setPresenter( Presenter presenter ) {
		this.presenter = presenter;
	}
}
