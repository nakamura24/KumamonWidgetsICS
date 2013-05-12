/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.sticky;

import jp.kumamon.widgets.R;
import jp.kumamon.widgets.lib.*;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class KumamonStickyWidget extends WidgetBase {
	private static final String TAG = "KumamonStickyWidget";
	public static final String COMMENT = "jp.kumamon.widgets.sticky.COMMENT";
	public static final String ICON = "jp.kumamon.widgets.sticky.ICON";
	public static final String BACK = "jp.kumamon.widgets.sticky.BACK";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(TAG, "onUpdate");
		try {
			context = context.getApplicationContext();
			for (int i = 0; i < appWidgetIds.length; i++) {
				updateWidget(context, appWidgetIds[i], "", 0, 0);
				Log.i(TAG, "mAppWidgetId=" + String.valueOf(appWidgetIds[i]));
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
			if (StickyWidgetConfigure.CONFIG_DONE.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int appWidgetId = extras.getInt(
							AppWidgetManager.EXTRA_APPWIDGET_ID,
							AppWidgetManager.INVALID_APPWIDGET_ID);
					Log.d(TAG, "appWidgetId=" + String.valueOf(appWidgetId));
					if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
						String comment = extras.getString(COMMENT);
						int icon = extras.getInt(ICON, 0);
						int back = extras.getInt(BACK, 0);

						StaticHash hash = new StaticHash(context);
						hash.put(COMMENT, String.valueOf(appWidgetId), comment);
						hash.put(ICON, String.valueOf(appWidgetId), icon);
						hash.put(BACK, String.valueOf(appWidgetId), back);

						updateWidget(context, appWidgetId, comment, icon, back);
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
		try {
			context = context.getApplicationContext();
			StaticHash hash = new StaticHash(context);
			for (int i = 0; i < appWidgetIds.length; i++) {
				hash.remove(COMMENT, String.valueOf(appWidgetIds[i]));
				hash.remove(ICON, String.valueOf(appWidgetIds[i]));
				hash.remove(BACK, String.valueOf(appWidgetIds[i]));
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		try {
			context = context.getApplicationContext();
			StaticHash hash = new StaticHash(context);
			ArrayList<String> keys = hash.keys(COMMENT);
			for (int i = 0; i < keys.size(); i++) {
				hash.remove(COMMENT, keys.get(i));
				hash.remove(ICON, keys.get(i));
				hash.remove(BACK, keys.get(i));
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	private void updateWidget(Context context, int appWidgetId, String comment,
			int icon, int back) {
		int[] icons = { R.drawable.kuma0, R.drawable.kuma1, R.drawable.kuma2,
				R.drawable.kuma3 };
		int[] backs = { R.drawable.back0, R.drawable.back1, R.drawable.back2,
				R.drawable.back3, R.drawable.back4, R.drawable.back5,
				R.drawable.back6, R.drawable.back7, R.drawable.back8,
				R.drawable.back9, };
		try {
			// ボタンが押された時に発行されるインテントを準備する
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.sticky_widget);
			Intent intent = new Intent(context, StickyWidgetConfigure.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					appWidgetId, intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.imageView1, pendingIntent);

			remoteViews.setTextViewText(R.id.textView, comment);
			remoteViews.setImageViewResource(R.id.imageView0, icons[icon]);
			remoteViews.setImageViewResource(R.id.imageView1, backs[back]);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
