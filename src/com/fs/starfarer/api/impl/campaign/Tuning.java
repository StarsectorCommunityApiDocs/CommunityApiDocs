package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.util.Misc;

public class Tuning {

	public static float EXT_DELAY_FACTOR = Global.getSettings().getFloat("ext_timeBasedThingsDelayFactor");
	
	public static float getExtDelayFactor() {
		if (Misc.isExtended()) return EXT_DELAY_FACTOR;
		return 1f;
	}
	
	public static float NO_PIRATE_RAID_DAYS_FROM_GAME_START = Global.getSettings().getFloat("noPirateRaidDays");
	
	public static float FAST_START_EXTRA_DAYS = Global.getSettings().getFloat("fastStartBaseExtraDays");
	public static float DAYS_UNTIL_FULL_TIME_FACTOR = Global.getSettings().getFloat("daysUntilFullTimeFactor");
	
	public static int PIRATE_BASE_MIN_TIMEOUT_MONTHS = Global.getSettings().getIntFromArray("pirateBaseDestroyedTimeoutMonths", 0); 
	public static int PIRATE_BASE_MAX_TIMEOUT_MONTHS = Global.getSettings().getIntFromArray("pirateBaseDestroyedTimeoutMonths", 1);
	
	public static int PATHER_BASE_MIN_TIMEOUT_MONTHS = Global.getSettings().getIntFromArray("patherBaseDestroyedTimeoutMonths", 0); 
	public static int PATHER_BASE_MAX_TIMEOUT_MONTHS = Global.getSettings().getIntFromArray("patherBaseDestroyedTimeoutMonths", 1);
	
	public static int PIRATE_RAID_MIN_TIMEOUT_MONTHS = Global.getSettings().getIntFromArray("basePirateRaidTimeoutMonths", 0); 
	public static int PIRATE_RAID_MAX_TIMEOUT_MONTHS = Global.getSettings().getIntFromArray("basePirateRaidTimeoutMonths", 1); 
	public static int PIRATE_RAID_DEFEATED_TIMEOUT_MONTHS = Global.getSettings().getInt("pirateRaidTimeoutRaidDefeatedMaxExtraMonths");
	
	
	public static float getDaysSinceStart() {
		return PirateBaseManager.getInstance().getDaysSinceStart();
	}
	
	
	
}
