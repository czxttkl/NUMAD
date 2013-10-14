package edu.neu.zhengxingchen.madcourse.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	/** Keeps track of all current registered clients. */
	HashMap<String, Messenger> mClients = new HashMap<String, Messenger>();
	/** Holds last value set by a client. */
	int mValue = 0;

	/**
	 * Command to the service to register a client, receiving callbacks from the
	 * service. The Message's replyTo field must be a Messenger of the client
	 * where callbacks should be sent.
	 */
	static final int MSG_REGISTER_CLIENT = 1;

	/**
	 * Command to the service to unregister a client, ot stop receiving
	 * callbacks from the service. The Message's replyTo field must be a
	 * Messenger of the client as previously given with MSG_REGISTER_CLIENT.
	 */
	static final int MSG_UNREGISTER_CLIENT = 2;

	/**
	 * Command to service to set a new value. This can be sent to the service to
	 * supply a new value, and will be sent by the service to any registered
	 * clients with the new value.
	 */
	static final int MSG_SET_VALUE = 3;

	static final int MSG_LOOK_FOR_GUYS_WAITING = 4;

	static final int MSG_GET_GUYS_WAITING = 5;

	public WaitRoomService() {
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	public void onCreate() {
		Log.d("comm", "oncreate");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting.
		// showNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("comm", "onStartCommand");
		return START_STICKY; // this service is explicitly started and stopped
								// as needed
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
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

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		// CharSequence text = getText(R.string.remote_service_started);
		//
		// // Set the icon, scrolling text and timestamp
		// Notification notification = new Notification(R.drawable.logo, text,
		// System.currentTimeMillis());
		//
		// // The PendingIntent to launch our activity if the user selects this
		// notification
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		i.setClass(this, WaitRoom.class);
		//
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
		//
		// // Set the info for the views that show in the notification panel.
		// notification.setLatestEventInfo(this,
		// getText(R.string.remote_service_label),
		// text, contentIntent);
		//
		// // Send the notification.
		// // We use a string id because it is a unique number. We use it later
		// to cancel.
		// mNM.notify(R.string.remote_service_started, notification);
		//

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentIntent(contentIntent).setContentTitle("Dabble")
				.setContentText("New guy found").setSmallIcon(R.drawable.noti);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(R.string.new_guy_found, mBuilder.build());

	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				Bundle b = msg.getData();
				// b.get("serial");
				mClients.put(b.getString("serial"), msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_VALUE:
				mValue = msg.arg1;
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						mClients.get(i).send(
								Message.obtain(null, MSG_SET_VALUE, mValue, 0));
					} catch (RemoteException e) {
						// The client is dead. Remove it from the list;
						// we are going through the list from back to front
						// so this is safe to do inside the loop.
						mClients.remove(i);
					}
				}
				break;
			case MSG_LOOK_FOR_GUYS_WAITING:
				mClients.put("563333", null);
				String serial = msg.getData().getString("serial");
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, Messenger> entry : mClients.entrySet()) {
					if (!entry.getKey().equals(serial)) {
						sb.append(":");
						sb.append(entry.getKey());
					}
				}
				String result = null;
				if (sb.length() != 0)
					result = sb.toString().substring(1);
				Message resultMsg = Message.obtain(null, MSG_GET_GUYS_WAITING,
						0, 0);
				Bundle b1 = new Bundle();
				b1.putString("list", result);
				resultMsg.setData(b1);
				try {
					mClients.get(serial).send(resultMsg);
				} catch (RemoteException e) {
					mClients.remove(serial);
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
