package com.instaclimb.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.instaclimb.app.*;

import java.util.List;
import java.util.ListIterator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraActivityFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraActivityFragment extends Fragment {
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static MainActivity m_Activity = null;
  private static final String ARG_PARAM2 = "param2";

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  private SessionImage m_SessionImg      = null;
  private String            m_AscentName      = "";
  private String            m_Location        = "";
  private int               m_CameraId        = -1;

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

  private OnFragmentInteractionListener mListener;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param2 Parameter 2.
   * @return A new instance of fragment CameraActivityFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static CameraActivityFragment newInstance(String param2) {
    CameraActivityFragment fragment = new CameraActivityFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  public CameraActivityFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    setUpCamera();

    // Inflate the layout for this fragment
    View fragment_layout_view = inflater.inflate(R.layout.fragment_camera_activity, container, false);

    // Create our Preview view and set it as the content of our activity.
    m_Activity.m_Preview = new CameraPreview(m_Activity);
    FrameLayout preview = (FrameLayout) fragment_layout_view.findViewById(R.id.camera_preview);
    preview.addView(m_Activity.m_Preview); //adds SurfaceView on top of everything

    //** Inflate the top camera overlay
    final View topOverlay = inflater.inflate(R.layout.camera_overlay_top, preview, false);
    if (topOverlay != null)
      preview.addView(topOverlay);

    //** Inflate the bottom camera overlay
    View bottomOverlay = inflater.inflate(R.layout.camera_overlay_bottom, preview, false);
    if (bottomOverlay != null)
      preview.addView(bottomOverlay);

    if (preview != null && bottomOverlay != null && topOverlay != null) {

      try {
        //calculate coordinates of capture rect as if (0, 0) is in the top-left corner (portrait mode)
        m_CaptureRect.left = 0;
        m_CaptureRect.top = topOverlay.getHeight()*(m_ImgDimInverted ? m_BestRes.width : m_BestRes.height)/m_Activity.m_Preview.getHeight();
        m_CaptureRect.right = (m_ImgDimInverted ? m_BestRes.height : m_BestRes.width);
        m_CaptureRect.bottom = m_CaptureRect.top +m_CaptureRect.right;
      } catch (Exception e) {
        e.printStackTrace();
      }

      // set height for top and bottom frame
      topOverlay.setBottom(preview.getHeight() - preview.getWidth() - bottomOverlay.getHeight());
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
    releaseCamera();              // release the camera immediately on pause event
  }

  @Override
  public void onResume() {
    super.onResume();

  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    m_Activity = (MainActivity) activity;
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
        + " must implement OnFragmentInteractionListener");
    }
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

//////////////////////////////////////////////////
  private Boolean setUpCamera() {
    if (!checkCameraHardware(m_Activity)) {
      Helpers.Do.msgBox(m_Activity, "Wow, no camera available -00-. Don't be SO lousy, buy yourself a better device...");
      return false;
    }

    if (m_Activity.m_Camera != null)
      releaseCamera();

    if ((m_Activity.m_Camera = getCameraInstance()) != null) {
      try {
        // Because we want a portrait app, calculate rotation respect to natural device orientation
        m_DevRotation = Helpers.Do.getRotationRelativeToNaturalOrientaton(m_Activity, m_CameraId);

        // In case the device natural orientation is not portrait (or multiple thereof)
        // let's save the bool to know if picture dimensions need to be considered as inverted
        // (W in place of H and viceversa)
        m_ImgDimInverted = (m_DevRotation == 90 || m_DevRotation == 270);

  //todo Log calls to be removed with ProGuard when publishing
  //        Log.v(Helpers.Const.DBGTAG, getCurrentCameraInfo());
        Camera.Parameters pars = m_Activity.m_Camera.getParameters();

        pars.setPictureFormat(ImageFormat.JPEG);
        if (!findFirstCameraResolution(1000, 1000))
          findBestCameraResolution();
        pars.setPictureSize(m_BestRes.width, m_BestRes.height);

        m_Activity.m_Camera.setParameters(pars);
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
    if (m_Activity.m_Camera != null) {
      m_Activity.m_Camera.stopPreview();
      m_Activity.m_Camera.release();        // release the camera for other applications
      m_Activity.m_Camera = null;
    }
  }

  private final Camera.PictureCallback m_Picture = new Camera.PictureCallback() {
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
//      new InstaJob().execute(data);
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

    Camera.Parameters pars = m_Activity.m_Camera.getParameters();

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
      ret += "Format: " + m_Activity.m_Camera.getParameters().getPictureFormat() + "; Size: " +
        cs.width + "x" + cs.height + "\n";
    }

    Camera.Size ps = pars.getPreviewSize();
    if (ps != null) {
      ret += "\nCurrent Preview Format and Size:\n";
      ret += "Format: " + m_Activity.m_Camera.getParameters().getPreviewFormat() + "; Size: " +
        ps.width + "x" + ps.height + "\n";
    }

    return ret;
  }

  private void findBestCameraResolution() {
    if (m_Activity.m_Camera == null)
      return;

    m_BestRes = m_Activity.m_Camera.new Size(0, 0);

    List<Camera.Size> sizes;
    ListIterator<Camera.Size> li;
    Camera.Size tmp;

    Camera.Parameters pars = m_Activity.m_Camera.getParameters();

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
    if (m_Activity.m_Camera == null)
      return false;

    m_BestRes = m_Activity.m_Camera.new Size(basew, baseh);
    Camera.Parameters pars = m_Activity.m_Camera.getParameters();
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

}
