package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;


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
    ClimbingInfo.this.startActivity(new Intent(ClimbingInfo.this, CameraActivity.class));
  }
}
