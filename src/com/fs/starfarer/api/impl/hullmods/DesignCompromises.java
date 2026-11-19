package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.combat.TemporalShellStats;

public class DesignCompromises extends BaseHullMod {

	public static float RANGE_MULT = 0.8f;
	public static float FLUX_MULT = 0.6f;
	public static float ENERGY_WEAPON_FLUX_INCREASE = 100f;
	public static float MISSILE_ROF_MULT = 0.5f;
	public static float BALLISTIC_RANGE_MULT = 0.85f;
	public static float TEMPORAL_SHELL_MODIFIER = -1f;
	
	public static boolean AlLOW_CONVERTED_HANGAR = true;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().modifyMult(id, BALLISTIC_RANGE_MULT);
		stats.getMissileRoFMult().modifyMult(id, MISSILE_ROF_MULT);
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_WEAPON_FLUX_INCREASE);
		
		stats.getFluxDissipation().modifyMult(id, FLUX_MULT);
		stats.getFluxCapacity().modifyMult(id, FLUX_MULT);
		stats.getSystemFluxCostBonus().modifyMult(id, FLUX_MULT);
		
		stats.getDynamic().getMod(Stats.TEMPORAL_SHELL_MODIFIER).modifyFlat(id, TEMPORAL_SHELL_MODIFIER);
		
		if (AlLOW_CONVERTED_HANGAR) {
			stats.getDynamic().getMod(Stats.FORCE_ALLOW_CONVERTED_HANGAR).modifyFlat(id, 1f);
			stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_CREW_INCREASE).modifyFlat(id, 1f);
			stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REARM_INCREASE).modifyFlat(id, 1f);
			stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REFIT_PENALTY).modifyFlat(id, 1f);
			//stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_DP_INCREASE).modifyFlat(id, 1f);
		}
	}


	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round((1f - FLUX_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round(TemporalShellStats.MAX_TIME_MULT + TEMPORAL_SHELL_MODIFIER) + Strings.X;
		if (index == 2) return "" + (int)Math.round((1f - BALLISTIC_RANGE_MULT) * 100f) + "%";
		if (index == 3) return "" + (int)Math.round((1f - MISSILE_ROF_MULT) * 100f) + "%";
		if (index == 4) return "" + (int)Math.round(ENERGY_WEAPON_FLUX_INCREASE) + "%";
		
		if (index == 5) return "Converted Hangar";
		if (index == 6) return "" + (int)Math.round(2);
		return null;
	}
	
}









