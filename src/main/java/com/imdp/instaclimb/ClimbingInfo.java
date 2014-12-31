package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.EditText;


public class ClimbingInfo extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    getWindow().setWindowAnimations(android.R.anim.slide_in_left);
//    getWindow().setWindowAnimations(android.R.anim.slide_out_right);
    setContentView(R.layout.climbing_info);
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
  }

  public void onNext(View v) {
    View asc = findViewById(R.id.ascentname);
    View loc = findViewById(R.id.location);

    String ascentname = (asc != null ? ((EditText)asc).getText().toString() : "");
    String location   = (loc != null ? ((EditText)loc).getText().toString() : "");

    Intent i = new Intent(ClimbingInfo.this, CameraActivity.class);
    i.putExtra(Helpers.Const.EXTRA_ASCENT_NAME, ascentname);
    i.putExtra(Helpers.Const.EXTRA_LOCATION, location);

    ClimbingInfo.this.startActivity(i);
  }
}
