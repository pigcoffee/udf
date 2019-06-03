package com.udf.mq.rocketmq.core;

import java.util.List;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

public class MessageQueueSelectorImpl implements MessageQueueSelector {

	@Override
	public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
		// TODO Auto-generated method stub
		MQPartition partition = (MQPartition) arg;
        int hashCode = partition.getKeys().hashCode();
        if (hashCode < 0)
        	hashCode = Math.abs(hashCode);
        
        int index = hashCode % mqs.size();

        return mqs.get(index);
	}
}
