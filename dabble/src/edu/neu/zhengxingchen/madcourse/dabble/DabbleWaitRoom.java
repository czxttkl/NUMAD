package edu.neu.zhengxingchen.madcourse.dabble;

import java.util.ArrayList;
import java.util.Arrays;

import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.AddGuyTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.PutValueTaskShuffleBoard;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.InviteGuyTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.InvitePopup;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.OfflineBroadcastReceiver;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.OnlineBroadcastReceiver;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.OnlineResultReceiver;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.GetGuysTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.PutValueTaskWaitRoom;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.OnlineResultReceiver.Receiver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class DabbleWaitRoom extends Activity implements Receiver {
	public String TAG = "dabblewaitroom";
	private TelephonyManager telMgr;
	OnlineResultReceiver mResultReceiver;
	RadioGroup mGuysList;
	Button connectButton;
	Button unconnectButton;
	String rival;
	public String list = "";
//	String formerMove = "";
	public static volatile boolean invitepopuped = false;
	public static volatile boolean networkError = false;
//	public volatile boolean connected = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_dabble_wait_room);
		mGuysList = (RadioGroup) findViewById(R.id.guys_radio);
		connectButton = (Button) findViewById(R.id.connect_button);
		unconnectButton = (Button) findViewById(R.id.unconnect_button);
		telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Global.SERIAL = telMgr.getDeviceId();
		/*
		 * The service will be considered required by the system only for as
		 * long as the calling context exists. For example, if this Context is
		 * an Activity that is stopped, the service will not be required to
		 * continue running until the Activity is resumed.
		 */
//		Intent i = new Intent(this, WaitRoomService.class);
//		startService(i);

//		mGuysList
//				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//					@Override
//					public void onCheckedChanged(RadioGroup radioGroup,
//							int radioButtonID) {
////						connect_button.setEnabled(true);
//					}
//				});

		mResultReceiver = new OnlineResultReceiver(new Handler());
		mResultReceiver.setReceiver(DabbleWaitRoom.this);

	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
//		Message m = new Message();
		Log.d(TAG, "waitroom : onlinereceiver:" + resultData.getString("status"));
		String[] tmp = resultData.getString("status").split(":");
		if (!tmp[0].startsWith("Error")) {
			String status = tmp[1];
			if (status.equals(Global.SERVER_STATUS_WAIT)
					|| status.equals(Global.SERVER_STATUS_INVITED)) {
				new GetGuysTask(this, GetGuysTask.LOOK_FOR_GUY).execute();
				connectButton.setText("Connect");
				connectButton.setEnabled(true);
				unconnectButton.setEnabled(true);
//				unshakeButton.setEnabled(false);
//				moveButton.setEnabled(false);
			}

			if (status.equals(Global.SERVER_STATUS_INVITED)) {
				rival = tmp[2];
				if(!invitepopuped)
					startInvitePopup();
			}

			if (status.equals(Global.SERVER_STATUS_INGAME)) {
				rival = tmp[2];
				
				if (connectButton != null) {
					connectButton.setText("Connected");
					connectButton.setEnabled(false);
				}
				
				//Set by the person you invite when he accepts your invitation
				if(tmp.length == 3) {
					Global.RIVAL = rival;
					new PutValueTaskWaitRoom(this, PutValueTaskWaitRoom.ENTER_SHUFFLE_BOARD)
					.execute(rival);				
					Toast.makeText(DabbleWaitRoom.this, "The rival has accepted your invite", Toast.LENGTH_LONG)
					.show();
					Intent i = new Intent();
					i.setClass(this, ShuffleBoard.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(i);
				}
				
				else if(tmp.length > 3) {
					String substatus = tmp[3];
					
//					if(substatus.equals(Global.SERVER_SUBSTATUS_SHUFFLE)) {
						//if it is shuffle, it means the game status is wrong. Reset then.
//					}
					
					//status with in_game means you should register as a new one to enter in the waitroom
					new GetGuysTask(this, GetGuysTask.REGISTER).execute();
				}
				
			}
		} else {
			String subresult = tmp[1].trim();
			
			if (subresult.startsWith("IOException")) {
				returnError();
			}
			
			//If network error, try to register
			if ( networkError || subresult.startsWith("No")) {
				new GetGuysTask(this, GetGuysTask.REGISTER).execute();
				networkError = false;
			}
			
			if (subresult.startsWith("Network")) {
				returnError();
			}
		}
	}

	/*
	 * Receive results from InvitePopup activity
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && data != null) {
			if (data.getBooleanExtra(Global.SERVER_KEY_INVITATATION_ACCEPTED,
					false) ) {
				Global.RIVAL = rival;
				new PutValueTaskWaitRoom(this, PutValueTaskWaitRoom.SET_CONNECTED)
						.execute(rival);
				Toast.makeText(DabbleWaitRoom.this, "Trying to establish connect", Toast.LENGTH_SHORT)
				.show();
				// connect_button.setText("connected");
				// connect_button.setEnabled(false);
			}

			if (!data.getBooleanExtra(Global.SERVER_KEY_INVITATATION_ACCEPTED,
					false)) {
				new PutValueTaskWaitRoom(this, PutValueTaskWaitRoom.SET_REWAIT).execute();
			}
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onresume");
		// doBindService();
		OnlineBroadcastReceiver.scheduleAlarms(this, mResultReceiver, Global.SERIAL);
		
		super.onResume();
		if( Music.musicPaused) {
			Music.start(this);
			Music.musicPaused = false;
		}
		
		Music.musicShouldPause = true;
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onpause");
		
		OnlineBroadcastReceiver.cancelAlarms(this);
		super.onPause();
		if(Music.musicShouldPause) {
			Music.pause(this);
			Music.musicPaused = true;
		}
	}

//	public void onClickPutValue(View v) {
//		new PutValueTask(this, PutValueTask.PUT_VALUE).execute("score", "79");
//	}
//
//	public void onClickGetValue(View v) {
//		new GetValueTask(this).execute("score");
//	}

	public void onClickConnect(View v) {
		// MoveReceiver.cancelAlarms(this);
		RadioButton r = (RadioButton) findViewById(mGuysList
				.getCheckedRadioButtonId());
		if (r != null) {
			Log.d(TAG, "click connect:" + r.getText());
			new InviteGuyTask(this).execute(r.getText().toString());
		}
	}
	
	public void onClickUnconnect(View v) {
		new PutValueTaskWaitRoom(this, PutValueTaskWaitRoom.SET_UNCONNECTED).execute();
	}
	
//	public void onClickUnshake(View v) {
//		new PutValueTask(this, PutValueTask.SET_UNSHAKE).execute(rival);
//	}
	
//	public void onClickMove(View v) {
//		new PutValueTask(this, PutValueTask.SET_MOVE).execute(rival);
//	}

	public void startInvitePopup() {
		invitepopuped = true;
		Intent i = new Intent();
		i.setClass(this, InvitePopup.class);
		Music.musicShouldPause = false;
		startActivityForResult(i, 1);
	}

	public void afterGetGuysList(String result) {
		if(list.equals(result)) {
		
		} else {
			ArrayList<String> newGuys = new ArrayList<String>(Arrays.asList(result.split(":")));
			ArrayList<String> oldGuys = new ArrayList<String>(Arrays.asList(list.split(":")));
			
			int l = mGuysList.getChildCount();
			if(l == 0) {
				for(String guy : newGuys) {
					if (!guy.equals(Global.SERIAL) && !guy.equals("")) {
						RadioButton r = new RadioButton(DabbleWaitRoom.this);
						r.setText(guy);
						mGuysList.addView(r);
					}
				}
			} else {
				RadioButton r = null ;
				int i;
				for( i = 0; i< l; i++) {
					r = (RadioButton)mGuysList.getChildAt(i);
					if( r!= null) {
						String removeItem = r.getText().toString();
						if( !newGuys.contains(removeItem) ) {
							mGuysList.removeView(r);
							l = l-1;
							i = i-1;
							oldGuys.remove(removeItem);
						}
					}
				}
				
				for(String guy : newGuys) {
					if(!oldGuys.contains(guy) && !guy.equals(Global.SERIAL) && !guy.equals("")) {
						RadioButton r1 = new RadioButton(DabbleWaitRoom.this);
						r1.setText(guy);
						mGuysList.addView(r1);
					}
				}
			}
			list = result;
		}
		
	}

	public void afterGetGuysListRegister() {
		mGuysList.removeAllViews();
		String[] guys = list.split(":");
		for (String guy : guys) {
			if (!guy.equals(Global.SERIAL) && !guy.equals("")) {
				RadioButton r = new RadioButton(DabbleWaitRoom.this);
				r.setText(guy);
				mGuysList.addView(r);
			}
		}
		Log.d(TAG, "afterGetGuysListRegister");
		new AddGuyTask(this).execute();
	}

	public void afterAddGuyTask() {
		Toast.makeText(DabbleWaitRoom.this, "Enter Room", Toast.LENGTH_SHORT)
				.show();
		connectButton.setEnabled(true);
		unconnectButton.setEnabled(true);
//		mStatus.append("Enter Room" + "\n");
	}

	public void afterInviteGuyTask() {
		Toast.makeText(DabbleWaitRoom.this, "Invitation sent",
				Toast.LENGTH_LONG).show();
	}
	
	public void afterUnshakeTask() {
		connectButton.setText("Connect");
		connectButton.setEnabled(true);
//		unshakeButton.setEnabled(false);
//		moveButton.setEnabled(false);
		invitepopuped = false;
		Toast.makeText(DabbleWaitRoom.this, "You have unshaked with your rival", Toast.LENGTH_LONG)
		.show();
	}
	
	public void afterSetConnected() {
		Toast.makeText(DabbleWaitRoom.this, "You have connected with your rival", Toast.LENGTH_LONG)
		.show();

		if (connectButton != null) {
			connectButton.setText("Connected");
			connectButton.setEnabled(false);
		}
		
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.setClass(this, ShuffleBoard.class);
		startActivity(i);
		finish();
		invitepopuped = false;

	}
	
	public void afterEnterShuffleBoard() {
		finish();
	}
	
	
	public void afterSetUnconnected() {
		Toast.makeText(DabbleWaitRoom.this, "You have quitted the room", Toast.LENGTH_LONG)
		.show();
		finish();
	}
	
	public void afterSetRewait() {
		Toast.makeText(DabbleWaitRoom.this, "You have denied connecting with your rival", Toast.LENGTH_LONG)
		.show();
		invitepopuped = false;
	}
	
	public void returnError() {
		networkError = true;
		Toast.makeText(DabbleWaitRoom.this, "Network error. We will try reconnecting automatically.", Toast.LENGTH_LONG)
				.show();
	}

}

