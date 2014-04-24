package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

public class CameraActivity extends Activity {

	private Camera m_Camera = null;
	private CameraPreview m_Preview = null;

  /*=======================================================================================*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    setContentView(R.layout.camera_activity);

		if (checkCameraHardware(this)) {
			if ((m_Camera = getCameraInstance()) != null) {
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

				try {
          Log.v(Static.Const.DBGTAG, getCurrentCameraInfo());
          Parameters pars = m_Camera.getParameters();

//          pars.setPictureFormat(ImageFormat.JPEG);
          pars.setPictureSize(960, 720);
          pars.setRotation(90);
          m_Camera.setParameters(pars);
				} 
				catch(RuntimeException re) {
					Log.e(Static.Const.DBGTAG, re.getMessage());
				}
				
	      // Create our Preview view and set it as the content of our activity.
	      m_Preview = new CameraPreview(this, m_Camera);
	      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	      preview.addView(m_Preview);

        LinearLayout ll = (LinearLayout)findViewById(R.id.top_frame);
        preview.removeView(ll);
        preview.addView(ll);

        //Add a listener to the Capture button
	    	Button captureButton = (Button) findViewById(R.id.button_capture);
	    	captureButton.setOnClickListener(
	    	  new View.OnClickListener() {
	          @Override
	          public void onClick(View v) {
	            // get an image from the camera
	            m_Camera.takePicture(null, null, m_Picture);
	          }
	    	  }
	    	); 
			}
		} else {
			Static.Do.MsgBox(this, "No camera available :-/ don't be _SO_ lousy, buy yourself a better device...");
		}
	}

  /*=======================================================================================*/
	@Override
  protected void onPause() {
    super.onPause();
    releaseCamera();              // release the camera immediately on pause event
  }

  /*=======================================================================================*/
  @Override
  protected void onResume() {
    super.onResume();
    //TBD Bisogna re-open the camera.....
  }

  /*=======================================================================================*/
  private void releaseCamera(){
		if (m_Camera != null){
    	m_Camera.stopPreview();
	    m_Camera.release();        // release the camera for other applications
	    m_Camera = null;
		}
  }
  
  /*=======================================================================================*/
	private PictureCallback m_Picture = new PictureCallback() {
	  @Override
	  public void onPictureTaken(byte[] data, Camera camera) {
	  	
      File pictureFile = Static.Do.getOutputMediaFile(Static.Const.MEDIA_TYPE_IMAGE, 
      																								getResources().getString(R.string.app_name));
      if (pictureFile == null){
				Log.d(Static.Const.DBGTAG, "Error creating media file, check storage permissions");
				return;
      }

      try {
        FileOutputStream fos = new FileOutputStream(pictureFile);
        fos.write(data);
        fos.close();
      } catch (FileNotFoundException e) {
        Log.d(Static.Const.DBGTAG, "File not found: " + e.getMessage());
      } catch (IOException e) {
        Log.d(Static.Const.DBGTAG, "Error accessing file: " + e.getMessage());
      }
     }
	};

  /*=======================================================================================*/
  private void cropImage(Uri imageCaptureUri) {
    Intent intent = new Intent("com.android.camera.action.CROP");
    intent.setClassName("com.android.camera", "com.android.camera.CropImage");

    intent.setData(imageCaptureUri);
    intent.putExtra("outputX", 720);
    intent.putExtra("outputY", 720);
    intent.putExtra("aspectX", 1);
    intent.putExtra("aspectY", 1);
    intent.putExtra("scale", true);
//    intent.putExtra("return-data", true);
//    intent.putExtra("output", Uri.parse("file:/" +  mFile.getAbsolutePath()));
    startActivityForResult(intent, 123);
  }

  /*=======================================================================================*/
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}	
	
  /*=======================================================================================*/
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
    Camera c = null;
    try {
    	c = Camera.open(); // attempt to get a Camera instance
    }
    catch (Exception e){
      // Camera is not available (in use or does not exist)
    	Log.e(Static.Const.DBGTAG, "Exception in getCameraInstance - sanne scassat' tutte ccos'\n" + e.getMessage());
    }
    return c; // returns null if camera is unavailable
	}
	
  /*=======================================================================================*/
	/** Camera info */
	private String getCurrentCameraInfo() {
		String ret = "";
		List<Camera.Size> sizes;
		ListIterator<Camera.Size> li;
		Camera.Size tmp;
		
		Parameters pars = m_Camera.getParameters();
		
		ret += "Supported Picture Sizes:\n";
		sizes = pars.getSupportedPictureSizes();
		li = sizes.listIterator();
		while (li.hasNext()) {
			tmp = li.next();
			ret += tmp.width + "x" + tmp.height + "\n";
		}
		
		ret += "\nSupported Preview Sizes:\n";
		sizes = pars.getSupportedPreviewSizes();
		li = sizes.listIterator();
		while (li.hasNext()) {
			tmp = li.next();
			ret += tmp.width + "x" + tmp.height + "\n";
		}
		
		ret += "\nCurrent Picture Format and Size:\n";
		ret += "Format: " + m_Camera.getParameters().getPictureFormat() + "; Size: " + 
					 m_Camera.getParameters().getPictureSize().width + "x" + m_Camera.getParameters().getPictureSize().height + "\n";
		
		ret += "\nCurrent Preview Format and Size:\n";
		ret += "Format: " + m_Camera.getParameters().getPreviewFormat() + "; Size: " + 
					 m_Camera.getParameters().getPreviewSize().width + "x" + m_Camera.getParameters().getPreviewSize().height + "\n";
		
		return ret;
	}
	
}
