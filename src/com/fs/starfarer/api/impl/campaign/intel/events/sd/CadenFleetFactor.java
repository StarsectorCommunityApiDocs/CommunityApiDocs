package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class CadenFleetFactor extends BaseOneTimeFactor {
	
	protected String desc;

	public CadenFleetFactor(String desc, int points) {
		super(points);
		this.desc = desc;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return desc;
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("The fleet commanded by Horacio Caden has been defeated.",
						0f, Misc.getHighlightColor(), "Horacio Caden");
			}
			
		};
	}
	
}
