package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.eucalyptus.webui.client.view.ImageView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.UploadImageView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.shared.aws.ImageType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ImageActivity extends AbstractSearchActivity implements ImageView.Presenter, DetailView.Presenter, UploadImageView.Presenter, InputView.Presenter, ConfirmationView.Presenter {
  
  public static final String TITLE = "镜像管理";
  public static final String[] BIND_IMAGE_CAPTION = {"", "绑定镜像"};
  public static final String[] BIND_IMAGE_SUBJECT = {"", "输入绑定镜像信息:"};
  public static final String[] UNBIND_IMAGE_CAPTION = {"", "取消绑定"};
  public static final String[] UNBIND_IMAGE_SUBJECT = {"", "确定要取消绑定下列镜像"};
  public static final String[] IMAGE_SYS_NAME_INPUT_TITLE = {"", "系统名称"};
  public static final String[] IMAGE_SYS_VER_INPUT_TITLE = {"", "系统版本"};
  public static final String[] FOOTERVIEW_BIND_SUCCESS = {"", "绑定镜像成功"};
  public static final String[] FOOTERVIEW_BIND_FAILURE = {"", "绑定镜像失败"};
  public static final String[] FOOTERVIEW_BIND_WORKING = {"", "绑定镜像中..."};
  public static final String[] FOOTERVIEW_UNBIND_SUCCESS = {"", "取消绑定成功"};
  public static final String[] FOOTERVIEW_UNBIND_FAILURE = {"", "取消绑定失败"};
  public static final String[] FOOTERVIEW_UNBIND_WORKING = {"", "取消绑定中..."};
  public static final String[] FOOTERVIEW_SELECT_IMAGES = {"", "请选择镜像"};
  public static final String[] FOOTERVIEW_SELECT_ONE_IMAGE = {"", "请选择一个镜像"};
  public static final int RESULT_IMAGE_ID = 3;

  
  private static final Logger LOG = Logger.getLogger( ImageActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  private ArrayList<String> mKernels = new ArrayList<String>();
  private ArrayList<String> mRamDisks = new ArrayList<String>();
  
  public ImageActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendAwsService( ).lookupImage( this.clientFactory.getLocalSession( ).getSession( ), 0, search, range,
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
    this.clientFactory.getBackendAwsService().uploadImage(this.clientFactory.getLocalSession( ).getSession( ), 0, file, type, bucket, name, kernel, ramdisk, new AsyncCallback<String>() {

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

  @Override
  public void onBindImage() {
    if ( currentSelected == null || currentSelected.size( ) != 1 ) {
      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_SELECT_ONE_IMAGE[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        return;
    }
    InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( BIND_IMAGE_CAPTION[1], BIND_IMAGE_SUBJECT[1], new ArrayList<InputField>( Arrays.asList(
      new InputField( ) {
        @Override
        public String getTitle( ) {
          return IMAGE_SYS_NAME_INPUT_TITLE[1];
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
          return IMAGE_SYS_VER_INPUT_TITLE[1];
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
  public void process(String subject, ArrayList<String> values) {
    if (BIND_IMAGE_SUBJECT[1].equals(subject)) {
      doBindImage(values.get(0), values.get(1));
    }    
  }

  private void doBindImage(String sysName, String sysVer) {
    if ( currentSelected == null || currentSelected.size( ) != 1 ) {
        return;
    }
    String id = "";
    for (SearchResultRow r: currentSelected) {
      id = r.getField(RESULT_IMAGE_ID);      
    }
    this.clientFactory.getBackendAwsService().bindImage(this.clientFactory.getLocalSession( ).getSession( ), 0, id, sysName, sysVer, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_BIND_FAILURE[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
      }

      @Override
      public void onSuccess(Void result) {
        clientFactory.getShellView().getFooterView().showStatus(StatusType.NONE, FOOTERVIEW_BIND_SUCCESS[1], FooterView.CLEAR_DELAY_SECOND * 5);
        reloadCurrentRange();
      }        
    });
  }

  @Override
  public void onUnbindImage() {
    if ( currentSelected == null || currentSelected.size( ) < 1 ) {
      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_SELECT_IMAGES[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        return;
    }
    System.out.println("size: " + currentSelected.size());
    
    ConfirmationView dialog = this.clientFactory.getConfirmationView( );
    dialog.setPresenter( this );
    dialog.display( UNBIND_IMAGE_CAPTION[1], UNBIND_IMAGE_SUBJECT[1], currentSelected, new ArrayList<Integer>( Arrays.asList( 1, 2 ) ));    
  }

  @Override
  public void confirm(String subject) {
    if (UNBIND_IMAGE_SUBJECT[1].equals(subject)) {
      doUnbindImage();
    }
  }

  private void doUnbindImage() {
    if ( currentSelected == null || currentSelected.size( ) < 1 ) {
      return;
    }
    
    final ArrayList<String> ids = Lists.newArrayList( ); 
    for ( SearchResultRow row : currentSelected ) {
      ids.add( row.getField( RESULT_IMAGE_ID ) );
    }
    
    clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, FOOTERVIEW_UNBIND_WORKING[1], 0 );
    
    clientFactory.getBackendAwsService( ).unbindImages( clientFactory.getLocalSession( ).getSession(), 0, ids, new AsyncCallback<Void>( ) {

      @Override    
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_UNBIND_FAILURE[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );

      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_UNBIND_SUCCESS[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        reloadCurrentRange( );
      }
    });

    
  }
 
}
