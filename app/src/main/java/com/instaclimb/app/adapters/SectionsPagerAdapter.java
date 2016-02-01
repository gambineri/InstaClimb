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
        ret_frag = new ClimbingInfoFragment();
        break;

      case 1:
        ret_frag = CameraActivityFragment.newInstance("");
        //        ret_frag = PlaceholderFragment.newInstance(position + 1);
        break;

      case 2:
        ret_frag = PlaceholderFragment.newInstance(position + 1);
        break;
    }

    return ret_frag;
  }

  @Override
  public int getCount() {
    // Show 3 total pages.
    return 3;
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
