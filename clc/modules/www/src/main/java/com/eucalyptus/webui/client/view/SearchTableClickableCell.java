package com.eucalyptus.webui.client.view;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class SearchTableClickableCell extends ClickableTextCell {

	public SearchTableClickableCell() {
	    super();
	}
	
	public SearchTableClickableCell(SafeHtmlRenderer<String> renderer) {
	    super(renderer);
	}
	
	public void setColIndex(int index) {
		this.colIndex = index;
	}
	public int getColIndex() {
		return this.colIndex;
	}
	
	private int colIndex = 0;
}
