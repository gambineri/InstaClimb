package com.instaclimb.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.instaclimb.app.R;


public class SettingsFragment extends PreferenceFragment {
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
