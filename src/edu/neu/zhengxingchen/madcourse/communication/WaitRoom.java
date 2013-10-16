package edu.neu.zhengxingchen.madcourse.communication;

import java.util.Date;

import edu.neu.zhengxingchen.madcourse.communication.EverythingResultReceiver.Receiver;

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

public class WaitRoom extends Activity implements Receiver{
	private TelephonyManager telMgr;
	RadioGroup mGuysList;
	TextView mStatus;
	Button connect_button;
	EverythingResultReceiver mResultReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("waitroom", "oncreate:" + getIntent().getBooleanExtra("continue", false));
		setContentView(R.layout.activity_wait_room);
		mStatus = (TextView) findViewById(R.id.status);
		mGuysList = (RadioGroup) findViewById(R.id.guys_radio);
		telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Global.SERIAL = telMgr.getDeviceId();
		/*
		 * The service will be considered required by the system only for as
		 * long as the calling context exists. For example, if this Context is
		 * an Activity that is stopped, the service will not be required to
		 * continue running until the Activity is resumed.
		 */
		Intent i = new Intent(this, WaitRoomService.class);
		startService(i);

		mGuysList
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup radioGroup,
							int radioButtonID) {
						connect_button = (Button) findViewById(R.id.connect_button);
						connect_button.setEnabled(true);
					}
				});
		
		mResultReceiver = new EverythingResultReceiver(new Handler());
		mResultReceiver.setReceiver(WaitRoom.this);
		
		
	}
	
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d("waitroom", "receiverresult" + resultData.getString("status"));
	}
	
	
	@Override
	protected void onResume() {
		Log.d("waitroom", "onresume");
		doBindService();
		EverythingBroadcastReceiver.scheduleAlarms(this, mResultReceiver);
		MoveReceiver.cancelAlarms(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d("waitroom", "onpause");
		MoveReceiver.scheduleAlarms(this, telMgr.getDeviceId() );
		EverythingBroadcastReceiver.cancelAlarms(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// doUnbindService();
	}

	Messenger mService = null;
	boolean mIsBound;

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WaitRoomService.MSG_GET_GUYS_WAITING:
				mGuysList.removeAllViews();
				Bundle b = msg.getData();
				String[] guys = b.getString("list").split(":");
				for (String guy : guys) {
					if (!guy.equals(telMgr.getDeviceId())) {
						RadioButton r = new RadioButton(WaitRoom.this);
						r.setText(guy);
						mGuysList.addView(r);
					}
				}
				break;
			case WaitRoomService.MSG_ENTER_ROOM:
				Toast.makeText(WaitRoom.this, R.string.enter_room,
						Toast.LENGTH_SHORT).show();
				mStatus.append("Enter Room" + "\n");
				startLookingForGuys();
				break;
			case WaitRoomService.MSG_NETWORK_ERROR:
				Toast.makeText(WaitRoom.this, R.string.network_error,
						Toast.LENGTH_LONG).show();
				break;
			case WaitRoomService.MSG_CONNECTED:
				Toast.makeText(WaitRoom.this, R.string.connected,
						Toast.LENGTH_LONG).show();
				break;
			case WaitRoomService.MSG_INVITATION_SENT:
//				connect_button.setEnabled(false);
				Toast.makeText(WaitRoom.this, R.string.invitation_sent,
						Toast.LENGTH_LONG).show();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {

			mStatus.append("Binded." + "\n");
			mService = new Messenger(service);
			registerThisPhone();
			Toast.makeText(WaitRoom.this, R.string.remote_service_connected,
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			Toast.makeText(WaitRoom.this, R.string.remote_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(WaitRoom.this, WaitRoomService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mStatus.append("Binding." + "\n");
		if (mIsBound) {
			mStatus.append("Binded." + "\n");
//			RadioButton r = findViewById(mGuysList.getCheckedRadioButtonId());
		}
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							WaitRoomService.MSG_UNREGISTER_CLIENT);
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			mStatus.append("Unbinding." + "\n");
		}
	}

	public void onClickPutValue(View v) {
		new PutValueTask(this).execute("score", "79");
	}

	public void onClickGetValue(View v) {
		new GetValueTask(this).execute("score");
	}

	public void onClickRefresh(View v) {
		registerThisPhone();
	}

	public void onClickConnect(View v) {

		// MoveReceiver.cancelAlarms(this);
		RadioButton r = (RadioButton) findViewById(mGuysList
				.getCheckedRadioButtonId());
		if (r != null)
			Log.d("waitroom", "click connect:" + r.getText());
			Message msg = Message.obtain(null,
				WaitRoomService.MSG_TO_CONNECT);
			Bundle b = new Bundle();
			b.putString("player", r.getText().toString());
			msg.setData(b);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void registerThisPhone() {
		Message msg = Message.obtain(null, WaitRoomService.MSG_REGISTER_CLIENT);
		msg.replyTo = mMessenger;
		Bundle b = new Bundle();
		b.putString("serial", telMgr.getDeviceId());
		msg.setData(b);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void afterPutValue(String result) {
		mStatus.append(result + "\n");
	}

	public void afterGetValue(String result) {
		mStatus.append(result + "\n");
	}

	public void startLookingForGuys() {
		Message msg;
		msg = Message.obtain(null, WaitRoomService.MSG_LOOK_FOR_GUYS_WAITING,
				0, 0);
		// Bundle b = new Bundle();
		// b.putString("serial", telMgr.getDeviceId());
		// msg.setData(b);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
