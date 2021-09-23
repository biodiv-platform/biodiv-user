package com.strandls.user.pojo;

import java.util.Map;

public class MapAggregationResponse {

	private Map<String, Long> role;
	private Map<String, Long> userGroup;
	private Map<String, Long> institution;
	private Map<String, Long> profession;
	private Map<String, Long> sex;

	public Map<String, Long> getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(Map<String, Long> userGroup) {
		this.userGroup = userGroup;
	}

	public Map<String, Long> getInstitution() {
		return institution;
	}

	public void setInstitution(Map<String, Long> institution) {
		this.institution = institution;
	}

	public Map<String, Long> getProfession() {
		return profession;
	}

	public void setProfession(Map<String, Long> profession) {
		this.profession = profession;
	}

	public Map<String, Long> getRole() {
		return role;
	}

	public void setRole(Map<String, Long> role) {
		this.role = role;
	}

	public Map<String, Long> getSex() {
		return sex;
	}

	public void setSex(Map<String, Long> sex) {
		this.sex = sex;
	}

}
