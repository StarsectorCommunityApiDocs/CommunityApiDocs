package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRHyderFactor extends BaseOneTimeFactor {
	
	public SDRHyderFactor() {
		super(SDRPoints.HYDER_FLEET);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderArrestDone")) {
			return "Hyder arrested for treason against the Sindrian Diktat";
		}
		return "Hyder pledges support for Achilles";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderArrestDone")) {
					tooltip.addPara("Oxana Hyder was arrested and stripped of command at the order of "
							+ "Chief High Inspector-General Dolos Macario.\n\nShe is accused of plotting to defect to the usurper Achilles, "
							+ "providing material assistance to terrorists, theft of naval assets, smuggling, spreading defeatism,"
							+ " and for conspiracy to assassinate the Supreme Executor."
						+ "This is a serious blow to the morale of the Sindrian Diktat Navy.",
						0f, Misc.getHighlightColor(), "Oxana Hyder'");
				}
				else {
					tooltip.addPara("Oxana Hyder has thrown her support behind Achilles Andrada, bringing with her the "
						+ "Askonia System Defense Armada and mid-level elements of the Sindrian Diktat Navy. "
						+ "This is a serious blow to Hector Andrada's Absolutist faction.",
						0f, Misc.getHighlightColor(), "Oxana Hyder'");
				}
			}
			
		};
	}
	
}