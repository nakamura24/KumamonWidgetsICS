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

public class CalendarEventData {
	public long id = 0;
	public long dtstart = 0;
	public long dtend = 0;
	public String title = null;
	public String description = null;
	public String location = null;
	public int all_day = 0;
}
