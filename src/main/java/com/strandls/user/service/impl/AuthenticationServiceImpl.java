package com.strandls.user.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.user.Constants;
import com.strandls.user.Constants.ERROR_CONSTANTS;
import com.strandls.user.Constants.SUCCESS_CONSTANTS;
import com.strandls.user.converter.UserConverter;
import com.strandls.user.dao.UserDao;
import com.strandls.user.dto.UserDTO;
import com.strandls.user.exception.InvalidVerificationTypeException;
import com.strandls.user.pojo.Language;
import com.strandls.user.pojo.User;
import com.strandls.user.pojo.UserVerification;
import com.strandls.user.pojo.requests.UserPasswordChange;
import com.strandls.user.service.AuthenticationService;
import com.strandls.user.service.LanguageService;
import com.strandls.user.service.MailService;
import com.strandls.user.service.RoleService;
import com.strandls.user.service.SMSService;
import com.strandls.user.service.UserService;
import com.strandls.user.service.UserVerificationService;
import com.strandls.user.util.AppUtil;
import com.strandls.user.util.AppUtil.VERIFICATION_ACTIONS;
import com.strandls.user.util.AppUtil.VERIFICATION_TYPE;
import com.strandls.user.util.AuthUtility;
import com.strandls.user.util.JWTUtil;
import com.strandls.user.util.MessageDigestPasswordEncoder;
import com.strandls.user.util.PropertyFileUtil;
import com.strandls.user.util.SimpleUsernamePasswordAuthenticator;
import com.strandls.user.util.ValidationUtil;

import net.minidev.json.JSONArray;

