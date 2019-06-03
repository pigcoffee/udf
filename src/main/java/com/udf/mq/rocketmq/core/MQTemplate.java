package com.udf.mq.rocketmq.core;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.AbstractMessageSendingTemplate;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udf.mq.rocketmq.support.MQUtil;

public class MQTemplate extends AbstractMessageSendingTemplate<String> implements MQProducer {
	private final static Logger log = LoggerFactory.getLogger(MQTemplate.class);
	
	@Autowired
	private MQProperties mqProperties;
	
	private String group;

	private DefaultMQProducer producer;

    private ObjectMapper objectMapper;

    private String charset = "UTF-8";

    private MessageQueueSelector selector;
    
    public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public MessageQueueSelector getMessageQueueSelector() {
        return selector;
    }

    public void setMessageQueueSelector(MessageQueueSelector messageQueueSelector) {
        this.selector = messageQueueSelector;
    }
    
    @PostConstruct
    public void init() throws MQClientException {
    	selector = new MessageQueueSelectorImpl();
        producer = new DefaultMQProducer(this.group);
        producer.setNamesrvAddr(mqProperties.getNameServer());
        producer.setSendMsgTimeout(mqProperties.getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(mqProperties.getRetryTimesWhenSendFailed());
        producer.setDefaultTopicQueueNums(mqProperties.getTopicQueueNum());
        
		objectMapper = new ObjectMapper();
        producer.start();
        log.info("MQ producer started, nameServer=" + producer.getNamesrvAddr() + ", producerGroup=" + this.group);
    }
    
    @PreDestroy
    public void destroy() {
        producer.shutdown();
        log.info("MQ producer shutdown, hostPort=" + producer.getNamesrvAddr() + ", producerGroup=" + this.group);
    }
    @Override
    public SendResult syncSend(String destination, Message<?> message) {
        return syncSend(destination, message, producer.getSendMsgTimeout());
    }
    public SendResult syncSend(String destination, Message<?> message, long timeout) {
        return syncSend(destination, message, timeout, 0);
    }
    public SendResult syncSend(String destination, Message<?> message, long timeout, int delayLevel) {
        if (message == null) {
            log.error("syncSend failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }

        try {
            long now = System.currentTimeMillis();
            org.apache.rocketmq.common.message.Message rocketMsg = MQUtil.convertToRocketMessage(objectMapper,
                charset, destination, message);
            if (delayLevel > 0) {
                rocketMsg.setDelayTimeLevel(delayLevel);
            }

            SendResult sendResult = producer.send(rocketMsg, timeout);
            long costTime = System.currentTimeMillis() - now;
            log.debug("send message cost: {} ms, msgId:{}", costTime, sendResult.getMsgId());
            return sendResult;
        } catch (Exception e) {
            log.error("syncSend failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }
    
    @Override
    public SendResult syncSend(String destination, Object payload) {
        return syncSend(destination, payload, producer.getSendMsgTimeout());
    }
    @Override
    public SendResult syncSend(String destination, Object payload, long timeout) {
        Message<?> message = this.doConvert(payload, null, null);

        if(payload instanceof MQPartition) {
        	return syncSendOrderly(destination, message, payload, producer.getSendMsgTimeout());
        } else {
        	return syncSend(destination, message, timeout);
        }
    }
    
    private SendResult syncSendOrderly(String destination, Message<?> message, Object arg, long timeout) {
        if (message == null || message.getPayload() == null) {
            log.error("syncSendOrderly failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }

        try {
            long now = System.currentTimeMillis();
            org.apache.rocketmq.common.message.Message rocketMsg = MQUtil.convertToRocketMessage(objectMapper,
                charset, destination, message);
            //设置Message Key
            MQPartition partition = (MQPartition) arg;
            rocketMsg.setKeys(partition.getKeys());
            
            SendResult sendResult = producer.send(rocketMsg, selector, arg, timeout);
            long costTime = System.currentTimeMillis() - now;
            log.debug("send message cost: {} ms, msgId:{}", costTime, sendResult.getMsgId());
            return sendResult;
        } catch (Exception e) {
            log.error("syncSendOrderly failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }
    
    @Override
    protected Message<?> doConvert(Object payload, Map<String, Object> headers, MessagePostProcessor postProcessor) {
        String content;

        if (payload instanceof String) {
            content = (String) payload;
        } else {
            // If payload not as string, use objectMapper change it.
            try {
                content = objectMapper.writeValueAsString(payload);

            } catch (JsonProcessingException e) {
                log.error("convert payload to String failed. payload:{}", payload);
                throw new RuntimeException("convert to payload to String failed.", e);
            }
        }

        MessageBuilder<?> builder = MessageBuilder.withPayload(content);
        if (headers != null) {
            builder.copyHeaders(headers);
        }
        builder.setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN);

        Message<?> message = builder.build();
        if (postProcessor != null) {
            message = postProcessor.postProcessMessage(message);
        }

        return message;
    }

	@Override
	protected void doSend(String arg0, Message<?> message) {
		// TODO Auto-generated method stub
		SendResult sendResult = syncSend(arg0, message);
	}
}
