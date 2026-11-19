package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.listeners.CommodityTooltipModifier;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

public class DomainGradePartsPlugin implements CommodityTooltipModifier, ShowLootListener {

	public static float BASE_DP = Global.getSettings().getFloat("partsSalvageBaseDP");
	public static float DP_EXPONENT = Global.getSettings().getFloat("partsSalvageDPExponent");
	public static float MIN_DP_PER_SHIP = Global.getSettings().getFloat("partsSalvageMinDPPerShip");
	
	public static float DMOD_REDUCTION = Global.getSettings().getFloat("partsReductionAtFiveDmods");
	
	
	public static boolean useModifiedDropRate() {
		return Global.getSettings().getBoolean("partsUseModifiedDropRate");
	}
	
	public static float getPostBattleDropRateMult() {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return 1f;

		float val = 1f;
		if (useModifiedDropRate()) {
			float dp = BaseSkillEffectDescription.getTotalOP(fleet.getFleetData(), Global.getSector().getPlayerStats());
			float ships = fleet.getNumShips();
			dp = Math.max(ships * MIN_DP_PER_SHIP, dp);
			if (dp < BASE_DP) dp = BASE_DP;
			val = (float) (Math.pow(BASE_DP, DP_EXPONENT) / Math.pow(dp, DP_EXPONENT));
		}
		val *= fleet.getStats().getDynamic().getValue(Stats.DOMAIN_GRADE_PARTS_DROP_RATE_MULT);
		return val;
	}
	
	public static float getNormalDropRateMult() {
		if (!useModifiedDropRate()) return 1f;
		
		float dp = 240f;
		if (dp < BASE_DP) dp = BASE_DP;
		float val = (float) (Math.pow(BASE_DP, DP_EXPONENT) / Math.pow(dp, DP_EXPONENT));
		//val = 1f;
		return val;
	}
	
