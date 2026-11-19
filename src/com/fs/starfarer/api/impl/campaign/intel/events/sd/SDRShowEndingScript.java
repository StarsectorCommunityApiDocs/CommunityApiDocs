package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDevastation.Stage;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDResolution.StandoffWinner;
import com.fs.starfarer.api.util.Misc;

public class SDRShowEndingScript implements EveryFrameScript {
	public static Logger log = Logger.getLogger(SDResolution.class);
	protected float daysLeft;
	protected Boolean done = null;
	protected StandoffWinner winner;
	protected Stage devastation;
	protected SectorEntityToken trigger;
	protected float triggerRange;
	
	public SDRShowEndingScript(StandoffWinner winner, SDDevastation.Stage devastation, SectorEntityToken trigger, float triggerRange) {
		this.winner = winner;
		this.devastation = devastation;
		
		this.trigger = trigger;
		this.triggerRange = triggerRange;
		
//		daysLeft = 0.3f;
//		// give it a little time to simmer
//		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$cadenBlowsUpSindria")) {
//			daysLeft =  1.0f;
//		}
		daysLeft = 3f;
		// give it a little time to simmer
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$cadenBlowsUpSindria")) {
			daysLeft =  5.0f;
		}
	}

	
	public void advance(float amount) {
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		daysLeft -= days;
		
		if (trigger != null) {
			if (trigger.isInCurrentLocation() && 
					Misc.getDistance(Global.getSector().getPlayerFleet(), trigger) < triggerRange) {
				daysLeft = 0f;
			}
		}
		
		if (daysLeft <= 0) {
			
			CampaignUIAPI ui = Global.getSector().getCampaignUI();
			if (ui.isShowingDialog()) {
				// this would spam the log as it happens every frame, it's just waiting for the UI to 
				// stop showing a dialog so it can show this one -am
				//log.info("aborting due to ui showing dialog?");
				return;
			}
			
			Global.getSector().getMemoryWithoutUpdate().set("$supremeExecutorWinner", winner.name());
			Global.getSector().getMemoryWithoutUpdate().set("$supremeExecutorDevastation", devastation.name());
			MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
			if (sindria == null) {
				// something went badly wrong somewhere, just bail on this permanently
				done = true;
				// I think the Caden PK sequence is triggered by this very dialog so Caden would not
				// be the *cause* of this state -am
				//log.info("aborting due to null Sindria! CADENNNN!!");
				return;
			}
			
			boolean shown = Misc.showRuleDialog(sindria.getPrimaryEntity(), "SupremeExecutorFinished");
			if (shown) {
				done = true;
			} else {
				daysLeft = 0.0f + 0.3f * (float) Math.random();
			}
		}
	}
	
	public boolean isDone() {
		return done != null && done == true;
	}

	public boolean runWhilePaused() {
		return false;
	}
}




