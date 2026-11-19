package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.util.Pair;

public interface IndustryMaxDeficitModifierListener {
	void modifyIndustryMaxDeficit(Industry ind, Pair<String, Integer> deficit, String ... commodityIds);
}
