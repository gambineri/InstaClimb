package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;


public class ClimbingInfo extends Activity {
  AutoCompleteTextView m_AscNameCtl = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.climbing_info);
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    // Get a reference to the AutoCompleteTextView in the layout
    m_AscNameCtl = (AutoCompleteTextView) findViewById(R.id.ascentname);

    loadPrefData(m_AscNameCtl, "IC_ASCENTNAME_HISTORY");
  }

  public void onNext(View v) {
    View asc = findViewById(R.id.ascentname);
    View loc = findViewById(R.id.location);

    String ascentname = (asc != null ? ((EditText)asc).getText().toString() : "");
    String location   = (loc != null ? ((EditText)loc).getText().toString() : "");

    Intent i = new Intent(ClimbingInfo.this, CameraActivity.class);
    i.putExtra(Helpers.Const.EXTRA_ASCENT_NAME, ascentname);
    i.putExtra(Helpers.Const.EXTRA_LOCATION, location);

    savePrefData(ascentname, m_AscNameCtl, "IC_ASCENTNAME_HISTORY");

    ClimbingInfo.this.startActivity(i);
  }

  private void loadPrefData(AutoCompleteTextView autoComplete, String shared_pref_name) {
    SharedPreferences settings = getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
    int size = settings.getInt("size", 0);		// Getting the number of existing lines
    if (size != 0) { 					                // size is 0 if no lines was saved
      String[] history = new String[size];		// Create array of strings for the lines
      for (int i = 0; i<size; i++) {		      //Save the lines to the array of strings
        history[i] = settings.getString(String.valueOf(i+1), "empty");
      }
      //Updating the adapter
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, history);
      autoComplete.setAdapter(adapter);
//      autoComplete.setText(saved_uri); //loading saved url
    }
  }

  private void savePrefData(String newStr, AutoCompleteTextView autoComplete, String shared_pref_name) {
    SharedPreferences settings = getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
    int size = settings.getInt("size", 0);
    String[] history = new String[size+1];		  // Create array with one more place for the new string
    boolean repeat = false;
    for (int i=0; i<size; i++) {                // Checking if the new line does not already exist
      history[i] = settings.getString(String.valueOf(i+1), "empty");
      if (history[i].equals(newStr)) {
        repeat = true;
        break;
      }
    }
    if (!repeat) {
      SharedPreferences.Editor editor = settings.edit();  // Saving the new string
      editor.putString(String.valueOf(size+1), newStr);
      editor.putInt("size", size+1);
      editor.apply();
      history[size] = newStr; //Updating the adapter online
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, history);
      autoComplete.setAdapter(adapter);
    }
  }
}
