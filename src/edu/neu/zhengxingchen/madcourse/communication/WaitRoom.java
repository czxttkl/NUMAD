package edu.neu.zhengxingchen.madcourse.communication;

import java.util.Date;

import edu.neu.zhengxingchen.madcourse.communication.OnlineResultReceiver.Receiver;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class WaitRoom extends Activity implements Receiver {
	private TelephonyManager telMgr;
	RadioGroup mGuysList;
	TextView mStatus;
	Button connectButton;
	Button unshakeButton;
	Button moveButton;
	OnlineResultReceiver mResultReceiver;
	String rival;
	String list = "";
	String formerMove = "";
	public volatile boolean invitepopuped = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("waitroom",
				"oncreate:" + getIntent().getBooleanExtra("continue", false));
		setContentView(R.layout.activity_wait_room);
		mStatus = (TextView) findViewById(R.id.status);
		mGuysList = (RadioGroup) findViewById(R.id.guys_radio);
		connectButton = (Button) findViewById(R.id.connect_button);
		unshakeButton = (Button) findViewById(R.id.unshake_button);
		moveButton = (Button) findViewById(R.id.move_button);
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
		mResultReceiver.setReceiver(WaitRoom.this);

	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Message m = new Message();
		Log.d("waitroom", "receiverresult" + resultData.getString("status"));
		String[] tmp = resultData.getString("status").split(":");
		if (!tmp[0].startsWith("Error")) {
			String status = tmp[1];
			if (status.equals(Global.SERVER_STATUS_WAIT)
					|| status.equals(Global.SERVER_STATUS_INVITED)) {
				new GetGuysTask(this, GetGuysTask.LOOK_FOR_GUY).execute();
				connectButton.setText("Connect");
				connectButton.setEnabled(true);
				unshakeButton.setEnabled(false);
				moveButton.setEnabled(false);
			}

			if (status.equals(Global.SERVER_STATUS_INVITED)) {
				rival = tmp[2];
				if(!invitepopuped)
					startInvitePopup();
			}

			if (status.equals(Global.SERVER_STATUS_INGAME)) {
				rival = tmp[2];
				new PutValueTask(this, PutValueTask.SET_UPDATE_CONNECTED)
						.execute(rival);
				
				if(tmp.length == 4) {
					String move = tmp[3];
//					if(!formerMove.equals(move)) {
//						formerMove = move;
						Toast.makeText(WaitRoom.this, "Your rival has a move", Toast.LENGTH_LONG)
					.show();
//					}
				}
				
				
				if (connectButton != null) {
					connectButton.setText("Connected");
					connectButton.setEnabled(false);
					unshakeButton.setEnabled(true);
					moveButton.setEnabled(true);
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
		// Log.d("waitroom", "onactivityresult:" +
		// data.getBooleanExtra("accept", false));
		if (requestCode == 1 && data != null) {
			if (data.getBooleanExtra(Global.SERVER_KEY_INVITATATION_ACCEPTED,
					false) ) {
				new PutValueTask(this, PutValueTask.SET_CONNECTED)
						.execute(rival);
				Toast.makeText(WaitRoom.this, "Trying to establish connect", Toast.LENGTH_LONG)
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
		Log.d("waitroom", "onresume");
		// doBindService();
		OnlineBroadcastReceiver.scheduleAlarms(this, mResultReceiver);
		OfflineBroadcastReceiver.cancelAlarms(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d("waitroom", "onpause");
		OfflineBroadcastReceiver.scheduleAlarms(this);
		OnlineBroadcastReceiver.cancelAlarms(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// doUnbindService();
	}

	@Override
	protected void onStop() {
		Log.d("waitroom", "onstop");
		super.onStop();
	}

	// Messenger mService = null;
	// boolean mIsBound;

	// class IncomingHandler extends Handler {
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case WaitRoomService.MSG_GET_GUYS_WAITING:
	// Bundle b = msg.getData();
	// String[] guys = b.getString("list").split(":");
	//
	// break;
	// case WaitRoomService.MSG_ENTER_ROOM:
	//
	// // startLookingForGuys();
	// break;
	// case WaitRoomService.MSG_NETWORK_ERROR:
	//
	// break;
	// case WaitRoomService.MSG_CONNECTED:
	// Toast.makeText(WaitRoom.this, R.string.connected,
	// Toast.LENGTH_LONG).show();
	// break;
	// case WaitRoomService.MSG_INVITATION_SENT:
	// // connect_button.setEnabled(false);
	// Toast.makeText(WaitRoom.this, R.string.invitation_sent,
	// Toast.LENGTH_LONG).show();
	// break;
	// case WaitRoomService.MSG_INVITED_BY_OTHER:
	//
	// break;
	// default:
	// super.handleMessage(msg);
	// }
	// }
	// }

	// /**
	// * Target we publish for clients to send messages to IncomingHandler.
	// */
	// final Messenger mMessenger = new Messenger(new IncomingHandler());
	//
	// /**
	// * Class for interacting with the main interface of the service.
	// */
	// private ServiceConnection mConnection = new ServiceConnection() {
	// public void onServiceConnected(ComponentName className, IBinder service)
	// {
	//
	// mStatus.append("Binded." + "\n");
	// mService = new Messenger(service);
	// //registerThisPhone();
	// Toast.makeText(WaitRoom.this, R.string.remote_service_connected,
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// public void onServiceDisconnected(ComponentName className) {
	// mService = null;
	// Toast.makeText(WaitRoom.this, R.string.remote_service_disconnected,
	// Toast.LENGTH_SHORT).show();
	// }
	// };
	//
	// void doBindService() {
	// // Establish a connection with the service. We use an explicit
	// // class name because there is no reason to be able to let other
	// // applications replace our component.
	// bindService(new Intent(WaitRoom.this, WaitRoomService.class),
	// mConnection, Context.BIND_AUTO_CREATE);
	// mStatus.append("Binding." + "\n");
	// if (mIsBound) {
	// mStatus.append("Binded." + "\n");
	// // RadioButton r =
	// // findViewById(mGuysList.getCheckedRadioButtonId());
	// }
	// mIsBound = true;
	// }
	//
	// void doUnbindService() {
	// if (mIsBound) {
	// // If we have received the service, and hence registered with
	// // it, then now is the time to unregister.
	// if (mService != null) {
	// try {
	// Message msg = Message.obtain(null,
	// WaitRoomService.MSG_UNREGISTER_CLIENT);
	// mService.send(msg);
	// } catch (RemoteException e) {
	// // There is nothing special we need to do if the service
	// // has crashed.
	// }
	// }
	//
	// // Detach our existing connection.
	// unbindService(mConnection);
	// mIsBound = false;
	// mStatus.append("Unbinding." + "\n");
	// }
	// }

	public void onClickPutValue(View v) {
		new PutValueTask(this, PutValueTask.PUT_VALUE).execute("score", "79");
	}

	public void onClickGetValue(View v) {
		new GetValueTask(this).execute("score");
	}

	public void onClickRefresh(View v) {
		new AddGuyTask(this).execute();
	}

	public void onClickConnect(View v) {
		// MoveReceiver.cancelAlarms(this);
		RadioButton r = (RadioButton) findViewById(mGuysList
				.getCheckedRadioButtonId());
		if (r != null) {
			Log.d("waitroom", "click connect:" + r.getText());
			new InviteGuyTask(this).execute(r.getText().toString());
		}
	}
	
	public void onClickUnshake(View v) {
		new PutValueTask(this, PutValueTask.SET_UNSHAKE).execute(rival);
	}
	
	public void onClickMove(View v) {
		new PutValueTask(this, PutValueTask.SET_MOVE).execute(rival);
	}

	// public void registerThisPhone() {
	// Message msg = Message.obtain(null, WaitRoomService.MSG_REGISTER_CLIENT);
	// msg.replyTo = mMessenger;
	// try {
	// mService.send(msg);
	// } catch (RemoteException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public void startInvitePopup() {
		invitepopuped = true;
		Intent i = new Intent();
		i.setClass(this, InvitePopup.class);
		startActivityForResult(i, 1);
	}

	public void afterPutValue(String result) {
		mStatus.append(result + "\n");
	}

	public void afterGetValue(String result) {
		mStatus.append(result + "\n");
	}

	public void afterGetGuysList() {
		mGuysList.removeAllViews();
		String[] guys = list.split(":");
		for (String guy : guys) {
			if (!guy.equals(Global.SERIAL)) {
				RadioButton r = new RadioButton(WaitRoom.this);
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
				RadioButton r = new RadioButton(WaitRoom.this);
				r.setText(guy);
				mGuysList.addView(r);
			}
		}
		new AddGuyTask(this).execute();
	}

	public void afterAddGuyTask() {
		Toast.makeText(WaitRoom.this, R.string.enter_room, Toast.LENGTH_SHORT)
				.show();
		mStatus.append("Enter Room" + "\n");
	}

	public void afterInviteGuyTask() {
		Toast.makeText(WaitRoom.this, R.string.invitation_sent,
				Toast.LENGTH_LONG).show();
	}
	
	public void afterUnshakeTask() {
		connectButton.setText("Connect");
		connectButton.setEnabled(true);
		unshakeButton.setEnabled(false);
		moveButton.setEnabled(false);
		invitepopuped = false;
		Toast.makeText(WaitRoom.this, "You have unshaked with your rival", Toast.LENGTH_LONG)
		.show();
	}
	
	public void afterSetConnected() {
		Toast.makeText(WaitRoom.this, "You have connected with your rival", Toast.LENGTH_LONG)
		.show();
	}
	
	public void afterSetRewait() {
		Toast.makeText(WaitRoom.this, "You have denied connecting with your rival", Toast.LENGTH_LONG)
		.show();
		invitepopuped = false;
	}
	
	public void returnError() {
		Toast.makeText(WaitRoom.this, R.string.network_error, Toast.LENGTH_LONG)
				.show();
	}
	// public void startLookingForGuys() {
	// Message msg;
	// msg = Message.obtain(null, WaitRoomService.MSG_LOOK_FOR_GUYS_WAITING,
	// 0, 0);
	// // Bundle b = new Bundle();
	// // b.putString("serial", telMgr.getDeviceId());
	// // msg.setData(b);
	// try {
	// mService.send(msg);
	// } catch (RemoteException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	

}
