
/**  
* @Title: ConcurrentProducer.java
* @Package com.gdf.threadpool
* @Description: TODO()
* @author "huiq_wang"
* @date 2018年3月23日
* @version V1.0  
*/

package com.udf.threadpool;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.udf.threadpool.MessageQueue;
import com.udf.threadpool.MessageQueueFactory;

/**
 * @ClassName: ConcurrentProducer
 * @Description: TODO()
 * @author "huiq_wang"
 * @date 2018年3月23日
 *
 */

public class ConcurrentProducer implements Runnable {
	int interval = 0;
	
	@Autowired
	private BusinessData businessData;
	
	public ConcurrentProducer() {
		this.interval = 1000;
	}
	
	public void init(int interval) {
		this.interval = interval;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		MessageQueue mq = MessageQueueFactory.getInstance().getMessageQueue(
				MessageQueueFactory.queueType.RECORD);

		while (true) {
			List<Object> array = null;
			//获取业务数据
			if(businessData != null) {
				array = businessData.getBusinessData();
			}
			if(array == null || array.size() == 0) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException n) {
					//None
				}
				System.out.println("businessData.getBusinessData() is null.");
				continue;
			}
System.out.println("array.size() = " + array.size());
			for (Object obj : array) {
				
				if(mq.isFull()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException n) {
						// None
					}
				}
				
				try {
					mq.push(obj);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			array = null;
		}
	}
}
