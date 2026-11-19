package com.fs.starfarer.api.impl.campaign;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;

public class RetirementScript implements EveryFrameScript, EconomyTickListener {
	public static String PLAYER_POSTPONED_RETIREMENT = "$ragingAgainstTheDyingOfTheLight";
	public static String KEY = "$retirementScript_ref";
	
	public static Logger log = Logger.getLogger(RetirementScript.class);
	//protected Boolean done = null;
	protected long retirementTimestamp;
	protected int retirementCost;
	
	protected boolean shown = false;
	protected boolean forceTrigger = false;
	protected float triggerAfterDelay = 0f;
	
	public static RetirementScript get() {
		return (RetirementScript) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public RetirementScript() {
		float min = Global.getSettings().getFloat("ext_retirementMinCycles");
		float max = Global.getSettings().getFloat("ext_retirementMaxCycles");
		float cycles = min + (max - min) * StarSystemGenerator.random.nextFloat();
		
		retirementCost = Global.getSettings().getInt("ext_retirementDelayCostBase");
		
		CampaignClockAPI clock = Global.getSector().getClock().createClock(Global.getSector().getClock().getTimestamp());
		clock.set(clock.getCycle() + (int) cycles, 
				StarSystemGenerator.random.nextInt(12), StarSystemGenerator.random.nextInt(28));
		retirementTimestamp = clock.getTimestamp();
		
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	public void showAfterDelay(float delay) {
		triggerAfterDelay = delay;
		forceTrigger = true;
	}
	
	public static boolean isPostponedRetirement() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PLAYER_POSTPONED_RETIREMENT);
	}
	
	public static boolean isEnabled() {
		float min = Global.getSettings().getFloat("ext_retirementMinCycles");
		if (min <= 0) return false;
		
		boolean extOnly = Global.getSettings().getBoolean("retirementEnabledExtModeOnly");
		return !extOnly || Misc.isExtended();
	}

	public void advance(float amount) {
		if (isDone()) return;
		
//		if (get() == null) {
//			Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
//		}
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//		}
		
		long ts = Global.getSector().getClock().getTimestamp();
		
		boolean forceShow = false;
		if (forceTrigger) {
			float days = Global.getSector().getClock().convertToDays(amount);
			triggerAfterDelay -= days;
			if (triggerAfterDelay <= 0f) {
				shown = false;
				forceShow = true;
				triggerAfterDelay = 0f;
			}
		}
		
		if (!isEnabled() && !forceShow) return;
		
		//ts = retirementTimestamp;
		if (!shown && (ts >= retirementTimestamp || forceShow)) {
			CampaignUIAPI ui = Global.getSector().getCampaignUI();
			if (ui.isShowingDialog()) { 
				return;
			}
			SectorEntityToken token = Global.getSector().getCurrentLocation().createToken(0, 0);
			token.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_retirement_screen");
			String trigger = "BeginRetirement";
			if (forceShow) trigger = "BeginRetirementOptional";
			shown = Misc.showRuleDialog(token, trigger);
			if (shown) {
				if (forceTrigger) {
					shown = false; // so that it will show again when time runs out
				}
				forceTrigger = false;
			}
//			if (shown) {
//				done = true;
//			}
		}
	}
	
	public boolean isDone() {
		//return done != null && done == true;
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	@Override
	public void reportEconomyTick(int iterIndex) {
		
	}

	@Override
	public void reportEconomyMonthEnd() {
		if (!isEnabled()) return;

		if (!isPostponedRetirement()) return;
		
		int addCost = Global.getSettings().getInt("ext_retirementDelayCostPerMonth");
		int maxCost = Global.getSettings().getInt("ext_retirementDelayCostMax");
		
		int cost = retirementCost;
		retirementCost += addCost;
		if (retirementCost > maxCost) {
			retirementCost = maxCost;
		}
	
		if (cost <= 0) return;
		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode ret = report.getNode(MonthlyReport.POSTPONE_RETIREMENT);
		ret.upkeep += cost;
		ret.name = "Rejuvenation treatments & cybernetics";
		ret.custom = MonthlyReport.POSTPONE_RETIREMENT;
		ret.tooltipCreator = report.getMonthlyReportTooltip();
	}
	
	
}









