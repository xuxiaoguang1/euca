package com.eucalyptus.webui.server.stat;

import java.util.Date;

public class HistoryAction {
	
	public static final String DATE_PATTERN = "yyyy-MM-dd-HH-mm-ss";

	private int id;
	private int action;
	private String reason;
	private Date date;
	private int userID;
	private int vmID;

	/**
	 * 
	 * @param id
	 * @param action 0:stop, 1:start
	 * @param reason some string
	 * @param date
	 * @param userID
	 * @param vmID
	 */
	public HistoryAction(int id, int action, String reason, Date date,
			int userID, int vmID) {
		this.id = id;
		this.action = action;
		this.reason = reason;
		this.date = date;
		this.userID = userID;
		this.vmID = vmID;
	}
	
	/**
	 * 
	 * @param action 0:stop, 1:start
	 * @param reason some string
	 * @param date
	 * @param userID
	 * @param vmID
	 */
	public HistoryAction(int action, String reason, Date date,
			int userID, int vmID) {
		this.action = action;
		this.reason = reason;
		this.date = date;
		this.userID = userID;
		this.vmID = vmID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int getVmID() {
		return vmID;
	}

	public void setVmID(int vmID) {
		this.vmID = vmID;
	}

}
