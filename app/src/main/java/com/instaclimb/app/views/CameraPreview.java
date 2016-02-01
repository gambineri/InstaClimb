package com.instaclimb.app.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.instaclimb.app.Helpers;
import com.instaclimb.app.activities.MainActivity;

import java.io.IOException;

/**
 * The Camera Preview class
 * */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private SurfaceHolder m_Holder      = null;
  private MainActivity  m_Activity    = null;
  private Camera        m_Camera      = null;
  private int           m_DevRotation = 0;

  public CameraPreview(Context context) {
    super(context);

    m_Activity = (MainActivity)context;
    m_Camera = m_Activity.m_Camera;

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
      if (m_Camera != null) {
        m_Camera.setPreviewDisplay(holder);
        m_Camera.startPreview();
      }
    } catch (IOException e) {
      Log.d(Helpers.Const.DBGTAG, "Error settings camera preview: " + e.getMessage());
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

    if (m_Camera == null)
      return;

    // stop preview before making changes
    try {
      m_Camera.stopPreview();
    } catch (Exception e){
      // ignore: tried to stop a non-existent preview
    }

    // set preview size and make any resize, rotate or reformatting changes here
    // and start preview with new settings
    try {
      m_Camera.setDisplayOrientation(m_DevRotation);
      m_Camera.setPreviewDisplay(m_Holder);
      m_Camera.startPreview();
    } catch (Exception e){
      Log.d(Helpers.Const.DBGTAG, "Error starting camera preview: " + e.getMessage());
    }
  }
} // *** class CameraPreview
