package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.ardverk.collection.Trie;

import edu.neu.madcourse.R;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class TestDictionary extends Activity {

	protected Trie<String, String> trie = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_dictionary);
		
		try {
			new LoadDicTask(this).execute(getResources().getAssets().open("wordlist.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		initEditTextListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		EditText input = (EditText)findViewById(R.id.input);
		Log.d("TD", input.isFocusableInTouchMode() + "");
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
	}
	
	private void initEditTextListener() {
		EditText wordSearch = (EditText)findViewById(R.id.input);
		
		wordSearch.addTextChangedListener(new TextWatcher() {

			String currentInput = "";
			
			@Override
			public void afterTextChanged(Editable arg0) {
				Log.d("TD","after:"+ arg0.toString());
				if( trie != null) {
					if(!currentInput.equals(arg0.toString())) {
						currentInput = arg0.toString();
						if(currentInput.equals(trie.selectValue(currentInput))){
							TextView result = (TextView)findViewById(R.id.result);
							result.setText(currentInput);
						}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				Log.d("TD","before:within "+ s + ", " + count + " characters beginning at " + start  + " are about to be replaced by " + count + " new text                         ");
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.d("TD","on: within "+ s + ", " + count + " characters beginning at " + start + " have just replaced " + count + " old texts");
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_dictionary, menu);
		return true;
	}

}
