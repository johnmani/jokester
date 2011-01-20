/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kairali.android.jokester;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.util.Log;
import android.hardware.*;
import java.util.*;
import java.io.*;


/**
 * The main JokeView activity
 */
public class JokeViewActivity extends Activity implements SensorEventListener {
    private TextView viewer;
	private List<String> jokes;
	private int index = 0;
	private Random random;
	private SensorManager sManager;
	private Sensor accelerometer;
	private float lastX = -1.0f, lastY = -1.0f, lastZ = -1.0f;
	private long lastTime;
	private int shakeCount = 0;
	private long lastShake;
	private long lastForce;

    private static final String TAG = "Jokester";
	private static final int FORCE_THRESHOLD = 350;
	private static final int TIME_THRESHOLD = 100;
	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;
    
    public JokeViewActivity() { }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.layout);

		Context context = getApplicationContext();
		// Read jokes file
		BufferedReader bir = new BufferedReader(new InputStreamReader(
								context.getResources().openRawResource(R.raw.jokes)));
		try {
		    jokes = parseJokes(bir);
		} catch (Exception ex) {
			jokes = new ArrayList<String>();
			jokes.add(getText(R.string.defaultjoke).toString());
		    Toast t = Toast.makeText(context, "Could not open jokes file", Toast.LENGTH_SHORT);
			t.show();
		} finally {
			try { bir.close(); } catch (Exception ignore) {}
		}

		Log.v(TAG, "Jokes database size = " + jokes.size());

		random = new Random();

        viewer = (TextView) findViewById(R.id.viewer);

        // Hook up button presses to the appropriate event handler.
        ((ImageButton) findViewById(R.id.next)).setOnClickListener(new OnClickListener() {
   					public void onClick(View v) {
					   viewer.setText(jokes.get(next()));
					}
				});

		/*
        ((Button) findViewById(R.id.shuffle)).setOnClickListener(new OnClickListener() {
   					public void onClick(View v) {
					   viewer.setText(jokes.get(shuffle()));
					}
   				});
		*/

        ((ImageButton) findViewById(R.id.shuffle)).setOnClickListener(new OnClickListener() {
   					public void onClick(View v) {
					   viewer.setText(jokes.get(shuffle()));
					}
   				});
        
        viewer.setText(getText(R.string.welcome));

		sManager = (SensorManager)getSystemService(SENSOR_SERVICE);			
		accelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

	private List parseJokes(BufferedReader br) throws Exception {
		ArrayList<String> jokes = new ArrayList<String>();

		String line;
		String joke = null;
		while ((line = br.readLine()) != null) {
        	if (line.startsWith("//")) // comment, skip
				continue;
			if (line.startsWith("-----")) { // EOJ, add joke to list
				jokes.add(joke);
				joke = null;
				continue;
			}

			if (joke == null)
				joke = line;
			else
				joke = joke + "\n" + line;
		}

		return jokes;
	}
	
	private synchronized int next() {
		if (index < jokes.size()-1)
			index++;
		else
			index = 0;

		Log.v(TAG, "next index = " + index);
	    return index;
	}

	private synchronized int shuffle() {
		index = random.nextInt(jokes.size());

		Log.v(TAG, "random index = " + index);
	    return index;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// stub
	}

	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		if (sensor.getType() != SensorManager.SENSOR_ACCELEROMETER) 
			return;

		long now = System.currentTimeMillis();
	
	    if ((now - lastForce) > SHAKE_TIMEOUT) {
			shakeCount = 0;
		}
					  
		if ((now - lastTime) > TIME_THRESHOLD) {
			long diff = now - lastTime;
			float x = event.values[SensorManager.DATA_X];
			float y = event.values[SensorManager.DATA_Y];
			float z = event.values[SensorManager.DATA_Z];

			float speed = Math.abs(x+y+z - lastX - lastY - lastZ) / diff * 10000;
			if (speed > FORCE_THRESHOLD) {
				if ((++shakeCount >= SHAKE_COUNT) && (now - lastShake > SHAKE_DURATION)) {
					lastShake = now;
					shakeCount = 0;

					Log.v(TAG, "detected a shake!");
					viewer.setText(jokes.get(shuffle()));
				}

				lastForce = now;
			}
			
			lastTime = now;
			lastX = x;
			lastY = y;
			lastZ = z;
		}
	}

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
		sManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Called when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
		sManager.unregisterListener(this);
    }

    /**
     * Called when your activity's options menu needs to be created.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // We are going to create two menus. Note that we assign them
        // unique integer IDs, labels from our string resources, and
        // given them shortcuts.
        menu.add(0, BACK_ID, 0, R.string.back).setShortcut('0', 'b');
        menu.add(0, CLEAR_ID, 0, R.string.clear).setShortcut('1', 'c');

        return true;
    }
     */

    /**
     * Called right before your activity's option menu is displayed.
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Before showing the menu, we need to decide whether the clear
        // item is enabled depending on whether there is text to clear.
        menu.findItem(CLEAR_ID).setVisible(mEditor.getText().length() > 0);

        return true;
    }
     */

    /**
     * Called when a menu item is selected.
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case BACK_ID:
            finish();
            return true;
        case CLEAR_ID:
            mEditor.setText("");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
     */

}
