package com.strandls.user.controller;

import java.util.Map;

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
import javax.ws.rs.core.Response.Status;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.strandls.user.ApiConstants;
import com.strandls.user.dto.UserDTO;
import com.strandls.user.service.AuthenticationService;
import com.strandls.user.service.UserService;
import com.strandls.user.util.AppUtil;
import com.strandls.user.util.AppUtil.VERIFICATION_TYPE;
import com.strandls.user.util.PropertyFileUtil;
import com.strandls.user.util.ValidationUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
	public Response authenticate(@FormParam("username") String userEmail, @FormParam("password") String password) {
		try {
			Map<String, Object> tokens = this.authenticationService.authenticateUser(userEmail, password);
			if (!Boolean.parseBoolean(tokens.get("status").toString())) {
				return Response.status(Status.OK).entity(tokens).build();
			}
			return Response.status(Status.OK).cookie(new NewCookie("BAToken", tokens.get("access_token").toString()))
					.cookie(new NewCookie("BRToken", tokens.get("refresh_token").toString())).entity(tokens).build();
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
			return Response.status(Status.OK).cookie(new NewCookie("BAToken", tokens.get("access_token").toString()))
					.cookie(new NewCookie("BRToken", tokens.get("refresh_token").toString())).entity(tokens).build();
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
	public Response signUp(@Context HttpServletRequest request, UserDTO userDTO) {
		try {
			String username = userDTO.getUsername();
			String password = userDTO.getPassword();
			String confirmPassword = userDTO.getConfirmPassword();
			String location = userDTO.getLocation();
			Double latitude = userDTO.getLatitude();
			Double longitude = userDTO.getLongitude();
			String email = userDTO.getEmail();
			String mobileNumber = userDTO.getMobileNumber();
			String verificationType = AppUtil.getVerificationType(userDTO.getVerificationType());
			if (username == null || username.isEmpty()) {
				return Response.status(Status.BAD_REQUEST).entity("Username cannot be empty").build();
			}
			if (!password.equals(confirmPassword) || password.length() < 8) {
				return Response.status(Status.BAD_REQUEST).entity("Password must be longer than 8 characters").build();
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
			Map<String, Object> data = authenticationService.addUser(request, userDTO);
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
	public Response validateAccount(@Context HttpServletRequest request, @FormParam("id") Long id,
			@FormParam("otp") String otp) {
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).entity("ID Cannot be empty").build();
		}
		if (otp == null || otp.isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("OTP Cannot be empty").build();
		}
		Map<String, Object> result = authenticationService.validateUser(request, id, otp);
		if (Boolean.parseBoolean(result.get("status").toString())) {
			return Response.status(Status.OK).cookie(new NewCookie("BAToken", result.get("access_token").toString()))
					.cookie(new NewCookie("BRToken", result.get("refresh_token").toString())).entity(result).build();
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
	public Response regenerateOTP(@Context HttpServletRequest request, @FormParam("id") Long id,
			@FormParam("action") Integer action) {
		Map<String, Object> data = authenticationService.regenerateOTP(request, id, action);
		return Response.status(Status.OK).entity(data).build();
	}

	@POST
	@Path(ApiConstants.FORGOT_PASSWORD)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forgotPassword(@Context HttpServletRequest request,
			@FormParam("verificationId") String verificationId) {
		Map<String, Object> data = authenticationService.forgotPassword(request, verificationId);
		return Response.status(Status.OK).entity(data).build();
	}

	@POST
	@Path(ApiConstants.RESET_PASSWORD)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
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
		return Response.status(Status.OK).entity(data).build();
	}

}