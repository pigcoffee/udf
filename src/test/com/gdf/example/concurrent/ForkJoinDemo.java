package com.gdf.example.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class ForkJoinDemo {
	public static void main(String args[]) {
		ForkJoinPool pool = new ForkJoinPool();
		
		try {
			CountTask task1 = new CountTask(1, 10);
			
			Future<Integer> result = pool.submit(task1);
		
			System.out.println("result 1 = " + result.get());
			
			CountTask task2 = new CountTask(1, 1000);
			
			Future<Integer> result2 = pool.submit(task2);
		
			System.out.println("result 2 = " + result2.get());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class CountTask extends RecursiveTask<Integer> {
	private static final long serialVersionUID = 1L;
	private int start,end;
	private static final int splitSize = 2;
	
	public CountTask(int start, int end) {
		this.start = start;
		this.end = end;
	}

	@Override
	protected Integer compute() {
		// TODO Auto-generated method stub
		int sum = 0;
		boolean canCompute = (end-start) <= splitSize;
		if(canCompute) {
			for(int i = start;i<=end;i++){
				sum += i;
			}
		} else {
			int middle = (start+end)/2;
			CountTask firstTask = new CountTask(start, middle);
			CountTask secondTask = new CountTask(middle+1, end);
			
			firstTask.fork();
			secondTask.fork();
			
			int firstResult = firstTask.join();
			int secondResult = secondTask.join();
			
			sum = firstResult + secondResult;
		}
		return sum;
	}
	
}
