package com.strandls.user.pojo;

import java.util.List;

public class UserTaxonRoleMapping extends RoleMapping {

	private List<Long> taxonomyids;

	public UserTaxonRoleMapping() {
		super();
	}

	/**
	 * 
	 * @param userid
	 * @param roleid
	 * @param role
	 * @param taxonomyids
	 */
	public UserTaxonRoleMapping(Long userid, Long roleid, String role, List<Long> taxonomyids) {
		super(userid, roleid, role);
		this.taxonomyids = taxonomyids;
	}

	public List<Long> getTaxonomyids() {
		return taxonomyids;
	}

	public void setTaxonomyids(List<Long> taxonomyids) {
		this.taxonomyids = taxonomyids;
	}

}