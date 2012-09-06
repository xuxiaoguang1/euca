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
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.DetailView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.StorageCtrlView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.InputField.ValueType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StorageCtrlActivity extends AbstractSearchActivity implements StorageCtrlView.Presenter, DetailView.Presenter, InputView.Presenter, ConfirmationView.Presenter {
  
  public static final String TITLE = "存储控制器管理";
  public static final String[] REGISTER_SC_CAPTION = {"Register a new cluster contoller", "注册一个新存储控制器"};
  public static final String[] REGISTER_SC_SUBJECT = {"Enter information to register a new cluster contoller:", "输入新存储控制器的信息:"};
  public static final String[] DEREGISTER_SC_CAPTION = {"Deregister cluster controllers", "注销存储控制器"};
  public static final String[] DEREGISTER_SC_SUBJECT = {"Are you sure to deregister cluster controllers?", "确定要注销选中存储控制器吗？"};
  public static final String[] SC_PART_INPUT_TITLE = {"Partition", "分区"};
  public static final String[] SC_NAME_INPUT_TITLE = {"Name", "名称"};
  public static final String[] SC_HOST_INPUT_TITLE = {"Host", "地址"};
  
  private static final Logger LOG = Logger.getLogger( StorageCtrlActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  public StorageCtrlActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendCmdService( ).lookupStorageCtrl( this.clientFactory.getLocalSession( ).getSession( ), search, range,
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
      this.view = this.clientFactory.getStorageCtrlView( );
      ( ( StorageCtrlView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( StorageCtrlView ) this.view ).clear( );
    }
    ( ( StorageCtrlView ) this.view ).showSearchResult( result );    
  }
  
  @Override
  public void onRegister() {
    InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( REGISTER_SC_CAPTION[1], REGISTER_SC_SUBJECT[1], new ArrayList<InputField>( Arrays.asList(
      new InputField( ) {
        @Override
        public String getTitle( ) {
          return SC_PART_INPUT_TITLE[1];
        }
  
        @Override
        public ValueType getType( ) {
          return ValueType.TEXT;
        }
  
        @Override
        public ValueChecker getChecker( ) {
          return null;
        }
      }, new InputField( ) {
        @Override
        public String getTitle( ) {
          return SC_NAME_INPUT_TITLE[1];
        }
  
        @Override
        public ValueType getType( ) {
          return ValueType.TEXT;
        }
  
        @Override
        public ValueChecker getChecker( ) {
          return null;
        }
      }, new InputField() {
      @Override
      public String getTitle() {
        return SC_HOST_INPUT_TITLE[1];
      }

      @Override
      public ValueType getType() {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker() {
        return null;
      }
    })));

    
  }

  @Override
  public void onDeregister() {
    if ( currentSelected == null || currentSelected.size( ) < 1 ) {
      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "请选择要注销的存储控制器", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        return;
    }
    System.out.println("size: " + currentSelected.size());
    
    ConfirmationView dialog = this.clientFactory.getConfirmationView( );
    dialog.setPresenter( this );
    dialog.display( DEREGISTER_SC_CAPTION[1], DEREGISTER_SC_SUBJECT[1], currentSelected, new ArrayList<Integer>( Arrays.asList(0, 1 ) ));    
    
    
  }

  @Override
  public void confirm(String subject) {
    if (DEREGISTER_SC_SUBJECT[1].equals(subject)) {
      doDeregisterSC();
    }
    
  }

  private void doDeregisterSC() {
    if ( currentSelected == null || currentSelected.size( ) != 1 ) {
      return;
    }
    
    String part = null;
    String name = null;
    for ( SearchResultRow row : currentSelected ) {
      part = row.getField(1);
      name = row.getField(0);
    }
    
    clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "注销存储控制器中...", 0 );
    
    clientFactory.getBackendCmdService( ).deregisterStorage(clientFactory.getLocalSession( ).getSession( ), part, name, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "注销存储控制器失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "注销存储控制器失败:" + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "注销存储控制器成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "注销存储控制器成功" );
        reloadCurrentRange( );
        currentSelected = null;
      }
    });
    
  }

  @Override
  public void process(String subject, ArrayList<String> values) {
    if (REGISTER_SC_SUBJECT[1].equals(subject)) {
      doRegisterSC(values.get(0), values.get(1), values.get(2));
    }
    
  }

  private void doRegisterSC(String part, String name, String host) {
    this.clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "注册存储控制器...", 0);
    this.clientFactory.getBackendCmdService().registerStorage(this.clientFactory.getLocalSession().getSession(), part, host, name,  
        new AsyncCallback<Void>( ) {
          @Override
          public void onFailure( Throwable caught ) {
          ActivityUtil.logoutForInvalidSession( clientFactory, caught );
          clientFactory
          .getShellView()
          .getFooterView()
          .showStatus(StatusType.ERROR, "注册存储控制器失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
          clientFactory
          .getShellView()
          .getLogView()
          .log(LogType.ERROR, "注册存储控制器失败" + ": " + caught.getMessage());
          }
          
          @Override
          public void onSuccess( Void result ) {
            clientFactory
            .getShellView()
            .getFooterView()
            .showStatus(StatusType.NONE, "注册存储控制器成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
            reloadCurrentRange();
          }
        } );    

    
  }
  
}
