package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class AreaViewImpl extends DialogBox implements AreaView {

	private static AreaViewImplUiBinder uiBinder = GWT.create(AreaViewImplUiBinder.class);
	
	Presenter presenter;
	
	
  @UiField
  TextArea input;
  
  @UiField
  Anchor button;

  
	interface AreaViewImplUiBinder extends UiBinder<Widget, AreaViewImpl> {
	}

	public AreaViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		button.addClickHandler(clickHandler);

	}
	private ClickHandler clickHandler = new ClickHandler() {

    @Override
    public void onClick(ClickEvent event) {
      AreaViewImpl.this.hide(); 
    }
	  
	};
	    

	
	@Override
	public void display() {
	  input.setHeight("15em");
	  input.setWidth("530px");
	  input.setText(this.presenter.getText());
		this.setWidth("550px");
		this.setHeight("300px");
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


  @Override
  public void setFocus() {
    // TODO Auto-generated method stub
    
  }
	
}
