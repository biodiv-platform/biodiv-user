package com.strandls.user.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.strandls.mail_utility.model.EnumModel.NOTIFICATION_DATA;
import com.strandls.mail_utility.model.EnumModel.NOTIFICATION_FIELDS;
import com.strandls.mail_utility.producer.RabbitMQProducer;
import com.strandls.mail_utility.util.JsonUtil;
import com.strandls.user.RabbitMqConnection;
import com.strandls.user.dto.FirebaseDTO;
import com.strandls.user.pojo.FirebaseTokens;

public class NotificationScheduler extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

	Connection connection;
	FirebaseDTO firebaseDTO;
	List<FirebaseTokens> tokens;

	public NotificationScheduler(Connection connection, FirebaseDTO firebaseDTO, List<FirebaseTokens> tokens) {
		this.connection = connection;
		this.firebaseDTO = firebaseDTO;
		this.tokens = tokens;
	}

	@Override
	public void run() {
		try {
			if (this.tokens != null && this.tokens.size() > 0) {
				// This thread runs once and exits, so it opens and closes its own
				// channel rather than going through a per-thread-reuse provider - a
				// fresh, never-reused Thread has nothing to gain from that caching
				// and would just leave the channel orphaned open forever otherwise.
				try (Channel channel = connection.createChannel()) {
					RabbitMQProducer producer = new RabbitMQProducer(channel);
					publishNotifications(producer);
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	private void publishNotifications(RabbitMQProducer producer) {
		try {
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
				producer.produceNotification(RabbitMqConnection.EXCHANGE, RabbitMqConnection.NOTIFICATION_ROUTING_KEY,
						null, JsonUtil.mapToJSON(data));
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

}
