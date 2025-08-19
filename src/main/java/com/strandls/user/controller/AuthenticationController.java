package com.strandls.user.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.user.ApiConstants;
import com.strandls.user.Constants;
import com.strandls.user.Constants.ERROR_CONSTANTS;
import com.strandls.user.dto.StringObjectMap;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// OpenAPI 3 for Jakarta
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

@Tag(name = "Authentication Service")
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
	@Operation(summary = "Ping", description = "Pong")
	@ApiResponse(responseCode = "200", description = "Simple ping test", content = @Content(schema = @Schema(type = "string", example = "Pong")))
	public Response getTestResponse() {
		return Response.status(Status.OK).entity("Pong").build();
	}

	@POST
	@Path(ApiConstants.LOGIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Operation(summary = "Authenticates User by Credentials", description = "Returns Tokens")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Tokens returned", content = @Content(schema = @Schema(implementation = StringObjectMap.class))),
			@ApiResponse(responseCode = "403", description = "Could not authenticate user", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(type = "string"))) })
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
			ResponseBuilder response = Response.ok().entity(new StringObjectMap<Object>(tokens));
			String noCookie = PropertyFileUtil.fetchProperty("config.properties", Constants.NO_COOKIE);
			if (status && !verification && noCookie.equals("0")) {
				NewCookie accessToken = new NewCookie(Constants.BA_TOKEN, tokens.get(Constants.ACCESS_TOKEN).toString(),
						"/", AppUtil.getDomain(request), "", 10 * 24 * 60 * 60, false);
				NewCookie refreshToken = new NewCookie(Constants.BR_TOKEN,
						tokens.get(Constants.REFRESH_TOKEN).toString(), "/", AppUtil.getDomain(request), "",
						10 * 24 * 60 * 60, false);
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
	@Operation(summary = "Generates new set of tokens based on the refresh token", description = "Returns New Set of Tokens")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "New tokens returned", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "403", description = "Invalid refresh token", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(type = "string"))) })
	public Response generateNewTokens(@QueryParam("refreshToken") String refreshToken) {

		CommonProfile profile = jwtAuthenticator.validateToken(refreshToken);
		if (profile == null) {
			logger.debug("Invalid response token");
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid refresh token").build();
		}
		try {
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
	@Operation(summary = "Validates access token", description = "Returns if token is valid or not")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Valid access token", content = @Content(schema = @Schema(type = "string", example = "true"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized access token", content = @Content(schema = @Schema(type = "string", example = "false"))),
			@ApiResponse(responseCode = "406", description = "Invalid access token", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Create new user", description = "Returns the created user")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User created", content = @Content(schema = @Schema(implementation = StringObjectMap.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input or other error", content = @Content(schema = @Schema(type = "string"))) })
	public Response signUp(@Context HttpServletRequest request,
			@RequestBody(description = "User data", required = true, content = @Content(schema = @Schema(implementation = UserDTO.class))) UserDTO userDTO) {

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

			if (check.isRobot(recaptcha)) {
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
			return Response.status(Status.OK).entity(new StringObjectMap<Object>(data)).build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity("Could not create user").build();
		}
	}

	@POST
	@Path(ApiConstants.VALIDATE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Validates the OTP for user", description = "Returns tokens if the OTP is valid")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Result of OTP validation", content = @Content(schema = @Schema(implementation = StringObjectMap.class))),
			@ApiResponse(responseCode = "400", description = "Missing ID or OTP", content = @Content(schema = @Schema(type = "string"))) })
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
					"/", AppUtil.getDomain(request), "", 10 * 24 * 60 * 60, false);
			NewCookie refreshToken = new NewCookie(Constants.BR_TOKEN, result.get(Constants.REFRESH_TOKEN).toString(),
					"/", AppUtil.getDomain(request), "", 10 * 24 * 60 * 60, false);
			return Response.ok().entity(new StringObjectMap<Object>(result)).cookie(accessToken).cookie(refreshToken)
					.build();
		}
		return Response.ok().entity(new StringObjectMap<Object>(result)).build();
	}

	@GET
	@Path(ApiConstants.VERIFICATION_CONFIG)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get verification config", description = "Returns verification config as string array")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Config values", content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(type = "string")))) })
	public Response getVerificationConfig() {

		return Response.status(Status.OK)
				.entity(PropertyFileUtil.fetchProperty("config.properties", "verification_config").split(",")).build();
	}

	@POST
	@Path(ApiConstants.REGENERATE_OTP)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Regenerates OTP", description = "Returns the status of the request")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "OTP regeneration status", content = @Content(schema = @Schema(implementation = Map.class))) })
	public Response regenerateOTP(@Context HttpServletRequest request, @FormParam("id") Long id,
			@FormParam("action") Integer action) {

		Map<String, Object> data = authenticationService.regenerateOTP(request, id, action);
		return Response.status(Status.OK).entity(data).build();
	}

	@POST
	@Path(ApiConstants.FORGOT_PASSWORD)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Forgot Password - Send Mail/SMS", description = "Returns the status")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Status object", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Password Reset", description = "Returns the status")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Password reset status", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Password Change", description = "Returns the status")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Password change status", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(type = "string"))) })
	public Response changePassword(@Context HttpServletRequest request,
			@RequestBody(description = "Password change request", required = true, content = @Content(schema = @Schema(implementation = UserPasswordChange.class))) UserPasswordChange inputUser) {

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
