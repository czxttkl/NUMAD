package edu.neu.zhengxingchen.madcourse.dabble;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class ShowHintTask extends AsyncTask<Void, Void, ArrayList<Tile>> {

	GameActivity gameActivity;

	public ShowHintTask(GameActivity gameActivity) {
		this.gameActivity = gameActivity;
	}

	public int findIndex(char find, char[] tmp, int startIndex) {
		int i = tmp.length;
		for(int j = startIndex; j < i; j++) {
			if(tmp[j] == find)
				return j;
		}
		return -1;
		
	}
	
	@Override
	protected ArrayList<Tile> doInBackground(Void... arg0) {
		
		char[] cmpOriginal = gameActivity.dabbleString.toCharArray();
		char[] cmpRandom = gameActivity.dabbleArray.clone();
		int i;
		for (i = 0; i < 18; i++) {
			if(cmpOriginal[i] == cmpRandom[i])
				continue;
			else
				break;
		}
		
		ArrayList<Integer> hintTiles = new ArrayList<Integer>();
		
		
		if( i!=18 ) {
			int startIndex = 0;
			if(i >=0 && i<=2) {
				for( int j = 0 ; j <3; j++) {
					int findIndex = findIndex(cmpOriginal[j],cmpRandom, 0);
					cmpRandom[findIndex] = ';';
					hintTiles.add(findIndex);
				}
			}
			
			if(i >=3 && i<=6) {
				for( int j = 3 ; j <7; j++) {
					int findIndex = findIndex(cmpOriginal[j],cmpRandom, 3);
					cmpRandom[findIndex] = ';';
					hintTiles.add(findIndex);					
				}
			}
			
			if(i >=7 && i<=11) {
				for( int j = 7 ; j <12; j++) {
						int findIndex = findIndex(cmpOriginal[j],cmpRandom, 7);
						cmpRandom[findIndex] = ';';
						hintTiles.add(findIndex);
					
				}
			}
			
			if(i >=12 && i<=17) {
				for( int j = 12 ; j <18; j++) {
						int findIndex = findIndex(cmpOriginal[j],cmpRandom, 12);
						cmpRandom[findIndex] = ';';
						hintTiles.add(findIndex);	
				}
			}
			
				
			
			
			
		}
		
	/*	char c = 0;
		int index = -1;
		
		char[] tmpArray = new char[18];
		tmpArray = gameActivity.dabbleArray.clone();
		String tmpString = String.valueOf(tmpArray);
		
		for (int j = 1; j < 19; j++) {
			int resId = gameActivity.getResources().getIdentifier(
					"tile" + Integer.toString(j), "id",
					gameActivity.getPackageName());
			Tile mTile = (Tile) gameActivity.findViewById(resId);
			
			c = mTile.getCharacter().charAt(0);
			index = tmpString.indexOf(c);
			if( index == (j-1) ) {
				tmpArray[index] = ';';
				tmpString = String.valueOf(tmpArray);
			}
			else 
				break;
//			if (mTile.getCharacterColor() == Color.RED) {
//				
//				break;
//			}
		}
		Log.d("dabble", "show hint task: " + c + ":" + index);
		
		
		tmpArray = gameActivity.dabbleArray.clone();
		tmpString = String.valueOf(tmpArray);
		
		ArrayList<Integer> hintTiles = new ArrayList<Integer>();
		int ttt;
		
		if (index >= 0 && index <= 2) {
			
			for(int zzz = 0;zzz< 3; zzz++) {
				ttt = tmp.indexOf(gameActivity.dabbleString.charAt(zzz));
				hintTiles.add(ttt);
				tmpArray[zzz] = ';';
				tmp = String.valueOf(tmpArray);
			}
			
//			hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(0)));
//			hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(1)));
//			hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(2)));
		} else {
			if (index >= 3 && index <= 6) {

				for(int zzz = 3;zzz<7; zzz++) {
					ttt = tmp.indexOf(gameActivity.dabbleString.charAt(zzz));
					hintTiles.add(ttt);
					tmpArray[zzz] = ';';
					tmp = String.valueOf(tmpArray);
				}
				
//				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(3)));
//				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(4)));
//				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(5)));
//				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(6)));
			} else {
				if (index >= 7 && index <= 11) {
					for(int zzz = 7;zzz< 12; zzz++) {
						ttt = tmp.indexOf(gameActivity.dabbleString.charAt(zzz));
						hintTiles.add(ttt);
						tmpArray[zzz] = ';';
						tmp = String.valueOf(tmpArray);
					}
					
//					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//							.charAt(7)));
//					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//							.charAt(8)));
//					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//							.charAt(9)));
//					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//							.charAt(10)));
//					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//							.charAt(11)));
				} else {
					if (index >= 12 && index <= 17) {
						for(int zzz = 12;zzz<18; zzz++) {
							ttt = tmp.indexOf(gameActivity.dabbleString.charAt(zzz));
							hintTiles.add(ttt);
							tmpArray[zzz] = ';';
							tmp = String.valueOf(tmpArray);
						}
						
//						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//								.charAt(13)));
//						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//								.charAt(14)));
//						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//								.charAt(15)));
//						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//								.charAt(16)));
//						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
//								.charAt(17)));
					}
				}

			}
		}

		boolean blink = false;
		*/
		ArrayList<Tile>  tileBlink = new ArrayList<Tile>();
		
		for (int j : hintTiles) {
			int resId = gameActivity.getResources().getIdentifier(
					"tile" + Integer.toString(j + 1), "id",
					gameActivity.getPackageName());
			Tile mTile = (Tile) gameActivity.findViewById(resId);
			tileBlink.add(mTile);
		}
		
		for(int q : hintTiles) {
			Log.d("dabble", "hinttask:" + q);
		}
		
		
		
		
		// TODO Auto-generated method stub
//		 Log.d("dabble", "showhinttask:tileBlink size:" + tileBlink.size());
		return tileBlink;
	}

	@Override
	protected void onPostExecute(ArrayList<Tile> result) {
		// TODO Auto-generated method stub
		gameActivity.blinkHintTile(result);
	}
}
