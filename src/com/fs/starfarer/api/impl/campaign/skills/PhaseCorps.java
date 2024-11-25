package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class PhaseCorps {
	
	//public static float PHASE_CLOAK_COOLDOWN_REDUCTION = 25f;
	//public static float FLUX_UPKEEP_REDUCTION = 30f;
	public static float PHASE_SPEED_BONUS = 50f;
	
	public static float PEAK_TIME_BONUS = 180f;
	
	//public static float PHASE_FIELD_BONUS_PERCENT = 50f;
	public static float PHASE_SHIP_SENSOR_BONUS_PERCENT = 100f;
	

	public static boolean isPhaseAndOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (!ship.getHullSpec().isPhase()) return false;
			return !ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			if (!member.isPhaseShip()) return false;
			return !member.getCaptain().isDefault();
		}
	}
	public static boolean isPhase(MutableShipStatsAPI stats) {
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null) return false;
		return member.isPhaseShip();
	}
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			return "\n*The sensor strength of phase ships also contributes to the fleetwide stealth bonus granted by the Phase Field hullmod.";
		}
		public Color[] getHighlightColors() {
			return null;
		}
		public String[] getHighlights() {
			return null;
		}
		public Color getTextColor() {
			return null;
		}
	}
	
//	public static class Level1 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			if (isPhaseAndOfficer(stats)) {
//				stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f - FLUX_UPKEEP_REDUCTION / 100f);
//			}
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getPhaseCloakUpkeepCostBonus().unmodifyMult(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int)(FLUX_UPKEEP_REDUCTION) + "% flux generated by active phase cloak";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.PILOTED_SHIP;
//		}
//	}
//
//	public static class Level2 implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			if (isPhaseAndOfficer(stats)) {
//				stats.getPeakCRDuration().modifyFlat(id, PEAK_TIME_BONUS);
//			}
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getPeakCRDuration().unmodifyFlat(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int) PEAK_TIME_BONUS + " seconds peak operating time";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.PILOTED_SHIP;
//		}
//	}
	
	
	
	public static class Level3 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getPhaseOPTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			//stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f - PHASE_CLOAK_COOLDOWN_REDUCTION / 100f);
			if (isPhase(stats) && !isCivilian(stats)) {
				//float upkeepBonus = computeAndCacheThresholdBonus(stats, "pc_upkeep", FLUX_UPKEEP_REDUCTION, ThresholdBonusType.PHASE_OP);
				float peakBonus = computeAndCacheThresholdBonus(stats, "pc_peak", PEAK_TIME_BONUS, ThresholdBonusType.PHASE_OP);
				//stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f - upkeepBonus / 100f);
				stats.getPeakCRDuration().modifyFlat(id, peakBonus);
				
				float sensorBonus = computeAndCacheThresholdBonus(stats, "pc_sensor", PHASE_SHIP_SENSOR_BONUS_PERCENT, ThresholdBonusType.PHASE_OP);
				stats.getSensorStrength().modifyPercent(id, sensorBonus);
				
				float speedBonus = computeAndCacheThresholdBonus(stats, "pc_speed", PHASE_SPEED_BONUS, ThresholdBonusType.PHASE_OP);
				stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).modifyFlat(id, speedBonus);
				stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).modifyFlat(id, speedBonus);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getPeakCRDuration().unmodifyFlat(id);
			stats.getSensorStrength().unmodifyPercent(id);
			stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).unmodifyFlat(id);
			stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
//			float op = getTotalOP(data, cStats);
//			bonus = getThresholdBasedRoundedBonus(DISSIPATION_PERCENT, op, OP_THRESHOLD);
//			
//			float op = getTotalOP(data, cStats);
//			bonus = getThresholdBasedRoundedBonus(CAPACITY_PERCENT, op, FIGHTER_BAYS_THRESHOLD);
			
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			FleetDataAPI data = getFleetData(null);
			
			//float upkeepBonus = computeAndCacheThresholdBonus(data, stats, "pc_upkeep", FLUX_UPKEEP_REDUCTION, ThresholdBonusType.PHASE_OP);
			float peakBonus = computeAndCacheThresholdBonus(data, stats, "pc_peak", PEAK_TIME_BONUS, ThresholdBonusType.PHASE_OP);
			float sensorBonus = computeAndCacheThresholdBonus(data, stats, "pc_sensor", PHASE_SHIP_SENSOR_BONUS_PERCENT, ThresholdBonusType.PHASE_OP);
			float speedBonus = computeAndCacheThresholdBonus(data, stats, "pc_speed", PHASE_SPEED_BONUS, ThresholdBonusType.PHASE_OP);
//			info.addPara("-%s flux generated by active phase cloak for combat phase ships (maximum: %s)", 0f, hc, hc,
//					"" + (int) upkeepBonus + "%",
//					"" + (int) FLUX_UPKEEP_REDUCTION + "%");
//			info.addPara("-%s phase cloak cooldown", 0f, hc, hc,
//					"" + (int) PHASE_CLOAK_COOLDOWN_REDUCTION + "%");
			
//			addFighterBayThresholdInfo(info, data);
			info.addSpacer(5f);
			
			info.addPara("+%s seconds peak operating time for combat phase ships (maximum: %s)", 0f, hc, hc,
					"" + (int) peakBonus,
					"" + (int) PEAK_TIME_BONUS);
			info.addPara("+%s top speed and acceleration while phase cloak active (maximum: %s)", 0f, hc, hc,
					"" + (int) speedBonus + "%",
					"" + (int) PHASE_SPEED_BONUS + "%");
			info.addPara("+%s to sensor strength of combat phase ships* (maximum: %s)", 0f, hc, hc,
					"" + (int) sensorBonus + "%",
					"" + (int) PHASE_SHIP_SENSOR_BONUS_PERCENT + "%");
			addPhaseOPThresholdInfo(info, data, stats);
			
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
//	public static class Level4 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
//		
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			if (isPhase(stats)) {
//				stats.getSensorStrength().modifyPercent(id, PHASE_SHIP_SENSOR_BONUS_PERCENT);
//			}
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getSensorStrength().unmodifyPercent(id);
//		}
//		
//		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
//				TooltipMakerAPI info, float width) {
//			init(stats, skill);
//			float opad = 10f;
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "all phase ships");
//			info.addSpacer(opad);
//			info.addPara("+%s to sensor strength of phase ships", 0f, hc, hc,
//					"" + (int) PHASE_SHIP_SENSOR_BONUS_PERCENT + "%");
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.CUSTOM;
//		}
//
//		public FleetTotalItem getFleetTotalItem() {
//			return null;
//		}
//	}
	
//	public static class Level4 extends BaseSkillEffectDescription implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			StatMod phaseFieldMod = stats.getDetectedRangeMod().getFlatBonus(PhaseField.MOD_KEY);
//			if (phaseFieldMod != null) {
//				int value = (int) Math.round(phaseFieldMod.value * PHASE_FIELD_BONUS_PERCENT / 100f);
//				if (value < 0) {
//					stats.getDetectedRangeMod().modifyFlat(id, value, "Phase corps");
//				}
//			}
//		}
//
//		public void unapply(MutableFleetStatsAPI stats, String id) {
////			stats.getDetectedRangeMod().unmodifyFlat(id);
//		}
//		
//		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
//											TooltipMakerAPI info, float width) {
//			init(stats, skill);
//			float opad = 10f;
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
//			info.addSpacer(opad);
//			info.addPara("+%s to fleetwide sensor profile reduction from phase field", 0f, hc, hc,
//					"" + (int) PHASE_FIELD_BONUS_PERCENT + "%");
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.FLEET;
//		}
//	}
	
}
