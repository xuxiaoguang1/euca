package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.LanguageSelection;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.UserAppAddView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserAppAddActivity extends AbstractSearchActivity {

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
}