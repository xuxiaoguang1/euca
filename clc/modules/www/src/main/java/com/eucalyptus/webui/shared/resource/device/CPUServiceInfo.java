package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.CPUState;

public class CPUServiceInfo implements Serializable {

	private static final long serialVersionUID = -1366046491326557115L;
	
	public int cpu_service_id;
	public String cpu_service_desc;
	public Date cpu_service_starttime;
	public Date cpu_service_endtime;
	public CPUState cpu_service_state;
	public Date cpu_service_creationtime;
	public Date cpu_service_modifiedtime;
	public int cpu_id;
	public int user_id;
	
	public CPUServiceInfo() {
	}
	
	public CPUServiceInfo(int cpu_service_id, String cpu_service_desc, Date cpu_service_starttime, Date cpu_service_endtime, CPUState cpu_service_state, Date cpu_service_creationtime, Date cpu_service_modifiedtime, int cpu_id, int user_id) {
		this.cpu_service_id = cpu_service_id;
		this.cpu_service_desc = cpu_service_desc;
		this.cpu_service_starttime = cpu_service_starttime;
		this.cpu_service_endtime = cpu_service_endtime;
		this.cpu_service_state = cpu_service_state;
		this.cpu_service_creationtime = cpu_service_creationtime;
		this.cpu_service_modifiedtime = cpu_service_modifiedtime;
		this.cpu_id = cpu_id;
		this.user_id = user_id;
	}
	
}
