/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.ics.sticky;

import jp.kumamon.widgets.lib.*;
import jp.kumamon.widgets.R;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class StickyWidgetService extends RemoteViewsService {
	private static final String TAG = "StickyWidgetService";
	public static final String EXTRA_ITEM = "jp.kumamon.widgets.ics.sticky.EXTRA_ITEM";

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.i(TAG, "onGetViewFactory");
		Bundle extras = intent.getExtras();
		int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		if (extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		return new StickyRemoteViewsFactory(this, appWidgetId);
	}

	class StickyRemoteViewsFactory implements
			RemoteViewsService.RemoteViewsFactory {
		private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		private Context mContext;
		private String[] mComments = {};

		public StickyRemoteViewsFactory(Context context, int appWidgetId) {
			try {
				mContext = context.getApplicationContext();
				mAppWidgetId = appWidgetId;
			} catch (Exception e) {
				ExceptionLog.Log(TAG, e);
			}
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
				String comment = hash.get(KumamonStickyWidget.COMMENT,
						String.valueOf(mAppWidgetId), "");
				if (comment != null && comment.length() > 0)
					mComments = comment.split("\n");

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
			String comment = hash.get(KumamonStickyWidget.COMMENT,
					String.valueOf(mAppWidgetId), "");
			mComments = comment.split("\n");
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
			return mComments.length;
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
			int[] backs = { R.drawable.back0, R.drawable.back1,
					R.drawable.back2, R.drawable.back3, R.drawable.back4,
					R.drawable.back5, R.drawable.back6, R.drawable.back7,
					R.drawable.back8, R.drawable.back9, };
			// position will always range from 0 to getCount() - 1.

			// We construct a remote views item based on our widget item xml
			// file, and set the
			// text based on the position.
			RemoteViews remoteviews = new RemoteViews(
					mContext.getPackageName(), R.layout.ics_sticky_item);
			try {
				if (mComments.length <= 0)
					return remoteviews;
				remoteviews.setTextViewText(R.id.textView, mComments[position]);
				StaticHash hash = new StaticHash(mContext);
				int back = hash.get(KumamonStickyWidget.BACK,
						String.valueOf(mAppWidgetId), 0);
				remoteviews.setImageViewResource(R.id.imageView1, backs[back]);

				// Next, we set a fill-intent which will be used to fill-in the
				// pending intent template
				// which is set on the collection view in StackWidgetProvider.
				Bundle extras = new Bundle();
				extras.putInt(EXTRA_ITEM, position);
				Intent fillInIntent = new Intent();
				fillInIntent.putExtras(extras);
				remoteviews.setOnClickFillInIntent(R.id.sticky_item,
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

		public boolean hasStableIds() {
			return true;
		}
	}
}
