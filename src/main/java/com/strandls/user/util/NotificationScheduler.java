package com.strandls.user.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.strandls.mail_utility.model.EnumModel.NOTIFICATION_DATA;
import com.strandls.mail_utility.model.EnumModel.NOTIFICATION_FIELDS;
import com.strandls.mail_utility.producer.RabbitMQProducer;
import com.strandls.mail_utility.util.JsonUtil;
import com.strandls.user.RabbitMqConnection;
import com.strandls.user.dto.FirebaseDTO;
import com.strandls.user.pojo.FirebaseTokens;

public class NotificationScheduler extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);


	Channel channel;
	FirebaseDTO firebaseDTO;
	List<FirebaseTokens> tokens;

	public NotificationScheduler(Channel channel, FirebaseDTO firebaseDTO, List<FirebaseTokens> tokens) {
		this.channel = channel;
		this.firebaseDTO = firebaseDTO;
		this.tokens = tokens;
	}

	@Override
	public void run() {
		try {
			if (this.tokens != null && this.tokens.size() > 0) {
				RabbitMQProducer producer = new RabbitMQProducer(channel);
				Map<String, Object> data = new HashMap<String, Object>();
				Map<String, Object> notification = new HashMap<String, Object>();
				notification.put(NOTIFICATION_DATA.TITLE.getAction(), firebaseDTO.getTitle());
				notification.put(NOTIFICATION_DATA.BODY.getAction(), firebaseDTO.getBody());
				String clickAction = firebaseDTO.getClickAction();
				notification.put(NOTIFICATION_DATA.CLICK_ACTION.getAction(),
						(clickAction == null || clickAction.isEmpty()) ? "/" : clickAction);
				String icon = firebaseDTO.getIcon();
				if (icon != null && !icon.isEmpty()) {
					notification.put(NOTIFICATION_DATA.ICON.getAction(), icon);
				}
				data.put(NOTIFICATION_FIELDS.NOTIFICATION.getAction(), JsonUtil.unflattenJSON(notification));
				for (FirebaseTokens token : tokens) {
					data.put(NOTIFICATION_FIELDS.TO.getAction(), token.getToken());
					producer.produceNotification(RabbitMqConnection.EXCHANGE,
							RabbitMqConnection.NOTIFICATION_ROUTING_KEY, null, JsonUtil.mapToJSON(data));
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

}
