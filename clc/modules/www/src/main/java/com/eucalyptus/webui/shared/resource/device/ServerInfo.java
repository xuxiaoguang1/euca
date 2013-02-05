package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;

public class ServerInfo implements Serializable {

	private static final long serialVersionUID = 5617587106385282267L;
	
	public int server_id;
	public String server_name;
	public String server_desc;
	public String server_ip;
	public int server_bw;
	public ServerState server_state;
	public Date server_creationtime;
	public Date server_modifiedtime;
	public int cabinet_id;
	
	public ServerInfo() {
	    /* do nothing */
	}
	
	public ServerInfo(int server_id, String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state, Date server_creationtime, Date server_modifiedtime, int cabinet_id) {
		this.server_id = server_id;
		this.server_name = server_name;
		this.server_desc = server_desc;
		this.server_ip = server_ip;
		this.server_bw = server_bw;
		this.server_state = server_state;
		this.server_creationtime = server_creationtime;
		this.server_modifiedtime = server_modifiedtime;
		this.cabinet_id = cabinet_id;
	}

}
