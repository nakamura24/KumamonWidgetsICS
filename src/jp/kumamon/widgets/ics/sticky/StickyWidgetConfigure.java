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

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.LinearLayout.LayoutParams;

public class StickyWidgetConfigure extends Activity {
	private static final String TAG = "WidgetConfiguration";
	public static final String CONFIG_DONE = "jp.kumamon.widgets.ics.sticky.CONFIG_DONE";
	private static final int REQUEST_CODE = 1000;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private String mComment = "";
	private int mIcon = 0;
	private int mBack = 0;

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
				StaticHash hash = new StaticHash(this);
				mComment = hash.get(KumamonStickyWidget.COMMENT,
						String.valueOf(mAppWidgetId), "");
				mIcon = hash.get(KumamonStickyWidget.ICON,
						String.valueOf(mAppWidgetId), 0);
				mBack = hash.get(KumamonStickyWidget.BACK,
						String.valueOf(mAppWidgetId), 0);
				Log.d(TAG, "mAppWidgetId=" + String.valueOf(mAppWidgetId));
				Log.d(TAG,
						"mComment=" + mComment + "mIcon="
								+ String.valueOf(mIcon) + "mBack="
								+ String.valueOf(mBack));
			}

			setContentView(R.layout.ics_sticky_widget_configure);
			getWindow().setLayout(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);

			EditText editText = (EditText) findViewById(R.id.editText);
			editText.setText(mComment);

			Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
			spinner1.setSelection(mIcon);
			// Spinner のアイテムが選択された時に呼び出されるコールバックを登録
			spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				// アイテムが選択された時の動作
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Spinner を取得
					Spinner spinner = (Spinner) parent;
					// 選択されたアイテムのテキストを取得
					mIcon = spinner.getSelectedItemPosition();
				}

				// 何も選択されなかった時の動作
				public void onNothingSelected(AdapterView<?> parent) {
					mIcon = 0;
				}
			});
			Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
			spinner2.setSelection(mBack);
			// Spinner のアイテムが選択された時に呼び出されるコールバックを登録
			spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				// アイテムが選択された時の動作
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// Spinner を取得
					Spinner spinner = (Spinner) parent;
					// 選択されたアイテムのテキストを取得
					mBack = spinner.getSelectedItemPosition();
				}

				// 何も選択されなかった時の動作
				public void onNothingSelected(AdapterView<?> parent) {
					mBack = 0;
				}
			});
			if (Build.VERSION.SDK_INT >= 14) {
				Button button = (Button) findViewById(R.id.button1);
				button.setVisibility(View.VISIBLE);
			}
			Log.i(TAG, "onCreate end");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	// Button の onClick で実装
	public void onCleanButtonClick(View v) {
		try {
			Log.i(TAG, "onCleanButtonClick");
			EditText editText = (EditText) findViewById(R.id.editText);
			editText.setText("");
			Log.i(TAG, "onCleanButtonClick End");
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	// Button の onClick で実装
	public void onSpeechButtonClick(View v) {
		try {
			Log.i(TAG, "onSpeechButtonClick");
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			startActivityForResult(intent, REQUEST_CODE);
			Log.i(TAG, "onSpeechButtonClick End");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			// requestCodeを確認して、自分が発行したIntentの結果であれば処理を行う
			if ((REQUEST_CODE == requestCode) && (RESULT_OK == resultCode)) {
				// 結果はArrayListで返ってくる
				ArrayList<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				// エディットテキストのテキストを取得します
				EditText editText = (EditText) findViewById(R.id.editText);
				String comment = editText.getText().toString();
				comment += results.get(0);
				editText.setText(comment);
			}
			super.onActivityResult(requestCode, resultCode, data);
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	// Button の onClick で実装
	public void onOKButtonClick(View v) {
		try {
			Log.i(TAG, "onOKButtonClick");
			// エディットテキストのテキストを取得します
			EditText editText = (EditText) findViewById(R.id.editText);
			mComment = editText.getText().toString();

			StaticHash hash = new StaticHash(this);
			hash.put(KumamonStickyWidget.COMMENT, String.valueOf(mAppWidgetId),
					mComment);
			hash.put(KumamonStickyWidget.ICON, String.valueOf(mAppWidgetId),
					mIcon);
			hash.put(KumamonStickyWidget.BACK, String.valueOf(mAppWidgetId),
					mBack);

			Intent intent = new Intent(this, KumamonStickyWidget.class);
			intent.setAction(CONFIG_DONE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			sendBroadcast(intent);
			setResult(RESULT_OK, intent);
			finish();
			Log.i(TAG, "onOKButtonClick End");
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}
}
