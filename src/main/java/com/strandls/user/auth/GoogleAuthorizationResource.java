package com.strandls.user.auth;

import java.util.HashMap;
import java.util.Map;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.jwt.ClaimsSet;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.io.JWTReader;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.ApiConstants;
import com.strandls.user.pojo.User;
import com.strandls.user.service.AuthenticationService;
import com.strandls.user.service.UserService;
import com.strandls.user.util.AuthUtility;
import com.strandls.user.util.PropertyFileUtil;

// OpenAPI 3 (Jakarta-compatible)
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@Tag(name = "Google Service")
@Path(ApiConstants.GOOGLE_CALLBACK)
public class GoogleAuthorizationResource {

	private static final Logger logger = LoggerFactory.getLogger(GoogleAuthorizationResource.class);

	@Context
	private UriInfo uriInfo;

	@Inject
	private AuthenticationService authenticationService;

	@Inject
	private UserService userService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response authorize(@QueryParam("code") String code, @QueryParam("state") String state) {
		try {
			OAuthClientRequest request = OAuthClientRequest.tokenProvider(OAuthProviderType.GOOGLE).setCode(code)
					.setClientId(PropertyFileUtil.fetchProperty("config.properties", "googleId"))
					.setClientSecret(PropertyFileUtil.fetchProperty("config.properties", "googleSecret"))
					.setRedirectURI(UriBuilder.fromUri(uriInfo.getBaseUri()).path("oauth2callback").build().toString())
					.setGrantType(GrantType.AUTHORIZATION_CODE).buildBodyMessage();
			OAuthClient client = new OAuthClient(new URLConnectionClient());
			OAuthJSONAccessTokenResponse oAuthResponse = client.accessToken(request);
			String jwtToken = oAuthResponse.getParam("id_token");
			JWT jwt = new JWTReader().read(jwtToken);
			ClaimsSet claims = jwt.getClaimsSet();
			String email = claims.getCustomField("email", String.class);
			Map<String, Object> tokens = new HashMap<>();
			User user = this.userService.getUserByEmail(email);
			if (user == null) {
				tokens.put("status", false);
				tokens.put("message", email);
				tokens.put("authCode", AuthUtility.buildTokenWithProp(JwtClaims.SUBJECT, email));
				return Response.status(Status.OK).entity(tokens).build();
			}
			tokens = this.authenticationService.buildTokens(AuthUtility.createUserProfile(user), user, true);
			tokens.put("status", true);
			return Response.status(Status.OK).cookie(new NewCookie("BAToken", tokens.get("access_token").toString()))
					.cookie(new NewCookie("BRToken", tokens.get("refresh_token").toString())).entity(tokens).build();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return Response.status(Status.FORBIDDEN).entity(ex.getMessage()).build();
		}
	}

}
