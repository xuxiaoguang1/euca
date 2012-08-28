package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.device.DeviceSyncException;
import com.eucalyptus.webui.server.device.VMDBProcWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.resource.VMImageType;

public class DeviceVMServiceProcImpl {

	public SearchResult lookupVM(Session session, String search, SearchRange range, int queryState)
	        throws EucalyptusServiceException {
		//System.err.println(Debug.footprint());
		return new SearchResult(0, range);
	}
	
	public ArrayList<VMImageType> queryVMImageType() throws EucalyptusServiceException {
		//System.err.println(Debug.footprint());
		try {
			ResultSetWrapper rsWrapper = this.vmDBProcWrapper.queryVMImageType();
			
			ResultSet rs = rsWrapper.getResultSet();
			
			ArrayList<VMImageType> list = null;

			if (rs != null) {
				list = new ArrayList<VMImageType>();
				  
				while (rs.next()) {
					VMImageType imageType = new VMImageType(Integer.valueOf(rs.getString(DBTableColName.VM_IMAGE_TYPE.ID)),
							  							rs.getString(DBTableColName.VM_IMAGE_TYPE.OS),
														rs.getString(DBTableColName.VM_IMAGE_TYPE.VER));
					list.add(imageType);
				}
			}
			  
			rsWrapper.close();
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to query VM image type");
			
		} catch (DeviceSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			throw new EucalyptusServiceException("Failed to query VM image type");
		} 
	}

	
	private VMDBProcWrapper vmDBProcWrapper = new VMDBProcWrapper();
	
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
