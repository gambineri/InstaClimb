package com.imdp.instaclimb;

import android.app.Activity;
import android.app.AlertDialog;

public final class Helpers {
  
	public final static class Const {
		public static final String DBGTAG = "IMDP";
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
	}
}
