package io.github.czxttkl.madcourse;


import edu.neu.mobileClass.PhoneCheckAPI;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager.LayoutParams;
import android.telephony.TelephonyManager;
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
		setDabbleButton();
		setQuitButton();

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
