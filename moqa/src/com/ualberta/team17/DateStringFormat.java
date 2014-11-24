package com.ualberta.team17;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class for standard date formatting operations used in this application.
 */
public class DateStringFormat {
	/**
	 * Our standard date format
	 */
	private static final String DATE_STRING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	/**
	 * A formatter for our standard date format.
	 */
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat mFormatter = new SimpleDateFormat(DATE_STRING_FORMAT);
	
	/**
	 * Turn a date string into a Date
	 * @param dateString
	 * @return
	 */
	public static Date parseDate(String dateString) {
		try {
			return mFormatter.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * Turn a date into a formatted date string
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		return mFormatter.format(date);
	}
}
