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

import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
//import android.content.res.Resources;
import android.util.Xml;

public class CityEntries {
	private static final String TAG = "CityEntries";

	public class CityEntryData {
		public String title = "";
		public String link = "";
		public int id = 0;
		public double longitude = 0;
		public double latitude = 0;
		public String name = "";
		public String temperature_name = "";
		public String pref_name = "";
		public String permalink = "";
		public String pref_permalink = "";
		public int pref_id = 0;
	}

	private ArrayList<CityEntryData> CityEntryDatas = new ArrayList<CityEntryData>();

	public CityEntries(Context context) {
		try {
			AssetManager as = context.getResources().getAssets();
			InputStream inpurStreamObj = as.open("city_entries.xml");

			// -----[パーサーの設定]
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(inpurStreamObj, "UTF-8");

			int eventType = parser.getEventType();
			CityEntryData cityEntryData = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = parser.getName();
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if ("item".equals(tag)) {
						cityEntryData = new CityEntryData();
						Log.v(TAG, "start tag=" + tag);
					}
					if (cityEntryData != null && "title".equals(tag)) {
						cityEntryData.title = parser.nextText();
						Log.v(TAG, "title=" + cityEntryData.title);
					}
					if (cityEntryData != null && "link".equals(tag)) {
						cityEntryData.link = parser.nextText();
						Log.v(TAG, "link=" + cityEntryData.link);
					}
					if (cityEntryData != null && "city".equals(tag)) {
						cityEntryData.id = Integer.valueOf(parser
								.getAttributeValue(null, "id"));
						cityEntryData.longitude = Float.valueOf(parser
								.getAttributeValue(null, "longitude"));
						cityEntryData.latitude = Float.valueOf(parser
								.getAttributeValue(null, "latitude"));
						cityEntryData.name = parser.getAttributeValue(null,
								"name");
						cityEntryData.temperature_name = parser
								.getAttributeValue(null, "temperature_name");
						cityEntryData.pref_name = parser.getAttributeValue(
								null, "pref_name");
						cityEntryData.permalink = parser.getAttributeValue(
								null, "permalink");
						cityEntryData.pref_permalink = parser
								.getAttributeValue(null, "pref_permalink");
						cityEntryData.pref_id = Integer.valueOf(parser
								.getAttributeValue(null, "pref_id"));
						Log.v(TAG, "name=" + cityEntryData.name);
					}
					break;
				case XmlPullParser.END_TAG:
					if ("item".equals(tag)) {
						CityEntryDatas.add(cityEntryData);
						cityEntryData = null;
						Log.v(TAG, "end tag=" + tag);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			ExceptionLog.Log(TAG, e);
		}
	}

	public ArrayList<String> getPrefNames() {
		ArrayList<String> pref_names = new ArrayList<String>();
		int pre_pref_id = 0;
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).pref_id != pre_pref_id) {
				pre_pref_id = CityEntryDatas.get(i).pref_id;
				pref_names.add(CityEntryDatas.get(i).pref_name);
			}
		}
		return pref_names;
	}

	public ArrayList<Integer> getPrefIds() {
		ArrayList<Integer> pref_ids = new ArrayList<Integer>();
		int pre_pref_id = 0;
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).pref_id != pre_pref_id) {
				pre_pref_id = CityEntryDatas.get(i).pref_id;
				pref_ids.add(CityEntryDatas.get(i).pref_id);
			}
		}
		return pref_ids;
	}

	public ArrayList<String> getNames(int pref_id) {
		ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).pref_id == pref_id) {
				names.add(CityEntryDatas.get(i).name + "("
						+ CityEntryDatas.get(i).temperature_name + ")");
				Log.d(TAG, "getNames - name=" + CityEntryDatas.get(i).name
						+ "(" + CityEntryDatas.get(i).temperature_name + ")");
			}
		}
		return names;
	}

	public ArrayList<Integer> getIds(int pref_id) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).pref_id == pref_id) {
				ids.add(CityEntryDatas.get(i).id);
			}
		}
		return ids;
	}

	public String getTitle(int id) {
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).id == id) {
				Log.d(TAG, "getTitle - title=" + CityEntryDatas.get(i).title);
				return CityEntryDatas.get(i).title;
			}
		}
		return "";
	}

	public String getLink(int id) {
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).id == id) {
				Log.d(TAG, "getLink - link=" + CityEntryDatas.get(i).link);
				return CityEntryDatas.get(i).link;
			}
		}
		return "";
	}

	public int getId(double longitude, double latitude) {
		int id = 63;
		double min_distance = (float) (180 * 180 + 90 * 90);
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			double distance = (CityEntryDatas.get(i).longitude - longitude)
					* (CityEntryDatas.get(i).longitude - longitude)
					+ (CityEntryDatas.get(i).latitude - latitude)
					* (CityEntryDatas.get(i).latitude - latitude);
			if (min_distance > distance) {
				id = CityEntryDatas.get(i).id;
				min_distance = distance;
			}
		}
		return id;
	}

	public int mPrefId = 0;
	public int mCityIid = 0;

	public void getLocation(int id) {
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).id == id) {
				mPrefId = CityEntryDatas.get(i).pref_id;
			}
		}
		for (int i = 0; i < CityEntryDatas.size(); i++) {
			if (CityEntryDatas.get(i).pref_id == mPrefId) {
				mCityIid = id - CityEntryDatas.get(i).id;
				if (mCityIid < 0)
					mCityIid = 0;
				return;
			}
		}
	}
}
