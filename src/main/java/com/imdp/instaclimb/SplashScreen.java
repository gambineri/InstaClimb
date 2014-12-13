package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by massimo on 11/18/14.
 */
public class SplashScreen extends Activity {

  private static Activity m_SplashScreenActivity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    m_SplashScreenActivity = this;
    setContentView(R.layout.splash_screen);

    Handler handler = new Handler();
    handler.postDelayed(new Runnable(){
      @Override
      public void run() {
        Intent intent = new Intent(SplashScreen.this, CameraActivity.class);
        startActivity(intent);
//        SplashScreen.this.finish();
      }
    }, 3000); //assuming you want for the splashscreen to be displayed for 3 seconds.
  }
}
