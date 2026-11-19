package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRTriTachyonFactor extends BaseEventFactor {
	
	protected String factorTitle;
	
	public SDRTriTachyonFactor() {
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
				
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_knowsAboutTTIntervention")) {
					factorTitle = "Covert Tri-Tachyon support";
					tooltip.addPara("It is nearly an open secret that Tri-Tachyon is backing the Sindrian Diktat Absolutists "
						+ "with high-tech weapons and surveillence technology. The full extent of Company involvement may not even be known "
						+ "to it's own top executives.", 0f, h, "Tri-Tachyon");
				}
				else {
					factorTitle = "Covert support for Hector from unknown faction";
					tooltip.addPara("An outside faction is covertly backing the Sindrian Diktat Absolutists.", 0f, h, "outside faction");
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
		return SDRPoints.TRITACHYON;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_knowsAboutTTIntervention")) {
			factorTitle = "Covert Tri-Tachyon support";
		}
		
		if (factorTitle == null) {
			return "Covert support from unknown faction";
		}
		
		return factorTitle; // "Covert Tri-Tachyon support";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}





