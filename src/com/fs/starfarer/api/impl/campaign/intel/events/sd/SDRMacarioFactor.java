package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRMacarioFactor extends BaseEventFactor {
	
	public SDRMacarioFactor() {
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
				tooltip.addPara("The scheming of Dolos Macario slowly but surely advances Hector's victory "
						+ "in the struggle for control of the Sindrian Diktat.", 0f, h, "Dolos Macario");
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac")) {
					tooltip.addPara("At your instigation, Faro Inesis is running a counter-intelligence operation "
							+ "to thwart the machinations of Dolos Macrio.", 8f, h, "Faro Inesis");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario") && 
						Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis")) {
					tooltip.addPara("At your instigation, Faro Inesis is running a counter-intelligence operation "
							+ "to specifically hinder and discredit Macrio.", 8f, h, "Faro Inesis");
				}
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenAntiMacario")) {
					tooltip.addPara("There are reports of unusual friction between the Lion's Guard and the internal "
							+ "security ministry of the Sindrian Diktat. There have already been arrests and counter-purges "
							+ "while the Askonian population suffers contradictory enforcement operations.", 8f, h, "unusual friction");
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
		
		int _points = SDRPoints.MACARIO;
		
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
			_points = SDRPoints.MACARIO_PRO_HECTOR;
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac")) {
			_points += SDRPoints.INESIS;
		}
		else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario")) {
			int _mult = 1;
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis")) {
				_mult = 2;
			}
			_points += SDRPoints.INESIS * _mult;
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenAntiMacario")) { 
			_points += SDRPoints.CADEN_RESENTMENT;
		}
		
		
			
		
		
		return _points;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "The machinations of Dolos Macario";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}





