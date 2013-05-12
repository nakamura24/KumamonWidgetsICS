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

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ForecastWeeklyDialog extends Activity {
	private static final String TAG = "ForecastWeeklyDialog";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		try {
			ArrayList<String> titles = new ArrayList<String>();
			// setResult(RESULT_CANCELED);
			// AppWidgetID の取得
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mAppWidgetId = extras.getInt(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				StaticHash hash = new StaticHash(this);
				int title_count = hash.get(String.valueOf(mAppWidgetId),
						ForecastTask.TITLE_COUNT, 0);
				for (int i = 0; i < title_count; i++) {
					titles.add(hash.get(String.valueOf(mAppWidgetId),
							ForecastTask.TITLE_NO + String.valueOf(i), ""));
				}
				setContentView(R.layout.forecast_weekly_dialog);
				ListView listView1 = (ListView) findViewById(R.id.listView_weekly);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1);
				for (int i = 0; i < titles.size(); i++) {
					adapter.add(titles.get(i));
				}
				listView1.setAdapter(adapter);
				Log.d(TAG, "mAppWidgetId=" + String.valueOf(mAppWidgetId));
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
			Intent resultIntent = new Intent();
			resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultIntent);
			finish();
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
