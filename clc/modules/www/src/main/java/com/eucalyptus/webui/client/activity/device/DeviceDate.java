package com.eucalyptus.webui.client.activity.device;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class DeviceDate {
	
	private static DateTimeFormat formatter = DateTimeFormat.getFormat("yyyy-MM-dd");
	
	public static String format(Date date) {
		return formatter.format(date);
	}
	
	public static String today() {
		return format(new Date());
	}
	
	public static Date parse(String date) throws Exception {
		return formatter.parse(date);
	}
	
	public static int calcLife(String end, String start) throws Exception {
    	return calcLife(formatter.parse(end), formatter.parse(start));
    }
	
	public static int calcLife(Date endtime, Date starttime) {
		return Math.max(0, (int)((endtime.getTime() - starttime.getTime()) / (1000L * 24 * 3600)) + 1);
	}

}
