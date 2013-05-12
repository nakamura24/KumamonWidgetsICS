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

import jp.kumamon.widgets.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class NoticeWidgetService extends RemoteViewsService {
	private static final String TAG = "NoticeWidgetService";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	public static CalendarEvent mCalendarEvent = new CalendarEvent();
	public static int mPositon;

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.i(TAG, "onGetViewFactory");
		Bundle extras = intent.getExtras();
		mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		return new NoticeRemoteViewsFactory(this.getApplicationContext(),
				intent);
	}

	class NoticeRemoteViewsFactory implements
			RemoteViewsService.RemoteViewsFactory {
		private Context mContext;

		public NoticeRemoteViewsFactory(Context context, Intent intent) {
			mContext = context.getApplicationContext();
		}

		@Override
		public void onCreate() {
			Log.i(TAG, "onCreate");
			// In onCreate() you setup any connections / cursors to your data
			// source. Heavy lifting,
			// for example downloading or creating content etc, should be
			// deferred to onDataSetChanged()
			// or getViewAt(). Taking more than 20 seconds in this call will
			// result in an ANR.
			mCalendarEvent.queryCalendar(mContext);

			// We sleep for 3 seconds here to show how the empty view appears in
			// the interim.
			// The empty view is set in the StackWidgetProvider and should be a
			// sibling of the
			// collection view.
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
			return mCalendarEvent.mEvents.size();
		}

		@Override
		public RemoteViews getViewAt(int position) {
			Log.i(TAG, "getViewAt");
			// position will always range from 0 to getCount() - 1.

			// We construct a remote views item based on our widget item xml
			// file, and set the
			// text based on the position.
			RemoteViews remoteviews = new RemoteViews(
					mContext.getPackageName(), R.layout.notice_item);
			if (mCalendarEvent.mEvents.get(position).all_day == 0) {
				SimpleDateFormat sdf1 = new SimpleDateFormat("MM/d(EEE) H:mm",
						Locale.JAPANESE);
				SimpleDateFormat sdf2 = new SimpleDateFormat("～H:mm",
						Locale.JAPANESE);
				remoteviews.setTextViewText(
						R.id.textView_date,
						sdf1.format(new Date(mCalendarEvent.mEvents
								.get(position).dtstart))
								+ sdf2.format(new Date(mCalendarEvent.mEvents
										.get(position).dtend)));
			} else {
				SimpleDateFormat sdf1 = new SimpleDateFormat("MM/d(EEE)",
						Locale.JAPANESE);
				remoteviews.setTextViewText(R.id.textView_date, sdf1
						.format(new Date(
								mCalendarEvent.mEvents.get(position).dtstart)));
			}
			remoteviews.setTextViewText(R.id.textView_title,
					mCalendarEvent.mEvents.get(position).title);

			// Next, we set a fill-intent which will be used to fill-in the
			// pending intent template
			// which is set on the collection view in StackWidgetProvider.
			Bundle extras = new Bundle();
			extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			extras.putInt(KumamonNoticeWidget.EXTRA_ITEM, position);
			Intent fillInIntent = new Intent();
			fillInIntent.putExtras(extras);
			remoteviews.setOnClickFillInIntent(R.id.notice_item, fillInIntent);

			/*
			 * Intent dialogIntent = new Intent(mContext,
			 * NoticeEventDialog.class); dialogIntent.putExtras(extras);
			 * PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
			 * mAppWidgetId, dialogIntent, 0);
			 * remoteviews.setOnClickPendingIntent(R.id.notice_item,
			 * pendingIntent);
			 */

			// You can do heaving lifting in here, synchronously. For example,
			// if you need to
			// process an image, fetch something from the network, etc., it is
			// ok to do it here,
			// synchronously. A loading view will show up in lieu of the actual
			// contents in the
			// interim.
			try {
				System.out.println("Loading view " + position);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Return the remote views object.
			return remoteviews;
		}

		@Override
		public RemoteViews getLoadingView() {
			// You can create a custom loading view (for instance when
			// getViewAt() is slow.) If you
			// return null here, you will get the default loading view.
			return null;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void onDataSetChanged() {
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
			mCalendarEvent.queryCalendar(mContext);
		}
	}
}
