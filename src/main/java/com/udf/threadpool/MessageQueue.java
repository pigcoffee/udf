
/**  
* @Title: MessageQueue.java
* @Package com.gdf.threadpool
* @Description: TODO()
* @author "huiq_wang"
* @date 2018年3月23日
* @version V1.0  
*/

package com.udf.threadpool;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @ClassName: MessageQueue
 * @Description: TODO()
 * @author "huiq_wang"
 * @date 2018年3月23日
 *
 */

public class MessageQueue {
	private static final int DEFAULTSIZE = 1024;
	private int size = 0;
	private ArrayBlockingQueue<Object> queue = null;

	public MessageQueue() {
		queue = new ArrayBlockingQueue<Object>(DEFAULTSIZE);
		this.size = DEFAULTSIZE;
	}

	public MessageQueue(int size) {
		this.size = size;
		if (this.size < DEFAULTSIZE) {
			this.size = DEFAULTSIZE;
		}
		queue = new ArrayBlockingQueue<Object>(this.size);
	}

	/**
	 * 
	 * Inserts the specified element at the tail of this queue, <br/>
	 * waiting for space to become available if the queue is full.
	 * 
	 * @param t
	 *            the element to add
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public void push(Object t) throws InterruptedException {
		this.queue.put(t);
	}

	/**
	 * 
	 * Retrieves and removes the head of this queue, <br/>
	 * waiting if necessary until an element becomes available.
	 * 
	 * @return <br/>
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public Object pop() throws InterruptedException {
		return this.queue.take();
	}

	/**
	 * 
	 * Returns the number of elements in this queue . <br/>
	 * 
	 * @return the number of elements in this queue
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * 
	 * contain:Returns true if this queue contains the specified element.<br/>
	 * 
	 * @param o
	 *            o object to be checked for containment in this queue
	 * 
	 * @return true if this queue contains the specified element
	 */
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	/**
	 * 
	 * isFull:check whether the queue is full .<br/>
	 * 
	 * @return
	 */
	public boolean isFull() {
		if (queue.size() == this.size) {
			return true;
		}
		return false;
	}
}
