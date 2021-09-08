package com.strandls.user.es.utils;

public enum UserIndex {
	INDEX("extended_user"), TYPE("_doc"), 
	CREATEDON("document.createdOn"),
	LASTLOGGEDIN("document.lastRevised"),
	ROLE("user.role"),
	USER("document.authorId"), USERGROUPID("userGroupIbp.id");

	private String field;

	private UserIndex(String field) {
		this.field = field;
	}

	public String getValue() {
		return field;
	}
}