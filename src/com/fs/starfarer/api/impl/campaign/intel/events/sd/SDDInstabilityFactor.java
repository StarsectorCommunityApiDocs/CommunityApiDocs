package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDDInstabilityFactor extends BaseEventFactor {
	
	protected MarketAPI market;

	public SDDInstabilityFactor(MarketAPI market) {
		this.market = market;
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
				tooltip.addPara("Instability " + market.getOnOrAt() + " %s: %s points "
						+ "of event progress per point of stability below %s.", 0f,
						h, market.getName(), "" + SDDPoints.PROGRESS_PER_MISSING_STABILITY_POINT,
						"" + SDDPoints.MARKET_STABILITY_THRESHOLD);
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
		int stability = (int) Math.round(market.getStabilityValue());
		int missing = SDDPoints.MARKET_STABILITY_THRESHOLD - stability;
		int points = missing * SDDPoints.PROGRESS_PER_MISSING_STABILITY_POINT;
		return Math.max(0, points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Instability " + market.getOnOrAt() + " " + market.getName();
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) > 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}





