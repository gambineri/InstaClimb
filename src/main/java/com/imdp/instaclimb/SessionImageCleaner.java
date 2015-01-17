package com.imdp.instaclimb;

import android.os.AsyncTask;

import java.io.File;
import java.util.*;

/**
 * Created by massimo on 08/01/15.
 */
public class SessionImageCleaner extends AsyncTask<File, Void, Void> {

  /* Num of days images are retained. Could be a user setting in the future */
  private final static int MAX_RETENTION_DAYS = 30;
  /* Num of milliseconds in 24 hours */
  private final static int MILLIS_PER_DAY = 86400000;

  @Override
  protected Void doInBackground(File...img_folder) {

//    final File[] sortedByDate = img_folder[0].listFiles();
//
//    if (sortedByDate != null && sortedByDate.length > 1) {
//      Arrays.sort(sortedByDate, new Comparator<File>() {
//        @Override
//        public int compare(File object1, File object2) {
//          return (int) ((object1.lastModified() > object2.lastModified()) ? object1.lastModified() : object2.lastModified());
//        }
//      });
//    }
//
//    final long max_retention_millis = MAX_RETENTION_DAYS*MILLIS_PER_DAY;
//    final long cut_time = GregorianCalendar.getInstance().getTimeInMillis();


    Calendar time = Calendar.getInstance();
    time.add(Calendar.DAY_OF_YEAR, -7);

    File[] files = img_folder[0].listFiles();
    for (File f : files) {
      if (f.isFile()) {
        Date lastModified = new Date(f.lastModified());
        if(lastModified.before(time.getTime())) {
          //file is older than a week
        }

      }
    }

    return null;
  }

}

