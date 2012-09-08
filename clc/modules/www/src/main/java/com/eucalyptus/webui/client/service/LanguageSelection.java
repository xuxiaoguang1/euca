package com.eucalyptus.webui.client.service;

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
	
	public enum EnumLanguage {
		ENGLISH,
		CHINESE,
	}
	
	EnumLanguage lan = EnumLanguage.CHINESE;
}
