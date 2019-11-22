/**
 * 
 */
package com.strandls.user.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.pac4j.core.profile.CommonProfile;

import com.google.inject.Inject;
import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.user.ApiConstants;
import com.strandls.user.pojo.User;
import com.strandls.user.pojo.UserIbp;
import com.strandls.user.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Abhishek Rudra
 *
 */

@Api("User Service")
@Path(ApiConstants.V1 + ApiConstants.USER)
public class UserController {

	@Inject
	private UserService userSerivce;

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Dummy API Ping", notes = "Checks validity of war file at deployment", response = String.class)
	public Response ping() throws Exception {
		return Response.status(Status.OK).entity("PONG").build();
	}

	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@ApiOperation(value = "Find User by User ID", notes = "Returns User details", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Traits not found", response = String.class) })

	public Response getUser(@Context HttpServletRequest request) {

		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long uId = Long.parseLong(profile.getId());
			User user = userSerivce.fetchUser(uId);
			return Response.status(Status.OK).entity(user).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path(ApiConstants.IBP)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@ApiOperation(value = "Find User by User ID for ibp", notes = "Returns User details", response = UserIbp.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Traits not found", response = String.class) })

	public Response getUserIbp(@Context HttpServletRequest request) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long id = Long.parseLong(profile.getId());
			UserIbp ibp = userSerivce.fetchUserIbp(id);
			return Response.status(Status.OK).entity(ibp).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
}
