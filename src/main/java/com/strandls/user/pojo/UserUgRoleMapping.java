package com.strandls.user.pojo;

import java.util.List;

public class UserUgRoleMapping extends RoleMapping {
	private List<Long> usergroupids;

	/**
	 * 
	 * @param userid
	 * @param roleid
	 * @param role
	 * @param usergroupids
	 */
	public UserUgRoleMapping(Long userid, Long roleid, String role, List<Long> usergroupids) {
		super(userid, roleid, role);
		this.usergroupids = usergroupids;
	}

	public UserUgRoleMapping() {
		super();
	}

	public List<Long> getUsergroupids() {
		return usergroupids;
	}

	public void setUsergroupids(List<Long> usergroupids) {
		this.usergroupids = usergroupids;
	}

}