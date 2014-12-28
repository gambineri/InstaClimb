package com.imdp.instaclimb;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;


public class ClimbingInfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.climbing_info);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

}
