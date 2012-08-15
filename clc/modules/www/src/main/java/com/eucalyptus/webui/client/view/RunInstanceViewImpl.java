package com.eucalyptus.webui.client.view;

import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

import com.eucalyptus.webui.client.activity.ImageActivity;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.shared.aws.ImageType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RunInstanceViewImpl extends DialogBox implements RunInstanceView {

	private static RunInstanceViewImplUiBinder uiBinder = GWT.create(RunInstanceViewImplUiBinder.class);
	
	Presenter presenter;
	
	String[] VMTYPES = {"m1.small", "m1.large", "m1.xlarge", "c1.medium", "c1.xlarge"};
	
  @UiField
  ListBox imageBox;
  @UiField
  ListBox keypairBox;
  @UiField
  ListBox vmtypeBox;
  @UiField
  ListBox groupBox;
  
  @UiField
  Anchor okButton;
  @UiField
  Anchor cancelButton;
  
	interface RunInstanceViewImplUiBinder extends UiBinder<Widget, RunInstanceViewImpl> {
	}

	public RunInstanceViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		okButton.addClickHandler(clickHandler);
		cancelButton.addClickHandler(clickHandler);
		
	}
	private ClickHandler clickHandler = new ClickHandler() {

    @Override
    public void onClick(ClickEvent event) {
      Object o = event.getSource();
      if (o == okButton) {
        String image = imageBox.getValue(imageBox.getSelectedIndex());
        String keypair = keypairBox.getValue(keypairBox.getSelectedIndex());
        String vmtype = vmtypeBox.getValue(vmtypeBox.getSelectedIndex());
        //TODO
        String group = ""; 
        presenter.processRun(image, keypair, vmtype, group);
        RunInstanceViewImpl.this.hide();
      } else if (o == cancelButton) {
        RunInstanceViewImpl.this.hide();
      }
    }
	  
	};
	    
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		//Document.get( ).getElementById( nameInput.getName() ).focus( );
	}
	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		clearInputField();
		
		imageBox.clear();
		for (String s : this.presenter.getImages())
		  imageBox.addItem(s);
		keypairBox.clear();
    for (String s : this.presenter.getKeypairs())
      keypairBox.addItem(s);
    
    vmtypeBox.clear();
    for (String s : VMTYPES)
      vmtypeBox.addItem(s);
    groupBox.clear();
    //TODO: security group
		this.setWidth("300px");
		this.setHeight("200px");
		this.center();
		this.show();
	}
	

	private void clearInputField() {

	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}
	
}
