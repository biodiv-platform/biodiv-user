package com.strandls.user.pojo;

public class UserLocationInfo {
	private String placeName;
	private Location location;

	public UserLocationInfo() {
		super();
	}

	public UserLocationInfo(String placeName, Location location) {
		super();
		this.placeName = placeName;
		this.location = location;
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}
