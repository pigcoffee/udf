/**  
* @Title: Processer.java
* @Package com.gdf.threadpool
* @Description: TODO()
* @author "huiq_wang"
* @date 2018年3月23日
* @version V1.0  
*/

package com.udf.threadpool;

/**
 * @ClassName: Processer
 * @Description: TODO()
 * @author "huiq_wang"
 * @date 2018年3月23日
 *
 */

public interface Processer {
	public void deal(Object obj) throws Exception;
	public void init() throws Exception;
}
