package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.camera.CropImageIntentBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

//cropimage lib


public class CameraActivity extends Activity {

  private Camera m_Camera = null;
  private CameraPreview m_Preview = null;
  private SessionImage m_SessionImg = null;

  private void setUpCamera() {
    if (!checkCameraHardware(this)) {
      Helpers.Do.MsgBox(this, "No camera available -00- Don't be SO lousy, buy yourself a better device...");
      return;
    }

    if (m_Camera != null)
      releaseCamera();

    if ((m_Camera = getCameraInstance()) != null) {
      this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

      try {
        Log.v(Helpers.Const.DBGTAG, getCurrentCameraInfo());
        Parameters pars = m_Camera.getParameters();

        pars.setPictureFormat(ImageFormat.JPEG);
        pars.setPictureSize(960, 720);
//        pars.setRotation(90);
        m_Camera.setParameters(pars);
      } catch (RuntimeException re) {
        Log.e(Helpers.Const.DBGTAG, re.getMessage());
      }

      //Create image wrapper obj for this session
      m_SessionImg = new SessionImage(getResources().getString(R.string.app_name));

      // Set the camera in CameraPreview view
      m_Preview.setCamera(m_Camera);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.camera_activity);

    // Create our Preview view and set it as the content of our activity.
    m_Preview = new CameraPreview(this);
    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(m_Preview);

    LinearLayout ll = (LinearLayout) findViewById(R.id.top_frame);
    preview.removeView(ll);
    preview.addView(ll);

    //Add a listener to the Capture button
    Button captureButton = (Button) findViewById(R.id.button_capture);
    captureButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // get an image from the camera
        m_Camera.takePicture(null, null, m_Picture);
      }
      }
    );
    captureButton.requestFocus();
  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseCamera();              // release the camera immediately on pause event
  }

  @Override
  protected void onResume() {
    super.onResume();
    setUpCamera();
  }

  private void releaseCamera() {
    if (m_Camera != null) {
      m_Camera.stopPreview();
      m_Camera.release();        // release the camera for other applications
      m_Camera = null;
    }
  }

  private PictureCallback m_Picture = new PictureCallback() {
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

      try {
        FileOutputStream fos = new FileOutputStream(m_SessionImg.getImageFile());
        fos.write(data);
        fos.close();
        cropImage();
      } catch (FileNotFoundException e) {
        Log.d(Helpers.Const.DBGTAG, "File not found: " + e.getMessage());
      } catch (IOException e) {
        Log.d(Helpers.Const.DBGTAG, "Error accessing file: " + e.getMessage());
      }
    }
  };

  private void cropImage() {

    Uri srcUri = Uri.fromFile(new File(m_SessionImg.getImageFilePathName()));
    Uri dstUri = Uri.fromFile(new File(m_SessionImg.getImageFilePathName() + ".jpg"));

    CropImageIntentBuilder cropImage = new CropImageIntentBuilder(1, 1, 600, 600, dstUri);
    cropImage.setSourceImage(srcUri);
    cropImage.setScaleUpIfNeeded(true);
    cropImage.setDoFaceDetection(false);

    // start activity CropImage with certain request code and listen for result
//    startActivity(cropImage.getIntent(this));
    startActivityForResult(cropImage.getIntent(this), Helpers.Const.CROP_IMAGE_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != RESULT_OK)
      return;

    switch (requestCode) {
      case Helpers.Const.CROP_IMAGE_REQUEST_CODE:


        Helpers.Do.MsgBox(this, "Fatto!");

        // cropped bitmap
//        Bitmap bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());

        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * Check if this device has a camera
   */
  private boolean checkCameraHardware(Context context) {
    if (context != null) {
      PackageManager pm = context.getPackageManager();
      return (pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA));
    }
    return false;
  }

  /**
   * A safe way to get an instance of the Camera object.
   */
  public static Camera getCameraInstance() {
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    } catch (Exception e) {
      // Camera is not available (in use or does not exist)
      Log.e(Helpers.Const.DBGTAG, "Exception in getCameraInstance - sanne scassate tutt'eccos'\n" + e.getMessage());
    }
    return c; // returns null if camera is unavailable
  }

  /**
   * Camera info
   */
  private String getCurrentCameraInfo() {
    String ret = "";
    List<Camera.Size> sizes;
    ListIterator<Camera.Size> li;
    Camera.Size tmp;

    Parameters pars = m_Camera.getParameters();

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

    ret += "\nCurrent Picture Format and Size:\n";
    ret += "Format: " + m_Camera.getParameters().getPictureFormat() + "; Size: " +
        m_Camera.getParameters().getPictureSize().width + "x" + m_Camera.getParameters().getPictureSize().height + "\n";

    ret += "\nCurrent Preview Format and Size:\n";
    ret += "Format: " + m_Camera.getParameters().getPreviewFormat() + "; Size: " +
        m_Camera.getParameters().getPreviewSize().width + "x" + m_Camera.getParameters().getPreviewSize().height + "\n";

    return ret;
  }

} // class CameraActivity
