package com.udf.mq.rocketmq.support;

import java.nio.charset.Charset;

import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MQUtil {
	public static org.apache.rocketmq.common.message.Message convertToRocketMessage(
	        ObjectMapper objectMapper, String charset,
	        String destination, org.springframework.messaging.Message<?> message) {
		Object payloadObj = message.getPayload();
		byte[] payloads;

		if (payloadObj instanceof String) {
			payloads = ((String) payloadObj).getBytes(Charset.forName(charset));
		} else if (payloadObj instanceof byte[]) {
			payloads = (byte[]) message.getPayload();
		} else {
			try {
				String jsonObj = objectMapper.writeValueAsString(payloadObj);

				payloads = jsonObj.getBytes(Charset.forName(charset));

			} catch (Exception e) {
				throw new RuntimeException("convert to RocketMQ message failed.", e);
			}
		}

		String[] tempArr = destination.split(":", 2);
		String topic = tempArr[0];
		String tags = "";
		if (tempArr.length > 1) {
			tags = tempArr[1];
		}

		org.apache.rocketmq.common.message.Message rocketMsg = new org.apache.rocketmq.common.message.Message(topic, tags, payloads);

		MessageHeaders headers = message.getHeaders();
		if (headers != null && !headers.isEmpty()) {
			Object keys = headers.get(RocketMQHeaders.KEYS);
			if (!StringUtils.isEmpty(keys)) { // if headers has 'KEYS', set rocketMQ message key
				rocketMsg.setKeys(keys.toString());
			}
			
			Object flagObj = headers.get("FLAG");
			if(flagObj == null) {
				flagObj = "0";
			}
			int flag = 0;
			try {
				flag = Integer.parseInt(flagObj.toString());
			} catch (NumberFormatException e) {
				// Ignore it
				//log.info("flag must be integer, flagObj:{}", flagObj);
			}
			rocketMsg.setFlag(flag);

			//Object waitStoreMsgOkObj = headers.getOrDefault("WAIT_STORE_MSG_OK", "true");
			Object waitStoreMsgOkObj = headers.get("WAIT_STORE_MSG_OK");
			if(waitStoreMsgOkObj == null)
				waitStoreMsgOkObj = "true";
			boolean waitStoreMsgOK = Boolean.TRUE.equals(waitStoreMsgOkObj);
			rocketMsg.setWaitStoreMsgOK(waitStoreMsgOK);
			/*
			headers.entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), RocketMQHeaders.KEYS)
	                    && !Objects.equals(entry.getKey(), "FLAG")
	                    && !Objects.equals(entry.getKey(), "WAIT_STORE_MSG_OK")) // exclude "KEYS", "FLAG", "WAIT_STORE_MSG_OK"
	                .forEach(entry -> {
	                    rocketMsg.putUserProperty("USERS_" + entry.getKey(), String.valueOf(entry.getValue())); // add other properties with prefix "USERS_"
	                });
	                */

		}

		return rocketMsg;
	}	
}
