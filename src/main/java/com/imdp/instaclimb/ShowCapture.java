package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

public class ShowCapture extends Activity {

  // The file path name of the insta-enriched image to be shared
  private String m_ImgFilePathName = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.show_capture);

    LinearLayout rootView = (LinearLayout)findViewById(R.id.sc_root_frame);

    ViewTreeObserver vto = rootView.getViewTreeObserver();
    if (vto != null) {
      vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
          LinearLayout  rootView  = (LinearLayout)findViewById(R.id.sc_root_frame);
          View          tf        = findViewById(R.id.sc_top_frame);
          ImageView     iv        = (ImageView)findViewById(R.id.sc_img_view);
          View          bf        = findViewById(R.id.sc_bottom_frame);
          int           topFrameW = 50;

          if (rootView != null && bf != null && tf != null && iv != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null)
              topFrameW  = bundle.getInt(Helpers.Const.EXTRA_TOP_FRAME_W);
            else
              Log.w(Helpers.Const.DBGTAG, "The Bundle in ShowCapture is null. Top frame is cluelessly assigned height 50.");

            int squareside = rootView.getWidth();

            tf.setTop(0);
            tf.setBottom(topFrameW);
            iv.setTop(topFrameW);
            iv.setBottom(topFrameW + squareside);
            bf.setTop(topFrameW + squareside);
            bf.setBottom(rootView.getHeight());

//            Log.v(Helpers.Const.DBGTAG, String.format("rootView.getWidth = %d tf.getHeight() = %d",
//                rootView.getWidth(), tf.getHeight()));
//            Log.v(Helpers.Const.DBGTAG, String.format("%d %d %d %d",
//                bf.getLeft(), bf.getTop(), bf.getRight(), bf.getBottom()));

            if (bundle != null) {
              m_ImgFilePathName = bundle.getString(Helpers.Const.EXTRA_CAPTURED_IMG_PATH);
              Bitmap bmp = BitmapFactory.decodeFile(m_ImgFilePathName);
              iv.setImageBitmap(bmp);
            } else {
              Helpers.Do.msgBox(ShowCapture.this, "Ooops. No image found.");
            }
          }
        }
      });
    }

    //Add a listener to the Share button
    Button shareButton = (Button) findViewById(R.id.button_share);
    shareButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Generic sharing support through app chooser
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(m_ImgFilePathName)));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent openInChooser = new Intent(intent);
        startActivity(openInChooser);
      }
    });

    //Add a listener to the Settings button
    Button settingsButton = (Button) findViewById(R.id.button_settings);
    settingsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Helpers.Do.msgBox(ShowCapture.this, "Nel dubbio sgrada.");
      }
    });
  }

  public void onRefresh(View v) {
    startActivity(new Intent(ShowCapture.this, ClimbingInfo.class));
  }
}
