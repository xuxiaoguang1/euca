package com.eucalyptus.webui.client.activity.device;

import java.util.LinkedList;
import java.util.List;

import com.eucalyptus.webui.client.view.DeviceMeasure;
import com.google.gwt.user.client.Window;

public class DevicePageSize {
	
	private static int index = -1;
	
	private static int[] pageSizeList = new int[]{10, 20, 30, 50, 100};
	
	public static int setPageSizeSelectedIndex(int index) {
		if (index >= 0 && index < pageSizeList.length) {
			DevicePageSize.index = index;
		}
		return DevicePageSize.index;
	}
		
	public static int getPageSizeSelectedIndex() {
		if (index < 0) {
			int height = (int)((DeviceMeasure.getHTMLSize("TEST").height) * 2);
			if (height > 0) {
				int size = (Window.getClientHeight() - 200) / height;
				for (index = 0; index < pageSizeList.length; index ++) {
					if (size <= pageSizeList[index]) {
						break;
					}
				}
				index = Math.min(index, pageSizeList.length - 1);
			}
			else {
				index = 1;
			}
		}
		return index;
	}
	
	public static int getPageSize() {
		return pageSizeList[getPageSizeSelectedIndex()];
	}
	
	public static List<String> getPageSizeList() {
		List<String> list = new LinkedList<String>();
		for (int pageSize : pageSizeList) {
			list.add(Integer.toString(pageSize));
		}
		return list;
	}
	
}
