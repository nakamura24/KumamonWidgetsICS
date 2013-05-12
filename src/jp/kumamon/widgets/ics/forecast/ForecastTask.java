/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.ics.forecast;

import jp.kumamon.widgets.forecast.AndroidSaxFeedParser;
import jp.kumamon.widgets.forecast.Message;
import jp.kumamon.widgets.lib.*;
import jp.kumamon.widgets.R;

import java.text.SimpleDateFormat;
import java.util.*;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

public class ForecastTask extends AsyncTask<Integer, Integer, Long> {
	private static final String TAG = "ForecastTask";
	private static final String APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";
	public static final String TITLE_HEADER = "TITLE_HEADER";
	public static final String TITLE_FOOTER = "TITLE_FOOTER";
	public static final String TITLE_COUNT = "TITLE_COUNT";
	public static final String TITLE_NO = "TITLE_";
	public static final String PUBDATE = "PUBDATE";
	private CityEntries cityEntries = null;
	private ArrayList<Message> mMessages;
	private Context mContext;
	private int appWidgetId;
	private int id = 63;

	public ForecastTask(Context context, int appWidgetId) {
		this.mContext = context.getApplicationContext();
		this.appWidgetId = appWidgetId;
		cityEntries = new CityEntries(mContext);
	}

	@Override
	protected Long doInBackground(Integer... params) {
		if (params.length > 0)
			id = params[0];
		String uri = "http://rss.rssad.jp/rss/tenki/forecast/city_"
				+ String.valueOf(id) + ".xml";
		Log.i(TAG, "doInBackground - " + uri);
		try {
			AndroidSaxFeedParser paser = new AndroidSaxFeedParser(uri);
			mMessages = paser.parse();
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
			return 0L;
		}
		return 0L;
	}

	@Override
	protected void onPostExecute(Long result) {
		try {
			Log.i(TAG, "onPostExecute - " + String.valueOf(appWidgetId));
			String header = cityEntries.getTitle(id);
			String footer;
			Resources resource = mContext.getResources();
			StaticHash hash = new StaticHash(mContext);
			hash.put(KumamonForecastWidget.LASTLOCATEID,
					String.valueOf(appWidgetId), id);
			hash.put(KumamonForecastWidget.LASTUPDATE,
					String.valueOf(appWidgetId), System.currentTimeMillis());
			hash.put(String.valueOf(appWidgetId), TITLE_HEADER, header);
			hash.put(String.valueOf(appWidgetId), TITLE_COUNT, mMessages.size());
			for (int i = 0; i < mMessages.size(); i++) {
				hash.put(String.valueOf(appWidgetId),
						TITLE_NO + String.valueOf(i), mMessages.get(i)
								.getTitle());
			}
			Date date = new Date();
			if (mMessages.size() > 0) {
				try {
					SimpleDateFormat FORMATTER = new SimpleDateFormat(
							"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
					date = FORMATTER.parse(mMessages.get(0).getDate());
				} catch (Exception e) {
					ExceptionLog.Log(TAG, e);
				}
			}
			SimpleDateFormat sdf = new SimpleDateFormat("d日HH時", Locale.JAPAN);
			footer = String.format(resource.getString(R.string.forecast),
					sdf.format(date));
			hash.put(String.valueOf(appWidgetId), TITLE_FOOTER, footer);

			updateAppWidget(id);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void updateAppWidget(int id) {
		try {
			Log.i(TAG, "onPostExecute - " + String.valueOf(appWidgetId));

			// ボタンが押された時に発行されるインテントを準備する
			Intent configIntent = new Intent(mContext,
					ForecastWidgetConfigure.class);
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			configIntent.setAction(APPWIDGET_CONFIGURE);
			PendingIntent configPendingIntent = PendingIntent.getActivity(
					mContext, appWidgetId, configIntent, 0);
			RemoteViews remoteViews = new RemoteViews(
					mContext.getPackageName(), R.layout.ics_forecast_widget);
			remoteViews.setOnClickPendingIntent(R.id.ics_imageView_icon,
					configPendingIntent);

			Intent stackViewIntent = new Intent(mContext,
					ForecastWidgetService.class);
			stackViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			// When intents are compared, the extras are ignored, so we need to
			// embed the extras
			// into the data so that the extras will not be ignored.
			stackViewIntent.setData(Uri.parse(stackViewIntent
					.toUri(Intent.URI_INTENT_SCHEME)));
			remoteViews.setRemoteAdapter(R.id.ics_stack_view, stackViewIntent);

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(mContext);
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
					R.id.ics_stack_view);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
