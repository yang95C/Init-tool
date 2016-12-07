package com.yxh.init_tool;

public class Contact {
	private int id;
	private String UserName;
	private String Mobilephone;
	private int status;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getMobilephone() {
		return Mobilephone;
	}

	public void setMobilephone(String mobilephone) {
		Mobilephone = mobilephone;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Contact [UserName：" + UserName + "，Mobilephone：" + Mobilephone
				+ "]";
	}

}
