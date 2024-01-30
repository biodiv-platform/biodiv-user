package com.strandls.user;

public class Constants {

	private Constants() {
	}

	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String VERIFICATION_REQUIRED = "verificationRequired";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String BA_TOKEN = "BAToken";
	public static final String BR_TOKEN = "BRToken";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String NO_COOKIE = "no_cookie";

	public static final String OBSERVATION = "observation";
	public static final String SPECIES_PARTICIPATION_OBSERVATION = "species.participation.Observation";
	public static final String DOCUMENT = "document";
	public static final String CONTENT_EML_DOCUMENT = "content.eml.Document";
	public static final String SPECIES = "species";
	public static final String SPECIES_SPECIES = "species.Species";
	public static final String DATATABLE = "content.eml.Datatable";

	public enum SUCCESS_CONSTANTS {
		TOKEN_SAVED, AUTHENTICATION_SUCCESSFUL, USER_VERIFICATION_SUCCESSFUL, OTP_REGENERATED, EMAIL_SMS_SENT,
		PASSWORD_UPDATED
	}

	public enum ERROR_CONSTANTS {
		ACCOUNT_DISABLED, USERNAME_REQUIRED, PASSWORD_REQUIRED, VERIFICATION_MODE_REQUIRED, EMAIL_VERIFICATION_FAILED,
		USER_NOT_FOUND, INVALID_CAPTCHA, INVALID_ACTION, ACCOUNT_LOCKED, EMAIL_MOBILE_ALREADY_EXISTS,
		COULD_NOT_CREATE_USER, VERIFIED_OR_OTP_DELETED, OTP_ATTEMPTS_EXCEEDED, COULD_NOT_GENERATE_OTP,
		COULD_NOT_SEND_MAIL_SMS, USER_DELETED, OTP_DELETED, INVALID_OR_EXPIRED_OTP, INVALID_PASSWORD
	}
}
