package com.fs.starfarer.api.campaign;

import java.util.GregorianCalendar;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CampaignClockAPI {
	public int getCycle();
	/**
	 * 1 = January, 12 = December.
	 * @return
	 */
	public int getMonth();

	/**
	 * Get the day since game start. If game started 4 in-game days ago, it will return 4.0.
	 * Example: Global.getSector().getClock().getDay()
	 * @return int value
	 */
	public int getDay();
	public int getHour();
	public float convertToDays(float realSeconds);
	public float convertToMonths(float realSeconds);
	/**
	 * Gets the timestamp of the current date as a 14 digit, negative signed long number. March 2, c206 is -55661253120000.
	 * Example: Global.getSector().getClock().getTimestamp()
	 * @return long value
	 */
	public long getTimestamp();
	public float getElapsedDaysSince(long timestamp);
	
	public String getMonthString();
	public String getShortMonthString();
	public float getSecondsPerDay();
	
	/**
	 * New clock based on the timestamp.
	 * @param timestamp
	 * @return
	 */
	CampaignClockAPI createClock(long timestamp);
	String getDateString();
	float convertToSeconds(float days);
	String getShortDate();
	String getCycleString();
	GregorianCalendar getCal();
}
