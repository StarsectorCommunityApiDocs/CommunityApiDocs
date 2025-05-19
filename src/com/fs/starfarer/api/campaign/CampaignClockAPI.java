package com.fs.starfarer.api.campaign;

import java.util.GregorianCalendar;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CampaignClockAPI {
	/**
	 * Displays the current year cycle.
	 * Example: outputs 206
	 * @return int value
	 */
	public int getCycle();
	/**
	 * 1 = January, 12 = December.
	 * @return int value
	 */
	public int getMonth();
	/**
	 * Get the day since game start. If game started 4 in-game days ago, it will return 4.0.
	 * Example: Global.getSector().getClock().getDay()
	 * @return int value
	 */
	public int getDay();

	/**
	 * Gives the hour of the day.
	 * Example: outputs 3 if 3rd hour of day.
	 * @return int value
	 */
	public int getHour();
	public float convertToDays(float realSeconds);
	public float convertToMonths(float realSeconds);
	/**
	 * Gets the timestamp of the current date as a 14 digit, negative signed long number. March 2, c206 is -55661253120000.
	 * Example: Global.getSector().getClock().getTimestamp()
	 * @return long value
	 */
	public long getTimestamp();
	/**
	 * Gets the time elasped after the specified timestamp.
	 * Example: Returns the time in float time. If partial day, it will return 0.5966898.
	 * @params long timestamp
	 * @return float value
	 */
	public float getElapsedDaysSince(long timestamp);
	/**
	 * Outputs the current in game month as a full string.
	 * @return String value
	 */
	public String getMonthString();
	public String getShortMonthString();
	public float getSecondsPerDay();
	/**
	 * New clock based on the timestamp.
	 * @param timestamp
	 * @return
	 */
	CampaignClockAPI createClock(long timestamp);
	/**
	 * Outputs the current date in a string format.
	 * Example: output is Mar 2, c206.
	 * @return String value
	 */
	String getDateString();
	float convertToSeconds(float days);
	/**
	 * Gets the short date in the format of cYYY.MM.DD.
	 * Example: Returns 206.3.3
	 * @return string value
	 */
	String getShortDate();
	/**
	 * Gets the cycle number in a string format
	 * Example: Returns 206
	 * @return string value
	 */
	String getCycleString();

	/**
	 * Returns the game calender using the Java.util.GregorianCalendar
	 * @return Java.util.GregorianCalendar[]
	 */
	GregorianCalendar getCal();
}
