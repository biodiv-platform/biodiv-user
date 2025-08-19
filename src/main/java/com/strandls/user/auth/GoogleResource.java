package com.strandls.user.auth;

import java.net.URI;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.ApiConstants;
import com.strandls.user.util.PropertyFileUtil;

// OpenAPI 3 (Jakarta-compatible)
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@Tag(name = "Google Callback Service")
@Path(ApiConstants.GOOGLE)
public class GoogleResource {

	private static final Logger logger = LoggerFactory.getLogger(GoogleResource.class);

	@Context
	private UriInfo uriInfo;

	@GET
	@Produces("text/html")
	public Response authenticate_google() {
		try {
			OAuthClientRequest request = OAuthClientRequest.authorizationProvider(OAuthProviderType.GOOGLE)
					.setClientId(PropertyFileUtil.fetchProperty("config.properties", "googleId"))
					.setResponseType("code").setScope("openid profile email")
					.setRedirectURI(UriBuilder.fromUri(uriInfo.getBaseUri()).path("oauth2callback").build().toString())
					.buildQueryMessage();
			URI redirect = new URI(request.getLocationUri());
			return Response.seeOther(redirect).build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.FORBIDDEN).entity(ex.getMessage()).build();
		}
	}
}
