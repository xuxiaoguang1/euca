package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.LanguageSelection;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.UserAppAddView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.user.UserApp;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserAppAddActivity extends AbstractSearchActivity implements UserAppAddView.Presenter {

	public static final String[] TITLE = {"User application adding acitity", "用户申请"};

	private final String[] FOOTERVIEW_FAILED_TO_QUERY_TEMPLATES = {"Failed to query templates", "查询模板库失败"};

  
	public UserAppAddActivity( SearchPlace place, ClientFactory clientFactory ) {
		super( place, clientFactory );
	}

  @Override
  protected void doSearch( String query, SearchRange range ) {
	  this.clientFactory.getBackendService().lookupDeviceTemplate(clientFactory.getLocalSession().getSession(), search, range, null, null, new AsyncCallback<SearchResult>() {

	        @Override
	        public void onFailure(Throwable caught) {
		        // TODO Auto-generated method stub
	        	int lan = LanguageSelection.instance().getCurLanguage().ordinal();
	        	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_QUERY_TEMPLATES[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_QUERY_TEMPLATES[lan] + ":" + caught.getMessage( ) );
	        }

	        @Override
	        public void onSuccess(SearchResult result) {
		        // TODO Auto-generated method stub;
	        	clientFactory.getUserAppAddView().showSearchResult(result);
	        }
	  	});
  }
  
  @Override
  protected String getTitle( ) {
	  int lan = LanguageSelection.instance().getCurLanguage().ordinal();
	  return TITLE[lan];
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getUserAppAddView();
      ((UserAppAddView)this.view).clear( );
    }
    
    ((UserAppAddView)this.view).showSearchResult( result );
  }

	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSelectionChange(Set<SearchResultRow> selections) {
		// TODO Auto-generated method stub
		
	}
	

	  @Override
	  public void doCreateUserApp(UserApp userApp) {
		  // TODO Auto-generated method stub
		  int userId = this.clientFactory.getSessionData().getLoginUser().getUserId();
		  userApp.setUserId(userId);
		  
		  final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		  this.clientFactory.getBackendService().addUserApp(clientFactory.getLocalSession().getSession(), 
				  											userApp,
				  											new AsyncCallback<Void>() {

		        @Override
		        public void onFailure(Throwable caught) {
			        // TODO Auto-generated method stub
		        	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_ADD_USERAPP[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_ADD_USERAPP[lan] + ":" + caught.getMessage( ) );
		        }
				@Override
				public void onSuccess(Void result) {
					// TODO Auto-generated method stub
					clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_ADD_USERAPP[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_ADD_USERAPP[lan]);
		    		
		    		reloadCurrentRange();
		    		//updateUserAppCountInfo();
				}
	      });
	  }
	  
	  private final String[] FOOTERVIEW_FAILED_TO_ADD_USERAPP = {"Failed to add user app", "增加申请失败"};
	  private final String[] FOOTERVIEW_ADD_USERAPP = {"Successfully add user apps", "增加申请成功"};
}