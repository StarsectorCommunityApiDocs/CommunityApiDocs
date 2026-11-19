package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.Industry;

/**
 * Note: these methods will also get called when creating certain tooltips etc, it should not be assumed that
 * they ONLY get called as part of the normal functioning of the economy.
 * 
 * @author Alex
 *
 */
public interface IndustryApplyListener {
    void reportIndustryAboutToBeApplied(Industry ind);
    void reportIndustryApplied(Industry ind);
}
