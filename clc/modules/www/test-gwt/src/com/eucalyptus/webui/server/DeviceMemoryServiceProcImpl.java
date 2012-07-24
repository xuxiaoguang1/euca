package com.eucalyptus.webui.server;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.session.Session;

public class DeviceMemoryServiceProcImpl {

	public SearchResult lookupMemory(Session session, String search, SearchRange range, int queryState)
	        throws EucalyptusServiceException {
		//System.err.println(Debug.footprint());
		return new SearchResult(0, range);
	}

	final private static int SELECT = 1;

	final private static String[] TABLE_COL_TITLE_CHECKALL = {"", "全选"};
	final private static String[] TABLE_COL_TITLE_NO = {"", "序号"};
	final private static String[] TABLE_COL_TITLE_NAME = {"", "名称"};
	final private static String[] TABLE_COL_TITLE_MARK = {"", "标识"};
	final private static String[] TABLE_COL_TITLE_IMAGE = {"", "镜像"};
	final private static String[] TABLE_COL_TITLE_CONF = {"", "配置"};
	final private static String[] TABLE_COL_TITLE_IP = {"", "IP地址"};
	final private static String[] TABLE_COL_TITLE_BW = {"", "带宽"};
	final private static String[] TABLE_COL_TITLE_STARTTIME = {"", "开始时间"};
	final private static String[] TABLE_COL_TITLE_LIFE = {"", "服务期限"};
	final private static String[] TABLE_COL_TITLE_REMAINS = {"", "剩余时间"};
	final private static String[] TABLE_COL_TITLE_STATE = {"", "状态"};

}
