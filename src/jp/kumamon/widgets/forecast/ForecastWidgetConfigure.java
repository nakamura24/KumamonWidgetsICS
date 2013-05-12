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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.LinearLayout.LayoutParams;

public class ForecastWidgetConfigure extends Activity {
	private static final String TAG = "WidgetConfiguration";
	public static final String CONFIG_DONE = "jp.kumamon.widgets.forecast.CONFIG_DONE";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private CityEntries cityEntries = null;
	private int mPref = 16 - 1;
	private int mCity = 0;
	private int mId = 63;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		try {
			// setResult(RESULT_CANCELED);
			// AppWidgetID の取得
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mAppWidgetId = extras.getInt(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				Log.d(TAG, "mAppWidgetId=" + String.valueOf(mAppWidgetId));
			}

			StaticHash hash = new StaticHash(this);
			int id = mId;
			if (hash.contains(KumamonForecastWidget.LASTLOCATEID,
					String.valueOf(mAppWidgetId))) {
				id = hash.get(KumamonForecastWidget.LASTLOCATEID,
						String.valueOf(mAppWidgetId), 63);
			}
			if (cityEntries == null)
				cityEntries = new CityEntries(this);
			cityEntries.getLocation(id);
			mPref = cityEntries.mPrefId - 1;
			mCity = cityEntries.mCityIid;

			setContentView(R.layout.forecast_widget_configure);
			getWindow().setLayout(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);

			Spinner spinner1 = (Spinner) findViewById(R.id.spinner_pref);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item);
			for (int i = 0; i < cityEntries.getPrefNames().size(); i++) {
				adapter.add(cityEntries.getPrefNames().get(i));
			}
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner1.setAdapter(adapter);
			// Spinner のアイテムが選択された時に呼び出されるコールバックを登録
			spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				// アイテムが選択された時の動作
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Spinner を取得
					Spinner spinner = (Spinner) parent;
					// 選択されたアイテムのテキストを取得
					mPref = spinner.getSelectedItemPosition();
					setSpinner2(cityEntries.getPrefIds().get(mPref));
					Log.i(TAG, "mPref=" + String.valueOf(mPref));
				}

				// 何も選択されなかった時の動作
				public void onNothingSelected(AdapterView<?> parent) {
					mPref = 0;
				}
			});
			spinner1.setSelection(mPref);
			setSpinner2(cityEntries.getPrefIds().get(mPref));
			Log.i(TAG, "onCreate end");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	private void setSpinner2(int pref) {
		Log.i(TAG, "setSpinner2");
		try {
			Spinner spinner2 = (Spinner) findViewById(R.id.spinner_city);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item);
			for (int i = 0; i < cityEntries.getNames(pref).size(); i++) {
				adapter.add(cityEntries.getNames(pref).get(i));
			}
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner2.setAdapter(adapter);
			mCity = 0;
			// Spinner のアイテムが選択された時に呼び出されるコールバックを登録
			spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				// アイテムが選択された時の動作
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Spinner を取得
					Spinner spinner = (Spinner) parent;
					// 選択されたアイテムのテキストを取得
					mCity = spinner.getSelectedItemPosition();
					mId = cityEntries.getIds(
							cityEntries.getPrefIds().get(mPref)).get(mCity);
					Log.i(TAG, "mId=" + String.valueOf(mId));
				}

				// 何も選択されなかった時の動作
				public void onNothingSelected(AdapterView<?> parent) {
					mCity = 0;
					mId = cityEntries.getIds(
							cityEntries.getPrefIds().get(mPref)).get(mCity);
				}
			});
			spinner2.setSelection(mCity);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	// Button の onClick で実装
	public void onOKButtonClick(View v) {
		try {
			Log.i(TAG, "onOKButtonClick");
			Intent intent = new Intent(this, KumamonForecastWidget.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			intent.putExtra(KumamonForecastWidget.LOCATEID, mId);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			intent.setAction(CONFIG_DONE);
			sendBroadcast(intent);
			setResult(RESULT_OK, intent);
			finish();
			Log.i(TAG, "onOKButtonClick End");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
