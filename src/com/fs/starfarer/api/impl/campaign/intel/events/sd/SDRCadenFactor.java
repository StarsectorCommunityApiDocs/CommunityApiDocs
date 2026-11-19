package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRCadenFactor extends BaseEventFactor {
	
	public SDRCadenFactor() {
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
				
				
				
				if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHector")) {
					
					tooltip.addPara("The fleets and paramilitary forces of the Lion's Guard, led by Guard High Deputy Executor Horacio Caden "
						+ "are eager to fight, and merciless, if not particularly skillfully led.", 0f, h, "Horacio Caden");
				}
				else {
					tooltip.addPara("The fleets and paramilitary forces of the Lion's Guard, led by Guard High Deputy Executor Horacio Caden "
						+ "are not quite as responsive to the strategic directives of Supreme Executor Hector Andrada"
						+ " as one would expect.", 0f, h, "Horacio Caden");
				}
				
				if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHyder") ||
					Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_suggestHyderBackHector")) {
					
					tooltip.addPara("\nForces of the the Sindrian Diktat navy under the de-facto leadership of Oxana Hyder are working closely with"
								+ " the Lion's Guard. Such cooperation has not been seen for many cycles.", 8f, h, "Oxana Hyder");
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
		int _mult = 1;
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gaveCadenCopyOfFiles")) {
			_mult = 2;
		}
		
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_suggestHyderBackHector")) {
			_mult++;
			_mult++;
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHyder")) {
			_mult++;
		}
		
		// invert if Caden actually tries
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHector") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHyder")) {
			_mult *= -1;
		}
		
		return SDRPoints.CADEN_RESENTMENT * _mult;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHector") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHyder")) {
			
			return "The Lion's Guard";
		}
		//$global.sdr_gotCadenBackHector
				//$global.sdr_gotCadenBackHyder
				//$global.sdr_gotCadenAntiMacario
		
		return "The resentment of Horatio Caden";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}





