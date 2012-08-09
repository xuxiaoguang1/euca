package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DetailView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.ImageView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.UploadImageView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.shared.aws.ImageType;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ImageActivity extends AbstractSearchActivity implements ImageView.Presenter, DetailView.Presenter, UploadImageView.Presenter {
  
  public static final String TITLE = "镜像管理";
  
  private static final Logger LOG = Logger.getLogger( ImageActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  private ArrayList<String> mKernels = new ArrayList<String>();
  private ArrayList<String> mRamDisks = new ArrayList<String>();
  
  public ImageActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendAwsService( ).lookupImage( this.clientFactory.getLocalSession( ).getSession( ), search, range,
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
        mKernels.clear();
        mRamDisks.clear();
        for (SearchResultRow r : result.getRows()) {
          //FIXME: constants
          if (r.getField(1).equals("ramdisk")) {
            mRamDisks.add(r.getField(0));
          } else if (r.getField(1).equals("kernel")) {
            mKernels.add(r.getField(0));
          }
        }
        System.out.println(mRamDisks);
        System.out.println(mKernels);
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
      this.view = this.clientFactory.getImageView( );
      ( ( ImageView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( ImageView ) this.view ).clear( );
    }
    ( ( ImageView ) this.view ).showSearchResult( result );    
  }

  @Override
  public void onUploadImage() {
    final UploadImageView dialog = this.clientFactory.createUploadImageView();
    dialog.setPresenter(this);
    dialog.display();
  }

  @Override
  public void processImage(String file, ImageType type, String bucket, String name, String kernel, String ramdisk) {
    ImageActivity.this.clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "上传镜像中...", 0);
    this.clientFactory.getBackendCmdService().uploadImage(this.clientFactory.getLocalSession( ).getSession( ), file, type, bucket, name, kernel, ramdisk, new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
        System.out.println("@@");
      }

      @Override
      public void onSuccess(String result) {
        // TODO Auto-generated method stub
        ImageActivity.this.clientFactory.getShellView().getFooterView().showStatus(StatusType.NONE, "上传镜像成功： " + result, FooterView.CLEAR_DELAY_SECOND * 5);
        ImageActivity.this.reloadCurrentRange();
      }
      
    });
  }

  @Override
  public List<String> getKernels() {
    return mKernels;
  }

  @Override
  public List<String> getRamDisks() {
    return mRamDisks;
  }
 
}
