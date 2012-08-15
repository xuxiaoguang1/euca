package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.AreaView;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.DetailView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.ImageView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.KeypairView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.InputField.ValueType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class KeypairActivity extends AbstractSearchActivity implements KeypairView.Presenter, DetailView.Presenter, InputView.Presenter, ConfirmationView.Presenter, AreaView.Presenter {
  
  public static final String TITLE = "Keypair管理";
  
  public static final String[] ADD_KEYPAIR_CAPTION = {"Add a new keypair", "增加一个 Keypair"};
  public static final String[] ADD_KEYPAIR_SUBJECT = {"Enter information to add a new keypair:", "输入新 Keypair 的名称:"};
  
  public static final String[] DELETE_KEYPAIR_CAPTION = {"Delete selected keypair", "删除所选 Keypair"};
  public static final String[] DELETE_KEYPAIR_SUBJECT = {"Are you sure you want to delete following selected keypair?", "确定要删除所选 KeyPair?"};
  
  public static final String[] KEYPAIR_NAME_INPUT_TITLE = {"Keypair name", "Keypair 名称"};
  private String[] KEYPAIR_ACTIVITY_NO_SELECTION = {"Select at least one keypair", "至少选择一个 Keypair"};
  
  private static final Logger LOG = Logger.getLogger( KeypairActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  private String privateKey;
  
  public KeypairActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendAwsService().lookupKeypair(this.clientFactory.getLocalSession( ).getSession( ), search, range,
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
    // Nothing to do for now.
  }

  @Override
  protected String getTitle( ) {
    return TITLE;
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getKeypairView( );
      ( ( KeypairView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( KeypairView ) this.view ).clear( );
    }
    ( (KeypairView ) this.view ).showSearchResult( result );    
  }

  @Override
  public void onAddKeypair() {
    InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( ADD_KEYPAIR_CAPTION[1], ADD_KEYPAIR_SUBJECT[1], new ArrayList<InputField>( Arrays.asList( new InputField( ) {
      @Override
      public String getTitle( ) {
        return KEYPAIR_NAME_INPUT_TITLE[1];
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return null;
      }
    })));
  }

  @Override
  public void onDelKeypair() {
    if (!selectionIsValid())
      return;
  
    ConfirmationView dialog = this.clientFactory.getConfirmationView( );
    dialog.setPresenter( this );
    dialog.display( DELETE_KEYPAIR_CAPTION[1], DELETE_KEYPAIR_SUBJECT[1], currentSelected, new ArrayList<Integer>( Arrays.asList( 0, 1 ) ) );
    
  }

  @Override
  public void onImportKeypair() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void process(String subject, ArrayList<String> values) {
    if (ADD_KEYPAIR_SUBJECT[1].equals(subject)) {
      doAddKeypair(values.get(0));
    }
    
  }

  private void doAddKeypair(final String name) {
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "添加Keypair " + name + "中 ...", 0 );
    this.clientFactory.getBackendAwsService().addKeypair(this.clientFactory.getLocalSession( ).getSession( ), name, 
        new AsyncCallback<String>( ) {

          @Override
          public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub
            
          }

          @Override
          public void onSuccess(String result) {
            privateKey = result;
            clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Keypair " + name + " 创建成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
            reloadCurrentRange( );
            AreaView d = clientFactory.createAreaView();
            d.setPresenter(KeypairActivity.this);
            d.display();
          }
    });
  }
  
  private boolean selectionIsValid() {
    if ( currentSelected == null || currentSelected.size( ) < 1 ) {
      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, KEYPAIR_ACTIVITY_NO_SELECTION[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
      return false;
    }
    
    return true;
  }

  @Override
  public void confirm(String subject) {
    if ( DELETE_KEYPAIR_SUBJECT[1].equals( subject ) ) {
      doDeleteKeypairs( );
    }
  }

  private void doDeleteKeypairs() {
    if ( currentSelected == null || currentSelected.size( ) < 1 ) {
      return;
    }
    
    final ArrayList<String> ids = Lists.newArrayList( ); 
    for ( SearchResultRow row : currentSelected ) {
      ids.add( row.getField( 0 ) );
    }
    
    clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "删除Keypair中...", 0 );
    clientFactory.getBackendAwsService().deleteKeypairs(clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        //clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to delete accounts", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        //clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to delete accounts " + ids + ": " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Keypair 已删除", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        //clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Accounts " + ids + " deleted" );
        reloadCurrentRange( );
      }
    } );
    
  }

  @Override
  public String getText() {
    return privateKey;
  }
  
  
}
