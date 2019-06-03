
/**  
* @Title: ConcurrentConsumer.java
* @Package com.gdf.threadpool
* @Description: TODO()
* @author "huiq_wang"
* @date 2018年3月23日
* @version V1.0  
*/

package com.udf.threadpool;
import org.springframework.beans.factory.annotation.Autowired;

import com.udf.threadpool.MessageQueue;
import com.udf.threadpool.MessageQueueFactory;


/**
 * @ClassName: ConcurrentConsumer
 * @Description: TODO()
 * @author "huiq_wang"
 * @date 2018年3月23日
 *
 */

public class ConcurrentConsumer implements Runnable {
	@Autowired
	private Processer process;
	
	public void init() {
		try {
			process.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		MessageQueue Q = MessageQueueFactory.getInstance().getMessageQueue(
                MessageQueueFactory.queueType.RECORD);
		System.out.println("ConcurrentConsumer is running...");
		while (true) {
			try {
                Object obj = (Object) Q.pop();
                if(obj == null) {
                	continue;
                }

                process.deal(obj);
            } catch (InterruptedException e) {
            	e.printStackTrace();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
}
