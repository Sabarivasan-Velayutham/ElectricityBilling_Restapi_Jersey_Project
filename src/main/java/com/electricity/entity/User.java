package com.electricity.entity;

public class User {
	private int id;
	private String username;
	private String password;
	private String address;
	private double billAmount;
	private String billStatus;

	private String email;
	private String phoneNo;
	private String city;
	private String state;
	private String pincode;

	public User() {

	}

	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public User(int id, String username, String password, String address, double billAmount, String billStatus) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.address = address;
		this.billAmount = billAmount;
		this.billStatus = billStatus;
	}

	public User(String username, String password, String address, double billAmount, String billStatus) {
		super();
		this.username = username;
		this.password = password;
		this.address = address;
		this.billAmount = billAmount;
		this.billStatus = billStatus;
	}

	public User(String username, String password, String address, double billAmount, String billStatus,
			String email, String phoneNo, String city, String state, String pincode) {
		super();
		this.username = username;
		this.password = password;
		this.address = address;
		this.billAmount = billAmount;
		this.billStatus = billStatus;
		this.email = email;
		this.phoneNo = phoneNo;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
	}
	
	public User(int id, String username, String password, String address, double billAmount, String billStatus,
			String email, String phoneNo, String city, String state, String pincode) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.address = address;
		this.billAmount = billAmount;
		this.billStatus = billStatus;
		this.email = email;
		this.phoneNo = phoneNo;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public double getBillAmount() {
		return billAmount;
	}

	public void setBillAmount(double billAmount) {
		this.billAmount = billAmount;
	}

	public String getBillStatus() {
		return billStatus;
	}

	public void setBillStatus(String billStatus) {
		this.billStatus = billStatus;
	}
}
