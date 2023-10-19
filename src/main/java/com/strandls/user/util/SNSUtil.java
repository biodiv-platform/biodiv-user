package com.strandls.user.util;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class SNSUtil {

	private static final Logger logger = LoggerFactory.getLogger(SNSUtil.class);

	@Inject
	private SnsClient snsClient;

	public void sendSMS(String message, String phoneNumber) {
		// Create message attributes
		MessageAttributeValue maxPriceAttribute = MessageAttributeValue.builder().dataType("Number")
				.stringValue("0.003").build();

		MessageAttributeValue senderIDAttribute = MessageAttributeValue.builder().dataType("String")
				.stringValue("IBPTEST").build();

		MessageAttributeValue smsTypeAttribute = MessageAttributeValue.builder().dataType("String")
				.stringValue("Transactional").build();

		// Create a map to hold the message attributes
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("AWS.SNS.SMS.MaxPrice", maxPriceAttribute);
		messageAttributes.put("AWS.SNS.SMS.SenderID", senderIDAttribute);
		messageAttributes.put("AWS.SNS.SMS.SMSType", smsTypeAttribute);

		PublishRequest request = PublishRequest.builder().message(message).phoneNumber(phoneNumber)
				.messageAttributes(messageAttributes).build();

		PublishResponse result = snsClient.publish(request);

		System.out.println(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());
	}

}
