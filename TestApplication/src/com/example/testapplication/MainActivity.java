package com.example.testapplication;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements NoteLocation.NoteLocationListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	public void requestLocationNoGps(View v) {
		NoteLocation temp = new NoteLocation(false, 1);
		temp.show(getSupportFragmentManager(), "dialog");
	}

	public void requestLocationGps(View v) {
		NoteLocation temp = new NoteLocation(true, 1);
		temp.show(getSupportFragmentManager(), "dialog");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onLocationFound(Location location, int typeFrag) {
		if (null == location) {
			Toast.makeText(this, "No Location found", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Location: " + String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
