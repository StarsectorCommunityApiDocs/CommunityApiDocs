package com.fs.starfarer.api.impl.campaign.intel.events.sd;

public class SDDPoints {

	public static int MARKET_STABILITY_THRESHOLD = 5;
	public static int PROGRESS_PER_MISSING_STABILITY_POINT = 2;
	
	public static int FUEL_PRODUCTION_THRESHOLD = 9;
	public static int PROGRESS_PER_MISSING_FUEL_POINT = 3;
	
	public static int DISRUPTED_HIGH_COMMAND = 15;
	
	public static int CADEN_FLEET = 40;
	public static int HYDER_FLEET = 60;
	
	public static int STATION_DESTROYED = 75; 
	
	public static int FP_PER_POINT = 20;
	public static float FP_MULT_TRADE_FLEET = 2.0f;
	public static float FP_MULT_LG_FLEET = 0.8f;
	
	// Askonia rebels, the "United Front"
	// These get added via each "supply" done by the player, up to 5x base value.
	public static int ARC_BASE = 5;
	public static int ARC_EFFECT = 3;
	public static int VSDC_BASE = 5;
	public static int VSDC_EFFECT = 3;
	public static int ACA_BASE = 2; // ACA effects for SDR are in that file
	public static int ACA_EFFECT = 2;
	
	public static int RAO_EFFECT = 8;
}
