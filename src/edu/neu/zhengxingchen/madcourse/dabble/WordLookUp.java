package edu.neu.zhengxingchen.madcourse.dabble;

import java.util.Arrays;

import android.graphics.Color;
import android.util.Log;

public class WordLookUp {
	static String[] wholeDict;
	
	public static int[] lookUp(char[] wholeChar) {
		int[] color = new int[18];
		Arrays.fill(color, Color.BLUE);
		
		String whole = String.valueOf(wholeChar);
		String row1 = whole.substring(0, 3);
		String row2 = whole.substring(3,7);
		String row3 = whole.substring(7, 12);
		String row4 = whole.substring(12, 18);
		
		int i = -1;
		i = Arrays.binarySearch(wholeDict, row1);
		if ( i < 0) {
			color[0] = Color.RED;
			color[1] = Color.RED;
			color[2] = Color.RED;
		} else {
			color[0] = Color.GREEN;
			color[1] = Color.GREEN;
			color[2] = Color.GREEN;
		}
		
		i = -1;
		i = Arrays.binarySearch(wholeDict, row2);
		if ( i < 0) {
			color[3] = Color.RED;
			color[4] = Color.RED;
			color[5] = Color.RED;
			color[6] = Color.RED;
		} else {
			color[3] = Color.GREEN;
			color[4] = Color.GREEN;
			color[5] = Color.GREEN;
			color[6] = Color.GREEN;
		}
		
		i = -1;
		i = Arrays.binarySearch(wholeDict, row3);
		if ( i < 0) {
			color[7] = Color.RED;
			color[8] = Color.RED;
			color[9] = Color.RED;
			color[10] = Color.RED;
			color[11] = Color.RED;
		} else {
			color[7] = Color.GREEN;
			color[8] = Color.GREEN;
			color[9] = Color.GREEN;
			color[10] = Color.GREEN;
			color[11] = Color.GREEN;
		}
		
		i = -1;
		i = Arrays.binarySearch(wholeDict, row4);
		if ( i < 0) {
			color[12] = Color.RED;
			color[13] = Color.RED;
			color[14] = Color.RED;
			color[15] = Color.RED;
			color[16] = Color.RED;
			color[17] = Color.RED;
		} else {
			color[12] = Color.GREEN;
			color[13] = Color.GREEN;
			color[14] = Color.GREEN;
			color[15] = Color.GREEN;
			color[16] = Color.GREEN;
			color[17] = Color.GREEN;
		}
		

		return color; 
	}
}
