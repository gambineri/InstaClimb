package com.instaclimb.app.activities;

import android.app.Activity;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.astuetz.PagerSlidingTabStrip;
import com.instaclimb.app.Helpers;
import com.instaclimb.app.R;
import com.instaclimb.app.adapters.SectionsPagerAdapter;
import com.instaclimb.app.fragments.CameraActivityFragment;
import com.instaclimb.app.views.CameraPreview;

public class MainActivity extends Activity implements CameraActivityFragment.OnFragmentInteractionListener {

  public CameraPreview m_Preview = null;
  public Camera        m_Camera  = null;

  /**
   * The {@link android.support.v4.view.PagerAdapter} that will provide
   * fragments for each of the sections. We use a
   * {@link FragmentPagerAdapter} derivative, which will keep every
   * loaded fragment in memory. If this becomes too memory intensive, it
   * may be best to switch to a
   * {@link android.support.v13.app.FragmentStatePagerAdapter}.
   */
  SectionsPagerAdapter m_SectionsPagerAdapter;

  /**
   * The {@link ViewPager} that will host the section contents.
   */
  ViewPager m_ViewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    m_SectionsPagerAdapter = new SectionsPagerAdapter(this, getFragmentManager());

    // Set up the ViewPager with the sections adapter.
    m_ViewPager = (ViewPager) findViewById(R.id.pager);
    m_ViewPager.setAdapter(m_SectionsPagerAdapter);

    // Bind the tabs to the ViewPager
    PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
    tabs.setViewPager(m_ViewPager);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

//    //noinspection SimplifiableIfStatement
//    if (id == R.id.action_settings) {
//      return true;
//    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onFragmentInteraction(Uri uri) {

  }



  public void onNext(View v) {
    Helpers.Do.toast(this, "next clicked");
  }
}
