/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.ics.notice;

import jp.kumamon.widgets.lib.*;
import jp.kumamon.widgets.R;

import java.util.*;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class KumamonNoticeWidget extends WidgetBase {
	private static final String TAG = "KumamonNoticeWidget";
	private static final String APPWIDGET = "jp.kumamon.widgets.ics.notice.APPWIDGET";
	public static final String ConfigureAllDay = "AllDay";
	public static final String ConfigurePeriod = "Period";
	public static final String EXTRA_ITEM = "jp.kumamon.widgets.ics.notice.EXTRA_ITEM";
	public static final String ACTION_FILLIN = "jp.kumamon.widgets.ics.notice.ACTION_FILLIN";
	private static final String ACTION_START_ALARM = "jp.kumamon.widgets.ics.notice.ACTION_START_ALARM";
	private static final long interval = 30 * 60 * 1000;
	public static int mPosition;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.i(TAG, "onEnabled");
		context = context.getApplicationContext();
		Intent intent = new Intent(context, WidgetService.class);
		context.startService(intent);
		StaticHash hash = new StaticHash(context);
		hash.put(ConfigureAllDay, false);
		hash.put(ConfigurePeriod, 7 * 2);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(TAG, "onUpdate");
		context = context.getApplicationContext();
		try {
			for (int i = 0; i < appWidgetIds.length; i++) {
				updateAppWidget(context, appWidgetIds[i]);
				StaticHash hash = new StaticHash(context);
				hash.put(APPWIDGET, String.valueOf(appWidgetIds[i]),
						appWidgetIds[i]);

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
			context = context.getApplicationContext();
			if (NoticeWidgetConfigure.CONFIG_DONE.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					Log.d(TAG, "appWidgetId=" + String.valueOf(appWidgetId));
					if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
						updateAppWidget(context, appWidgetId);
						setAlarm(context, appWidgetId);
					}
				}
			}
			if (ACTION_START_ALARM.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					StaticHash hash = new StaticHash(context);
					ArrayList<String> keys = hash.keys(APPWIDGET);
					for (int i = 0; i < keys.size(); i++) {
						if (appWidgetId == Integer.parseInt(keys.get(i))) {
							updateAppWidget(context, appWidgetId);
							setAlarm(context, appWidgetId);
							Log.d(TAG, "ACTION_START_ALARM appWidgetId="
									+ String.valueOf(appWidgetId));
						}
					}
				}
			}
			if (ACTION_FILLIN.equals(intent.getAction())) {
				context = context.getApplicationContext();
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					int position = extras.getInt(EXTRA_ITEM, 0);
					Intent dialogIntent = new Intent(context,
							NoticeEventDialog.class);
					dialogIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
							appWidgetId);
					dialogIntent.putExtra(KumamonNoticeWidget.EXTRA_ITEM,
							position);
					PendingIntent pendingIntent = PendingIntent.getActivity(
							context, appWidgetId, dialogIntent, 0);
					pendingIntent.send(context, appWidgetId, dialogIntent);
					mPosition = position;
					Log.d(TAG, "position =" + String.valueOf(position));
				}
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(TAG, "onDeleted");
		context = context.getApplicationContext();
		StaticHash hash = new StaticHash(context);
		ArrayList<String> keys = hash.keys(APPWIDGET);
		for (int i = 0; i < appWidgetIds.length; i++) {
			for (int j = 0; j < keys.size(); j++) {
				if (appWidgetIds[i] == Integer.parseInt(keys.get(j))) {
					hash.remove(APPWIDGET, keys.get(j));
					Log.d(TAG, "onDeleted - " + String.valueOf(appWidgetIds[i]));
				}
			}
		}
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		context = context.getApplicationContext();
		StaticHash hash = new StaticHash(context);
		ArrayList<String> keys = hash.keys(APPWIDGET);
		for (int j = 0; j < keys.size(); j++) {
			hash.remove(APPWIDGET, keys.get(j));
		}
		Intent intent = new Intent(context, WidgetService.class);
		context.stopService(intent);
		super.onDisabled(context);
	}

	public static void updateAppWidget(Context context, int appWidgetId) {
		Log.i(TAG, "updateAppWidget " + String.valueOf(appWidgetId));
		try {
			context = context.getApplicationContext();
			Intent viewIntent = new Intent(context, NoticeWidgetService.class);
			viewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			// When intents are compared, the extras are ignored, so we need to
			// embed the extras
			// into the data so that the extras will not be ignored.
			viewIntent.setData(Uri.parse(viewIntent
					.toUri(Intent.URI_INTENT_SCHEME)));
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.notice_widget);
			remoteViews.setRemoteAdapter(R.id.stack_view_notice, viewIntent);

			// The empty view is displayed when the collection has no items. It
			// should be a sibling
			// of the collection view.
			remoteViews.setEmptyView(R.id.stack_view_notice, R.id.empty_view);

			Intent fillInIntent = new Intent(context, KumamonNoticeWidget.class);
			fillInIntent.setAction(ACTION_FILLIN);
			PendingIntent fillInPendingIntent = PendingIntent.getBroadcast(
					context, appWidgetId, fillInIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setPendingIntentTemplate(R.id.stack_view_notice,
					fillInPendingIntent);

			// ボタンが押された時に発行されるインテントを準備する
			Intent intent = new Intent(context, NoticeWidgetConfigure.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					appWidgetId, intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.imageView_icon,
					pendingIntent);

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
					R.id.stack_view_notice);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	private void setAlarm(Context context, int appWidgetId) {
		try {
			context = context.getApplicationContext();
			Intent intent = new Intent(context, KumamonNoticeWidget.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.setAction(ACTION_START_ALARM);
			PendingIntent operation = PendingIntent.getBroadcast(context,
					appWidgetId, intent, 0);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			long now = System.currentTimeMillis();
			long oneHourAfter = now + interval;
			alarmManager.set(AlarmManager.RTC, oneHourAfter, operation);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	public static class WidgetService extends Service {
		@Override
		public IBinder onBind(Intent in) {
			return null;
		}

		@Override
		public void onCreate() {
			super.onCreate();
			Log.i(TAG, "onCreate");
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_USER_PRESENT);
			registerReceiver(mReceiver, filter);
		}

		@Override
		public void onDestroy() {
			Log.i(TAG, "onDestroy");
			unregisterReceiver(mReceiver);
			super.onDestroy();
		}
	}

	private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "BroadcastReceiver onReceive - " + intent.getAction());
			try {
				context = context.getApplicationContext();
				String action = intent.getAction();
				if (Intent.ACTION_USER_PRESENT.equals(action)) {
					Log.d(TAG, "ACTION_USER_PRESENT - " + intent.getAction());
					StaticHash hash = new StaticHash(context);
					ArrayList<String> keys = hash.keys(APPWIDGET);
					for (int i = 0; i < keys.size(); i++) {
						updateAppWidget(context, Integer.parseInt(keys.get(i)));
						Log.d(TAG, "appWidgetId - " + keys.get(i));
					}
				}
			} catch (Exception e) {
				ExceptionLog.Log(TAG, e);
			}
		}
	};
}
