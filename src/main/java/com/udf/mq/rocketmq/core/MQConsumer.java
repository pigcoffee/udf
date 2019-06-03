package com.udf.mq.rocketmq.core;

import org.apache.rocketmq.client.exception.MQClientException;

public interface MQConsumer {
	void subscribe(String topic, Class<?> classType, MQListener<Object> listener) throws MQClientException;
}
