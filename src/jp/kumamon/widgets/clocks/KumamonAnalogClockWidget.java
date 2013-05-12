/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.clocks;

import jp.kumamon.widgets.lib.*;
import jp.kumamon.widgets.R;

import java.util.*;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class KumamonAnalogClockWidget extends WidgetBase {
	private static final String TAG = "KumamonAnalogClockWidget";
	public static final String AMPM = "AnalogClockWidget.AMPM";
	public static final String HOUR = "AnalogClockWidget.HOUR";
	public static final String MINUTE = "AnalogClockWidget.MINITE";
	public static final String SET = "AnalogClockWidget.SET";
	public static final String REPEAT = "AnalogClockWidget.REPEAT";
	public static final String ALARM = "AnalogClockWidget.ALARM";
	public static final String APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";
	public static final String APPWIDGET_ALARM = "android.appwidget.action.APPWIDGET_ALARM";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		try {
			for (int i = 0; i < appWidgetIds.length; i++) {
				Log.d(TAG, "onUpdate - " + String.valueOf(appWidgetIds[i]));
				UpdateAppWidget(context, appWidgetIds[i]);
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i(TAG, "onReceive" + intent.getAction());
		try {
			if (AnalogClockWidgetConfigure.CONFIG_DONE.equals(intent
					.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					Log.d(TAG, "appWidgetId=" + String.valueOf(appWidgetId));
					if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
						StaticHash hash = new StaticHash(context);
						boolean ampm = extras.getBoolean(AMPM, hash.get(AMPM,
								String.valueOf(appWidgetId), false));
						int hour = extras.getInt(HOUR,
								hash.get(HOUR, String.valueOf(appWidgetId), 0));
						int minute = extras.getInt(MINUTE, hash.get(MINUTE,
								String.valueOf(appWidgetId), 0));
						boolean set = extras.getBoolean(SET, hash.get(SET,
								String.valueOf(appWidgetId), false));
						boolean repeat = extras.getBoolean(REPEAT, hash.get(
								REPEAT, String.valueOf(appWidgetId), false));
						hash.put(AMPM, String.valueOf(appWidgetId), ampm);
						hash.put(HOUR, String.valueOf(appWidgetId), hour);
						hash.put(MINUTE, String.valueOf(appWidgetId), minute);
						hash.put(SET, String.valueOf(appWidgetId), set);
						hash.put(REPEAT, String.valueOf(appWidgetId), repeat);
						Log.d(TAG,
								"ampm=" + String.valueOf(ampm) + " hour="
										+ String.valueOf(hour) + " minute="
										+ String.valueOf(minute));
						if (hash.get(ALARM, String.valueOf(appWidgetId), false)) {
							Intent cancelIntent = new Intent(context,
									AnalogClockWidgetAlarm.class);
							cancelIntent.putExtra(
									AppWidgetManager.EXTRA_APPWIDGET_ID,
									appWidgetId);
							cancelIntent.setAction(APPWIDGET_ALARM);
							PendingIntent operation = PendingIntent
									.getActivity(context, appWidgetId,
											cancelIntent, 0);
							AlarmManager alarmManager = (AlarmManager) context
									.getSystemService(Context.ALARM_SERVICE);
							alarmManager.cancel(operation);
							hash.remove(ALARM, String.valueOf(appWidgetId));
						}
						if (set) {
							setAlarm(context, appWidgetId);
						}
					}
				}
			}
			if (AnalogClockWidgetAlarm.ALARM_DONE.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					Log.d(TAG, "appWidgetId=" + String.valueOf(appWidgetId));
					if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
						StaticHash hash = new StaticHash(context);
						if (hash.get(SET, String.valueOf(appWidgetId), false)
								&& hash.get(REPEAT,
										String.valueOf(appWidgetId), false)) {
							hash.remove(ALARM, String.valueOf(appWidgetId));
							setAlarm(context, appWidgetId);
						}
					}
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
			StaticHash hash = new StaticHash(context);
			for (int appWidgetId : appWidgetIds) {
				hash.remove(AMPM, String.valueOf(appWidgetId));
				hash.remove(HOUR, String.valueOf(appWidgetId));
				hash.remove(MINUTE, String.valueOf(appWidgetId));
				hash.remove(SET, String.valueOf(appWidgetId));
				hash.remove(REPEAT, String.valueOf(appWidgetId));
				hash.remove(ALARM, String.valueOf(appWidgetId));
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		super.onDisabled(context);
	}

	private void UpdateAppWidget(Context context, int appWidgetId) {
		try {
			// ボタンが押された時に発行されるインテントを準備する
			Intent buttonIntent = new Intent(context,
					AnalogClockWidgetConfigure.class);
			buttonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			buttonIntent.setAction(APPWIDGET_CONFIGURE);
			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					appWidgetId, buttonIntent, 0);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.analog_clock_widget);
			remoteViews.setOnClickPendingIntent(R.id.analogclock_back,
					pendingIntent);
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	private void setAlarm(Context context, int appWidgetId) {
		Log.i(TAG, "setAlarm - " + String.valueOf(appWidgetId));
		try {
			Calendar alarmDate = Calendar.getInstance();
			StaticHash hash = new StaticHash(context);
			if (hash.get(AMPM, String.valueOf(appWidgetId), false)) {
				alarmDate.set(Calendar.HOUR_OF_DAY,
						12 + hash.get(HOUR, String.valueOf(appWidgetId), 0));
				alarmDate.set(Calendar.MINUTE,
						hash.get(MINUTE, String.valueOf(appWidgetId), 0));
				alarmDate.set(Calendar.SECOND, 0);
				alarmDate.set(Calendar.MILLISECOND, 0);
			} else {
				alarmDate.set(Calendar.HOUR_OF_DAY,
						hash.get(HOUR, String.valueOf(appWidgetId), 0));
				alarmDate.set(Calendar.MINUTE,
						hash.get(MINUTE, String.valueOf(appWidgetId), 0));
				alarmDate.set(Calendar.SECOND, 0);
				alarmDate.set(Calendar.MILLISECOND, 0);
			}
			Calendar now = Calendar.getInstance();
			if (now.getTimeInMillis() > alarmDate.getTimeInMillis())
				alarmDate.add(Calendar.DATE, 1);
			Log.d(TAG, "setAlarm - " + alarmDate.toString());

			Intent intent = new Intent(context, AnalogClockWidgetAlarm.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.setAction(APPWIDGET_ALARM);
			PendingIntent operation = PendingIntent.getActivity(context,
					appWidgetId, intent, 0);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					alarmDate.getTimeInMillis(), operation);
			hash.put(ALARM, String.valueOf(appWidgetId), true);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
