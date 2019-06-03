/**  
* @Title: MessageQueueFactory.java
* @Package com.gdf.threadpool
* @Description: TODO()
* @author "huiq_wang"
* @date 2018年3月23日
* @version V1.0  
*/

package com.udf.threadpool;

import java.util.concurrent.ConcurrentHashMap;

import com.udf.threadpool.MessageQueue;

/**
 * @ClassName: MessageQueueFactory
 * @Description: TODO()
 * @author "huiq_wang"
 * @date 2018年3月23日
 *
 */

public class MessageQueueFactory {
	public static enum queueType {
		FILE, 		// 文件队列
		RECORD,		// 数据库表记录
		MESSAGE,	// 消息记录
		OTHER 		// 其他
	}

	private volatile static MessageQueueFactory instance = null;
	private ConcurrentHashMap<queueType, MessageQueue> m = new ConcurrentHashMap<queueType, MessageQueue>();
	private static Object O_O = new Object();

	/**
	 * 
	 * getInstance:单例，通过工厂类取得一个队列.
	 * 
	 * @return
	 */
	public static MessageQueueFactory getInstance() {
		if (instance == null) {
			synchronized (MessageQueueFactory.class) {
				if (instance == null) {
					instance = new MessageQueueFactory();
				}
			}
		}
		return instance;
	}

	/**
	 * 
	 * getMessageQueue:获取一个带有默认大小的队列ArrayBlockingQueue.
	 * 
	 * @param type
	 *            队列类型
	 * @return
	 */
	public MessageQueue getMessageQueue(queueType type) {
		MessageQueue q = m.get(type);
		if (q == null) {
			synchronized (O_O) {
				q = m.get(type);
				if (q == null) {
					q = new MessageQueue();
					m.put(type, q);
				}
			}
		}
		return q;
	}
}
