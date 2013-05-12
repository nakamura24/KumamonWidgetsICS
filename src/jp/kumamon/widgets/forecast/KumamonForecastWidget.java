/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.forecast;

import jp.kumamon.widgets.lib.*;

import java.util.*;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class KumamonForecastWidget extends WidgetBase {
	private static final String TAG = "KumamonForecastWidget";
	public static final String LOCATEID = "forecast.LocateId";
	public static final String LASTLOCATEID = "forecast.LastLocateId";
	public static final String LASTUPDATE = "forecast.LastUpdate";
	public static final String APPWIDGET_ALARM = "jp.kumamon.widgets.forecast.APPWIDGET_ALARM";
	private static final long gps_minTime = 2 * 60 * 60 * 1000;
	private static final long gps_minDistance = 0;
	private static Context mContext = null;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.i(TAG, "onEnabled");
		mContext = context.getApplicationContext();
		try {
			// Acquire a reference to the system Location Manager
			LocationManager locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			// Register the listener with the Location Manager to receive
			// location updates
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, gps_minTime, gps_minDistance,
					mLocationListener);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(TAG, "onUpdate");
		mContext = context.getApplicationContext();
		try {
			for (int i = 0; i < appWidgetIds.length; i++) {
				Log.d(TAG, "onUpdate - " + String.valueOf(appWidgetIds[i]));
				StaticHash hash = new StaticHash(context);
				hash.put(LOCATEID, String.valueOf(appWidgetIds[i]), 0);
				// Forecast Get
				getForecast(appWidgetIds[i]);
				setAlarm(context, appWidgetIds[i]);
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i(TAG, "onReceive - " + intent.getAction());
		try {
			mContext = context.getApplicationContext();
			if (ForecastWidgetConfigure.CONFIG_DONE.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					int id = extras.getInt(LOCATEID, 63);
					Log.d(TAG,
							"CONFIG_DONE appWidgetId="
									+ String.valueOf(appWidgetId) + "id="
									+ String.valueOf(id));
					StaticHash hash = new StaticHash(mContext);
					hash.put(LOCATEID, String.valueOf(appWidgetId), id);
					if (hash.contains(LASTLOCATEID, String.valueOf(appWidgetId))) {
						hash.remove(LASTLOCATEID, String.valueOf(appWidgetId));
					}
					getForecast(appWidgetId);
				}
			}
			if (APPWIDGET_ALARM.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					getForecast(appWidgetId);
					setAlarm(context, appWidgetId);
				}
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(TAG, "onDeleted");
		super.onDeleted(context, appWidgetIds);
		try {
			context = context.getApplicationContext();
			StaticHash hash = new StaticHash(context);
			for (int i = 0; i < appWidgetIds.length; i++) {
				Log.d(TAG, "onDeleted - " + String.valueOf(appWidgetIds[i]));
				hash.remove(LOCATEID, String.valueOf(appWidgetIds[i]));
				hash.remove(LASTLOCATEID, String.valueOf(appWidgetIds[i]));
				hash.remove(LASTUPDATE, String.valueOf(appWidgetIds[i]));
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		super.onDisabled(context);
		try {
			context = context.getApplicationContext();
			StaticHash hash = new StaticHash(context);
			ArrayList<String> keys = hash.keys(LOCATEID);
			for (int i = 0; i < keys.size(); i++) {
				Log.d(TAG, "onDeleted - " + keys);
				hash.remove(LOCATEID, keys.get(i));
				hash.remove(LASTLOCATEID, keys.get(i));
				hash.remove(LASTUPDATE, keys.get(i));
			}
			LocationManager locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			locationManager.removeUpdates(mLocationListener);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	private final LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.i(TAG, "onLocationChanged");
			if (location != null) {
				try {
					double longitude = location.getLongitude();
					double latitude = location.getLatitude();
					double altitude = location.getAltitude();
					Log.d(TAG,
							"onLocationChanged - " + String.valueOf(longitude)
									+ " " + String.valueOf(latitude) + " "
									+ String.valueOf(altitude));
				} catch (Exception e) {
					ExceptionLog.Log(TAG, e);
				}
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.i(TAG, "onStatusChanged");
		}

		public void onProviderEnabled(String provider) {
			Log.i(TAG, "onProviderEnabled");
		}

		public void onProviderDisabled(String provider) {
			Log.i(TAG, "onProviderDisabled");
		}
	};

	private CityEntries mCityEntries = null;
	private int mAppWidgetId = 0;
	private Handler mHandler = new Handler();

	private Runnable mRunnable = new Runnable() {
		public void run() {
			Log.i(TAG, "run - " + String.valueOf(mAppWidgetId));
			try {
				StaticHash hash = new StaticHash(mContext);
				int id = hash.get(LOCATEID, String.valueOf(mAppWidgetId), 63);
				if (id == 0) {
					LocationManager locationManager = (LocationManager) mContext
							.getSystemService(Context.LOCATION_SERVICE);
					Location location = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (mCityEntries != null && location != null)
						id = mCityEntries.getId(location.getLongitude(),
								location.getLatitude());
					else
						id = 63;
				}
				// システムから接続情報をとってくる
				ConnectivityManager conMan = (ConnectivityManager) mContext
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				// モバイル回線（３G）の接続状態を取得
				State mobile = conMan.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE).getState();
				// wifiの接続状態を取得
				State wifi = conMan.getNetworkInfo(
						ConnectivityManager.TYPE_WIFI).getState();

				// 3Gデータ通信／wifi共に接続状態じゃない場合
				if ((mobile != State.CONNECTED) && (wifi != State.CONNECTED)) {
					// ネットワーク未接続
					Log.w(TAG,
							"Network Disconnect - "
									+ String.valueOf(mAppWidgetId));
					return;
				}
				ForecastTask task = new ForecastTask(mContext, mAppWidgetId);
				task.execute(id);
				Log.d(TAG, "run ForecastTask - " + String.valueOf(mAppWidgetId));
			} catch (Exception e) {
				ExceptionLog.Log(TAG, e);
			}
		}
	};

	private void getForecast(int appWidgetId) {
		Log.i(TAG, "getForecast - " + String.valueOf(appWidgetId));
		mAppWidgetId = appWidgetId;
		mCityEntries = new CityEntries(mContext);
		try {
			StaticHash hash = new StaticHash(mContext);
			int id = 0;
			if (hash.contains(LOCATEID, String.valueOf(appWidgetId))) {
				id = hash.get(LOCATEID, String.valueOf(appWidgetId), 63);
			}
			if (id == 0) {
				LocationManager locationManager = (LocationManager) mContext
						.getSystemService(Context.LOCATION_SERVICE);
				Location location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (mCityEntries != null && location != null)
					id = mCityEntries.getId(location.getLongitude(),
							location.getLatitude());
				else
					id = 63;
			}
			if (hash.contains(LASTLOCATEID, String.valueOf(appWidgetId))
					&& hash.get(LASTLOCATEID, String.valueOf(appWidgetId), 63) == id) {
				if (hash.contains(LASTUPDATE, String.valueOf(appWidgetId))) {
					Calendar lastUpdate = Calendar.getInstance();
					lastUpdate.setTimeInMillis(hash.get(LASTUPDATE,
							String.valueOf(appWidgetId), 0L));
					Calendar nowDate = Calendar.getInstance();
					Log.d(TAG, "LastUpdate - " + lastUpdate.toString());
					if (lastUpdate.get(Calendar.DAY_OF_YEAR) == nowDate
							.get(Calendar.DAY_OF_YEAR)
							&& lastUpdate.get(Calendar.HOUR_OF_DAY) >= 7
							&& nowDate.get(Calendar.HOUR_OF_DAY) < 12)
						return;
					if (lastUpdate.get(Calendar.DAY_OF_YEAR) == nowDate
							.get(Calendar.DAY_OF_YEAR)
							&& lastUpdate.get(Calendar.HOUR_OF_DAY) >= 12
							&& nowDate.get(Calendar.HOUR_OF_DAY) < 18)
						return;
					if (lastUpdate.get(Calendar.DAY_OF_YEAR) == nowDate
							.get(Calendar.DAY_OF_YEAR)
							&& lastUpdate.get(Calendar.HOUR_OF_DAY) >= 18)
						return;
					if (lastUpdate.get(Calendar.DAY_OF_YEAR) == nowDate
							.get(Calendar.DAY_OF_YEAR)
							&& lastUpdate.get(Calendar.HOUR_OF_DAY) < 7
							&& nowDate.get(Calendar.HOUR_OF_DAY) < 7)
						return;
				}
			}
			// システムから接続情報をとってくる
			ConnectivityManager conMan = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			// モバイル回線（３G）の接続状態を取得
			State mobile = conMan.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState();
			// wifiの接続状態を取得
			State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();

			// 3Gデータ通信／wifi共に接続状態じゃない場合
			if ((mobile != State.CONNECTED) && (wifi != State.CONNECTED)) {
				// ネットワーク未接続
				mHandler.postDelayed(mRunnable, 30 * 1000);
				return;
			}
			ForecastTask task = new ForecastTask(mContext, appWidgetId);
			task.execute(id);
			Log.d(TAG,
					"getForecast ForecastTask - " + String.valueOf(appWidgetId));
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	private void setAlarm(Context context, int appWidgetId) {
		Log.i(TAG, "setAlarm - " + String.valueOf(appWidgetId));
		try {
			// context = context.getApplicationContext();
			Calendar alarmDate = Calendar.getInstance();
			alarmDate.add(Calendar.HOUR_OF_DAY, 1);
			int hour = alarmDate.get(Calendar.HOUR_OF_DAY);
			hour &= 0xfffe;
			alarmDate.set(Calendar.HOUR_OF_DAY, hour + 1);
			alarmDate.set(Calendar.MINUTE, 0);
			alarmDate.set(Calendar.SECOND, 0);
			alarmDate.set(Calendar.MILLISECOND, 0);
			Log.d(TAG, "setAlarm - " + alarmDate.toString());

			Intent intent = new Intent(context, KumamonForecastWidget.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.setAction(APPWIDGET_ALARM);
			PendingIntent operation = PendingIntent.getBroadcast(context,
					appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					alarmDate.getTimeInMillis(), operation);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
