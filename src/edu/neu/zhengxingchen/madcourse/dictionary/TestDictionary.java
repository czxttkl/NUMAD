package edu.neu.zhengxingchen.madcourse.dictionary;

import edu.neu.madcourse.R;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;

public class TestDictionary extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_dictionary);
		
		new LoadDicTask().execute("wordlist.txt");
		
		initEditTextListener();
	}

	private void initEditTextListener() {
		EditText wordSearch = (EditText)findViewById(R.id.input);
		
		wordSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				Log.d("TD","after:"+ arg0.toString());
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
