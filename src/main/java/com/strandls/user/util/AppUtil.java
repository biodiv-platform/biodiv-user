package com.strandls.user.util;

import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InternetDomainName;

public class AppUtil {

	private final static Logger log = LoggerFactory.getLogger(AppUtil.class);

	private AppUtil() {
	}

	public enum VERIFICATION_TYPE {
		EMAIL, MOBILE
	};

	public enum VERIFICATION_ACTIONS {
		USER_REGISTRATION(0), FORGOT_PASSWORD(1);

		private int action = -1;

		private VERIFICATION_ACTIONS(int action) {
			this.action = action;
		}

		public int getAction() {
			return action;
		}
	};

	public enum AUTH_MODE {
		MANUAL("manual"), OAUTH_GOOGLE("oauth-google");

		private String action = null;

		private AUTH_MODE(String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}
	};

	public static String getVerificationType(String selectedType) {
		for (VERIFICATION_TYPE type : VERIFICATION_TYPE.values()) {
			if (type.name().equalsIgnoreCase(selectedType)) {
				return type.name();
			}
		}
		return null;
	}

	public static String getVerificationAction(int selectedAction) {
		for (VERIFICATION_ACTIONS action : VERIFICATION_ACTIONS.values()) {
			if (action.getAction() == selectedAction) {
				return action.name();
			}
		}
		return null;
	}

	public static Map<String, Object> generateResponse(boolean status, Object message) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("status", status);
		response.put("message", message);
		return response;
	}

	public static String encodeString(String data) {
		return new String(Base64.getEncoder().encode(data.getBytes()));
	}

	public static String decodeString(String data) {
		return new String(Base64.getDecoder().decode(data.getBytes()));
	}

	public static String generateOTP() {
		try {
			Random random = SecureRandom.getInstance("SHA1PRNG");
			return String.valueOf(random.nextInt(900000) + 100000);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public static String buildURI(HttpServletRequest request, String path, Map<String, String> params,
			boolean includeCtxPath) throws URISyntaxException {
		StringBuilder url = new StringBuilder();
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		String contextPath = request.getContextPath();
		url.append(scheme).append("://").append(serverName);
		if (includeCtxPath == true)
			url.append(contextPath);
		url.append(path);

		URIBuilder builder = new URIBuilder(url.toString());
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String key = entry.getKey();
				if (!key.isEmpty()) {
					String value = entry.getValue();
					builder.addParameter(key, value);
				}
			}
		}
		return builder.build().toString();
	}

	public static String capitalize(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	public static String getDomain(HttpServletRequest request) {
		String domain = "";
		String tmpDomain = request.getHeader(HttpHeaders.HOST);
		if (tmpDomain != null && !tmpDomain.isEmpty() && tmpDomain.contains(".")) {
			domain = InternetDomainName.from(tmpDomain).topDomainUnderRegistrySuffix().toString();
		}
		return domain;
	}

}
