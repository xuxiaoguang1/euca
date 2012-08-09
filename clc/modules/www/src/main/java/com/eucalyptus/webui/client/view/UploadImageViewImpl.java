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

public class UploadImageViewImpl extends DialogBox implements UploadImageView {

	private static UploadImageViewImplUiBinder uiBinder = GWT.create(UploadImageViewImplUiBinder.class);
	
	Presenter presenter;
	
  @UiField 
  FlowPanel uploadPanel;
  @UiField
  ListBox typeBox;
  @UiField
  ListBox kernelBox;
  @UiField
  ListBox ramDiskBox;
  @UiField
  Label kernelLabel;
  @UiField
  Label ramDiskLabel;
  
  @UiField
  TextBox nameBox;
  @UiField
  TextBox bucketBox;
  
  @UiField
  Anchor okButton;
  Anchor cancelButton;
  
  SingleUploader uploader;
  
	
	interface UploadImageViewImplUiBinder extends UiBinder<Widget, UploadImageViewImpl> {
	}

	public UploadImageViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		uploader = new SingleUploader();
		uploadPanel.add(uploader);
		uploader.addOnFinishUploadHandler(onFinishUploaderHandler);
		uploader.addOnChangeUploadHandler(onChangeUploaderHandler);
		uploader.addOnStartUploadHandler(onStartUploaderHandler);
		
		typeBox.addItem("内核", ImageType.KERNEL.toString());
		typeBox.addItem("RamDisk", ImageType.RAMDISK.toString());
		typeBox.addItem("根文件系统", ImageType.ROOTFS.toString());
		
		typeBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        int i = typeBox.getSelectedIndex();
        if (i == 2) 
          setKRVisible(true);
        else 
          setKRVisible(false);
      }
		  
		});
		okButton.addClickHandler(clickHandler);
		
		
	}
	private ClickHandler clickHandler = new ClickHandler() {

    @Override
    public void onClick(ClickEvent event) {
      Object o = event.getSource();

      if (o == okButton) {
        uploader.submit();
      } else if (o == cancelButton) {
        UploadImageViewImpl.this.hide();
      }
      
    }
	  
	};
	private IUploader.OnChangeUploaderHandler onChangeUploaderHandler = new IUploader.OnChangeUploaderHandler() {
    
    @Override
    public void onChange(IUploader uploader) {
      String name = uploader.getBasename();
      nameBox.setText(name);
    }
  };
	private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {

	  @Override
    public void onFinish(IUploader uploader) {
	    System.out.println(uploader.getBasename());
	    System.out.println(uploader.getServerResponse());
	    
	    presenter.processImage(uploader.getServerInfo().message, 
	                           ImageType.valueOf(typeBox.getValue(typeBox.getSelectedIndex())),
	                           bucketBox.getText(), 
	                           nameBox.getText(), 
	                           kernelBox.getValue(kernelBox.getSelectedIndex()),
	                           ramDiskBox.getValue(ramDiskBox.getSelectedIndex())
	                           );
	                           
	    UploadImageViewImpl.this.hide();
	    }
  };
  
  private IUploader.OnStartUploaderHandler onStartUploaderHandler = new IUploader.OnStartUploaderHandler() {
    
    @Override
    public void onStart(IUploader uploader) {
      //uploader.setServletPath(uploader.getServletPath() + "?name=" + nameBox.getText());
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
		
		kernelBox.clear();
		for (String s : this.presenter.getKernels())
		  kernelBox.addItem(s);
		ramDiskBox.clear();
    for (String s : this.presenter.getRamDisks())
      ramDiskBox.addItem(s);
		this.setWidth("300px");
		this.setHeight("200px");
		this.center();
		this.show();
	}
	

	private void clearInputField() {
	  typeBox.setItemSelected(0, true);
	  setKRVisible(false);
	  nameBox.setText("");
	  bucketBox.setText("");
	}
	
	private void setKRVisible(boolean v) {
    kernelBox.setVisible(v);
    ramDiskBox.setVisible(v);
    kernelLabel.setVisible(v);
    ramDiskLabel.setVisible(v);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}
	
}
