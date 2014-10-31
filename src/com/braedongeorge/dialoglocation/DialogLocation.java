/*
 * 	Copyright 2014 Braedon Reid
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */

package com.example.testapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.DialogFragment;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DialogLocation extends DialogFragment implements LocationListener {
	private static final String DIALOG_LOCATION_UPDATING_NETWORK_TITLE = "Updating Location (Network)..";
	private static final String	DIALOG_LOCATION_USE_NETWORK = "Use network location";
	private static final String DIALOG_LOCATION_NO_LOCATION_SERVICES_TITLE = "No Location Services Enabled";
	private static final String DIALOG_LOCATION_NO_LOCATION_SERVICES_MESSAGE = "Please enable location services to use the functionality of this app";
	private static final String DIALOG_LOCATION_UPDATING_GPS_TITLE = "Updating Location (GPS)..";
	private static final String DIALOG_LOCATION_ONLY_GPS_MESSAGE = "GPS is the only location service enabled. Click settings to enable other location services";
	private static final String DIALOG_LOCATION_BUTTON_SETTINGS = "Change location settings";
	private static final String DIALOG_LOCATION_CANCEL = "Cancel";
	private static final String DIALOG_LOCATION_NETWORK_BUTTON_TEXT = "Using Network...";
	
	private static final int NO_LOCATION_SERVICES = 0;
	private static final int USING_ONLY_GPS_LOCATION = 1;
	private static final int USING_GPS_LOCATION_NETWORK_AVAILABLE = 2;
	private static final int USING_NETWORK_LOCATION = 3;
	
	
	private DialogLocationListener mCallback;
	public LocationManager mLocationManager;
	private String mProvider;
	private Criteria mCriteria;
	private int mFragmentId;
	private AlertDialog mRealDialog;
	private boolean mAbortRequest = false;
	private boolean mGpsPref;
	private ProgressBar mProgressBar;
	private ProgressBar mProgressBarInv;
		

	public DialogLocation(boolean ignoreGPS, int fragmentId) {
		mFragmentId = fragmentId;
		mGpsPref = ignoreGPS;
	}

	public interface DialogLocationListener {
		public void onLocationFound(Location location, int fragmentId);
	}

	@SuppressLint("InlinedApi")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mProgressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleLarge);
		mProgressBarInv = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleLarge);
		mProgressBarInv.setVisibility(ProgressBar.GONE);
		
		
		mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		mCriteria = new Criteria();
		int criteria = (mGpsPref) ? Criteria.POWER_HIGH : Criteria.POWER_MEDIUM;
		mCriteria.setPowerRequirement(criteria);

		mProvider = mLocationManager.getBestProvider(mCriteria, true);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		int telephonyInfo = tm.getNetworkType();
		
		boolean networkAvailable = true;
		
		if ((telephonyInfo == TelephonyManager.NETWORK_TYPE_UNKNOWN && !networkInfo.isConnected()) || !mLocationManager.isProviderEnabled("network")) {
			networkAvailable = false;
		}
		
		int locationMode = -1;
		int locationType = -1;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				locationMode = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
			} catch (SettingNotFoundException e) {
				e.printStackTrace();
			}
			
			if (locationMode == Settings.Secure.LOCATION_MODE_OFF || (!networkAvailable && (mProvider.matches("network"))))
				locationType = NO_LOCATION_SERVICES;
			else if (mGpsPref && (locationMode == Settings.Secure.LOCATION_MODE_SENSORS_ONLY || locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY)) 
				locationType = (locationMode == Settings.Secure.LOCATION_MODE_SENSORS_ONLY || !networkAvailable) ? USING_ONLY_GPS_LOCATION : USING_GPS_LOCATION_NETWORK_AVAILABLE;
			else if (mProvider.matches("network") && (locationMode == Settings.Secure.LOCATION_MODE_BATTERY_SAVING || locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY))	
				locationType = USING_NETWORK_LOCATION;
			
		} else {
			if (mProvider.matches("passive") || !networkAvailable && (mProvider.matches("network") || (!mGpsPref && mProvider.matches("gps"))))
				locationType = NO_LOCATION_SERVICES;
			else if ( mProvider.matches("gps") && mGpsPref) 
				locationType = ((mProvider.matches("gps")) || !networkAvailable) ? USING_ONLY_GPS_LOCATION : USING_GPS_LOCATION_NETWORK_AVAILABLE;
			else if (mProvider.matches("network"))
				locationType = USING_NETWORK_LOCATION;
		}
		

		switch (locationType) {
		case NO_LOCATION_SERVICES:
			builder.setTitle(DIALOG_LOCATION_NO_LOCATION_SERVICES_TITLE);
			builder.setMessage(DIALOG_LOCATION_NO_LOCATION_SERVICES_MESSAGE);
			builder.setNeutralButton(DIALOG_LOCATION_BUTTON_SETTINGS, noNetworkButton);
			mAbortRequest = true;
			break;
		case USING_ONLY_GPS_LOCATION:
			builder.setTitle(DIALOG_LOCATION_UPDATING_GPS_TITLE);
			builder.setMessage(DIALOG_LOCATION_ONLY_GPS_MESSAGE);
			builder.setNeutralButton(DIALOG_LOCATION_BUTTON_SETTINGS, noNetworkButton);
			builder.setView(mProgressBar);
			break;
		case USING_GPS_LOCATION_NETWORK_AVAILABLE: 
			builder.setTitle(DIALOG_LOCATION_UPDATING_GPS_TITLE);
			builder.setPositiveButton(DIALOG_LOCATION_USE_NETWORK, null);
			builder.setView(mProgressBar);
			break;
		case USING_NETWORK_LOCATION:
			builder.setView(mProgressBar);
			builder.setTitle(DIALOG_LOCATION_UPDATING_NETWORK_TITLE);
			break;
		}

		builder.setNegativeButton(DIALOG_LOCATION_CANCEL, cancelListener);
		builder.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					mCallback.onLocationFound(null, mFragmentId);
					mLocationManager.removeUpdates(DialogLocation.this);
					Toast.makeText(getActivity(), "Location request cancelled", Toast.LENGTH_SHORT).show();
					dialog.cancel();
					return true;
				}
				return false;
			}
		});
				
		mRealDialog = builder.create();
		mRealDialog.setOnShowListener(usingNetwork);
		mRealDialog.setCanceledOnTouchOutside(false);
		return mRealDialog;
	}

	private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			mCallback.onLocationFound(null, mFragmentId);
			mLocationManager.removeUpdates(DialogLocation.this);
		}
	};

	private DialogInterface.OnClickListener noNetworkButton = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(callGPSSettingIntent);

		}
	};

	private DialogInterface.OnShowListener usingNetwork = new DialogInterface.OnShowListener() {
		@Override
		public void onShow(DialogInterface dialog) {

			final Button button = mRealDialog.getButton(DialogInterface.BUTTON_POSITIVE);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mCriteria.setPowerRequirement(Criteria.POWER_MEDIUM);
					mProvider = mLocationManager.getBestProvider(mCriteria, true);

					if (mProvider.matches("network")) {
						mLocationManager.requestLocationUpdates(mProvider, 200, 0, DialogLocation.this);
						button.setText(DIALOG_LOCATION_NETWORK_BUTTON_TEXT);
						mRealDialog.setTitle(DIALOG_LOCATION_UPDATING_NETWORK_TITLE);
						mProgressBarInv.setVisibility(ProgressBar.VISIBLE);
						
						// mRealDialog.show();
					}
				}
			});
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (DialogLocationListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DialogLocationListener");
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!mAbortRequest) {
			mLocationManager.requestLocationUpdates(mProvider, 200, 0, this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		mCallback.onLocationFound(location, mFragmentId);
		mLocationManager.removeUpdates(this);
		this.dismiss();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(getActivity(), "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
		Criteria criteria = new Criteria();
		provider = mLocationManager.getBestProvider(criteria, true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, this);

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(getActivity(), "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
		Criteria criteria = new Criteria();
		provider = mLocationManager.getBestProvider(criteria, true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(this);
		this.dismiss();
	}

}
