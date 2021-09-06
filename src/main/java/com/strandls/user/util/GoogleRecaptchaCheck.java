package com.strandls.user.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleRecaptchaCheck {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final String GOOGLE_API_ENDPOINT = "https://www.google.com/recaptcha/api/siteverify";

	public GoogleRecaptchaCheck() {
		// Constructor
	}

	public boolean isRobot(String userResponse) throws IOException {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost postRequest = new HttpPost(GOOGLE_API_ENDPOINT);

		List<NameValuePair> postParameters = new ArrayList<>();

		postParameters.add(new BasicNameValuePair("secret",
				PropertyFileUtil.fetchProperty("config.properties", "recaptchaSecret")));
		postParameters.add(new BasicNameValuePair("response", userResponse));
		postRequest.setEntity(new UrlEncodedFormEntity(postParameters));

		CloseableHttpResponse response = null;

		log.debug(userResponse);

		try {
			response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream body = response.getEntity().getContent();
				ObjectMapper mapper = new ObjectMapper();

				Map<String, Object> json;
				json = mapper.readValue(body, new TypeReference<Map<String, Object>>() {
				});
				if (json != null && (Boolean) json.get("success")) {
					return false;
				}
				return true;
			}
			return true;
		} finally {
			HttpClientUtils.closeQuietly(httpClient);
			HttpClientUtils.closeQuietly(response);
		}
	}
}
