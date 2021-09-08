package com.strandls.user.pojo;

public class UserUgRoleMapping {
	private Long userid;
	private Long roleid;
	private String role;
	private Long[] usergroupids;

	/**
	 * 
	 * @param userid
	 * @param roleid
	 * @param role
	 * @param usergroupids
	 */
	public UserUgRoleMapping(Long userid, Long roleid, String role, Long[] usergroupids) {
		super();
		this.userid = userid;
		this.roleid = roleid;
		this.role = role;
		this.usergroupids = usergroupids;
	}

	public UserUgRoleMapping() {
		super();
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public Long getRoleid() {
		return roleid;
	}

	public void setRoleid(Long roleid) {
		this.roleid = roleid;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Long[] getUsergroupids() {
		return usergroupids;
	}

	public void setUsergroupids(Long[] usergroupids) {
		this.usergroupids = usergroupids;
	}

}