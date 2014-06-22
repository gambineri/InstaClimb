package com.imdp.instaclimb;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ShowCapture extends Activity {

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
          LinearLayout rootView = (LinearLayout)findViewById(R.id.sc_root_frame);
          View tf               = findViewById(R.id.sc_top_frame);
          ImageView iv          = (ImageView)findViewById(R.id.sc_img_view);
          View bf               = findViewById(R.id.sc_bottom_frame);

          if (rootView != null && bf != null && tf != null && iv != null) {
            Bundle bundle = getIntent().getExtras();
            int topFrameW  = bundle.getInt(Helpers.Const.EXTRA_TOP_FRAME_W);
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
            Bitmap bmp = BitmapFactory.decodeFile(bundle.getString(Helpers.Const.EXTRA_CAPTURED_IMG_PATH));
            iv.setImageBitmap(bmp);
          }
        }
      });
    }
  }
}
