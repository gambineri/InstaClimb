package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;


/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
  private SurfaceHolder m_Holder;
  private Camera m_Camera;
  protected Activity m_Activity;

  public CameraPreview(Context context, Camera camera) {
    super(context);
    m_Camera = camera;
    m_Activity = (Activity)context;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    m_Holder = getHolder();
    if (m_Holder != null) {
      m_Holder.addCallback(this);
      // deprecated setting, but required on Android versions prior to 3.0
      m_Holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    } else {
      throw new AssertionError();
    }
  }

  public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		try {
      m_Camera.setPreviewDisplay(holder);
      m_Camera.startPreview();
    } catch (IOException e) {
	    Log.d(Helpers.Const.DBGTAG, "Error setting camera preview: " + e.getMessage());
		}
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // empty. Take care of releasing the Camera preview in your activity.
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    // If your preview can change or rotate, take care of those events here.
    // Make sure to stop the preview before resizing or reformatting it.

    if (m_Holder.getSurface() == null){
      // preview surface does not exist
      return;
    }

    // stop preview before making changes
    try {
      m_Camera.stopPreview();
    } catch (Exception e){
      // ignore: tried to stop a non-existent preview
    }

    // set preview size and make any resize, rotate or reformatting changes here

    // start preview with new settings
    try {
      m_Camera.setDisplayOrientation(90);
      m_Camera.setPreviewDisplay(m_Holder);
      m_Camera.startPreview();
    } catch (Exception e){
      Log.d(Helpers.Const.DBGTAG, "Error starting camera preview: " + e.getMessage());
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    int parentWidth = View.MeasureSpec.getSize(widthMeasureSpec);
    int parentHeight = View.MeasureSpec.getSize(heightMeasureSpec);


    int top = (parentHeight-parentWidth)/2;
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(parentWidth, parentWidth);
    layoutParams.setMargins(0, top, parentWidth, parentWidth);
    setLayoutParams(layoutParams);

    setMeasuredDimension(parentWidth, parentWidth);

//    Helpers.Do.MsgBox(m_Activity, this.getWidth() + "");
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
