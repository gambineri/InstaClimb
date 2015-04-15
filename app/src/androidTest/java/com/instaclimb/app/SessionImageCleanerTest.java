package com.instaclimb.app;

import android.util.Log;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Calendar;

public class SessionImageCleanerTest extends TestCase {
  private final static int NUM_TEST_FILES = 2;
  File m_img_folder = null;
  File[] files_to_del = new File[10];

  @Before
  public void setUp() throws Exception {
    m_img_folder = (new SessionImage("InstaClimb")).getCapturedImageDir();

    for (int i=0; i<NUM_TEST_FILES; i++) {
      Log.d(Helpers.Const.DBGTAG, m_img_folder + "/file" + i + ".png");
      files_to_del[i] = new File(m_img_folder + "/file" + i + ".png");
      files_to_del[i].createNewFile();
    }

    Calendar time = Calendar.getInstance();
    time.add(Calendar.DAY_OF_YEAR, -31);

    boolean ret = false;
    long t = 0;
    t = (time.getTimeInMillis()/1000)*1000;
    for (int i=0; i<NUM_TEST_FILES/2; i++) {
      Log.d(Helpers.Const.DBGTAG, "t = " + t);
      /* 2015.01.27 BUG - https://code.google.com/p/android/issues/detail?id=18624#c29 */
      ret = files_to_del[i].setLastModified(t); // NOT WORKING!!!
      Log.d(Helpers.Const.DBGTAG, "" + Boolean.toString(ret));
    }
  }

  @After
  public void tearDown() throws Exception {
    for (int i=0; i<NUM_TEST_FILES; i++) {
//      files_to_del[i].delete();
    }
  }

  @Test
  public void testDoInBackground() throws Exception {
    SessionImageCleaner sic = new SessionImageCleaner(0, m_img_folder);
    sic.execute();

//    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)
//      sic.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    else
//      sic.execute();
  }
}