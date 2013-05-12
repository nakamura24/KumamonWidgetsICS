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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class NoticeWidgetConfigure extends Activity {
	private static final String TAG = "WidgetConfiguration";
	public static final String CONFIG_DONE = "jp.kumamonclockwidget.CONFIG_DONE";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		try {
			// AppWidgetID の取得
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mAppWidgetId = extras.getInt(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				Log.d(TAG, "mAppWidgetId=" + String.valueOf(mAppWidgetId));
				setContentView(R.layout.notice_widget_configure);

				StaticHash hash = new StaticHash(this);
				boolean allday = hash.get(KumamonNoticeWidget.ConfigureAllDay,
						false);
				int period = hash.get(KumamonNoticeWidget.ConfigurePeriod, 14);

				CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox_allday);
				checkBox1.setChecked(allday);

				EditText editText1 = (EditText) findViewById(R.id.editText_priod);
				editText1.setText(String.valueOf(period));
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
			CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox_allday);
			EditText editText1 = (EditText) findViewById(R.id.editText_priod);
			StaticHash hash = new StaticHash(this);
			hash.put(KumamonNoticeWidget.ConfigureAllDay, checkBox1.isChecked());
			hash.put(KumamonNoticeWidget.ConfigurePeriod,
					Integer.parseInt(editText1.getText().toString()));
			Intent intent = new Intent(this, KumamonNoticeWidget.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			intent.setAction(CONFIG_DONE);
			sendBroadcast(intent);
			finish();
			Log.i(TAG, "onOKButtonClick End");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
