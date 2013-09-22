package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.ardverk.collection.Trie;

import edu.neu.madcourse.R;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class TestDictionary extends Activity{

	protected Trie<String, String> trie = null;
	protected SoundPool sp = null;
	protected int beepStreamId = 0;
	
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
		initTextViewList();
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
		if(input.isFocusableInTouchMode())
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
	
	private void initTextViewList() {
		
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

}
