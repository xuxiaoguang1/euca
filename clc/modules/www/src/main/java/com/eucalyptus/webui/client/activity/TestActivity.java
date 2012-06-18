package com.eucalyptus.webui.client.activity;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.TestPlace;
import com.eucalyptus.webui.client.view.TestView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Showing the start page, providing guides for first-time users,
 * and shortcuts for experienced users.
 * 
 * @author Ye Wen (wenye@eucalyptus.com)
 *
 */
public class TestActivity extends AbstractActivity {
  
  public static final String TITLE = "START GUIDE";
  
  public static final String SERVICE_SNIPPET = "service";
  public static final String IAM_SNIPPET = "iam";
  
  private static final Logger LOG = Logger.getLogger( StartActivity.class.getName( ) );
  
  private ClientFactory clientFactory;
  private TestPlace place;
  
  public TestActivity( TestPlace place, ClientFactory clientFactory ) {
    this.clientFactory = clientFactory;
    this.place = place;
  }
  
  @Override
  public void start( AcceptsOneWidget container, EventBus eventBus ) {
    LOG.log( Level.INFO, "Start TestActivity" );
    this.clientFactory.getShellView( ).getContentView( ).setContentTitle( TITLE );
    TestView testView = this.clientFactory.getTestView( );
    container.setWidget( testView );
    loadSnippets( testView, eventBus );
    ActivityUtil.updateDirectorySelection( clientFactory );
  }
  
  private void loadSnippets( TestView view, EventBus eventBus ) {
	boolean isSystemAdmin = this.clientFactory.getSessionData( ).getLoginUser( ).isSystemAdmin( );
    if ( isSystemAdmin ) {
      new CloudRegistrationActivity( clientFactory ).start( view.getCloudRegSnippetDisplay( ), eventBus );
      new GenericGuideActivity( clientFactory, SERVICE_SNIPPET ).start( view.getServiceSnippetDisplay( ), eventBus );
    }
    new DownloadActivity( clientFactory ).start( view.getDownloadSnippetDisplay( ), eventBus );
    new GenericGuideActivity( clientFactory, IAM_SNIPPET ).start( view.getIamSnippetDisplay( ), eventBus );
  }
  
}
