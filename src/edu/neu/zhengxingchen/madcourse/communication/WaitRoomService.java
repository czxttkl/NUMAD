package edu.neu.zhengxingchen.madcourse.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class WaitRoomService extends Service {

	/** For showing and hiding our notification. */
	NotificationManager mNM;

	Messenger mClient;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	String list = null;

	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_VALUE = 3;
	static final int MSG_LOOK_FOR_GUYS_WAITING = 4;
	static final int MSG_GET_GUYS_WAITING = 5;
	static final int MSG_ENTER_ROOM = 6;
	static final int MSG_NETWORK_ERROR = 7;
	static final int MSG_CONNECTED = 8;
	static final int MSG_TO_CONNECT = 9;
	static final int MSG_INVITATION_SENT = 10;
	static final int MSG_INVITED_BY_OTHER = 11;

	public WaitRoomService() {

	}
	
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				Bundle b = msg.getData();
				mClient = msg.replyTo;
				updateGuysList(GetGuysTask.REGISTER);
				break;
			case MSG_UNREGISTER_CLIENT:
				updateGuysList(GetGuysTask.UNREGISTER);
				break;
			case MSG_LOOK_FOR_GUYS_WAITING:
				updateGuysList(GetGuysTask.LOOK_FOR_GUY);
				break;
			case MSG_TO_CONNECT:
				Bundle b1 = msg.getData();
				String playerSerial = b1.getString("player");
				new ConnectGuyTask(WaitRoomService.this).execute(playerSerial);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	@Override
	public void onCreate() {
		Log.d("waitroom", "oncreate");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting.
		// showNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("waitroom", "onstartcommand");
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		Log.d("waitroom", "waitroomservice ondestroy");
		mNM.cancel(R.string.remote_service_started);
		// Tell the user we stopped.
		Toast.makeText(this, R.string.remote_service_stopped,
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public void startRemoveGuyTask() {
		new RemoveGuyTask(WaitRoomService.this).execute();
	}

	public void afterRemoveGuyTask() {
		mClient = null;
	}

	public void startAddGuyTask() {
		new AddGuyTask(WaitRoomService.this).execute(list);
	}

	public void afterAddGuyTask(String result) {
		Message msg = Message
				.obtain(null, WaitRoomService.MSG_ENTER_ROOM, 0, 0);
		try {
			mClient.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void afterLookForGetGuys() {
		Message resultMsg = Message.obtain(null, MSG_GET_GUYS_WAITING, 0, 0);
		Bundle b1 = new Bundle();
		b1.putString("list", list);
		resultMsg.setData(b1);
		try {
			mClient.send(resultMsg);
		} catch (RemoteException e) {
			mClient = null;
		}
	}

	public void afterConnectGuyTask(String result) {
		Message resultMsg = Message.obtain(null, MSG_INVITATION_SENT, 0, 0);
		try {
			mClient.send(resultMsg);
		} catch (RemoteException e) {
			mClient = null;
		}
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		i.setClass(this, WaitRoom.class);
		//
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentIntent(contentIntent).setContentTitle("Dabble")
				.setContentText("New guy found").setSmallIcon(R.drawable.noti);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(R.string.new_guy_found, mBuilder.build());

	}

	public void updateGuysList(int code) {
		new GetGuysTask(WaitRoomService.this).execute(code);
	}

	public void returnError() {
		Message msg = Message.obtain(null, WaitRoomService.MSG_NETWORK_ERROR,
				0, 0);
		try {
			mClient.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
