package com.eucalyptus.webui.server.vm;

public class VmImageType {

	private int id;
	private String os;
	private String ver;
	private String euca_vit_id;
	
	public VmImageType(int id, String os, String ver, String euca_vit_id) {
		this.setId(id);
		this.setOs(os);
		this.setVer(ver);
		this.setEucaVITId(euca_vit_id);
	}
	
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
	
	public void setEucaVITId(String euca_vit_id) {
		this.euca_vit_id = euca_vit_id;
	}
	public String getEucaVITId() {
		return this.euca_vit_id;
	}
}
