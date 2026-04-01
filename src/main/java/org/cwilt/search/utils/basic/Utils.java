/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.ArrayList;

public class Utils {
	public static float arraylistAverage(ArrayList<Float> nums){
		float sum = 0;
		for(float value : nums)
			sum += value;
		return sum / (float) nums.size();
	}
	
	
}
