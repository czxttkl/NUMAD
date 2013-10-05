package edu.neu.zhengxingchen.madcourse.dabble;

import java.util.Arrays;

import android.graphics.Color;
import android.util.Log;

public class WordLookUp {
	static String[] wholeDict;
	
	public static int[] lookUp(char[] updated, char[] original) {
		int[] color = new int[19];
		Arrays.fill(color, Color.BLUE);
		
		String updatedString = String.valueOf(updated);
		String updatedRow1 = updatedString.substring(0, 3);
		String updatedRow2 = updatedString.substring(3,7);
		String updatedRow3 = updatedString.substring(7, 12);
		String updatedRow4 = updatedString.substring(12, 18);
		
		String originalString = String.valueOf(original);
		String originalRow1 = originalString.substring(0, 3);
		String originalRow2 = originalString.substring(3,7);
		String originalRow3 = originalString.substring(7, 12);
		String originalRow4 = originalString.substring(12, 18);
		
		int playSound = 0;
		
		Log.d("dabble", "wordlookup:" + updatedString + ":" + originalString);
		
		int i = -1;
		i = Arrays.binarySearch(wholeDict, updatedRow1);
		if ( i < 0) {
			color[0] = Color.RED;
			color[1] = Color.RED;
			color[2] = Color.RED;
		} else {
			color[0] = Color.GREEN;
			color[1] = Color.GREEN;
			color[2] = Color.GREEN;
			if(!updatedRow1.equals(originalRow1)) {
				playSound = 1;
			}
		}
		
		
		
		
		i = -1;
		i = Arrays.binarySearch(wholeDict, updatedRow2);
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
			
			if(!updatedRow2.equals(originalRow2)) {
				playSound = 1;
			}
		}
		
		i = -1;
		i = Arrays.binarySearch(wholeDict, updatedRow3);
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
			
			if(!updatedRow3.equals(originalRow3)) {
				playSound = 1;
			}
		}
		
		i = -1;
		i = Arrays.binarySearch(wholeDict, updatedRow4);
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
			
			if(!updatedRow4.equals(originalRow4)) {
				playSound = 1;
			}
		}
		
		
		color[18] = playSound; 
		return color; 
	}
}
