package com.fs.starfarer.api.impl.campaign.submarkets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BaseSubmarketPlugin implements SubmarketPlugin {

	public static String MARKET_TARIFF_MULT = "market_tariff_mult";
	public static float GLOBAL_TARIFF_MULT = 1f;
	
	public static float getMarketTariffMult(MarketAPI market) {
		if (market == null) return GLOBAL_TARIFF_MULT;
		return market.getStats().getDynamic().getValue(MARKET_TARIFF_MULT) * GLOBAL_TARIFF_MULT;
	}
	
	//public static float TRADE_IMPACT_DAYS = 30f;
	public static float TRADE_IMPACT_DAYS = 180f;
	
	public static class ShipSalesData {
		private String variantId;
		private float numShips;
		private float totalValue;
		public String getVariantId() {
			return variantId;
		}
		public void setVariantId(String variantId) {
			this.variantId = variantId;
		}
		public float getNumShips() {
			return numShips;
		}
		public void setNumShips(float numShips) {
			this.numShips = numShips;
		}
		public float getTotalValue() {
			return totalValue;
		}
		public void setTotalValue(float totalValue) {
			this.totalValue = totalValue;
		}
	}
	
	protected MarketAPI market;
	protected SubmarketAPI submarket;
	
	protected CargoAPI cargo;
	protected float minSWUpdateInterval = 30; // campaign days
	protected float sinceSWUpdate = 30f + 1;
	protected float sinceLastCargoUpdate = 30f + 1;
	
	protected Random itemGenRandom = new Random();
	
	
	public void init(SubmarketAPI submarket) {
		this.submarket = submarket;
		this.market = submarket.getMarket();
	}
	
	protected Object readResolve() {
		return this;
	}

	public String getName() {
		return null;
	}

	public CargoAPI getCargo() {
		if (cargo == null) {
			this.cargo = Global.getFactory().createCargo(true);
			this.cargo.initMothballedShips(submarket.getFaction().getId());
		}
		return cargo;
	}
	
	public CargoAPI getCargoNullOk() {
		return cargo;
	}
	
	public void setCargo(CargoAPI cargo) {
		this.cargo = cargo;
	}

	public void updateCargoPrePlayerInteraction() {
		
	}
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		sinceLastCargoUpdate += days;
		sinceSWUpdate += days;
	}
	
	public boolean okToUpdateShipsAndWeapons() {
		//if (true) return true;
		return sinceSWUpdate >= minSWUpdateInterval;
	}
	
	public void addAllCargo(CargoAPI otherCargo) {
		for (CargoStackAPI stack : otherCargo.getStacksCopy()) {
			if (stack.isNull()) continue;
			getCargo().addItems(stack.getType(), stack.getData(), stack.getSize());
		}
	}
	

	public float getTariff() {
		return market.getTariff().getModifiedValue();
	}

	public String getBuyVerb() {
		return "Buy";
	}

	public String getSellVerb() {
		return "Sell";
	}

	public boolean isFreeTransfer() {
		return false;
	}

	public boolean isEnabled(CoreUIAPI ui) {
		return ui.getTradeMode() == CoreUITradeMode.OPEN || isBlackMarket();
		//return true;
	}
	public OnClickAction getOnClickAction(CoreUIAPI ui) {
		return OnClickAction.OPEN_SUBMARKET;
	}
	public String getDialogText(CoreUIAPI ui) {
		return null;
	}
	public Highlights getDialogTextHighlights(CoreUIAPI ui) {
		return null;
	}
	public DialogOption [] getDialogOptions(CoreUIAPI ui) {
		return null;
	}
	public String getTooltipAppendix(CoreUIAPI ui) {
		return null;
	}
	public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
		return null;
	}
	
	
	public PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
		return PlayerEconomyImpactMode.NONE;
	}
	
	public float getPlayerTradeImpactMult() {
		return 1f;
	}
	
	public float getTradeSellRemoveMult() {
		return 0.5f;
	}
	
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		if (!isParticipatesInEconomy()) return;
		
		PlayerEconomyImpactMode mode = getPlayerEconomyImpactMode();
		//if (mode == PlayerEconomyImpactMode.NONE) return;
		
		//mode = PlayerEconomyImpactMode.NONE;
		
		SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket).addTransaction(transaction);
		
		
		for (CargoStackAPI stack : transaction.getSold().getStacksCopy()) {
			if (stack.isCommodityStack()) {
				float qty = stack.getSize() * getPlayerTradeImpactMult();
				if (qty <= 0) continue;
				CommodityOnMarketAPI com = market.getCommodityData(stack.getCommodityId());
				
				if (mode == PlayerEconomyImpactMode.BOTH) {
					com.addTradeMod("sell_" + Misc.genUID(), qty, TRADE_IMPACT_DAYS);
				} else if (mode == PlayerEconomyImpactMode.PLAYER_SELL_ONLY) {
					com.addTradeModPlus("sell_" + Misc.genUID(), qty, TRADE_IMPACT_DAYS);
				} else if (mode == PlayerEconomyImpactMode.PLAYER_BUY_ONLY || mode == PlayerEconomyImpactMode.NONE) {
					com.addTradeModMinus("sell_" + Misc.genUID(), qty, TRADE_IMPACT_DAYS);
				}
				
				float currQty = com.getCombinedTradeModQuantity();
				float origQty = Math.max(0f, currQty - qty);
				int origMod = (int) com.getModValueForQuantity(origQty);
				int currMod = (int) com.getModValueForQuantity(Math.min(origQty + com.getDeficitQuantity(), currQty));
				
				int modDiff = currMod - origMod;
				float remove = stack.getSize() * getTradeSellRemoveMult();
				if (modDiff > 0) {
					float modVal = com.getQuantityForModValue(modDiff);
					remove = modVal + Math.max(0f, (stack.getSize() - modVal) * getTradeSellRemoveMult());
				}
				if (remove > 0) {
					getCargo().removeCommodity(com.getId(), remove);
				}
				
			}
		}
		for (CargoStackAPI stack : transaction.getBought().getStacksCopy()) {
			if (stack.isCommodityStack()) {
				float qty = stack.getSize() * getPlayerTradeImpactMult();
				if (qty <= 0) continue;
				CommodityOnMarketAPI com = market.getCommodityData(stack.getCommodityId());
				
				if (mode == PlayerEconomyImpactMode.BOTH) {
					com.addTradeMod("buy_" + Misc.genUID(), -qty, TRADE_IMPACT_DAYS);
				} else if (mode == PlayerEconomyImpactMode.PLAYER_SELL_ONLY || mode == PlayerEconomyImpactMode.NONE) {
					com.addTradeModPlus("buy_" + Misc.genUID(), -qty, TRADE_IMPACT_DAYS);
				} else if (mode == PlayerEconomyImpactMode.PLAYER_BUY_ONLY) {
					com.addTradeModMinus("buy_" + Misc.genUID(), -qty, TRADE_IMPACT_DAYS);
				}
			}
		}
	}

	public boolean isMilitaryMarket() {
		return false;
	}
	
	public boolean isBlackMarket() {
		//return false;
		return market.getFaction().isHostileTo(submarket.getFaction());
	}

	public boolean isOpenMarket() {
		return false;
	}
	
	public boolean isParticipatesInEconomy() {
		return true;
	}
	

	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
