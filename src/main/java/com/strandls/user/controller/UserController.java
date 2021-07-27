/**
 * 
 */
package com.strandls.user.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.user.ApiConstants;
import com.strandls.user.converter.UserConverter;
import com.strandls.user.dto.FirebaseDTO;
import com.strandls.user.exception.UnAuthorizedUserException;
import com.strandls.user.pojo.FirebaseTokens;
import com.strandls.user.pojo.Follow;
import com.strandls.user.pojo.Recipients;
import com.strandls.user.pojo.User;
import com.strandls.user.pojo.UserIbp;
import com.strandls.user.pojo.requests.UserDetails;
import com.strandls.user.pojo.requests.UserEmailPreferences;
import com.strandls.user.pojo.requests.UserRoles;
import com.strandls.user.service.UserService;
import com.strandls.user.util.AuthUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.minidev.json.JSONArray;

/**
 * @author Abhishek Rudra
 *
 */

@Api("User Service")
@Path(ApiConstants.V1 + ApiConstants.USER)
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Inject
	private UserService userService;

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Dummy API Ping", notes = "Checks validity of war file at deployment", response = String.class)
	public Response ping() {
		return Response.status(Status.OK).entity("PONG").build();
	}

	@GET
	@Path("/{userId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find User by User ID", notes = "Returns User details", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Traits not found", response = String.class) })

	public Response getUser(@PathParam("userId") String userId) {

		try {

			Long uId = Long.parseLong(userId);
			User user = userService.fetchUser(uId);
			if (user.getIsDeleted().booleanValue()) {
				return Response.status(Status.NOT_FOUND).entity("User deleted").build();
			}
			return Response.status(Status.OK).entity(user).build();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path(ApiConstants.IBP + "/{userId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find User by User ID for ibp", notes = "Returns User details", response = UserIbp.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = String.class) })

	public Response getUserIbp(@PathParam("userId") String userId) {
		try {
			Long id = Long.parseLong(userId);
			UserIbp ibp = userService.fetchUserIbp(id);
			return Response.status(Status.OK).entity(ibp).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.IMAGE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "update the user", notes = "Returns User details", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = String.class) })
	@ValidateUser
	public Response updateUserImage(@Context HttpServletRequest request, @QueryParam("id") Long userId,
			@QueryParam("profilePic") String profilePic) throws UnAuthorizedUserException {
		User user = userService.updateProfilePic(request, userId, profilePic);
		return Response.status(Status.OK).entity(user).build();
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.DETAILS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "update the user", notes = "Returns User details", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = String.class) })
	@ValidateUser
	public Response updateUserDetails(@Context HttpServletRequest request,
			@ApiParam(name = "user") UserDetails inputUser) throws UnAuthorizedUserException {
		User user = userService.updateUserDetails(request, inputUser);
		return Response.status(Status.OK).entity(user).build();
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.EMAIL_PREFERENCES)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "update the user", notes = "Returns User Email preferences", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = String.class) })
	@ValidateUser
	public Response updateUserEmailPreferences(@Context HttpServletRequest request,
			@ApiParam(name = "user") UserEmailPreferences inputUser) throws UnAuthorizedUserException {
		User user = userService.updateEmailPreferences(request, inputUser);
		return Response.status(Status.OK).entity(user).build();
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.ROLES)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "update the user", notes = "Returns User roles", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = String.class) })
	@ValidateUser
	public Response updateUserRoles(@Context HttpServletRequest request, @ApiParam(name = "user") UserRoles inputUser)
			throws UnAuthorizedUserException {
		if (AuthUtility.isAdmin(request)) {
			Response.status(Status.UNAUTHORIZED).build();
		}
		User user = userService.updateRolesAndPermission(request, inputUser);
		return Response.status(Status.OK).entity(user).build();
	}

	@GET
	@Path(ApiConstants.BULK + ApiConstants.IBP)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find User by User ID in bulk for ibp", notes = "Returns User details", response = UserIbp.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = String.class) })

	public Response getUserIbbpBulk(@QueryParam("userIds") String userIds) {
		try {
			List<Long> uIds = new ArrayList<>();
			for (String uId : userIds.split(","))
				uIds.add(Long.parseLong(uId));
			List<UserIbp> result = userService.fetchUserIbpBulk(uIds);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.ME)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Find the Current user Details", notes = "Returns the Current User Details", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = String.class) })

	public Response getCurretUser(@Context HttpServletRequest request) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long uId = Long.parseLong(profile.getId());
			User user = userService.fetchUser(uId);
			return Response.status(Status.OK).entity(user).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.FOLLOW + "/{followId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find follow by followid", notes = "Return follows", response = Follow.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Follow not Found", response = String.class) })

	public Response getByFollowID(@PathParam("followId") String followId) {

		try {
			Long id = Long.parseLong(followId);
			Follow follow = userService.fetchByFollowId(id);
			return Response.status(Status.OK).entity(follow).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.OBJECTFOLLOW + "/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	@ApiOperation(value = "Find follow by objectId", notes = "Return follows", response = Follow.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Follow not Found", response = String.class) })

	public Response getFollowByObject(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId) {
		try {

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long objId = Long.parseLong(objectId);
			Long authId = Long.parseLong(profile.getId());

			Follow follow = userService.fetchByFollowObject(objectType, objId, authId);
			return Response.status(Status.OK).entity(follow).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.USERFOLLOW + "/{userId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	@ApiOperation(value = "Find follow by userID", notes = "Return list follows", response = Follow.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Follow not Found", response = String.class) })

	public Response getFollowbyUser(@Context HttpServletRequest request) {

		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long id = Long.parseLong(profile.getId());
			List<Follow> follows = userService.fetchFollowByUser(id);
			return Response.status(Status.OK).entity(follows).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}

	}

	@POST
	@Path(ApiConstants.FOLLOW)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser

	@ApiOperation(value = "Marks follow for a User", notes = "Returnt the follow details", response = Follow.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to mark follow", response = String.class) })

	public Response updateFollow(@Context HttpServletRequest request, @FormParam("object") String object,
			@FormParam("objectId") String objectId) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long userId = Long.parseLong(profile.getId());
			Long objId = Long.parseLong(objectId);
			Follow result = userService.updateFollow(object, objId, userId);

			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.UNFOLLOW + "/{type}/{objectId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser

	@ApiOperation(value = "Marks unfollow for a User", notes = "Returnt the follow details", response = Follow.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to mark unfollow", response = String.class) })

	public Response unfollow(@Context HttpServletRequest request, @PathParam("type") String type,
			@PathParam("objectId") String objectId) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long userId = Long.parseLong(profile.getId());
			Long objId = Long.parseLong(objectId);
			Follow result = userService.unFollow(type, objId, userId);

			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.AUTOCOMPLETE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Names autocomplete", notes = "Returns list of names", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to return the data", response = String.class) })
	public Response autocomplete(@QueryParam("name") String name) {
		try {
			Set<UserIbp> users = UserConverter.convertToIbpSet(userService.getNames(name));
			return Response.ok().entity(users).build();
		} catch (Exception ex) {
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.RECIPIENTS)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetches recipients", notes = "Returns list of recipients", response = Recipients.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to return the data", response = String.class) })
	public Response getRecipients(@FormParam("objectType") String objectType, @FormParam("objectId") Long objectId) {
		try {
			List<Recipients> users = UserConverter
					.convertToRecipientList(userService.fetchRecipients(objectType, objectId));
			logger.debug("***** Total Recipients #: {} *****", users.size());
			for (Recipients recipient : users) {
				logger.debug("***** Recipient #: {} *****", recipient.getId());
			}
			return Response.ok().entity(users).build();
		} catch (Exception ex) {
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.SAVE_TOKEN)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ValidateUser
	@ApiOperation(value = "Save Token", notes = "Associates token with a user", response = FirebaseTokens.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to return the data", response = String.class) })
	public Response saveToken(@Context HttpServletRequest request, FirebaseDTO firebaseDTO) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long userId = Long.parseLong(profile.getId());
			FirebaseTokens savedToken = userService.saveToken(userId, firebaseDTO.getToken());
			return Response.ok().entity(savedToken).build();
		} catch (Exception ex) {
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.SEND_NOTIFICATION)
	@ValidateUser
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Push Notifications", notes = "Send generalized push notifications to all users")
	public Response sendGeneralNotification(@Context HttpServletRequest request, FirebaseDTO firebaseDTO) {
		try {
			userService.sendPushNotifications(firebaseDTO);
			return Response.status(Status.OK).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path(ApiConstants.DELETE + "/{userId}")
	@ValidateUser
	@ApiOperation(value = "Delete an existing user", notes = "Gets the user id and deletes the user", response = String.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", paramType = "header") })
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to delete the user", response = String.class) })

	public Response deleteUser(@Context HttpServletRequest request, @PathParam("userId") String userId) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray userRole = (JSONArray) profile.getAttribute("roles");

			if (userRole.contains("ROLE_ADMIN")) {
				Long user = Long.parseLong(userId);
				if (!profile.getId().equalsIgnoreCase(userId)) {
					String data = userService.deleteUser(request, user);
					return Response.status(Status.OK).entity(data).build();
				}
				return Response.status(Status.OK).entity("CANNOT DELETE SELF").build();
			}
			return Response.status(Status.OK).entity("USER NOT ALLOWED TO PERFORM THE TASK").build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.ADMIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Fetch all the admins of the portal", notes = "Returns a list of admins", response = User.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to fetch the admins", response = String.class) })

	public Response getAllAdmins() {
		try {
			List<User> result = userService.getAllAdmins();
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

}
