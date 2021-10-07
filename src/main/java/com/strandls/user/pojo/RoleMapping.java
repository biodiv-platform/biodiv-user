package com.strandls.user.pojo;

public class RoleMapping {

	private Long userid;
	private Long roleid;
	private String role;

	public RoleMapping() {
		super();
	}

	public RoleMapping(Long userid, Long roleid, String role) {
		super();
		this.userid = userid;
		this.roleid = roleid;
		this.role = role;
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

}
