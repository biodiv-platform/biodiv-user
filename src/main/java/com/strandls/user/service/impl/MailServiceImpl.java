package com.strandls.user.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.strandls.mail_utility.model.EnumModel.FIELDS;
import com.strandls.mail_utility.model.EnumModel.INFO_FIELDS;
import com.strandls.mail_utility.model.EnumModel.MAIL_TYPE;
import com.strandls.mail_utility.model.EnumModel.RESET_PASSWORD;
import com.strandls.mail_utility.model.EnumModel.USER_REGISTRATION;
import com.strandls.mail_utility.model.EnumModel.WELCOME_MAIL;
import com.strandls.mail_utility.producer.RabbitMQProducer;
import com.strandls.mail_utility.util.JsonUtil;
import com.strandls.user.RabbitChannelProvider;
import com.strandls.user.RabbitMqConnection;
import com.strandls.user.pojo.User;
import com.strandls.user.service.MailService;
import com.strandls.user.util.AppUtil;
import com.strandls.user.util.PropertyFileUtil;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

public class MailServiceImpl implements MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

	@Inject
	private RabbitChannelProvider channelProvider;

	@Override
	public void sendActivationMail(HttpServletRequest request, User user, String otp) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(FIELDS.TO.getAction(), new String[] { user.getEmail() });
		data.put(FIELDS.SUBSCRIPTION.getAction(), true);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(USER_REGISTRATION.OTP.getAction(), otp);
		model.put(USER_REGISTRATION.USERNAME.getAction(), user.getUserName());
		model.put(USER_REGISTRATION.TYPE.getAction(), MAIL_TYPE.USER_REGISTRATION.getAction());
		data.put(FIELDS.DATA.getAction(), JsonUtil.unflattenJSON(model));

		Map<String, Object> mailData = new HashMap<String, Object>();
		mailData.put(INFO_FIELDS.TYPE.getAction(), MAIL_TYPE.USER_REGISTRATION.getAction());
		mailData.put(INFO_FIELDS.RECIPIENTS.getAction(), Arrays.asList(data));
		try {
			Channel channel = channelProvider.get();
			RabbitMQProducer producer = new RabbitMQProducer(channel);
			producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
					JsonUtil.mapToJSON(mailData));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void sendWelcomeMail(HttpServletRequest request, User user) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(FIELDS.TO.getAction(), new String[] { user.getEmail() });
		data.put(FIELDS.SUBSCRIPTION.getAction(), true);
		Map<String, Object> model = new HashMap<>();
		Properties config = PropertyFileUtil.fetchProperty("config.properties");
		model.put(WELCOME_MAIL.USERNAME.getAction(), AppUtil.capitalize(user.getUserName()));
		StringBuilder profileUrl = new StringBuilder();
		profileUrl.append("/user/show/").append(String.valueOf(user.getId()));
		model.put(WELCOME_MAIL.USER_PROFILE_URL.getAction(), profileUrl.toString());
		model.put(WELCOME_MAIL.SERVER_URL.getAction(), config.getProperty("serverUrl"));
		model.put(WELCOME_MAIL.SITENAME.getAction(), config.getProperty("siteName"));
		model.put(WELCOME_MAIL.FACEBOOK_URL.getAction(), config.getProperty("facebookUrl"));
		model.put(WELCOME_MAIL.TWITTER_URL.getAction(), config.getProperty("twitterUrl"));
		model.put(WELCOME_MAIL.FEEDBACKFORM_URL.getAction(), config.getProperty("feedbackFormUrl"));
		model.put(WELCOME_MAIL.MAIL_DEFAULT_FROM.getAction(), config.getProperty("mail_sender_email"));

		data.put(FIELDS.DATA.getAction(), JsonUtil.unflattenJSON(model));

		Map<String, Object> mailData = new HashMap<String, Object>();
		mailData.put(INFO_FIELDS.TYPE.getAction(), MAIL_TYPE.WELCOME_MAIL.getAction());
		mailData.put(INFO_FIELDS.RECIPIENTS.getAction(), Arrays.asList(data));
		try {
			Channel channel = channelProvider.get();
			RabbitMQProducer producer = new RabbitMQProducer(channel);
			producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
					JsonUtil.mapToJSON(mailData));
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	@Override
	public void sendForgotPasswordMail(HttpServletRequest request, User user, String otp) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(FIELDS.TO.getAction(), new String[] { user.getEmail() });
		data.put(FIELDS.SUBSCRIPTION.getAction(), true);
		Map<String, Object> model = new HashMap<>();
		model.put(RESET_PASSWORD.USERNAME.getAction(), user.getUserName());
		model.put(RESET_PASSWORD.OTP.getAction(), otp);
		model.put(RESET_PASSWORD.TYPE.getAction(), MAIL_TYPE.RESET_PASSWORD.getAction());

		data.put(FIELDS.DATA.getAction(), JsonUtil.unflattenJSON(model));
		Map<String, Object> mailData = new HashMap<String, Object>();
		mailData.put(INFO_FIELDS.TYPE.getAction(), MAIL_TYPE.RESET_PASSWORD.getAction());
		mailData.put(INFO_FIELDS.RECIPIENTS.getAction(), Arrays.asList(data));
		try {
			Channel channel = channelProvider.get();
			RabbitMQProducer producer = new RabbitMQProducer(channel);
			producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
					JsonUtil.mapToJSON(mailData));
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

}
