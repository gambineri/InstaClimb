package com.imdp.instaclimb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

public class CameraActivity extends Activity {

  private Camera        m_Camera          = null;
  private int           m_CameraId        = -1;
  private CameraPreview m_Preview         = null;
  private SessionImage  m_SessionImg      = null;
  ClimbInfoView         m_ClimbInfoView   = null;
  private boolean       m_AscNameTouched  = false;

  // Best resolution for the camera hardware on the current device
  private Camera.Size   m_BestRes         = null;

  // Degrees of rotation if the portrait mode is rotated (90 or 270)
  // with respect to the natural orientation of the device
  private int           m_DevRotation     = 0;

  // True if the portrait mode is rotated (90 or 270) with respect
  // to the natural orientation of the device
  private boolean       m_ImgDimInverted  = false;

  // Coordinates of crop area (crop rect) with respect to a cartesian system
  // having the origin in the top-left corner of the (portrait) screen
  private Rect          m_CaptureRect     = new Rect(0, 0, 0, 0);

  private void setUpCamera() {
    if (!checkCameraHardware(this)) {
      Helpers.Do.msgBox(this, "Wow, no camera available -00-. Don't be SO lousy, buy yourself a better device...");
      return;
    }

    if (m_Camera != null)
      releaseCamera();

    if ((m_Camera = getCameraInstance()) != null) {
      try {
        // Because we want a portrait app, calculate rotation respect to natural device orientation
        m_DevRotation = Helpers.Do.getRotationRelativeToNaturalOrientaton(this, m_CameraId, m_Camera);

        // In case the device natural orientation is not portrait (or multiple thereof)
        // let's save the bool to know if picture dimensions need to be considered as inverted
        // (W in place of H and viceversa)
        m_ImgDimInverted = (m_DevRotation == 90 || m_DevRotation == 270);

//todo Log calls to be removed with ProGuard when publishing
        Log.v(Helpers.Const.DBGTAG, getCurrentCameraInfo());
        Parameters pars = m_Camera.getParameters();

        pars.setPictureFormat(ImageFormat.JPEG);
        findBestCameraResolution();
        pars.setPictureSize(m_BestRes.width, m_BestRes.height);
//        pars.setPreviewSize(1280, 720);

        m_Camera.setParameters(pars);
      } catch (RuntimeException re) {
        Log.e(Helpers.Const.DBGTAG, re.getMessage());
      }

      //Create image wrapper obj for this session
      m_SessionImg = new SessionImage(getResources().getString(R.string.app_name));
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.camera_activity);

    // Create our Preview view and set it as the content of our activity.
    m_Preview = new CameraPreview(this);
    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(m_Preview); //adds SurfaceView on top of everything

    /* Inflate the top and bottom camera overlays:
       it must be done here and cannot be merged into the main camera_activity.xml because of
       previous line of code, **preview.addView(m_Preview);** which would add the SurfaceView
       (the camera preview container) on top of everything, thus hiding the overlays. */

    //** Inflate the top camera overlay
    final View topOverlay = getLayoutInflater().inflate(R.layout.camera_overlay_top, preview, false);
    if (topOverlay != null)
      preview.addView(topOverlay);

    //** Inflate the bottom camera overlay
    View bottomOverlay = getLayoutInflater().inflate(R.layout.camera_overlay_bottom, preview, false);
    if (bottomOverlay != null)
      preview.addView(bottomOverlay);

    TextView ascentname =(TextView)findViewById(R.id.EditAscentName);
    ascentname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {

          if (m_ClimbInfoView != null) {
            m_ClimbInfoView.setAscentName((String) v.getText());
            m_ClimbInfoView.postInvalidate();
          }
          return true;
        }
        return false;
      }
    });

    ascentname.setOnTouchListener(new TextView.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (!m_AscNameTouched) {
          m_AscNameTouched = true;
          ((TextView)view).setText("");
        }
        return false;
      }
    });

    m_ClimbInfoView = new ClimbInfoView(this);
    m_ClimbInfoView.setDrawingCacheEnabled(true);
    m_ClimbInfoView.setVisibility(View.INVISIBLE);
    m_ClimbInfoView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    m_ClimbInfoView.layout(0, 0, m_ClimbInfoView.getMeasuredWidth(), m_ClimbInfoView.getMeasuredHeight());

    ViewTreeObserver vto = preview.getViewTreeObserver();
    if (vto != null) {
      vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
        //At this point the layout is complete and the dimensions of myView and any child views are known.
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        View bf = findViewById(R.id.bottom_frame);
        View tf = findViewById(R.id.top_frame);
        if (preview != null && bf != null && tf != null) {
          bf.setTop(preview.getWidth() + tf.getHeight());

          //calculate coordinates of capture rect as if (0, 0) is in the top-left corner (portrait mode)
          m_CaptureRect.left = 0;
          m_CaptureRect.top = tf.getHeight()*(m_ImgDimInverted ? m_BestRes.width : m_BestRes.height)/m_Preview.getHeight();
          m_CaptureRect.right = (m_ImgDimInverted ? m_BestRes.height : m_BestRes.width);
          m_CaptureRect.bottom = m_CaptureRect.top +m_CaptureRect.right;

          m_ClimbInfoView.setLeft(0);
          m_ClimbInfoView.setRight(preview.getWidth());
          m_ClimbInfoView.setTop(tf.getHeight());
          m_ClimbInfoView.setBottom(preview.getWidth() + tf.getHeight());

          m_ClimbInfoView.setSquareSide(preview.getWidth());

          preview.removeView(m_ClimbInfoView);
          preview.addView(m_ClimbInfoView);
        }
        }
      });
    }

    //Add a listener to the Capture button
    Button captureButton = (Button) findViewById(R.id.button_capture);
    captureButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // get an image from the camera
        m_Camera.takePicture(null, null, m_Picture);
      }
    });
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

      new InstaJob().execute(data);
    }
  };

  private Bitmap rotBMP(Bitmap srcBmp) {
    Matrix matrix = new Matrix();
    matrix.setRotate(m_DevRotation);
    return Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(), srcBmp.getHeight(), matrix, false);
  }

  private Bitmap cropImage(Bitmap srcBitmap, Rect srcRect, boolean scaleUpIfNeeded) {

    int srcW = srcRect.width();
    int srcH = srcRect.height();

    Bitmap croppedImage = null;

    // If the output is required to a specific size, create an new image
    // with the cropped image in the center and the extra space filled.
    if (srcW != 0 && srcH != 0 && scaleUpIfNeeded) {

      // Don't scale the image but instead fill it so it's the required dimension
      croppedImage = Bitmap.createBitmap(srcW, srcH, Bitmap.Config.RGB_565);
      Canvas canvas = new Canvas(croppedImage);

      Rect dstRect = new Rect(0, 0,srcW, srcH);

      // Draw the cropped bitmap in the center
      canvas.drawBitmap(srcBitmap, srcRect, dstRect, null);

      // Release bitmap memory as soon as possible
      srcBitmap.recycle();
    }

    return croppedImage;
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
  private Camera getCameraInstance() {
    Camera c = null;
    try {
      //Find backward camera
      m_CameraId = 0;
      boolean found = false;
      while(m_CameraId<Camera.getNumberOfCameras() && !found) {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        Camera.getCameraInfo(m_CameraId, ci);
        if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
          found = true;
        else
          m_CameraId++;
      }

      c = Camera.open(m_CameraId); // attempt to get a Camera instance
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

  private void findBestCameraResolution() {
    if (m_Camera == null)
      return;

    m_BestRes = m_Camera.new Size(0, 0);

    List<Camera.Size> sizes;
    ListIterator<Camera.Size> li;
    Camera.Size tmp;

    Parameters pars = m_Camera.getParameters();

    if ((sizes = pars.getSupportedPictureSizes()) != null) {
      li = sizes.listIterator();
      while (li.hasNext()) {
        tmp = li.next();
        if (tmp.width > m_BestRes.width || tmp.height > m_BestRes.height)
          m_BestRes = tmp;
      }
    }

  }

  /**
   * The Camera Preview class
   * */
  public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private   SurfaceHolder m_Holder = null;
    protected Activity      m_Activity = null;

    public CameraPreview(Context context) {
      super(context);

      m_Activity = (Activity)context;

      // Install a SurfaceHolder.Callback so we get notified when the
      // underlying surface is created and destroyed.
      m_Holder = getHolder();
      if (m_Holder != null) {
        m_Holder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        m_Holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      } else throw new AssertionError("m_Holder is null in CameraPreview(Context context, Camera camera)");
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

  /**
   * The InstaJob class
   * */
  private class InstaJob extends AsyncTask<byte[], String, Void> {

    ProgressDialog m_ProgressDlg = null;

    private void drawInstaClimbInfo(Canvas canvas, int ss) {
      Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

      // ---***--- Feels Like
      RectF feelsLikeRect = new RectF(0, ss-350, ss, ss);
      p.setColor(Color.GRAY);
      p.setAlpha(128);
      canvas.drawRoundRect(feelsLikeRect, 10, 10, p);

      Typeface feelsLikeFont = Typeface.createFromAsset(getAssets(), "Khand-Light.ttf");//"ArchitectsDaughter.ttf");
      p.setTypeface(feelsLikeFont);
      p.setColor(Color.WHITE);
      p.setTextSize(70);
      p.setShadowLayer(5f, 5f, 5f, Color.BLACK);

      String feelsLike = "Feels like: 7b+";
      int feelsLikeLen = (int)p.measureText(feelsLike);
      canvas.drawText(feelsLike, feelsLikeRect.right - feelsLikeLen - feelsLikeLen/10, ss-70, p);

      // Date
      Typeface instafont = Typeface.createFromAsset(getAssets(), "Khand-Light.ttf");
//      p.setTypeface(instafont);
      p.setColor(Color.WHITE);
      p.setTextSize(60);
      p.setShadowLayer(5f, 5f, 5f, Color.BLACK);

      Time now = new Time();
      now.setToNow();
      String datestr = now.format("%d.%m.%Y - %H:%M");
      canvas.drawText(datestr, 50, 100, p);

      // Spot
      String spotname = "Secret Spot";
      int spotnameLen = (int)p.measureText(feelsLike);
      p.setTextSize(80);
      canvas.drawText(spotname, 50, 200, p);

      // Ascent name and Insta grade...
      String instaGrade = "ZMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMZ 7c+";
      p.setColor(Color.WHITE);
      p.setTextSize(110);
      p.setShadowLayer(2f, 2f, 2f, Color.BLACK);
      canvas.drawText(instaGrade, 50, ss-200, p);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      m_ProgressDlg = new ProgressDialog(CameraActivity.this);
      m_ProgressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      m_ProgressDlg.setCancelable(true);
      m_ProgressDlg.setTitle("InstaClimbing right now...");
      m_ProgressDlg.show();

    }

    @Override
    protected Void doInBackground(byte[]... data) {
      try {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
        Bitmap bmpRotated = rotBMP(bitmap);
        bitmap.recycle();
        Bitmap croppedImage = cropImage(bmpRotated, m_CaptureRect, true);
        bmpRotated.recycle();

//        publishProgress("Calculating...");
//        m_ProgressDlg.setMessage("Rotating /  Cropping...");

        int ssRealImage = croppedImage.getWidth();
        Bitmap cs = Bitmap.createBitmap(ssRealImage, ssRealImage, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);

//        publishProgress("Drawing snapshot...");

        // Draw picture shot layer image
        comboImage.drawBitmap(croppedImage, 0f, 0f, null);
        drawInstaClimbInfo(comboImage, ssRealImage);

//        publishProgress("Grading...");

        // Garbage collect
        croppedImage.recycle();

        //Save on disk
        FileOutputStream fos = new FileOutputStream(m_SessionImg.getCroppedImageFilePathName());
        cs.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        cs.recycle();
        fos.close();

//        publishProgress("Done.");
      }
      catch (FileNotFoundException e) {
        Log.d(Helpers.Const.DBGTAG, "File not found: " + e.getMessage());
      } catch (IOException e) {
        Log.d(Helpers.Const.DBGTAG, "Error accessing file: " + e.getMessage());
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);

      // Show results
      Intent i = new Intent(CameraActivity.this, ShowCapture.class);
      i.putExtra(Helpers.Const.EXTRA_CAPTURED_IMG_PATH, m_SessionImg.getCroppedImageFilePathName());
      View tf = findViewById(R.id.top_frame);
      i.putExtra(Helpers.Const.EXTRA_TOP_FRAME_W, tf.getHeight());
      CameraActivity.this.startActivity(i);

      m_ProgressDlg.dismiss();
    }

    @Override
    protected void onProgressUpdate(String... values) {
      super.onProgressUpdate(values);

    }
  } // *** class InstaJob

} // *** class CameraActivity
