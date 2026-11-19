package com.fs.starfarer.api.impl.campaign.submarkets;

import java.util.Random;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem;
import com.fs.starfarer.api.impl.campaign.CoreCampaignPluginImpl;
import com.fs.starfarer.api.impl.campaign.DelayedBlueprintLearnScript;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BlackMarketPlugin extends BaseSubmarketPlugin {
	
	public static Logger log = Global.getLogger(BlackMarketPlugin.class);
	
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}


	public void updateCargoPrePlayerInteraction() {
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;

		
		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;
			float stability = market.getStabilityValue();
			
			pruneWeapons(0f);
			
			boolean military = Misc.isMilitary(market);
//			boolean hiddenBase = market.getMemoryWithoutUpdate().getBoolean(MemFlags.HIDDEN_BASE_MEM_FLAG);
//			
//			float extraShips = 0f;
//			int extraShipSize = 0;
//			if (military && hiddenBase && !market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
//				extraShips = 500f;
//				extraShipSize = 1;
//			}
			
			WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<String>(itemGenRandom);
			factionPicker.add(market.getFactionId(), 15f - stability);
			factionPicker.add(Factions.INDEPENDENT, 4f);
			factionPicker.add(submarket.getFaction().getId(), 6f);
			
			int weapons = 6 + Math.max(0, market.getSize() - 1) + (military ? 5 : 0);
			int fighters = 2 + Math.max(0, (market.getSize() - 3) / 2) + (military ? 2 : 0);
			weapons = 6 + Math.max(0, market.getSize() - 1);
			fighters = 2 + Math.max(0, (market.getSize() - 3) / 2);
			
			addWeapons(weapons, weapons + 2, 3, factionPicker);
			addFighters(fighters, fighters + 2, 3, factionPicker);
			
			if (military) {
				weapons = market.getSize();
				fighters = Math.max(1, market.getSize() / 3);
				addWeapons(weapons, weapons + 2, 3, market.getFactionId(), false);
				addFighters(fighters, fighters + 2, 3, market.getFactionId());
			}
			
			float sMult = 0.5f + Math.max(0, (1f - stability / 10f)) * 0.5f;
			getCargo().getMothballedShips().clear();
			float pOther = 0.1f;
			
			FactionDoctrineAPI doctrine = market.getFaction().getDoctrine().clone();
//			FactionDoctrineAPI doctrine = submarket.getFaction().getDoctrine().clone();
//			doctrine.setWarships(3);
			if (doctrine.getWarships() > 0) {
				doctrine.setWarships(Math.max(2, doctrine.getWarships()));
			}
			if (doctrine.getCarriers() > 0) {
				doctrine.setCarriers(Math.max(2, doctrine.getCarriers()));
			}
			if (doctrine.getPhaseShips() > 0) {
				doctrine.setPhaseShips(Math.max(2, doctrine.getPhaseShips()));
			}
//			doctrine.setPhaseShips(2);
			
			addShips(market.getFactionId(),
					70f * sMult, // combat
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // freighter 
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // tanker
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // transport
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // liner
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // utilityPts
					null,
					0f, // qualityMod
					null,
					doctrine);
			FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
			doctrineOverride.setWarships(3);
			doctrineOverride.setPhaseShips(2);
			doctrineOverride.setCarriers(2);
			doctrineOverride.setCombatFreighterProbability(1f);
			doctrineOverride.setShipSize(5);
			addShips(submarket.getFaction().getId(),
					70f, // combat
					10f, // freighter 
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // tanker
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // transport
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // liner
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // utilityPts
					//0.8f,
					Math.min(1f, Misc.getShipQuality(market, market.getFactionId()) + 0.5f),
					0f, // qualityMod
					null,
					doctrineOverride,
					3 // no capital ships, max size cruiser
					);
			addShips(Factions.INDEPENDENT,
					15f + 15f * sMult, // combat
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // freighter 
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // tanker
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // transport
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // liner
					itemGenRandom.nextFloat() > pOther ? 0f : 10f, // utilityPts
					//0.8f,
					Math.min(1f, Misc.getShipQuality(market, market.getFactionId()) + 0.5f),
					0f, // qualityMod
					null,
					null,
					3 // no capital ships, max size cruiser
					);
			
			if (Misc.hasHeavyIndustry(market)) {
				addShips(Factions.INDEPENDENT,
						40f, // combat
						0f, // freighter 
						0f, // tanker
						0f, // transport
						0f, // liner
						0f, // utilityPts
						null, // qualityOverride
						-0.5f, // qualityMod
						null,
						null,
						3, // min ship size
						4);
			}			
			
			addHullMods(4, 1 + itemGenRandom.nextInt(3));
		}
		
		getCargo().sort();
	}
	
	protected Object writeReplace() {
		if (okToUpdateShipsAndWeapons()) {
			pruneWeapons(0f);
			getCargo().getMothballedShips().clear();
		}
		return this;
	}
	
	@Override
	public int getStockpileLimit(CommodityOnMarketAPI com) {
//		int demand = com.getMaxDemand();
//		int available = com.getAvailable();
//		
//		//float limit = BaseIndustry.getSizeMult(available) - BaseIndustry.getSizeMult(Math.max(0, demand - 2));
//		float limit = BaseIndustry.getSizeMult(available);
//		limit *= com.getCommodity().getEconUnit();
		
		//limit *= com.getMarket().getStockpileMult().getModifiedValue();
		
		float limit = OpenMarketPlugin.getBaseStockpileLimit(com);
		
		Random random = new Random(market.getId().hashCode() + submarket.getSpecId().hashCode() + Global.getSector().getClock().getMonth() * 170000);
		limit *= 0.9f + 0.2f * random.nextFloat();
		
		float sm = 1f - market.getStabilityValue() / 10f;
		limit *= (0.25f + 0.75f * sm);
		
		if (limit < 0) limit = 0;
		
		return (int) limit;
	}
	
	@Override
	public PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
		//return PlayerEconomyImpactMode.PLAYER_BUY_ONLY;
		// if the player buying stuff can cause a shortage, it can result in profitable buy/sell cycles, so: don't do that
		//return PlayerEconomyImpactMode.NONE;
		return PlayerEconomyImpactMode.PLAYER_SELL_ONLY;
	}


	public float getDesiredCommodityQuantity(CommodityOnMarketAPI com) {
		boolean illegal = market.isIllegal(com.getId());
		if (illegal) return com.getStockpile();
		
		float blackMarketLegalFraction = 1f - 0.09f * market.getStabilityValue();
		return com.getStockpile() * blackMarketLegalFraction;
	}


	@Override
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		super.reportPlayerMarketTransaction(transaction);
		
		FactionAPI faction = submarket.getFaction();
		delayedLearnBlueprintsFromTransaction(faction, getCargo(), transaction, 60f + 60 * (float) Math.random());
	}
	
	public static void delayedLearnBlueprintsFromTransaction(FactionAPI faction, CargoAPI cargo, PlayerMarketTransaction transaction) {
		delayedLearnBlueprintsFromTransaction(faction, cargo, transaction, 60f + 60 * (float) Math.random());
	}
	public static void delayedLearnBlueprintsFromTransaction(FactionAPI faction, CargoAPI cargo, PlayerMarketTransaction transaction, float daysDelay) { 
		DelayedBlueprintLearnScript script = new DelayedBlueprintLearnScript(faction.getId(), daysDelay);
		for (CargoStackAPI stack : transaction.getSold().getStacksCopy()) {
			SpecialItemPlugin plugin = stack.getPlugin();
			if (plugin instanceof BlueprintProviderItem) {
				BlueprintProviderItem bpi = (BlueprintProviderItem) plugin;
				
				boolean learnedSomething = false;
				if (bpi.getProvidedFighters() != null) {
					for (String id : bpi.getProvidedFighters()) {
						if (faction.knowsFighter(id)) continue;
						script.getFighters().add(id);
						learnedSomething = true;
					}
				}
				if (bpi.getProvidedWeapons() != null) {
					for (String id : bpi.getProvidedWeapons()) {
						if (faction.knowsWeapon(id)) continue;
						script.getWeapons().add(id);
						learnedSomething = true;
					}
				}
				if (bpi.getProvidedShips() != null) {
					for (String id : bpi.getProvidedShips()) {
						if (faction.knowsShip(id)) continue;
						script.getShips().add(id);
						learnedSomething = true;
					}
				}
				if (bpi.getProvidedIndustries() != null) {
					for (String id : bpi.getProvidedIndustries()) {
						if (faction.knowsIndustry(id)) continue;
						script.getIndustries().add(id);
						learnedSomething = true;
					}
				}
				
				if (learnedSomething) {
					cargo.removeItems(stack.getType(), stack.getData(), 1);
				}
			}
		}
		
		if (!script.getFighters().isEmpty() || !script.getWeapons().isEmpty() ||
				!script.getShips().isEmpty() || !script.getIndustries().isEmpty()) {
			Global.getSector().addScript(script);
			cargo.sort();
		}
	}


	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		return false;
	}

	@Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		return false;
	}
	
