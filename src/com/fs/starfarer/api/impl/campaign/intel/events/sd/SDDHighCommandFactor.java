package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDDHighCommandFactor extends BaseEventFactor {
	
	protected MarketAPI market;

	public SDDHighCommandFactor() {
		this.market = Global.getSector().getEconomy().getMarket("sindria");
	}

	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return market.isInEconomy() && !market.hasCondition(Conditions.DECIVILIZED); // && getProgress(intel) > 0;
	}
	
	@Override
	public TooltipCreator getMainRowTooltip(final BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				Color h = Misc.getHighlightColor();
				tooltip.addPara("If the operations of the High Command " + market.getOnOrAt() + " are disrupted.", 0f,
						h, "High Command", market.getName());
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
		Industry ind = market.getIndustry(Industries.HIGHCOMMAND);
		if (ind != null && !ind.isDisrupted()) return 0;
		return SDDPoints.DISRUPTED_HIGH_COMMAND;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Disrupted High Command " + market.getOnOrAt() + " " + market.getName();
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) > 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}





