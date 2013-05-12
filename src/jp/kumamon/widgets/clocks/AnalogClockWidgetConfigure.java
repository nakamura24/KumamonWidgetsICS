/*
 * Copyright (C) 2012 M.Nakamura
 *
 * This software is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 2.1 Japan License.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://creativecommons.org/licenses/by-nc-sa/2.1/jp/legalcode
 */
package jp.kumamon.widgets.clocks;

import jp.kumamon.widgets.lib.*;
import jp.kumamon.widgets.R;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class AnalogClockWidgetConfigure extends Activity {
	private static final String TAG = "AnalogClockWidgetConfigure";
	public static final String CONFIG_DONE = "jp.kumamon.widgets.analogclock.CONFIG_DONE";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private boolean ampm = false;
	private int hour = 0;
	private int minute = 0;
	private boolean set = false;
	private boolean repeat = false;

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
				StaticHash hash = new StaticHash(this);
				ampm = hash.get(KumamonAnalogClockWidget.AMPM,
						String.valueOf(mAppWidgetId), false);
				hour = hash.get(KumamonAnalogClockWidget.HOUR,
						String.valueOf(mAppWidgetId), 0);
				minute = hash.get(KumamonAnalogClockWidget.MINUTE,
						String.valueOf(mAppWidgetId), 0);
				minute /= 5;
				set = hash.get(KumamonAnalogClockWidget.SET,
						String.valueOf(mAppWidgetId), false);
				repeat = hash.get(KumamonAnalogClockWidget.REPEAT,
						String.valueOf(mAppWidgetId), false);
			}
			setContentView(R.layout.clock_widget_configure);

			RadioButton radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
			radioButton1.setChecked(!ampm);
			RadioButton radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
			radioButton2.setChecked(ampm);

			Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
			spinner1.setSelection(hour);
			// Spinner のアイテムが選択された時に呼び出されるコールバックを登録
			spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				// アイテムが選択された時の動作
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Spinner を取得
					Spinner spinner = (Spinner) parent;
					// 選択されたアイテムのテキストを取得
					hour = spinner.getSelectedItemPosition();
					Log.d(TAG, "hour=" + String.valueOf(hour));
				}

				// 何も選択されなかった時の動作
				public void onNothingSelected(AdapterView<?> parent) {
					hour = 0;
				}
			});

			Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
			spinner2.setSelection(minute);
			// Spinner のアイテムが選択された時に呼び出されるコールバックを登録
			spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				// アイテムが選択された時の動作
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Spinner を取得
					Spinner spinner = (Spinner) parent;
					// 選択されたアイテムのテキストを取得
					minute = spinner.getSelectedItemPosition();
					Log.d(TAG, "minute=" + String.valueOf(minute));
				}

				// 何も選択されなかった時の動作
				public void onNothingSelected(AdapterView<?> parent) {
					minute = 0;
				}
			});
			ToggleButton toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
			toggleButton1.setChecked(set);
			CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
			if (set) {
				checkBox1.setVisibility(View.VISIBLE);
			} else {
				checkBox1.setVisibility(View.INVISIBLE);
				repeat = false;
			}
			checkBox1.setChecked(repeat);

			Log.i(TAG, "onCreate end");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	// Button の onClick で実装
	public void onToggleButtonClick(View v) {
		try {
			ToggleButton toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
			set = toggleButton1.isChecked();
			CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
			if (set) {
				checkBox1.setVisibility(View.VISIBLE);
			} else {
				checkBox1.setVisibility(View.INVISIBLE);
				repeat = false;
				checkBox1.setChecked(repeat);
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	// Button の onClick で実装
	public void onOKButtonClick(View v) {
		try {
			Log.i(TAG, "onOKButtonClick");
			RadioButton radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
			ampm = radioButton2.isChecked();
			CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
			repeat = checkBox1.isChecked();
			Intent intent = new Intent(this, KumamonAnalogClockWidget.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			intent.putExtra(KumamonAnalogClockWidget.AMPM, ampm);
			intent.putExtra(KumamonAnalogClockWidget.HOUR, hour);
			intent.putExtra(KumamonAnalogClockWidget.MINUTE, minute * 5);
			intent.putExtra(KumamonAnalogClockWidget.SET, set);
			intent.putExtra(KumamonAnalogClockWidget.REPEAT, repeat);
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
