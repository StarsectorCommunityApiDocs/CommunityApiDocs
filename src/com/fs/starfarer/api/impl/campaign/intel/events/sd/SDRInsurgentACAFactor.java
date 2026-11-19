package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRInsurgentACAFactor extends BaseEventFactor {
	
	protected MarketAPI market;

	public SDRInsurgentACAFactor() {
		this.market = Global.getSector().getEconomy().getMarket("nortia");
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
				tooltip.addPara("The Charterists (ACA) favor neither side of the Sindrian Diktat civil war, and opportunistically attack"
						+ " the forces of both Hector's absolutists and Achilles' reformers.", 0f, h, "Charterists");
				
				tooltip.addPara("The effect of Charterist resistance will hinder whichever heir appears to be winning the conflict.", 8f, h, "hinder");
				
				if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_missionCompleted") && 
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverEveryone") && 
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverRobedMan")) {
						
					tooltip.addPara("A talented young pilot has led the ACA on several daring raids, freeing political prisoners from Cruor and infiltrating heavily-defended Diktat military installations. "
								+ "This mop-haired youth has become a hero of the uprising; his image is shown throughout the Sector as a symbol of the cause.", 8f, h, "youth");
					
				}
				// TODO: and if someone invades/destroys Nortia?
			
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
		
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_missionCompleted") &&
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverEveryone") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverRobedMan")) {
			
			_multiplier++;
		}
			
		// negative for Hector, positive for Achilles.
		// Who is winning?
		if(SDResolution.get() != null) { 
			if(SDResolution.get().getProgress() > SDResolution.ACHILLES / 2) {
				return SDRPoints.ACA_BASE + SDRPoints.ACA_EFFECT * -_multiplier;
			}
			else {
				return SDRPoints.ACA_BASE + SDRPoints.ACA_EFFECT * _multiplier;
			}
		}
		
		return 0;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Charterist raiding in Askonia";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}