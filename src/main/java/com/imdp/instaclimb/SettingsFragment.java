package com.imdp.instaclimb;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;


public class SettingsFragment extends PreferenceFragment {
    private Activity m_Activity = null;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        m_Activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
//        if (m_Activity != null)
//            m_Activity.setContentView(R.layout.settings);


    }
}
