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

public class DateComparator implements java.util.Comparator<Object> {
	public int compare(Object o1, Object o2) {
		return (int) ((((CalendarEventData) o1).dtstart - ((CalendarEventData) o2).dtstart) / 1000);
	}
}
