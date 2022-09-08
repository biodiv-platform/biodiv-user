package com.strandls.user.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.user.ApiConstants;
import com.strandls.user.Constants;
import com.strandls.user.Constants.ERROR_CONSTANTS;
import com.strandls.user.dto.UserDTO;
import com.strandls.user.pojo.User;
import com.strandls.user.pojo.requests.UserPasswordChange;
import com.strandls.user.service.AuthenticationService;
import com.strandls.user.service.RoleService;
import com.strandls.user.service.UserService;
import com.strandls.user.util.AppUtil;
import com.strandls.user.util.AppUtil.VERIFICATION_TYPE;
import com.strandls.user.util.AuthUtility;
import com.strandls.user.util.GoogleRecaptchaCheck;
import com.strandls.user.util.PropertyFileUtil;
import com.strandls.user.util.ValidationUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.minidev.json.JSONArray;

@Api("Authentication Service")
@Path(ApiConstants.V1 + ApiConstants.AUTHENTICATE)
public class AuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

	@Inject
	private JwtAuthenticator jwtAuthenticator;

	@Inject
	private AuthenticationService authenticationService;

	@Inject
	private UserService userService;

	@Inject
	private RoleService roleService;

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Ping", notes = "Pong", response = String.class)
	public Response getTestResponse() {
		return Response.status(Status.OK).entity("Pong").build();
	}

	@POST
	@Path(ApiConstants.LOGIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "Authenticates User by Credentials", notes = "Returns Tokens", response = Map.class)
	@ApiResponses(value = {
			@ApiResponse(code = 403, message = "Could not authenticate user", response = String.class) })
	public Response authenticate(@Context HttpServletRequest request, @FormParam("username") String userEmail,
			@FormParam("password") String password, @FormParam("mode") String mode) {
		try {
			if (userEmail == null || userEmail.isEmpty()) {
				return Response.status(Status.BAD_REQUEST)
						.entity(AppUtil.generateResponse(false, ERROR_CONSTANTS.USERNAME_REQUIRED)).build();
			}
			if (password == null || password.isEmpty()) {
				return Response.status(Status.BAD_REQUEST)
						.entity(AppUtil.generateResponse(false, ERROR_CONSTANTS.PASSWORD_REQUIRED)).build();
			}
			if (mode == null || mode.isEmpty()) {
				return Response.status(Status.BAD_REQUEST)
						.entity(AppUtil.generateResponse(false, ERROR_CONSTANTS.VERIFICATION_MODE_REQUIRED)).build();
			}
			Map<String, Object> tokens = new HashMap<>();
			if (mode.equalsIgnoreCase(AppUtil.AUTH_MODE.MANUAL.getAction())) {
				tokens = this.authenticationService.authenticateUser(userEmail, password);
			} else if (mode.equalsIgnoreCase(AppUtil.AUTH_MODE.OAUTH_GOOGLE.getAction())) {
				JSONObject obj = AuthUtility.verifyGoogleToken(password);
				if (obj != null) {
					User user = userService.getUserByEmail(obj.getString("email"));
					if (user == null) {
						return Response.status(Status.BAD_REQUEST)
								.entity(AppUtil.generateResponse(false, ERROR_CONSTANTS.USER_NOT_FOUND)).build();
					}
					if (user.getAccountLocked().booleanValue()) {
						user.setRoles(roleService.setDefaultRoles(AuthUtility.getDefaultRoles()));
						user.setAccountLocked(false);
						user.setLastLoginDate(new Date());
						user = userService.updateUser(user);
					}
					CommonProfile profile = AuthUtility.createUserProfile(user);
					tokens = authenticationService.buildTokens(profile, user, true);
					tokens.put(Constants.STATUS, true);
					tokens.put("verificationRequired", false);
				} else {
					return Response.status(Status.BAD_REQUEST).entity("Token expired").build();
				}
			}
			boolean status = Boolean.parseBoolean(tokens.get(Constants.STATUS).toString());
			boolean verification = Boolean.parseBoolean(tokens.get("verificationRequired").toString());
			ResponseBuilder response = Response.ok().entity(tokens);
			String noCookie = PropertyFileUtil.fetchProperty("config.properties", Constants.NO_COOKIE);
			if (status && !verification && noCookie.equals("0")) {
				NewCookie accessToken = new NewCookie(Constants.BA_TOKEN, tokens.get(Constants.ACCESS_TOKEN).toString(),
						"/", AppUtil.getDomain(request), "", 10 * 24 * 60 * 60, false);// NOSONAR
				NewCookie refreshToken = new NewCookie(Constants.BR_TOKEN,
						tokens.get(Constants.REFRESH_TOKEN).toString(), "/", AppUtil.getDomain(request), "",
						10 * 24 * 60 * 60, false);// NOSONAR
				return response.cookie(accessToken).cookie(refreshToken).build();
			} else {
				return response.build();
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.REFRESH_TOKENS)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "Generates new set of tokens based on the refresh token", notes = "Returns New Set of Tokens", response = Map.class)
	@ApiResponses(value = { @ApiResponse(code = 403, message = "Invalid refresh token", response = String.class) })
	public Response generateNewTokens(@QueryParam("refreshToken") String refreshToken) {
		CommonProfile profile = jwtAuthenticator.validateToken(refreshToken);
		if (profile == null) {
			logger.debug("Invalid response token");
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid refresh token").build();
		}
		try {
			// Retrieve the claims from JWT and call the buildTokens method to generate the
			// tokens
			Map<String, Object> tokens = this.authenticationService.buildTokens(profile,
					this.userService.fetchUser(Long.parseLong(profile.getId())), true);

			String noCookie = PropertyFileUtil.fetchProperty("config.properties", Constants.NO_COOKIE);
			if (noCookie.equals("0")) {
				return Response.status(Status.OK)
						.cookie(new NewCookie(Constants.BA_TOKEN, tokens.get(Constants.ACCESS_TOKEN).toString()))
						.cookie(new NewCookie(Constants.BR_TOKEN, tokens.get(Constants.REFRESH_TOKEN).toString()))
						.entity(tokens).build();
			}
			return Response.status(Status.OK).entity(tokens).build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.VALIDATE_TOKEN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Validates access token", notes = "Returns if token is valid or not", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Unauthorized access token", response = String.class),
			@ApiResponse(code = 406, message = "Invalid access token", response = String.class) })
	public Response validateToken(@QueryParam("accessToken") String accessToken) {
		if (accessToken == null || accessToken.isEmpty()) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		try {
			CommonProfile profile = jwtAuthenticator.validateToken(accessToken);
			boolean validToken = profile != null;
			return Response.status(validToken ? Status.OK : Status.UNAUTHORIZED).entity(String.valueOf(validToken))
					.build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity("Invalid Access Token").build();
		}
	}

	@POST
	@Path(ApiConstants.SIGNUP)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create new user", notes = "Returns the created user", response = Map.class)
	public Response signUp(@Context HttpServletRequest request, @ApiParam(name = "userDTO") UserDTO userDTO) {
		try {
			String username = userDTO.getUsername();
			String password = userDTO.getPassword();
			String confirmPassword = userDTO.getConfirmPassword();
			String location = userDTO.getLocation();
			Double latitude = userDTO.getLatitude();
			Double longitude = userDTO.getLongitude();
			String email = userDTO.getEmail().toLowerCase();
			String mobileNumber = userDTO.getMobileNumber();
			String verificationType = AppUtil.getVerificationType(userDTO.getVerificationType());
			String mode = userDTO.getMode();
			String recaptcha = userDTO.getRecaptcha();
			GoogleRecaptchaCheck check = new GoogleRecaptchaCheck();

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute("roles");
			if (!roles.contains("ROLE_ADMIN") && check.isRobot(recaptcha)) {
				return Response.status(Status.BAD_REQUEST)
						.entity(AppUtil.generateResponse(false, ERROR_CONSTANTS.INVALID_CAPTCHA)).build();
			}

			if (username == null || username.isEmpty()) {
				return Response.status(Status.BAD_REQUEST).entity("Username cannot be empty").build();
			}
			if (mode != null && mode.equalsIgnoreCase("manual")) {
				if (!password.equals(confirmPassword) || password.length() < 8) {
					return Response.status(Status.BAD_REQUEST).entity("Password must be longer than 8 characters")
							.build();
				}
			} else if (mode != null && mode.equalsIgnoreCase(AppUtil.AUTH_MODE.OAUTH_GOOGLE.getAction())) {
				JSONObject obj = AuthUtility.verifyGoogleToken(password);
				if (obj == null) {
					return Response.status(Status.BAD_REQUEST).entity("Google token expired").build();
				}
				if (!obj.getString("email").equalsIgnoreCase(email)) {
					return Response.status(Status.BAD_REQUEST)
							.entity(AppUtil.generateResponse(false, ERROR_CONSTANTS.EMAIL_VERIFICATION_FAILED)).build();
				}
			} else {
				return Response.status(Status.BAD_REQUEST).entity("Invalid auth code").build();
			}
			if (location == null) {
				return Response.status(Status.BAD_REQUEST).entity("Location cannot be null").build();
			}
			if (latitude == null) {
				return Response.status(Status.BAD_REQUEST).entity("Latitude cannot be null").build();
			}
			if (longitude == null) {
				return Response.status(Status.BAD_REQUEST).entity("Longitude cannot be null").build();
			}
			if (verificationType == null) {
				return Response.status(Status.BAD_REQUEST).entity("Invalid verification type").build();
			}
			if (VERIFICATION_TYPE.EMAIL.toString().equalsIgnoreCase(verificationType)
					&& !ValidationUtil.validateEmail(email)) {
				return Response.status(Status.BAD_REQUEST).entity("Invalid email").build();
			} else if (VERIFICATION_TYPE.MOBILE.toString().equalsIgnoreCase(verificationType)
					&& !ValidationUtil.validatePhone(mobileNumber)) {
				return Response.status(Status.BAD_REQUEST).entity("Invalid mobile number").build();
			}
			Map<String, Object> data = authenticationService.addUser(request, userDTO, verificationType);
			return Response.status(Status.OK).entity(data).build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity("Could not create user").build();
		}
	}

	@POST
	@Path(ApiConstants.VALIDATE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Validates the OTP for user", notes = "Returns tokens if the OTP is valid", response = Map.class)
	public Response validateAccount(@Context HttpServletRequest request, @FormParam("id") Long id,
			@FormParam("otp") String otp) {
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).entity("ID Cannot be empty").build();
		}
		if (otp == null || otp.isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("OTP Cannot be empty").build();
		}
		Map<String, Object> result = authenticationService.validateUser(request, id, otp);
		String noCookie = PropertyFileUtil.fetchProperty("config.properties", Constants.NO_COOKIE);
		if (Boolean.parseBoolean(result.get(Constants.STATUS).toString()) && noCookie.equals("0")) {
			NewCookie accessToken = new NewCookie(Constants.BA_TOKEN, result.get(Constants.ACCESS_TOKEN).toString(),
					"/", AppUtil.getDomain(request), "", 10 * 24 * 60 * 60, false);// NOSONAR
			NewCookie refreshToken = new NewCookie(Constants.BR_TOKEN, result.get(Constants.REFRESH_TOKEN).toString(),
					"/", AppUtil.getDomain(request), "", 10 * 24 * 60 * 60, false);// NOSONAR
			return Response.ok().entity(result).cookie(accessToken).cookie(refreshToken).build();
		}
		return Response.status(Status.OK).entity(result).build();
	}

	@GET
	@Path(ApiConstants.VERIFICATION_CONFIG)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVerificationConfig() {
		return Response.status(Status.OK)
				.entity(PropertyFileUtil.fetchProperty("config.properties", "verification_config").split(",")).build();
	}

	@POST
	@Path(ApiConstants.REGENERATE_OTP)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Regenerates OTP", notes = "Returns the status of the request", response = Map.class)
	public Response regenerateOTP(@Context HttpServletRequest request, @FormParam("id") Long id,
			@FormParam("action") Integer action) {
		Map<String, Object> data = authenticationService.regenerateOTP(request, id, action);
		return Response.status(Status.OK).entity(data).build();
	}

	@POST
	@Path(ApiConstants.FORGOT_PASSWORD)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Forgot Password - Send Mail/SMS", notes = "Returns the status", response = Map.class)
	public Response forgotPassword(@Context HttpServletRequest request,
			@FormParam("verificationId") String verificationId) {
		Map<String, Object> data = authenticationService.forgotPassword(request, verificationId);
		if (data != null)
			return Response.status(Status.OK).entity(data).build();
		return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path(ApiConstants.RESET_PASSWORD)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Password Reset", notes = "Returns the status", response = Map.class)
	public Response resetPassword(@Context HttpServletRequest request, @FormParam("id") Long id,
			@FormParam("otp") String otp, @FormParam("password") String password,
			@FormParam("confirmPassword") String confirmPassword) {
		if (password == null || password.isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("Password cannot be empty").build();
		}
		if (!password.equals(confirmPassword)) {
			return Response.status(Status.BAD_REQUEST).entity("Passwords do not match").build();
		}
		Map<String, Object> data = authenticationService.resetPassword(request, id, otp, password);
		boolean status = Boolean.parseBoolean(data.get("status").toString());
		if (status)
			return Response.status(Status.OK).entity(data).build();
		return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path(ApiConstants.CHANGE_PASSWORD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Password Change", notes = "Returns the status", response = Map.class)
	public Response changePassword(@Context HttpServletRequest request,
			@ApiParam(name = "user") UserPasswordChange inputUser) {

		if (inputUser.getNewPassword() == null || inputUser.getNewPassword().isEmpty()
				|| inputUser.getConfirmNewPassword() == null) {
			return Response.status(Status.BAD_REQUEST).entity("Password cannot be empty").build();
		}
		if (inputUser.getConfirmNewPassword() != null && !inputUser.getConfirmNewPassword().isEmpty()) {
			if (!inputUser.getNewPassword().equals(inputUser.getConfirmNewPassword())) {
				return Response.status(Status.BAD_REQUEST).entity("Passwords do not match").build();
			}
		}

		Map<String, Object> data = authenticationService.changePassword(request, inputUser);
		return Response.status(Status.OK).entity(data).build();
	}
}
