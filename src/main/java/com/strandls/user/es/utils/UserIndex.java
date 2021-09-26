package com.strandls.user.es.utils;

public enum UserIndex {
	INDEX("extended_user"), TYPE("_doc"), 
	EMAIL_KEYWORD("user.email.keyword"),
	USERNAME_KEYWORD("user.userName.keyword"),
	PHONE("user.mobileNumber"),
	INSTITUTION_KEYWORD("user.institution.keyword"),
	SEX_KEYWORD("user.sexType.keyword"),
	OCCUPATION_KEYWORD("user.occupation.keyword"),
	CREATEDON("user.dateCreated"),
	LOCATION("locationInformation.location"),
	LASTLOGGEDIN("user.lastLoginDate"),
	ROLE_KEYWORD("userGroup.role.keyword"),
	ROLE_KEYWORD_NESTED("nested.userGroup.role.keyword"),
	USER("user.id"), USERGROUPID_NESTED("nested.userGroup.usergroupids"),
	USERGROUPID("userGroup.usergroupids"),
	USER_GROUP_NESTED_PATH("userGroup");

	private String field;

	private UserIndex(String field) {
		this.field = field;
	}

	public String getValue() {
		return field;
	}
}