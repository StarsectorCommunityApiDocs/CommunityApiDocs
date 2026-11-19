package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDDInsurgentACAFactor extends BaseEventFactor {
	
	public SDDInsurgentACAFactor() {
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
				tooltip.addPara("The Nortia-based Askonia Charterist Alliance (ACA) is participating in the United Front uprising, carrying out a campaign of"
						+ " subversion, sabotage, and raids against official Sindrian Diktat forces.", 0f, h, "Askonia Charterist Alliance");
				
				// if well supplied ... 
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedACAfinished")) {
					tooltip.addPara("Through Arnaud Iscare you have provided the ACA with credits, fuel,"
							+ " and supplies which greatly increases their effectiveness.", 8f, h, "Arnaud Iscare");
				}
				
				if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) {
					tooltip.addPara("The ACA is receiving additional materiel from Arroyo's smuggling operation.", 8f, h, "Arroyo");
				}
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis") &&
					(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario") || 
					Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac"))) {
					tooltip.addPara("Sindrian Diktat Absolutist forces have suffered from a series of inexplicable "
							+ "intelligence failures in their attempts to deal with the ACA rebels.", 8f, h, "inexplicable");
				}
				
				// TODO: and if someone invades Nortia..
				//tooltip.addPara("The ACA base of operations on Nortia has been invaded and destroyed by" 
				//+ " Sindrian Diktat forces, greatly hindering their effectiveness.", 8f, h, "Nortia");
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
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveFuelACA")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveCreditsACA")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesACA")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedACAfinished")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksHegemony")) _multiplier++;
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis") &&
			(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac"))) {
			_multiplier++;
		}
		
		//if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis")) _multiplier++;
			
		return SDDPoints.ACA_BASE + SDDPoints.ACA_EFFECT * _multiplier;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "ACA raiding";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}