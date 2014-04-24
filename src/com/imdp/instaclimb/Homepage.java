package com.imdp.instaclimb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Homepage extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homepage);

  	//Add a listener to the Capture button
  	Button goShootButton = (Button) findViewById(R.id.go_shoot);
  	goShootButton.setOnClickListener(
  	  new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					 Intent intent = new Intent(Homepage.this, CameraActivity.class);
					 Bundle b = new Bundle();
					 b.putString("AscentName", ((EditText)findViewById(R.id.EditAscentName)).getText().toString());
					 b.putString("Location", ((EditText)findViewById(R.id.EditLocation)).getText().toString());
					 intent.putExtras(b);
					 startActivityForResult(intent, 0);
				}
  	  }
  	);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
