package com.fs.starfarer.api.impl.hullmods;

import java.util.HashMap;
import java.util.Map;

import java.awt.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RecoveryShuttles extends BaseLogisticsHullMod {

	public static float MIN_CR = 0.1f;
	public static float MAX_RECOVERED = 0.8f;
	
	public static float SMOD_MULT = 1.5f;
	
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 10f);
		mag.put(HullSize.DESTROYER, 20f);
		mag.put(HullSize.CRUISER, 40f);
		mag.put(HullSize.CAPITAL_SHIP, 80f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		float mult = 1f;
		if (sMod) mult = SMOD_MULT;
		stats.getDynamic().getMod(Stats.FIGHTER_PILOTS_RECOVERED_MOD).modifyFlat(id, (Float) mag.get(hullSize) * mult);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return Misc.getRoundedValueMaxOneAfterDecimal(SMOD_MULT) + Strings.X;
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		if (Misc.isAutomated(ship.getVariant())) return false;
		return super.isApplicableToShip(ship); 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (Misc.isAutomated(ship.getVariant())) {
			return "Can not be installed on automated ships";
		}
		return super.getUnapplicableReason(ship);
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		if (isForModSpec || ship == null) return;
		if (Global.getSettings().getCurrentState() == GameState.TITLE) return;
		
		float fleetMod = getFighterPilotsRecoveredForPlayerShips();
		float cr = ship.getCurrentCR();
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getId().equals(ship.getFleetMemberId())) {
				cr = member.getRepairTracker().getCR();
				if (!member.getVariant().hasHullMod(HullMods.RECOVERY_SHUTTLES) &&
						ship.getVariant().hasHullMod(HullMods.RECOVERY_SHUTTLES)) {
					fleetMod += ship.getMutableStats().getDynamic().getMod(Stats.FIGHTER_PILOTS_RECOVERED_MOD).computeEffective(0f);
				}
				break;
			}
		}
		
		if (cr < MIN_CR) {
			tooltip.addPara("This ship's combat readiness is below %s " +
				"and the recovery shuttles on it can not be utilized.", opad);
		}
		
		tooltip.addPara("The maximum number of fighter pilots that can "
				+ "be recovered by all of the shuttles in your fleet is %s.", opad, h,
				"" + (int)Math.round(fleetMod) + "");
		
	}
	
	public static int getFighterPilotsRecoveredForPlayerShips() {
		return getFighterPilotsRecoveredForPlayerShips(null);
	}
	
	public static int getFighterPilotsRecoveredForPlayerShips(FleetMemberAPI exclude) {
		float total = 0f;
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			if (member.getRepairTracker().getCR() < RecoveryShuttles.MIN_CR) continue;
			if (member == exclude) continue;
			float v = member.getStats().getDynamic().getMod(Stats.FIGHTER_PILOTS_RECOVERED_MOD).computeEffective(0f);
			if (v <= 0) continue;
			total += v;
		}
		return Math.round(total);
	}
}











