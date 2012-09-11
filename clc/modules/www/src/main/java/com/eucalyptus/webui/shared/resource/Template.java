package com.eucalyptus.webui.shared.resource;

import java.io.Serializable;

public class Template implements Serializable {
	
	private static final long serialVersionUID = 1118015847500947726L;
	
	private String ID;
	private String name;
	private String cpu;
	private String mem;
	private String disk;
	private String bw;
	private String image;
	private String ncpus;
	
	public String getNCPUs() {
		return ncpus;
	}
	
	public void setNCPUs(String ncpus) {
		this.ncpus = ncpus;
	}
	
	public void setID(String ID) {
		this.ID = ID;
	}
	
	public String getID() {
		return ID;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setCPU(String cpu) {
		this.cpu = cpu;
	}
	
	public String getCPU() {
		return this.cpu;
	}
	
	public void setMem(String mem) {
		this.mem = mem;
	}
	
	public String getMem() {
		return this.mem;
	}
	
	public void setDisk(String disk) {
		this.disk = disk;
	}
	
	public String getDisk() {
		return this.disk;
	}
	
	public void setBw(String bw) {
		this.bw = bw;
	}
	
	public String getBw() {
		return this.bw;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getImage() {
		return this.image;
	}
	
}
