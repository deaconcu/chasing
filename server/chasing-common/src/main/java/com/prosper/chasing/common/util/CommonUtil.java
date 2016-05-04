package com.prosper.chasing.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Date;

public class CommonUtil {
	
	/**
	 * get stack trace from throwable object
	 */
	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	
	/**
	 * get time String formated to "yyyy-MM-dd HH:mm:ss" from date object 
	 */
	public static String getTime(Date date) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	
	/**
	 * get time millis from time string
	 * @throws ParseException when time string is not formated as "yyyy-MM-dd HH:mm:ss"
	 */
	public static long getMillis(String dateString) throws ParseException {
		try {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = sdf.parse(dateString);
			return date.getTime();
		} catch (ParseException e) {
			throw new RuntimeException("time format failed, date: " + dateString, e);
		}
	}

}
