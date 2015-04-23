package com.instaclimb.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.Locale;

public final class Helpers {
  /* CONST */
	public final static class Const {
    public static final String DBGTAG = "IMDP";
		public static final String CAPTURED_IMG_PREFIX = "CAPTUREDIMG_";

    //Bundles extras
    public static final String EXTRA_CAPTURED_IMG_PATH  = "capturedImgPath";
    public static final String EXTRA_TOP_FRAME_W        = "topFrameWidth";
    public static final String EXTRA_ASCENT_NAME        = "ascentName";
    public static final String EXTRA_LOCATION           = "location";
    public static final String ASCENTNAME_HIST          = "IC_ASCENTNAME_HISTORY";
    public static final String LOCATION_HIST            = "IC_LOCATION_HISTORY";
  }

  /* METHODS */
  public final static class Do {
		public static void msgBox(Activity activity, String msg) {
      if (activity == null) {
        Log.e(Const.DBGTAG, "Cannot create AlertDialog to show a msgBox: passed in activity is null.");
        return;
      }

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

    public static String toCamelCase(String input, String separator, String replacement) {
      if (input == null || input.length() <= 0)
        return "";

      if (replacement == null)
        replacement = separator;

      String[] words = input.toLowerCase(Locale.US).split(java.util.regex.Pattern.quote(separator));
      StringBuilder ret = new StringBuilder();

      for (String word : words)
        if (word != null && word.length() > 0)
          ret.append(word.substring(0, 1).toUpperCase(Locale.US)).append(word.substring(1)).append(replacement);

      return ret.toString().substring(0, ret.toString().length() - replacement.length());
    }

    public static void toast(Activity activity, String msg) {
      Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
      camera.setDisplayOrientation(getRotationRelativeToNaturalOrientaton(activity, cameraId));
    }

    public static int getRotationRelativeToNaturalOrientaton(Activity activity, int cameraId) {
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

    public static void loadPrefData(Context ctx, AutoCompleteTextView autoComplete, String shared_pref_name) {
      SharedPreferences prefs = ctx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
      int               size  = prefs.getInt("size", 0);		// Getting the number of existing lines

      if (size != 0) { 					                // size is 0 if no lines was saved
        String[] history = new String[size];		// Create array of strings for the lines
        for (int i = 0; i<size; i++) {		      //Save the lines to the array of strings
          history[i] = prefs.getString(String.valueOf(i+1), "empty");
        }

        //Updating the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_dropdown_item_1line, history);
        autoComplete.setAdapter(adapter);
      }
    }

    public static void savePrefData(Context ctx, AutoCompleteTextView autoComplete, String shared_pref_name, String newStr) {
      SharedPreferences prefs   = ctx.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
      int               size    = prefs.getInt("size", 0);
      String[]          history = new String[size+1];		  // Create array with one more place for the new string
      boolean           repeat  = false;

      for (int i=0; i<size; i++) {                // Checking if the new line does not already exist
        history[i] = prefs.getString(String.valueOf(i+1), "empty");
        if (history[i].equals(newStr)) {
          repeat = true;
          break;
        }
      }

      if (!repeat) {
        SharedPreferences.Editor editor = prefs.edit();  // Saving the new string
        editor.putString(String.valueOf(size+1), newStr);
        editor.putInt("size", size+1);
        editor.apply();
        history[size] = newStr; //Updating the adapter online
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_dropdown_item_1line, history);
        autoComplete.setAdapter(adapter);
      }
    }

    public static void showAboutDlg(Context ctx) {
      String versionname = "";
      try {
        versionname = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      Helpers.Do.msgBox((Activity) ctx,
        "InstaClimb " +
          versionname +
          "\n\nThe one and only serious approach to grading.");
    }
  }
}
