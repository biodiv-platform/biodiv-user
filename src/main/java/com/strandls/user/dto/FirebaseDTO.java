package com.strandls.user.dto;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement
@JsonInclude(value = Include.NON_NULL)
public class FirebaseDTO {
	private String token;
	private String title;
	private String body;
	private String icon;
	private String clickAction;

	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getClickAction() {
		return clickAction;
	}
	public void setClickAction(String clickAction) {
		this.clickAction = clickAction;
	}

}
