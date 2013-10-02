package edu.neu.zhengxingchen.madcourse.dabble;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

public class GameMenu extends Activity implements OnClickListener{
	
	private volatile boolean acknowledgementPopedUp = false;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Music.stop(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Music.play(this, R.raw.background);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		setContentView(R.layout.activity_game_menu);
		initAcknowledgements();
		
		View continueButton = findViewById(R.id.continue_button);
	    continueButton.setOnClickListener(this);
	    View newButton = findViewById(R.id.new_button);
	    newButton.setOnClickListener(this);
	    View settingsButton = findViewById(R.id.settings_button);
	    settingsButton.setOnClickListener(this);
	    View exitButton = findViewById(R.id.exit_button);
	    exitButton.setOnClickListener(this);
	    
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_menu, menu);
		return true;
	}
	
	public void onClick(View v) {
	      int id = v.getId();
		if (id == R.id.continue_button) {
//			startGame(Game.DIFFICULTY_CONTINUE);
		} else if (id == R.id.settings_button) {
			Intent i = new Intent(this, Prefs.class);
			startActivity(i);
		} else if (id == R.id.new_button) {
//			openNewGameDialog();
		} else if (id == R.id.exit_button) {
			finish();
		}
	   }


	private void initAcknowledgements() {
		Button acknowledgements = (Button)findViewById(R.id.acknowledgement_button);
		acknowledgements.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!acknowledgementPopedUp) {
					acknowledgementPopedUp = true;
					LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
							.getSystemService(LAYOUT_INFLATER_SERVICE);

					View aboutPopupView = layoutInflater.inflate(
							R.layout.acknowledgements_popup, null);
					
					final PopupWindow aboutpopupWindow = new PopupWindow(
							aboutPopupView, 600,
							LayoutParams.WRAP_CONTENT);
					
					//aboutpopupWindow.setBackgroundDrawable(new BitmapDrawable());
					aboutpopupWindow.showAtLocation(aboutPopupView, Gravity.CENTER, 0, 0);		
					
					Button dismissButton = (Button) aboutPopupView
							.findViewById(R.id.dismiss_button);
					dismissButton.setClickable(true);
					dismissButton
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									aboutpopupWindow.dismiss();
									acknowledgementPopedUp = false;
								}
							});
				}
			}
		});
	}

	
	
}
