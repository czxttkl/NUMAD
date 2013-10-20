package edu.neu.zhengxingchen.madcourse.dabble;

import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.AddGuyTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.GetValueTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.InviteGuyTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.InvitePopup;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.OfflineBroadcastReceiver;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.OnlineBroadcastReceiver;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.OnlineResultReceiver;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.GetGuysTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.PutValueTask;
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
//	Button unshakeButton;
//	Button moveButton;
	String rival;
	public String list = "";
	String formerMove = "";
	public static volatile boolean invitepopuped = false;
	public volatile boolean connected = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dabble_wait_room);
		mGuysList = (RadioGroup) findViewById(R.id.guys_radio);
		connectButton = (Button) findViewById(R.id.connect_button);
//		unshakeButton = (Button) findViewById(R.id.unshake_button);
//		moveButton = (Button) findViewById(R.id.move_button);
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

		mGuysList
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup radioGroup,
							int radioButtonID) {
//						connect_button.setEnabled(true);
					}
				});

		mResultReceiver = new OnlineResultReceiver(new Handler());
		mResultReceiver.setReceiver(DabbleWaitRoom.this);

	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Message m = new Message();
		Log.d(TAG, "receiverresult" + resultData.getString("status"));
		String[] tmp = resultData.getString("status").split(":");
		if (!tmp[0].startsWith("Error")) {
			String status = tmp[1];
			if (status.equals(Global.SERVER_STATUS_WAIT)
					|| status.equals(Global.SERVER_STATUS_INVITED)) {
				new GetGuysTask(this, GetGuysTask.LOOK_FOR_GUY).execute();
				connectButton.setText("Connect");
				connectButton.setEnabled(true);
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
				if(!connected)
					new PutValueTask(this, PutValueTask.SET_CONNECTED)
						.execute(rival);
				
				if (connectButton != null) {
					connectButton.setText("Connected");
					connectButton.setEnabled(false);
//					unshakeButton.setEnabled(true);
//					moveButton.setEnabled(true);
				}
				
				if(tmp.length > 3) {
					String substatus = tmp[3];
//					if(!formerMove.equals(move)) {
//						formerMove = move;
					if(substatus.equals(Global.SERVER_SUBSTATUS_MOVE)) {
							Toast.makeText(DabbleWaitRoom.this, "Your rival has a move", Toast.LENGTH_LONG)
							.show();
							new PutValueTask(this, PutValueTask.REMOVE_MOVE)
							.execute(rival);
					}
					
					if(substatus.equals(Global.SERVER_SUBSTATUS_INVITEACCEPT)) {
						Toast.makeText(DabbleWaitRoom.this, "The rival has accepted your invite", Toast.LENGTH_LONG)
						.show();
					}
				}
				
			}
		} else {
			String subresult = tmp[1].trim();
			
			if (subresult.startsWith("IOException")) {
				returnError();
			}
			
			if (subresult.startsWith("No")) {
				new GetGuysTask(this, GetGuysTask.REGISTER).execute();
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
		// Log.d(TAG, "onactivityresult:" +
		// data.getBooleanExtra("accept", false));
		if (requestCode == 1 && data != null) {
			if (data.getBooleanExtra(Global.SERVER_KEY_INVITATATION_ACCEPTED,
					false) ) {
				new PutValueTask(this, PutValueTask.SET_CONNECTED)
						.execute(rival);
				Toast.makeText(DabbleWaitRoom.this, "Trying to establish connect", Toast.LENGTH_LONG)
				.show();
				// connect_button.setText("connected");
				// connect_button.setEnabled(false);
			}

			if (!data.getBooleanExtra(Global.SERVER_KEY_INVITATATION_ACCEPTED,
					false)) {
				new PutValueTask(this, PutValueTask.SET_REWAIT).execute();
			}
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onresume");
		// doBindService();
		OnlineBroadcastReceiver.scheduleAlarms(this, mResultReceiver);
		OfflineBroadcastReceiver.cancelAlarms(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onpause");
		OfflineBroadcastReceiver.scheduleAlarms(this);
		OnlineBroadcastReceiver.cancelAlarms(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onstop");
		super.onStop();
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
		startActivityForResult(i, 1);
	}

	public void afterGetGuysList() {
		mGuysList.removeAllViews();
		String[] guys = list.split(":");
		for (String guy : guys) {
			if (!guy.equals(Global.SERIAL) && !guy.equals("")) {
				RadioButton r = new RadioButton(DabbleWaitRoom.this);
				r.setText(guy);
				mGuysList.addView(r);
			}
		}
	}

	public void afterGetGuysListRegister() {
		mGuysList.removeAllViews();
		String[] guys = list.split(":");
		for (String guy : guys) {
			if (!guy.equals(Global.SERIAL)) {
				RadioButton r = new RadioButton(DabbleWaitRoom.this);
				r.setText(guy);
				mGuysList.addView(r);
			}
		}
		new AddGuyTask(this).execute();
	}

	public void afterAddGuyTask() {
		Toast.makeText(DabbleWaitRoom.this, "Enter Room", Toast.LENGTH_SHORT)
				.show();
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
		connected = true;
		if (connectButton != null) {
			connectButton.setText("Connected");
			connectButton.setEnabled(false);
//			unshakeButton.setEnabled(true);
//			moveButton.setEnabled(true);
		}
		Log.d(TAG, "aftersetconnected");
	}
	
	public void afterSetRewait() {
		Toast.makeText(DabbleWaitRoom.this, "You have denied connecting with your rival", Toast.LENGTH_LONG)
		.show();
		invitepopuped = false;
	}
	
	public void returnError() {
		Toast.makeText(DabbleWaitRoom.this, "Network error", Toast.LENGTH_LONG)
				.show();
	}

}

