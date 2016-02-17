package com.instaclimb.app;

/**
 * Created by massimo on 17/02/16.
 */

import android.content.SharedPreferences;
import android.graphics.*;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import com.instaclimb.app.fragments.CameraActivityFragment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * The InstaJob class
 * */
public class InstaJob extends AsyncTask<byte[], Void, Void> {

  private CameraActivityFragment m_CAF = null;
  private int m_ProgressStatus = 0;

  public InstaJob(CameraActivityFragment caf) {
    m_CAF = caf;
  }

  private void updateProgressBar(int ps_val) {
    // Update the progress bar

    m_ProgressStatus = ps_val;
//    m_Handler.post(new Runnable() {
//      public void run() {
//        m_Progress.setProgress(m_ProgressStatus);
//      }
//    });

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

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(m_CAF.m_Activity);
    boolean serious_mode = sp.getBoolean("serious_mode", false);

    p.setColor(Color.WHITE);
    Typeface feelsLikeFont = Typeface.createFromAsset(m_CAF.m_Activity.getAssets(), "Khand-Light.ttf");//"ArchitectsDaughter.ttf");
    p.setTypeface(feelsLikeFont);
    p.setTextSize(70);
    p.setShadowLayer(5f, 5f, 5f, Color.BLACK);

    if (m_CAF.m_AscentName.length() > 0 || !serious_mode) {
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
    String spotname = Helpers.Do.toCamelCase(m_CAF.m_Location, " ", null);
    p.setTextSize(bestFontSizePerWidth(spotname, ss-marginTextL*2, 130, p));
    canvas.drawText(spotname, marginTextL, marginTextT*(float)3.5, p);

    // Ascent name and Insta grade...
    p.setShadowLayer(2f, 2f, 2f, Color.BLACK);
    String instaGrade = Helpers.Do.toCamelCase(m_CAF.m_AscentName, " ", null) +
      (serious_mode ? "" : "  " + generateGrade());
    p.setTextSize(bestFontSizePerWidth(instaGrade, ss-marginTextL*2, 180, p));
    canvas.drawText(instaGrade, marginTextL, ss-marginBox-grayRectH/2, p);

    //top-right app-name logo
    canvas.save();
    Typeface instaFont = Typeface.createFromAsset(m_CAF.m_Activity.getAssets(), "Orbitron-Regular.ttf");
    p.setTypeface(instaFont);
    p.setTextSize(50);
    canvas.drawText(m_CAF.m_Activity.getResources().getString(R.string.logo_string), ss-35*marginBox, marginTextT, p);
    canvas.restore();
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
//    m_Progress.setVisibility(View.VISIBLE);
  }

  @Override
  protected Void doInBackground(byte[]... data) {
    try {
      updateProgressBar(10);
      Bitmap bitmap = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
      updateProgressBar(20);
      Bitmap bmpRotated = Helpers.Do.rotBMP(bitmap, m_CAF.m_MyCamera.getDeviceRotation());
      bitmap.recycle();
      updateProgressBar(30);
      Bitmap croppedImage = Helpers.Do.cropImage(bmpRotated, m_CAF.m_CaptureRect);
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
      FileOutputStream fos = new FileOutputStream(m_CAF.m_SessionImg.getCroppedImageFilePathName());
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

//    // Show results
//    Intent i = new Intent(CameraActivity.this, ShowCapture.class);
//    i.putExtra(Helpers.Const.EXTRA_CAPTURED_IMG_PATH, m_SessionImg.getCroppedImageFilePathName());
//    View tf = findViewById(R.id.top_frame);
//    i.putExtra(Helpers.Const.EXTRA_TOP_FRAME_W, tf.getHeight());
//    CameraActivity.this.startActivity(i);

//    m_Progress.setVisibility(View.INVISIBLE);
    m_ProgressStatus = 0;
  }

} // *** class InstaJob
