
/**  
* @Title: ServiceStartup.java
* @Package com.gdf.threadpool
* @Description: TODO()
* @author "huiq_wang"
* @date 2018年4月2日
* @version V1.0  
*/

package com.udf.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @ClassName: ServiceStartup
 * @Description: TODO()
 * @author "huiq_wang"
 * @date 2018年4月2日
 *
 */

public class ServiceStartup {
	private int threadNum = 1;
	private int interval = 1000;
	private ClassPathXmlApplicationContext context = null;
	
	public ServiceStartup(int threadNum, int interval, ClassPathXmlApplicationContext context) {
		this.threadNum = threadNum;
		this.interval = interval;
		this.context = context;
	}
	
	public void start(){
		
		context.start();
		
		ConcurrentProducer producer = (ConcurrentProducer)context.getBean("producer");
		producer.init(interval);
		
		System.out.println("threadNum = " + threadNum);
		if(threadNum <= 0 || threadNum > 64) {
			System.out.println("threadNum<" + threadNum + "> is error." );
			return;
		}
		
		ConcurrentConsumer[] worker = new ConcurrentConsumer[threadNum];
		ExecutorService pool = Executors.newFixedThreadPool(threadNum);
		
		//启动消费者
		for(int i = 0; i < threadNum; i++) {
			worker[i] = (ConcurrentConsumer)context.getBean("consumer");
			worker[i].init();
			pool.execute(worker[i]);
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//启动生产者
		Thread producerThread = new Thread(producer, "producer");
		producerThread.start();
	}
}
