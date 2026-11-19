package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRVolturnVSDCFactor extends BaseEventFactor {
	
	// Whoever owns Volturn - Achilles or Hector - gets a hit due to the VSDC insurgency.
	protected MarketAPI market;

	public SDRVolturnVSDCFactor() {
		this.market = Global.getSector().getEconomy().getMarket("volturn");
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
				tooltip.addPara("The uprising of the Volturn Self Defense Covenant is a drain on the resources and legitimacy of the Sindrian Diktat occupiers."
						+ " The VSDC makes no distinction between Achilles' reformers and Hector's absolutists, viewing both as enemies of Blessed Peace.", 0f, h, "no distinction");
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) {
					tooltip.addPara("Arroyo's smuggling operation is supporting the insurrection on Volturn.", 8f, h, "Arroyo");
				}
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_missionCompleted")) {
					if(	!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverEveryone") && 
						!Global.getSector().getMemoryWithoutUpdate().getBoolean("$anh_handedOverRobedMan")) {
						tooltip.addPara("The VSDC is displaying unusually skillful operational planning, as if their forces were advised by an experienced officer.", 8f, h, "experienced officer");
					}
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
		
		int _multiplier = 1;
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

		String _owner = market.getFactionId();
		if(Factions.DIKTAT.equals(_owner)) {
			return (SDRPoints.VSDC_HINDER_ACHILLES + SDRPoints.VSDC_HINDER_ACHILLES) * -1 * _multiplier;
		}
		else if(Factions.DIKTAT_ACHILLES.equals(_owner)) {
			return (SDRPoints.VSDC_HINDER_ACHILLES + SDRPoints.VSDC_HINDER_ACHILLES) * _multiplier;
		}
		
		return 0;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "VSDC uprising on Volturn";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}