//		if (market.hasCondition(Conditions.FREE_PORT)) return false;
//		//return market.isIllegal(commodityId); 
//		return submarket.getFaction().isIllegal(commodityId); 
		return market.isIllegal(commodityId);
	}

	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (!stack.isCommodityStack()) return false;
		return isIllegalOnSubmarket((String) stack.getData(), action);
	}
	
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "Illegal to trade on the " + submarket.getNameOneLine().toLowerCase() + " here";
	}
	
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL && !isBlackMarket() && Misc.isAutomated(member)) {
			return true;
		}
		return false;
	}
	
	public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
		//return "Illegal to trade on the " + submarket.getNameOneLine().toLowerCase() + " here";
		if (action == TransferAction.PLAYER_BUY) {
			return "Illegal to buy"; // this shouldn't happen
		} else {
			if (isFreeTransfer()) {
				return "Illegal to store";
			}
			return "Illegal to sell";
		}
	}
	
	
//	protected void addWeapons(int min, int max, int maxTier, WeightedRandomPicker<String> factionPicker) {
//		int num = min + itemGenRandom.nextInt(max - min + 1);
//		for (int i = 0; i < num; i++) {
//			String factionId = factionPicker.pick();
//			addWeapons(1, 1, maxTier, factionId);
//		}
//	}
	
	protected void addFighters(int min, int max, int maxTier, WeightedRandomPicker<String> factionPicker) {
		int num = min + itemGenRandom.nextInt(max - min + 1);
		for (int i = 0; i < num; i++) {
			String factionId = factionPicker.pick();
			addFighters(1, 1, maxTier, factionId);
		}
	}
	
	protected void addWeapons(int min, int max, int maxTier, String factionId) {
		addWeapons(min, max, maxTier, factionId, true);
	}
	protected void addWeapons(int min, int max, int maxTier, String factionId, boolean withCategories) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(itemGenRandom);
		picker.add(factionId);
		addWeapons(min, max, maxTier, picker, withCategories);
	}
	
	protected void addWeapons(int min, int max, int maxTier, WeightedRandomPicker<String> factionPicker) {
		addWeapons(min, max, maxTier, factionPicker, true);
	}
	protected void addWeapons(int min, int max, int maxTier, WeightedRandomPicker<String> factionPicker, boolean withCategories) {
		WeightedRandomPicker<WeaponSpecAPI> picker = new WeightedRandomPicker<WeaponSpecAPI>(itemGenRandom);
		
		WeightedRandomPicker<WeaponSpecAPI> pd = new WeightedRandomPicker<WeaponSpecAPI>(itemGenRandom);
		WeightedRandomPicker<WeaponSpecAPI> kinetic = new WeightedRandomPicker<WeaponSpecAPI>(itemGenRandom);
		WeightedRandomPicker<WeaponSpecAPI> nonKinetic = new WeightedRandomPicker<WeaponSpecAPI>(itemGenRandom);
		WeightedRandomPicker<WeaponSpecAPI> missile = new WeightedRandomPicker<WeaponSpecAPI>(itemGenRandom);
		WeightedRandomPicker<WeaponSpecAPI> strike = new WeightedRandomPicker<WeaponSpecAPI>(itemGenRandom);

		for (int i = 0; i < factionPicker.getItems().size(); i++) {
			String factionId = factionPicker.getItems().get(i);
			float w = factionPicker.getWeight(i);
			if (factionId == null) factionId = market.getFactionId();
		
			float quality = Misc.getShipQuality(market, factionId);
			FactionAPI faction = Global.getSector().getFaction(factionId);
			
			for (String id : faction.getKnownWeapons()) {
				WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
				if (spec.getTier() > maxTier) continue;
				if (spec.getAIHints().contains(AIHints.SYSTEM)) continue;
				if (spec.hasTag(Tags.WEAPON_NO_SELL)) continue;
				if (isBlackMarket() && spec.hasTag(Tags.NO_SELL_BLACK_MARKET)) continue;
				
				float p = DefaultFleetInflater.getTierProbability(spec.getTier(), quality);
				p = 1f; // 
				p *= w;
				if (faction.getWeaponSellFrequency().containsKey(id)) {
					p *= faction.getWeaponSellFrequency().get(id);
				}
				picker.add(spec, p);
				
				String cat = spec.getAutofitCategory();
				if (cat != null && spec.getSize() != WeaponSize.LARGE) {
					if (CoreAutofitPlugin.PD.equals(cat)) {
						pd.add(spec, p);
					} else if (CoreAutofitPlugin.STRIKE.equals(cat)) {
						strike.add(spec, p);
					} else if (CoreAutofitPlugin.KINETIC.equals(cat)) {
						kinetic.add(spec, p);
					} else if (CoreAutofitPlugin.MISSILE.equals(cat) || CoreAutofitPlugin.ROCKET.equals(cat)) {
						missile.add(spec, p);
					} else if (CoreAutofitPlugin.HE.equals(cat) || CoreAutofitPlugin.ENERGY.equals(cat)) {
						nonKinetic.add(spec, p);
					}
				}
			}
		}
		
		int num = min + itemGenRandom.nextInt(max - min + 1);
		num = getModifiedNum(num, true, "ext_weaponForSaleFraction");
		
		if (withCategories) {
			if (num > 0 && !pd.isEmpty()) {
				pickAndAddWeapons(pd);
				num--;
			}
			if (num > 0 && !kinetic.isEmpty()) {
				pickAndAddWeapons(kinetic);
				num--;
			}
			if (num > 0 && !missile.isEmpty()) {
				pickAndAddWeapons(missile);
				num--;
			}
			if (num > 0 && !nonKinetic.isEmpty()) {
				pickAndAddWeapons(nonKinetic);
				num--;
			}
			if (num > 0 && !strike.isEmpty()) {
				pickAndAddWeapons(strike);
				num--;
			}
		}
		

		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			pickAndAddWeapons(picker);
		}
	}
	
	protected int getModifiedNum(int num, boolean sqrt, String keepFractonSettingsId) {
		float keepFraction = Misc.isExtended() ? Global.getSettings().getFloat(keepFractonSettingsId) : 1f;
		if (keepFraction >= 1f) return num;
		if (sqrt) keepFraction = (float) Math.sqrt(keepFraction);
		if (keepFraction <= 0f) return 0;
		
		if (keepFraction < 1f) {
			float plusMinus = Math.max(1f, num * keepFraction * 0.25f);
			float modified = (num * keepFraction) + itemGenRandom.nextFloat() * plusMinus * 2f - plusMinus;
			if (modified < 1f) {
				if (itemGenRandom.nextFloat() < modified) {
					modified = 1f;
				} else {
					modified = 0f;
				}
			}
			//num = (int) Math.round(modified);
			num = (int) modified;
		}
		return num;
	}
	
	protected void pickAndAddWeapons(WeightedRandomPicker<WeaponSpecAPI> picker) {
		WeaponSpecAPI spec = picker.pick();
		if (spec == null) return;
		
//		int count = 2;
//		switch (spec.getSize()) {
//		case LARGE: count = 2; break;
//		case MEDIUM: count = 4; break;
//		case SMALL: count = 8; break;
//		}
//		count = count + itemGenRandom.nextInt(count + 1) - count/2;
		
		int count = 1;
		switch (spec.getSize()) {
		case LARGE: count = 1; break;
		case MEDIUM: count = 2; break;
		case SMALL: count = 3; break;
		}
		count = count + itemGenRandom.nextInt(count + 2) - itemGenRandom.nextInt(count + 1);
		if (count < 1) count = 1;
		
		count = getModifiedNum(count, true, "ext_weaponForSaleFraction");
		if (count > 0) {
			cargo.addWeapons(spec.getWeaponId(), count);
		}
	}
	
	
	protected void addFighters(int min, int max, int maxTier, String factionId) {
		if (factionId == null) factionId = market.getFactionId();
		
		int num = min + itemGenRandom.nextInt(max - min + 1);
		num = getModifiedNum(num, true, "ext_fighterForSaleFraction");
		
		float quality = Misc.getShipQuality(market, factionId);
		
		FactionAPI faction = Global.getSector().getFaction(factionId);
		
		WeightedRandomPicker<FighterWingSpecAPI> picker = new WeightedRandomPicker<FighterWingSpecAPI>(itemGenRandom);
		for (String id : faction.getKnownFighters()) {
			FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(id);
			if (spec == null) {
				throw new RuntimeException("Fighter wing spec with id [" + id + "] not found");
			}
			if (spec.getTier() > maxTier) continue;
			if (spec.hasTag(Tags.WING_NO_SELL)) continue;
			
			float p = DefaultFleetInflater.getTierProbability(spec.getTier(), quality);
			p = 1f;
			if (faction.getFighterSellFrequency().containsKey(id)) {
				p *= faction.getFighterSellFrequency().get(id);
			}
			picker.add(spec, p);
		}
		
		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			FighterWingSpecAPI spec = picker.pick();
			
			int count = 2;
			switch (spec.getRole()) {
			case ASSAULT: count = 2; break;
			case BOMBER: count = 2; break;
			case INTERCEPTOR: count = 4; break;
			case FIGHTER: count = 3; break;
			case SUPPORT: count = 2; break;
			}
			
			count = count + itemGenRandom.nextInt(count + 1) - count/2;
			count = getModifiedNum(count, true, "ext_fighterForSaleFraction");
			if (count > 0) {
				cargo.addItems(CargoItemType.FIGHTER_CHIP, spec.getId(), count);
			}
		}
	}
	protected void pruneWeapons(float keepFraction) {
		CargoAPI cargo = getCargo();
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			if (stack.isWeaponStack() || stack.isFighterWingStack()) {
				float qty = stack.getSize();
				if (qty <= 1) {
					if (itemGenRandom.nextFloat() > keepFraction) {
						cargo.removeItems(stack.getType(), stack.getData(), 1);
					}
				} else {
					cargo.removeItems(stack.getType(), stack.getData(), Math.round(qty * (1f - keepFraction)));
				}
			}
		}
	}
	
	public void addShips(String factionId, 
			float combat,
			float freighter,
			float tanker,
			float transport, 
			float liner, 
			float utility,
			Float qualityOverride,
			float qualityMod,
			ShipPickMode modeOverride,
			FactionDoctrineAPI doctrineOverride) {
		addShips(factionId, combat, freighter, tanker, transport, liner, utility, qualityOverride, qualityMod, modeOverride, doctrineOverride, 1000);
		
	}
	public void addShips(String factionId, 
			float combat,
			float freighter,
			float tanker,
			float transport, 
			float liner, 
			float utility,
			Float qualityOverride,
			float qualityMod,
			ShipPickMode modeOverride,
			FactionDoctrineAPI doctrineOverride,
			int maxShipSize) {
		addShips(factionId, combat, freighter, tanker, transport, liner, utility, 
				qualityOverride, qualityMod, modeOverride, doctrineOverride, 0, maxShipSize);
	}
	
	public void addShips(String factionId, 
						float combat,
						float freighter,
						float tanker,
						float transport, 
						float liner, 
						float utility,
						Float qualityOverride,
						float qualityMod,
						ShipPickMode modeOverride,
						FactionDoctrineAPI doctrineOverride,
						int minShipSize,
						int maxShipSize) {
		FleetParamsV3 params = new FleetParamsV3(
				market,
				Global.getSector().getPlayerFleet().getLocationInHyperspace(),
				factionId,
				null, // qualityOverride
				FleetTypes.PATROL_LARGE,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				transport, // transportPts
				liner, // linerPts
				utility, // utilityPts
				0f // qualityMod
				);
		params.minShipSize = minShipSize;
		params.maxShipSize = maxShipSize;
		params.random = new Random(itemGenRandom.nextLong());
		
		if (Misc.isExtended()) {
			qualityMod += Global.getSettings().getFloat("ext_shipForSaleQualityMod");
		}
		
		params.qualityOverride = Misc.getShipQuality(market, factionId) + qualityMod;
		if (qualityOverride != null) {
			params.qualityOverride = qualityOverride + qualityMod;
		}
		//params.qualityMod = qualityMod;
		
		params.withOfficers = false;
		
		params.forceAllowPhaseShipsEtc = true;
		params.treatCombatFreighterSettingAsFraction = true;
		
		params.modeOverride = Misc.getShipPickMode(market, factionId);
		if (modeOverride != null) {
			params.modeOverride = modeOverride;
		}
		
		params.doctrineOverride = doctrineOverride;

		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet != null) {
			//float p = 0.5f;
			//p = 1f;
			
			float p = Misc.isExtended() ? Global.getSettings().getFloat("ext_shipForSaleFraction") : 1f;
			WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<>(itemGenRandom);
			FactionAPI faction = Global.getSector().getFaction(factionId);
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				float f = 1f;
				if (faction != null) {
					Float mult = faction.getFactionSpec().getShipSellFrequency().get(member.getHullId());
					if (mult != null) {
						f *= mult;
					}
				}
				if (itemGenRandom.nextFloat() > f * 0.5f) continue;
				if (itemGenRandom.nextFloat() > p) continue;
				if (member.getHullSpec().hasTag(Tags.NO_SELL)) continue;
				if (!isMilitaryMarket() && member.getHullSpec().hasTag(Tags.MILITARY_MARKET_ONLY)) continue;
				
				picker.add(member, f);
			}
			
			List<FleetMemberAPI> members = new ArrayList<>();
			while (!picker.isEmpty()) {
				members.add(picker.pickAndRemove());
			}
			
			for (FleetMemberAPI member : members) {
				//if (itemGenRandom.nextFloat() > p) continue;
//				if (member.getHullSpec().hasTag(Tags.NO_SELL)) continue;
//				if (!isMilitaryMarket() && member.getHullSpec().hasTag(Tags.MILITARY_MARKET_ONLY)) continue;
				String emptyVariantId = member.getHullId() + "_Hull";
				addShip(emptyVariantId, true, params.qualityOverride);
			}
		}
	}
	
	protected FleetMemberAPI addShip(String variantOrWingId, boolean withDmods, float quality) {
		FleetMemberAPI member = null;
		if (variantOrWingId.endsWith("_wing")) {
			member = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, variantOrWingId);
		} else {
			member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantOrWingId);
		}
		
		if (withDmods) {
			float averageDmods = DefaultFleetInflater.getAverageDmodsForQuality(quality);
			int addDmods = DefaultFleetInflater.getNumDModsToAdd(member.getVariant(), averageDmods, itemGenRandom);
			if (addDmods > 0) {
				DModManager.setDHull(member.getVariant());
				DModManager.addDMods(member, true, addDmods, itemGenRandom);
			}
		}
		
		member.getRepairTracker().setMothballed(true);
		member.getRepairTracker().setCR(0.5f);
