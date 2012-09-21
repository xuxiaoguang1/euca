package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.ViewSearchTableClientConfig;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.KeyView;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.config.EnumService;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class KeyActivity extends AbstractSearchActivity implements KeyView.Presenter, ConfirmationView.Presenter {
  
  public static final String[] TITLE = {"ACCESS KEYS", "访问密钥"};
  
  public static final String[] DELETE_KEY_CAPTION = {"Delete selected access key", "删除所选密钥"};
  public static final String[] DELETE_KEY_SUBJECT = {"Are you sure you want to delete the following selected access key?", "确定要删除选择的密钥？"};
  
  public static final String[] ACTIVATE_KEY_CAPTION = {"Activate selected access key", "激活所选密钥"};
  public static final String[] ACTIVATE_KEY_SUBJECT = {"Are you sure you want to activate the following selected access key?", "确定要激活选择的密钥？"};
  
  public static final String[] PAUSE_KEY_CAPTION = {"Pause selected access key", "暂停所选密钥"};
  public static final String[] PAUSE_KEY_SUBJECT = {"Are you sure you want to pause the following selected access key?", "确定要暂停选择的密钥？"};
  
  private static final String[] FOOTER_VIEW_KEY_SELECTION = {"Select at least one access key to delete", "至少选择一个删除密钥"};
  private static final String[] FOOTER_VIEW_FAILED_TO_DELETE_KEYS = {"Failed to delete keys", "删除密钥失败"};
  private static final String[] FOOTER_VIEW_KEYS_DELETED = {"Keys deleted", "密钥删除成功"};
  private static final String[] FOOTER_VIEW_FAILED_TO_MODIFY_KEYS = {"Failed to modify keys", "修改密钥失败"};
  private static final String[] FOOTER_VIEW_KEYS_MODIFIED = {"Keys modified", "密钥修改成功"};
  
  private static final Logger LOG = Logger.getLogger( KeyActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  public KeyActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendService( ).lookupKey( this.clientFactory.getLocalSession( ).getSession( ), search, range,
                                                           new AsyncCallback<SearchResult>( ) {
      
      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        LOG.log( Level.WARNING, "Search failed: " + caught );
        displayData( null );
      }
      
      @Override
      public void onSuccess( SearchResult result ) {
        LOG.log( Level.INFO, "Search success:" + result );
        displayData( result );
      }
      
    } );
  }
  
  @Override
  public void onSelectionChange( Set<SearchResultRow> selection ) {
    this.currentSelected = selection;
  }

  @Override
  protected String getTitle( ) {
    return TITLE[1];
  }
  
  @Override
  public int getPageSize() {
	  return ViewSearchTableClientConfig.instance().getPageSize(EnumService.KEY_SRV);
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getKeyView( );
      ( ( KeyView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( KeyView ) this.view ).clear( );
    }
    ( ( KeyView ) this.view ).showSearchResult( result );    
  }

  @Override
  public void confirm( String subject ) {
    if ( DELETE_KEY_SUBJECT[1].equals( subject ) ) {
      doDeleteKey( );
    }
    else if ( ACTIVATE_KEY_SUBJECT[1].equals( subject ) ) {
    	doModifyKey(true);
    }
    else if ( PAUSE_KEY_SUBJECT[1].equals( subject ) ) {
    	doModifyKey(false);
    }
  }

  private void doDeleteKey( ) {
    if ( currentSelected == null) {
      return;
    }
    
    final ArrayList<String> ids = Lists.newArrayList( ); 
    for ( SearchResultRow row : currentSelected ) {
      ids.add( row.getField( 0 ) );
    }
    
    clientFactory.getBackendService( ).deleteAccessKey( clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTER_VIEW_FAILED_TO_DELETE_KEYS[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTER_VIEW_FAILED_TO_DELETE_KEYS[1] + ids + ": " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTER_VIEW_KEYS_DELETED[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Key " + ids + " deleted" );
        reloadCurrentRange( );
      }
      
    } );
  }
  
  private void doModifyKey(boolean active) {
	    if ( currentSelected == null) {
	      return;
	    }
	    
	    final ArrayList<String> ids = Lists.newArrayList( ); 
	    for ( SearchResultRow row : currentSelected ) {
	      ids.add( row.getField( 0 ) );
	    }
	    
	    clientFactory.getBackendService( ).modifyAccessKey(clientFactory.getLocalSession( ).getSession( ), ids, active, new AsyncCallback<Void>( ) {

	    	@Override
	        public void onFailure( Throwable caught ) {
	          ActivityUtil.logoutForInvalidSession( clientFactory, caught );
	          clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTER_VIEW_FAILED_TO_MODIFY_KEYS[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	          clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTER_VIEW_FAILED_TO_MODIFY_KEYS[1] + ids  + ": " + caught.getMessage( ) );
	        }

	        @Override
	        public void onSuccess( Void arg0 ) {
	          clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTER_VIEW_KEYS_MODIFIED[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	          clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, FOOTER_VIEW_KEYS_MODIFIED[1] + ids );
	          //clientFactory.getShellView( ).getDetailView( ).disableSave( );
	          reloadCurrentRange( );
	        }	      
	    } );
  }

  @Override
  public void onDeleteKey( ) {
    if (!selectionIsValid())
    	return;
    
    ConfirmationView dialog = this.clientFactory.getConfirmationView( );
    dialog.setPresenter( this );
    dialog.display( DELETE_KEY_CAPTION[1], DELETE_KEY_SUBJECT[1]);
  }

  @Override
  public void onActivateKey() {
	// TODO Auto-generated method stub
	  if (!selectionIsValid())
	  	return;
	  
	  ConfirmationView dialog = this.clientFactory.getConfirmationView( );
	  dialog.setPresenter( this );
	  dialog.display( ACTIVATE_KEY_CAPTION[1], ACTIVATE_KEY_SUBJECT[1]);
  }

  @Override
  public void onPauseKey() {
	// TODO Auto-generated method stub
	  if (!selectionIsValid())
	  	return;
	  
	  ConfirmationView dialog = this.clientFactory.getConfirmationView( );
	  dialog.setPresenter( this );
	  dialog.display( PAUSE_KEY_CAPTION[1], PAUSE_KEY_SUBJECT[1]);
  }
  
  private boolean selectionIsValid() {
	  if ( currentSelected == null || currentSelected.size( ) < 1 ) {
		  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTER_VIEW_KEY_SELECTION[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		  return false;
	  }
	  
	  return true;
  }

@Override
public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
	// TODO Auto-generated method stub
}
}
