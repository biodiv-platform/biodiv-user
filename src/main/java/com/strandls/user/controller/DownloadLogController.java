package com.strandls.user.controller;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.ApiConstants;
import com.strandls.user.pojo.DownloadLogListMapping;
import com.strandls.user.service.DowloadLogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("User Service")
@Path(ApiConstants.V1 + ApiConstants.DOWNLOADLOG)
public class DownloadLogController {

	private static final Logger logger = LoggerFactory.getLogger(DownloadLogController.class);

	@Inject
	private DowloadLogService downloadLogService;

	@GET
	@Path(ApiConstants.LIST)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Fetch the Download Log list", notes = "Returns the Download Log  list", response = DownloadLogListMapping.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to fetch the data", response = String.class) })

	public Response observationList(@DefaultValue("createdOn") @QueryParam("sort") String sortOn,
			@DefaultValue("0") @QueryParam("offset") String Offset,
			@DefaultValue("10") @QueryParam("limit") String Limit) {

		try {
			Integer offset = Integer.parseInt(Offset);
			Integer limit = Integer.parseInt(Limit);
			DownloadLogListMapping result = downloadLogService.getDownloadLogList(sortOn, limit, offset);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}

	}

}
