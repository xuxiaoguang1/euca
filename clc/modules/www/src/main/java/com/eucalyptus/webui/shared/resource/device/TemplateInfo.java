package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class TemplateInfo implements Serializable {

	private static final long serialVersionUID = 7951692646466707568L;
	
	public int template_id;
	public String template_name;
	public String template_desc;
	public String template_cpu;
	public int template_ncpus;
	public long template_mem;
	public long template_disk;
	public int template_bw;
	public String template_image;
	public Date template_creationtime;
	public Date template_modifiedtime;
	
	public TemplateInfo() {
	}
	
	public TemplateInfo(int template_id, String template_name, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image, Date template_creationtime, Date template_modifiedtime) {
		this.template_id = template_id;
		this.template_name = template_name;
		this.template_desc = template_desc;
		this.template_cpu = template_cpu;
		this.template_ncpus = template_ncpus;
		this.template_mem = template_mem;
		this.template_disk = template_disk;
		this.template_bw = template_bw;
		this.template_image = template_image;
		this.template_creationtime = template_creationtime;
		this.template_modifiedtime = template_modifiedtime;
	}

}
