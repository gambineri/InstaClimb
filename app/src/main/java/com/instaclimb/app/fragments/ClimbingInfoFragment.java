package com.instaclimb.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import com.instaclimb.app.R;

/**
 * Created by massimo on 10/01/16.
 */
public class ClimbingInfoFragment extends Fragment {

  private AutoCompleteTextView  m_AscNameCtl  = null;
  private AutoCompleteTextView  m_LocationCtl = null;
  private Activity              m_Activity    = null;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    // Store current activity
    m_Activity = getActivity();

    PreferenceManager.setDefaultValues(m_Activity, R.xml.preferences, false);

    // Get a reference to the AutoCompleteTextView in the layout
    m_AscNameCtl  = (AutoCompleteTextView) m_Activity.findViewById(R.id.ascentname);
    m_LocationCtl = (AutoCompleteTextView) m_Activity.findViewById(R.id.location);

    //BUG
//    Helpers.Do.loadPrefData(m_Activity, m_AscNameCtl, Helpers.Const.ASCENTNAME_HIST);
//    Helpers.Do.loadPrefData(m_Activity, m_LocationCtl, Helpers.Const.LOCATION_HIST);

    // clean up images older than 30 days
//    NOT WORKING due to android BUG in setLastModified(): always fails!
//    File img_folder = (new SessionImage("InstaClimb")).getCapturedImageDir();
//    SessionImageCleaner sic = new SessionImageCleaner(30, img_folder);
//    sic.execute();

    return inflater.inflate(R.layout.climbing_info, container, false);
  }

  public static ClimbingInfoFragment newInstance() {
    ClimbingInfoFragment fragment = new ClimbingInfoFragment();
    return fragment;
  }

  public void onNext(View v) {
    // va rivisto totalmente perche` non lancia un'altra activity!!!!!
  }
}
