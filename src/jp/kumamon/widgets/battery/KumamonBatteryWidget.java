/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.battery;

import jp.kumamon.widgets.lib.*;
import jp.kumamon.widgets.R;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class KumamonBatteryWidget extends WidgetBase {
	private static final String TAG = "KumamonBatteryWidget";

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.i(TAG, "onEnabled");
		Intent intent = new Intent(context, WidgetService.class);
		context.startService(intent);
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		Intent intent = new Intent(context, WidgetService.class);
		context.stopService(intent);
		super.onDisabled(context);
	}

	public static class WidgetService extends Service {
		@Override
		public void onCreate() {
			super.onCreate();
			Log.i(TAG, "onCreate");
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			registerReceiver(batteryReceiver, filter);
		}

		@Override
		public void onDestroy() {
			Log.i(TAG, "onDestroy");
			unregisterReceiver(batteryReceiver);
			super.onDestroy();
		}

		@Override
		public IBinder onBind(Intent in) {
			return null;
		}
	}

	private static BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		final int[] IMAGE = { R.drawable.battery20, R.drawable.battery40,
				R.drawable.battery60, R.drawable.battery80,
				R.drawable.battery100, R.drawable.battery100 };
		int scale = 100;
		int level = 0;

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "BroadcastReceiver onReceive - " + intent.getAction());
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				level = intent.getIntExtra("level", 0);
				scale = intent.getIntExtra("scale", 0);
				RemoteViews remoteViews = new RemoteViews(
						context.getPackageName(), R.layout.battery_widget);
				remoteViews.setImageViewResource(R.id.imageView_icon,
						IMAGE[(int) (level * 100 / scale / 20.0)]);
				AppWidgetManager appWidgetManager = AppWidgetManager
						.getInstance(context);
				ComponentName componentName = new ComponentName(context,
						KumamonBatteryWidget.class);
				appWidgetManager.updateAppWidget(componentName, remoteViews);
			}
		}
	};
}
