package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.PolicyView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.InputField.ValueType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PolicyActivity extends AbstractSearchActivity implements PolicyView.Presenter, ConfirmationView.Presenter, InputView.Presenter {
  
  public static final String[] TITLE = {"ACCESS POLICIES", "策略"};
  
  public static final String[] DELETE_POLICY_CAPTION = {"Delete selected policy", "删除所选的策略"};
  public static final String[] DELETE_POLICY_SUBJECT = {"Are you sure you want to delete the following selected policy?", "确定要删除所选的策略？"};
  
  public static final String[] MODIFY_POLICY_CAPTION = {"Modify selected policy", "修改策略"};
  public static final String[] MODIFY_POLICY_SUBJECT = {"Modify selected policy...", "确定要修改所选的策略？"};
  
  public static final String[] POLICY_NAME_INPUT_TITLE = {"", "名称"};
  
  public static final String[] POLICY_CONTENT_INPUT_TITLE = {"", "策略内容"};
  
  private static final Logger LOG = Logger.getLogger( PolicyActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  public PolicyActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

	@Override
	protected void doSearch(String query, SearchRange range) {
		this.clientFactory.getBackendService().lookupPolicy(
				this.clientFactory.getLocalSession().getSession(), search,range, new AsyncCallback<SearchResult>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,caught);
						LOG.log(Level.WARNING, "Search failed: " + caught);
						displayData(null);
					}

					@Override
					public void onSuccess(SearchResult result) {
						LOG.log(Level.INFO, "Search success:" + result);
						displayData(result);
					}

				});
	}
  
  @Override
  public void onSelectionChange( Set<SearchResultRow> selection ) {
    this.currentSelected = selection;
    if ( selection == null || selection.size( ) != 1 ) {
      LOG.log( Level.INFO, "Not a single selection" );      
      this.clientFactory.getShellView( ).hideDetail( );
    } else {
      LOG.log( Level.INFO, "Selection changed to " + selection );
      this.clientFactory.getShellView( ).showDetail( DETAIL_PANE_SIZE );
      showSingleSelectedDetails( selection.toArray( new SearchResultRow[0] )[0] );
    }
  }

  @Override
  public void saveValue( ArrayList<String> keys, ArrayList<HasValueWidget> values ) {
    // Nothing will happen here.
  }

  @Override
  protected String getTitle( ) {
    return TITLE[1];
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getPolicyView( );
      ( ( PolicyView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( PolicyView ) this.view ).clear( );
    }
    ( ( PolicyView ) this.view ).showSearchResult( result );    
  }

  @Override
  public void onDeletePolicy( ) {
    if ( currentSelected == null || currentSelected.size( ) == 0 ) {
      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Select at least one policy to delete", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
      return;
    }
    ConfirmationView dialog = this.clientFactory.getConfirmationView( );
    dialog.setPresenter( this );
    dialog.display( DELETE_POLICY_CAPTION[1], DELETE_POLICY_SUBJECT[1]);
  }

  @Override
  public void confirm( String subject ) {
    if ( DELETE_POLICY_SUBJECT[1].equals( subject ) ) {
      doDeletePolicy( );
    }
  }

  private void doDeletePolicy( ) {
    if ( currentSelected == null || currentSelected.size( ) == 0 ) {
      return;
    }
    
    final ArrayList<String> ids = Lists.newArrayList();
	for (SearchResultRow row : currentSelected) {
		ids.add(row.getField(0));
	}    

    clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Deleting policy ...", 0 );
    
    clientFactory.getBackendService( ).deletePolicy( clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to delete policy", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to delete policy "+ ": " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Policy deleted", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Policy deleted" );
        reloadCurrentRange( );
      }
      
    } );
  }
  
	@Override
	public void onModifyPolicy() {
		if ( currentSelected == null || currentSelected.size( ) != 1 ) {
		      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Must select a single policy to add certificate", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		      return;
		    }
		InputView dialog = this.clientFactory.getInputView();
		dialog.setPresenter(this);
		dialog.display(MODIFY_POLICY_CAPTION[1], MODIFY_POLICY_SUBJECT[1],
				new ArrayList<InputField>(Arrays.asList(
					new InputField() {
	
						@Override
						public String getTitle() {
							return POLICY_NAME_INPUT_TITLE[1];
						}
	
						@Override
						public ValueType getType() {
							return ValueType.TEXT;
						}
	
						@Override
						public ValueChecker getChecker() {
							return ValueCheckerFactory.createPolicyNameChecker();
						}
	
					}, 
					new InputField() {
	
						@Override
						public String getTitle() {
							return POLICY_CONTENT_INPUT_TITLE[1];
						}
	
						@Override
						public ValueType getType() {
							return ValueType.TEXTAREA;
						}
	
						@Override
						public ValueChecker getChecker() {
							return ValueCheckerFactory.createNonEmptyValueChecker();
						}

				})));
	}

	@Override
	public void process(String subject, ArrayList<String> values) {
		if(MODIFY_POLICY_SUBJECT[1].equals(subject)){
			doModifyPolicy(values.get(0), values.get(1));
		}
	}
	
	private void doModifyPolicy(String name, String content){
		if (currentSelected == null || currentSelected.size() != 1) {
			return;
		}
		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Modifying policy ...", 0 );
		
		String policyId = Lists.newArrayList(currentSelected).get(0).getField(0);

		clientFactory.getBackendService().modifyPolicy(clientFactory.getLocalSession().getSession(), policyId, name,content, 
				new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,
								caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR,"Failed to modify policy",FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR,"Failed to modify policy " + ": "+ caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE, "Policy modified",FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "Policy modified");
						reloadCurrentRange();
					}

				});
	}
  
}
