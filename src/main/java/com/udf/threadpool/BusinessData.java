/**  
* @Title: BusinessData.java
* @Package com.gdf.threadpool
* @Description: TODO()
* @author "huiq_wang"
* @date 2018年3月23日
* @version V1.0  
*/

package com.udf.threadpool;

import java.util.List;

/**
 * @ClassName: BusinessData
 * @Description: TODO()
 * @author "huiq_wang"
 * @param <T>
 * @date 2018年3月23日
 *
 */

public interface BusinessData<T> {
	public List<T> getBusinessData();
}

