package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRGensKatoFactor extends BaseEventFactor {
	
	public SDRGensKatoFactor() {
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
				
				tooltip.addPara("Fortuna Kato is using the wealth and influence of Gens Kato to openly support "
						+ "Achilles Andrada and his reformer faction as the next Supreme Executor.", 0f, h, "Fortuna Kato");
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hannanBlockingKato")) {
					tooltip.addPara("However, Reynard Hannan is using the power of the office of Prime Demarchon to undercut Gens Kato support "
							+ "for Achilles Andrada through various bureaucratic maneuvers.", 8f, h, "Reynard Hannan");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hannanBlockingKato") &&
					Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
					
					tooltip.addPara("Reynard Hannan is trying to use the power of the office of Prime Demarchon to undercut Gens Kato support "
							+ "for Achilles Andrada through various bureaucratic maneuvers.", 8f, h, "Reynard Hannan");
				}
				//$global.sdr_hannanBlockingKatoResolved
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
					tooltip.addPara("Horus Yaribay has thrown the support of Gens Yaribay behind Fortuna "
							+ "Kato's efforts to install Achilles Andrada as the next Supreme Executor. "
							+ "The influence of Gens Kato and Yaribay together are able to override the veto of the Prime Demarchon.", 8f, h, "Horus Yaribay");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksReynard")) {
					tooltip.addPara("Horus Yaribay has thrown the support of Gens Yaribay behind Reynard Hannan. "
							+ "With overwhelming votes in the All-League Council the Prime Demarchon may legally "
							+ "draw upon exceptional powers to stymie Fortuna Kato's efforts.", 8f, h, "Horus Yaribay");
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
		int _amount = SDRPoints.KATO_SUPPORT; // this happens regardless!
		
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
			_amount = SDRPoints.KATO_SUPPORT_PRO_HECTOR;
		}
		
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hannanBlockingKato") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
			_amount += SDRPoints.HANNAN_BLOCKS_SUPPORT;
		}
		//$global.sdr_hannanBlockingKatoResolved
				
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
			_amount += SDRPoints.YARIBAY_SUPPORT;
		}
		else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksReynard")) {
			_amount -= SDRPoints.YARIBAY_SUPPORT;
		}
					
		return _amount;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Gens Kato openly backs Achilles Andrada";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}
}





