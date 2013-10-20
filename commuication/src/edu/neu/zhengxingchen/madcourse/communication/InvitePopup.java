 package edu.neu.zhengxingchen.madcourse.communication;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class InvitePopup extends Activity {
	//private MyDialog dialog;
	private LinearLayout layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_popup);
		//dialog=new MyDialog(this);
//		layout=(LinearLayout)findViewById(R.id.exit_layout);
//		layout.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Toast.makeText(getApplicationContext(), "提示：点击窗口外部关闭窗口！", 
//						Toast.LENGTH_SHORT).show();	
//			}
//		});
	}

	
	public void yesButton(View v) {  
		 Intent returnIntent = new Intent();
		 returnIntent.putExtra(Global.SERVER_KEY_INVITATATION_ACCEPTED, true);
		// Log.d("waitroom","invitepopup yes");
		 setResult(RESULT_OK,returnIntent);   
    	this.finish();    	
      }  
	public void noButton(View v) {
		Intent returnIntent = new Intent();
		 returnIntent.putExtra(Global.SERVER_KEY_INVITATATION_ACCEPTED, false);
		setResult(RESULT_OK, returnIntent);        
    	this.finish();
      }  
	
}
