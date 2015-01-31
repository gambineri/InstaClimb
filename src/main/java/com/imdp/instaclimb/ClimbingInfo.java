package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.io.File;

public class ClimbingInfo extends Activity {
  AutoCompleteTextView m_AscNameCtl  = null;
  AutoCompleteTextView m_LocationCtl = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.climbing_info);
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    // Get a reference to the AutoCompleteTextView in the layout
    m_AscNameCtl  = (AutoCompleteTextView) findViewById(R.id.ascentname);
    m_LocationCtl = (AutoCompleteTextView) findViewById(R.id.location);

    Helpers.Do.loadPrefData(this, m_AscNameCtl, Helpers.Const.ASCENTNAME_HIST);
    Helpers.Do.loadPrefData(this, m_LocationCtl, Helpers.Const.LOCATION_HIST);

    // clean up images older than 30 days
    File img_folder = (new SessionImage("InstaClimb")).getCapturedImageDir();
    SessionImageCleaner sic = new SessionImageCleaner(30, img_folder);
    sic.execute();
  }

  public void onNext(View v) {
    View asc = findViewById(R.id.ascentname);
    View loc = findViewById(R.id.location);

    String ascentname = (asc != null ? ((EditText)asc).getText().toString() : "");
    String location   = (loc != null ? ((EditText)loc).getText().toString() : "");

    Intent i = new Intent(ClimbingInfo.this, CameraActivity.class);
    i.putExtra(Helpers.Const.EXTRA_ASCENT_NAME, ascentname);
    i.putExtra(Helpers.Const.EXTRA_LOCATION, location);

    Helpers.Do.savePrefData(this, m_AscNameCtl, Helpers.Const.ASCENTNAME_HIST, ascentname);
    Helpers.Do.savePrefData(this, m_LocationCtl, Helpers.Const.LOCATION_HIST, location);

    ClimbingInfo.this.startActivity(i);
  }
}
