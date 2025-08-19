package com.strandls.user.controller;

import java.util.List;

import com.strandls.user.ApiConstants;
import com.strandls.user.pojo.Role;
import com.strandls.user.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// --- OpenAPI 3 for Jakarta EE 10 / Jersey 3.x ---
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Tag(name = "Role Service")
@Path(ApiConstants.V1 + ApiConstants.ROLES)
public class RolesController {

	@Inject
	private RoleService roleService;

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Dummy API Ping", description = "Checks validity of war file at deployment")
	@ApiResponse(responseCode = "200", description = "Ping successful", content = @Content(schema = @Schema(type = "string", example = "PONG")))
	public Response pingRoles() {
		return Response.status(Status.OK).entity("PONG").build();
	}

	@GET
	@Path("all")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get all roles", description = "Returns all the roles available")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Roles retrieved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Role.class)))),
			@ApiResponse(responseCode = "404", description = "Roles not found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getAllRoles() {
		List<Role> roles = roleService.getAllRoles();
		return Response.status(Status.OK).entity(roles).build();
	}
}
