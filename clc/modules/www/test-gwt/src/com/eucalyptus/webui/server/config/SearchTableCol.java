package com.eucalyptus.webui.server.config;

import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;

public class SearchTableCol {
	String[] title = null;
	String width = null;
	TableDisplay display = TableDisplay.NONE;
	Type text = Type.TEXT;
	
	boolean sortable = false;
	boolean editable = false;
	boolean hidden = false;
	boolean selected = false;
	
	String dbField = null;
	
	public SearchTableCol(String[] title, String width, TableDisplay display, Type text, String db_field,
			boolean sortable, boolean editable, boolean hidden, boolean selected) {
		this.setTitle(title);
		this.setWidth(width);
		this.setDisplay(display);
		this.setTextType(text);
		this.setSortable(sortable);
		this.setEditable(editable);
		this.setHidden(hidden);
		this.setSelected(selected);
		this.setDbField(db_field);
	}
	
	public void setTitle(String[] title) {
		this.title = title;
	}
	public String[] getTitle() {
		return this.title;
	}
	
	public void setWidth(String width) {
		this.width = width;
	}
	public String getWidth() {
		return this.width;
	}
	
	public void setDisplay(TableDisplay display) {
		this.display = display;
	}
	public TableDisplay getDisplay() {
		return this.display;
	}
	
	public void setTextType(Type text) {
		this.text = text;
	}
	public Type getTextType() {
		return this.text;
	}
	
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}
	public boolean getSortable() {
		return this.sortable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public boolean getEditable() {
		return this.editable;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public boolean getHidden() {
		return this.hidden;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean getSelected() {
		return this.selected;
	}
	
	public void setDbField(String dbField) {
		this.dbField = dbField;
	}
	public String getDbField() {
		return this.dbField;
	}
}
