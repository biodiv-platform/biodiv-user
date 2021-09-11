package com.strandls.user.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserMappingList {
	private Long id;
	private User user;
	private UserUgRoleMapping[] userGroup;
	private UserTaxonRoleMapping[] taxonomy;
	private Object locationInformation;

	/**
	 * 
	 * @param id
	 * @param user
	 * @param userGroup
	 * @param taxonomy
	 * @param locationInformation
	 */
	public UserMappingList(Long id, User user, UserUgRoleMapping[] userGroup, UserTaxonRoleMapping[] taxonomy,
			Object locationInformation) {
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
	public UserUgRoleMapping[] getUsergroup() {
		return userGroup;
	}

	public void setUsergroup(UserUgRoleMapping[] userGroup) {
		this.userGroup = userGroup;
	}

	public UserTaxonRoleMapping[] getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(UserTaxonRoleMapping[] taxonomy) {
		this.taxonomy = taxonomy;
	}

	public Object getLocationInformation() {
		return locationInformation;
	}

	public void setLocationInformation(Object locationInformation) {
		this.locationInformation = locationInformation;
	}

}