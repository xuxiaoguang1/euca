package com.eucalyptus.webui.shared.resource;

public class VMImageType implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String os;
	private String ver;
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return this.id;
	}
	
	public void setOs(String os) {
		this.os = os;
	}
	public String getOs() {
		return this.os;
	}
	
	public void setVer(String ver) {
		this.ver = ver;
	}
	public String getVer() {
		return this.ver;
	}
}
