/**
 * 
 */
package com.strandls.user;

/**
 * @author Abhishek Rudra
 *
 */
public class ApiConstants {

	private ApiConstants() {
	}

	// ------------VERSIONING-------------
	public static final String V1 = "/v1";

	// ------------CONTROLLER PATH-----------

	public static final String USER = "/user";
	public static final String PING = "/ping";
	public static final String IBP = "/ibp";
	public static final String AUTHENTICATE = "/authenticate";
	public static final String USERS = "/users";
	public static final String ME = "/me";
	public static final String LOGIN = "/login";
	public static final String SIGNUP = "/signup";
	public static final String REFRESH_TOKENS = "/refresh-tokens";
	public static final String VALIDATE_TOKEN = "/validate-token";
	public static final String GOOGLE = "/google";
	public static final String PERMISSIONS = "/permissions";
	public static final String GOOGLE_CALLBACK = "/oauth2callback"; // Change redirection URL in Google Console
	// for Biodiv Password
	public static final String VALIDATE = "/validate";
	public static final String VERIFICATION_CONFIG = "/verification-config";
	public static final String REGENERATE_OTP = "/regenerate-otp";
	public static final String FORGOT_PASSWORD = "/forgot-password";
	public static final String RESET_PASSWORD = "/reset-password";
	public static final String CHANGE_PASSWORD = "/change-password";
	public static final String FOLLOW = "/follow";
	public static final String OBJECTFOLLOW = "/objectfollow";
	public static final String USERFOLLOW = "/userfollow";
	public static final String UNFOLLOW = "/unfollow";
	public static final String GROUPMEMBER = "/groupMember";
	public static final String AUTOCOMPLETE = "/autocomplete";
	public static final String RECIPIENTS = "/recipients";
	public static final String SAVE_TOKEN = "/save-token";
	public static final String COUNT = "/count";
	public static final String ADD = "/add";
	public static final String REMOVE = "/remove";
	public static final String LEAVE = "/leave";
	public static final String GROUP = "/group";
	public static final String CHECK = "/check";
	public static final String JOIN = "/join";
	public static final String MODERATOR = "/moderator";
	public static final String DIRECT = "/direct";
	public static final String FOUNDERLIST = "/founderList";
	public static final String MODERATORLIST = "/moderatorList";
	public static final String BULK = "/bulk";
	public static final String SEND_NOTIFICATION = "/send-notification";

	public static final String UPDATE = "/update";
	public static final String IMAGE = "/image";
	public static final String DETAILS = "/details";
	public static final String EMAIL_PREFERENCES = "/emailPreferences";
	public static final String ROLES = "/roles";

	public static final String DELETE = "/delete";
	public static final String ADMIN = "/admin";

}
