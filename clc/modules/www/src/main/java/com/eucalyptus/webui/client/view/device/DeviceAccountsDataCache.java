package com.eucalyptus.webui.client.view.device;

import java.util.ArrayList;
import java.util.List;

public class DeviceAccountsDataCache {

	List<String> keyList;

	List<List<String>> valueList;

	public void setKey(List<String> keyList) {
		this.keyList = keyList;
		valueList = new ArrayList<List<String>>();
		for (int i = 0; i < keyList.size(); i ++) {
			valueList.add(null);
		}
	}

	public boolean setValue(String key, List<String> value) {
		int index = keyList.indexOf(key);
		if (index != -1) {
			valueList.set(index, value);
			return true;
		}
		return false;
	}

	public List<String> getAccounts() {
		return keyList;
	}

	public List<String> getUsersByAccount(String account) {
		int index;
		if (keyList != null && (index = keyList.indexOf(account)) != -1) {
			return valueList.get(index);
		}
		return null;
	}

}
