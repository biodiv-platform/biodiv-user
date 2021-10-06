package com.strandls.user.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserMappingList {
	private Long id;
	private User user;
	private List<UserUgRoleMapping> userGroup;
	private List<UserTaxonRoleMapping> taxonomy;
	private UserLocationInfo locationInformation;

	/**
	 * 
	 * @param id
	 * @param user
	 * @param userGroup
	 * @param taxonomy
	 * @param locationInformation
	 */
	public UserMappingList(Long id, User user,  List<UserUgRoleMapping> userGroup, List<UserTaxonRoleMapping> taxonomy,
			UserLocationInfo locationInformation) {
		super();
		this.id = id;
		this.user = user;
		this.userGroup = userGroup;
		this.taxonomy = taxonomy;
		this.locationInformation = locationInformation;
	}

	public UserMappingList() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@JsonProperty("userGroup")
	public List<UserUgRoleMapping> getUsergroup() {
		return userGroup;
	}

	public void setUsergroup(List<UserUgRoleMapping> userGroup) {
		this.userGroup = userGroup;
	}

	public List<UserTaxonRoleMapping> getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(List<UserTaxonRoleMapping> taxonomy) {
		this.taxonomy = taxonomy;
	}

	public UserLocationInfo getLocationInformation() {
		return locationInformation;
	}

	public void setLocationInformation(UserLocationInfo locationInformation) {
		this.locationInformation = locationInformation;
	}

}