/**
 *
 */
package com.strandls.user.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.esmodule.ApiException;
import com.strandls.esmodule.pojo.MapBoundParams;
import com.strandls.esmodule.pojo.MapBounds;
import com.strandls.esmodule.pojo.MapGeoPoint;
import com.strandls.esmodule.pojo.MapSearchParams;
import com.strandls.esmodule.pojo.MapSearchParams.SortTypeEnum;
import com.strandls.esmodule.pojo.MapSearchQuery;
import com.strandls.user.ApiConstants;
import com.strandls.user.converter.UserConverter;
import com.strandls.user.dto.FirebaseDTO;
import com.strandls.user.es.utils.EsUtility;
import com.strandls.user.exception.UnAuthorizedUserException;
import com.strandls.user.pojo.EsLocationListParams;
import com.strandls.user.pojo.FirebaseTokens;
import com.strandls.user.pojo.Follow;
import com.strandls.user.pojo.MapAggregationResponse;
import com.strandls.user.pojo.Recipients;
import com.strandls.user.pojo.User;
import com.strandls.user.pojo.UserIbp;
import com.strandls.user.pojo.UserListData;
import com.strandls.user.pojo.requests.UserDetails;
import com.strandls.user.pojo.requests.UserEmailPreferences;
import com.strandls.user.pojo.requests.UserRoles;
import com.strandls.user.service.UserListService;
import com.strandls.user.service.UserService;
import com.strandls.user.util.AuthUtility;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import net.minidev.json.JSONArray;

/**
 * @author Abhishek Rudra
 *
 */

@Tag(name = "User Service")
@Path(ApiConstants.V1 + ApiConstants.USER)
public class UserController {

	@Inject
	private EsUtility esUtility;
	@Inject
	private UserListService userListService;
	@Inject
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Dummy API Ping", description = "Checks validity of war file at deployment")
	@ApiResponse(responseCode = "200", description = "Ping successful", content = @Content(schema = @Schema(type = "string", example = "PONG")))
	public Response ping() {
		return Response.status(Status.OK).entity("PONG").build();
	}

