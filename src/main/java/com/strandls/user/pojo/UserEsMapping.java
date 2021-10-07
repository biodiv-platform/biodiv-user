package com.strandls.user.pojo;

import java.util.Date;

public class UserEsMapping {

	private Long id;
	private String name;
	private String profilePic;
	private String institution;
	private Date lastLoginDate;
	private String occupation;
	private Boolean accountLocked;
	private Long languageId;
	private Boolean sendDigest;
	private Boolean accountExpired;
	private String location;
	private Date dateCreated;
	private String mobileNumber;
	private Boolean allowEmailNotification;
	private String sexType;
	private Boolean sendPushNotification;
	private String userName;
	private String aboutMe;
	private Boolean hideEmial;
	private String email;

	public UserEsMapping() {
		super();
	}

	public UserEsMapping(Long id, String name, String profilePic, String institution, Date lastLoginDate,
			String occupation, Boolean accountLocked, Long languageId, Boolean sendDigest, Boolean accountExpired,
			String location, Date dateCreated, String mobileNumber, Boolean allowEmailNotification, String sexType,
			Boolean sendPushNotification, String userName, String aboutMe, Boolean hideEmial, String email) {
		super();
		this.id = id;
		this.name = name;
		this.profilePic = profilePic;
		this.institution = institution;
		this.lastLoginDate = lastLoginDate;
		this.occupation = occupation;
		this.accountLocked = accountLocked;
		this.languageId = languageId;
		this.sendDigest = sendDigest;
		this.accountExpired = accountExpired;
		this.location = location;
		this.dateCreated = dateCreated;
		this.mobileNumber = mobileNumber;
		this.allowEmailNotification = allowEmailNotification;
		this.sexType = sexType;
		this.sendPushNotification = sendPushNotification;
		this.userName = userName;
		this.aboutMe = aboutMe;
		this.hideEmial = hideEmial;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public Boolean getAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(Boolean accountLocked) {
		this.accountLocked = accountLocked;
	}

	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	public Boolean getSendDigest() {
		return sendDigest;
	}

	public void setSendDigest(Boolean sendDigest) {
		this.sendDigest = sendDigest;
	}

	public Boolean getAccountExpired() {
		return accountExpired;
	}

	public void setAccountExpired(Boolean accountExpired) {
		this.accountExpired = accountExpired;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public Boolean getIdentificationMail() {
		return allowEmailNotification;
	}

	public void setIdentificationMail(Boolean allowEmailNotification) {
		this.allowEmailNotification = allowEmailNotification;
	}

	public String getSexType() {
		return sexType;
	}

	public void setSexType(String sexType) {
		this.sexType = sexType;
	}

	public Boolean getSendPushNotification() {
		return sendPushNotification;
	}

	public void setSendPushNotification(Boolean sendPushNotification) {
		this.sendPushNotification = sendPushNotification;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAboutMe() {
		return aboutMe;
	}

	public void setAboutMe(String aboutMe) {
		this.aboutMe = aboutMe;
	}

	public Boolean getHideEmial() {
		return hideEmial;
	}

	public void setHideEmial(Boolean hideEmial) {
		this.hideEmial = hideEmial;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
