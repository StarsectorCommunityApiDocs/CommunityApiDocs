package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * MarketHasIndustry <id1> <id2> <id3> etc
 * Checks if market has ANY of the listed industries.
 */
public class MarketHasIndustry extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		if (dialog == null) return false;
		SectorEntityToken target = dialog.getInteractionTarget();
		if (target == null) return false;
		MarketAPI market = target.getMarket();
		if (market == null) return false;
		
		
		for (int i = 0; i < params.size(); i++) {
			String id = params.get(i).getString(memoryMap);
			if (market.hasIndustry(id)) return true;
		}
		
		return false;
	}
}
