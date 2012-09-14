package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
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
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.InstanceView;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.client.view.RunInstanceView;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class InstanceActivity extends AbstractSearchActivity
    implements InstanceView.Presenter, ConfirmationView.Presenter, InputView.Presenter, RunInstanceView.Presenter {
  
  public static final String TITLE = "虚拟机管理";
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
  }

  @Override
  public void process(String subject, ArrayList<String> values) {
	  // TODO Auto-generated method stub

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
}