package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TestDictionary extends Activity{

	//protected Trie<String, String> trie = null;
	protected String[] sa = null;
	protected SoundPool sp = null;
	protected int beepStreamId = 0;
	private volatile boolean aboutPopedUp = false;
	protected volatile ArrayList<String> record = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_dictionary);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		try {
			new LoadDicTask(this).execute(getResources().getAssets().open("wordlist.txt"));
			new LoadBeepTask(this).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		initEditTextListener();
		initAcknowledgements();
	}


	private void initAcknowledgements() {
		Button acknowledgements = (Button)findViewById(R.id.acknowledgements);
		acknowledgements.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!aboutPopedUp) {
					aboutPopedUp = true;
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
							.findViewById(R.id.dismiss_about);
					dismissButton.setClickable(true);
					dismissButton
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									aboutpopupWindow.dismiss();
									aboutPopedUp = false;
								}
							});
				}
			}
		});
		
	}


	@Override
	protected void onResume() {
		super.onResume();
		
		EditText input = (EditText)findViewById(R.id.input);
//		Log.d("TD", input.isFocusableInTouchMode() + "");
//		if(input.isFocusableInTouchMode()){
//			input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
//			input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));           
//		}
//			input.requestFocus();
//		Log.i("clickable", "" + input.isClickable());
//		Log.i("focusable", "" + input.isFocusable());
		if(input.isFocusableInTouchMode() && input.isClickable() && input.isFocusable())
			showVirturalKeyboard();
	}

	private void showVirturalKeyboard(){
	    Timer timer = new Timer();
	    timer.schedule(new TimerTask() {
	         @Override
	         public void run() {
	              InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	              if(m != null){
	                m.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
	              } 
	         }
	    }, 200);      
	    
	    if(sp!=null && beepStreamId!=0 )
			sp.play(beepStreamId, 1, 1, 0, 0, 1);
	    
	}
	
	
	private void initEditTextListener() {
//		Log.d("TD", "initEdit");
		EditText wordSearch = (EditText)findViewById(R.id.input);
		
		wordSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void afterTextChanged(Editable arg0) {
				new WordLookUpTask(TestDictionary.this).execute(arg0.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
//				Log.d("TD","before:within "+ s + ", " + count + " characters beginning at " + start  + " are about to be replaced by " + count + " new text                         ");
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
//				Log.d("TD","on: within "+ s + ", " + count + " characters beginning at " + start + " have just replaced " + count + " old texts");
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_dictionary, menu);
		return true;
	}

	
	public void onWordFound(String result) {
		TextView resultTv = (TextView) findViewById(R.id.result);
		resultTv.setText(result + "\n" + resultTv.getText());
		if(sp!=null && beepStreamId!=0 )
			sp.play(beepStreamId, 1, 1, 0, 0, 1);
	}
	
	public void onClear(View view) {
		TextView resultTv = (TextView) findViewById(R.id.result);
		resultTv.setText("");
		EditText wordSearch = (EditText)findViewById(R.id.input);
		wordSearch.setText("");
		record.clear();
	}
	
	public void onReturnMenu(View view) {
		finish();
	}

}
