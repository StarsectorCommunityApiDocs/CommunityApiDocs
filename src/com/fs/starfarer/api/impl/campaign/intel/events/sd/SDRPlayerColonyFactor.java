package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRPlayerColonyFactor extends BaseEventFactor {
	
	public SDRPlayerColonyFactor() {
	}

	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return true;
	}
	
	@Override
	public TooltipCreator getMainRowTooltip(final BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				Color h = Misc.getHighlightColor();

				int _popCount = 0;
				for (MarketAPI market : Misc.getPlayerMarkets(false)) {
					_popCount += market.getSize();
				}
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToAchilles")) {
					tooltip.addPara("You have pledged the support of your colonial polity to the cause of Achilles Andrada. "
							+ "Though distant and insignifcant compared to the Core Worlds, you can provide much needed "
							+ "war materiel and a safe volume for training and outfitting.", 0f, h, "Achilles Andrada");
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToHector")) {
					tooltip.addPara("You have pledged the support of your colonial polity to the cause of Hector Andrada. "
							+ "Though distant and insignifcant compared to the Core Worlds, you can provide much needed "
							+ "war materiel and a safe volume for training and outfitting.", 0f, h, "Hector Andrada");
				}

				if(_popCount > 14) {
					tooltip.addPara("Your colonial development is extensive, providing significant facilities and means to influence the outcome of the conflict.", 
							8f, h, "significant");
				}
				else if (_popCount > 9) {
					tooltip.addPara("Your colonial development is respectable, providing a meaningful influence on the outcome of the conflict.", 
							8f, h, "meaningful");
				}
				else if (_popCount > 4) {
					tooltip.addPara("Your colonial development is limited, providing a small but appreciable influence on the outcome of the conflict.", 
							8f, h, "appreciable");
				}
				else {
					tooltip.addPara("Your colonial development is primitive and provides only a small influence on the outcome of the conflict.", 
							8f, h, "small");
				}
			}
		};
	}

	@Override
	public String getProgressStr(BaseEventIntel intel) {
//		if (getProgress(intel) <= 0) return "";
		return super.getProgressStr(intel);
	}
	
	@Override
	public int getProgress(BaseEventIntel intel) {
		
		int _popCount = 0;
		int _effect = 1;
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			_popCount += market.getSize();
		}
		
		if(_popCount > 14) {
			_effect = 10;
		}
		else if (_popCount > 9) {
			_effect = 7;
		}
		else if (_popCount > 4) {
			_effect = 4;
		}
		
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToHector"))
			_effect *= -1;

		return _effect;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return Global.getSector().getFaction(Factions.PLAYER).getDisplayName() + " support";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}





