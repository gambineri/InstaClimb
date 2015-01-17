package com.imdp.instaclimb;

import android.app.Activity;
import android.util.Log;

public class CameraActivityTest extends android.test.ActivityInstrumentationTestCase2<CameraActivity> {

  Activity mActivity;

  public CameraActivityTest() {
    super(CameraActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    setActivityInitialTouchMode(false);
    Activity mActivity = getActivity();
  }

  public void test() {
    Log.d(Helpers.Const.DBGTAG, "finally inside test()...");
    Helpers.Do.msgBox(mActivity, "aaa");
//    assertEquals("stocasio", "1", "2");
  }

  public void test_toCamelCase() {
    System.out.println(">" + Helpers.Do.toCamelCase("aaa bbb ccc", " ", null)+"<");
    System.out.println(">" + Helpers.Do.toCamelCase("aaa+bbb+ccc", "+", null)+"<");
    System.out.println(">" + Helpers.Do.toCamelCase("aaazbbbzccc", "z", " ")+"<");
    System.out.println(">" + Helpers.Do.toCamelCase("aaayzbbbyzccc", "yz", "-")+"<");
    System.out.println(">" + Helpers.Do.toCamelCase("aaaxyzbbbxyzccc", "xyz", " - ")+"<");
    System.out.println(">" + Helpers.Do.toCamelCase("aaa1bbb1ccc", "1", ".")+"<");
    System.out.println(">" + Helpers.Do.toCamelCase("aaa_bbb_ccc", "_", " * ")+"<");
  }
}