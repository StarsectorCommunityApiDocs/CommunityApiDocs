package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class SDFuelShortage implements EconomyTickListener {
	
	public static String KEY = "$sindrianDiktatFuelShortage_ref";
	public static SDFuelShortage get() {
		return (SDFuelShortage) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public static void end() {
		Global.getSector().getListenerManager().removeListener(get());
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	
	protected boolean firstTime = true;
	
	public SDFuelShortage() {
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		reportEconomyTick(0);
	}

	public void reportEconomyTick(int iterIndex) {
		String fuelShortageId = "SDFuelShortage";
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (Factions.DIKTAT.equals(market.getFactionId())) continue;
			if (Factions.DIKTAT_ACHILLES.equals(market.getFactionId())) continue;
			
			int penalty = 1;
			if (market.getSize() >= 5) penalty = 2;
			CommodityOnMarketAPI fuel = market.getCommodityData(Commodities.FUEL);
			fuel.getAvailableStat().addTemporaryModFlat(
					10f,
					fuelShortageId, "Sindrian Diktat civil war", -penalty);
			
			if (firstTime) {
				RecentUnrest.get(market).add(1, "Outbreak of civil war in the Sindrian Diktat");
			}
		}
		firstTime = false;
	}

	@Override
	public void reportEconomyMonthEnd() {
		
	}
	
}






