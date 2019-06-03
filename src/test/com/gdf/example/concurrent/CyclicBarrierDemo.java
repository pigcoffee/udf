package com.gdf.example.concurrent;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CyclicBarrierDemo implements Runnable {
	
	//创建拦截线程数为4，线程都达到Barrier后执行当前类的run方法的CyclicBarrier
	private CyclicBarrier cyclicBarrier = new CyclicBarrier(4, this);
	
	private Executor executor = Executors.newFixedThreadPool(4);
	
	private ConcurrentHashMap<String, Integer> count = new ConcurrentHashMap<String, Integer>();

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int result = 0;
		for(Map.Entry<String, Integer> entry : count.entrySet()){
			result += entry.getValue();
		}
		
		count.put(Thread.currentThread().getName(), result);
		System.out.println(count.toString());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void calculate() {
		for(int i = 0; i < 4; i++){
			executor.execute(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					count.put(Thread.currentThread().getName(), 10);
					System.out.println(Thread.currentThread().getName() + " is waiting.");
					try {
						cyclicBarrier.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BrokenBarrierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println(Thread.currentThread().getName() + " is working.");
				}
				
			});
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new CyclicBarrierDemo().calculate();
	}

}
