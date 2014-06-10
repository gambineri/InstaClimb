package com.imdp.instaclimb;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Camera;
import android.view.Surface;
import android.widget.Toast;

public final class Helpers {
  
	public final static class Const {
    public static final String DBGTAG = "IMDP";
		public static final String CAPTURED_IMG_PREFIX = "CAPTUREDIMG_";
		public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int CROP_IMAGE_REQUEST_CODE = 321;
  }
	
	public final static class Do {
		public static void MsgBox(Activity activity, String msg) {
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			// 2. Chain together various setter methods to set the dialog characteristics
			builder.setMessage(msg)
			       .setTitle("Alert")
			       .setPositiveButton("Ok", null);

			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();		
			
			// 4. Display the dialog 
			dialog.show();
		}

    public static void Toast(Activity activity, String msg) {
      Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
      camera.setDisplayOrientation(getRotationRelativeToNaturalOrientaton(activity, cameraId, camera));
    }

    public static int getRotationRelativeToNaturalOrientaton(Activity activity,
                                                             int cameraId,
                                                             android.hardware.Camera camera) {
      Camera.CameraInfo info = new Camera.CameraInfo();
      Camera.getCameraInfo(cameraId, info);
      int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      int degrees = 0;
      switch (rotation) {
        case Surface.ROTATION_0: degrees = 0; break;
        case Surface.ROTATION_90: degrees = 90; break;
        case Surface.ROTATION_180: degrees = 180; break;
        case Surface.ROTATION_270: degrees = 270; break;
      }

      int result;
      if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        result = (info.orientation + degrees) % 360;
        result = (360 - result) % 360;  // compensate the mirror
      } else {  // back-facing
        result = (info.orientation - degrees + 360) % 360;
      }
      return result;
    }
  }
}
