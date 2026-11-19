package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDRInesisFactor extends BaseEventFactor {
	
	public SDRInesisFactor() {
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
				
				tooltip.addPara("Faro Inesis's network of spies and black-tech contacts allows him to feed technology and intel to "
							+ "whoever happens to be his present ally and disinformation to whoever finds themselves to be his enemy.", 0f, h, "Faro Inesis");
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAchilles")) {
					tooltip.addPara("He is providing support to Achilles and his Reformer faction to diminish Fortuna Kato's influence.", 8f, h, "Achilles");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac")) {
					tooltip.addPara("He is providing support to Hector and his Absolutist faction while specifically attacking Dolos Macario's intelligence apparatus.", 8f, h, "Hector");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector")) {
					tooltip.addPara("He is providing support to Hector and his absolutist faction.", 8f, h, "Hector");
				}
				else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario")) {
					tooltip.addPara("At your request, he is targeting Dolos Macario's intelligence apparatus. This slightly "
							+ "impairs Hector's Absolutists - and will reduce Macario's influence.", 8f, h, "Dolos Macario");
				}

				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis")) {
					tooltip.addPara("With your assistance, Inesis has infiltrated the Askonian communications network, enhancing "
							+ "his ability to manipulate involved parties.", 8f, h, "communications network");
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
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis")) {
			_mult = 2;
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAchilles")) {
			return SDRPoints.INESIS * _mult;
		}
		else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac")) {
			return SDRPoints.INESIS * -_mult;
		}
		else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector")) {
			_mult++; // because there's no extra shenanigans
			return SDRPoints.INESIS * -_mult;
		}
		else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario")) {
			return SDRPoints.INESIS; // * -_mult;
		}
		
		// default is support for Hector at a low level.
		// but this shouldn't actually happen - only show Inesis influence if PC gets involved.
		return 0; //-SDRPoints.INESIS;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		String _title = "Faro Inesis' intelligence operation";
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector")) {
			_title = "Faro Inesis' intel and logistics operation";
		}
		
		return _title;
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}
}





