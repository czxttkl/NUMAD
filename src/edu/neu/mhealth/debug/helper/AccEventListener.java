/**
 * 
 * @author "Shang Ma"
 *
 */
package edu.neu.mhealth.debug.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.Toast;

public class AccEventListener implements SensorEventListener {
	
	private static final String TAG = "AccEventListener";
    private static final float ALPHA = 0.8f;
    
    private static final int status_invalid = -1;
    private static final int status_initial = 0;
    private static final int status_first = 1;
    private static final int status_second = 2;
    private static final int status_third = 3;
    private static final int status_forth = 4;    
    private static final int status_fifth = 5;
    
    private Context mContext;
    private int status;
    private float[] gravity;

    public AccEventListener(Context context) {
        gravity = new float[3];
        status = status_invalid;
        mContext = context;
    }
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float[] values = event.values.clone();
		values = highPass(values[0], values[1], values[2]);
		
		Log.e(TAG, "z axis value: " + values[2]);
		if (status == status_invalid) {
			if (values[2] < 1 && values[2] > -1) {
				status = status_initial;
			}
		}
		if (status == status_initial) {
			if (values[2] < -2 && values[2] > -4) {
				status = status_first;
			}
		}
		
		if (status == status_first) {
			if (values[2] < 6 && values[2] > 4) {
				status = status_second;
			}
		}

		if (status == status_second) {
			if (values[2] < -7 && values[2] > -10) {
				status = status_third;
			}
		}

		if (status == status_third) {
			if (values[2] < 6 && values[2] > 4) {
				status = status_forth;
			}
		}

		if (status == status_forth) {
			if (values[2] < 1 && values[2] > -1) {
				status = status_fifth;
			}
		}

		if (status == status_fifth) {
			
		}
		
		Log.i(TAG, "current status:---------- " + status);
	}
	
    /**
     * This method derived from the Android documentation and is available under
     * the Apache 2.0 license.
     * 
     * @see http://developer.android.com/reference/android/hardware/SensorEvent.html
     */
    private float[] highPass(float x, float y, float z)
    {
        float[] filteredValues = new float[3];
        
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];
        
        return filteredValues;
    }
}
