package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.CPUState;

public class CPUServiceInfo implements Serializable {

	private static final long serialVersionUID = -1366046491326557115L;
	
	public int cs_id;
	public String cs_desc;
	public Date cs_starttime;
	public Date cs_endtime;
	public CPUState cpu_state;
	public Date cs_creationtime;
	public Date cs_modifiedtime;
	public int cpu_id;
	public int user_id;
	
	public CPUServiceInfo() {
	}
	
	public CPUServiceInfo(int cs_id, String cs_desc, Date cs_starttime, Date cs_endtime, CPUState cpu_state, Date cs_creationtime, Date cs_modifiedtime, int cpu_id, int user_id) {
		this.cs_id = cs_id;
		this.cs_desc = cs_desc;
		this.cs_starttime = cs_starttime;
		this.cs_endtime = cs_endtime;
		this.cpu_state = cpu_state;
		this.cs_creationtime = cs_creationtime;
		this.cs_modifiedtime = cs_modifiedtime;
		this.cpu_id = cpu_id;
		this.user_id = user_id;
	}
	
}
