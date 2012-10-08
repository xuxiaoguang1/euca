package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class CPUInfo implements Serializable {

	private static final long serialVersionUID = -8959658652658533444L;
	
	public int cpu_id;
	public String cpu_name;
	public int cpu_total;
	public String cpu_desc;
	public String cpu_vendor;
	public String cpu_model;
	public double cpu_ghz;
	public double cpu_cache;
	public Date cpu_creationtime;
	public Date cpu_modifiedtime;
	public int server_id;
	
	public CPUInfo() {
	}
	
	public CPUInfo(int cpu_id, String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, Date cpu_creationtime, Date cpu_modifiedtime, int server_id) {
		this.cpu_id = cpu_id;
		this.cpu_name = cpu_name;
		this.cpu_desc = cpu_desc;
		this.cpu_total = cpu_total;
		this.cpu_vendor = cpu_vendor;
		this.cpu_model = cpu_model;
		this.cpu_ghz = cpu_ghz;
		this.cpu_cache = cpu_cache;
		this.cpu_creationtime = cpu_creationtime;
		this.cpu_modifiedtime = cpu_modifiedtime;
		this.server_id = server_id;
	}

}
