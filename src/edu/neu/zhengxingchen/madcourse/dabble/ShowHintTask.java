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

	@Override
	protected ArrayList<Tile> doInBackground(Void... arg0) {
		char c = 0;
		int index = -1;

		for (int j = 1; j < 19; j++) {
			int resId = gameActivity.getResources().getIdentifier(
					"tile" + Integer.toString(j), "id",
					gameActivity.getPackageName());
			Tile mTile = (Tile) gameActivity.findViewById(resId);

			if (mTile.getCharacterColor() == Color.RED) {
				c = mTile.getCharacter().charAt(0);
				index = gameActivity.dabbleString.indexOf(c);
				break;
			}
		}
		Log.d("dabble", "show hint task: " + c + ":" + index);
		
		String tmp = String.valueOf(gameActivity.dabbleArray);
		ArrayList<Integer> hintTiles = new ArrayList<Integer>();

		if (index >= 0 && index <= 2) {
			hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(0)));
			hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(1)));
			hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(2)));
		} else {
			if (index >= 3 && index <= 6) {
				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(3)));
				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(4)));
				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(5)));
				hintTiles.add(tmp.indexOf(gameActivity.dabbleString.charAt(6)));
			} else {
				if (index >= 7 && index <= 11) {
					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
							.charAt(7)));
					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
							.charAt(8)));
					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
							.charAt(9)));
					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
							.charAt(10)));
					hintTiles.add(tmp.indexOf(gameActivity.dabbleString
							.charAt(11)));
				} else {
					if (index >= 12 && index <= 17) {
						int start = 0;
						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
								.charAt(12)));
						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
								.charAt(13)));
						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
								.charAt(14)));
						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
								.charAt(15)));
						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
								.charAt(16)));
						hintTiles.add(tmp.indexOf(gameActivity.dabbleString
								.charAt(17)));
					}
				}

			}
		}

		boolean blink = false;
		
		ArrayList<Tile>  tileBlink = new ArrayList<Tile>();
		
		for (int j : hintTiles) {
			int resId = gameActivity.getResources().getIdentifier(
					"tile" + Integer.toString(j + 1), "id",
					gameActivity.getPackageName());
			Tile mTile = (Tile) gameActivity.findViewById(resId);
			tileBlink.add(mTile);
		}
		

		
		
		
		
		
		// TODO Auto-generated method stub
		 Log.d("dabble", "showhinttask:tileBlink size:" + tileBlink.size());
		return tileBlink;
	}

	@Override
	protected void onPostExecute(ArrayList<Tile> result) {
		// TODO Auto-generated method stub
		gameActivity.blinkHintTile(result);
	}
}