//	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
//		return market.isIllegal(commodityId);
//	}
//
//	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
//		if (!stack.isCommodityStack()) return false;
//		return isIllegalOnSubmarket((String) stack.getData(), action);
//	}
//	
//	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
//		return "Illegal to trade on the " + submarket.getNameOneLine().toLowerCase() + " here";
//	}
	
	
	public float getTariff() {
		if (Misc.isExtended()) {
			CoreUIAPI core = Global.getSector().getCampaignUI().getCore2();
			boolean tradingTOff = core != null && core.getTradeMode() != CoreUITradeMode.NONE &&
					Global.getSector().getPlayerFleet() != null && !Global.getSector().getPlayerFleet().isTransponderOn(); 
			if (core != null && core.getTradeMode() != CoreUITradeMode.SNEAK && !market.isFreePort() && !tradingTOff) {
				return Global.getSettings().getFloat("ext_blackMarketTariff") * getMarketTariffMult(market);
			}
			return Global.getSettings().getFloat("ext_blackMarketTariffSneak") * getMarketTariffMult(market);
		}
		return 0f;
	}

	@Override
	public String getTariffTextOverride() {
		if (getTariff() > 0f) {
			return "Bribes (" + (int)Math.round(getTariff() * 100f) + "%)";
		}
		return super.getTariffTextOverride();
	}


	@Override
	public boolean isBlackMarket() {
		return true;
	}
	
	
	public String getTooltipAppendix(CoreUIAPI ui) {
		if (isEnabled(ui)) {
//			CampaignEventManagerAPI manager = Global.getSector().getEventManager();
//			EventProbabilityAPI ep = manager.getProbability(Events.INVESTIGATION_SMUGGLING, market);
//			float p = ep.getProbability();
			
			String pre = "";
			if (Misc.isExtended()) {
				CoreUIAPI core = Global.getSector().getCampaignUI().getCore2();
				boolean tradingTOff = core != null && core.getTradeMode() != CoreUITradeMode.NONE &&
						Global.getSector().getPlayerFleet() != null && !Global.getSector().getPlayerFleet().isTransponderOn();
				if (core != null && core.getTradeMode() != CoreUITradeMode.SNEAK && !market.isFreePort() && !tradingTOff) {
					if (Global.getSettings().getFloat("ext_blackMarketTariffSneak") > 0f) {
						pre = "Sneak into port with the transponder off to pay reduced bribes to allow black market trade\n\n";
					} else {
						pre = "Sneak into port with the transponder off to avoid having to pay bribes to allow black market trade\n\n";
					}
				}
			}
			
			float p = CoreCampaignPluginImpl.computeSmugglingSuspicionLevel(market);
			if (p < 0.05f) return pre + "Suspicion level: none";
			
			if (p < 0.1f) {
				return pre + "Suspicion level: minimal";
			}
			if (p < 0.2f) {
				return pre + "Suspicion level: medium";
			}
			if (p < 0.3f) {
				return pre + "Suspicion level: high";
			}
			if (p < 0.5f) {
				return pre + "Suspicion level: very high";
			}
			return pre + "Suspicion level: extreme";
		}

		return null;
	}
	
	public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
		String appendix = getTooltipAppendix(ui);
		if (appendix == null) return null;
		
		Highlights h = new Highlights();
		h.setText(appendix);
		h.setColors(Misc.getNegativeHighlightColor());
		return h;
	}
}



