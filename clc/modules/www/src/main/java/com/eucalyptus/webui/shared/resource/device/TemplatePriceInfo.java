package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class TemplatePriceInfo implements Serializable {

	private static final long serialVersionUID = 8618101930035521382L;
	
	public final int tp_id;
	public final int template_id;
	public final String tp_desc;
	public final double tp_cpu;
	public final double tp_mem;
	public final double tp_disk;
	public final double tp_bw;
	public final Date tp_creationtime;
	public final Date tp_modifiedtime;
	
	public TemplatePriceInfo(int tp_id, int template_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw, Date tp_creationtime, Date tp_modifiedtime) {
		this.tp_id = tp_id;
		this.template_id = template_id;
		this.tp_desc = tp_desc;
		this.tp_cpu = tp_cpu;
		this.tp_mem = tp_mem;
		this.tp_disk = tp_disk;
		this.tp_bw = tp_bw;
		this.tp_creationtime = tp_creationtime;
		this.tp_modifiedtime = tp_modifiedtime;
	}

}
