package com.strandls.user.pojo;

public class UserMappingList {
	private User user;
	private UserUgRoleMapping[] usergroup;
	private UserTaxonRoleMapping[] taxonomy; 

	/**
	 * 
	 * @param user
	 * @param usergroup
	 * @param taxonomy
	 */
	public UserMappingList(User user, UserUgRoleMapping[] usergroup, UserTaxonRoleMapping[] taxonomy) {
		super();
		this.user = user;
		this.usergroup = usergroup;
		this.taxonomy = taxonomy;
	}

	public UserMappingList() {
		super();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public UserUgRoleMapping[] getUsergroup() {
		return usergroup;
	}

	public void setUsergroup(UserUgRoleMapping[] usergroup) {
		this.usergroup = usergroup;
	}

	public UserTaxonRoleMapping[] getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(UserTaxonRoleMapping[] taxonomy) {
		this.taxonomy = taxonomy;
	}

}