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
		// Log.d("dabble", "findIndex:" + find + " dabbleArray:" +
		// String.valueOf(tmp));
		for (int j = startIndex; j < i; j++) {
			if (tmp[j] == find)
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
			if (cmpOriginal[i] == cmpRandom[i])
				continue;
			else
				break;
		}

		ArrayList<Integer> hintTiles = new ArrayList<Integer>();

		int findIndex = -1;
		if (i != 18) {
			int startIndex = 0;
			if (i >= 0 && i <= 2) {
				for (int j = 0; j < 3; j++) {
					findIndex = findIndex(cmpOriginal[j], cmpRandom, 0);
					if (findIndex < 0)
						break;
					cmpRandom[findIndex] = ';';
					hintTiles.add(findIndex);
				}
			}

			if (i >= 3 && i <= 6) {
				for (int j = 3; j < 7; j++) {
					findIndex = findIndex(cmpOriginal[j], cmpRandom, 3);
					if (findIndex < 0)
						break;
					cmpRandom[findIndex] = ';';
					hintTiles.add(findIndex);
				}
			}

			if (i >= 7 && i <= 11) {
				for (int j = 7; j < 12; j++) {
					findIndex = findIndex(cmpOriginal[j], cmpRandom, 7);
					if (findIndex < 0)
						break;
					cmpRandom[findIndex] = ';';
					hintTiles.add(findIndex);

				}
			}

			if (i >= 12 && i <= 17) {
				for (int j = 12; j < 18; j++) {
					findIndex = findIndex(cmpOriginal[j], cmpRandom, 12);
					if (findIndex < 0)
						break;
					cmpRandom[findIndex] = ';';
					hintTiles.add(findIndex);
				}
			}

		}
		if (findIndex < 0)
			hintTiles.clear();
		Log.d("dabble", "findIndex:" + findIndex + " dabbleString:" + String.valueOf(cmpOriginal)  + " dabbleArray:" + String.valueOf(cmpRandom) + " :gameactivity dabblearray:" + String.valueOf(gameActivity.dabbleArray));
			
		ArrayList<Tile> tileBlink = new ArrayList<Tile>();

		for (int j : hintTiles) {
			int resId = gameActivity.getResources().getIdentifier(
					"tile" + Integer.toString(j + 1), "id",
					gameActivity.getPackageName());
			Tile mTile = (Tile) gameActivity.findViewById(resId);
			tileBlink.add(mTile);
		}

		// for(int q : hintTiles) {
		// Log.d("dabble", "hinttask:" + q);
		// }
		return tileBlink;
	}

	@Override
	protected void onPostExecute(ArrayList<Tile> result) {
		// TODO Auto-generated method stub
		gameActivity.blinkHintTile(result);
	}
}