	@GET
	@Path("/{userId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Find User by User ID", description = "Returns User details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string"))) })
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
			return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.IBP + "/{userId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Find User by User ID for ibp", description = "Returns User details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "UserIbp found", content = @Content(schema = @Schema(implementation = UserIbp.class))),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getUserIbp(@PathParam("userId") String userId) {
		try {
			Long id = Long.parseLong(userId);
			UserIbp ibp = userService.fetchUserIbp(id);
			return Response.status(Status.OK).entity(ibp).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.IBP + "/users")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Find Users by User ID list for ibp", description = "Returns Users details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of UserIbp or empty.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserIbp.class)))) })
	public Response getUserIbpInBulk(
			@RequestBody(description = "userIdList", required = true, content = @Content(array = @ArraySchema(schema = @Schema(type = "integer")))) List<Long> userIdList) {
		try {
			return Response.status(Status.OK).entity(userService.fetchUserIbpBulk(userIdList)).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.IBP + "/userList")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Find User by User ID in bulk for ibp", description = "Returns User details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of users.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
			@ApiResponse(responseCode = "400", description = "Bad request") })
	public Response getUserBulk(
			@RequestBody(description = "userIds", required = true, content = @Content(array = @ArraySchema(schema = @Schema(type = "long")))) List<Long> userIdList) {
		try {
			List<User> users = userService.fetchUserBulk(userIdList);
			return Response.status(Status.OK).entity(users).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.IMAGE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "update the user", description = "Returns User details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Updated user", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string"))) })
	@ValidateUser
	public Response updateUserImage(@Context HttpServletRequest request, @QueryParam("id") Long userId,
			@QueryParam("profilePic") String profilePic) throws UnAuthorizedUserException, ApiException {
		User user = userService.updateProfilePic(request, userId, profilePic);
		return Response.status(Status.OK).entity(user).build();
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.DETAILS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "update the user", description = "Returns User details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Updated user", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string"))) })
	@ValidateUser
	public Response updateUserDetails(@Context HttpServletRequest request,
			@RequestBody(description = "User update details", required = true, content = @Content(schema = @Schema(implementation = UserDetails.class))) UserDetails inputUser)
			throws UnAuthorizedUserException, ApiException {
		User user = userService.updateUserDetails(request, inputUser);
		return Response.status(Status.OK).entity(user).build();
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.EMAIL_PREFERENCES)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "update the user", description = "Returns User Email preferences")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Updated user", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string"))) })
	@ValidateUser
	public Response updateUserEmailPreferences(@Context HttpServletRequest request,
			@RequestBody(description = "User email preferences", required = true, content = @Content(schema = @Schema(implementation = UserEmailPreferences.class))) UserEmailPreferences inputUser)
			throws UnAuthorizedUserException, ApiException {
		User user = userService.updateEmailPreferences(request, inputUser);
		return Response.status(Status.OK).entity(user).build();
	}

	@GET
	@Path(ApiConstants.UNSUBSCRIBE + "/{token}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Unsubscribe user mail notification", description = "Returns User details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Unsubscribed", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Invalid token or error", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(type = "string"))) })
	public Response updateUserEmailPreferences(@PathParam("token") String token)
			throws UnAuthorizedUserException, ApiException {
		User user = null;
		if (token == null || token.contentEquals("x")) {
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized").build();
		}
		String email = AuthUtility.getUserEmail(token);
		if (email == null || email.isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("Provided token is invalid").build();
		}
		try {
			user = userService.unsubscribeByUserEmail(email);
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		return Response.status(Status.OK).entity("Unsubscribed").build();
	}

	@PUT
	@Path(ApiConstants.UPDATE + ApiConstants.ROLES)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "update the user", description = "Returns User roles")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Updated user", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(type = "string"))) })
	@ValidateUser
	public Response updateUserRoles(@Context HttpServletRequest request,
			@RequestBody(description = "User roles update", required = true, content = @Content(schema = @Schema(implementation = UserRoles.class))) UserRoles inputUser)
			throws UnAuthorizedUserException, ApiException {
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
	@Operation(summary = "Find User by User ID in bulk for ibp", description = "Returns User details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Users found", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserIbp.class)))),
			@ApiResponse(responseCode = "400", description = "User not found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getUserIbbpBulk(@QueryParam("userIds") String userIds) {
		try {
			List<Long> uIds = new ArrayList<>();
			for (String uId : userIds.split(","))
				uIds.add(Long.parseLong(uId));
			List<UserIbp> result = userService.fetchUserIbpBulk(uIds);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.ME)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Find the Current user Details", description = "Returns the Current User Details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Current user", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "400", description = "Error", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Find follow by followid", description = "Return follows")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Follow found", content = @Content(schema = @Schema(implementation = Follow.class))),
			@ApiResponse(responseCode = "400", description = "Follow not Found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getByFollowID(@PathParam("followId") String followId) {
		try {
			Long id = Long.parseLong(followId);
			Follow follow = userService.fetchByFollowId(id);
			return Response.status(Status.OK).entity(follow).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.OBJECTFOLLOW + "/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Find follow by objectId", description = "Return follows")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Follow found", content = @Content(schema = @Schema(implementation = Follow.class))),
			@ApiResponse(responseCode = "400", description = "Follow not Found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getFollowByObject(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long objId = Long.parseLong(objectId);
			Long authId = Long.parseLong(profile.getId());
			Follow follow = userService.fetchByFollowObject(objectType, objId, authId);
			return Response.status(Status.OK).entity(follow).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.USERFOLLOW + "/{userId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Find follow by userID", description = "Return list follows")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of follows", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Follow.class)))),
			@ApiResponse(responseCode = "400", description = "Follow not Found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getFollowbyUser(@Context HttpServletRequest request) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long id = Long.parseLong(profile.getId());
			List<Follow> follows = userService.fetchFollowByUser(id);
			return Response.status(Status.OK).entity(follows).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.FOLLOW)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Marks follow for a User", description = "Returns the follow details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Updated follow", content = @Content(schema = @Schema(implementation = Follow.class))),
			@ApiResponse(responseCode = "400", description = "Unable to mark follow", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Marks unfollow for a User", description = "Returns the follow details")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Unfollowed", content = @Content(schema = @Schema(implementation = Follow.class))),
			@ApiResponse(responseCode = "400", description = "Unable to mark unfollow", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Names autocomplete", description = "Returns list of names")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of ibp users", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserIbp.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to return the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response autocomplete(@QueryParam("name") String name) {
		try {
			Set<UserIbp> users = UserConverter.convertToIbpSet(userService.getNames(name));
			return Response.ok().entity(users).build();
		} catch (Exception ex) {
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.IBP + ApiConstants.AUTOCOMPLETE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Names autocomplete using es", description = "Returns list of names")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of ibp users (es)", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserIbp.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to return the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response esAutocomplete(@QueryParam("name") String name, @QueryParam("userGroupId") String userGroupId) {
		try {
			Set<UserIbp> users = userService.getAutoComplete(userGroupId, name);
			return Response.ok().entity(users).build();
		} catch (Exception ex) {
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.SPECIESCONTRIBUTOR + ApiConstants.AUTOCOMPLETE)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Names autocomplete using es", description = "Returns list of names")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of species contributors", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserIbp.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to return the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response getSpeciesContributorAutocompleteEs(@QueryParam("name") String name) {
		try {
			Set<UserIbp> users = userService.getSpeciesContributorAutoComplete(name);
			return Response.ok().entity(users).build();
		} catch (Exception ex) {
			return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.RECIPIENTS)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Fetches recipients", description = "Returns list of recipients")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of recipients", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Recipients.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to return the data", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Save Token", description = "Associates token with a user")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Token saved", content = @Content(schema = @Schema(implementation = FirebaseTokens.class))),
			@ApiResponse(responseCode = "400", description = "Unable to return the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response saveToken(@Context HttpServletRequest request,
			@RequestBody(description = "firebaseDTO", required = true, content = @Content(schema = @Schema(implementation = FirebaseDTO.class))) FirebaseDTO firebaseDTO) {
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
	@Operation(summary = "Push Notifications", description = "Send generalized push notifications to all users")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Notification sent"),
			@ApiResponse(responseCode = "400", description = "Unable to send notification", content = @Content(schema = @Schema(type = "string"))) })
	public Response sendGeneralNotification(@Context HttpServletRequest request,
			@RequestBody(description = "firebaseDTO", required = true, content = @Content(schema = @Schema(implementation = FirebaseDTO.class))) FirebaseDTO firebaseDTO) {
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
	@Operation(summary = "Delete an existing user", description = "Gets the user id and deletes the user")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User deleted", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Unable to delete the user", content = @Content(schema = @Schema(type = "string"))) })
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
	@Operation(summary = "Fetch all the admins of the portal", description = "Returns a list of admins")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of users that are admins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
			@ApiResponse(responseCode = "400", description = "unable to fetch the admins", content = @Content(schema = @Schema(type = "string"))) })
	public Response getAllAdmins() {
		try {
			List<User> result = userService.getAllAdmins();
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.LIST + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get User list with filters, aggregation, map, etc.", description = "Returns paginated user list and aggregations")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Paginated user list + aggregations", content = @Content(schema = @Schema(implementation = UserListData.class))),
			@ApiResponse(responseCode = "400", description = "Error", content = @Content(schema = @Schema(type = "string"))) })
	public Response UserList(@Context HttpServletRequest request, @PathParam("index") String index,
			@PathParam("type") String type, @DefaultValue("10") @QueryParam("max") Integer max,
			@DefaultValue("0") @QueryParam("offset") Integer offset,
			@DefaultValue("user.dateCreated") @QueryParam("sort") String sortOn,
			@QueryParam("createdOnMaxDate") String createdOnMaxDate,
			@QueryParam("createdOnMinDate") String createdOnMinDate,
			@QueryParam("lastLoggedInMaxDate") String lastLoggedInMaxDate,
			@QueryParam("lastLoggedInMinDate") String lastLoggedInMinDate,
			@DefaultValue("") @QueryParam("user") String user, @QueryParam("left") Double left,
			@QueryParam("right") Double right, @QueryParam("top") Double top, @QueryParam("bottom") Double bottom,
			@DefaultValue("") @QueryParam("userGroupList") String userGroupList,
			@DefaultValue("") @QueryParam("role") String role,
			@DefaultValue("") @QueryParam("taxonomyList") String taxonomyList,
			@DefaultValue("") @QueryParam("taxonRole") String taxonRole,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoShapeFilterField") String geoShapeFilterField,
			@QueryParam("nestedField") String nestedField, @DefaultValue("") @QueryParam("email") String email,
			@DefaultValue("") @QueryParam("profession") String profession,
			@DefaultValue("") @QueryParam("sex") String sex,
			@DefaultValue("") @QueryParam("institution") String institution,
			@DefaultValue("") @QueryParam("name") String name,
			@DefaultValue("") @QueryParam("userName") String userName,
			@DefaultValue("") @QueryParam("phoneNumber") String phoneNumber,
			@DefaultValue("1") @QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			@QueryParam("onlyFilteredAggregation") Boolean onlyFilteredAggregation,
			@RequestBody(required = false, description = "location params as EsLocationListParams", content = @Content(schema = @Schema(implementation = EsLocationListParams.class))) EsLocationListParams location) {
		try {
			if (max > 50) {
				max = 50;
			}
			MapBounds bounds = null;
			if (top != null || bottom != null || left != null || right != null) {
				bounds = new MapBounds();
				bounds.setBottom(bottom);
				bounds.setLeft(left);
				bounds.setRight(right);
				bounds.setTop(top);
			}
			MapBoundParams mapBoundsParams = new MapBoundParams();
			MapSearchParams mapSearchParams = new MapSearchParams();
			mapSearchParams.setFrom(offset);
			mapBoundsParams.setBounds(bounds);
			mapSearchParams.setLimit(max);
			mapSearchParams.setSortOn(sortOn);
			mapSearchParams.setSortType(SortTypeEnum.DESC);
			mapSearchParams.setMapBoundParams(mapBoundsParams);
			String loc = location.getLocation();
			if (loc != null) {
				if (loc.contains("/")) {
					String[] locationArray = loc.split("/");
					List<List<MapGeoPoint>> multiPolygonPoint = esUtility.multiPolygonGenerator(locationArray);
					mapBoundsParams.setMultipolygon(multiPolygonPoint);
				} else {
					mapBoundsParams.setPolygon(esUtility.polygonGenerator(loc));
				}
			}
			MapAggregationResponse aggregationResult = null;
			if (offset == 0) {
				aggregationResult = userListService.mapAggregate(index, type, user, profession, phoneNumber, email, sex,
						institution, name, userName, createdOnMaxDate, createdOnMinDate, userGroupList,
						lastLoggedInMinDate, lastLoggedInMaxDate, role, geoShapeFilterField, taxonRole, taxonomyList,
						mapSearchParams);
			}
			MapSearchQuery mapSearchQuery = esUtility.getMapSearchQuery(user, profession, phoneNumber, email, sex,
					institution, name, userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
					lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);
			UserListData result = userListService.getUserListData(request, index, type, geoAggregationField,
					geoShapeFilterField, nestedField, aggregationResult, mapSearchQuery);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
}
