package com.strandls.user.util;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class SNSUtil {

	private static final Logger logger = LoggerFactory.getLogger(SNSUtil.class);

	@Inject
	private SnsClient snsClient;

	public void sendSMS(String message, String phoneNumber) {
		try {
			MessageAttributeValue smsTypeAttribute = MessageAttributeValue.builder().dataType("String")
					.stringValue("Transactional").build();

			Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
			messageAttributes.put("AWS.SNS.SMS.SMSType", smsTypeAttribute);

			PublishRequest request = PublishRequest.builder().message(message).phoneNumber(phoneNumber)
					.messageAttributes(messageAttributes).build();

			snsClient.publish(request);

		} catch (SdkException e) {
			logger.error("Error sending SMS: {}", e.getMessage());
		}
	}

}
