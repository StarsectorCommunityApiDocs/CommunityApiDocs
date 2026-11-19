package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDDInsurgentVSDCFactor extends BaseEventFactor {
	
	public SDDInsurgentVSDCFactor() {
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
				tooltip.addPara("The Volturnian Self Defense Covenant (VSDC) is participating in a general uprising across the Askonia system, carrying out a campaign of"
						+ " subversion, sabotage, and assassination against the Sindrian Diktat.", 0f, h, "Volturnian Self Defense Covenant");
				
				// if well supplied ... 
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedVSDCfinished")) {
					tooltip.addPara("Through Moyra Standfast you have provided the VSDC with heavy weapons, credits,"
							+ " and supplies which greatly increases the impact of their insurgency.", 8f, h, "Moyra Standfast");
				}
				
				if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) {
					tooltip.addPara("The VSDC is receiving additional materiel from Arroyo's smuggling operation.", 8f, h, "Arroyo");
				}
				
				if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_missionCompleted") &&
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverEveryone") && 
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverRobedMan")) {
							
					tooltip.addPara("The VSDC has demonstrated unexpectedly competent strategic ability. "
								+ "Some speak of the Blessing of Providence, but others whisper that a retired general"
								+ " of the Hegemony Legion is secretly assisting their cause.",8f, h, "retired general");

				}
				
				// TODO: and if someone destroys Umbra..
				// Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstRao"))
				
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
		
		int _multiplier = 0;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveWeaponsVSDC")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveCreditsVSDC")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesVSDC")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedVSDCfinished")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) _multiplier++;
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_missionCompleted")) {
			if(	!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverEveryone") && 
				!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverRobedMan")) {
				_multiplier++;
			}
		}
			
		return SDDPoints.VSDC_BASE + SDDPoints.VSDC_EFFECT * _multiplier;
		
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "VSDC uprising";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}