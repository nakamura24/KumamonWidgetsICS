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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class CalendarEvent {
	private static final String TAG = "CalendarEvent";
	private boolean allday = false; // カレンダーを検索する期間(日)
	private int Period = 7 * 2; // カレンダーを検索する期間(日)
	// Parameters for quering the calendar
	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	private static final String[] CALENDAR_PROJECTION = new String[] {
			Calendars._ID, // 0
			Calendars.ACCOUNT_NAME, // 1
			Calendars.CALENDAR_DISPLAY_NAME // 2
	};

	private static final String[] EVENT_PROJECTION = new String[] { Events._ID, // 0
			Events.DTSTART, // 1
			Events.DTEND, // 2
			Events.TITLE, // 3
			Events.DESCRIPTION, // 4
			Events.EVENT_LOCATION, // 5
			Events.ALL_DAY // 6
	};

	// The indices for the projection array above.
	private static final int PROJECTION_DTSTART_ID = 0;
	private static final int PROJECTION_DTSTART_INDEX = 1;
	private static final int PROJECTION_DTEND_INDEX = 2;
	private static final int PROJECTION_TITLE_INDEX = 3;
	private static final int PROJECTION_DESCRIPTION_INDEX = 4;
	private static final int PROJECTION_EVENT_LOCATION = 5;
	private static final int PROJECTION_ALL_DAY = 6;

	public ArrayList<CalendarEventData> mEvents = new ArrayList<CalendarEventData>();

	public void queryCalendar(Context context) {
		Log.i(TAG, "queryCalendar");
		try {
			context = context.getApplicationContext();
			StaticHash configure = new StaticHash(context);
			allday = configure.get(KumamonNoticeWidget.ConfigureAllDay, false);
			Period = configure.get(KumamonNoticeWidget.ConfigurePeriod, 14);
			// Run query
			Cursor cur = null;
			ContentResolver cr = context.getContentResolver();
			Uri uri = Calendars.CONTENT_URI;
			String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
					+ Calendars.ACCOUNT_TYPE + " = ?))";

			// Replace this with your own user and account type
			Resources res = context.getResources();
			String google_account = res.getString(R.string.google_account);
			Account[] accounts = AccountManager.get(context).getAccountsByType(
					"com.google");
			if (accounts.length > 0) {
				google_account = accounts[0].name;
			}
			Log.d(TAG, google_account);

			String[] selectionArgs = new String[] { google_account,
					"com.google" };
			// Submit the query and get a Cursor object back.
			cur = cr.query(uri, CALENDAR_PROJECTION, selection, selectionArgs,
					null);
			Log.d(TAG, "CALENDAR_PROJECTION=" + String.valueOf(cur.getCount()));
			while (cur.getCount() == 0) {
				return;
			}

			// Use the cursor to step through the returned records
			if (cur.moveToNext()) {
				mEvents.clear();
				queryAllEvent(cr);
				queryEvent(cr);
				Object[] temp = mEvents.toArray(); // 配列に変換
				Arrays.sort(temp, new DateComparator());
				mEvents.clear();
				for (int i = 0; i < temp.length; i++) {
					mEvents.add((CalendarEventData) temp[i]);
					Calendar cal_dtstart = Calendar.getInstance(); // 現在日時を取得
					cal_dtstart
							.setTimeInMillis(((CalendarEventData) temp[i]).dtstart);
					Log.d(TAG,
							"sorted - "
									+ cal_dtstart.getTime().toString()
									+ " "
									+ ((CalendarEventData) temp[i]).title
									+ " "
									+ String.valueOf(((CalendarEventData) temp[i]).all_day));
				}
			}
		} catch (Exception e) { // (2)
			Log.e(TAG, e.getMessage());
		}
	}

	private void queryEvent(ContentResolver cr) {
		Log.i(TAG, "queryEvent");
		try {
			// イベント取得用URLをセット
			Uri eventUri = Events.CONTENT_URI;

			// 開始日付を取得
			Calendar cal = Calendar.getInstance(); // 現在日時を取得
			if (allday) {
				cal.set(Calendar.HOUR_OF_DAY, 0); // 時を0でクリア
			}
			cal.set(Calendar.SECOND, 0); // 秒を0でクリア
			cal.set(Calendar.MILLISECOND, 0); // ミリ秒を0でクリア
			Long startMillis = cal.getTimeInMillis(); // Long値へ変換

			// 終了日付を取得
			cal.add(Calendar.DATE, Period);
			Long stopMillis = cal.getTimeInMillis(); // Long値へ変換
			// 開始日付と終了日付をパラメータ配列にセット
			String[] params = new String[] { "" + startMillis, "" + stopMillis };

			Cursor eventCursor = cr.query(eventUri, EVENT_PROJECTION,
					"allday = 0 and dtstart >= ? and dtstart < ?", params,
					"dtstart asc");
			Log.d(TAG,
					"EVENT_PROJECTION="
							+ String.valueOf(eventCursor.getCount()));

			while (eventCursor.moveToNext()) {
				// Get the field values
				CalendarEventData event = new CalendarEventData();
				event.id = eventCursor.getLong(PROJECTION_DTSTART_ID);
				event.dtstart = eventCursor.getLong(PROJECTION_DTSTART_INDEX);
				event.dtend = eventCursor.getLong(PROJECTION_DTEND_INDEX);
				event.title = eventCursor.getString(PROJECTION_TITLE_INDEX);
				event.description = eventCursor
						.getString(PROJECTION_DESCRIPTION_INDEX);
				event.location = eventCursor
						.getString(PROJECTION_EVENT_LOCATION);
				event.all_day = eventCursor.getInt(PROJECTION_ALL_DAY);
				mEvents.add(event);
			}
		} catch (Exception e) { // (2)
			Log.e(TAG, e.getMessage());
		}
	}

	private void queryAllEvent(ContentResolver cr) {
		Log.i(TAG, "queryAllEvent");
		try {
			// イベント取得用URLをセット
			Uri eventUri = Events.CONTENT_URI;

			// 開始日付を取得
			Calendar cal = Calendar.getInstance(); // 現在日時を取得
			cal.set(Calendar.HOUR_OF_DAY, 0); // 時を0でクリア
			cal.set(Calendar.SECOND, 0); // 秒を0でクリア
			cal.set(Calendar.MILLISECOND, 0); // ミリ秒を0でクリア
			Long startMillis = cal.getTimeInMillis(); // Long値へ変換

			// 終了日付を取得
			cal.add(Calendar.DATE, Period);
			Long stopMillis = cal.getTimeInMillis(); // Long値へ変換
			// 開始日付と終了日付をパラメータ配列にセット
			String[] params = new String[] { "" + startMillis, "" + stopMillis };

			Cursor eventCursor = cr.query(eventUri, EVENT_PROJECTION,
					"allday > 0 and dtstart >= ? and dtstart < ?", params,
					"dtstart asc");
			Log.d(TAG,
					"EVENT_PROJECTION="
							+ String.valueOf(eventCursor.getCount()));

			while (eventCursor.moveToNext()) {
				// Get the field values
				CalendarEventData event = new CalendarEventData();
				event.id = eventCursor.getLong(PROJECTION_DTSTART_ID);
				event.dtstart = eventCursor.getLong(PROJECTION_DTSTART_INDEX) - 9 * 3600000;
				event.dtend = eventCursor.getLong(PROJECTION_DTEND_INDEX);
				event.title = eventCursor.getString(PROJECTION_TITLE_INDEX);
				event.description = eventCursor
						.getString(PROJECTION_DESCRIPTION_INDEX);
				event.location = eventCursor
						.getString(PROJECTION_EVENT_LOCATION);
				event.all_day = eventCursor.getInt(PROJECTION_ALL_DAY);
				mEvents.add(event);
			}
		} catch (Exception e) { // (2)
			Log.e(TAG, e.getMessage());
		}
	}
}
