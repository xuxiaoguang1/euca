package com.eucalyptus.webui.server;

import com.eucalyptus.webui.server.config.SysConfParser;
import com.eucalyptus.webui.shared.config.SysConfig;
import com.eucalyptus.webui.shared.dictionary.ConfDef;

public class SysConfProcImpl {

	public static SysConfProcImpl instance() {
		if (instance == null) {
			instance = new SysConfProcImpl();
			instance.sysConfParser.parse(ConfDef.SYS_CONFIG_FILE);
		}
		
		return instance;
	}
	
	private SysConfProcImpl() {
	}
	
	public SysConfig getSysConfig() {
		return this.sysConfParser.getSysConfig();
	}
	
	SysConfParser sysConfParser = new SysConfParser();
	private static SysConfProcImpl instance;
}
