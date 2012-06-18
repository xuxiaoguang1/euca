package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TestViewImpl extends Composite implements TestView {
  
  private static TestViewImplUiBinder uiBinder = GWT.create( TestViewImplUiBinder.class );
  
  interface TestViewImplUiBinder extends UiBinder<Widget, TestViewImpl> {}
  
  @UiField
  SimplePanel serviceSnippet;

  @UiField
  SimplePanel iamSnippet;

  @UiField
  SimplePanel cloudRegSnippet;

  @UiField
  SimplePanel downloadSnippet;

  @UiField
  Anchor serviceHeader;

  @UiField
  Anchor iamHeader;

  @UiField
  Anchor cloudRegHeader;

  @UiField
  Anchor downloadHeader;
  
  @UiField
  TextBox textBox;
  
  @UiField
  Button btn;
  
  @UiField
  TextArea textArea;

  
  public TestViewImpl( ) {
    initWidget( uiBinder.createAndBindUi( this ) );
  }

  @UiHandler( "btn")
  void handleButtonCtextlick( ClickEvent e) {
	  String str = textBox.getText();
	  textArea.setText(str);
  }
  
  @UiHandler( "serviceHeader" )
  void handleServiceHeaderClick( ClickEvent e ) {
    serviceSnippet.setVisible( !serviceSnippet.isVisible( ) );
  }

  @UiHandler( "iamHeader" )
  void handleIamHeaderClick( ClickEvent e ) {
    iamSnippet.setVisible( !iamSnippet.isVisible( ) );
  }

  @UiHandler( "cloudRegHeader" )
  void handlecloudRegHeaderClick( ClickEvent e ) {
    cloudRegSnippet.setVisible( !cloudRegSnippet.isVisible( ) );
  }

  @UiHandler( "downloadHeader" )
  void handleDownloadHeaderClick( ClickEvent e ) {
    downloadSnippet.setVisible( !downloadSnippet.isVisible( ) );
  }

  @Override
  public AcceptsOneWidget getCloudRegSnippetDisplay( ) {
    return cloudRegSnippet;
  }

  @Override
  public AcceptsOneWidget getDownloadSnippetDisplay( ) {
    return downloadSnippet;
  }

  @Override
  public AcceptsOneWidget getServiceSnippetDisplay( ) {
    return serviceSnippet;
  }

  @Override
  public AcceptsOneWidget getIamSnippetDisplay( ) {
    return iamSnippet;
  }
  
}
