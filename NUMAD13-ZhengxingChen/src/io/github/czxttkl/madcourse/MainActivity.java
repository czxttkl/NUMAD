package io.github.czxttkl.madcourse;


import java.security.PublicKey;

import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.mobileClass.PhoneCheckAPI;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager.LayoutParams;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MainActivity extends Activity {

	private volatile boolean aboutPopedUp = false;
	private TelephonyManager telMgr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		PhoneCheckAPI.doAuthorization(this);
		
		setSudokuButton();
		setDictionaryButton();
		setAboutButton();
		setGenerateErrorButton();
		setCommunicationButton();
		setDabbleButton();
		setTwoPlayerDabbleButton();
		setFinalProjectButton();
		setQuitButton();
	}

	private void setFinalProjectButton() {
		final TextView finalTv = (TextView) findViewById(R.id.final_tv);
		finalTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent debugIntent = new Intent(MainActivity.this, edu.neu.mhealth.debug.MainActivity.class);
				startActivity(debugIntent);
				finish();
			}
		});
	}
	
	private void setQuitButton() {
		final TextView quitTv = (TextView) findViewById(R.id.quit_tv);
		quitTv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
	}
	
	private void setTwoPlayerDabbleButton() {
		final TextView quitTv = (TextView) findViewById(R.id.two_player_dabble_tv);
		quitTv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent dabbleIntent = new Intent();
				//ComponentName sudokuComponentName = new ComponentName("io.github.czxttkl.zhengxingchen.sudoku", Sudoku.class.getName());
				//sudokuIntent.setComponent(sudokuComponentName);
				dabbleIntent.setAction("edu.neu.zhengxingchen.madcourse.dabble.launch");
				dabbleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(dabbleIntent);
			}
			
		});
	}
	
	

	private void setDabbleButton() {
		final TextView quitTv = (TextView) findViewById(R.id.dabble_tv);
		quitTv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent dabbleIntent = new Intent();
				//ComponentName sudokuComponentName = new ComponentName("io.github.czxttkl.zhengxingchen.sudoku", Sudoku.class.getName());
				//sudokuIntent.setComponent(sudokuComponentName);
				dabbleIntent.setAction("edu.neu.zhengxingchen.madcourse.dabble.launch");
				dabbleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(dabbleIntent);
			}
			
		});
	}
	
	private void setGenerateErrorButton() {
		final TextView generateErrorTv = (TextView) findViewById(R.id.generr_tv);
		generateErrorTv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView tx = (TextView)findViewById(R.id.dismiss_about);
				tx.setText("aaa");
			}
			
		});
	}

	private void setAboutButton() {
		final TextView aboutTv = (TextView) findViewById(R.id.about_tv);
		telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		
		aboutTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				aboutTv.setShadowLayer(2, 3, 4, Color.BLUE);

				if (!aboutPopedUp) {
					aboutPopedUp = true;
					LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
							.getSystemService(LAYOUT_INFLATER_SERVICE);

					View aboutPopupView = layoutInflater.inflate(
							R.layout.about_popup, null);
					
					final PopupWindow aboutpopupWindow = new PopupWindow(
							aboutPopupView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);

					TextView IMEITv = (TextView)aboutPopupView.findViewById(R.id.imei_tv);
					if (telMgr.getDeviceId() != "") {
						IMEITv.append(telMgr.getDeviceId() + "\n");
					}
					
					//aboutpopupWindow.setBackgroundDrawable(new BitmapDrawable());
					aboutpopupWindow.showAtLocation(aboutPopupView, Gravity.CENTER, 0, 0);
					
					
					Button dismissButton = (Button) aboutPopupView
							.findViewById(R.id.dismiss_about);
					dismissButton.setClickable(true);
					dismissButton
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									aboutpopupWindow.dismiss();
									aboutTv.setShadowLayer(0, 0, 0, Color.BLACK);
									aboutPopedUp = false;
								}

							});
				}
			}
		});
		
	}

	private void setSudokuButton() {
		final TextView sudokuTv = (TextView) findViewById(R.id.sudoku_tv);
		sudokuTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sudokuIntent = new Intent();
				//ComponentName sudokuComponentName = new ComponentName("io.github.czxttkl.zhengxingchen.sudoku", Sudoku.class.getName());
				//sudokuIntent.setComponent(sudokuComponentName);
				sudokuIntent.setAction("edu.neu.zhengxingchen.madcourse.sudoku.launch");
				sudokuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(sudokuIntent);
			}
		});
	}
	
	private void setDictionaryButton() {
		final TextView dictionaryTv = (TextView) findViewById(R.id.dictionary_tv);
		dictionaryTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent dictionaryIntent = new Intent();
				dictionaryIntent.setAction("edu.neu.zhengxingchen.madcourse.dictionary.launch");
				dictionaryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(dictionaryIntent);
			}
		});
	}

	private void setCommunicationButton() {
		final TextView quitTv = (TextView) findViewById(R.id.communication_tv);
		quitTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent commIntent = new Intent();
				commIntent.setAction("edu.neu.zhengxingchen.madcourse.communication.launch");
				commIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(commIntent);
				
//				new Handler().post(new Runnable(){
//					@Override
//					public void run(){
//						Log.d("onclickcomm", "avaialbe:" + KeyValueAPI.isServerAvailable());
//						if(KeyValueAPI.isServerAvailable())
//							Log.d("onclickcomm", "get key:" + KeyValueAPI.get("czxttkl", "cZxttkl,1", "guyslist"));
//							Log.d("onclickcomm", "get key:" + KeyValueAPI.get("czxttkl", "cZxttkl,1", "356489052133381"));
//							Log.d("onclickcomm", "get key:" + KeyValueAPI.get("czxttkl", "cZxttkl,1", "000000000000000"));
//
//					}
//				});
//					
//				new Handler().postDelayed(new Runnable(){
//					@Override
//					public void run(){
//						if(KeyValueAPI.isServerAvailable()) {
//							Log.d("onclickcomm", "run:" + KeyValueAPI.put("czxttkl", "cZxttkl,1", "guyslist", ""));
//							Log.d("onclickcomm", "run:" + KeyValueAPI.clearKey("czxttkl", "cZxttkl,1", "356489052133381"));
//							Log.d("onclickcomm", "run:" + KeyValueAPI.clearKey("czxttkl", "cZxttkl,1", "1382050953285"));
//						}
//					}
//				}, 5000);
				
			}
			
		});
	}
	
	public void onClickCommunication(View v) {
	
	}

}
