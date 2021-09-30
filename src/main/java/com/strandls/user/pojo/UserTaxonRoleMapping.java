package com.strandls.user.pojo;

public class UserTaxonRoleMapping extends RoleMapping {

	private Long[] taxonomyids;

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
	public UserTaxonRoleMapping(Long userid, Long roleid, String role, Long[] taxonomyids) {
		super(userid, roleid, role);
		this.taxonomyids = taxonomyids;
	}

	public Long[] getTaxonomyids() {
		return taxonomyids;
	}

	public void setTaxonomyids(Long[] taxonomyids) {
		this.taxonomyids = taxonomyids;
	}

}