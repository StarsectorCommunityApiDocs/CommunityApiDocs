package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class AchillesFleetFactor extends BaseOneTimeFactor {
	
	public AchillesFleetFactor() {
		super(-SDRPoints.PRETENDER_FLEET);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Achilles' fleet defeated";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("The fleet commanded by Achilles Andrada has been destroyed. It is a huge blow to his"
						+ " prospects of becoming the next Supreme Executor.",
						0f, Misc.getHighlightColor(), "Achilles Andrada");
			}
			
		};
	}
	
}
