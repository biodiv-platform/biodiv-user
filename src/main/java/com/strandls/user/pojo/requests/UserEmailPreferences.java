package com.strandls.user.pojo.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEmailPreferences {

	private Long id;
	private Boolean hideEmial;
	private Boolean sendNotification;
	private Boolean identificationMail;
	private Boolean sendPushNotification;

	public UserEmailPreferences() {
		super();
	}

	/**
	 * @param id
	 * @param hideEmial
	 * @param sendNotification
	 * @param identificationMail
	 * @param sendPushNotification
	 */
	public UserEmailPreferences(Long id, Boolean hideEmial, Boolean sendNotification, Boolean identificationMail,
			Boolean sendPushNotification) {
		super();
		this.id = id;
		this.hideEmial = hideEmial;
		this.sendNotification = sendNotification;
		this.identificationMail = identificationMail;
		this.sendPushNotification = sendPushNotification;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getHideEmial() {
		return hideEmial;
	}

	public void setHideEmial(Boolean hideEmial) {
		this.hideEmial = hideEmial;
	}

	public Boolean getSendNotification() {
		return sendNotification;
	}

	public void setSendNotification(Boolean sendNotification) {
		this.sendNotification = sendNotification;
	}

	public Boolean getIdentificationMail() {
		return identificationMail;
	}

	public void setIdentificationMail(Boolean identificationMail) {
		this.identificationMail = identificationMail;
	}

	public Boolean getSendPushNotification() {
		return sendPushNotification;
	}

	public void setSendPushNotification(Boolean sendPushNotification) {
		this.sendPushNotification = sendPushNotification;
	}
}
