package com.strandls.user.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.authentication_utility.util.PropertyFileUtil;
import com.strandls.user.pojo.Role;
import com.strandls.user.pojo.User;

import net.minidev.json.JSONArray;

public class AuthUtility {

	private static final Logger logger = LoggerFactory.getLogger(AuthUtility.class);

	private AuthUtility() {
	}

	private static final String CONFIG = "config.properties";

	public static CommonProfile createUserProfile(User user) {
		if (user == null)
			return null;
		try {
			Set<Role> roles = user.getRoles();
			Set<String> strRoles = new LinkedHashSet<>();
			if (roles != null) {
				for (Role r : roles) {
					strRoles.add(r.getAuthority());
				}
			}
			String email = user.getEmail();
			String mobile = user.getMobileNumber();
			return createUserProfile(user.getId(), user.getUserName(),
					(email == null || email.isEmpty()) ? mobile : email, strRoles);
		} catch (Exception e) {
			throw e;
		}
	}

	public static CommonProfile createUserProfile(Long userId, String username, String email, Set<String> authorities) {
		CommonProfile profile = new CommonProfile();
		updateUserProfile(profile, userId, username, email, authorities);
		return profile;
	}

	public static void updateUserProfile(CommonProfile profile, Long userId, String username, String email,
			Set<String> authorities) {
		if (profile == null)
			return;
		profile.setId(userId.toString());
		profile.addAttribute("id", userId);
		profile.addAttribute(Pac4jConstants.USERNAME, username);
		profile.addAttribute(CommonProfileDefinition.EMAIL, email);
		profile.addAttribute(JwtClaims.EXPIRATION_TIME, JWTUtil.getAccessTokenExpiryDate());
		profile.addAttribute(JwtClaims.ISSUED_AT, new Date());
		profile.setRoles(authorities);
		for (Object authority : authorities) {
			profile.addRole((String) authority);
		}
	}

	public static String buildTokenWithProp(String key, String value) {
		JwtGenerator<CommonProfile> generator = new JwtGenerator<CommonProfile>(
				new SecretSignatureConfiguration(PropertyFileUtil.fetchProperty(CONFIG, "jwtSalt")));
		Map<String, Object> claims = new HashMap<>();
		claims.put(key, value);
		return generator.generate(claims);
	}

	public static String verifyTokenWithProp(String token) {
		JwtAuthenticator authenticator = new JwtAuthenticator();
		authenticator.addSignatureConfiguration(
				new SecretSignatureConfiguration(PropertyFileUtil.fetchProperty(CONFIG, "jwtSalt")));
		CommonProfile profile = authenticator.validateToken(token);
		return profile.getAttribute(JwtClaims.SUBJECT).toString();
	}

	public static JSONObject verifyGoogleToken(String token) {
		JSONObject obj = null;
		try {
			StringBuilder response = new StringBuilder();
			URL oracle = new URL("https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + token);
			URLConnection yc = oracle.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			obj = new JSONObject(response.toString());
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return obj;
	}

	public static String getUserEmail(String token) {
		CommonProfile profile = AuthUtil.getProfileFromToken(token);
		return profile != null && profile.getEmail() != null  ? profile.getEmail() : null;
	}

	public static String[] getDefaultRoles() {
		String[] roleNames = PropertyFileUtil.fetchProperty(CONFIG, "user.defaultRoleNames").split(",");
		return roleNames;
	}

	public static boolean isAdmin(HttpServletRequest request) {
		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		JSONArray roles = (JSONArray) profile.getAttribute("roles");
		if (roles.contains("ROLE_ADMIN"))
			return true;
		return false;
	}
}
