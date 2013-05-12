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

import jp.kumamon.widgets.lib.*;
import jp.kumamon.widgets.R;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ForecastWidgetService extends RemoteViewsService {
	private static final String TAG = "ForecastWidgetService";
	public static final String EXTRA_ITEM = "jp.kumamonwidgets.forecast.EXTRA_ITEM";
	public static final String ONLYFORECAST = "jp.kumamonwidgets.forecast.ONLYFORECAST";

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.i(TAG, "onGetViewFactory");
		return new ForecastRemoteViewsFactory(this.getApplicationContext(),
				intent);
	}

	class ForecastRemoteViewsFactory implements
			RemoteViewsService.RemoteViewsFactory {
		private Context mContext;
		private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		private ArrayList<String> mForecasts = new ArrayList<String>();
		String mHeader;
		String mFooter;

		public ForecastRemoteViewsFactory(Context context, Intent intent) {
			mContext = context.getApplicationContext();
			mAppWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		@Override
		public void onCreate() {
			Log.i(TAG, "onCreate");
			try {
				// In onCreate() you setup any connections / cursors to your
				// data source. Heavy lifting,
				// for example downloading or creating content etc, should be
				// deferred to onDataSetChanged()
				// or getViewAt(). Taking more than 20 seconds in this call will
				// result in an ANR.
				StaticHash hash = new StaticHash(mContext);
				mHeader = hash.get(String.valueOf(mAppWidgetId),
						ForecastTask.TITLE_HEADER, "");
				mFooter = hash.get(String.valueOf(mAppWidgetId),
						ForecastTask.TITLE_FOOTER, "");
				int size = hash.get(String.valueOf(mAppWidgetId),
						ForecastTask.TITLE_COUNT, 0);
				mForecasts.clear();
				for (int i = 0; i < size; i++) {
					mForecasts.add(hash.get(String.valueOf(mAppWidgetId),
							ForecastTask.TITLE_NO + String.valueOf(i), ""));
				}

				// We sleep for 3 seconds here to show how the empty view
				// appears in the interim.
				// The empty view is set in the StackWidgetProvider and should
				// be a sibling of the
				// collection view.
				Thread.sleep(3000);
			} catch (Exception e) {
				ExceptionLog.Log(TAG, e);
			}
		}

		@Override
		public void onDataSetChanged() {
			Log.i(TAG, "getViewAt");
			// This is triggered when you call AppWidgetManager
			// notifyAppWidgetViewDataChanged
			// on the collection view corresponding to this factory. You can do
			// heaving lifting in
			// here, synchronously. For example, if you need to process an
			// image, fetch something
			// from the network, etc., it is ok to do it here, synchronously.
			// The widget will remain
			// in its current state while work is being done here, so you don't
			// need to worry about
			// locking up the widget.
			StaticHash hash = new StaticHash(mContext);
			mHeader = hash.get(String.valueOf(mAppWidgetId),
					ForecastTask.TITLE_HEADER, "");
			mFooter = hash.get(String.valueOf(mAppWidgetId),
					ForecastTask.TITLE_FOOTER, "");
			int size = hash.get(String.valueOf(mAppWidgetId),
					ForecastTask.TITLE_COUNT, 0);
			mForecasts.clear();
			for (int i = 0; i < size; i++) {
				mForecasts.add(hash.get(String.valueOf(mAppWidgetId),
						ForecastTask.TITLE_NO + String.valueOf(i), ""));
			}
		}

		@Override
		public void onDestroy() {
			Log.i(TAG, "onDestroy");
			// In onDestroy() you should tear down anything that was setup for
			// your data source,
			// eg. cursors, connections, etc.
		}

		@Override
		public int getCount() {
			return mForecasts.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public RemoteViews getLoadingView() {
			// You can create a custom loading view (for instance when
			// getViewAt() is slow.) If you
			// return null here, you will get the default loading view.
			return null;
		}

		@Override
		public RemoteViews getViewAt(int position) {
			Log.i(TAG, "getViewAt");
			// position will always range from 0 to getCount() - 1.

			// We construct a remote views item based on our widget item xml
			// file, and set the
			// text based on the position.
			RemoteViews remoteviews = new RemoteViews(
					mContext.getPackageName(), R.layout.ics_forecast_item);
			try {
				if (mForecasts.size() <= 0)
					return remoteviews;
				String[] forcast = mForecasts.get(position).split("[ /]");
				if (forcast.length >= 4) {
					remoteviews.setTextViewText(R.id.ics_textView_date,
							forcast[0]);
					remoteviews.setTextViewText(R.id.ics_textView_forecast,
							forcast[1]);
					remoteviews.setTextViewText(R.id.ics_textView_max,
							forcast[2]);
					remoteviews.setTextViewText(R.id.ics_textView_min,
							forcast[3]);
				}
				remoteviews.setTextViewText(R.id.ics_textView_title, mHeader);
				remoteviews
						.setTextViewText(R.id.ics_textView_resource, mFooter);

				// Next, we set a fill-intent which will be used to fill-in the
				// pending intent template
				// which is set on the collection view in StackWidgetProvider.
				Bundle extras = new Bundle();
				extras.putInt(EXTRA_ITEM, position);
				Intent fillInIntent = new Intent();
				fillInIntent.putExtras(extras);
				remoteviews.setOnClickFillInIntent(R.id.ics_forecast_item,
						fillInIntent);

				// You can do heaving lifting in here, synchronously. For
				// example, if you need to
				// process an image, fetch something from the network, etc., it
				// is ok to do it here,
				// synchronously. A loading view will show up in lieu of the
				// actual contents in the
				// interim.
				Log.d(TAG, "Loading view=" + String.valueOf(position));
				Thread.sleep(500);
			} catch (Exception e) {
				ExceptionLog.Log(TAG, e);
			}

			// Return the remote views object.
			return remoteviews;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}
