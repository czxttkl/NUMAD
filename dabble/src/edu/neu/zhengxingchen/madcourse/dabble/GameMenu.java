package edu.neu.zhengxingchen.madcourse.dabble;

import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;
import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.view.ViewPager.LayoutParams;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class GameMenu extends Activity implements OnClickListener {

	private volatile boolean acknowledgementPopedUp = false;
//	private boolean musicShouldPause = true;
	PopupWindow acknowledgementPopupWindowMenu;
	SharedPreferences mSharedPreferences;
	public View continueButton;
	public static GameMenu instance;
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(Music.musicShouldPause && !Music.musicPaused) {
			Music.pause(this);
			Music.musicPaused = true;
		}
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if( Music.musicPaused) {
			Music.start(this);
			Music.musicPaused = false;
		}
		Music.musicShouldPause = true;
		mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
//		Log.d("dabble", "gamemenu prefsaved:" + Prefs.getSaved(getBaseContext()));
		
		if(!Prefs.getSaved(getBaseContext()))
			continueButton.setEnabled(false);
		else
			continueButton.setEnabled(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game_menu);
//		initAcknowledgements();
		Music.start(this, R.raw.background);
		
		continueButton = findViewById(R.id.continue_button);
		continueButton.setOnClickListener(this);
		
		View newButton = findViewById(R.id.new_button);
		newButton.setOnClickListener(this);
		View settingsButton = findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(this);
		View acknowledgementButton = findViewById(R.id.acknowledgement_button);
		acknowledgementButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);
		View twoPlayerButton = findViewById(R.id.two_player_button);
		twoPlayerButton.setOnClickListener(this);
		View dummyButton = findViewById(R.id.dummy_button);
		dummyButton.setOnClickListener(this);
		
		
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		instance = this;
//		Log.d("dabble", "gamemenu oncreate");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		if( acknowledgementPopedUp) {
			acknowledgementPopupWindowMenu.dismiss();
			acknowledgementPopedUp = false;
		}
		return true;
	}

	
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.continue_button) {
			 initContinueGame();
		} else if (id == R.id.settings_button) {
//			Intent i = new Intent(this, Prefs.class);
//			startActivity(i);
			initSettings();
		} else if (id == R.id.acknowledgement_button) {
			initAcknowledgements();
		} else if (id == R.id.new_button) {
			initNewGame();
		} else if (id == R.id.exit_button) {
			initQuit();
		} else if (id == R.id.two_player_button) {
			initDabbleWaitRoom();
		} else if (id == R.id.dummy_button) {
			initDummy();
		}
	}

	private void initDummy() {
		Log.d("initdummy", "initDummy");
		TelephonyManager telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		KeyValueAPI.put(Global.USER_NAME, Global.PASSWORD, Global.SERVER_KEY_GUY_LIST, "");
		KeyValueAPI.clearKey(Global.USER_NAME, Global.PASSWORD, "356489052133381");
		KeyValueAPI.clearKey(Global.USER_NAME, Global.PASSWORD, "000000000000000");
		KeyValueAPI.clearKey(Global.USER_NAME, Global.PASSWORD, telMgr.getDeviceId());
	}

	private void initDabbleWaitRoom() {
		
		Intent i = new Intent();
		
		if(Prefs.getFirstEnterTwoPlayer(this)) {
			i.setClass(this, Viewpager.class);
		} else {
			i.setClass(this, DabbleWaitRoom.class);
		}
		
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		Music.musicShouldPause = false;
		Prefs.setFirstEnterTwoPlayer(this, false);
		
/*		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.setClass(this, ShuffleBoard.class);
		startActivity(i);
		finish();*/
	}

	private void initQuit() {
		Intent i = new Intent();
		i.setClass(this, Exit.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	private void initContinueGame() {
		Intent i = new Intent();
		i.setClass(this, GameActivity.class);
		i.putExtra("continue", true);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		Log.d("dabble", "Gamemenu initcontinue game:" + Prefs.getSaved(getBaseContext()));
		Music.musicShouldPause = false;
	}

	private void initNewGame() {
		Intent i = new Intent();
		i.setClass(this, GameActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		Music.musicShouldPause = false;
	}

	private void initAcknowledgements() {
		if (!acknowledgementPopedUp) {
			acknowledgementPopedUp = true;
			LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);

			View acknowledgementPopupView = layoutInflater.inflate(
					R.layout.acknowledgements_popup2, null);

			final PopupWindow acknowledgementPopupWindow = new PopupWindow(
					acknowledgementPopupView, 600, LayoutParams.WRAP_CONTENT);
			
			acknowledgementPopupWindowMenu = acknowledgementPopupWindow;
			
			// aboutpopupWindow.setBackgroundDrawable(new BitmapDrawable());
			acknowledgementPopupWindow.showAtLocation(acknowledgementPopupView, Gravity.CENTER, 0,
					0);
			Button dismissButton = (Button) acknowledgementPopupView
					.findViewById(R.id.dabble_dismiss_button);
			dismissButton.setClickable(true);
			dismissButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					acknowledgementPopupWindow.dismiss();
					acknowledgementPopedUp = false;
				}
			});
		}
	}
	
	private void initSettings() {
		Intent i = new Intent();
		i.setClass(GameMenu.this, Prefs.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		Music.musicShouldPause = false;
	}
	
	// Listener defined by anonymous inner class.
	public OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
//			Log.d("dabble", "change preference");
			if (key.equals("music")) {
				boolean music = sharedPreferences.getBoolean(key, true);
				if (!music) {
					Music.stop(GameMenu.this);
				} else {
					Music.play(GameMenu.this, R.raw.background);
				}
			}
		}
	};

	
}
