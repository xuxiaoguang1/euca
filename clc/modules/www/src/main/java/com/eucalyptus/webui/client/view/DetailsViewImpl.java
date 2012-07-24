package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets.ForIsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;

public class DetailsViewImpl extends Composite implements DetailsView {

	private static DetailsViewImplUiBinder uiBinder = GWT
			.create(DetailsViewImplUiBinder.class);
	
	@UiField LayoutPanel contentPanel;

	interface DetailsViewImplUiBinder extends UiBinder<Widget, DetailsViewImpl> {
	}

	public DetailsViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public ForIsWidget getContentContainer() {
		// TODO Auto-generated method stub
		return this.contentPanel;
	}

}
