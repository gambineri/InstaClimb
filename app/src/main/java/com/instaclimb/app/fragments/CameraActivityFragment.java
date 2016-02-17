package com.instaclimb.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.instaclimb.app.MyCamera;
import com.instaclimb.app.R;
import com.instaclimb.app.SessionImage;
import com.instaclimb.app.activities.MainActivity;
import com.instaclimb.app.views.CameraPreview;


/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraActivityFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraActivityFragment extends Fragment {

  public final MyCamera      m_MyCamera         = MyCamera.getInstance();

  public MainActivity         m_Activity        = null;
  public CameraPreview        m_Preview         = null;
  public SessionImage         m_SessionImg      = null;
  public String               m_AscentName      = "";
  public String               m_Location        = "";

  // Coordinates of crop area (crop rect) with respect to a cartesian system
  // having the origin in the top-left corner of the (portrait) screen
  public final Rect    m_CaptureRect     = new Rect(0, 0, 0, 0);

  // Handler to this thread to visually update the progress bar
  private final Handler m_Handler         = new Handler();

  // Progress bar for insta transformations
  private ProgressBar   m_Progress        = null;

  private OnFragmentInteractionListener mListener;

  /*
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   */
  public static CameraActivityFragment newInstance() {
    CameraActivityFragment fragment = new CameraActivityFragment();
    return fragment;
  }

  public CameraActivityFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      m_MyCamera.setupMyCamera(m_Activity);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View fragment_layout_view = inflater.inflate(R.layout.fragment_camera_activity, container, false);

    // Create our Preview view and set it as the content of our activity.
    m_Preview = new CameraPreview(m_Activity, m_MyCamera, m_MyCamera.getDeviceRotation());
    FrameLayout preview = (FrameLayout) fragment_layout_view.findViewById(R.id.camera_preview);
    preview.addView(m_Preview); //adds SurfaceView on top of everything

    //** Inflate the bottom camera overlay
    View bottomOverlay = inflater.inflate(R.layout.camera_overlay_bottom, preview, false);
    if (bottomOverlay != null)
      preview.addView(bottomOverlay);

    if (preview != null && bottomOverlay != null) {

      try {
        //calculate coordinates of capture rect as if (0, 0) is in the top-left corner (portrait mode)
        m_CaptureRect.left = 0;
        m_CaptureRect.top = 0;
        m_CaptureRect.right = (m_MyCamera.isImgDimInverted() ? m_MyCamera.getBestRes().height : m_MyCamera.getBestRes().width);
        m_CaptureRect.bottom = m_CaptureRect.top + m_CaptureRect.right;

      } catch (Exception e) {
        e.printStackTrace();
      }

      /*
      m_Progress = (ProgressBar) findViewById(R.id.progressBar);
      m_Progress.bringToFront();
      */
    }

    return preview;
  }

  @Override
  public void onPause() {
    super.onPause();
    m_MyCamera.releaseCamera(); // release the camera immediately on pause event
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!m_MyCamera.isOpen())
      m_MyCamera.setupMyCamera(m_Activity);
  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
        + " must implement OnFragmentInteractionListener");
    }
    m_Activity = (MainActivity) activity;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  private Bitmap rotBMP(Bitmap srcBmp) {
    Matrix matrix = new Matrix();
    matrix.setRotate(m_MyCamera.getDeviceRotation());
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


}