public class AuthenticationServiceImpl implements AuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	@Inject
	private UserService userService;

	@Inject
	private UserDao userDao;

	@Inject
	private SimpleUsernamePasswordAuthenticator usernamePasswordAuthenticator;

	@Inject
	private RoleService roleService;

	@Inject
	private MailService mailService;

	@Inject
	private SMSService smsService;

	@Inject
	private LanguageService languageService;

	@Inject
	private UserVerificationService verificationService;

	@Override
	public Map<String, Object> authenticateUser(String userEmail, String password) {
		Map<String, Object> tokens = new HashMap<>();
		User user = userService.getUserByEmailOrMobile(userEmail);
		if (user == null) {
			return AppUtil.generateResponse(false, ERROR_CONSTANTS.USER_NOT_FOUND);
		}
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userEmail, password);
		usernamePasswordAuthenticator.validate(credentials, null);
		CommonProfile profile = credentials.getUserProfile();
		user = this.userService.fetchUser(Long.parseLong(profile.getId()));
		if (user.getEnabled().booleanValue()) {
			if (!user.getAccountLocked().booleanValue()) {
				tokens = this.buildTokens(profile, user, true);
				tokens.put(Constants.STATUS, true);
				tokens.put(Constants.MESSAGE, SUCCESS_CONSTANTS.AUTHENTICATION_SUCCESSFUL.toString());
				tokens.put(Constants.VERIFICATION_REQUIRED, false);
			} else {
				tokens.put(Constants.STATUS, true);
				tokens.put(Constants.MESSAGE, ERROR_CONSTANTS.ACCOUNT_LOCKED.toString());
				tokens.put("user", UserConverter.convertToDTO(user));
				tokens.put(Constants.VERIFICATION_REQUIRED, true);
			}
		} else {
			tokens.put(Constants.STATUS, false);
			tokens.put(Constants.MESSAGE, ERROR_CONSTANTS.ACCOUNT_DISABLED.toString());
		}
		return tokens;
	}

	@Override
	public Map<String, Object> buildTokens(CommonProfile profile, User user, boolean getRefreshToken) {
		Map<String, Object> response = new HashMap<>();
		try {
			String accessToken = generateAccessToken(profile, user);
			response.put("access_token", accessToken);
			response.put("token_type", "bearer");
			response.put("timeout", JWTUtil.getAccessTokenExpiryDate());

			if (getRefreshToken) {
				String refreshToken = generateRefreshToken(profile, user);
				response.put("refresh_token", refreshToken);
			}

			user.setLastLoginDate(new Date());
			userService.updateUser(user);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return response;
	}

	private String generateAccessToken(CommonProfile profile, User user) {
		JwtGenerator<CommonProfile> generator = new JwtGenerator<>(
				new SecretSignatureConfiguration(PropertyFileUtil.fetchProperty("config.properties", "jwtSalt")));

		Set<String> roles = new HashSet<>();
		if (user.getRoles() != null) {
			user.getRoles().forEach(role -> roles.add(role.getAuthority()));
		}

		Map<String, Object> jwtClaims = new HashMap<>();
		jwtClaims.put("id", profile.getId());
		jwtClaims.put(JwtClaims.SUBJECT, profile.getId() + "");
		jwtClaims.put(Pac4jConstants.USERNAME, profile.getUsername());
		jwtClaims.put(CommonProfileDefinition.EMAIL,
				(profile.getEmail() == null || profile.getEmail().isEmpty()) ? "" : profile.getEmail());
		jwtClaims.put(JwtClaims.EXPIRATION_TIME, JWTUtil.getAccessTokenExpiryDate());
		jwtClaims.put(JwtClaims.ISSUED_AT, new Date());
		jwtClaims.put("roles", roles);
		jwtClaims.put("name", user.getName());
		jwtClaims.put("profile_pic", user.getProfilePic());
		return generator.generate(jwtClaims);
	}

	private String generateRefreshToken(CommonProfile profile, User user) {
		JwtGenerator<CommonProfile> generator = new JwtGenerator<>(
				new SecretSignatureConfiguration(PropertyFileUtil.fetchProperty("config.properties", "jwtSalt")));

		Set<String> roles = new HashSet<>();
		if (user.getRoles() != null) {
			user.getRoles().forEach(role -> roles.add(role.getAuthority()));
		}

		Map<String, Object> jwtClaims = new HashMap<>();
		jwtClaims.put("id", profile.getId());
		jwtClaims.put(JwtClaims.SUBJECT, profile.getId() + "");
		jwtClaims.put(Pac4jConstants.USERNAME, profile.getUsername());
		jwtClaims.put(CommonProfileDefinition.EMAIL,
				(profile.getEmail() == null || profile.getEmail().isEmpty()) ? "" : profile.getEmail());
		jwtClaims.put(JwtClaims.EXPIRATION_TIME, JWTUtil.getRefreshTokenExpiryDate());
		jwtClaims.put(JwtClaims.ISSUED_AT, new Date());
		jwtClaims.put("roles", roles);
		jwtClaims.put("name", user.getName());
		jwtClaims.put("profile_pic", user.getProfilePic());
		return generator.generate(jwtClaims);
	}

	@Override
	public Map<String, Object> addUser(HttpServletRequest request, UserDTO userDTO, String type) {
		Map<String, Object> response = new HashMap<>();
		User user = new User();
		user.setName(userDTO.getUsername());
		user.setUserName(userDTO.getUsername());
		if (VERIFICATION_TYPE.EMAIL.toString().equalsIgnoreCase(type)) {
			user.setSendNotification(true);
			user.setEmail(userDTO.getEmail());
		} else {
			user.setSendNotification(false);
			user.setEmail(userDTO.getMobileNumber() + "@ibp.org");
			user.setMobileNumber(userDTO.getMobileNumber());
		}
		MessageDigestPasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("MD5");
		user.setPassword(passwordEncoder.encodePassword(userDTO.getPassword(), null));
		user.setLocation(userDTO.getLocation());
		user.setLatitude(userDTO.getLatitude());
		user.setLongitude(userDTO.getLongitude());
		user.setOccupation(userDTO.getProfession());
		user.setInstitution(userDTO.getInstitution());
		user.setSexType(userDTO.getGender());
		user.setDateCreated(new Date());
		user.setHideEmial(true);
		user.setEnabled(true);
		user.setAccountExpired(false);
		user.setSendDigest(true);
		user.setAccountLocked(true);
		user.setPasswordExpired(false);
		user.setTimezone(0F);
		user.setSendPushNotification(false);
		user.setIdentificationMail(true);
		user.setIsDeleted(false);
		try {
			Locale locale = request.getLocale();
			Language language = languageService.getLanguageByTwoLetterCode(locale.getLanguage());
			user.setLanguageId(language.getId());

			user.setVersion(0L);
			User existingUser = null;
			String verificationType = AppUtil.getVerificationType(userDTO.getVerificationType());
			switch (verificationType) {
			case "EMAIL":
				existingUser = userService.getUserByEmail(userDTO.getEmail());
				break;
			case "MOBILE":
				existingUser = userService.getUserByMobile(userDTO.getMobileNumber());
				break;
			default:
				logger.debug("Invalid Verification Type");
				throw new InvalidVerificationTypeException("Invalid Verification Type");
			}
			if (existingUser != null) {
				response.put(Constants.STATUS, false);
				response.put(Constants.MESSAGE, ERROR_CONSTANTS.EMAIL_MOBILE_ALREADY_EXISTS.toString());
				return response;
			}
			user = userDao.save(user);
			userService.esUserUpdate(user, false);
			boolean isManual = userDTO.getMode().equalsIgnoreCase(AppUtil.AUTH_MODE.MANUAL.getAction());
			if (!isManual) {
				user.setAccountLocked(false);
				user.setRoles(roleService.setDefaultRoles(AuthUtility.getDefaultRoles()));
				user = userDao.update(user);

				CommonProfile profile = AuthUtility.createUserProfile(user);
				response = this.buildTokens(profile, user, true);
				response.put(Constants.STATUS, true);
				response.put(Constants.VERIFICATION_REQUIRED, false);
				response.put(Constants.MESSAGE, SUCCESS_CONSTANTS.USER_VERIFICATION_SUCCESSFUL.toString());
				mailService.sendWelcomeMail(request, user);
				return response;
			}
			response.put(Constants.STATUS, true);
			response.put(Constants.VERIFICATION_REQUIRED, true);
			response.put(Constants.MESSAGE, "User created successfully");
			response.put("user", UserConverter.convertToDTO(user));
			String otp = AppUtil.generateOTP();
			if (verificationType.equalsIgnoreCase("EMAIL")) {
				verificationService.saveOtp(user.getId(), otp, VERIFICATION_TYPE.EMAIL.toString(), user.getEmail(),
						VERIFICATION_ACTIONS.USER_REGISTRATION.toString());
				mailService.sendActivationMail(request, user, otp);
			} else if (verificationType.equalsIgnoreCase("MOBILE")) {
				verificationService.saveOtp(user.getId(), otp, VERIFICATION_TYPE.MOBILE.toString(),
						user.getMobileNumber(), VERIFICATION_ACTIONS.USER_REGISTRATION.toString());
				smsService.sendSMS(user.getMobileNumber(), otp);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			response.put(Constants.STATUS, false);
			response.put(Constants.MESSAGE, ERROR_CONSTANTS.COULD_NOT_CREATE_USER);
		}
		return response;
	}

	@Override
	public Map<String, Object> validateUser(HttpServletRequest request, Long id, String otp) {
		Map<String, Object> result = new HashMap<>();
		User user = null;
		UserVerification verification = verificationService.getUserVerificationDetails(id,
				VERIFICATION_ACTIONS.USER_REGISTRATION.toString());
		if (verification == null) {
			logger.debug("Account already verified or otp deleted");
			result.put(Constants.STATUS, false);
			result.put(Constants.MESSAGE, ERROR_CONSTANTS.VERIFIED_OR_OTP_DELETED.toString());
			return result;
		}
		try {
			long time = verification.getDate().getTime() + verification.getTimeout().longValue();
			boolean validOTP = new Date(time).compareTo(new Date()) > 0;
			if (validOTP && verification.getOtp().equals(otp)) {
				user = userService.fetchUser(id);
				user.setAccountLocked(false);
				user.setRoles(roleService.setDefaultRoles(AuthUtility.getDefaultRoles()));
				user = userDao.update(user);

				userService.esUserUpdate(user, true);
				CommonProfile profile = AuthUtility.createUserProfile(user);
				result = this.buildTokens(profile, user, true);
				result.put(Constants.STATUS, true);
				result.put(Constants.MESSAGE, SUCCESS_CONSTANTS.USER_VERIFICATION_SUCCESSFUL.toString());
				if (AppUtil.VERIFICATION_TYPE.EMAIL.toString().equalsIgnoreCase(verification.getVerificationType())) {
					mailService.sendWelcomeMail(request, user);
				} else {
					smsService.sendSMS(verification.getVerificationId(), "Welcome to IBP");
				}
				verificationService.deleteOtp(verification.getId());
			} else {
				// Invalid OTP
				result.put(Constants.STATUS, false);
				result.put(Constants.MESSAGE, ERROR_CONSTANTS.INVALID_OR_EXPIRED_OTP.toString());
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return result;
	}

	@Override
	public Map<String, Object> regenerateOTP(HttpServletRequest request, Long id, int action) {
		Map<String, Object> data = new HashMap<>();
		try {
			String act = AppUtil.getVerificationAction(action);
			if (act == null) {
				data.put(Constants.STATUS, false);
				data.put(Constants.MESSAGE, ERROR_CONSTANTS.INVALID_ACTION);
				return data;
			}
			UserVerification verification = verificationService.getUserVerificationDetails(id, act);
			Integer attempts = verification.getNoOfAttempts();
			if (++attempts > 2 && Hours.hoursBetween(new DateTime(verification.getDate()), new DateTime(new Date()))
					.isLessThan(Hours.hours(24))) {
				data.put(Constants.STATUS, false);
				data.put(Constants.MESSAGE, ERROR_CONSTANTS.OTP_ATTEMPTS_EXCEEDED.toString());
				return data;
			}
			String otp = AppUtil.generateOTP();
			String verificationType = AppUtil.getVerificationType(verification.getVerificationType());
			String verificationId = verification.getVerificationId();
			verification.setOtp(otp);
			verification.setDate(new Date());
			verification.setNoOfAttempts(attempts > 2 ? 0 : attempts);

			verificationService.updateOtp(verification);
			User user = userService.fetchUser(id);
			data.put(Constants.STATUS, true);
			data.put(Constants.MESSAGE, SUCCESS_CONSTANTS.OTP_REGENERATED.toString());

			if (VERIFICATION_TYPE.EMAIL.toString().equalsIgnoreCase(verificationType)) {
				if (VERIFICATION_ACTIONS.USER_REGISTRATION.toString().equals(act)) {
					mailService.sendActivationMail(request, user, otp);
				} else {
					mailService.sendForgotPasswordMail(request, user, otp);
				}
			} else {
				smsService.sendSMS(verificationId, otp);
			}
		} catch (Exception ex) {
			data.put(Constants.STATUS, false);
			data.put(Constants.MESSAGE, ERROR_CONSTANTS.COULD_NOT_GENERATE_OTP.toString());
			logger.error(ex.getMessage());
		}
		return data;
	}

	@Override
	public Map<String, Object> forgotPassword(HttpServletRequest request, String verificationId) {
		Map<String, Object> data = new HashMap<>();
		try {
			boolean isEmail = ValidationUtil.validateEmail(verificationId);
			boolean isPhone = ValidationUtil.validatePhone(verificationId);
			UserVerification verification = verificationService.getDetailsByVerificationId(verificationId,
					VERIFICATION_ACTIONS.FORGOT_PASSWORD.toString());
			if (verification == null) {
				verification = new UserVerification();
			}
			Integer attempts = verification.getNoOfAttempts() != null ? verification.getNoOfAttempts() : 0;
			if (++attempts > 3 && Hours.hoursBetween(new DateTime(verification.getDate()), new DateTime(new Date()))
					.isLessThan(Hours.hours(24))) {
				data.put(Constants.STATUS, false);
				data.put(Constants.MESSAGE, ERROR_CONSTANTS.OTP_ATTEMPTS_EXCEEDED.toString());
				return data;
			}
			User user = userService.getUserByEmailOrMobile(verificationId);
			if (user == null) {
				logger.error("User does not exist");
				data.put(Constants.STATUS, false);
				data.put(Constants.MESSAGE, ERROR_CONSTANTS.COULD_NOT_SEND_MAIL_SMS.toString());
				return data;
			} else if (user.getIsDeleted().booleanValue()) {
				logger.error("User deleted");
				data.put("status", false);
				data.put("message", ERROR_CONSTANTS.USER_DELETED.toString());
				return data;
			}

			System.out.println("--------before otp generation----------");
			System.out.println();
			String otp = AppUtil.generateOTP();
			System.out.println();
			System.out.println("--------after otp generation---------");
			verification.setAction(VERIFICATION_ACTIONS.FORGOT_PASSWORD.toString());
			verification.setDate(new Date());
			verification.setOtp(otp);
			verification.setTimeout(24 * 60 * 60 * 1000L);
			verification.setUserId(user.getId());
			verification.setVerificationId(verificationId);
			if (isEmail) {
				verification.setVerificationType(VERIFICATION_TYPE.EMAIL.toString());
			} else if (isPhone) {
				verification.setVerificationType(VERIFICATION_TYPE.MOBILE.toString());
			}
			if (verification.getId() == null) {
				verification = verificationService.saveOtp(user.getId(), otp, verification.getVerificationType(),
						verificationId, VERIFICATION_ACTIONS.FORGOT_PASSWORD.toString());
			} else {
				verification.setNoOfAttempts(attempts > 3 ? 0 : attempts);
				verification = verificationService.updateOtp(verification);
			}
			if (verification == null)
				return null;
			data.put(Constants.STATUS, true);
			data.put(Constants.MESSAGE, SUCCESS_CONSTANTS.EMAIL_SMS_SENT.toString());
			data.put("user", UserConverter.convertToDTO(user));
			if (AppUtil.VERIFICATION_TYPE.EMAIL.toString().equalsIgnoreCase(verification.getVerificationType())) {
				mailService.sendForgotPasswordMail(request, user, otp);
			} else {
				smsService.sendSMS(verification.getVerificationId(), otp);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			data.put(Constants.STATUS, false);
			data.put(Constants.MESSAGE, ERROR_CONSTANTS.COULD_NOT_SEND_MAIL_SMS.toString());
		}
		return data;
	}

	@Override
	public Map<String, Object> resetPassword(HttpServletRequest request, Long id, String otp, String password) {
		Map<String, Object> data = new HashMap<>();
		try {
			UserVerification verification = verificationService.getUserVerificationDetails(id,
					VERIFICATION_ACTIONS.FORGOT_PASSWORD.toString());
			if (verification == null) {
				logger.error("OTP deleted");
				data.put(Constants.STATUS, false);
				data.put(Constants.MESSAGE, ERROR_CONSTANTS.OTP_DELETED.toString());
				return data;
			}
			long time = verification.getDate().getTime() + verification.getTimeout().longValue();
			boolean validOTP = new Date(time).compareTo(new Date()) > 0;
			if (validOTP && verification.getOtp().equals(otp)) {
				User user = userService.fetchUser(id);
				if (user == null) {
					logger.debug("User not found");
					data.put(Constants.STATUS, false);
					data.put(Constants.MESSAGE, ERROR_CONSTANTS.USER_NOT_FOUND.toString());
					return data;
				} else if (user.getIsDeleted().booleanValue()) {
					logger.error("User deleted");
					data.put("status", false);
					data.put("message", ERROR_CONSTANTS.USER_DELETED.toString());
					return data;
				}
				MessageDigestPasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("MD5");
				user.setPassword(passwordEncoder.encodePassword(password, null));
				userService.updateUser(user);
				verificationService.deleteOtp(verification.getId());
				data.put(Constants.STATUS, true);
				data.put(Constants.MESSAGE, SUCCESS_CONSTANTS.PASSWORD_UPDATED.toString());
			} else {
				data.put(Constants.STATUS, false);
				data.put(Constants.MESSAGE, ERROR_CONSTANTS.INVALID_OR_EXPIRED_OTP);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return data;
	}

	@Override
	public Map<String, Object> changePassword(HttpServletRequest request, UserPasswordChange inputUser) {
		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		Long userId = Long.parseLong(profile.getId());
		JSONArray userRoles = (JSONArray) profile.getAttribute("roles");
		if (!userRoles.contains("ROLE_ADMIN")) {
			inputUser.setId(userId);
		}

		Map<String, Object> data = new HashMap<>();
		if (inputUser.getId() == null) {
			logger.debug("User id not found");
			data.put(Constants.STATUS, false);
			data.put(Constants.MESSAGE, ERROR_CONSTANTS.USER_NOT_FOUND.toString());
			return data;
		}

		User user = userService.fetchUser(inputUser.getId());
		if (user == null) {
			logger.debug("User not found");
			data.put(Constants.STATUS, false);
			data.put(Constants.MESSAGE, ERROR_CONSTANTS.USER_NOT_FOUND.toString());
			return data;
		} else if (user.getIsDeleted().booleanValue()) {
			logger.error("User deleted");
			data.put("status", false);
			data.put("message", ERROR_CONSTANTS.USER_DELETED.toString());
			return data;
		}

		MessageDigestPasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("MD5");
		String encodedPassword = null;
		if (!userRoles.contains("ROLE_ADMIN")) {
			encodedPassword = passwordEncoder.encodePassword(inputUser.getOldPassword(), null);
			if (!encodedPassword.equals(user.getPassword())) {
				logger.debug("Incorrect old password");
				data.put(Constants.STATUS, false);
				data.put(Constants.MESSAGE, ERROR_CONSTANTS.INVALID_PASSWORD.toString());
				return data;
			}
		}

		encodedPassword = passwordEncoder.encodePassword(inputUser.getNewPassword(), null);
		user.setPassword(encodedPassword);
		userService.updateUser(user);

		data.put(Constants.STATUS, true);
		data.put(Constants.MESSAGE, SUCCESS_CONSTANTS.PASSWORD_UPDATED.toString());

		return data;
	}

}
