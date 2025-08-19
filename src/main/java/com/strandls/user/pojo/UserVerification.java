package com.strandls.user.pojo;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "suser_verification")
public class UserVerification implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6964237746715122153L;

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "otp")
	private String otp;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "timeout")
	private Long timeout;

	@Column(name = "creation_date")
	private Date creationDate;

	@Column(name = "verification_id")
	private String verificationId;

	@Column(name = "verification_type")
	private String verificationType;

	@Column(name = "no_of_attempts", columnDefinition = "integer default 0")
	private Integer noOfAttempts;

	@Column(name = "action")
	private String action;

	public UserVerification() {
		// TODO Auto-generated constructor stub
	}

	public UserVerification(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public Date getDate() {
		return creationDate;
	}

	public void setDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getVerificationId() {
		return verificationId;
	}

	public void setVerificationId(String verificationId) {
		this.verificationId = verificationId;
	}

	public String getVerificationType() {
		return verificationType;
	}

	public void setVerificationType(String verificationType) {
		this.verificationType = verificationType;
	}

	public Integer getNoOfAttempts() {
		return noOfAttempts;
	}

	public void setNoOfAttempts(Integer noOfAttempts) {
		this.noOfAttempts = noOfAttempts;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
