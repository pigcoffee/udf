package com.udf.mq.rocketmq.core;

import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.messaging.Message;

public interface MQProducer {
	public SendResult syncSend(String destination, Message<?> message);
	public SendResult syncSend(String destination, Object payload);
	public SendResult syncSend(String destination, Object payload, long timeout);
}
