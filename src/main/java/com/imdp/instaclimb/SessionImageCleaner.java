package com.imdp.instaclimb;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.*;

/**
 * Created by massimo on 08/01/15.
 */
public class SessionImageCleaner extends AsyncTask<SessionImageCleaner.Params, Void, Void> {

  /* Num of milliseconds in 24 hours */
  private final static int MILLIS_PER_DAY = 86400000;

  class Params {
    int  m_RetentionDays = -1;
    File m_Folder        = null;
  }

  /* Params for this cleaner instance */
  Params m_Params = new Params();

  public SessionImageCleaner(File folder_path, int retention_days) {
    m_Params.m_RetentionDays = retention_days;
    m_Params.m_Folder = folder_path;
  }

  @Override
  protected Void doInBackground(SessionImageCleaner.Params...params) {

    if (m_Params == null) {
      Log.d(Helpers.Const.DBGTAG, "Could not create SessionImageCleaner: m_Params is null.");
      return null;
    }

    Calendar time = Calendar.getInstance();
    time.add(Calendar.DAY_OF_YEAR, -1*params[0].m_RetentionDays);

    File[] files = params[0].m_Folder.listFiles();
    for (File f : files) {
      if (f.isFile()) {
        Date lastModified = new Date(f.lastModified());
        if(lastModified.before(time.getTime())) {
          f.delete(); //file is older than XX days
        }
      }
    }

    return null;
  }

}

