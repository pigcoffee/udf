package com.udf.concurrent.service.impl;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.udf.concurrent.service.IConsumer;
import com.udf.concurrent.service.IProducer;

public class ConcurrentServiceDaemon {
	private final static Logger log = LoggerFactory.getLogger(ConcurrentServiceDaemon.class);
	
	private static final int MAX_TIMEOUT = 600000;
	
	private long timeOut = 1000L;
	private int threadPoolSize = 1;
	private int interval = 1000;
	private IProducer producer;
	private IConsumer consumer;
	
	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public IProducer getProducer() {
		return producer;
	}

	public void setProducer(IProducer producer) {
		this.producer = producer;
	}

	public IConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(IConsumer consumer) {
		this.consumer = consumer;
	}

	ExecutorService threadPool = null;
	
	public void init() {
		
		if(this.threadPoolSize < 0 || this.threadPoolSize > 128) {
			log.warn("thread pool size<" + threadPoolSize + "> config error. Please config it [0,128].");
			return;
		}
		if(this.timeOut < 0 || this.timeOut > ConcurrentServiceDaemon.MAX_TIMEOUT) {
			log.warn("timeOut <" + timeOut + "> config error. Please config it [0,600000].");
			return;
		}
		
		threadPool = Executors.newFixedThreadPool(this.threadPoolSize);
		
		this.process();
	}
	
	public void process() {
		while(true) {
			List<Object> lists = producer.produce();
			long start = System.currentTimeMillis();
			if(lists == null || lists.size() == 0) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				continue;
			}
			
			final ThreadPoolExecutor pool = (ThreadPoolExecutor)threadPool;

			final CountDownLatch countDown = new CountDownLatch(lists.size());
			for (final Object obj : lists) {
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							consumer.consume(obj);
						} catch (Exception e) {
							log.error(e.toString());
						} finally {
							countDown.countDown();
							log.info("countdown count = "+countDown.getCount() 
									+ ", queue:"+pool.getQueue().size()
									+",task count:"+pool.getTaskCount()
									+",completed task:"+pool.getCompletedTaskCount()
									);
						}
					}
				});
			}
			
			try {
				boolean result = countDown.await(this.timeOut, TimeUnit.SECONDS);
				if(!result) {
					log.warn("Consumer timeout:" +this.timeOut + " s, current service wait:" 
							+ (System.currentTimeMillis() - start)
							+ " ms. Total is: " + lists.size()
							+ ", queue:"+pool.getQueue().size()
							+",task count:"+pool.getTaskCount()
							+",completed task:"+pool.getCompletedTaskCount()
							);
				}
			} catch (InterruptedException e) {
				log.error("Consumer timeout:" +this.timeOut + ", Exception:" + e.toString());
			}
			
			log.info("Service batch size: " + lists.size() + ", consume: " + (System.currentTimeMillis() - start));
		}
	}
}
