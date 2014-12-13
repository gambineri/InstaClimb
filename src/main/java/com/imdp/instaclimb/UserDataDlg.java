package com.imdp.instaclimb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by massimo on 10/6/14.
 */
public class UserDataDlg extends DialogFragment {

  private String m_AscentName = "";
  private String m_Location   = "";

  public String getAscentName() { return m_AscentName; }
  public String getLocation()   { return m_Location;   }

  /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
  public interface UserDataDlgListener {
    public void onDialogPositiveClick(DialogFragment dialog);
  }

  // Use this instance of the interface to deliver action events
  private UserDataDlgListener mListener;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      mListener = (UserDataDlgListener) activity;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the Builder class for convenient dialog construction
    final Activity caller = getActivity();
    if (caller == null)
      return null;

    AlertDialog.Builder builder = new AlertDialog.Builder(caller);
    LayoutInflater inflater = caller.getLayoutInflater();

    final View dlgLayout = inflater.inflate(R.layout.dlg_user_data, null);
    builder.setView(dlgLayout)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {

            m_AscentName = ((EditText) (dlgLayout.findViewById(R.id.ascent_name))).getText().toString();
            m_Location = ((EditText) (dlgLayout.findViewById(R.id.location))).getText().toString();

// this is to set focus on startup - not used at moment
//            dlgLayout.findViewById(R.id.ascent_name).requestFocus();

            // Send the positive button event back to the host activity
            mListener.onDialogPositiveClick(UserDataDlg.this);
          }
        })
        .setNegativeButton("Skip", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}