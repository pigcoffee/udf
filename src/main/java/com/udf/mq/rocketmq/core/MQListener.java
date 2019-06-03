package com.udf.mq.rocketmq.core;

public interface MQListener<T> {
	void onMessage(T request, String messageId) throws Exception;
}
