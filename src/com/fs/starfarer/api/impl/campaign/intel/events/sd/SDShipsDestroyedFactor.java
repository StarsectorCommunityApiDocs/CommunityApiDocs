package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class SDShipsDestroyedFactor extends BaseOneTimeFactor {
	

	public static enum ShipsDestroyedType {
		GENERAL,
		REFORMER,
		ABSOLUTIST,
	}
	
	protected ShipsDestroyedType type;
	public SDShipsDestroyedFactor(int points, ShipsDestroyedType type) {
		super(points);
		this.type = type;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		if (type == ShipsDestroyedType.REFORMER) {
			return "Reformer fleet elements destroyed";
		} else if (type == ShipsDestroyedType.ABSOLUTIST) {
			return "Absolutist fleet elements destroyed";
		}
		return "Sindrian Diktat fleet elements destroyed";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				if (type == ShipsDestroyedType.REFORMER) {
					tooltip.addPara("Destroyed Sindrian Diktat fleet elements aligned with Achilles Andrada.", 0f);
				} else if (type == ShipsDestroyedType.ABSOLUTIST) {
					tooltip.addPara("Destroyed Sindrian Diktat fleet elements aligned with Hector Andrada.", 0f);
				} else {
					tooltip.addPara("Destroyed Sindrian Diktat fleet elements.", 0f);
				}
			}
			
		};
	}
	
}