//		assignShipName(member, submarket.getFaction().getId());
		getCargo().getMothballedShips().addFleetMember(member);
		return member;
	}

// TODO not 100% sure about performance implications, maybe look at this later
//	public void assignShipName(FleetMemberAPI member, String factionId) {
//		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(factionId, null, true);
//		fleet.getFleetData().setShipNameRandom(itemGenRandom);
//		fleet.getFleetData().addFleetMember(member);
//		fleet.getFleetData().removeFleetMember(member);
//	}
	
	protected void pruneShips(float mult) {
		CargoAPI cargo = getCargo();
		FleetDataAPI data = cargo.getMothballedShips();
		for (FleetMemberAPI member : data.getMembersListCopy()) {
			if (itemGenRandom.nextFloat() > mult) {
				data.removeFleetMember(member);
			}
		}
	}
	
	protected void addHullMods(int maxTier, int num) {
		addHullMods(maxTier, num, null);
	}
	protected void addHullMods(int maxTier, int num, String factionId) {
		//float p = Global.getSettings().getFloat("sellHullmodProb");
		
		num = getModifiedNum(num, false, "ext_hullmodForSaleFraction");
		
		CargoAPI cargo = getCargo();
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			if (stack.isModSpecStack()) {
				cargo.removeStack(stack);
			}
		}
		
		FactionAPI faction = null;
		if (factionId != null) {
			faction = Global.getSector().getFaction(factionId);
		}
		
		WeightedRandomPicker<HullModSpecAPI> picker = new WeightedRandomPicker<HullModSpecAPI>(itemGenRandom);
		for (String id : submarket.getFaction().getKnownHullMods()) {
			//if (Global.getSector().getCharacterData().knowsHullMod(id)) continue;
			HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
			if (spec.isHidden()) continue;
			if (spec.isAlwaysUnlocked()) continue;
			if (spec.getTier() > maxTier) continue;
			float p = spec.getRarity();
			if (faction != null && faction.getHullmodSellFrequency().containsKey(id) &&
					!Global.getSector().getPlayerFaction().knowsHullMod(id)) {
				p *= faction.getHullmodSellFrequency().get(id);
			}
//			if (Global.getSector().getPlayerFaction().knowsHullMod(id)) {
//				p *= 0.25f;
//			}
				
			picker.add(spec, p);
		}
		//picker.getItems().contains("missile_autoloader");
		for (int i = 0; i < num; i++) {
			HullModSpecAPI pick = picker.pickAndRemove();
			if (pick == null) continue;
			
			String id = pick.getId();
			if (cargoAlreadyHasMod(id)) continue;
			
			if (Global.getSector().getPlayerFaction().knowsHullMod(id)) continue;
			
			//cargo.addItems(CargoItemType.MOD_SPEC, id, 1);
			
			cargo.addItems(CargoItemType.SPECIAL, new SpecialItemData(Items.TAG_MODSPEC, id), 1);
		}
		
	}
	
	protected boolean removeModFromCargo(String id) {
		CargoAPI cargo = getCargo();
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			if (stack.isModSpecStack() && stack.getData().equals(id)) {
				cargo.removeStack(stack);
			}
		}
		return false;
	}
	
	protected boolean cargoAlreadyHasMod(String id) {
		CargoAPI cargo = getCargo();
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			//if (stack.isModSpecStack() && stack.getData().equals(id)) return true;
			if (stack.isSpecialStack() && stack.getSpecialDataIfSpecial().getId().equals(Items.TAG_MODSPEC) &&
					stack.getSpecialDataIfSpecial().getData().equals(id)) return true;
		}
		return false;
	}
	

	public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action) {
		return null;
	}

	public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, TransferAction action) {
		return null;
	}

	public float getMinSWUpdateInterval() {
		return minSWUpdateInterval;
	}

	public void setMinSWUpdateInterval(float minCargoUpdateInterval) {
		this.minSWUpdateInterval = minCargoUpdateInterval;
	}

	public float getSinceLastCargoUpdate() {
		return sinceLastCargoUpdate;
	}

	public void setSinceLastCargoUpdate(float sinceLastCargoUpdate) {
		this.sinceLastCargoUpdate = sinceLastCargoUpdate;
	}

	public float getSinceSWUpdate() {
		return sinceSWUpdate;
	}

	public void setSinceSWUpdate(float sinceSWUpdate) {
		this.sinceSWUpdate = sinceSWUpdate;
	}

	public boolean hasCustomTooltip() {
		return true;
	}
	
	public void createTooltip(CoreUIAPI ui, TooltipMakerAPI tooltip, boolean expanded) {
		float opad = 10f;
		
//		tooltip.setTitleSmallOrbitron();
//		tooltip.setParaSmallInsignia();
		
		tooltip.addTitle(submarket.getNameOneLine());
		String desc = submarket.getSpec().getDesc();

		desc = Global.getSector().getRules().performTokenReplacement(null, desc, market.getPrimaryEntity(), null);
		
		String appendix = submarket.getPlugin().getTooltipAppendix(ui);
		if (appendix != null) desc = desc + "\n\n" + appendix;
		
		if (desc != null && !desc.isEmpty()) {
			LabelAPI body = tooltip.addPara(desc, opad);
			
			if (getTooltipAppendixHighlights(ui) != null) {
				Highlights h = submarket.getPlugin().getTooltipAppendixHighlights(ui);
				if (h != null) {
					body.setHighlightColors(h.getColors());
					body.setHighlight(h.getText());
				}
			}
		}
		
		createTooltipAfterDescription(tooltip, expanded);
	}
	
	
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		
	}
	
	public boolean isTooltipExpandable() {
		return false;
	}
	
	public float getTooltipWidth() {
		return 400f;
	}

	public boolean isHidden() {
		return false;
	}

	public boolean showInFleetScreen() {
		return true;
	}

	public boolean showInCargoScreen() {
		return true;
	}

	public MarketAPI getMarket() {
		return market;
	}

	public SubmarketAPI getSubmarket() {
		return submarket;
	}
	
	
	public int getStockpileLimit(CommodityOnMarketAPI com) {
		return 0;
	}
	
	public float getStockpilingAddRateMult(CommodityOnMarketAPI com) {
		return 1f;
	}
	
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		return true;
	}
	
	public void addAndRemoveStockpiledResources(float amount, 
												boolean withShortageCountering,
												boolean withDecreaseToLimit,
												boolean withCargoUpdate) {
		for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
			if (com.isNonEcon()) continue;
			if (com.getCommodity().isMeta()) continue;
			
			//if (com.getMaxSupply() <= 0 && com.getMaxDemand() <= 0) continue;
			
//			if (market.getId().equals("mazalot") && com.getId().equals("ore")) {
//				System.out.println("wefwefew");
//			}
//			if (com.isIllegal() && com.getMarket().isPlayerOwned()) {
//				System.out.println("wefwefew");
//			}
			addAndRemoveStockpiledResources(com, amount, withShortageCountering, withDecreaseToLimit, withCargoUpdate);
		}
	}
	
	protected boolean doShortageCountering(CommodityOnMarketAPI com, float amount, boolean withShortageCountering) {
		return false;
	}
	
	public static float getCommodityAvailabilityMult(MarketAPI market, SubmarketAPI submarket, String cid) {
		
//		if (cid.equals("supplies") && market.isPlayerOwned()) {
//			System.out.println("fwewfwefwefwe");
//		}
		
		if (Misc.isExtended()) {
			String modId = "ext_" + cid + "ForSaleFraction";
			
			float mult = 1f;
			if (submarket != null && submarket.getPlugin() instanceof LocalResourcesSubmarketPlugin) {
				mult *= Global.getSettings().getFloat("ext_localResourcesStockpileAndRateMult");
			}
			
			String modId3 = "ext_" + cid + "ForSaleMilitaryMult";
			if (Global.getSettings().hasKey(modId3) && submarket != null && submarket.getPlugin().isMilitaryMarket()) {
				mult *= Global.getSettings().optFloat(modId3, 1f);
			}
			
			if (market.isPlayerOwned()) {
				String modId2 = "ext_" + cid + "ForSaleFractionPlayerColony";
				if (Global.getSettings().hasKey(modId2)) {
					return Global.getSettings().optFloat(modId2, 1f) * mult;
				}
			} else if (market.isFreePort() || 
					(market.getFaction() != null && market.getFaction().getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE))) {
				String modId2 = "ext_" + cid + "ForSaleFractionFreePort";
				if (Global.getSettings().hasKey(modId2)) {
					return Global.getSettings().optFloat(modId2, 1f) * mult;
				}
			}
			
			if (submarket != null) {
				CommodityOnMarketAPI com = market.getCommodityData(cid);
				if (com.isPersonnel()) {
					float rel = submarket.getFaction().getRelToPlayer().getRel();
					if (rel < 0) {
						mult *= 1f + (1f - Global.getSettings().getFloat("ext_personnelForSaleMinMultBadRep")) * rel;
					} else {
						mult *= 1f + (Global.getSettings().getFloat("ext_personnelForSaleMaxMultGoodRep") - 1f) * rel;
					}
					
				}
			}
			
			return Global.getSettings().optFloat(modId, 1f) * mult;
		}
		return 1f;
	}
	
	public void addAndRemoveStockpiledResources(CommodityOnMarketAPI com, float amount,
												boolean withShortageCountering,
												boolean withDecreaseToLimit,
												boolean withCargoUpdate) {
		
//		if (com.isIllegal() && com.getMarket().isPlayerOwned()) {
//			System.out.println("wefwefew");
//		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		//if (days <= 0) return;
		
		if (com.isNonEcon()) return;
		if (com.getCommodity().isMeta()) return;
		//if (com.getMaxSupply() <= 0 && com.getMaxDemand() <= 0) return;
		
		CargoAPI cargo = getCargo();
		//String modId = "localRes";
//		String modId = submarket.getSpecId();
//			
//		com.getAvailableStat().unmodifyFlat(modId);
//		
//		int demand = com.getMaxDemand();
//		int available = com.getAvailable();
		
		
		if (withShortageCountering) {
			withShortageCountering = market.isUseStockpilesForShortages();
		}
		
		//if (demand > available && withShortageCountering) {
		if (doShortageCountering(com, amount, withShortageCountering)) {
			return;
		}
		
		float mult = getCommodityAvailabilityMult(market, submarket, com.getId());
		//days *= mult;
		
		if (!shouldHaveCommodity(com)) {
			if (withDecreaseToLimit) {
				float limit = getStockpileLimit(com) * mult;
				float curr = cargo.getCommodityQuantity(com.getId());
				if (curr > limit && withDecreaseToLimit) {
					float removeRate = (curr - limit) * 2f / 30f;
					float removeAmount = removeRate * days * mult;
					
					if (curr - removeAmount < limit) {
						removeAmount = curr - limit;
					}
					if (removeAmount > 0 && curr <= 1) {
						removeAmount = 1f;
					}
					
					if (removeAmount > 0) {
						cargo.removeCommodity(com.getId(), removeAmount);
						if (Commodities.SUPPLIES.equals(com.getId())) {
							float partsFraction = getPartsGeneratedAsFractionOfSupplies();
							cargo.removeCommodity(Commodities.PARTS, Math.max(1f, removeAmount * partsFraction));
						}
					}
				}
			}
			return;
		}

		// add stockpile, up to limit
		float limit = getStockpileLimit(com) * mult;
		float curr = cargo.getCommodityQuantity(com.getId());
		
//		if (this instanceof LocalResourcesSubmarketPlugin) {
//			cargo.removeCommodity(com.getId(), cargo.getCommodityQuantity(com.getId()));
//			cargo.addCommodity(com.getId(), limit);
//			return;
//		}
		
//		if (com.getId().equals(Commodities.SUPPLIES) && market.getId().equals("chicomoztoc")) {
//			System.out.println("efkjwefew");
//		}
//		if (com.getId().equals(Commodities.SUPPLIES) && market.isPlayerOwned() && this instanceof LocalResourcesSubmarketPlugin) {
//			System.out.println("efkjwefew");
//		}
		
		// deal with Domain-grade Parts
		if (Commodities.SUPPLIES.equals(com.getId())) {
//			if (com.getMarket().isPlayerOwned() && this instanceof LocalResourcesSubmarketPlugin) {
//				System.out.println("wefwefew");
//			}
			
//			if (com.getMarket().isPlayerOwned() && this instanceof LocalResourcesSubmarketPlugin && 
//					com.getMarket().getName().equals("Ghant")) {
//				System.out.println("wefwefew");
//			}			
			
			float partsFraction = getPartsGeneratedAsFractionOfSupplies();
			if (partsFraction > 0) {
				float partsLimit = limit * partsFraction;
				float currParts = cargo.getCommodityQuantity(Commodities.PARTS);
				
				if (currParts < partsLimit && withCargoUpdate) {
					float addRate = limit / 30f * getStockpilingAddRateMult(com);
					float addAmount = addRate * days * partsFraction;
					if (currParts + addAmount > partsLimit) {
						addAmount = partsLimit - currParts;
					}
					if (addAmount > 0) {
						float q = cargo.getCommodityQuantity(Commodities.PARTS) + addAmount;
						if (q < 1) {
							addAmount = 1f; // add at least 1 unit or it won't do anything
						}
						cargo.addCommodity(Commodities.PARTS, addAmount);
					}
				}
				if (currParts > partsLimit && withDecreaseToLimit) {
					float removeRate = (currParts - partsLimit) * 2f / 30f;
					float removeAmount = removeRate * days * mult;
						
					if (currParts - removeAmount < partsLimit) {
						removeAmount = currParts - partsLimit;
					}
					if (removeAmount > 0 && currParts <= 1) {
						removeAmount = 1f;
					}
					if (removeAmount > 0) {
						cargo.removeCommodity(Commodities.PARTS, removeAmount);
					}
				}
			}
		}
		
		
		
		if (curr < limit && withCargoUpdate) {
			if (limit <= 0) return;
			
//			if (market.isPlayerOwned() && market.getName().startsWith("Dark")) {
//				System.out.println("wefwef" + market.getName());
//			}
			
			float addRate = limit / 30f * getStockpilingAddRateMult(com);
			
			// make it so the player constantly re-checking doesn't keep adding cargo more quickly than it should,
			// due to having to add at least 1 unit if there's nothing 
			if (sinceLastCargoUpdate * addRate + curr < 1) {
				return;
			}
			
//			if (Commodities.SUPPLIES.equals(com.getId())) {
//				if (com.getMarket().isPlayerOwned() && this instanceof LocalResourcesSubmarketPlugin) {
//					System.out.println("wefwefew123");
//				}
//			}
			
			float addAmount = addRate * days;
				
			
			if (curr + addAmount > limit) {
				addAmount = limit - curr;
			}
			
			if (addAmount > 0) {
				float q = cargo.getCommodityQuantity(com.getId()) + addAmount;
				if (q < 1) {
					if (Misc.isExtended() && com.isPersonnel()) {
						if (itemGenRandom.nextFloat() < q) {
							addAmount = 1f;
						} else {
							addAmount = 0f;
						}
					} else {
						addAmount = 1f; // add at least 1 unit or it won't do anything
					}
				}
				
				cargo.addCommodity(com.getId(), addAmount);
			}
			
			return;
		}
		
		//withDecreaseToLimit = true;
		if (curr > limit && withDecreaseToLimit) {
			float removeRate = (curr - limit) * 2f / 30f;
			float removeAmount = removeRate * days * mult;
			
			if (curr - removeAmount < limit) {
				removeAmount = curr - limit;
			}
			if (removeAmount > 0 && curr <= 1) {
				removeAmount = 1f;
			}
			
//			if (true) {
//				removeAmount = curr - limit;
//			}
			
			if (removeAmount > 0) {
				cargo.removeCommodity(com.getId(), removeAmount);
			}
			return;
		}
	}

	
	protected float getPartsGeneratedAsFractionOfSupplies() {
		if (!Misc.usePartsBasedDModRepairs()) return 0f;
		
		float quality = market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).computeEffective(0f);
		quality += 0.3f;
		float prod = 0f;
		CommodityOnMarketAPI com = market.getCommodityData(Commodities.SUPPLIES);
		if (com != null) prod = com.getMaxSupply();
		
		float mult = Misc.isExtended() ? Global.getSettings().getFloat("ext_partsForSaleFraction") : 1f;
		
		float fBase = Global.getSettings().getFloat("partsForSaleBaseSuppliesFraction");
		float fProd = Global.getSettings().getFloat("partsForSaleBaseProductionFraction");
		
		return (fBase + quality * fProd * Math.min(1.5f, prod/10f)) * mult;
	}
	
	public String getTariffTextOverride() {
		return null;
	}
	public String getTariffValueOverride() {
		return null;
	}
	public String getTotalTextOverride() {
		return null;
	}
	public String getTotalValueOverride() {
		return null;
	}
}







