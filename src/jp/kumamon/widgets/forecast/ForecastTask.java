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
import jp.kumamon.widgets.R;

import java.text.SimpleDateFormat;
import java.util.*;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class ForecastTask extends AsyncTask<Integer, Integer, Long> {
	private static final String TAG = "ForecastTask";
	private static final String APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";
	private static final String APPWIDGET_WEEKLY = "jp.kumamon.widgets.forecast.APPWIDGET_WEEKLY";
	public static final String TITLE_COUNT = "TITLE_COUNT";
	public static final String TITLE_NO = "TITLE_";
	public static final String ICON = "ICON_";
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
			StaticHash hash = new StaticHash(mContext);
			hash.put(KumamonForecastWidget.LASTLOCATEID,
					String.valueOf(appWidgetId), id);
			hash.put(KumamonForecastWidget.LASTUPDATE,
					String.valueOf(appWidgetId), System.currentTimeMillis());
			hash.put(String.valueOf(appWidgetId), TITLE_COUNT, mMessages.size());
			for (int i = 0; i < mMessages.size(); i++) {
				hash.put(String.valueOf(appWidgetId),
						TITLE_NO + String.valueOf(i), mMessages.get(i)
								.getTitle());
				hash.put(String.valueOf(appWidgetId), ICON + String.valueOf(i),
						mMessages.get(i).getUrl());
			}
			if (mMessages.size() > 0) {
				hash.put(String.valueOf(appWidgetId), PUBDATE, mMessages.get(0)
						.getDate());
			}

			updateAppWidget(id);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	public void updateAppWidget(int id) {
		try {
			Log.i(TAG, "updateAppWidget - " + String.valueOf(appWidgetId));
			// ボタンが押された時に発行されるインテントを準備する
			Intent configIntent = new Intent(mContext,
					ForecastWidgetConfigure.class);
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			configIntent.setAction(APPWIDGET_CONFIGURE);
			PendingIntent configPendingIntent = PendingIntent.getActivity(
					mContext, appWidgetId, configIntent, 0);
			RemoteViews remoteViews = new RemoteViews(
					mContext.getPackageName(), R.layout.forecast_widget);
			remoteViews.setOnClickPendingIntent(R.id.imageView_icon,
					configPendingIntent);
			// 都市名が押された時に発行されるインテントを準備する
			Intent browseIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(cityEntries.getLink(id)));
			browseIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			PendingIntent browsePendingIntent = PendingIntent.getActivity(
					mContext, appWidgetId, browseIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.textView_title,
					browsePendingIntent);
			// weeklyが押された時に発行されるインテントを準備する
			Intent weeklyIntent = new Intent(mContext,
					ForecastWeeklyDialog.class);
			weeklyIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			weeklyIntent.setAction(APPWIDGET_WEEKLY);
			PendingIntent weeklyPendingIntent = PendingIntent.getActivity(
					mContext, appWidgetId, weeklyIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.linearLayout,
					weeklyPendingIntent);

			remoteViews.setTextViewText(R.id.textView_title,
					cityEntries.getTitle(id));
			StaticHash hash = new StaticHash(mContext);
			remoteViews.setImageViewBitmap(
					R.id.imageView_1st,
					setIcon(hash.get(String.valueOf(appWidgetId),
							ICON + String.valueOf(0), "")));
			remoteViews.setImageViewBitmap(
					R.id.imageView_2nd,
					setIcon(hash.get(String.valueOf(appWidgetId),
							ICON + String.valueOf(1), "")));
			remoteViews.setImageViewBitmap(
					R.id.imageView_3rd,
					setIcon(hash.get(String.valueOf(appWidgetId),
							ICON + String.valueOf(2), "")));
			remoteViews.setTextViewText(
					R.id.textView_1st,
					hash.get(String.valueOf(appWidgetId),
							TITLE_NO + String.valueOf(0), "")
							.replace(' ', '\n'));
			remoteViews.setTextViewText(
					R.id.textView_2nd,
					hash.get(String.valueOf(appWidgetId),
							TITLE_NO + String.valueOf(1), "")
							.replace(' ', '\n'));
			remoteViews.setTextViewText(
					R.id.textView_3rd,
					hash.get(String.valueOf(appWidgetId),
							TITLE_NO + String.valueOf(2), "")
							.replace(' ', '\n'));
			Resources resource = mContext.getResources();
			SimpleDateFormat FORMATTER = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
			Date date = FORMATTER.parse(hash.get(String.valueOf(appWidgetId),
					PUBDATE, FORMATTER.format(new Date())));
			Log.d(TAG, date.toString());
			SimpleDateFormat sdf = new SimpleDateFormat("d日HH時",
					Locale.getDefault());
			remoteViews.setTextViewText(
					R.id.textView_resource,
					String.format(resource.getString(R.string.forecast),
							sdf.format(date)));
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(mContext);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	private Bitmap setIcon(String url) {
		Bitmap bitmap = null;
		try {
			String[] split = url.split("/");
			if (split[split.length - 1].equals("01.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather01);
			if (split[split.length - 1].equals("02.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather02);
			if (split[split.length - 1].equals("03.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather03);
			if (split[split.length - 1].equals("04.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather04);
			if (split[split.length - 1].equals("05.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather05);
			if (split[split.length - 1].equals("06.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather06);
			if (split[split.length - 1].equals("07.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather07);
			if (split[split.length - 1].equals("08.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather08);
			if (split[split.length - 1].equals("09.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather09);
			if (split[split.length - 1].equals("10.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather10);
			if (split[split.length - 1].equals("11.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather11);
			if (split[split.length - 1].equals("12.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather12);
			if (split[split.length - 1].equals("13.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather13);
			if (split[split.length - 1].equals("14.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather14);
			if (split[split.length - 1].equals("15.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather15);
			if (split[split.length - 1].equals("16.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather16);
			if (split[split.length - 1].equals("17.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather17);
			if (split[split.length - 1].equals("18.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather18);
			if (split[split.length - 1].equals("19.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather19);
			if (split[split.length - 1].equals("20.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather20);
			if (split[split.length - 1].equals("21.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather21);
			if (split[split.length - 1].equals("22.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather22);
			if (split[split.length - 1].equals("23.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather23);
			if (split[split.length - 1].equals("24.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather24);
			if (split[split.length - 1].equals("25.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather25);
			if (split[split.length - 1].equals("26.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather26);
			if (split[split.length - 1].equals("27.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather27);
			if (split[split.length - 1].equals("28.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather28);
			if (split[split.length - 1].equals("29.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather29);
			if (split[split.length - 1].equals("30.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather30);
			if (split[split.length - 1].equals("01_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather01_n);
			if (split[split.length - 1].equals("02_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather02_n);
			if (split[split.length - 1].equals("03_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather03_n);
			if (split[split.length - 1].equals("04_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather04_n);
			if (split[split.length - 1].equals("05_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather05_n);
			if (split[split.length - 1].equals("06_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather06_n);
			if (split[split.length - 1].equals("07_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather07_n);
			if (split[split.length - 1].equals("08_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather08_n);
			if (split[split.length - 1].equals("09_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather09_n);
			if (split[split.length - 1].equals("10_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather10_n);
			if (split[split.length - 1].equals("11_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather11_n);
			if (split[split.length - 1].equals("12_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather12_n);
			if (split[split.length - 1].equals("13_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather13_n);
			if (split[split.length - 1].equals("14_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather14_n);
			if (split[split.length - 1].equals("15_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather15_n);
			if (split[split.length - 1].equals("16_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather16_n);
			if (split[split.length - 1].equals("17_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather17_n);
			if (split[split.length - 1].equals("18_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather18_n);
			if (split[split.length - 1].equals("19_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather19_n);
			if (split[split.length - 1].equals("20_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather20_n);
			if (split[split.length - 1].equals("21_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather21_n);
			if (split[split.length - 1].equals("22_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather22_n);
			if (split[split.length - 1].equals("23_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather23_n);
			if (split[split.length - 1].equals("24_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather24_n);
			if (split[split.length - 1].equals("25_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather25_n);
			if (split[split.length - 1].equals("26_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather26_n);
			if (split[split.length - 1].equals("27_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather27_n);
			if (split[split.length - 1].equals("28_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather28_n);
			if (split[split.length - 1].equals("29_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather29_n);
			if (split[split.length - 1].equals("30_n.gif"))
				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.weather30_n);
			Log.v(TAG, "setIcon - " + split[split.length - 1]);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
		return bitmap;
	}
}
