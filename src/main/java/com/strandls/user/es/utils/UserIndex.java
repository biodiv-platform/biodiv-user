package com.strandls.user.es.utils;

public enum UserIndex {
	INDEX("extended_user"), TYPE("_doc"), 
	NAME("user.name"),
	EMAIL("user.email"),
	USERNAME("user.userName"),
	PHONE("user.mobileNumber"),
	INSTITUTION("user.institution"),
	INSTITUTION_KEYWORD("user.institution.keyword"),
	SEX("user.sexType"),
	OCCUPATION("user.occupation"),
	OCCUPATION_KEYWORD("user.occupation.keyword"),
	CREATEDON("user.dateCreated"),
	LOCATION("locationInformation.location"),
	LASTLOGGEDIN("user.lastLoginDate"),
	ROLE("userGroup.role"),
	ROLE_KEYWORD("userGroup.role.keyword"),
	USER("user.id"), USERGROUPID("userGroup.usergroupids");

	private String field;

	private UserIndex(String field) {
		this.field = field;
	}

	public String getValue() {
		return field;
	}
}