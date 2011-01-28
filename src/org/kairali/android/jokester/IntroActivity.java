/*
 * Intro (splash) screen
 */

package org.kairali.android.jokester;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.util.Log;
import java.util.*;
import java.io.*;

/**
 * Splash screen
 */
public class IntroActivity extends Activity {
    private static final String TAG = "Jokester";
	private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.intro);

		context = getApplicationContext();

        ImageView intro = (ImageView) findViewById(R.id.intro);
		intro.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
   				Log.v(TAG, "got touch!");
				try {
					// Target our joke viewer class
					startActivity(new Intent(context, 
   						   Class.forName("org.kairali.android.jokester.JokeViewActivity")));
				} catch (Exception ex) {
					Log.e(TAG, "Error when starting JokeViewActivity", ex);
					return true;
				}

				return true;
			}
		});
    }
}