	public static float getNormalizedDropRate() {
		float rate = getPostBattleDropRateMult();
		float normalRate = getNormalDropRateMult();
		float rateAsMultiplier = rate / normalRate;
		return rateAsMultiplier;
	}
	
	
	@Override
	public void addSectionAfterPrice(TooltipMakerAPI info, float width, boolean expanded, CargoStackAPI stack) {
		// I apologize for putting this code here -am
		if (Misc.isExtended() && stack != null && stack.isPersonnelStack()) {
			info.addPara("The number of personnel available for hire at a submarket depends greatly on your relationship "
					+ "with its faction.", 10f);
			return;
		}
		
		if (!Commodities.PARTS.equals(stack.getCommodityId())) return;
		
		float opad = 10f;
		Color h = Misc.getHighlightColor();

		CommoditySpecAPI parts = Global.getSettings().getCommoditySpec(Commodities.PARTS);
		String name = Misc.ucFirst(parts.getName().toLowerCase());
		
		if (useModifiedDropRate()) {
			LabelAPI heading = info.addSectionHeading("Post-battle salvage rate", 
					Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, opad);
			heading.autoSizeToWidth(info.getTextWidthOverride());
			PositionAPI p = heading.getPosition();
			p.setSize(p.getWidth(), p.getHeight() + 3f);
			
			float rate = getPostBattleDropRateMult();
			int ratePct = (int)Math.round(rate * 100f);
			String rateStr = "" + ratePct + "%";
			if (ratePct <= 0) {
				rateStr = "<1%";
			}
	
			boolean showAsMult = false;
			showAsMult = true;
			if (showAsMult) {
				float normalRate = getNormalDropRateMult();
				float rateAsMultiplier = rate / normalRate;
				rateStr = Misc.getRoundedValue(rateAsMultiplier) + Strings.X;
			}
			
			int maxShips = (int)Math.round(BASE_DP / MIN_DP_PER_SHIP);
			
			if (showAsMult) {
				info.addPara("Identifying critical parts during post-battle salvage operations requires an intimate knowledge of the ships "
						+ " that need repairs, and is generally most efficient for small fleets. "
						+ "The chance of finding " + name + " is at its maximum when the fleet has %s or less total "
						+ "deployment points, and consists of %s or fewer ships.", opad, h,
						"" + (int) Math.round(BASE_DP), "" + maxShips);
				
				info.addPara("Current chance of finding " + name + ": %s normal.", opad, h, rateStr);			
			} else {
				info.addPara("Identifying critical parts during salvage operations requires an intimate knowledge of the ships "
						+ " that need repairs, and generally becomes less and less practical as the size of a fleet grows. "
						+ "The rate of finding " + name + " is at its maximum when the fleet has %s or less total "
						+ "deployment points, and consists of %s or fewer ships.", opad, h,
						"" + (int) Math.round(BASE_DP), "" + maxShips);
				
				info.addPara("Current rate of finding " + name + ": %s", opad, h, rateStr);
			}
		}

		
		LabelAPI heading = null;
		if (Misc.isExtended()) {
			heading = info.addSectionHeading("D-mod repairs & ship value", 
					Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, opad);
		} else {
			heading = info.addSectionHeading("D-mod repairs", 
					Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, opad);
		}
		heading.autoSizeToWidth(info.getTextWidthOverride());
		PositionAPI p = heading.getPosition();
		p.setSize(p.getWidth(), p.getHeight() + 3f);

		info.addPara("Repairing d-mods on larger and more expensive hulls generally requires more parts.", opad);
		
		if (Misc.isExtended()) {
			int perPart = Global.getSettings().getInt("ext_shipSellValueIncreasePerPartUsed");
			info.addPara("A ship that had d-mods removed using " + name + " will have its resale value "
					+ "increased by %s per part used, up to a maximum of %s d-mods worth of parts, less "
					+ "the number of d-mods the hull currently has.", opad, h, Misc.getDGSCredits(perPart), "5");
			
			float perSMod = Global.getSettings().getFloat("ext_shipSellValueMultPerSMod");
			if (perSMod > 0) {
				info.addPara("Ships with s-mods have their resale value greatly increased, including the portion "
						+ "of the value that comes from " + name + ".", opad,
						Misc.getStoryOptionColor(), "s-mods");
			}
		}
		
		// specified per-hull now, not per size clas
		if (false) {
			List<HullSize> sizes = new ArrayList<>();
			sizes.add(HullSize.FRIGATE);
			sizes.add(HullSize.DESTROYER);
			sizes.add(HullSize.CRUISER);
			sizes.add(HullSize.CAPITAL_SHIP);
			
			FactionAPI f = Global.getSector().getPlayerFaction();
			float colW = 170f;
			info.beginTable(f, 20f, "Hull size", colW, "Parts per d-mod", colW + 0f);
			
			String costStr;
			costStr = "" + (int) Global.getSettings().optFloatFromArray("dModRepairCostInParts", HullSize.FRIGATE.ordinal() - 2, 1);
			info.addRow(Alignment.MID, h, "Frigate", Alignment.MID, h, costStr);
			
			costStr = "" + (int) Global.getSettings().optFloatFromArray("dModRepairCostInParts", HullSize.DESTROYER.ordinal() - 2, 1);
			info.addRow(Alignment.MID, h, "Destroyer", Alignment.MID, h, costStr);
			
			costStr = "" + (int) Global.getSettings().optFloatFromArray("dModRepairCostInParts", HullSize.CRUISER.ordinal() - 2, 1);
			info.addRow(Alignment.MID, h, "Cruiser", Alignment.MID, h, costStr);
			
			costStr = "" + (int) Global.getSettings().optFloatFromArray("dModRepairCostInParts", HullSize.CAPITAL_SHIP.ordinal() - 2, 1);
			info.addRow(Alignment.MID, h, "Capital", Alignment.MID, h, costStr);
			
			info.addTable("", 0, opad);
			UIComponentAPI prev = info.getPrev();
			int xOff = (int) ((info.getTextWidthOverride() - prev.getWidth()) / 2f);
			prev.getPosition().setXAlignOffset(xOff);
			info.addSpacer(3f);
		}
	}
	
