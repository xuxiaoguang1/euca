package com.eucalyptus.webui.client.view;

import java.util.logging.Logger;

import com.eucalyptus.webui.client.chart.PieStatChart;
import com.eucalyptus.webui.client.service.SearchResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CPUStatViewImpl extends Composite implements CPUStatView {
	
	private static final Logger LOG = Logger.getLogger( CPUStatViewImpl.class.getName( ) );

	private static CPUStatViewImplUiBinder uiBinder = GWT.create(CPUStatViewImplUiBinder.class);

	interface CPUStatViewImplUiBinder extends UiBinder<Widget, CPUStatViewImpl> {}
	
	private Presenter presenter;

	public CPUStatViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	AbsolutePanel chartPanel;

	public CPUStatViewImpl(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	private void initChart(String title, String[] label, int[] size){
		PieStatChart chart = new PieStatChart(title, label, size); 
		chartPanel.add(chart, 80, 20);
		chart.init();
		chart.update();
	}

	public void setText(String text) {
	}

	public String getText() {
		return null;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}


	@Override
	public void showSearchResult(SearchResult result) {
		initChart("CPU信息", new String[]{"已使用", "未使用"}, new int[]{60, 120});
	}

}
