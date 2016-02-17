package com.instaclimb.app.views;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.instaclimb.app.Helpers;
import com.instaclimb.app.MyCamera;

import java.io.IOException;

/**
 * The Camera Preview class
 * */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private SurfaceHolder m_Holder      = null;
  private MyCamera      m_MyCamera    = null;

  public CameraPreview(Context context, MyCamera c, int dev_rot) {
    super(context);

    m_MyCamera = c;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    m_Holder = getHolder();
    if (m_Holder != null) {
      m_Holder.addCallback(this);
      // deprecated settings, but required on Android versions prior to 3.0
      m_Holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    } else throw new AssertionError("m_Holder is null in CameraPreview(Context context, Camera camera)");
  }

  public void surfaceCreated(SurfaceHolder holder) {
    // The Surface has been created, now tell the camera where to draw the preview.
    try {
      if (m_MyCamera.isOpen()) {
        m_MyCamera.getAndroidCamera().setPreviewDisplay(holder);
        m_MyCamera.getAndroidCamera().startPreview();
      }
    } catch (IOException e) {
      Log.d(Helpers.Const.DBGTAG, "surfaceCreated: Error settings camera preview: " + e.getMessage());
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // empty. Take care of releasing the Camera preview in your activity.
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    // If your preview can change or rotate, take care of those events here.
    // Make sure to stop the preview before resizing or reformatting it.
    if (m_Holder.getSurface() == null)
      return; // preview surface does not exist

    if (!m_MyCamera.isOpen())
      return;

    // stop preview before making changes
    try {
      m_MyCamera.getAndroidCamera().stopPreview();
    } catch (Exception e){
      // ignore: tried to stop a non-existent preview
    }

    // set preview size and make any resize, rotate or reformatting changes here
    // and start preview with new settings
    try {
      Log.d(Helpers.Const.DBGTAG, "surfaceChanged: StartPreview");
      m_MyCamera.getAndroidCamera().setDisplayOrientation(m_MyCamera.getDeviceRotation());
      m_MyCamera.getAndroidCamera().setPreviewDisplay(m_Holder);
      m_MyCamera.getAndroidCamera().startPreview();
    } catch (Exception e){
      Log.d(Helpers.Const.DBGTAG, "surfaceChanged: Error starting camera preview: " + e.getMessage());
    }
  }
} // *** class CameraPreview
