package com.eucalyptus.webui.client.service;

import java.io.Serializable;
import java.util.List;

import com.eucalyptus.webui.shared.message.ClientMessage;

public class SearchResultFieldDesc implements Serializable {
	
	private static final long serialVersionUID = -9181213163531915697L;

	public static enum TableDisplay {
		MANDATORY,
		OPTIONAL,
		NONE,
	}
	
	public static enum Type {
		TEXT, // single line text string
		ARTICLE, // multi-line text
		HIDDEN, // password like text
		REVEALING, // text revealing itself when mouseover (for security related
					// stuff, like secret key)
		BOOLEAN, // boolean
		DATE, // date in long
		ENUM, // enum value
		KEYVAL, // dynamic key value (like single line text but can be removed)
		NEWKEYVAL, // empty key value (for adding new)
		LINK, // URL link
		ACTION, // custom action, usually causing a popup
	}
	
	public static final String LINK_VALUE[] = {"Details ...", "查看 ..."};
	
	private String name; // ID of the field, also used as the key of a KEYVAL
	private String title; // title for display
	private ClientMessage message;
	private Boolean sortable; // if sortable in table display
	private String width; // width of column for table display
	private TableDisplay tableDisplay; // table display type
	private Type type; // value type
	private Boolean editable; // if this field is editable
	private Boolean hidden; // if this field should be hidden in properties
							// panel
	private List<String> enumValues; // the list of enum values for an ENUM
	private Boolean selected; // selected (for check box control)
	
	public SearchResultFieldDesc() {
	}
	
	public SearchResultFieldDesc(String title, Boolean sortable, String width) {
		this.name = title;
		this.title = title;
		this.sortable = sortable;
		this.width = width;
		this.tableDisplay = TableDisplay.MANDATORY;
		this.type = Type.TEXT;
		this.setEditable(true);
		this.setHidden(false);
		this.setSelected(false);
	}
	
	public SearchResultFieldDesc(boolean sortable, String width, ClientMessage clientMsg) {
		this(null, sortable, width);
		this.message = clientMsg;
	}
	
	public SearchResultFieldDesc(String title, String width, Boolean selected) {
		this.name = title;
		this.title = title;
		this.sortable = false;
		this.width = width;
		this.tableDisplay = TableDisplay.MANDATORY;
		this.type = Type.BOOLEAN;
		this.setEditable(true);
		this.setHidden(false);
		this.setSelected(selected);
	}
	
	public SearchResultFieldDesc(String width, boolean selected, ClientMessage clientMsg) {
		this(null, width, selected);
		this.message = clientMsg;
	}
	
	public SearchResultFieldDesc(String title, Boolean sortable, String width, TableDisplay tableDisplay, Type type, Boolean editable, Boolean hidden) {
		this.name = title;
		this.title = title;
		this.sortable = sortable;
		this.width = width;
		this.tableDisplay = tableDisplay;
		this.type = type;
		this.editable = editable;
		this.hidden = hidden;
		this.selected = false;
	}
	
	public SearchResultFieldDesc(boolean sortable, String width, ClientMessage clientMsg, TableDisplay tableDisplay, Type type, Boolean editable, Boolean hidden) {
		this(null, sortable, width, tableDisplay, type, editable, hidden);
		this.message = clientMsg;
	}
	
	public SearchResultFieldDesc(String name, String title, Boolean sortable, String width, TableDisplay tableDisplay, Type type, Boolean editable, Boolean hidden) {
		this.name = name;
		this.title = title;
		this.sortable = sortable;
		this.width = width;
		this.tableDisplay = tableDisplay;
		this.type = type;
		this.editable = editable;
		this.hidden = hidden;
		this.selected = false;
	}
	
	public SearchResultFieldDesc(String name, boolean sortable, String width, ClientMessage message, TableDisplay tableDisplay, Type type, Boolean editable, Boolean hidden) {
		this(name, null, sortable, width, tableDisplay, type, editable, hidden);
		this.message = message;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("name=").append(getName()).append(",");
		sb.append("title=").append(getTitle()).append(",");
		sb.append("sortable=").append(sortable).append(",");
		sb.append("width=").append(width).append(",");
		sb.append("tableDisplay=").append(tableDisplay).append(",");
		sb.append("type=").append(type).append(",");
		sb.append("editable=").append(editable).append(",");
		sb.append("hidden=").append(hidden);
		sb.append("selected=").append(selected);
		sb.append(")");
		return sb.toString();
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		if (title != null) {
			return title;
		}
		if (message != null) {
			return message.toString();
		}
		return null;
	}
	
	public void setSortable(Boolean sortable) {
		this.sortable = sortable;
	}
	
	public Boolean getSortable() {
		return sortable;
	}
	
	public void setWidth(String width) {
		this.width = width;
	}
	
	public String getWidth() {
		return width;
	}
	
	public void setTableDisplay(TableDisplay tableDisplay) {
		this.tableDisplay = tableDisplay;
	}
	
	public TableDisplay getTableDisplay() {
		return tableDisplay;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setEditable(Boolean editable) {
		this.editable = editable;
	}
	
	public Boolean getEditable() {
		return editable;
	}
	
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	
	public Boolean getHidden() {
		return hidden;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		if (name != null) {
			return name;
		}
		if (message != null) {
			return message.getText();
		}
		return null;
	}
	
	public void setEnumValues(List<String> enumValues) {
		this.enumValues = enumValues;
	}
	
	public List<String> getEnumValues() {
		return enumValues;
	}
	
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	
	public Boolean getSelected() {
		return selected;
	}
}
