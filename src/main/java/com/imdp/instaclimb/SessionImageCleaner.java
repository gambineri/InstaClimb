package com.imdp.instaclimb;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.util.*;

/**
 * Created by massimo on 08/01/15.
 */
public class SessionImageCleaner extends AsyncTask<Void, Void, Void> {

  /* Num of milliseconds in 24 hours */
  private final static int MILLIS_PER_DAY = 86400000;

  int  m_RetentionDays = -1;
  File m_Folder        = null;

  public SessionImageCleaner(int retention_days, File folder) {
    m_RetentionDays = retention_days;
    m_Folder = folder;
  }

  @Override
  protected Void doInBackground(Void...p) {

//    android.os.Debug.waitForDebugger();

    Log.d(Helpers.Const.DBGTAG, "------------------------ 0");

    if (m_Folder == null) {
      Log.d(Helpers.Const.DBGTAG, "Cannot doInBackground: m_Folder is null.");
      return null;
    }

    Log.d(Helpers.Const.DBGTAG, "------------------------ m_RetentionDays = " + m_RetentionDays);

    Calendar time = Calendar.getInstance();
    time.add(Calendar.DAY_OF_YEAR, -1*m_RetentionDays);

    Log.d(Helpers.Const.DBGTAG, "------------------------ m_Folder = " + m_Folder.getAbsolutePath());

    File[] files = m_Folder.listFiles();
    for (File f : files) {
      if (f.isFile()) {
        Date lastModified = new Date(f.lastModified());
        if(lastModified.before(time.getTime())) {
          f.delete(); //file is older than XX days
          Log.d(Helpers.Const.DBGTAG, "------------------------ Deleted " + f.getAbsolutePath());
        }
      }
    }

    Log.d(Helpers.Const.DBGTAG, "------------------------ End" );
    return null;
  }

}

