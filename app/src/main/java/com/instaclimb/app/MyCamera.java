package com.instaclimb.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by massimo on 07/02/16.
 */
public enum MyCamera {

  INSTANCE;

  private SessionImage  m_SessionImg      = null;
  private Activity       m_Activity = null;

  private int                   m_CameraId = -1;

  private Camera                m_Camera   = null;

  // Best resolution for the camera hardware on the current device
  private Camera.Size           m_BestRes         = null;

  // Degrees of rotation if the portrait mode is rotated (90 or 270)
  // with respect to the natural orientation of the device
  private int                   m_DevRotation     = 0;

  // True if the portrait mode is rotated (90 or 270) with respect
  // to the natural orientation of the device
  private boolean               m_ImgDimInverted  = false;

  /**** Getters and Setters ****/
  public Camera getAndroidCamera() {
    return m_Camera;
  }

  public int getDeviceRotation() {
    return m_DevRotation;
  }

  public Camera.Size getBestRes() {
    return m_BestRes;
  }

  public boolean isImgDimInverted() {
    return m_ImgDimInverted;
  }

  // Static getter
  public static MyCamera getInstance() {
    return INSTANCE;
  }

  // Constructor (CAVEAT: You cannot invoke an enum constructor yourself)
  MyCamera() {
  }

  public void setupMyCamera(Activity activity) {

    if (activity != null)
      m_Activity = activity;
    else
      return;

    if (!checkCameraHardware()) {
      Helpers.Do.msgBox(m_Activity, "Wow, no camera available -00-. Don't be SO lousy, buy yourself a better device...");
      return;
    }

    if (m_Camera != null)
      releaseCamera();

    if (findBackCamera()) {
      try {
        // Because we want a portrait app, calculate rotation respect to natural device orientation
        m_DevRotation = Helpers.Do.getRotationRelativeToNaturalOrientaton(m_Activity, m_CameraId);

        // In case the device natural orientation is not portrait (or multiple thereof)
        // let's save the bool to know if picture dimensions need to be considered as inverted
        // (W in place of H and viceversa)
        m_ImgDimInverted = (m_DevRotation == 90 || m_DevRotation == 270);

        //todo Log calls to be removed with ProGuard when publishing
        //        Log.v(Helpers.Const.DBGTAG, getCurrentCameraInfo());
        Camera.Parameters pars = m_Camera.getParameters();

        pars.setPictureFormat(ImageFormat.JPEG);
        if (!findFirstCameraResolution(1000, 1000))
          findBestCameraResolution();
        pars.setPictureSize(m_BestRes.width, m_BestRes.height);

        m_Camera.setParameters(pars);
      } catch (RuntimeException re) {
        Log.e(Helpers.Const.DBGTAG, re.getMessage());
        return;
      }

      //Create image wrapper obj for this session
      m_SessionImg = new SessionImage(m_Activity.getResources().getString(R.string.app_name));
    }
  }

  public void releaseCamera() {
    if (m_Camera != null) {
      m_Camera.stopPreview();
      m_Camera.release();        // release the camera for other applications
      m_Camera = null;
    }
  }

  public void startCamera() {
    if (m_CameraId == -1)
      return;

    try {
      m_Camera = Camera.open(m_CameraId); // attempt to get a Camera instance
    } catch (Exception e) {
      // Camera is not available (in use or does not exist)
      Log.e(Helpers.Const.DBGTAG, "Exception in startCamera: Could not open camera (sanne scassate tutte ccose)\n" + e.getMessage());
    }

    if (m_Camera != null) {
      m_Camera.startPreview();
    }
  }

  private final Camera.PictureCallback m_Picture = new Camera.PictureCallback() {
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
//      new InstaJob().execute(data);
    }
  };


  /**
   * Check if this device has a camera
   */
  private boolean checkCameraHardware() {
    if (m_Activity != null) {
      PackageManager pm = m_Activity.getPackageManager();
      return (pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA));
    }
    return false;
  }

  /**
   * A safe way to get an instance of the Camera object.
   */
  private boolean findBackCamera() {
    boolean found = false;

    try {
      //Find backward camera
      m_CameraId = 0;
      while(m_CameraId < Camera.getNumberOfCameras() && !found) {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        Camera.getCameraInfo(m_CameraId, ci);
        if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
          found = true;
        else
          m_CameraId++;
      }

      m_Camera = Camera.open(m_CameraId); // attempt to get a Camera instance
    } catch (Exception e) {
      // Camera is not available (in use or does not exist)
      Log.e(Helpers.Const.DBGTAG, "Exception in findBackCamera - sanne scassate tutte cose\n" + e.getMessage());
    }

    return found; // returns false if camera is found
  }

  private void findBestCameraResolution() {
    if (m_Camera == null)
      return;

    m_BestRes = m_Camera.new Size(0, 0);

    List<Camera.Size> sizes;
    ListIterator<Camera.Size> li;
    Camera.Size tmp;

    Camera.Parameters pars = m_Camera.getParameters();

    if ((sizes = pars.getSupportedPictureSizes()) != null) {
      li = sizes.listIterator();
      while (li.hasNext()) {
        tmp = li.next();
        if (tmp.width > m_BestRes.width || tmp.height > m_BestRes.height)
          m_BestRes = tmp;
      }
    }
  }

  private boolean findFirstCameraResolution(int basew, int baseh) {
    if (m_Camera == null)
      return false;

    m_BestRes = m_Camera.new Size(basew, baseh);
    Camera.Parameters pars = m_Camera.getParameters();
    List<Camera.Size> sizes;
    ListIterator<Camera.Size> li;
    Camera.Size tmp;

    if ((sizes = pars.getSupportedPictureSizes()) != null) {
      li = sizes.listIterator();
      while (li.hasNext()) {
        tmp = li.next();
        if (tmp.width >= m_BestRes.width && tmp.height >= m_BestRes.height) {
          m_BestRes = tmp;
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Camera info - debug facility
   */
  private String getCurrentCameraInfo() {
    String ret = "";
    List<Camera.Size> sizes;
    ListIterator<Camera.Size> li;
    Camera.Size tmp;

    Camera.Parameters pars = m_Camera.getParameters();

    ret += "Supported Picture Sizes:\n";
    if ((sizes = pars.getSupportedPictureSizes()) != null) {
      li = sizes.listIterator();
      while (li.hasNext()) {
        tmp = li.next();
        ret += tmp.width + "x" + tmp.height + "\n";
      }
    }

    ret += "\nSupported Preview Sizes:\n";
    if ((sizes = pars.getSupportedPreviewSizes()) != null) {
      li = sizes.listIterator();
      while (li.hasNext()) {
        tmp = li.next();
        ret += tmp.width + "x" + tmp.height + "\n";
      }
    }

    Camera.Size cs = pars.getPictureSize();
    if (cs != null) {
      ret += "\nCurrent Picture Format and Size:\n";
      ret += "Format: " + m_Camera.getParameters().getPictureFormat() + "; Size: " +
        cs.width + "x" + cs.height + "\n";
    }

    Camera.Size ps = pars.getPreviewSize();
    if (ps != null) {
      ret += "\nCurrent Preview Format and Size:\n";
      ret += "Format: " + m_Camera.getParameters().getPreviewFormat() + "; Size: " +
        ps.width + "x" + ps.height + "\n";
    }

    return ret;
  }
}
