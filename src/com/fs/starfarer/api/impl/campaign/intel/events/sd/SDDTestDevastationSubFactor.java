package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDDTestDevastationSubFactor extends BaseOneTimeFactor {
	public SDDTestDevastationSubFactor() {
		super(-100);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Devastation SUBTRACTION test factor";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Test devastaton factor which removes 100 devastation.",
						0f, Misc.getHighlightColor(), "devastation");
			}
			
		};
	}
}