package com.emagroup.sdk;

import java.io.Serializable;

public class UserLoginInfoBean implements Serializable {

	private String username;
	private String sid;
	private String uuid;
	private long lastLoginTime;
	private boolean anlaiye;//标记是否是俺来账号 

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public boolean isAnlaiye() {
		return anlaiye;
	}

	public void setAnlaiye(boolean anlaiye) {
		this.anlaiye = anlaiye;
	}

}
