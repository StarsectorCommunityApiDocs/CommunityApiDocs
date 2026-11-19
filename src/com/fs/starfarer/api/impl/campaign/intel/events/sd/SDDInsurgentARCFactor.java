package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDDInsurgentARCFactor extends BaseEventFactor {
	protected MarketAPI market;
	
	public SDDInsurgentARCFactor() {
		this.market = Global.getSector().getEconomy().getMarket("umbra");
	}
	
	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return market.isInEconomy() && !market.hasCondition(Conditions.DECIVILIZED); // && getProgress(intel) > 0;
	}
	
	@Override
	public TooltipCreator getMainRowTooltip(final BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				Color h = Misc.getHighlightColor();
				tooltip.addPara("The Umbra-based ARC is participating in a general uprising, carrying out a campaign of"
						+ " subversion, sabotage, and assassination against the Sindrian Diktat.", 0f, h, "ARC");
				
				// if well supplied ... 
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$imoinuCalledComrade")) {
					tooltip.addPara("Through Imoinu Kato you have provided the ARC with heavy weapons, fuel,"
							+ " and supplies which greatly increases their effectiveness.", 8f, h, "Imoinu Kato");
				}
				
				if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) {
					tooltip.addPara("The ARC is receiving additional materiel from Arroyo's smuggling operation.", 8f, h, "Arroyo");
				}
				// TODO: and if someone destroys Umbra..
				// Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstRao"))
				
				if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_missionCompleted") &&
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverEveryone") && 
					Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverRobedMan")) {
						
					tooltip.addPara("A talented young pilot has led the ARC on several daring raids, freeing political prisoners from Cruor "
							+ "and infiltrating heavily-defended Diktat military installations. "
							+ "A happenstance holo-image of this mop-haired youth's grimly determined expression has spread throughout "
							+ "the Sector as a symbol of any and all rebellion against authority.", 8f, h, "youth");
				
				}
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis") &&
					(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario") || 
					Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac"))) {
					tooltip.addPara("Sindrian Diktat Absolutist forces have suffered from a series of inexplicable "
							+ "intelligence failures in their attempts to deal with the ARC rebels.", 8f, h, "inexplicable");
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
		
		int _multiplier = 0;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveFuelARC")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesARC")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveWeaponsARC")) _multiplier++;
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$imoinuCalledComrade")) _multiplier++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) _multiplier++;
		
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_missionCompleted") &&
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverEveryone") && 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverRobedMan")) {
			_multiplier++;
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis") &&
			(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac"))) {
			_multiplier++;
		}
			
		return SDDPoints.ARC_BASE + SDDPoints.ARC_EFFECT * _multiplier;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "ARC insurgency";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}