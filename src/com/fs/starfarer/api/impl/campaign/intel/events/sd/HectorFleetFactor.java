package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class HectorFleetFactor extends BaseOneTimeFactor {
	
	public HectorFleetFactor() {
		super(SDRPoints.PRETENDER_FLEET);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Hector's fleet defeated";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("The fleet commanded by Hector Andrada has been destroyed. This is a huge blow to his"
						+ " prospects of securing his place as Supreme Executor.",
						0f, Misc.getHighlightColor(), "Hector Andrada");
			}
			
		};
	}
	
}
