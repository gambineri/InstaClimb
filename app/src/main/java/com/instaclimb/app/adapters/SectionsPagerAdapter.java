package com.instaclimb.app.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.instaclimb.app.R;
import com.instaclimb.app.fragments.CameraActivityFragment;
import com.instaclimb.app.fragments.ClimbingInfoFragment;

import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

  Context m_Context = null;
  public Fragment m_Fragments[] = new Fragment[3];



  public SectionsPagerAdapter(Context ctx, FragmentManager fm) {
    super(fm);
    m_Context = ctx;
  }

  @Override
  public Fragment getItem(int position) {
    // getItem is called to instantiate the fragment for the given page.
    // Return a PlaceholderFragment (defined as a static inner class below).

    Fragment ret_frag = null;
    switch (position) {
      case 0:
        ret_frag = ClimbingInfoFragment.newInstance();
        break;

      case 1:
        ret_frag = CameraActivityFragment.newInstance();
        break;

      case 2:
        ret_frag = PlaceholderFragment.newInstance(position + 1);
        break;
    }

    return ret_frag;
  }

  @Override
  public int getCount() {
    return 3; // Show 3 total pages
  }

  @Override
  public CharSequence getPageTitle(int position) {
    Locale l = Locale.getDefault();
    switch (position) {
      case 0:
        return m_Context.getString(R.string.title_section1).toUpperCase(l);
      case 1:
        return m_Context.getString(R.string.title_section2).toUpperCase(l);
      case 2:
        return m_Context.getString(R.string.title_section3).toUpperCase(l);
    }
    return null;
  }

  // Here we can finally safely save a reference to the created
  // Fragment, no matter where it came from (either getItem() or
  // FragmentManger). Simply save the returned Fragment from
  // super.instantiateItem() into an appropriate reference depending
  // on the ViewPager position.
  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
    // save the appropriate reference depending on position
    switch (position) {
      case 0:
        m_Fragments[0] = createdFragment;
        break;
      case 1:
        m_Fragments[1] = createdFragment;
        break;
      case 2:
        m_Fragments[2] = createdFragment;
        break;
    }
    return createdFragment;
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_main, container, false);
    }
  }

}
