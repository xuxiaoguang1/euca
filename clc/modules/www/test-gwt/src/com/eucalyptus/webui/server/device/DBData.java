package com.eucalyptus.webui.server.device;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBData {
	
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	public static String format(Date date) {
		if (date != null) {
			return formatter.format(date);
		}
		return "";
	}
	
	public static Date parse(String s) throws ParseException {
		return formatter.parse(s);
	}
	
	public static Date getDate(ResultSet rs, DBTableColumn column) {
		try {
			return rs.getDate(column.toString());
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static int getInt(ResultSet rs, DBTableColumn column) throws SQLException {
		return rs.getInt(column.toString());
	}
	
	public static long getLong(ResultSet rs, DBTableColumn column) throws SQLException {
		return rs.getLong(column.toString());
	}
	
	public static String getString(ResultSet rs, DBTableColumn column) throws SQLException {
		return rs.getString(column.toString());
	}
	
	public static String getString(ResultSet rs, String name) throws SQLException {
		return rs.getString(name);
	}
	
	public static double getDouble(ResultSet rs, DBTableColumn column) throws SQLException {
		return rs.getDouble(column.toString());
	}
	
	public static String format(double value) {
	    return Double.toString((double)((int)(value * 1000)) / 1000);
	}
	
}
