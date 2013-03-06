package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.InstancePlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputListField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.InstanceView;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.client.view.RunInstanceView;
import com.eucalyptus.webui.client.view.SearchTableCellClickHandler;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class InstanceActivity extends AbstractSearchActivity
    implements InstanceView.Presenter, ConfirmationView.Presenter, InputView.Presenter, RunInstanceView.Presenter, SearchTableCellClickHandler {
  
  public static final String TITLE = "虚拟机管理";
  public static final String[] ASSOCIATE_ADDRESS_CAPTION = {"", "关联IP"};
  public static final String[] ASSOCIATE_ADDRESS_SUBJECT = {"", "输入关联IP信息:"};
  public static final String[] ASSOCIATE_ADDRESS_INPUT_TITLE = {"", "IP地址"};
  public static final String[] FOOTERVIEW_SELECT_ONE_INSTANCE = {"", "请选择一个虚拟机"};
  private static final Logger LOG = Logger.getLogger( InstanceActivity.class.getName( ) );

  private Set<SearchResultRow> currentSelected;
  
  private ArrayList<String> mImages;
  private ArrayList<String> mKeypairs;
    
  public InstanceActivity( InstancePlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  protected void doSearch( final String search, SearchRange range ) {
	
    this.clientFactory.getBackendAwsService( ).lookupInstance( this.clientFactory.getLocalSession( ).getSession( ), 0, search, range,
                                                           new AsyncCallback<SearchResult>( ) {
      
      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        LOG.log( Level.WARNING, "Search failed: " + caught );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Instance search " + search + " failed: " + caught.getMessage( ) );
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
      // this.clientFactory.getShellView( ).showDetail( DETAIL_PANE_SIZE );
      // showSingleSelectedDetails( selection.toArray( new SearchResultRow[0] )[0] );
    }
  }

  @Override
  protected String getTitle( ) {
    return TITLE;
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getInstanceView( );
      ( ( InstanceView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( InstanceView ) this.view ).clear( );
    }
    
    ( ( InstanceView ) this.view ).showSearchResult( result );
    //Registering setCellClickProc must await UserAppView's table inited  
    ( ( InstanceView ) this.view ).setCellClickProc( this );
  }

  @Override
  public void process(String subject, ArrayList<String> values) {
	  LOG.log(Level.INFO, values.toString());
	  String ip = values.get(0);
	  String instance = null;
    for ( SearchResultRow row : currentSelected ) {
      instance = row.getField(2);
    }
    this.clientFactory.getBackendAwsService( ).associateAddress(clientFactory.getLocalSession( ).getSession( ), 0, ip, instance, new AsyncCallback<Void>( ) {
      @Override
      public void onFailure(Throwable caught) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "关联IP失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY );        
      }

      @Override
      public void onSuccess(Void result) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "关联IP成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        InstanceActivity.this.reloadCurrentRange();
      }
    });
  }

  @Override
  public void confirm(String subject) {
	  // TODO Auto-generated method stub

  }

  @Override
  public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
	  // TODO Auto-generated method stub

  }

  @Override
  public void onStartInstances() {
  	final ArrayList<String> ids = Lists.newArrayList();
      for ( SearchResultRow row : currentSelected ) {
          ids.add(row.getField(0));
      }
      
  	this.clientFactory.getBackendAwsService().startInstances(clientFactory.getLocalSession( ).getSession( ), 0, ids, new AsyncCallback<ArrayList<String>>( ) {
  	      @Override
  	      public void onFailure( Throwable caught ) {
  	        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
  	        // Log
  	      }
  	      @Override
  	      public void onSuccess( ArrayList<String> arg ) {
  	    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Instances stopped", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
  	    	// Log
  	      }
  	});
  	
  }
  
  @Override
  public void onStopInstances() {
  	final ArrayList<String> ids = Lists.newArrayList();
      for ( SearchResultRow row : currentSelected ) {
          ids.add(row.getField(0));
      }
  	this.clientFactory.getBackendAwsService().stopInstances(clientFactory.getLocalSession( ).getSession( ), 0, ids, new AsyncCallback<ArrayList<String>>( ) {
  	      @Override
  	      public void onFailure( Throwable caught ) {
  	        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
  	        // Log
  	      }
  	      @Override
  	      public void onSuccess( ArrayList<String> arg ) {
  	    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Instances stopped", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
  	    	// Log
  	      }
  	});
  	
  }
  
  @Override
  public void onTerminateInstances() {
  	final ArrayList<String> ids = Lists.newArrayList();
      for ( SearchResultRow row : currentSelected ) {
          ids.add(row.getField(0));
      }
  	this.clientFactory.getBackendAwsService().terminateInstances(clientFactory.getLocalSession( ).getSession( ), 0, ids, new AsyncCallback<ArrayList<String>>( ) {
  	      @Override
  	      public void onFailure( Throwable caught ) {
  	        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
  	        // Log
  	      }
  	      @Override
  	      public void onSuccess( ArrayList<String> arg ) {
  	    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "正在关闭虚拟机...", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
  	    	  InstanceActivity.this.reloadCurrentRange();
  	    	// Log
  	      }
  	});
  	
  }
  
  @Override
  public void onRunInstance() {
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "获取信息中...", 0 );
    mImages = new ArrayList<String>();
    mKeypairs = new ArrayList<String>();
    this.clientFactory.getBackendAwsService().lookupKeypair(clientFactory.getLocalSession( ).getSession( ), 0, "", new SearchRange(), new AsyncCallback<SearchResult> () {
  
      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
        
      }
  
      @Override
      public void onSuccess(SearchResult result) {
        for (SearchResultRow r : result.getRows()) 
          mKeypairs.add(r.getField(0));
        InstanceActivity.this.clientFactory.getBackendAwsService().lookupImage(clientFactory.getLocalSession( ).getSession( ), 0, "machine", new SearchRange(), new AsyncCallback<SearchResult>() {
  
          @Override
          public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub
            
          }
  
          @Override
          public void onSuccess(SearchResult result) {
            for (SearchResultRow r : result.getRows()) 
              mImages.add(r.getField(0));
            clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
            _onRunInstance();
          }     
        });
      }
      
    });
    
  }
  
  private void _onRunInstance() {
    final RunInstanceView dialog = this.clientFactory.createRunInstanceView();
    dialog.setPresenter(this);
    dialog.display();  
  }
  
  @Override
  public void processRun(String image, String keypair, String vmtype, String group) {
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "虚拟机创建中...", 0 );
    this.clientFactory.getBackendAwsService().runInstance(clientFactory.getLocalSession( ).getSession( ), 0, image, keypair, vmtype, group, new AsyncCallback<String>() {
    //this.clientFactory.getBackendAwsService().runInstance(clientFactory.getLocalSession( ).getSession( ), image, keypair, new AsyncCallback<String>() {
  
      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
        
      }
  
      @Override
      public void onSuccess(String result) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "虚拟机 " + result + " 已创建", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        reloadCurrentRange();
      }
    
  });
  }
  
  @Override
  public List<String> getImages() {
    // TODO Auto-generated method stub
    return mImages;
  }
  
  @Override
  public List<String> getKeypairs() {
    // TODO Auto-generated method stub
    return mKeypairs;
  }
  
  @Override
  public void onClick(int rowIndex, int colIndex, SearchResultRow row) {
    // TODO Auto-generated method stub
    Window.alert(row.getLink(colIndex));
  }

  @Override
  public void onAssociateAddress() {
    if ( currentSelected == null || currentSelected.size( ) != 1 ) {
      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_SELECT_ONE_INSTANCE[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        return;
    }
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "获取可用IP中...", 0 );
    this.clientFactory.getBackendAwsService().lookupOwnAddress(clientFactory.getLocalSession( ).getSession( ), 0, new AsyncCallback<List<String>>( ) {

      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
        LOG.log(Level.WARNING, caught.getMessage());
      }

      @Override
      public void onSuccess(List<String> result) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        final List<String> ret = new ArrayList<String>(result);
        InputView dialog = InstanceActivity.this.clientFactory.getInputView( );
        dialog.setPresenter(InstanceActivity.this );
        dialog.display( ASSOCIATE_ADDRESS_CAPTION[1], ASSOCIATE_ADDRESS_SUBJECT[1], new ArrayList<InputField>( Arrays.asList(
          new InputListField( ) {
            @Override
            public String getTitle( ) {
              return ASSOCIATE_ADDRESS_INPUT_TITLE[1];
            }
      
            @Override
            public ValueType getType( ) {
              return ValueType.LISTBOX;
            }
      
            @Override
            public ValueChecker getChecker( ) {
              return null;
            }
            @Override
            public List<String> getItems() {
              return ret;
            }
          })));        
      }      
    });
    
  
  }
}