package com.strandls.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.user.ApiConstants;
import com.strandls.user.pojo.DownloadLogData;
import com.strandls.user.pojo.DownloadLogListMapping;
import com.strandls.user.service.DowloadLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// --- OpenAPI 3 (Swagger for jakarta) ---
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Tag(name = "User Service")
@Path(ApiConstants.V1 + ApiConstants.DOWNLOADLOG)
public class DownloadLogController {

	private static final Logger logger = LoggerFactory.getLogger(DownloadLogController.class);

	@Inject
	private DowloadLogService downloadLogService;

	@GET
	@Path(ApiConstants.LIST)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Fetch the Download Log list", description = "Returns the Download Log  list")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully fetched data", content = @Content(schema = @Schema(implementation = DownloadLogListMapping.class))),
			@ApiResponse(responseCode = "400", description = "Unable to fetch the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response observationList(@DefaultValue("createdOn") @QueryParam("sort") String sortOn,
			@DefaultValue("0") @QueryParam("offset") String Offset,
			@DefaultValue("10") @QueryParam("limit") String Limit, @QueryParam("sourceType") String sourceType) {

		try {
			Integer offset = Integer.parseInt(Offset);
			Integer limit = Integer.parseInt(Limit);
			DownloadLogListMapping result = downloadLogService.getDownloadLogList(sourceType, sortOn, limit, offset);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.CREATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ValidateUser
	@Operation(summary = "Log the download", description = "Return a message indicating status of logging")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Download logged", content = @Content(schema = @Schema(type = "string", example = "Download logged"))),
			@ApiResponse(responseCode = "406", description = "Unable to log the download", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Unable to log the download", content = @Content(schema = @Schema(type = "string"))) })
	public Response logDocumentDownload(@Context HttpServletRequest request,
			@RequestBody(required = true, description = "The download log data to create", content = @Content(schema = @Schema(implementation = DownloadLogData.class))) DownloadLogData downloadLogData) {
		try {
			Boolean result = downloadLogService.createDownloadLog(request, downloadLogData);

			if (result != null && result)
				return Response.status(Status.OK).entity("Download logged").build();
			return Response.status(Status.NOT_ACCEPTABLE).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
}
