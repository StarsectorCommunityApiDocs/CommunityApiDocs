package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CarrierGroup {
	
	public static float TOP_SPEED_PERCENT = 10;
	public static float REPLACEMENT_RATE_PERCENT = 50;
	
	public static float DAMAGE_REDUCTION_PERCENT = 40;
	
	public static float OFFICER_MULT = 1.5f;
	public static boolean isOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship == null) return false;
			if (ship.isFighter() && ship.getWing() != null && ship.getWing().getSourceShip() != null) {
				ship = ship.getWing().getSourceShip();
			}
			if (ship == null) return false;
			return !ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			return !member.getCaptain().isDefault();
		}
	}
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getFighterBaysTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (hasFighterBays(stats)) {
				float rateBonus = computeAndCacheThresholdBonus(stats, "cg_rep_rate", REPLACEMENT_RATE_PERCENT, ThresholdBonusType.FIGHTER_BAYS);
				if (isOfficer(stats)) rateBonus *= OFFICER_MULT;
				float timeMult = 1f / ((100f + rateBonus) / 100f);
				stats.getFighterRefitTimeMult().modifyMult(id, timeMult);
				
//				float speedBonus = computeAndCacheThresholdBonus(stats, "cg_top_speed", TOP_SPEED_PERCENT, ThresholdBonusType.FIGHTER_BAYS);
//				if (isOfficer(stats)) speedBonus *= OFFICER_MULT;
//				stats.getMaxSpeed().modifyPercent(id, speedBonus);
			}
//			else {
//				// may be needed to make refit screen show the right values when adding/removing Converted Hangar
//				// leaving this be for now
//				stats.getFighterRefitTimeMult().unmodifyMult(id);
//				stats.getMaxSpeed().unmodifyPercent(id);
//			}
		}
		
		// NOTE: this is a copy-paste from Level2, which is what actually gets used to apply the bonus!
		protected float getDamageReduction(FleetDataAPI data) {
			if (data == null) return DAMAGE_REDUCTION_PERCENT;
			String key = "carrier_group_damage_reduction";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float bays = getNumFighterBays(data);
			bonus = getThresholdBasedRoundedBonus(DAMAGE_REDUCTION_PERCENT, bays, FIGHTER_BAYS_THRESHOLD);
			
			data.getCacheClearedOnSync().put(key, bonus);
			return bonus;
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFighterRefitTimeMult().unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			FleetDataAPI data = getFleetData(null);
			float rateBonus = computeAndCacheThresholdBonus(data, stats, "cg_rep_rate", REPLACEMENT_RATE_PERCENT, ThresholdBonusType.FIGHTER_BAYS);
			
			info.addPara("+%s faster fighter replacement rate (maximum: %s)", 0f, hc, hc,
					"" + (int) rateBonus + "%",
					"" + (int) REPLACEMENT_RATE_PERCENT + "%");
			
//			float speedBonus = computeAndCacheThresholdBonus(data, stats, "cg_top_speed", TOP_SPEED_PERCENT, ThresholdBonusType.FIGHTER_BAYS);
//			
//			info.addPara("+%s ship top speed (maximum: %s)", 0f, hc, hc,
//					"" + (int) speedBonus + "%",
//					"" + (int) TOP_SPEED_PERCENT + "%");
			
			float damReduction = getDamageReduction(data);
			info.addPara("-%s damage taken by ship's fighters (maximum: %s)", 0f, hc, hc,
					"" + (int) damReduction + "%",
					"" + (int) DAMAGE_REDUCTION_PERCENT + "%");			
			
			addFighterBayThresholdInfo(info, data);
			info.addPara(indent + "Effect increased by %s for ships with officers, including flagship",
					0f, tc, hc, 
					"" + Misc.getRoundedValueMaxOneAfterDecimal(OFFICER_MULT) + Strings.X);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_CARRIERS;
		}

	}
	
	public static class Level2 extends BaseSkillEffectDescription implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float damReduction = getDamageReduction(stats);
			if (isOfficer(stats)) damReduction *= OFFICER_MULT;
			
			stats.getHullDamageTakenMult().modifyMult(id, 1f - (damReduction / 100f));
			stats.getArmorDamageTakenMult().modifyMult(id, 1f - (damReduction / 100f));
			stats.getShieldDamageTakenMult().modifyMult(id, 1f - (damReduction / 100f));
			stats.getEmpDamageTakenMult().modifyMult(id, 1f - (damReduction / 100f));
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getHullDamageTakenMult().unmodifyMult(id);
			stats.getArmorDamageTakenMult().unmodifyMult(id);
			stats.getShieldDamageTakenMult().unmodifyMult(id);
			stats.getEmpDamageTakenMult().unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
	
		protected float getDamageReduction(MutableShipStatsAPI stats) {
			FleetDataAPI data = getFleetData(stats);
			return getDamageReduction(data);
		}
		
		protected float getDamageReduction(FleetDataAPI data) {
			if (data == null) return DAMAGE_REDUCTION_PERCENT;
			String key = "carrier_group_damage_reduction";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float bays = getNumFighterBays(data);
			bonus = getThresholdBasedRoundedBonus(DAMAGE_REDUCTION_PERCENT, bays, FIGHTER_BAYS_THRESHOLD);
			
			data.getCacheClearedOnSync().put(key, bonus);
			return bonus;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			if (true) return;
			init(stats, skill);
			
			FleetDataAPI data = getFleetData(null);
			float damReduction = getDamageReduction(data);
			info.addPara("-%s damage taken by fighters (maximum: %s)", 0f, hc, hc,
					"" + (int) damReduction + "%",
					"" + (int) DAMAGE_REDUCTION_PERCENT + "%");
			
			addFighterBayThresholdInfo(info, data);
			info.addPara(indent + "Effect increased by %s for ships with officers, including flagship",
					0f, tc, hc, 
					"" + Misc.getRoundedValueMaxOneAfterDecimal(OFFICER_MULT) + Strings.X);
			
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_FIGHTERS;
		}

	}

}





