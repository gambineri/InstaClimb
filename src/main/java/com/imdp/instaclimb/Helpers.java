package com.imdp.instaclimb;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Helpers {
  
	public final static class Const {
		public static final String DBGTAG = "IMDP";
		public static final int MEDIA_TYPE_IMAGE = 1;		
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
	
		/** Create a File for saving an image or video */
		public static File getOutputMediaFile(int type, String appname){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = 
     		new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appname);
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	      if (! mediaStorageDir.mkdirs()){
	        Log.d("MyCameraApp", "failed to create directory");
	        return null;
	      }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == Helpers.Const.MEDIA_TYPE_IMAGE)
	      mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
	    else 
	      return null;

	    return mediaFile;
		}	
	}
}
