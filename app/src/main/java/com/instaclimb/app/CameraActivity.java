package com.instaclimb.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import static com.instaclimb.app.Helpers.Do.showAboutDlg;

public class CameraActivity extends Activity {

  private Camera            m_Camera          = null;
  private int               m_CameraId        = -1;
  private CameraPreview     m_Preview         = null;
  private SessionImage      m_SessionImg      = null;
  private String            m_AscentName      = "";
  private String            m_Location        = "";

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
  private final Rect    m_CaptureRect     = new Rect(0, 0, 0, 0);

  // Handler to this thread to visually update the progress bar
  private final Handler m_Handler         = new Handler();

  // Progress bar for insta transformations
  private ProgressBar   m_Progress        = null;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.show_settings:
        Intent i = new Intent(CameraActivity.this, SettingsActivity.class);
        CameraActivity.this.startActivity(i);
      return true;

      case R.id.show_about:
        showAboutDlg(CameraActivity.this);
      return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseCamera();              // release the camera immediately on pause event
  }

  @Override
  protected void onResume() {
    super.onResume();
    (new CameraSetup()).execute();
  }

  private class CameraSetup extends AsyncTask<Void, Void, Boolean> {
    @Override
    protected Boolean doInBackground(Void...p) {
      return setUpCamera();
    }

    @Override
    protected void onPostExecute(Boolean doInBgRetVal) {

      if (!doInBgRetVal)
        return;

//      getWindow().setBackgroundDrawable(null);
      setContentView(R.layout.camera_activity);

      // Create our Preview view and set it as the content of our activity.
      m_Preview = new CameraPreview(CameraActivity.this);
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

      ViewTreeObserver vto = preview.getViewTreeObserver();
      if (vto != null) {
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {

            //At this point the layout is complete and the dimensions of myView and any child views are known.
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            View        bf      = findViewById(R.id.bottom_frame);
            View        tf      = findViewById(R.id.top_frame);

            if (preview != null && bf != null && tf != null) {
              //calculate coordinates of capture rect as if (0, 0) is in the top-left corner (portrait mode)
              m_CaptureRect.left = 0;
              m_CaptureRect.top = tf.getHeight()*(m_ImgDimInverted ? m_BestRes.width : m_BestRes.height)/m_Preview.getHeight();
              m_CaptureRect.right = (m_ImgDimInverted ? m_BestRes.height : m_BestRes.width);
              m_CaptureRect.bottom = m_CaptureRect.top +m_CaptureRect.right;

              // set height for top and bottom frame
              tf.setBottom(preview.getHeight() - preview.getWidth() - bf.getHeight());

              m_Progress = (ProgressBar) findViewById(R.id.progressBar);
              m_Progress.bringToFront();
            }
          }
        });
      }
    }
  }

  public void onCapture(View v) {
    // get an image from the camera
    m_Camera.takePicture(null, null, m_Picture);
  }

  public void onRefresh(View v) {
    startActivity(new Intent(CameraActivity.this, ClimbingInfo.class));
  }

  public void onSettings(View v) {
    CameraActivity.this.openOptionsMenu();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      m_AscentName = extras.getString(Helpers.Const.EXTRA_ASCENT_NAME);
      m_Location   = extras.getString(Helpers.Const.EXTRA_LOCATION);
    }
  }

  private Boolean setUpCamera() {
    if (!checkCameraHardware(this)) {
      Helpers.Do.msgBox(this, "Wow, no camera available -00-. Don't be SO lousy, buy yourself a better device...");
      return false;
    }

    if (m_Camera != null)
      releaseCamera();

    if ((m_Camera = getCameraInstance()) != null) {
      try {
        // Because we want a portrait app, calculate rotation respect to natural device orientation
        m_DevRotation = Helpers.Do.getRotationRelativeToNaturalOrientaton(this, m_CameraId);

        // In case the device natural orientation is not portrait (or multiple thereof)
        // let's save the bool to know if picture dimensions need to be considered as inverted
        // (W in place of H and viceversa)
        m_ImgDimInverted = (m_DevRotation == 90 || m_DevRotation == 270);

//todo Log calls to be removed with ProGuard when publishing
//        Log.v(Helpers.Const.DBGTAG, getCurrentCameraInfo());
        Parameters pars = m_Camera.getParameters();

        pars.setPictureFormat(ImageFormat.JPEG);
        if (!findFirstCameraResolution(1000, 1000))
          findBestCameraResolution();
        pars.setPictureSize(m_BestRes.width, m_BestRes.height);

        m_Camera.setParameters(pars);
      } catch (RuntimeException re) {
        Log.e(Helpers.Const.DBGTAG, re.getMessage());
        return false;
      }

      //Create image wrapper obj for this session
      m_SessionImg = new SessionImage(getResources().getString(R.string.app_name));
    }

    return true;
  }

  private void releaseCamera() {
    if (m_Camera != null) {
      m_Camera.stopPreview();
      m_Camera.release();        // release the camera for other applications
      m_Camera = null;
    }
  }

  private final PictureCallback m_Picture = new PictureCallback() {
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

  private Bitmap cropImage(Bitmap srcBitmap, Rect srcRect) {

    int srcW = srcRect.width();
    int srcH = srcRect.height();

    Bitmap croppedImage = null;

    // If the output is required to a specific size, create an new image
    // with the cropped image in the center and the extra space filled.
    if (srcW != 0 && srcH != 0) {

      // Don't scale the image but instead fill it so it's the required dimension
      croppedImage = Bitmap.createBitmap(srcW, srcH, Bitmap.Config.RGB_565);
      Canvas canvas = new Canvas(croppedImage);

      Rect dstRect = new Rect(0, 0, srcW, srcH);

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

  private boolean findFirstCameraResolution(int basew, int baseh) {
    if (m_Camera == null)
      return false;

    m_BestRes = m_Camera.new Size(basew, baseh);
    Parameters pars = m_Camera.getParameters();
    List<Camera.Size> sizes;
    ListIterator<Camera.Size> li;
    Camera.Size tmp;

    if ((sizes = pars.getSupportedPictureSizes()) != null) {
      li = sizes.listIterator();
      while (li.hasNext()) {
        tmp = li.next();
        if (tmp.width >= m_BestRes.width && tmp.height >= m_BestRes.height) {
          m_BestRes = tmp;
          return true;
        }
      }
    }

    return false;
  }

  /**
   * The Camera Preview class
   * */
  public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private   SurfaceHolder m_Holder = null;
    Activity                m_Activity = null;

    public CameraPreview(Context context) {
      super(context);

      m_Activity = (Activity)context;

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

  /**
   * The InstaJob class
   * */
  private class InstaJob extends AsyncTask<byte[], Void, Void> {

    private int m_ProgressStatus = 0;

    private void updateProgressBar(int ps_val) {
      // Update the progress bar

      m_ProgressStatus = ps_val;
      m_Handler.post(new Runnable() {
        public void run() {
          m_Progress.setProgress(m_ProgressStatus);
        }
      });

    }

    private String generateGrade() {
      String gradeNum[] = {"5", "6", "7", "8", "9"};
      String gradeAbc[] = {"a", "b", "c"};
      String gradePlus[] = {"", "+"};

      Random r = new Random();
      return gradeNum[r.nextInt(5)] + gradeAbc[r.nextInt(3)] + gradePlus[r.nextInt(2)];
    }

    private int bestFontSizePerWidth(String txt, int maxwidth,  int startsize, Paint p) {
      p.setTextSize(startsize);

      int curwidth = (int)p.measureText(txt);
      int cursize = startsize;

      while (curwidth > maxwidth) {
        cursize--;
        p.setTextSize(cursize);
        curwidth = (int)p.measureText(txt);
      }

      return cursize;
    }

    private void drawInstaClimbInfo(Canvas canvas, int ss) {
      int marginBox     = ss /90;
      int marginTextL   = 3*marginBox;
      int marginTextT   = 5*marginBox;
//      int grayRectW   = ss - 2*marginBox;
      int grayRectH     = ss/4;
      int grayRectVPad  = grayRectH/10;
      Paint p           = new Paint(Paint.ANTI_ALIAS_FLAG);
      RectF grayRect    = new RectF(marginBox, ss-marginBox-grayRectH, ss-marginBox, ss-marginBox);

      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CameraActivity.this);
      boolean serious_mode = sp.getBoolean("serious_mode", false);

      p.setColor(Color.WHITE);
      Typeface feelsLikeFont = Typeface.createFromAsset(getAssets(), "Khand-Light.ttf");//"ArchitectsDaughter.ttf");
      p.setTypeface(feelsLikeFont);
      p.setTextSize(70);
      p.setShadowLayer(5f, 5f, 5f, Color.BLACK);

      if (m_AscentName.length() > 0 || !serious_mode) {
        // Gray rectangle with ascent and feelings
        Paint p1 = new Paint(p);
        p1.setColor(Color.GRAY);
        p1.setAlpha(100);
        canvas.drawRoundRect(grayRect, 10, 10, p1);
      }

      if (!serious_mode) {
        // Feels like...
        String feelsLike = "Feels like: " + generateGrade();
        int feelsLikeLen = (int) p.measureText(feelsLike);
        canvas.drawText(feelsLike, grayRect.right - feelsLikeLen - feelsLikeLen / 10, ss - marginBox - grayRectVPad, p);
      }

      // Date
      Time now = new Time();
      now.setToNow();
      String datestr = now.format("%d/%m/%Y - %H:%M");
      canvas.drawText(datestr, marginTextL, marginTextT, p);

      // Spot name
      String spotname = Helpers.Do.toCamelCase(m_Location, " ", null);
      p.setTextSize(bestFontSizePerWidth(spotname, ss-marginTextL*2, 130, p));
      canvas.drawText(spotname, marginTextL, marginTextT*(float)3.5, p);

      // Ascent name and Insta grade...
      p.setShadowLayer(2f, 2f, 2f, Color.BLACK);
      String instaGrade = Helpers.Do.toCamelCase(m_AscentName, " ", null) +
        (serious_mode ? "" : "  " + generateGrade());
      p.setTextSize(bestFontSizePerWidth(instaGrade, ss-marginTextL*2, 180, p));
      canvas.drawText(instaGrade, marginTextL, ss-marginBox-grayRectH/2, p);

      //top-right app-name logo
      canvas.save();
      Typeface instaFont = Typeface.createFromAsset(getAssets(), "Orbitron-Regular.ttf");
      p.setTypeface(instaFont);
      p.setTextSize(50);
      canvas.drawText(getResources().getString(R.string.logo_string), ss-35*marginBox, marginTextT, p);
      canvas.restore();
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      m_Progress.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(byte[]... data) {
      try {
        updateProgressBar(10);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
        updateProgressBar(20);
        Bitmap bmpRotated = rotBMP(bitmap);
        bitmap.recycle();
        updateProgressBar(30);
        Bitmap croppedImage = cropImage(bmpRotated, m_CaptureRect);
        bmpRotated.recycle();
        updateProgressBar(40);
        int ssRealImage = croppedImage.getWidth();
        Bitmap cs = Bitmap.createBitmap(ssRealImage, ssRealImage, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);
        updateProgressBar(50);

        // Draw picture shot layer image
        comboImage.drawBitmap(croppedImage, 0f, 0f, null);
        updateProgressBar(60);
        drawInstaClimbInfo(comboImage, ssRealImage);
        updateProgressBar(70);

        // Garbage collect
        croppedImage.recycle();
        updateProgressBar(80);

        //Save on disk
        FileOutputStream fos = new FileOutputStream(m_SessionImg.getCroppedImageFilePathName());
        cs.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        updateProgressBar(90);
        cs.recycle();
        fos.close();
        updateProgressBar(100);
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

      m_Progress.setVisibility(View.INVISIBLE);
      m_ProgressStatus = 0;
    }

  } // *** class InstaJob

} // *** class CameraActivity
