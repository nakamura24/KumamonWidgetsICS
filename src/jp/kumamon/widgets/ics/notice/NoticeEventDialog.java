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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.kumamon.widgets.R;
import jp.kumamon.widgets.lib.*;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class NoticeEventDialog extends Activity {
	private static final String TAG = "NoticeEventDialog";
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		try {
			// AppWidgetID の取得
			Intent intent = getIntent();
			mAppWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			int position = KumamonNoticeWidget.mPosition;
			Log.d(TAG, "appWidgetId=" + String.valueOf(mAppWidgetId));
			Log.d(TAG, "position=" + String.valueOf(position));

			if (NoticeWidgetService.mCalendarEvent.mEvents.size() > 0) {
				setContentView(R.layout.notice_event_dialog);

				TextView textView_title = (TextView) findViewById(R.id.textView_title);
				textView_title
						.setText(NoticeWidgetService.mCalendarEvent.mEvents
								.get(position).title);
				TextView textView_date = (TextView) findViewById(R.id.textView_date);
				if (NoticeWidgetService.mCalendarEvent.mEvents.get(position).all_day == 0) {
					SimpleDateFormat sdf1 = new SimpleDateFormat(
							"MM/d(EEE) H:mm", Locale.JAPANESE);
					SimpleDateFormat sdf2 = new SimpleDateFormat("～H:mm",
							Locale.JAPANESE);
					textView_date.setText(sdf1.format(new Date(
							NoticeWidgetService.mCalendarEvent.mEvents
									.get(position).dtstart))
							+ sdf2.format(new Date(
									NoticeWidgetService.mCalendarEvent.mEvents
											.get(position).dtend)));
				} else {
					SimpleDateFormat sdf1 = new SimpleDateFormat("MM/d(EEE)",
							Locale.JAPANESE);
					textView_date.setText(sdf1.format(new Date(
							NoticeWidgetService.mCalendarEvent.mEvents
									.get(position).dtstart)));
				}
				TextView textView_location = (TextView) findViewById(R.id.textView_location);
				textView_location
						.setText(NoticeWidgetService.mCalendarEvent.mEvents
								.get(position).location);
				TextView textView_description = (TextView) findViewById(R.id.textView_description);
				textView_description
						.setText(NoticeWidgetService.mCalendarEvent.mEvents
								.get(position).description);
			}
			Log.i(TAG, "onCreate end");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	// Button の onClick で実装
	public void onOKButtonClick(View v) {
		try {
			Log.i(TAG, "onOKButtonClick");
			// Result をセットして終了
			Intent intent = new Intent(this, KumamonNoticeWidget.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			intent.setAction(NoticeWidgetConfigure.CONFIG_DONE);
			sendBroadcast(intent);
			finish();
			Log.i(TAG, "onOKButtonClick End");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

}
