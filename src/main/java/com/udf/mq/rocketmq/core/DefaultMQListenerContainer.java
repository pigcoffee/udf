package com.udf.mq.rocketmq.core;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udf.mq.rocketmq.annotation.ConsumeMode;
import com.udf.mq.rocketmq.annotation.MQMessageListener;
import com.udf.mq.rocketmq.annotation.MessageModel;
import com.udf.mq.rocketmq.annotation.SelectorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.Assert;

public class DefaultMQListenerContainer {
	private final static Logger log = LoggerFactory.getLogger(DefaultMQListenerContainer.class);
	
	@Autowired
	private MQProperties mqProperties;
	
	private long suspendCurrentQueueTimeMillis = 1000;
	
	private int delayLevelWhenNextConsume = 0;
	
	private String nameServer;

    private String consumerGroup;

    private String topic;

    private int consumeThreadMax = 32;

    private String charset = "UTF-8";
    
    private ObjectMapper objectMapper;

    private MQListener mqListener;

    private DefaultMQPushConsumer consumer;

    private Class<?> messageType;

	// The following properties came from @RocketMQMessageListener.
    private ConsumeMode consumeMode;
    private SelectorType selectorType;
    private String selectorExpression;
    private MessageModel messageModel;
    
    public long getSuspendCurrentQueueTimeMillis() {
        return suspendCurrentQueueTimeMillis;
    }

    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
        this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    }
    
    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }

    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }
    
    public String getNameServer() {
        return nameServer;
    }

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
	
	public ConsumeMode getConsumeMode() {
        return consumeMode;
    }

    public SelectorType getSelectorType() {
        return selectorType;
    }

    public String getSelectorExpression() {
        return selectorExpression;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

	public DefaultMQPushConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(DefaultMQPushConsumer consumer) {
		this.consumer = consumer;
	}
	
	public Class<?> getMessageType() {
		return messageType;
	}

	public void setMessageType(Class<?> messageType) {
		this.messageType = messageType;
	}

	public MQListener getMqListener() {
		return mqListener;
	}

	public void setMqListener(MQListener mqListener) {
		this.mqListener = mqListener;
	}


	public class DefaultMessageListenerConcurrently implements MessageListenerConcurrently {

        @SuppressWarnings("unchecked")
        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            for (MessageExt messageExt : msgs) {
                try {
                    long now = System.currentTimeMillis();
                    //System.out.println(messageExt.toString());
                    mqListener.onMessage(doConvertMessage(messageExt), messageExt.getMsgId());
                    long costTime = System.currentTimeMillis() - now;
                    log.debug("consume {} cost: {} ms", messageExt.getMsgId(), costTime);
                } catch (Exception e) {
                    log.warn("consume message failed. messageExt:{}", messageExt, e);
                    context.setDelayLevelWhenNextConsume(delayLevelWhenNextConsume);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }
	
	public class DefaultMessageListenerOrderly implements MessageListenerOrderly {

        @SuppressWarnings("unchecked")
        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {

            for (MessageExt messageExt : msgs) {
                //log.debug("received msg: {}", messageExt);
                try {
                    long now = System.currentTimeMillis();
                    mqListener.onMessage(doConvertMessage(messageExt), messageExt.getMsgId());
                    long costTime = System.currentTimeMillis() - now;
                    log.debug("consume {} cost: {} ms", messageExt.getMsgId(), costTime);
                } catch (Exception e) {
                    log.warn("consume message failed. messageExt:{}", messageExt, e);
                    context.setSuspendCurrentQueueTimeMillis(suspendCurrentQueueTimeMillis);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }

            return ConsumeOrderlyStatus.SUCCESS;
        }
    }
	
	@SuppressWarnings("unchecked")
    private Object doConvertMessage(MessageExt messageExt) {

        if (Objects.equals(messageType, MessageExt.class)) {
            return messageExt;
        } else {
            String str = new String(messageExt.getBody(), Charset.forName(charset));
            if (Objects.equals(messageType, String.class)) {
                return str;
            } else {
                // If msgType not string, use objectMapper change it.
                try {
                    return objectMapper.readValue(str, messageType);
                } catch (Exception e) {
                    log.info("convert failed. str:{}, msgType:{}", str, messageType);
                    throw new RuntimeException("cannot convert message to " + messageType, e);
                }
            }
        }
    }
	
	@PostConstruct
	private void init() throws MQClientException {
		Assert.notNull(mqListener, "Property 'rocketMQListener' is required");
		
		Class<?> clazz = AopProxyUtils.ultimateTargetClass(mqListener);
		MQMessageListener annotation = clazz.getAnnotation(MQMessageListener.class);
		
		log.info("Listener Init topic:{}, consumerGroup:{}, consumeThreadMax:{}, consumeMode:{}, messageModel:{}"
				,annotation.topic(),
				annotation.consumerGroup(),
				annotation.consumeThreadMax(),
				annotation.consumeMode(),
				annotation.messageModel());
		
		objectMapper = new ObjectMapper();  
		
		nameServer = mqProperties.getNameServer();
		consumerGroup = annotation.consumerGroup();
		topic = annotation.topic();
		consumeMode = annotation.consumeMode();
		messageModel = annotation.messageModel();
		consumeThreadMax = annotation.consumeThreadMax();
		selectorType = annotation.selectorType();

        consumer = new DefaultMQPushConsumer(annotation.consumerGroup());
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeThreadMax(consumeThreadMax);
        if (consumeThreadMax < consumer.getConsumeThreadMin()) {
            consumer.setConsumeThreadMin(consumeThreadMax);
        }

        switch (messageModel) {
            case BROADCASTING:
                consumer.setMessageModel(org.apache.rocketmq.common.protocol.heartbeat.MessageModel.BROADCASTING);
                break;
            case CLUSTERING:
                consumer.setMessageModel(org.apache.rocketmq.common.protocol.heartbeat.MessageModel.CLUSTERING);
                break;
            default:
                throw new IllegalArgumentException("Property 'messageModel' was wrong.");
        }

        switch (selectorType) {
            case TAG:
                consumer.subscribe(topic, selectorExpression);
                break;
            default:
                throw new IllegalArgumentException("Property 'selectorType' was wrong.");
        }

        switch (consumeMode) {
            case ORDERLY:
                consumer.setMessageListener(new DefaultMessageListenerOrderly());
                break;
            case CONCURRENTLY:
                consumer.setMessageListener(new DefaultMessageListenerConcurrently());
                break;
            default:
                throw new IllegalArgumentException("Property 'consumeMode' was wrong.");
        }

        consumer.start();
    }
}
