package com.eucalyptus.webui.shared.config;


public class LanguageSelection {

	static public LanguageSelection instance() {
		if (instance == null)
			instance = new LanguageSelection();
		
		return instance;
	}
	
	public EnumLanguage getCurLanguage() {
		return this.lan;
	}
	public void setCurLanguage(EnumLanguage lan) {
		this.lan = lan;
	}
	
	private LanguageSelection() {	
	}
	
	static private LanguageSelection instance = null;
	
	EnumLanguage lan;
}
