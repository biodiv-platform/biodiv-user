package com.strandls.user.pojo;

public class UserUgRoleMapping extends RoleMapping {
	private Long[] usergroupids;

	/**
	 * 
	 * @param userid
	 * @param roleid
	 * @param role
	 * @param usergroupids
	 */
	public UserUgRoleMapping(Long userid, Long roleid, String role, Long[] usergroupids) {
		super(userid, roleid, role);
		this.usergroupids = usergroupids;
	}

	public UserUgRoleMapping() {
		super();
	}

	public Long[] getUsergroupids() {
		return usergroupids;
	}

	public void setUsergroupids(Long[] usergroupids) {
		this.usergroupids = usergroupids;
	}

}