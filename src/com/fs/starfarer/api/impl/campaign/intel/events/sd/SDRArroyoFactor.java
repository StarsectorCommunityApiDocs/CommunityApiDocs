package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRArroyoFactor extends BaseEventFactor {
	
	public SDRArroyoFactor() {
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
				tooltip.addPara("Rayan Arroyo's network of gray-market contacts, rogue traders, and countless front companies allows him to deliver "
							+ "sensitive commodities - weapons, for instance - past sanctions and blockades.", 0f, h, "Rayan Arroyo");
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistHector")) {
					tooltip.addPara("He has agreed to back Hector's Absolutist faction.", 8f, h, "Hector's");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistAchilles")) {
					tooltip.addPara("He has agreed to back Achilles' Reformer faction.", 8f, h, "Achilles'");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) {
					tooltip.addPara("He has agreed to back the rebels of the United Front in their uprising against the Sindrian Diktat.", 8f, h, "United Front");
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
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistHector")) 
		{
			return -SDRPoints.ARROYO;
		}
		else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistAchilles")) 
		{
			return SDRPoints.ARROYO;
		}
		else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) 
		{
			return SDRPoints.ARROYO;
		}
		
		// if he hasn't been explicitly asked to assist, Arroyo backs no one.
		// This shouldn't appear if PC hasn't made contact with Arroyo about this.
		
		return 0; // SDRPoints.ARROYO;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Arroyo's smuggling operation";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}
}