	public static float getPartsPerUnitOfSupplies() {
		if (Misc.isExtended()) {
			return Global.getSettings().getFloat("ext_partsPerUnitOfSupplies");
		}
		return Global.getSettings().getFloat("partsPerUnitOfSupplies");
	}

	@Override
	public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
		if (loot == null) return;
		
		if (!Misc.usePartsBasedDModRepairs()) {
			for (CargoStackAPI stack : loot.getStacksCopy()) {
				if (Commodities.PARTS.equals(stack.getCommodityId())) {
					loot.removeStack(stack);
				}
			}
			return;
		}
		
		
		if (dialog == null) return;
		SectorEntityToken target = dialog.getInteractionTarget();
		if (target == null) return;
		
		// target is a fleet when getting loot from fighting automated defenses
		// and the snapshots are set
		// likewise for a regular battle
		
		float mult = getPartsPerUnitOfSupplies();
		BattleAPI battle = null;
		if (target instanceof CampaignFleetAPI && ((CampaignFleetAPI) target).getBattle() != null) {
			battle = ((CampaignFleetAPI) target).getBattle();
		} else {
			battle = Global.getSector().getPlayerFleet().getBattle();
		}
		if (battle != null) {
			mult *= getPostBattleDropRateMult();
			
//			CampaignFleetAPI fleet = (CampaignFleetAPI) target;
//			BattleAPI battle = fleet.getBattle();
			
			//float f = battle.getPlayerInvolvementFraction();
			//mult *= f; // already accounted for by the amount of supplies
			
			// this does not take into account ships that were recovered
			// just getting an approximate value for the amount of d-mods on the destroyed
			float dmodWeight = 0f;
			float traderWeight = 0f;
			float memberWeight = 0f;
			for (CampaignFleetAPI curr : battle.getSnapshotBothSides()) {
				boolean trader = Misc.isTrader(curr);
				for (FleetMemberAPI member : Misc.getSnapshotMembersLost(curr)) {
					float weight = member.getDeploymentPointsCost();
					float dmods = DModManager.getNumDMods(member.getVariant());
					memberWeight += weight;
					dmodWeight += dmods * weight;
					if (trader) {
						traderWeight += weight;
					}
				}
			}
			float averageDmods = dmodWeight / Math.max(1f, memberWeight);
			float fraction = averageDmods / 5f;
			if (fraction < 0f) fraction = 0f;
			if (fraction > 1f) fraction = 1f;
			
			float dModMult = 1f - fraction * (1f - DMOD_REDUCTION);
			mult *= dModMult;
			
			if (traderWeight > 0f) {
				float traderMult = Global.getSettings().getFloat("partsDropMultFromTradeFleets");
				float traderFraction = (traderWeight / Math.max(1f, memberWeight));
				if (traderFraction > 1f) traderFraction = 1f;
				float nonTraderFraction = 1f - traderFraction;
				if (nonTraderFraction > 1f) nonTraderFraction = 1f;
				
				traderMult = nonTraderFraction + traderFraction * traderMult;
				
				if (traderMult < 0f) traderMult = 0f;
				if (traderMult > 1f) traderMult = 1f;
				mult *= traderMult;
			}
			
		} else {
			if (!target.hasTag(Tags.SALVAGEABLE)) {
				return;
			}
			
			mult *= getNormalDropRateMult();
		}
		
		float supplies = loot.getSupplies();
		float parts = mult * supplies;
		
		Random random = Misc.getRandom(Misc.getSalvageSeed(target) * Global.getSector().getPlayerBattleSeed(), 17);
		
		int partsToAdd = 0;
		
		if (parts < 1f) {
			if (random.nextFloat() < parts) {
				partsToAdd++;
			}
		} else if (parts < 20f) {
			for (float f = 0; f <= parts; f += 0.5f) {
				if (random.nextFloat() < 0.5f) {
					partsToAdd++;
				}	
			}
		} else {
			parts = parts * (0.5f + random.nextFloat());
			partsToAdd += (int) Math.round(parts);
		}
		
		loot.addCommodity(Commodities.PARTS, partsToAdd);
	}

}








