package edu.neu.zhengxingchen.madcourse.communication;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class WaitRoom extends Activity {
	private TelephonyManager telMgr;
	RadioGroup mGuysList;
	TextView mStatus;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wait_room);
		mStatus = (TextView) findViewById(R.id.status);
		mGuysList = (RadioGroup) findViewById(R.id.guys_radio);
//		mListView = (ListView) findViewById(R.id.guys);
		telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
	}

	@Override
	protected void onResume() {
		doBindService();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	/** Some text view we are using to show state information. */
	
	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case WaitRoomService.MSG_GET_GUYS_WAITING:
	            	Bundle b = msg.getData();
	            	String[] guys = b.getString("list").split(":");
	            	for(String guy : guys) {
	            		RadioButton r = new RadioButton(WaitRoom.this);
	            		r.setText(guy);
	            		mGuysList.addView(r);
	            	}
	               // mCallbackText.setText("Received from service: " + msg.arg1);
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
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
	        mService = new Messenger(service);
//	        mCallbackText.setText("Establish WaitRoom Service");

	        // We want to monitor the service for as long as we are
	        // connected to it.
	        try {
	            Message msg = Message.obtain(null,
	                    WaitRoomService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            Bundle b = new Bundle();
	            b.putString("serial", telMgr.getDeviceId());
	           // b.putString("name", "");
	           // msg.arg1 = Integer.valueOf(telMgr.getDeviceId().substring(5, 8));
	            msg.setData(b);
	            mService.send(msg);

	            // Give it some value as an example.
	            msg = Message.obtain(null,
	            		WaitRoomService.MSG_LOOK_FOR_GUYS_WAITING, this.hashCode(), 0);
	            msg.setData(b);
	            mService.send(msg);
	        } catch (RemoteException e) {
	            // In this case the service has crashed before we could even
	            // do anything with it; we can count on soon being
	            // disconnected (and then reconnected if it can be restarted)
	            // so there is no need to do anything here.
	        }

	        // As part of the sample, tell the user what happened.
	        Toast.makeText(WaitRoom.this, R.string.remote_service_connected,
	                Toast.LENGTH_SHORT).show();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        mService = null;
	     //   mCallbackText.setText("Disconnected.");

	        // As part of the sample, tell the user what happened.
	        Toast.makeText(WaitRoom.this, R.string.remote_service_disconnected,
	                Toast.LENGTH_SHORT).show();
	    }
	};

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because there is no reason to be able to let other
	    // applications replace our component.
	    bindService(new Intent(WaitRoom.this, 
	            WaitRoomService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	    mStatus.append("Binding." + "\n");
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // If we have received the service, and hence registered with
	        // it, then now is the time to unregister.
	        if (mService != null) {
	            try {
	                Message msg = Message.obtain(null,
	                        WaitRoomService.MSG_UNREGISTER_CLIENT);
	                msg.replyTo = mMessenger;
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
	
	public void afterPutValue(String result) {
		mStatus.append(result + "\n");
	}
	
	public void afterGetValue(String result) {
		mStatus.append(result + "\n");
	}
	
	
}
