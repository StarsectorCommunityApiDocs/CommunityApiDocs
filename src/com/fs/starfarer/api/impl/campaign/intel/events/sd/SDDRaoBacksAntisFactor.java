package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SDDRaoBacksAntisFactor extends BaseEventFactor {
	
	//protected MarketAPI market;
	
	public SDDRaoBacksAntisFactor() {
		//this.market = Global.getSector().getEconomy().getMarket("ragnar_complex");
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

				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_knowsAboutHegIntervention")) {
					tooltip.addPara("Orcus Rao has organized the Hegemony's covert backing of the United Front of the Askonian Resistance, providing intel,"
							+ " credits, and weapons for their parallel campaigns of subversion and sabotage against the Sindrian Diktat.",
							0f, h, "Orcus Rao");
				}
				else {
					tooltip.addPara("There are rumors that the Hegemony is covertly backing the United Front of the Askonian Resistance, providing intel,"
						+ " credits, and weapons for their parallel campaigns of subversion and sabotage against the Sindrian Diktat.\n\nThe name Orcus Rao is"
						+ " whispered in association with covert shipments of arms, supplies, and credits.",
						8f, h, "Orcus Rao");
				}
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksHegemony")) {
					tooltip.addPara("Horus Yaribay is channelling support to Askonian rebels in close coordination with suspected Hegemony covert action.",
							8f, h, "Horus Yaribay");
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_knowsAboutHegIntervention")) {
					tooltip.addPara("Horus Yaribay is channelling support to Askonian rebels in close coordination with Orcus Rao's covert campain.",
							8f, h, "Horus Yaribay");
				}
				
				boolean _disruption = false;
				MarketAPI market = Global.getSector().getEconomy().getMarket("ragnar_complex");
				if(market != null) {
					Industry ind1 = market.getIndustry(Industries.MILITARYBASE);
					Industry ind2 = market.getIndustry(Industries.SPACEPORT);
					Industry ind3 = market.getIndustry(Industries.BATTLESTATION);
					if (ind1 != null && ind1.isDisrupted()) _disruption = true;
					if (ind2 != null && ind2.isDisrupted()) _disruption = true;
					if (ind3 != null && ind3.isDisrupted()) _disruption = true;
				}
				
				//System.out.println("_disruption: " + _disruption); 

				if(_disruption) {
					if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_knowsAboutHegIntervention")) {
						tooltip.addPara("Recent attacks on Ragnar Complex have disrupted the Hegemony's covert intervention in the Second Askonia Crisis.",
							8f, h, "Ragnar Complex");
					}
					else {
						tooltip.addPara("Recent attacks on Ragnar Complex are rumored to have disrupted the suspected Hegemony covert intervention in the Second Askonia Crisis.",
							8f, h, "Ragnar Complex");
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
		int _amount = SDDPoints.RAO_EFFECT;
		MarketAPI market = Global.getSector().getEconomy().getMarket("ragnar_complex");
		Industry ind1 = market.getIndustry(Industries.MILITARYBASE);
		Industry ind2 = market.getIndustry(Industries.SPACEPORT);
		Industry ind3 = market.getIndustry(Industries.BATTLESTATION);
		if (ind1 != null && ind1.isDisrupted()) _amount -= 2;
		if (ind2 != null && ind2.isDisrupted()) _amount -= 2;
		if (ind3 != null && ind3.isDisrupted()) _amount -= 2;
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksHegemony")) {
			_amount += SDRPoints.YARIBAY_SUPPORT;
		}
		return _amount;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_knowsAboutHegIntervention")) {
			return "Orcus Rao's secret war";
		}
		else {
			return "Covert Hegemony? intervention";
		}
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) != 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

	
}