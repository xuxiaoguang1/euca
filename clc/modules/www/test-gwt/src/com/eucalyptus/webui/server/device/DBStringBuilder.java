package com.eucalyptus.webui.server.device;

import java.util.Date;

public class DBStringBuilder {
	
	private StringBuilder sb = new StringBuilder();
	
	public DBStringBuilder append(DBTable table) {
		sb.append(table.toString());
		return this;
	}
	
	public DBStringBuilder append(DBTableColumn column) {
		sb.append(column.toString());
		return this;
	}
	
	public DBStringBuilder append(int v) {
		sb.append(v);
		return this;
	}
	
	public DBStringBuilder append(double v) {
		sb.append(v);
		return this;
	}
	
	public DBStringBuilder append(String s) {
		sb.append(s);
		return this;
	}
	
	public DBStringBuilder appendDate(Date date) {
		sb.append("\"").append(DBData.format(date)).append("\"");
		return this;
	}
	
	public DBStringBuilder appendString(String s) {
		sb.append("\"").append(s).append("\"");
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}