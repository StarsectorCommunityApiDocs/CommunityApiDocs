package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRTestHectorFactor extends BaseOneTimeFactor {
	
	public SDRTestHectorFactor() {
		super(-Global.getSector().getMemoryWithoutUpdate().getInt("$DEV_sdrHectorPoints"));
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Hector test factor";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("One-time test factor to advance Hector's victory.",
						0f, Misc.getHighlightColor(), "Hector's");
			}
			
		};
	}
	
}





