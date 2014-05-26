package com.imdp.instaclimb;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by massimo on 5/26/14.
 */
public class SessionImage {

  private String m_ImageFilePathName = null;
  private File m_ImageFile = null;
  private File m_MediaStorageDir = null;

  public SessionImage(String appname) {
    //TODO: To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    m_MediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appname);
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (!m_MediaStorageDir.exists()){
      if (!m_MediaStorageDir.mkdirs())
        Log.d(appname, "failed to create media storage directory");
    }

    if (m_MediaStorageDir != null) {
      buildOutputFile();
      if (m_ImageFile == null) throw new InstantiationError("Could not create SessionImage object");
    }
  }

  public String getImageFilePathName() {
    return m_ImageFilePathName;
  }

  public File getImageFile() {
    return m_ImageFile;
  }

  /** Creates a File for saving an image */
  private void buildOutputFile() {
    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    m_ImageFilePathName = m_MediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";

    //create output file
    m_ImageFile = new File(m_ImageFilePathName);
  }
}
