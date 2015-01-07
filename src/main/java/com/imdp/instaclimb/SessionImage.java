package com.imdp.instaclimb;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by massimo on 5/26/14.
 *
 * Wrapper class for the captured image I/O mgmt.
 *
 */
class SessionImage {

  private String  m_CapturedImgFilePathName = null;
  private File    m_CapturedImgFile         = null;
  private File    m_MediaStorageDir         = null;
  private String  m_CroppedImgFilePathName  = null;

  public SessionImage(String appname) {
    //TODO: To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.
    m_MediaStorageDir =
        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                 appname);

    // Create the storage directory if it does not exist
    if (!m_MediaStorageDir.exists()){
      if (!m_MediaStorageDir.mkdirs())
        Log.d(appname, "failed to create media storage directory");
    }

    if (m_MediaStorageDir != null) {
      buildOutputFile();
      if (m_CapturedImgFile == null) throw new InstantiationError("Could not create SessionImage object");
    }
  }

  public File getCapturedImageDir() {
    return m_MediaStorageDir;
  }

  public String getCapturedImageFilePathName() {
    return m_CapturedImgFilePathName;
  }

  public File getCapturedImageFile() {
    return m_CapturedImgFile;
  }

  public String getCroppedImageFilePathName() { return m_CroppedImgFilePathName; }

  /** Creates a File for saving an image */
  private void buildOutputFile() {
    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    m_CapturedImgFilePathName = m_MediaStorageDir.getPath() + File.separator +
        Helpers.Const.CAPTURED_IMG_PREFIX + timeStamp + ".jpg";

    //create output file
    m_CapturedImgFile = new File(m_CapturedImgFilePathName);

    // Generate also a cropped image file pathname
    m_CroppedImgFilePathName = m_MediaStorageDir.getPath() + File.separator +timeStamp + ".jpg";
  }
}
