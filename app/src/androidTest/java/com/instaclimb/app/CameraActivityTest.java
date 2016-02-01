package com.instaclimb.app;

import android.app.Activity;
import android.test.UiThreadTest;
import android.util.Log;
import com.instaclimb.app.activities.CameraActivity;

public class CameraActivityTest extends android.test.ActivityInstrumentationTestCase2<CameraActivity> {

  Activity mActivity;

  public CameraActivityTest() {
    super(CameraActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    setActivityInitialTouchMode(true);

//    Intent i = new Intent(, CameraActivity.class);
//    i.putExtra(Helpers.Const.EXTRA_ASCENT_NAME, "Senza Penzieri");
//    i.putExtra(Helpers.Const.EXTRA_LOCATION, "Bomarz");
//    setActivityIntent(i);
    mActivity = getActivity();
  }

  @UiThreadTest
  public void testCameraActivity() {
    Log.d(Helpers.Const.DBGTAG, "finally inside test()...");
//    Helpers.Do.msgBox(mActivity, "aaa");
    assertEquals("stocasio", "1", "1");
    assertNotNull(mActivity);
  }

}