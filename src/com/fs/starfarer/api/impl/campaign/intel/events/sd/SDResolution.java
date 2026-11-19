package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import java.awt.Color;

import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.MarketInteractionMode;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetHoracioCaden;
import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetOxanaHyder;
import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetScript;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.FactionHostilityIntel;
import com.fs.starfarer.api.impl.campaign.intel.FactionHostilityManager;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.askonia.SDSevenSons;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerAction;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.impl.campaign.missions.hub.TriggerFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Commission;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SDResolution extends BaseEventIntel implements FleetEventListener {
	
	public static enum StandoffWinner {
		HECTOR,
		ACHILLES,
	}
	
	public static float PERSONAL_FLEET_DEFEAT_TIMEOUT = 30f;
	
	public static int ACHILLES = 200;
	public static int HECTOR_ATTACK = 160;
	public static int ACHILLES_ATTACK = 40;
	public static int HECTOR = 0;
	
	public static String KEY = "$sdr_ref";
	public static Logger log = Logger.getLogger(SDResolution.class);
	
	public static LinkedHashSet<String> INVOLVED_MARKETS = new LinkedHashSet<>();
	static {
		INVOLVED_MARKETS.add("yesod");
		INVOLVED_MARKETS.add("orthrus");
		INVOLVED_MARKETS.add("nomios");
		INVOLVED_MARKETS.add("qaras");
		INVOLVED_MARKETS.add("sindria");
		INVOLVED_MARKETS.add("volturn");
		INVOLVED_MARKETS.add("umbra");
		INVOLVED_MARKETS.add("nortia");
		INVOLVED_MARKETS.add("cruor");
	}
	
	public static boolean isAskoniaStandoffCompleted() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$askoniaStandoffCompleted");
	}
	public static boolean isAskoniaStandoffInProgress() {
		return get() != null;
	}
	
	public static enum Stage {
		HECTOR,
		ACHILLES_ATTACK,
		HECTOR_ATTACK,
		ACHILLES,
	}
	

	public static void addFactorCreateIfNecessary(EventFactor factor, InteractionDialogAPI dialog) {
		if (get() == null) {
			new SDResolution(null, false);
		}
		if (get() != null) {
			get().addFactor(factor, dialog);
		}
	}
	
	public static SDResolution get() {
		return (SDResolution) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	
	protected TimeoutTracker<String> personalFleetDefeats = new TimeoutTracker<>();
	protected boolean achillesArrived = false;
	protected boolean didLastDitchAttack = false;
	
	protected boolean triedAddingFuelShortage = false;
	
	protected boolean hectorWinReached = false;
	protected boolean achillesWinReached = false;
	
	public SDResolution(TextPanelAPI text, boolean withIntelNotification) {
		super();
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		setup();
		
		// now that the event is fully constructed, add it and send notification
		Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
	}
	
	protected Object readResolve() {
		boolean recreate = true;
		for (EventStageData curr : stages) {
			if (curr.id == Stage.ACHILLES_ATTACK) {
				recreate = false;
				break;
			}
		}
		
		if (recreate) {
			setup();
		}
		
		return this;
	}


	protected void setup() {
		factors.clear();
		stages.clear();
		
		setMaxProgress(ACHILLES);
		
		addStage(Stage.HECTOR, HECTOR, StageIconSize.LARGE);
		addStage(Stage.ACHILLES_ATTACK, ACHILLES_ATTACK, StageIconSize.MEDIUM);
		addStage(Stage.HECTOR_ATTACK, HECTOR_ATTACK, StageIconSize.MEDIUM);
		addStage(Stage.ACHILLES, ACHILLES, StageIconSize.LARGE);
		
		getDataFor(Stage.HECTOR).showIconIfFirstStage = true;
		
		getDataFor(Stage.HECTOR).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.HECTOR).alwaysShowIconAtFullBrightness = true;
		getDataFor(Stage.HECTOR).sendIntelUpdateOnReaching = false;
		
		getDataFor(Stage.ACHILLES).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.ACHILLES).alwaysShowIconAtFullBrightness = true;
		getDataFor(Stage.ACHILLES).sendIntelUpdateOnReaching = false;
		
		getDataFor(Stage.ACHILLES_ATTACK).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.ACHILLES_ATTACK).alwaysShowIconAtFullBrightness = true;
		getDataFor(Stage.ACHILLES_ATTACK).sendIntelUpdateOnReaching = false;
		
		getDataFor(Stage.HECTOR_ATTACK).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.HECTOR_ATTACK).alwaysShowIconAtFullBrightness = true;
		getDataFor(Stage.HECTOR_ATTACK).sendIntelUpdateOnReaching = false;
		
		addFactor(new SDRMacarioFactor());
		
		setProgress(maxProgress / 2);
		
		hectorWinReached = false;
		achillesWinReached = false;
	}
	
	public boolean isAchillesArrived() {
		return achillesArrived;
	}

	public void setAchillesArrived(boolean achillesArrived) {
		this.achillesArrived = achillesArrived;
	}

	public boolean allowPersonalFleetRewpawns() {
		return false;
	}
	
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, 
			   						Color tc, float initPad) {
		
		if (addEventFactorBulletPoints(info, mode, isUpdate, tc, initPad)) {
			return;
		}
		
		Color h = Misc.getHighlightColor();
		if (isUpdate && getListInfoParam() instanceof EventStageData) {
			EventStageData esd = (EventStageData) getListInfoParam();
			
			if (esd.id == Stage.HECTOR) {
				info.addPara("Hector Andrada declares victory", tc, initPad);
			} else if (esd.id == Stage.ACHILLES) {
				info.addPara("Achilles Andrada declares victory", tc, initPad);
			} else if (esd.id == Stage.ACHILLES_ATTACK) {
				info.addPara("The forces of Achilles launch a last-ditch attack", tc, initPad);
				// TODO hyder notification? maybe in tooltip?
			} else if (esd.id == Stage.HECTOR_ATTACK) {
				info.addPara("Hector's forces launch a last-ditch attack", tc, initPad);
			}
			
			return;
		}
	}

	
	public float getImageSizeForStageDesc(Object stageId) {
		return super.getImageSizeForStageDesc(stageId);
	}
	public float getImageIndentForStageDesc(Object stageId) {
		return super.getImageIndentForStageDesc(stageId);
	}

	@Override
	public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
		float opad = 10f;
		float small = 0f;
		Color h = Misc.getHighlightColor();
		
		//setProgress(0);
		//setProgress(199);
		//setProgress(600);
		//setProgress(899);
		//setProgress(1000);
		//setProgress(499);
		//setProgress(600);
		
		EventStageData stage = getDataFor(stageId);
		if (stage == null) return;
		
		if (isStageActiveAndLast(stageId)) {
			addStageDesc(info, stageId, small, false);
		}
	}
	
	
	public void addStageDesc(TooltipMakerAPI info, Object stageId, float initPad, boolean forTooltip) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		//if (true) return;
//		if (stageId == Stage.ACHILLES_ATTACK) {
//			info.addPara("The forces of Achilles are preparing a desperate last attack that will decide the issue once and for all.", initPad);			
//		} else if (stageId == Stage.HECTOR_ATTACK) {
//			info.addPara("Hector's forces are preparing a desperate last attack that will decide the issue once and for all.", initPad);			
//		} else {
			info.addPara("Determines whether Hector or Achilles - or what remains of their faction - ultimately wins control "
					+ "of the Askonia system.", initPad);
//		}
	}
	
	public TooltipCreator getStageTooltipImpl(Object stageId) {
		final EventStageData esd = getDataFor(stageId);
		
		if (esd != null) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					
//					if (stageId == Stage.ACHILLES_ATTACK) {
////						info.addPara("The forces of Achilles are preparing a desperate last attack that will decide the issue once and for all.", initPad);			
////					} else if (stageId == Stage.HECTOR_ATTACK) {
////						info.addPara("Hector's forces are preparing a desperate last attack that will decide the issue once and for all.", initPad);			
////					} else {
					//if(esd.id == Stage)
					if (esd.id == Stage.HECTOR) {
						tooltip.addTitle("Hector Victory");
						tooltip.addPara("Hector Andrada seeks to claim legitimacy as heir to Philip Andrada. "
								+ "He is backed by the Absolutist faction which believes in upholding the absolute and unlimited power of the Supreme Executor and his right to conquest.", opad);
					} else if (esd.id == Stage.ACHILLES) {
						tooltip.addTitle("Achilles Victory");
						tooltip.addPara("Achilles seeks to claim legitimacy as heir to Philip Andrada. "
								+ "He is backed by the Reformer faction which believes in reforming the ossified hierarchy of the Sindrian Diktat while building alliances with outside factions.", opad);
					} else if (esd.id == Stage.ACHILLES_ATTACK) {
						tooltip.addTitle("Impending Defeat of Achilles");
						tooltip.addPara("With all nearly lost, the forces of Achilles will launch a "
								+ "desperate last attack that will decide the issue once and for all.", opad);
					} else if (esd.id == Stage.HECTOR_ATTACK) {
						tooltip.addTitle("Impending Defeat of Hector");
						tooltip.addPara("With all nearly lost, Hector's forces will launch a "
								+ "desperate last attack that will decide the issue once and for all.", opad);
					}
					esd.addProgressReq(tooltip, opad);
				}
			};
		}
		
		return null;
	}


	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("events", "sd_resolution");
	}
	
	public PersonAPI getHector() {
		PersonAPI person = People.getPerson(People.HECTOR);
		if (person == null) {
			SDSevenSons.addLargeSons();
			person = People.getPerson(People.HECTOR);
		}
		return person;
	}
	public PersonAPI getAchilles() {
		PersonAPI person = People.getPerson(People.ACHILLES);
		if (person == null) {
			SDSevenSons.addLargeSons();
			person = People.getPerson(People.ACHILLES);
		}
		return person;
	}

	public String getImageOverrideForStage(Object stageId) {
//		if (stageId == Stage.ACHILLES_ATTACK || stageId == Stage.HECTOR_ATTACK) {
//			return null;
//		}
		return Global.getSettings().getSpriteName("events", "sd_resolution_stage");
	}
	
	protected String getStageIconImpl(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null) return null;
		
	
		if (esd.id == Stage.HECTOR) {
			return getHector().getPortraitSprite();
		}
		if (esd.id == Stage.ACHILLES) {
			return getAchilles().getPortraitSprite();
		}
		if (esd.id == Stage.ACHILLES_ATTACK) {
			return Global.getSettings().getSpriteName("events", "sd_resolution_imminent_attack_achilles");
		}
		if (esd.id == Stage.HECTOR_ATTACK) {
			return Global.getSettings().getSpriteName("events", "sd_resolution_imminent_attack_hector");
		}
		// should not happen
		return Global.getSettings().getSpriteName("events", "sd_resolution");
	}
	
	@Override
	public float getBarWidth() {
		return 700f;
		//return super.getBarWidth();
	}
	
	@Override
	public float getBarXOffset() {
		return 9f;
	}

	@Override
	public Color getBarColor() {
		Color color = Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor();
		color = Misc.interpolateColor(color, Color.black, 0.25f);
		return color;
	}
	
	@Override
	public Color getBarProgressIndicatorColor() {
		return super.getBarProgressIndicatorColor();
	}

	@Override
	protected int getStageImportance(Object stageId) {
		return super.getStageImportance(stageId);
	}


	@Override
	protected String getName() {
		return "Sindrian Diktat Civil War";
	}
	

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		
	}
	
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isEnded() || isEnding()) return;
		
	}
	

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_STORY);
		tags.add(Factions.DIKTAT);
		//tags.remove(Tags.INTEL_MAJOR_EVENT);
		return tags;
	}
	

	@Override
	public void setProgress(int progress) {
		if (progress <= HECTOR) {
			hectorWinReached = true;
		}
		if (progress >= ACHILLES) {
			achillesWinReached = true;
		}
		super.setProgress(progress);
	}

	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		//Global.getSector().getMemoryWithoutUpdate().set("$askoniaStandoff", true);
		
		// just to make it work for existing saves
		if (!triedAddingFuelShortage) {
			if (SDFuelShortage.get() == null) {
				new SDFuelShortage();
			}
			triedAddingFuelShortage = true;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		personalFleetDefeats.advance(days);
		
		//if (amount > 0) progress++;
//		setProgress(180);
//		setProgress(20);
//		setProgress(200);
//		setProgress(0);
		
		
//		EventStageData hector = getDataFor(Stage.HECTOR);
//		EventStageData achilles = getDataFor(Stage.ACHILLES);
//		if (hector != null && achilles != null && (hector.wasEverReached || achilles.wasEverReached)) {
		//if (progress <= HECTOR || progress >= ACHILLES) {
		if (hectorWinReached || achillesWinReached) {
			endImmediately();
			//StandoffWinner winner = progress <= HECTOR ? StandoffWinner.HECTOR : StandoffWinner.ACHILLES;
			//StandoffWinner winner = hector.wasEverReached ? StandoffWinner.HECTOR : StandoffWinner.ACHILLES;
			StandoffWinner winner = !achillesWinReached ? StandoffWinner.HECTOR : StandoffWinner.ACHILLES;
			endStandoffInAskonia(winner);
			if (winner == StandoffWinner.HECTOR) {
				sendUpdateIfPlayerHasIntel(getDataFor(Stage.HECTOR), getTextPanelForStageChange());
				Global.getSector().getMemoryWithoutUpdate().set("$sdse_hectorWon", true);
			} else {
				sendUpdateIfPlayerHasIntel(getDataFor(Stage.ACHILLES), getTextPanelForStageChange());
				Global.getSector().getMemoryWithoutUpdate().set("$sdse_achillesWon", true);
			}
			return;
		}
		
		//doLastDitchAttackAchilles();
		if (!didLastDitchAttack && achillesArrived) {
			if (progress <= ACHILLES_ATTACK) {
				doLastDitchAttackAchilles();
				sendUpdateIfPlayerHasIntel(getDataFor(Stage.ACHILLES_ATTACK), getTextPanelForStageChange());
				didLastDitchAttack = true;
				return;
			} else if (progress >= HECTOR_ATTACK) {
				doLastDitchAttackHector();
				sendUpdateIfPlayerHasIntel(getDataFor(Stage.HECTOR_ATTACK), getTextPanelForStageChange());
				didLastDitchAttack = true;
				return;
			}
		}
	}
	

	@Override
	protected void notifyStageReached(EventStageData stage) {
	}
	
	public boolean withMonthlyFactors() {
		return true;
	}
	
	protected String getSoundForStageReachedUpdate(Object stageId) {
		return super.getSoundForStageReachedUpdate(stageId);
	}

	@Override
	protected String getSoundForOneTimeFactorUpdate(EventFactor factor) {
		return null;
	}
	
	
	public static String MODIFIER_ID = "SDResolution";
	public static String STORY_CRITICAL_ID = "SDResolution";

	public static String REFORMER_FLEET_RETURNING_TO_PL = "$reformerFleetReturningToPL";
	
	
	
	public static boolean canBeginStandoffInAskonia() {
		for (String mid : INVOLVED_MARKETS) { 
			MarketAPI market = Global.getSector().getEconomy().getMarket(mid);
			if (market == null || market.hasCondition(Conditions.DECIVILIZED)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean beginStandoffInAskonia(TextPanelAPI textPanel, boolean withIntel) {
		if (!canBeginStandoffInAskonia()) {
			return false;
		}
		
		Global.getSector().getMemoryWithoutUpdate().set("$askoniaStandoff", true);
		
		new SDDevastation(textPanel, false);
		new SDResolution(textPanel, true);
		
		new SDFuelShortage();
		
		
		for (String mid : INVOLVED_MARKETS) { 
			Misc.makeStoryCritical(mid, STORY_CRITICAL_ID);
		}
		
		CampaignFleetAPI hyder = strengthenAndGetPersonalFleet(PersonalFleetOxanaHyder.class, true);
		CampaignFleetAPI caden = strengthenAndGetPersonalFleet(PersonalFleetHoracioCaden.class, true);
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		StarSystemAPI askonia = sindria.getStarSystem();
		if (hyder == null || caden == null || sindria == null || volturn == null ||  
				askonia == null) return false;
		
		sindria.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).modifyMult(MODIFIER_ID, 0f);
		askonia.getMemoryWithoutUpdate().set(MemFlags.SYSTEM_SUSPEND_MILITARY_RESPONSE_SCRIPT, true);
		
		for (int i = 0; i < 5; i++) {
			PatrolType type = PatrolType.HEAVY;
			if (i >= 3) {
				type = PatrolType.COMBAT;
			}
			CampaignFleetAPI fleet = MilitaryBase.createPatrol(type, 0f, Factions.DIKTAT, null, null, askonia.getLocation(), null);
			fleet.setLocation(sindria.getPrimaryEntity().getLocation().x, sindria.getPrimaryEntity().getLocation().y);
			askonia.addEntity(fleet);
		}
		
		for (int i = 0; i < 3; i++) {
			PatrolType type = PatrolType.HEAVY;
			if (i >= 3) {
				type = PatrolType.COMBAT;
			}
			CampaignFleetAPI fleet = MilitaryBase.createPatrol(type, 0f, Factions.LIONS_GUARD, null, null, askonia.getLocation(), null);
			fleet.setLocation(sindria.getPrimaryEntity().getLocation().x, sindria.getPrimaryEntity().getLocation().y);
			fleet.setFaction(Factions.DIKTAT);
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				if (member.isCapital()) {
					member.setVariant(member.getVariant().clone(), false, false);
					member.getVariant().setSource(VariantSource.REFIT);
					member.getVariant().addTag(Tags.TAG_NO_AUTOFIT);
					member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
				}
			}
			
			askonia.addEntity(fleet);
		}
		
		for (CampaignFleetAPI curr : askonia.getFleets()) {
			if (!curr.getFaction().getId().equals(Factions.DIKTAT)) continue;
			if (Misc.isPatrol(curr) || Misc.isWarFleet(curr)) {
				curr.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
				curr.removeScriptsOfClass(PatrolAssignmentAIV4.class);
				curr.removeScriptsOfClass(TriggerFleetAssignmentAI.class);
				curr.clearAssignments();
				curr.addScript(new LeashAssignmentAI(curr, askonia, sindria.getPrimaryEntity(), null));
			}
		}
		
		FactionAPI diktat = Global.getSector().getFaction(Factions.DIKTAT);
		FactionHostilityManager.getInstance().setExcluded(Factions.DIKTAT, true);
		for (FactionHostilityIntel intel : FactionHostilityManager.getInstance().getHostilitiesInvolving(diktat)) {
			if (intel.isEnded() || intel.isEnding()) continue;
			intel.endHostilties();
		}
		
		FactionAPI reformers = Global.getSector().getFaction(Factions.DIKTAT_ACHILLES);
		reformers.setShowInIntelTab(true);
		
		diktat.setRelationship(Factions.TRITACHYON, 0.25f);
		diktat.setRelationship(Factions.PERSEAN, -0.25f);
		
		reformers.setRelationship(Factions.TRITACHYON, -0.25f);
		reformers.setRelationship(Factions.PERSEAN, 0.25f);
		reformers.setRelationship(Factions.PIRATES, -0.50f);
		reformers.setRelationship(Factions.LUDDIC_PATH, -0.50f);
		
		// This is a pretty crude way to do it, but what the heck.
		if (Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proAchilles")) {
			if (Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_acceptedAchillesCeremonyInvite")) {
				reformers.setRelationship(Factions.PLAYER, 0.35f);
			}
			else {
				reformers.setRelationship(Factions.PLAYER, 0.25f);
			}
		}
		// Likewise crude.
		if (Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
			reformers.setRelationship(Factions.PLAYER, -0.50f);
		}
		
		
		AchillesForces.createHectorFleet();
		
		sendAchillesForces();
		
		if (	Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_maeusWillFightDolosRogue")) {
			spawnMorozFleet(true);
		}
		else if (	Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_maeusWillHelpFight")) {
			spawnMorozFleet(false);
		}
		
		return true;
	}
	
	public static void sendAchillesForces() {
		AchillesForces.create();
	}
	
	public static void achillesArrivedAtVolturn() {
		if (SDResolution.get() != null) {
			SDResolution.get().setAchillesArrived(true);
		}
		MarketAPI yesod = Global.getSector().getEconomy().getMarket("yesod");
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		MarketAPI nortia = Global.getSector().getEconomy().getMarket("nortia");
		StarSystemAPI askonia = volturn.getStarSystem();
		
		// not needed - already set earlier, just making an existing test save work
		askonia.getMemoryWithoutUpdate().set(MemFlags.SYSTEM_SUSPEND_MILITARY_RESPONSE_SCRIPT, true);
		
		changeFaction(volturn, Factions.DIKTAT_ACHILLES, createRandomAdmin(Factions.DIKTAT_ACHILLES, PersonImportance.MEDIUM), 2);
		
		for (SectorEntityToken obj : askonia.getEntitiesWithTag(Tags.OBJECTIVE)) {
			if (obj.getName().toLowerCase().contains(("navigation"))) {
				obj.setFaction(Factions.DIKTAT_ACHILLES);
				break;
			}
		}
		
		for (CampaignFleetAPI fleet : askonia.getFleets()) {
			if (fleet.getFaction().getId().equals(Factions.DIKTAT_ACHILLES)) {
				if (!fleet.hasScriptOfClass(LeashAssignmentAI.class)) {
					// possible for some fleets to end up without the leash script on arrival, not 100% sure how
					fleet.addScript(new LeashAssignmentAI(fleet, askonia, volturn.getPrimaryEntity(), null));
				}
			}
		}
		
		FactionAPI diktat = Global.getSector().getFaction(Factions.DIKTAT);
		diktat.setRelationship(Factions.DIKTAT_ACHILLES, -1f);
	}
	
	public static void triggerHyderChangesSides() {
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		MarketAPI nortia = Global.getSector().getEconomy().getMarket("nortia");
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		StarSystemAPI askonia = volturn.getStarSystem();

		// lower defection rate if Hyder doesn't put her support behind Achilles.
		float switchSidesChance = 0.3f;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderDecidedToBackAchilles")) {
			switchSidesChance = 0.5f;
		}
		
		Set<CampaignFleetAPI> changingSides = new LinkedHashSet<>();
		CampaignFleetAPI hyder = strengthenAndGetPersonalFleet(PersonalFleetOxanaHyder.class, false);
		
		if( !Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderDecidedToBackHector")) {
			changingSides.add(hyder);
		}
		
		for (CampaignFleetAPI fleet : askonia.getFleets()) {
			if (!fleet.getFaction().getId().equals(Factions.DIKTAT)) continue;
			boolean lg = fleet.getName().toLowerCase().contains("lion's guard");
			if (fleet != hyder && Misc.isPatrol(fleet) && !lg && (float) Math.random() < switchSidesChance) {
				changingSides.add(fleet);
			}
			
			// we need at least one traitor
			if(changingSides.size() == 0 && fleet != hyder && Misc.isPatrol(fleet) && !lg) {
				changingSides.add(fleet);
			}
		}
				
		for (CampaignFleetAPI fleet : changingSides) {
			fleet.clearAssignments();
			fleet.removeScriptsOfClass(LeashAssignmentAI.class);
			fleet.getMemoryWithoutUpdate().set("$sdrChangingSides", true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_BUSY, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, nortia.getPrimaryEntity(), 10000f, "conducting training exercise", new Script() {
				@Override
				public void run() {
					
					if (isAskoniaStandoffCompleted()) {
						// We might be hitting this if we get too enthusiastic during testing, but just to be sure...
						return;
					}
					
					if (fleet.getFaction().getId().equals(Factions.DIKTAT)) {
						
						// announce arrest here, if it's happening.
						if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_macarioArrestHyder") && 
							!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderArrestDone")) {
							
							Global.getSector().getMemoryWithoutUpdate().set("$sdr_hyderArrestDone", true);
							
							// Hyder arrested intel
							MessageIntel intel = new MessageIntel();
							PersonAPI person = Global.getSector().getImportantPeople().getPerson(People.HYDER);
							if(person != null) {
								
								intel.addLine( person.getNameString() + " has been arrested and stripped of command at the order of Chief High Inspector-General Dolos Macario", null, 
											 new String[] {person.getNameString()}, 
											 			   person.getFaction().getColor()); 
								
								intel.setIcon(person.getPortraitSprite());
							}
							
							SDResolution sdr = SDResolution.get();
							sdr.addOrReplaceFactor(new SDRHyderFactor(), null);		
							removeFleetCommander(hyder);
							Global.getSector().getCampaignUI().addMessage(intel);
						}
						
						// announce her defection here, if it's happening.
						if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderDecidedToBackAchilles") && 
							 !Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_macarioArrestHyder") && 
							 !Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderToAchillesDone")) {
							
							Global.getSector().getMemoryWithoutUpdate().set("$sdr_hyderToAchillesDone", true);
							
							// Hyder defection intel alert!
							MessageIntel intel = new MessageIntel();
							PersonAPI person = Global.getSector().getImportantPeople().getPerson(People.HYDER);
							if(person != null) {
								
								String _intelText = person.getNameString() + " has pledged the " + hyder.getName() + " to the cause of Achilles Andrada";
								
								// failed arrest due to player meddling
								if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_macarioArrestHyderFail")) {
									_intelText = person.getNameString() + " thwarted an arrest attempt by INTSEC then pledged the " + hyder.getName() + " to the cause of Achilles Andrada";
								}

								intel.addLine( 	_intelText,
												null, 
												new String[] {person.getNameString()}, 
											 	person.getFaction().getColor()); 
								
								
								intel.setIcon(person.getPortraitSprite());
							}
							
							SDResolution sdr = SDResolution.get();
							sdr.addOrReplaceFactor(new SDRHyderFactor(), null);		
							
							//intel.setIcon(Global.getSettings().getSpriteName("intel_categories", "reputation"));
							Global.getSector().getCampaignUI().addMessage(intel);
						}
						
						// when the first fleet gets there, kick off the change of allegiance for all of them
						for (CampaignFleetAPI fleet2 : changingSides) {
							
							fleet2.getMemoryWithoutUpdate().unset("$sdrChangingSides");
							fleet2.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
							fleet2.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
							
							// if Hyder gets arrested, her fleet returns to Sindria w/o changing sides.
							if(	fleet2 == hyder && 
								Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderDecidedToBackAchiiles") && 
								!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_macarioArrestHyder")) {
								
								fleet2.getMemoryWithoutUpdate().unset(MemFlags.FLEET_BUSY);
								// removeFleetCommander(fleet2); // done in the arrest announcement for ALL cases
								fleet2.addScript(new LeashAssignmentAI(fleet2, askonia, sindria.getPrimaryEntity(), null));
								continue;
							}

							fleet2.getMemoryWithoutUpdate().unset("$sdrChangingSides");
							fleet2.clearAssignments();
							fleet2.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
							fleet2.setFaction(Factions.DIKTAT_ACHILLES);
							fleet2.addScript(new LeashAssignmentAI(fleet2, askonia, volturn.getPrimaryEntity(), "declaring for Achilles"));
						}
					}
				}
			});
		}
	}
	
	public static void doLastDitchAttackAchilles() {
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		StarSystemAPI askonia = sindria.getStarSystem();
		
		// Maeus, if he's here.
		CampaignFleetAPI moroz = getMorozFleet();
		
		for (CampaignFleetAPI curr : askonia.getFleets()) {
			if (!Misc.isPatrol(curr) && !Misc.isWarFleet(curr) && curr != moroz) continue;
			if (curr.getMemoryWithoutUpdate().getBoolean("$sdrChangingSides")) {
				curr.getMemoryWithoutUpdate().unset("$sdrChangingSides");
				curr.clearAssignments();
				curr.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, false);
				curr.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
				curr.setFaction(Factions.DIKTAT_ACHILLES);
				
			}
			if (curr.getFaction().getId().equals(Factions.DIKTAT_ACHILLES) || curr == moroz) {
				curr.removeScriptsOfClass(LeashAssignmentAI.class);
				curr.clearAssignments();
				curr.addAssignment(FleetAssignment.ORBIT_PASSIVE, volturn.getPrimaryEntity(), 3f, "making final preparations", null);
				curr.addAssignment(FleetAssignment.PATROL_SYSTEM, sindria.getPrimaryEntity(), 10000f, 
						"attacking " + sindria.getName());
			}
		}
	}
	
	public static void doLastDitchAttackHector() {
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		StarSystemAPI askonia = sindria.getStarSystem();
		
		for (CampaignFleetAPI curr : askonia.getFleets()) {
			if (!Misc.isPatrol(curr) && !Misc.isWarFleet(curr)) continue;
			if (curr.getMemoryWithoutUpdate().getBoolean("$sdrChangingSides")) {
				curr.getMemoryWithoutUpdate().unset("$sdrChangingSides");
				curr.clearAssignments();
				curr.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, false);
				curr.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
				curr.setFaction(Factions.DIKTAT_ACHILLES);
			}
			if (curr.getFaction().getId().equals(Factions.DIKTAT)) {
				curr.clearAssignments();
				curr.addAssignment(FleetAssignment.ORBIT_PASSIVE, sindria.getPrimaryEntity(), 3f, "making final preparations", null);
				curr.addAssignment(FleetAssignment.PATROL_SYSTEM, volturn.getPrimaryEntity(), 10000f, 
						"attacking " + volturn.getName());
			}
		}
	}
	
	
	public static void endStandoffInAskonia(StandoffWinner winner) {
		log.info("SDResolution starting endStandoffInAskonia");
		FactionAPI reformers = Global.getSector().getFaction(Factions.DIKTAT_ACHILLES);
		reformers.setShowInIntelTab(false);
		
		SDFuelShortage.end();
		
		if (winner == StandoffWinner.ACHILLES) {
			FactionAPI diktat = Global.getSector().getFaction(Factions.DIKTAT);
			diktat.setRelationship(Factions.TRITACHYON, reformers.getRelationship(Factions.TRITACHYON));
			diktat.setRelationship(Factions.PERSEAN, reformers.getRelationship(Factions.PERSEAN));
			diktat.setRelationship(Factions.PLAYER, reformers.getRelationship(Factions.PLAYER));
			diktat.setRelationship(Factions.INDEPENDENT, reformers.getRelationship(Factions.INDEPENDENT));
		}
		
		
		for (String mid : INVOLVED_MARKETS) { 
			Misc.makeNonStoryCritical(mid, STORY_CRITICAL_ID);
		}
		
		Global.getSector().getMemoryWithoutUpdate().unset("$askoniaStandoff");
		Global.getSector().getMemoryWithoutUpdate().set("$askoniaStandoffCompleted", true);
		
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		MarketAPI cruor = Global.getSector().getEconomy().getMarket("cruor");
		MarketAPI nortia = Global.getSector().getEconomy().getMarket("nortia");
		MarketAPI umbra = Global.getSector().getEconomy().getMarket("umbra");
		MarketAPI yesod = Global.getSector().getEconomy().getMarket("yesod");
		
		StarSystemAPI askonia = sindria.getStarSystem();
		StarSystemAPI zagan = yesod == null ? null : yesod.getStarSystem();
		sindria.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).unmodifyMult(MODIFIER_ID);
		askonia.getMemoryWithoutUpdate().unset(MemFlags.SYSTEM_SUSPEND_MILITARY_RESPONSE_SCRIPT);
		
		if (askonia != null) {
			List<CampaignFleetAPI> fleets = new ArrayList<>();
			fleets.addAll(askonia.getFleets());
			fleets.addAll(Global.getSector().getHyperspace().getFleets());
			if (zagan != null) {
				fleets.addAll(zagan.getFleets());
			}
			boolean sindriaBlowsUp = Global.getSector().getMemoryWithoutUpdate().getBoolean("$cadenBlowsUpSindria");
			for (CampaignFleetAPI curr : fleets) {
				if (curr.getFaction().getId().equals(Factions.DIKTAT_ACHILLES)) {
					curr.setFaction(Factions.DIKTAT);
					MarketAPI source = Misc.getSourceMarket(curr);
					if (source != null && source.getFactionId().equals(Factions.PERSEAN)) {
						if (sindriaBlowsUp || winner == StandoffWinner.HECTOR || (float) Math.random() > 0.5f) {
							curr.setFaction(Factions.PERSEAN);
							Misc.giveStandardReturnToSourceAssignments(curr, true);
							curr.getMemoryWithoutUpdate().set(REFORMER_FLEET_RETURNING_TO_PL, true);
						} else {
							curr.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SOURCE_MARKET, "sindria");
							Misc.giveStandardReturnToSourceAssignments(curr, true);
						}
					}
				}
				if (!curr.getFaction().getId().equals(Factions.DIKTAT)) continue;
				if (Misc.isPatrol(curr) || Misc.isWarFleet(curr)) {
					Misc.giveStandardReturnToSourceAssignments(curr, true);
				}
			}
			
			// some of these are handled specifically further down, but this is good baseline. -dgb.
			for (SectorEntityToken obj : askonia.getEntitiesWithTag(Tags.OBJECTIVE)) {
				if (obj.getFaction().getId().equals(Factions.DIKTAT_ACHILLES)) {
					obj.setFaction(Factions.DIKTAT);
					break;
				}
			}
		}
		
		CampaignFleetAPI moroz = getMorozFleet();
		if (moroz != null) {
			moroz.clearAssignments();
			Misc.giveStandardReturnToSourceAssignments(moroz);
			if (winner == StandoffWinner.ACHILLES) {
				Misc.makeHostileToFaction(moroz, Factions.DIKTAT, false, -1f);
			}
		}
		
		removeFleetCommander(PersonalFleetScript.getFleet(PersonalFleetOxanaHyder.class));
		removeFleetCommander(PersonalFleetScript.getFleet(PersonalFleetHoracioCaden.class));
		
		List<CampaignFleetAPI> fleets = new ArrayList<>();
		fleets.addAll(askonia.getFleets());
		fleets.addAll(Global.getSector().getHyperspace().getFleets());
		if (zagan != null) {
			fleets.addAll(zagan.getFleets());
		}
		
		for (CampaignFleetAPI fleet : fleets) {
			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
			if (fleet.getCommander() != null && fleet.getCommander().getId().equals(People.ACHILLES)) {
				removeFleetCommander(fleet);
				fleet.setNoFactionInName(false);
				fleet.setName(fleet.getFaction().getFleetTypeName(FleetTypes.PATROL_LARGE));
			} 
			else if (fleet.getCommander() != null && fleet.getCommander().getId().equals(People.HECTOR)) {
				removeFleetCommander(fleet);
				fleet.setNoFactionInName(false);
				fleet.setName(fleet.getFaction().getFleetTypeName(FleetTypes.PATROL_LARGE));
			}
		}
		
		// stay non-hostile for a bit to let things settle down, let PL fleets leave, etc
		Global.getSector().addScript(new DelayedActionScript(30f) {
			@Override
			public void doAction() {
				FactionHostilityManager.getInstance().setExcluded(Factions.DIKTAT, false);
			}
		});

		if (winner == StandoffWinner.ACHILLES) {
			FactionAPI lionsGuard = Global.getSector().getFaction(Factions.LIONS_GUARD);
			FactionAPI diktat = Global.getSector().getFaction(Factions.DIKTAT);
			FactionAPI diktatAchilles = Global.getSector().getFaction(Factions.DIKTAT_ACHILLES);
			diktat.setFactionLogoOverride(diktatAchilles.getLogo());
			diktat.setFactionCrestOverride(diktatAchilles.getCrest());
			diktat.setDescIdOverride("diktat_achilles");
			diktat.addKnownWeapon("gigacannon", false);
			diktat.addKnownWeapon("kineticblaster", false);
			diktat.getModifiedWeaponSellFrequency().put("gigacannon", 5f);
			diktat.getModifiedWeaponSellFrequency().put("kineticblaster", 5f);
			for (String hullId : lionsGuard.getKnownShips()) {
				ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
				if (spec.hasTag(Tags.LG_BP)) {
					diktat.addKnownShip(hullId, true);
				}
			}
		}
		
		SDDevastation.Stage stage = SDDevastation.Stage.START;
		EventStageData esd = SDDevastation.get().getLastActiveStage(false);
		
		if (esd != null) {
			stage = (SDDevastation.Stage) esd.id;
		}
		
		int sddStage = 0;
		
		if (stage == SDDevastation.Stage.START) sddStage = 0;
		else if (stage == SDDevastation.Stage.LEVEL_1) sddStage = 1;
		else if (stage == SDDevastation.Stage.LEVEL_2) sddStage = 2;
		else if (stage == SDDevastation.Stage.LEVEL_3) sddStage = 3;
		else if (stage == SDDevastation.Stage.LEVEL_4) sddStage = 4;
		else if (stage == SDDevastation.Stage.FINAL) sddStage = 5;
		
		Global.getSector().getMemoryWithoutUpdate().set("$sddDevastationLevel", sddStage);
		
		String _playerFactionCommission = Global.getSector().getPlayerMemoryWithoutUpdate().getString("$fcm_faction");
			
		boolean _sindrianDiktatCollapse = false;
		if(sddStage == 5) {
			_sindrianDiktatCollapse = true;
			Global.getSector().getMemoryWithoutUpdate().set("$sindrianDiktatCollapsed", true);
			// message about this will be sent from cleanUpFleetsAffectedByPlanetOrFactionDestruction()
		}
		
		
		// We've got lots of questions to answer. 
		int _achillesIsNotClientOfKatoScore = 0; //
		int _hectorDumpsMacarioScore = 0;
		
		int _nortiaToHegemonyScore = 0;
		int _nortiaToPLScore = 0;
		int _nortiaToPiratesScore = 0;
		
		int _umbraToIndieScore = 0;
		
		// Can track certain outcomes by factional influence
		// Basically: how much power to they have to get what they want.
		int _hegInfluence = 0;
		int _leagueInfluence = 0;
		int _ttInfluence = 0; // Note that many Inesis and Arroyo work AGAINST the TT party line 
		int _pirateInfluence = 0; // aka Kanta
		int _churchInfluence = 0;
		int _indieInfluenceScore = 0;
		
		// Is Sindria destroyed by Caden's use of the PK?
		boolean _cadenBlowsUpSindria = false;

		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$gavePKtoDiktat") &&
			((winner == StandoffWinner.ACHILLES) ||
			(winner == StandoffWinner.HECTOR && !Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHector")))) {
			
			// If you gave the SD the PK, it's going to have to get used.
			// If he was convinced to support Hector (and Hector won), he won't do it.
			
			_cadenBlowsUpSindria = true;
			
			_achillesIsNotClientOfKatoScore--; // Achilles will not be in a great place here.
			
			// use this during the wrap-up dialog; have Caden interrupt in the middle of the settlement announcement.
			Global.getSector().getMemoryWithoutUpdate().set("$cadenBlowsUpSindria", true);
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenAntiMacario")) {
			_hectorDumpsMacarioScore++;
		}
		
		
		if(winner.equals(StandoffWinner.HECTOR))
			_ttInfluence++;
		else if(winner.equals(StandoffWinner.ACHILLES)) {
			_nortiaToHegemonyScore++;
			_nortiaToPLScore++;
		}
		
		if(_playerFactionCommission == null) {
			_indieInfluenceScore++;
		}
		
		
		// Hegemony & Orcus Rao
		if(_playerFactionCommission != null && _playerFactionCommission.equals(Factions.HEGEMONY)) {
			_hegInfluence++;
		}
		if (_sindrianDiktatCollapse) 
			_hegInfluence++; // for obvious reasons, one hopes. 
		
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToOrcus")) {
			// gives Rao more political influence
			_hegInfluence++;
		}
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_gaveHerculesOrcusEscape") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_gaveHerculesHegemonyEscape")) {
			// gives Rao more political influence
			_hegInfluence++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_toldRaoAboutFortunaAchilles")) {
			// promptes Rao to counter Fortuna Kato's plotting
			_hegInfluence++;
			_leagueInfluence--;
			_achillesIsNotClientOfKatoScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_offeredToAskRaoForHyderDefection")) {
			// gives Rao more political influence 
			_hegInfluence++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackRao")) {
			// gives Rao more covert ops influence
			_hegInfluence++;
		}
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_affirmedSupportingRaoAgainstAchilles")) {
			_hegInfluence++;
			_leagueInfluence--;
		}
		//if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_liedAboutSupportingRaoAgainstAchilles")) {
		//	_hegInfluence++;
		//}

		
		// TT
		if( _playerFactionCommission != null && _playerFactionCommission.equals(Factions.TRITACHYON)) {
			_ttInfluence++;
		}

		// Arroyo 
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistAchilles")) {
			// helps build independent power base for Achilles
			_achillesIsNotClientOfKatoScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistHector")) {
			// helps build independent power base for Hector
			_ttInfluence++;
			_hectorDumpsMacarioScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) {
			_indieInfluenceScore++;
			_churchInfluence++;
		}
		
		// TT - Inesis
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis"))
			_ttInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario"))
			_hectorDumpsMacarioScore++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac")) {
			_ttInfluence++;
			_hectorDumpsMacarioScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector")) {
			_ttInfluence++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAchilles"))
			_achillesIsNotClientOfKatoScore++;
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_soldLeonidasToInesis")) {
			_ttInfluence++;
		}
		
		// Church.
		if(_playerFactionCommission != null && _playerFactionCommission.equals(Factions.LUDDIC_CHURCH))
			_churchInfluence++;
		
		// VSDC
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveWeaponsVSDC")) 
			_churchInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveCreditsVSDC")) 
			_churchInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesVSDC")) 
			_churchInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedVSDCfinished")) 
			_churchInfluence++;	
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$bffi_missionCompleted")) // a stronger church
			_churchInfluence++;
		
		// ACA
		// Their goal is Askonian independence - but they'll take a Hegemony win if they must
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveFuelACA")) {
			_indieInfluenceScore++;
			_nortiaToHegemonyScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveCreditsACA")) {
			_indieInfluenceScore++;
			_nortiaToHegemonyScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesACA")) {
			_indieInfluenceScore++;
			_nortiaToHegemonyScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedACAfinished")) {
			_indieInfluenceScore++;
			_nortiaToHegemonyScore++;
		}
		
		// ARC
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveFuelARC")) {
			_nortiaToPiratesScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesARC")) {
			_nortiaToPiratesScore++;
		}
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveWeaponsARC")) {
			_nortiaToPiratesScore++;
		}
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$imoinuCalledComrade")) {
			_nortiaToPiratesScore++;
			_umbraToIndieScore--;
		}
		
		// Pirates
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$everHadKantaProtection")) {
			_pirateInfluence++;
		}
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$kantaProtection")) {
			_pirateInfluence++;
		}
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToKanta")) {
			_pirateInfluence++;
		}
		
		// PL influence
		// Basically, PL gets to take a planet (or more!) if the player helped them out
		if( _playerFactionCommission != null && _playerFactionCommission.equals(Factions.PERSEAN)) _leagueInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaSiyavong")) _leagueInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaReynard")) _leagueInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaYaribay")) _leagueInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToSiyavong")) _leagueInfluence++;
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_proReynard")) _leagueInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_workingForReynard")) _leagueInfluence++;
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksReynard")) _leagueInfluence++;
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_affirmedYaribayInfluence")) _leagueInfluence++;
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_backingFortuna")) _leagueInfluence++;
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_askHorusToBackFortunaDone")) _leagueInfluence++;
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksHegemony")) _leagueInfluence--;
		
		// Achilles client of Gens Kato factors
		// Arroyo and Inesis are covered in their own sections.
		_achillesIsNotClientOfKatoScore += 5 - sddStage; // more devastation means he's more likely he's a client.
		
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_acceptedAchillesCeremonyInvite")) _achillesIsNotClientOfKatoScore++;
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToAchilles")) _achillesIsNotClientOfKatoScore++;
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hannanBlockingKato") &&
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
			_achillesIsNotClientOfKatoScore++;
		}

		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderDecidedToBackAchilles") &&
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$hyderDefeatedDuringStandoff")) {
			_achillesIsNotClientOfKatoScore++;
		}
		 
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
			_achillesIsNotClientOfKatoScore--;
		}
		if( Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proKato") && 
			Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_backingFortuna")) {
			_achillesIsNotClientOfKatoScore--;
		}
		
		// If Achilles won, determine if he is going to be client of Kato or not.
		// And what, if anything, the PL takes over. (Of course, if he IS a client, he converts to PL!)
		
		PersonAPI achilles = Global.getSector().getImportantPeople().getData(People.ACHILLES).getPerson();
		if (achilles != null && winner == StandoffWinner.ACHILLES) {
			// if Achilles has sufficient not-client points, he gets his own Diktat
			if(_achillesIsNotClientOfKatoScore > 3) {
				Global.getSector().getMemoryWithoutUpdate().set("$sdse_achillesWonNotClient", true);	
				//log.info("Achilles won and is NOT a client of Gens Kato (score: " + _achillesIsNotClientOfKatoScore + ")" );
			}
			else {
				// Otherwise he becomes a puppet of Fortuna Kato
				achilles.getMemoryWithoutUpdate().set("$clientOfKato",  true);
				Global.getSector().getMemoryWithoutUpdate().set("$sdse_achillesWonPL", true);	
				
				achilles.setImportance(PersonImportance.HIGH); 
				achilles.setFaction(Factions.PERSEAN);
				achilles.setPostId(Ranks.POST_ADMINISTRATOR);
				achilles.setRankId(Ranks.SPACE_ADMIRAL);
				_leagueInfluence++;
				//log.info("Achilles won and IS a client of Gens Kato (score: " + _achillesIsNotClientOfKatoScore + ")");
			}
			
		}
	
		// Now lets do a breakdown by planet.
		
		// Sindria
		if(_cadenBlowsUpSindria) {
			// There goes the neighbourhood!
			// The actual destruction of Sindria will be done in the resolution dialog.
			
			if(winner == StandoffWinner.HECTOR) {
				// Let's assume he's killed.
				// And this is the end of the Sindrian Diktat.
				
				_sindrianDiktatCollapse = true;
				Global.getSector().getMemoryWithoutUpdate().set("$sindrianDiktatCollapsed", true);
			}
		}
		else if (_sindrianDiktatCollapse) {
			// There is no more Sindrian Diktat in this case.
			changeFaction(sindria, Factions.INDEPENDENT, createRandomAdmin(Factions.INDEPENDENT, PersonImportance.HIGH), 5);
			
			sindria.getPlanetEntity().setCustomDescriptionId("planet_sindria_independent");
			sindria.getPlanetEntity().setInteractionImage("illustrations", "urban01");
			
			// downgrade fuel production, etc
			sindria.getIndustry(Industries.FUELPROD).setSpecialItem(null);
			sindria.removeIndustry(Industries.HIGHCOMMAND, MarketInteractionMode.LOCAL, false);
			sindria.addIndustry(Industries.MILITARYBASE);
			RecentUnrest.get(sindria).add(2, "de-Andradification campaign");
		}
		else if (winner == StandoffWinner.ACHILLES && achilles.getFaction().getId().equals(Factions.PERSEAN)) {
			changeFaction(sindria, Factions.PERSEAN, achilles, 2);
			sindria.getPlanetEntity().setCustomDescriptionId("planet_sindria_pl");
			RecentUnrest.get(sindria).add(3, "League-sponsored counter-insurgency");
			sindria.getPlanetEntity().setInteractionImage("illustrations", "urban01");
		} else if (winner == StandoffWinner.ACHILLES) {
			// also replaces a bunch of the personnel in the comm directory, which feels appropriate here
			changeFaction(sindria, Factions.DIKTAT, achilles, 2);
		}
		else if (winner == StandoffWinner.HECTOR) {
			
			PersonAPI hector = Global.getSector().getImportantPeople().getData(People.HECTOR).getPerson();
			
			hector.setMarket(sindria);
			sindria.addPerson(hector);
			sindria.setAdmin(hector);
			sindria.getCommDirectory().removePerson(hector);
			sindria.getCommDirectory().addPerson(hector, 0);
			sindria.getCommDirectory().getEntryForPerson(hector).setHidden(false);
		
			sindria.getPlanetEntity().setCustomDescriptionId("planet_sindria_hector");
		}
		
		// Volturn
		//boolean _hideStandfast = true;
		if((_cadenBlowsUpSindria && winner.equals(StandoffWinner.ACHILLES) && !_sindrianDiktatCollapse )) {
			// if Achilles won, he has to move his capital to Volturn.
			for (PersonAPI person : volturn.getPeopleCopy()) {
				if (Ranks.POST_ADMINISTRATOR.equals(person.getPostId())) {
					person.setPostId(Ranks.POST_CITIZEN);
					person.addTag(Tags.INVOLUNTARY_RETIREMENT);
					person.setImportance(person.getImportance().prev());
					volturn.getCommDirectory().removePerson(person);
					//volturn.getCommDirectory().addPerson(person, 1000);
					// probably just disappear them - I mean mechanically!
					break;
				}
			}

			if (achilles != null) {
				// base faction of Volturn on Achilles' faction (which was set earlier).
				if(achilles.getFaction().getId().equals(Factions.PERSEAN)) {
					changeFaction(volturn, Factions.PERSEAN, achilles, 3);
					volturn.getPlanetEntity().setCustomDescriptionId("planet_volturn_persean");
					RecentUnrest.get(volturn).add(2, "League-sponsored counter-insurgency");
				}
				else {
					changeFaction(volturn, Factions.DIKTAT, achilles, 3);
					volturn.getPlanetEntity().setCustomDescriptionId("planet_volturn_achilles");
					RecentUnrest.get(volturn).add(2, "counter-insurgency campaign");
				}
			}
		}
		else if((_cadenBlowsUpSindria && winner.equals(StandoffWinner.HECTOR)) || sddStage >= 3) {
			// If the SD is destroyed, choose Church or Indie
			
			if(_churchInfluence > _indieInfluenceScore && (_sindrianDiktatCollapse || _churchInfluence >= 4)) {
				changeFaction(volturn, Factions.LUDDIC_CHURCH, createRandomAdmin(Factions.LUDDIC_CHURCH, PersonImportance.MEDIUM), 1);
				volturn.getPlanetEntity().setCustomDescriptionId("planet_volturn_church");
				RecentUnrest.get(volturn).add(1, "Knights of Ludd pacification campaign");
			}
			else {
				changeFaction(volturn, Factions.INDEPENDENT, createRandomAdmin(Factions.INDEPENDENT, PersonImportance.MEDIUM), 1);
				volturn.getPlanetEntity().setCustomDescriptionId("planet_volturn_indie");
				RecentUnrest.get(volturn).add(2, "counter-insurgency operations");
			}
		}
		else if (winner.equals(StandoffWinner.HECTOR) || winner.equals(StandoffWinner.ACHILLES)){
			// fallback option to 'return' Volturn to an heir.
			
			if(winner.equals(StandoffWinner.HECTOR)) {
				changeFaction(volturn, Factions.DIKTAT, createRandomAdmin(Factions.DIKTAT, PersonImportance.MEDIUM), 1);
				RecentUnrest.get(volturn).add(3, "unrestrained counter-insurgency");
			}
			else if(winner.equals(StandoffWinner.ACHILLES)) {
				if (achilles != null) {
					if(achilles.getFaction().getId().equals(Factions.PERSEAN)) {

						changeFaction(volturn, Factions.PERSEAN, createRandomAdmin(Factions.PERSEAN, PersonImportance.MEDIUM), 3);
						// put a  gens character in charge.
						for (PersonAPI admin : volturn.getPeopleCopy()) {
							if (Ranks.POST_ADMINISTRATOR.equals(admin.getPostId())) {
								// no Yaribay payoff position for Volturn - it's too important.
								if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_workingForReynard")) {
									admin.getName().setLast("Hannan");
									admin.addTag(Tags.GENS_HANNAN);
								}
								else {
									admin.getName().setLast("Kato");
									admin.addTag(Tags.GENS_KATO);
									// add Dardan's management? boy that'd be good.
								}
								break;
							}
						}
						
						volturn.getPlanetEntity().setCustomDescriptionId("planet_volturn_persean");
						RecentUnrest.get(volturn).add(2, "League-sponsored counter-insugency");
					}
					else {
						changeFaction(volturn, Factions.DIKTAT, createRandomAdmin(Factions.DIKTAT, PersonImportance.MEDIUM), 3);
						volturn.getPlanetEntity().setCustomDescriptionId("planet_volturn_achilles");
						RecentUnrest.get(volturn).add(2, "counter-insurgency campaign");
					}
				}
			}
		}
		else {
			// and if no one wins, fallback sets Volturn to indie.
			changeFaction(volturn, Factions.INDEPENDENT, createRandomAdmin(Factions.INDEPENDENT, PersonImportance.MEDIUM), 1);
			volturn.getPlanetEntity().setCustomDescriptionId("planet_volturn_indie");
			RecentUnrest.get(volturn).add(2, "counter-insurgency operations");
		}
		
		// Moyra Standfast remains only if Volturn goes independent or Church.
		// update: No, always hide her. Providence will provide. -dgb.
		//if(_hideStandfast) {
		PersonAPI standfast = Global.getSector().getImportantPeople().getPerson(People.STANDFAST);
		if (standfast != null &&
			standfast.getMarket() != null &&
			standfast.getMarket().getCommDirectory() != null &&
			standfast.getMarket().getCommDirectory().getEntryForPerson(standfast.getId()) != null) {
			standfast.getMarket().getCommDirectory().getEntryForPerson(standfast.getId()).setHidden(true);
		}
		//}
		
		// Cruor
		if(_pirateInfluence > 0 && sddStage >= 4) {
			changeFaction(cruor, Factions.PIRATES, createRandomAdmin(Factions.PIRATES, PersonImportance.LOW), 3);
			cruor.getPlanetEntity().setCustomDescriptionId("planet_cruor_pirate");
			RecentUnrest.get(cruor).add(3, "rampant banditry");
		}
		else if(sddStage >= 4) {
			DecivTracker.decivilize(cruor, false, true);
			// it doesn't end well, does it.
			
			cruor.getMemoryWithoutUpdate().set("$sdse_decivilizedAfterCivilWar", true);
			Global.getSector().getMemoryWithoutUpdate().set("$sdr_cruorDecived", true);
			cruor.getPlanetEntity().setCustomDescriptionId("planet_cruor_deciv");
		}
		else if (_sindrianDiktatCollapse) {
			changeFaction(cruor, Factions.PIRATES, createRandomAdmin(Factions.PIRATES, PersonImportance.LOW), 3);
			cruor.getPlanetEntity().setCustomDescriptionId("planet_cruor_pirate");
			RecentUnrest.get(cruor).add(3, "rampant banditry");
		}
		else if(volturn.getFactionId().equals(Factions.PERSEAN)) {
			changeFaction(cruor, Factions.PERSEAN, createRandomAdmin(Factions.PERSEAN, PersonImportance.LOW), 3);
			cruor.getPlanetEntity().setCustomDescriptionId("planet_cruor_league");
			RecentUnrest.get(cruor).add(2, "banditry");
		}
		else if(volturn.getFactionId().equals(Factions.INDEPENDENT)) {
			changeFaction(cruor, Factions.PIRATES, createRandomAdmin(Factions.PIRATES, PersonImportance.LOW), 3);
			cruor.getPlanetEntity().setCustomDescriptionId("planet_cruor_pirate");
			RecentUnrest.get(cruor).add(3, "rampant banditry");
		}

		
		// Nortia
		_nortiaToHegemonyScore += _hegInfluence;
		_nortiaToPLScore += _leagueInfluence;
		if( !_cadenBlowsUpSindria && (sddStage == 0 || (sddStage == 1 && _indieInfluenceScore == 0))) {
			// a strong SD will conquer Nortia outright regardless of winner
			// weak Charterists will be defeated by the SD 
			
			if(volturn.getFactionId().equals(Factions.PERSEAN)) {
				changeFaction(nortia, Factions.PERSEAN, createRandomAdmin(Factions.PERSEAN, PersonImportance.MEDIUM), 3);
				nortia.getPlanetEntity().setCustomDescriptionId("planet_nortia_pl");
				RecentUnrest.get(nortia).add(2, "League-backed occupation");
				
				// put a PL gens in charge.
				for (PersonAPI admin : nortia.getPeopleCopy()) {
					if (Ranks.POST_ADMINISTRATOR.equals(admin.getPostId())) {
						
						if (	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksReynard") ||
								Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
							// Yaribay gets a post as payoff
							admin.getName().setLast("Yaribay");
							admin.addTag(Tags.GENS_YARIBAY);
						}
						else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_workingForReynard")) {
							admin.getName().setLast("Hannan");
							admin.addTag(Tags.GENS_HANNAN);
						}
						else {
							admin.getName().setLast("Kato");
							admin.addTag(Tags.GENS_KATO);
						}
						break;
					}
				}
			}
			else {
				changeFaction(nortia, Factions.DIKTAT, createRandomAdmin(Factions.DIKTAT, PersonImportance.MEDIUM), 3);
				nortia.getPlanetEntity().setCustomDescriptionId("planet_nortia_sd");
				RecentUnrest.get(nortia).add(5, "Sindrian Diktat counter-insurgency");
			}
		
			// Arnaud Iscare definitely needs to flee in this case.
			PersonAPI iscare = Global.getSector().getImportantPeople().getPerson(People.GA_RECRUITER);
			if (iscare != null &&
				iscare.getMarket() != null &&
				iscare.getMarket().getCommDirectory() != null &&
				iscare.getMarket().getCommDirectory().getEntryForPerson(iscare.getId()) != null) {
				iscare.getMarket().getCommDirectory().getEntryForPerson(iscare.getId()).setHidden(true);
			}
		}
		else if(sddStage >= 4 || _cadenBlowsUpSindria) {
			// a high sdd OR no Sindria will force SD to surrender claims
			// TT commission as tiebreaker.
			
			// If high devastation and heavy TT involvement, Hector is forced to repay his debts with Nortia
			// IF he can.
			
			// If high devastation and heavy Hegemony involvement, either may be forced to cede Nortia to the Heg
			// This requires very high devastation!
			
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sindrianDiktatCollapsed")) {
				_nortiaToHegemonyScore++;
			}

			// Generally, Nortia will stay independent.
			
			// some unlikely scenarios
			if(_ttInfluence > 3 && _nortiaToHegemonyScore < 7) 
			{
				changeFaction(nortia, Factions.TRITACHYON, createRandomAdmin(Factions.PIRATES, PersonImportance.MEDIUM), 3);
				nortia.getPlanetEntity().setCustomDescriptionId("planet_nortia_tt");
				RecentUnrest.get(nortia).add(4, "Tri-Tachyon transitional administration");
			}
			else if (_pirateInfluence > 0 && _nortiaToHegemonyScore <= 6) 
			{
				changeFaction(nortia, Factions.PIRATES, createRandomAdmin(Factions.PIRATES, PersonImportance.MEDIUM), 3);
				nortia.getPlanetEntity().setCustomDescriptionId("planet_nortia_pirates");
				nortia.getPlanetEntity().setInteractionImage("illustrations", "pirate_station");
				RecentUnrest.get(nortia).add(5, "pirate takeover");
			}
			else if(_nortiaToHegemonyScore > 6) 
			{
				changeFaction(nortia, Factions.HEGEMONY, createRandomAdmin(Factions.HEGEMONY, PersonImportance.MEDIUM), 3);
				nortia.getPlanetEntity().setCustomDescriptionId("planet_nortia_heg");
				RecentUnrest.get(nortia).add(3, "Hegemony occupation");
			}
			else if(_nortiaToPLScore > 4) 
			{
				changeFaction(nortia, Factions.PERSEAN, createRandomAdmin(Factions.PERSEAN, PersonImportance.MEDIUM), 3);
				nortia.getPlanetEntity().setCustomDescriptionId("planet_nortia_pl");
				RecentUnrest.get(nortia).add(3, "Persean League occupation");
				
				// put a PL gens in charge.
				for (PersonAPI admin : nortia.getPeopleCopy()) {
					if (Ranks.POST_ADMINISTRATOR.equals(admin.getPostId())) {
						
						if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksReynard") ||
							Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
							// Yaribay gets a post as payoff
							admin.getName().setLast("Yaribay");
							admin.addTag(Tags.GENS_YARIBAY);
						}
						else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_workingForReynard")) {
							admin.getName().setLast("Hannan");
							admin.addTag(Tags.GENS_HANNAN);
						}
						else {
							admin.getName().setLast("Kato");
							admin.addTag(Tags.GENS_KATO);
						}
						break;
					}
				}
			}
		}

		// Umbra
		boolean _hideImoinu = true;
		_umbraToIndieScore += _indieInfluenceScore;
		
		if(sddStage <= 1 && !_sindrianDiktatCollapse && !_cadenBlowsUpSindria) {
			// 0 or 1 SDD means SD will conquer Umbra (regardless of winner)
			
			
			if(winner == StandoffWinner.ACHILLES && volturn.getFactionId().equals(Factions.PERSEAN)) {
				changeFaction(umbra, Factions.PERSEAN, createRandomAdmin(Factions.PERSEAN, PersonImportance.LOW), 3);
				umbra.getPlanetEntity().setCustomDescriptionId("planet_umbra_pl");
				umbra.getPlanetEntity().setInteractionImage("illustrations", "mine");
				RecentUnrest.get(umbra).add(4, "League-backed occupation");
				
				// put a PL gens in charge.
				for (PersonAPI admin : umbra.getPeopleCopy()) {
					if (Ranks.POST_ADMINISTRATOR.equals(admin.getPostId())) {
						
						if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksReynard") ||
							Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
							// Yaribay gets a post as payoff
							admin.getName().setLast("Yaribay");
							admin.addTag(Tags.GENS_YARIBAY);
						}
						else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_workingForReynard")) {
							admin.getName().setLast("Hannan");
							admin.addTag(Tags.GENS_HANNAN);
						}
						else {
							admin.getName().setLast("Kato");
							admin.addTag(Tags.GENS_KATO);
						}
						break;
					}
				}
			}
			else {
				changeFaction(umbra, Factions.DIKTAT, createRandomAdmin(Factions.DIKTAT, PersonImportance.LOW), 3);
				umbra.getPlanetEntity().setCustomDescriptionId("planet_umbra_sd");
				umbra.getPlanetEntity().setInteractionImage("illustrations", "mine");
				RecentUnrest.get(umbra).add(5, "Sindrian Diktat counter-insurgency");
			}
		}
		else if ((sddStage == 2 || sddStage == 3) && 
				!_sindrianDiktatCollapse && 
				!_cadenBlowsUpSindria && 
				winner.equals(StandoffWinner.HECTOR) && 
				_ttInfluence > 0) {
			
			// Hector pays back TT because he *needs* to; isn't strong enough to say no.
			changeFaction(umbra, Factions.TRITACHYON, createRandomAdmin(Factions.TRITACHYON, PersonImportance.LOW), 3);
			umbra.getPlanetEntity().setCustomDescriptionId("planet_umbra_tt");
			umbra.getPlanetEntity().setInteractionImage("illustrations", "mine");
			umbra.setFreePort(true);
			RecentUnrest.get(umbra).add(3, "Tri-Tachyon transitional administration");
			
		}
		else if (sddStage >= 4 || _cadenBlowsUpSindria) {
			
			// Due to SD weakness, a strong faction might be able to take Umbra...
			if( (_hegInfluence > 6 && sddStage == 4) || (_hegInfluence > 5 && sddStage == 5)) 
			{
				// The Hegemony has some assertive notions, if its influencial enough.
				
				changeFaction(umbra, Factions.HEGEMONY, createRandomAdmin(Factions.HEGEMONY, PersonImportance.LOW), 3);
				umbra.getPlanetEntity().setCustomDescriptionId("planet_umbra_heg");
				umbra.getPlanetEntity().setInteractionImage("illustrations", "mine");
				RecentUnrest.get(umbra).add(3, "Hegemony occupation");
			}
			else if(winner.equals(StandoffWinner.ACHILLES) && !_sindrianDiktatCollapse && _leagueInfluence > 3) 
			{
				// if Achilles wins, he's forced to cede Umbra to the PL... if they have decent influence.
					
				changeFaction(umbra, Factions.PERSEAN, createRandomAdmin(Factions.PERSEAN, PersonImportance.LOW), 3);
				
				// put a PL gens in charge.
				for (PersonAPI admin : umbra.getPeopleCopy()) {
					if (Ranks.POST_ADMINISTRATOR.equals(admin.getPostId())) {
						if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_workingForReynard")) {
							admin.getName().setLast("Hannan");
							admin.addTag(Tags.GENS_HANNAN);
						}
						else if (	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksReynard") ||
									Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_horusBacksFortuna")) {
							// Yaribay gets a post as payoff
							admin.getName().setLast("Yaribay");
							admin.addTag(Tags.GENS_YARIBAY);
						}
						else {
							admin.getName().setLast("Kato");
							admin.addTag(Tags.GENS_KATO);
						}
						
						break;
					}
				}

				umbra.getPlanetEntity().setCustomDescriptionId("planet_umbra_pl");
				umbra.getPlanetEntity().setInteractionImage("illustrations", "mine");
				RecentUnrest.get(umbra).add(3, "Persean League occupation");
			
			}
			else if (_umbraToIndieScore >= 4)
			{
				// Otherwise, a strong ACA can make Umbra indie
				changeFaction(umbra, Factions.INDEPENDENT, createRandomAdmin(Factions.INDEPENDENT, PersonImportance.LOW), 3);
				umbra.getPlanetEntity().setCustomDescriptionId("planet_umbra_indie");
				umbra.getPlanetEntity().setInteractionImage("illustrations", "mine");
				//_hideImoinu = false; // let's follow this narrative thread in another update -dgb.
			}
		}
		
		if(!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$metImoinuKato")) {
			// if player never even met Imoinu, then she gets hidden.
			_hideImoinu = true;
		}

		// hide Imoinu in almost all cases
		if(_hideImoinu) {
			PersonAPI imoinu = Global.getSector().getImportantPeople().getPerson(People.IMOINU_KATO);
			if (imoinu != null &&
				imoinu.getMarket() != null &&
				imoinu.getMarket().getCommDirectory() != null &&
				imoinu.getMarket().getCommDirectory().getEntryForPerson(imoinu.getId()) != null) {
				imoinu.getMarket().getCommDirectory().getEntryForPerson(imoinu.getId()).setHidden(true);
			}
		}
		

		
		// assign orbital infrastructure appropriately.
		// if the originals were destroyed, then too bad.
		// whoever owns Sindria gets the comms relay - fallback to Volturn
		SectorEntityToken relay = Global.getSector().getEntityById("sindria_relay");
		
		if (relay == null) {
			for (SectorEntityToken objective : askonia.getEntitiesWithTag(Tags.OBJECTIVE)) {
				if (objective.hasTag(Tags.NON_CLICKABLE)) continue;
				if (objective.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
				float _orbitPeriod = objective.getOrbit().getOrbitalPeriod();
				if(Math.abs(_orbitPeriod - 100) < 0.01f) { // very stupid, but if it works...
					relay = objective;
					break;
				}
			}
		}
		
		if (relay != null) {
			if(_cadenBlowsUpSindria) {
				relay.setFaction(volturn.getFactionId());
			}
			else {
				relay.setFaction(sindria.getFactionId());
			}
		}
		
		// whoever owns Volturn gets the nav buoy
		SectorEntityToken nav_buoy = Global.getSector().getEntityById("askonia_nav_buoy");
		
		if (nav_buoy == null) {
			for (SectorEntityToken objective : askonia.getEntitiesWithTag(Tags.OBJECTIVE)) {
				if (objective.hasTag(Tags.NON_CLICKABLE)) continue;
				if (objective.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
				float _orbitPeriod = objective.getOrbit().getOrbitalPeriod();
				if(Math.abs(_orbitPeriod - 45) < 0.01f) { // very stupid, but if it works...
					nav_buoy = objective;
					break;
				}
			}
		}
				
		if (nav_buoy != null) {
			nav_buoy.setFaction(volturn.getFactionId());
		}
		
		// if low SDD sensors go to Sindria/Volturn owner
		// otherwise get taken by Nortia owner.
		SectorEntityToken sensor_array = Global.getSector().getEntityById("askonia_sensor_array");
		
		if (sensor_array == null) {
			for (SectorEntityToken objective : askonia.getEntitiesWithTag(Tags.OBJECTIVE)) {
				if (objective.hasTag(Tags.NON_CLICKABLE)) continue;
				if (objective.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
				float _orbitPeriod = objective.getOrbit().getOrbitalPeriod();
				if( Math.abs(_orbitPeriod - 600) < 0.01f) {
					sensor_array = objective;
					break;
				}
			}
		}
		
		if (sensor_array != null) {
			if(sddStage > 2) {
				sensor_array.setFaction(nortia.getFactionId());
			}
			else {
				if(_cadenBlowsUpSindria) {
					sensor_array.setFaction(volturn.getFactionId());
				}
				else {
					sensor_array.setFaction(sindria.getFactionId());
				}
			}
		}
		
		// Macario always gets hidden
		// And might as well give him his Luddic disguise while we're at it.
		PersonAPI macario = Global.getSector().getImportantPeople().getPerson(People.MACARIO);
		if (macario != null &&
			macario.getMarket() != null &&
			macario.getMarket().getCommDirectory() != null &&
			macario.getMarket().getCommDirectory().getEntryForPerson(macario.getId()) != null) 
		{
			macario.getMarket().getCommDirectory().getEntryForPerson(macario.getId()).setHidden(true);
			macario.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dolos_macario2"));
		}
		
		// Does Hector dump Macario?
		if(_hectorDumpsMacarioScore > 0) {
			Global.getSector().getMemoryWithoutUpdate().set("$sdr_hectorDumpsMacario", true);
		}

		
		// Which faction has the most influence in the resolution?
		if(_hegInfluence > 5) {
			Global.getSector().getMemoryWithoutUpdate().set("$sdResolutionChairFaction", Factions.HEGEMONY);
		}
		else if(_ttInfluence > 3) {
			Global.getSector().getMemoryWithoutUpdate().set("$sdResolutionChairFaction", Factions.TRITACHYON);
		}
		else if(_churchInfluence > 4) {
			Global.getSector().getMemoryWithoutUpdate().set("$sdResolutionChairFaction", Factions.LUDDIC_CHURCH);
		}
		else if(_leagueInfluence > 5) {
			Global.getSector().getMemoryWithoutUpdate().set("$sdResolutionChairFaction", Factions.PERSEAN);
		}
		else if(_playerFactionCommission != null)
			Global.getSector().getMemoryWithoutUpdate().set("$sdResolutionChairFaction", _playerFactionCommission); 
		else
			Global.getSector().getMemoryWithoutUpdate().set("$sdResolutionChairFaction", "independent");
		
		
		checkDiktatCompletelyDestroyed();
		
		SectorEntityToken trigger = sindria.getPlanetEntity();
		if (!_cadenBlowsUpSindria) {
			trigger = null;
		}
		Global.getSector().addScript(new SDRShowEndingScript(winner, stage, trigger, 1000f));
		SDDevastation.get().endImmediately();
		
		log.info("SDResolution finished endStandoffInAskonia");
		
		if(Global.getSettings().isDevMode()) { 
			String _infoDump = "*** SDResolution calculation dump ***";
			if(winner == StandoffWinner.ACHILLES)
				_infoDump += "\n* winner == ACHILLES"; 
			else 
				_infoDump += "\n* winner == HECTOR";
			
			_infoDump += "\n* sddDevastationLevel: " + sddStage;
			_infoDump += "\n* _sindrianDiktatCollapse: " + _sindrianDiktatCollapse;
			_infoDump += "\n* _cadenBlowsUpSindria: " + _cadenBlowsUpSindria;
			_infoDump += "\n* _achillesIsNotClientOfKatoScore: " + _achillesIsNotClientOfKatoScore;
			_infoDump += "\n* sdse_achillesWonNotClient: " + Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_achillesWonNotClient");
			_infoDump += "\n* sdse_achillesWonPL: " + Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_achillesWonPL");	
			_infoDump += "\n* _hectorDumpsMacarioScore: " +_hectorDumpsMacarioScore;
			_infoDump += "\n* _nortiaToHegemonyScore: " + _nortiaToHegemonyScore;
			_infoDump += "\n* _nortiaToPLScore: " + _nortiaToPLScore;
			_infoDump += "\n* _nortiaToPiratesScore: " + _nortiaToPiratesScore;
			_infoDump += "\n* _umbraToIndieScore: " + _umbraToIndieScore;
			_infoDump += "\n* _hegInfluence: " + _hegInfluence;
			_infoDump += "\n* _leagueInfluence: " + _leagueInfluence;
			_infoDump += "\n* _ttInfluence: " + _ttInfluence;
			_infoDump += "\n* _pirateInfluence: " + _pirateInfluence;
			_infoDump += "\n* _churchInfluence: " + _churchInfluence;
			_infoDump += "\n* _indieInfluenceScore: " + _indieInfluenceScore;
			
			_infoDump += "\n******";
			log.info(_infoDump);
		}
	}
	
	
	protected static void checkDiktatCompletelyDestroyed() {
		if (Misc.getFactionMarkets(Factions.DIKTAT).isEmpty()) {
			cleanUpFleetsAffectedByPlanetOrFactionDestruction();
		}
	}

	// specific to SD, not generic
	public static void cleanUpFleetsAffectedByPlanetOrFactionDestruction() {
		
		boolean destroyed = Misc.getFactionMarkets(Factions.DIKTAT).isEmpty(); 
		if (destroyed) {
			FactionAPI faction = Global.getSector().getFaction(Factions.DIKTAT);
			faction.setShowInIntelTab(false);
			
			FactionCommissionIntel intel = Misc.getCommissionIntel();
			if (intel != null && intel.getFaction() == faction) {
				intel.sendUpdateIfPlayerHasIntel(FactionCommissionIntel.UPDATE_PARAM_ANNULLED, false, true);
				Commission.resign(false, null);
			}
			
			// Tell the player!
			String _intelLine = "The Sindrian Diktat is no more.";
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sindrianDiktatCollapsed")) {
				_intelLine = "The Sindrian Diktat has collapsed.";
			}
			else if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_achillesWonPL") ) {
				_intelLine = "The Sindrian Diktat is no longer an independent polity.";
			}
			
			MessageIntel intel2 = new MessageIntel();
			FactionAPI diktat = Global.getSector().getFaction(Factions.DIKTAT);
			intel2.addLine(_intelLine, null, 
					new String[] {"Sindrian Diktat"}, diktat.getColor()); 

			intel2.setIcon(Global.getSettings().getSpriteName("events", "sd_devastation"));
			Global.getSector().getCampaignUI().addMessage(intel2);
		}
		
		// this'll probably drop a frame or two
		for (LocationAPI curr : Global.getSector().getAllLocations()) {
			for (CampaignFleetAPI fleet : curr.getFleets()) {
				
//				if ("dc05ae".equals(fleet.getId())) {
//					System.out.println("wefkwefwe");
//				}
					
				MemoryAPI mem = fleet.getMemoryWithoutUpdate();
				if (mem.getBoolean(REFORMER_FLEET_RETURNING_TO_PL)) continue;
				
				boolean smuggler = mem.getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
				boolean trader = mem.getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET);
				
				if (fleet.isPlayerFleet()) continue;
				if (fleet.getFaction().isPlayerFaction() && !smuggler && !trader) continue;
				
				String sourceId = mem.getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
				MarketAPI source = Misc.getSourceMarket(fleet);
				
				if (sourceId != null && source == null) {
					fleet.removeScriptsOfClass(PatrolAssignmentAIV4.class);
					Misc.setFlagWithReason(mem, MemFlags.FLEET_IGNORES_OTHER_FLEETS, "factionLost", true, -1f);
					// will return to some point in hyper and despawn
					Misc.giveStandardReturnToSourceAssignments(fleet);
					continue;
				}
				
				RouteData route = RouteManager.getInstance().getRoute(EconomyFleetRouteManager.SOURCE_ID, fleet);
				if (route != null) {
					EconomyRouteData data = (EconomyRouteData) route.getCustom();
					boolean fromExists = data.from != null && 
										 Global.getSector().getEconomy().getMarket(data.from.getId()) != null;
					boolean toExists = data.to != null && 
										 Global.getSector().getEconomy().getMarket(data.to.getId()) != null;
					if (!fromExists || !toExists) {
						fleet.removeScriptsOfClass(EconomyFleetAssignmentAI.class);
						fleet.removeScriptsOfClass(PatrolAssignmentAIV4.class);
						Misc.setFlagWithReason(mem, MemFlags.FLEET_IGNORES_OTHER_FLEETS, "factionLost", true, -1f);
						Misc.giveStandardReturnToSourceAssignments(fleet);
						RouteManager.getInstance().removeRoute(route);
						continue;
					}
				}
				
				if (fleet.getFaction().getId().equals(Factions.DIKTAT)) {
					fleet.removeScriptsOfClass(PatrolAssignmentAIV4.class);
					fleet.removeScriptsOfClass(EconomyFleetAssignmentAI.class);
					Misc.setFlagWithReason(mem, MemFlags.FLEET_IGNORES_OTHER_FLEETS, "factionLost", true, -1f);
					Misc.giveStandardReturnToSourceAssignments(fleet);
					continue;
				}
				
			}	
		}
		
		for (RouteData curr : new ArrayList<>(RouteManager.getInstance().getRoutes())) {
			if (curr.getActiveFleet() != null) continue; // handled above
			if (curr.getSource() != null && curr.getSource().equals(EconomyFleetRouteManager.SOURCE_ID) &&
					curr.getCustom() instanceof EconomyRouteData) {
				EconomyRouteData data = (EconomyRouteData) curr.getCustom();
				boolean fromExists = data.from != null && 
						 Global.getSector().getEconomy().getMarket(data.from.getId()) != null;
				boolean toExists = data.to != null && 
						 Global.getSector().getEconomy().getMarket(data.to.getId()) != null;
				if (!fromExists || !toExists) { 
					RouteManager.getInstance().removeRoute(curr);
				}
			}
		}
	}
	
	
	public static void shooPirates() {
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		StarSystemAPI askonia = volturn.getStarSystem();

		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
		for (MarketAPI curr : Misc.getFactionMarkets(Factions.PIRATES)) {
			picker.add(curr, curr.getSize());
		}
		
		for (CampaignFleetAPI fleet : askonia.getFleets()) {
			if (!fleet.getFaction().getId().equals(Factions.PIRATES)) continue;
			
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			String sourceId = mem.getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
			MarketAPI source = Misc.getSourceMarket(fleet);
			if (sourceId == null || source == null || !source.getFactionId().equals(Factions.PIRATES)) {
				MarketAPI pick = picker.pick();
				if (pick != null) {
					mem.set(MemFlags.MEMORY_KEY_SOURCE_MARKET, pick.getId());
					sourceId = mem.getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
					source = Misc.getSourceMarket(fleet);
				}
			}
			if (sourceId != null && source != null) {
				Misc.giveStandardReturnToSourceAssignments(fleet);
				continue;
			}
			
		}	
	}	
	
	public static void replacePersonOfPost(MarketAPI market, String postId) {
		PersonAPI found = null;
		for (CommDirectoryEntryAPI curr : market.getCommDirectory().getEntriesCopy()) {
			if (curr.getEntryData() instanceof PersonAPI) {
				PersonAPI person = (PersonAPI) curr.getEntryData();
				if (postId.equals(person.getPostId())) {
					found = person;
					break;
				}
			}
		}
		
		if (found == null) return;

		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		ip.removePerson(found);
		market.getCommDirectory().removePerson(found);

		
		PersonAPI replacement = market.getFaction().createRandomPerson();
		replacement.setImportance(found.getImportance());
		replacement.setPostId(found.getPostId());
		replacement.setRankId(found.getRankId());
		
		market.getCommDirectory().addPerson(replacement);
		market.addPerson(replacement);
		ip.addPerson(replacement);
		ip.getData(replacement).getLocation().setMarket(market);
		ip.checkOutPerson(replacement, "permanent_staff");
		if (market.getAdmin() == found) {
			market.setAdmin(replacement);
		}
	}
	
	
	public static void changeFaction(MarketAPI market, String factionId, PersonAPI admin, int minUnrest) {
		if (market == null) return;
		for (SubmarketAPI sub : market.getSubmarketsCopy()) {
			if (sub.getPlugin() != null && sub.getPlugin().isBlackMarket()) continue;
			if (sub.getFaction() != null && sub.getFaction().getId().equals(market.getFactionId())) {
				sub.setFaction(Global.getSector().getFaction(factionId));
			}
		}
		market.setFactionId(factionId);
		market.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET);
//		market.setPlayerOwned(false);
//		market.setFreePort(false);
//		market.setUseStockpilesForShortages(false);
		
		
		if (admin != null) {
			ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
			if (market.getAdmin() != null) {
				ip.removePerson(market.getAdmin());
				market.getCommDirectory().removePerson(market.getAdmin());
			}
			market.getCommDirectory().addPerson(admin);
			market.addPerson(admin);
			
			ip.addPerson(admin);
			ip.getData(admin).getLocation().setMarket(market);
			ip.checkOutPerson(admin, "permanent_staff");

			market.setAdmin(admin);
		}
		
		CampaignFleetAPI station = Misc.getStationFleet(market);
		if (station == null) station = Misc.getStationBaseFleet(market);
		if (station != null) {
			station.setFaction(factionId, true);
		}
		
		for (SectorEntityToken curr : market.getConnectedEntities()) {
			curr.setFaction(factionId);
		}
		
		replacePersonOfPost(market, Ranks.POST_STATION_COMMANDER);
		replacePersonOfPost(market, Ranks.POST_BASE_COMMANDER);
		replacePersonOfPost(market, Ranks.POST_OUTPOST_COMMANDER);
		replacePersonOfPost(market, Ranks.POST_PORTMASTER);
		replacePersonOfPost(market, Ranks.POST_SUPPLY_OFFICER);
		
//		if (!market.hasSubmarket(Submarkets.SUBMARKET_OPEN)) {
//			market.addSubmarket(Submarkets.SUBMARKET_OPEN);
//		}
//		if (!market.hasSubmarket(Submarkets.SUBMARKET_BLACK)) {
//			market.addSubmarket(Submarkets.SUBMARKET_BLACK);
//		}
//		if (Misc.isMilitary(market) || 
//				market.hasIndustry(Industries.MILITARYBASE) || 
//				market.hasIndustry(Industries.HIGHCOMMAND)) {
//			if (!market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
//				market.addSubmarket(Submarkets.GENERIC_MILITARY);
//			}
//		}
		
		
		int unrest = Math.max(RecentUnrest.get(market).getPenalty(), minUnrest);
		RecentUnrest.get(market).setPenalty(unrest);
		
		if (market.getAdmin() == null || admin == null) {
			ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
			if (market.getAdmin() != null) {
				ip.removePerson(market.getAdmin());
				market.getCommDirectory().removePerson(market.getAdmin());
			}
			
			PersonAPI person = market.getFaction().createRandomPerson(StarSystemGenerator.random);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_ADMINISTRATOR);
			
			if (market.getSize() >= 8) {
				person.setImportanceAndVoice(PersonImportance.VERY_HIGH, StarSystemGenerator.random);
			} else if (market.getSize() >= 6) {
				person.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);
			} else {
				person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random);
			}
			
			person.addTag(Tags.CONTACT_TRADE);
			
			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			ip.getData(person).getLocation().setMarket(market);
			ip.checkOutPerson(person, "permanent_staff");
			admin = person;
			market.setAdmin(person);
		}
		
		if (market.getId().equals("umbra") && !Factions.PIRATES.equals(factionId)) {
			shooPirates();
		}
	}
	
	public static PersonAPI createRandomAdmin(String factionId, PersonImportance importance) {
		FactionAPI faction = Global.getSector().getFaction(factionId);
		
		PersonAPI person = faction.createRandomPerson(StarSystemGenerator.random);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_ADMINISTRATOR);
		
		person.setImportanceAndVoice(importance, StarSystemGenerator.random);
		person.addTag(Tags.CONTACT_TRADE);
		
		return person;
	}
	
	public static void removeFleetCommander(CampaignFleetAPI fleet) {
		if (fleet == null) return;
		PersonAPI replacement = OfficerManagerEvent.createOfficer(fleet.getFaction(), 5);
		replacement.setRankId(Ranks.SPACE_CAPTAIN);
		replacement.setPostId(Ranks.POST_FLEET_COMMANDER);
		fleet.setCommander(replacement);
		if (fleet.getFlagship() != null) {
			fleet.getFlagship().setCaptain(replacement);
		}
		fleet.getFleetData().ensureHasFlagship();
	}
	
	
	public static <T extends PersonalFleetScript> CampaignFleetAPI strengthenAndGetPersonalFleet(Class<T> scriptClass, boolean strengthen) {
		T script = PersonalFleetScript.getScript(scriptClass);
		CampaignFleetAPI fleet = script.getFleet();
		if (strengthen) {
			if (fleet != null && fleet.getFleetPoints() < script.getOrigFP() * 0.65f) {
				fleet.despawn(FleetDespawnReason.OTHER, null);
				fleet = null;
			}
			if (fleet == null) {
				script.spawnFleetAndDoRelatedHousekeeping();
				fleet = script.getFleet();
			}
		}
		return fleet;
	}
	
	public static CampaignFleetAPI getMorozFleet() {
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		StarSystemAPI askonia = sindria.getStarSystem();
		for (CampaignFleetAPI curr : askonia.getFleets()) {
			if (curr.getCommander() != null && curr.getCommander().getId().equals(People.MAEUS_MOROZ)) {
				return curr;
			}
		}
		return null;
	}

	public static boolean spawnMorozFleet(boolean rogue) {
		String key = "$spawnedMorozForSDR";
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(key)) {
			return false;
		}
		Global.getSector().getMemoryWithoutUpdate().set(key, true);
		
		CampaignFleetAPI fleet = SDSevenSons.spawnMorozFleet(null, true);
		fleet.getCommander().setPersonality(Personalities.RECKLESS);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
		Misc.makeNonHostileToFaction(fleet, Factions.DIKTAT_ACHILLES, -1);
		
		MarketAPI orthrus = Global.getSector().getEconomy().getMarket("orthrus");
		CommDirectoryEntryAPI entry = orthrus.getCommDirectory().getEntryForPerson(fleet.getCommander());
		if (entry != null) entry.setHidden(true);
		
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		StarSystemAPI askonia = volturn.getStarSystem();
		
		fleet.clearAssignments();
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, orthrus.getPrimaryEntity(), 3f, "preparing for revenge", null);
		if (rogue) {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, sindria.getPrimaryEntity(), 10000f, new Script() {
				@Override
				public void run() {
					fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
					Misc.makeHostileToFaction(fleet, Factions.DIKTAT, -1);
				}
			});			
		} else {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, volturn.getPrimaryEntity(), 10000f, new Script() {
				@Override
				public void run() {
					if (SDResolution.get() != null && SDResolution.get().didLastDitchAttack) {
						fleet.clearAssignments();
						fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, sindria.getPrimaryEntity(), 10000f, 
								"attacking " + sindria.getName());
					} else {
						fleet.addScript(new LeashAssignmentAI(fleet, askonia, volturn.getPrimaryEntity(), null));
					}
					fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
					Misc.makeHostileToFaction(fleet, Factions.DIKTAT, -1);
				}
			});
		}
		
		
		return true;
	}
	
	
	
	public static void sendCompetingTTExecutive() {

		if (Global.getSettings().isDevMode()) {
			Global.getSector().getMemoryWithoutUpdate().unset(DelayedFleetEncounter.TIMEOUT_KEY);
		}
		
		boolean dweller = Global.getSector().getPlayerMemoryWithoutUpdate().getFloat("$itemValueSoldToZGRMonster") >= 500000f;
		boolean dwellerALot = Global.getSector().getPlayerMemoryWithoutUpdate().getFloat("$itemValueSoldToZGRMonster") >= 5000000f;
		boolean threat = Global.getSector().getPlayerMemoryWithoutUpdate().getFloat("$itemValueSoldToZGRThreat") >= 500000f;

		
		Random random = new Random();
		
		PersonAPI commander = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(random);
		commander.setPersonality(Personalities.AGGRESSIVE);
		commander.setRankId(Ranks.SENIOR_EXECUTIVE);
		commander.getStats().setLevel(15);
		commander.getStats().setSkipRefresh(true);
		commander.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		commander.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		commander.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		commander.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		commander.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		//commander.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		commander.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
		//commander.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
		
		commander.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
		
		commander.getStats().setSkillLevel(Skills.NAVIGATION, 1);
		commander.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		commander.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
		commander.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
		commander.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
		commander.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
		commander.getStats().setSkillLevel(Skills.AUTOMATED_SHIPS, 1);
		commander.getStats().setSkillLevel(Skills.CYBERNETIC_AUGMENTATION, 1);
		commander.getStats().setSkipRefresh(false);
		
		
		Random r = Misc.getRandom(random.nextLong(), 3);
		DelayedFleetEncounter e = new DelayedFleetEncounter(r, "TTCompetingExecutive");
		if (Global.getSettings().isDevMode()) {
			e.setDelayNone();
		} else {
			e.setDelay(30f);
		}
		e.setDoNotAbortWhenPlayerFleetTooStrong();
		e.setLocationCoreOnly(true, Factions.TRITACHYON);
		e.setRequireFactionPresence(Factions.TRITACHYON);
		e.beginCreate();
		e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_2, Factions.TRITACHYON, FleetTypes.PATROL_LARGE, new Vector2f());
		e.triggerGetFleetParams().maxNumShips = 20;
		
		e.triggerSetFleetDoctrineComp(3, 0, 2);
		e.triggerSetFleetCommander(commander);
		e.triggerSetFleetOfficers(OfficerNum.FC_ONLY, OfficerQuality.DEFAULT);
		
		e.triggerFleetMakeFaster(false, 2, true);
		
		
		//paragon_Executive
		//radiant_Advanced
		//radiant_Restricted dweller
		//aurora_Advanced
		//aurora_Classified: threat
		//aurora_Restricted: dweller
		//medusa_Advanced
		
		e.triggerSetFleetFaction(Factions.TRITACHYON);
		e.triggerMakeNoRepImpact();
		e.triggerSetStandardAggroInterceptFlags();
		e.triggerMakeFleetIgnoreOtherFleets();
		e.triggerSetFleetGenericHailPermanent("TTCompetingExecutiveHail"); // this is the hail trigger
		
		e.triggerCustomAction(new TriggerAction() {
			@Override
			public void doAction(TriggerActionContext context) {
				context.fleet.setNoFactionInName(true);
				context.fleet.setName("Executive Armada");
				
				for (FleetMemberAPI curr : context.fleet.getFleetData().getMembersListCopy()) {
					if (!curr.isCivilian() && !curr.isFrigate()) {
						context.fleet.getFleetData().removeFleetMember(curr);
					}
				}
				context.fleet.getFleetData().sort();
				
				
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				
				FleetMemberAPI member = addToFleet(context.fleet, "paragon_Executive", random);
				context.fleet.getFleetData().setFlagship(member);
				
				String radiant = "radiant_Advanced";
				if (dwellerALot) radiant = "radiant_Restricted";
				member = addToFleet(context.fleet, radiant, random);
				
				PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, context.fleet.getFaction().getId(), random);
				person.getStats().setSkipRefresh(true);
				person.getStats().setLevel(8); // as if "integrated"
				person.getStats().resetSkills();
				person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
				person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
				person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
				person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
				person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
				person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);				
				person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
				person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
				person.getStats().setSkipRefresh(false);
				member.setCaptain(person);
				
				
				List<String> auroraVariants = new ArrayList<>();
				auroraVariants.add("aurora_Advanced");
				auroraVariants.add("aurora_Advanced");
				if (threat) {
					auroraVariants.add("aurora_Classified");
				} else {
					auroraVariants.add("aurora_Advanced");
				}
				if (dweller) {
					auroraVariants.add("aurora_Restricted");
				} else {
					auroraVariants.add("aurora_Advanced");
				}
				
				for (String vid : auroraVariants) {
					// "cryopod" style officers - 7 skills, 5 elite
					PersonAPI officer = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(random);
					officer.setPersonality(Personalities.AGGRESSIVE);
					officer.getStats().setLevel(7);
					officer.getStats().setSkipRefresh(true);
					officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
					//officer.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
					officer.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
					officer.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
					officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
					officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
					officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
					officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
					officer.getStats().setSkipRefresh(false);
					
					member = addToFleet(context.fleet, vid, random);
					member.setCaptain(officer);
				}
				
				for (int i = 0; i < 4; i++) {
					PersonAPI officer = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(random);
					officer.setPersonality(Personalities.AGGRESSIVE);
					officer.getStats().setLevel(5);
					officer.getStats().setSkipRefresh(true);
					officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
					officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
					officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 1);
					officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
					officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
					officer.getStats().setSkipRefresh(false);
					
					member = addToFleet(context.fleet, "medusa_Advanced", random);
					member.setCaptain(officer);
				}
				
				context.fleet.getFleetData().sort();
				
				for (FleetMemberAPI curr : context.fleet.getFleetData().getMembersListCopy()) {
					curr.updateStats();
					curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
				}
				
				
				CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
				CivOnlyChecker check = new CivOnlyChecker(context.fleet);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "eliminating competition", false, check, check);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "eliminating competition", false, check, check);
				
			}
		});
		
		
		e.endCreate();
				
	}
	
	public static void sendDiktatSuppliedMerc() {
		
		if (Global.getSettings().isDevMode()) {
			Global.getSector().getMemoryWithoutUpdate().unset(DelayedFleetEncounter.TIMEOUT_KEY);
		}
		
		Random random = new Random();
		
		PersonAPI commander = Global.getSector().getFaction(Factions.MERCENARY).createRandomPerson(random);
		commander.setPersonality(Personalities.AGGRESSIVE);
		commander.setRankId(Ranks.SPACE_CAPTAIN);
		commander.getStats().setLevel(15);
		commander.getStats().setSkipRefresh(true);
		
		//commander.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1); // making room for Phase Corps
		commander.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
		commander.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
		commander.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
		commander.getStats().setSkillLevel(Skills.FIGHTER_UPLINK, 1);
		commander.getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
		commander.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
		commander.getStats().setSkillLevel(Skills.OFFICER_MANAGEMENT, 1);
		commander.getStats().setSkillLevel(Skills.BEST_OF_THE_BEST, 1);
		commander.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
		
		commander.getStats().setSkillLevel(Skills.NAVIGATION, 1);
		commander.getStats().setSkillLevel(Skills.SENSORS, 1);
		commander.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
		commander.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
		commander.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
		commander.getStats().setSkillLevel(Skills.AUTOMATED_SHIPS, 1);
		commander.getStats().setSkipRefresh(false);
		
		
		Random r = Misc.getRandom(random.nextLong(), 3);
		DelayedFleetEncounter e = new DelayedFleetEncounter(r, "DiktatSuppliedMercHail");
		if (Global.getSettings().isDevMode()) {
			e.setDelayNone();
		} else {
			e.setDelay(30f);
		}
		e.setDoNotAbortWhenPlayerFleetTooStrong();
		e.setLocationCoreOnly(true, Factions.INDEPENDENT);
		e.beginCreate();
		e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_3, Factions.MERCENARY, FleetTypes.PATROL_LARGE, new Vector2f());
		e.triggerGetFleetParams().maxNumShips = 20;
		
		e.triggerSetFleetDoctrineComp(5, 0, 0);
		e.triggerSetFleetCommander(commander);
		e.triggerSetFleetOfficers(OfficerNum.FC_ONLY, OfficerQuality.DEFAULT);
		
		e.triggerFleetMakeFaster(false, 2, true);
		
		e.triggerSetFleetFaction(Factions.INDEPENDENT);
		e.triggerMakeNoRepImpact();
		e.triggerSetStandardAggroInterceptFlags();
		e.triggerMakeFleetIgnoreOtherFleets();
		e.triggerSetFleetGenericHailPermanent("DiktatSuppliedMercHail"); // this is the hail trigger
		
		//eagle_LG_Mercenary
		//kite_s_Command
		//glimmer_Mercenary
		//heron_Mercenary
		//afflictor_Mercenary
		
		e.triggerCustomAction(new TriggerAction() {
			@Override
			public void doAction(TriggerActionContext context) {
				context.fleet.setNoFactionInName(true);
				context.fleet.setName("Mercenary Force");
				
				for (FleetMemberAPI curr : context.fleet.getFleetData().getMembersListCopy()) {
					if (!curr.isCivilian()) {
						context.fleet.getFleetData().removeFleetMember(curr);
					}
				}
				context.fleet.getFleetData().sort();
				
				
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				
				FleetMemberAPI member = addToFleet(context.fleet, "kite_s_Command", random);
				context.fleet.getFleetData().setFlagship(member);
				
				for (int i = 0; i < 7; i++) {
					//String variant = "glimmer_Mercenary";
					String variant = "lumen_Mercenary";
					member = addToFleet(context.fleet, variant, random);
					
					PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, context.fleet.getFaction().getId(), random);
					person.getStats().setSkipRefresh(true);
					person.getStats().setLevel(8); // as if "integrated"
					person.getStats().resetSkills();
					person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
					person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
					person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
					person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
					person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
					person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);				
					person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
					//person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
					person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
					person.getStats().setSkipRefresh(false);
					member.setCaptain(person);
				}
				
				
				List<String> eagleVariants = new ArrayList<>();
				eagleVariants.add("eagle_LG_Mercenary");
				eagleVariants.add("eagle_LG_Mercenary");
				eagleVariants.add("eagle_LG_Mercenary");
				eagleVariants.add("eagle_LG_Mercenary");
				eagleVariants.add("eagle_LG_Mercenary");
				eagleVariants.add("eagle_LG_Mercenary");
				
				for (String vid : eagleVariants) {
					// level 6 officers with 2 elite skills, per the skill setup of the fleet commander
					PersonAPI officer = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(random);
					officer.setPersonality(Personalities.AGGRESSIVE);
					officer.getStats().setLevel(6);
					officer.getStats().setSkipRefresh(true);
					officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
					officer.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
					officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
					officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
					officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
					officer.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 1);
					officer.getStats().setSkipRefresh(false);
					
					member = addToFleet(context.fleet, vid, random);
					member.setCaptain(officer);
				}
				
				List<String> afflictorVariants = new ArrayList<>();
				afflictorVariants.add("afflictor_Mercenary");
				afflictorVariants.add("afflictor_Mercenary");
				afflictorVariants.add("afflictor_Mercenary");
				afflictorVariants.add("afflictor_Mercenary");
				
				for (String vid : afflictorVariants) {
					// level 6 officers with 2 elite skills, per the skill setup of the fleet commander
					PersonAPI officer = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(random);
					officer.setPersonality(Personalities.AGGRESSIVE);
					officer.getStats().setLevel(6);
					officer.getStats().setSkipRefresh(true);
					officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
					officer.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
					officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
					officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
					officer.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 1);
					officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
					officer.getStats().setSkipRefresh(false);
					
					member = addToFleet(context.fleet, vid, random);
					member.setCaptain(officer);
				}

				// captain-less, for use with Support Doctrine
				for (int i = 0; i < 3; i++) {
					member = addToFleet(context.fleet, "heron_Mercenary", random);
				}
				
				context.fleet.getFleetData().sort();
				
				for (FleetMemberAPI curr : context.fleet.getFleetData().getMembersListCopy()) {
					curr.updateStats();
					curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
				}
				
			}
		});
		
		
		e.endCreate();
		
	}
	
	
	public static void sendLGKillFleet() {
		
		if (Global.getSettings().isDevMode()) {
			Global.getSector().getMemoryWithoutUpdate().unset(DelayedFleetEncounter.TIMEOUT_KEY);
		}
		
		Random random = new Random();
		
		PersonAPI commander = Global.getSector().getFaction(Factions.DIKTAT).createRandomPerson(random);
		commander.setPersonality(Personalities.AGGRESSIVE);
		commander.setRankId(Ranks.SPACE_CAPTAIN);
		commander.getStats().setLevel(10);
		commander.getStats().setSkipRefresh(true);
		
		commander.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		commander.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		commander.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		commander.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		commander.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		commander.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		
		commander.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1); // making room for Phase Corps
		commander.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
		commander.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
		
		commander.getStats().setSkillLevel(Skills.NAVIGATION, 1);
		commander.getStats().setSkipRefresh(false);
		
		
		Random r = Misc.getRandom(random.nextLong(), 3);
		DelayedFleetEncounter e = new DelayedFleetEncounter(r, "LGKillFleet");
		if (Global.getSettings().isDevMode()) {
			e.setDelayNone();
		} else {
			e.setDelay(30f);
		}
		e.setDoNotAbortWhenPlayerFleetTooStrong();
		e.setLocationCoreOnly(true, Factions.DIKTAT);
		e.beginCreate();
		e.triggerCreateFleet(FleetSize.HUGE, FleetQuality.SMOD_1, Factions.LIONS_GUARD, FleetTypes.PATROL_LARGE, new Vector2f());
		e.triggerGetFleetParams().maxNumShips = 20;
		
		e.triggerSetFleetDoctrineComp(5, 0, 0);
		e.triggerSetFleetCommander(commander);
		e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.DEFAULT);
		
		e.triggerFleetMakeFaster(false, 2, true);
		
		e.triggerSetFleetFaction(Factions.DIKTAT);
		e.triggerMakeNoRepImpact();
		e.triggerSetStandardAggroInterceptFlags();
		e.triggerMakeFleetIgnoreOtherFleets();
		e.triggerSetFleetGenericHailPermanent("LGKillFleetHail"); // this is the hail trigger

		
		e.triggerCustomAction(new TriggerAction() {
			@Override
			public void doAction(TriggerActionContext context) {
				context.fleet.setNoFactionInName(true);
				context.fleet.setName("The Lion's Sword");
				
				
				FleetInflater inflater = context.fleet.getInflater();
				if (inflater != null && inflater.getParams() instanceof DefaultFleetInflaterParams) {
					DefaultFleetInflaterParams p = (DefaultFleetInflaterParams) inflater.getParams();
					p.allWeapons = true;
				}

				context.fleet.getFleetData().sort();
				
				for (FleetMemberAPI curr : context.fleet.getFleetData().getMembersListCopy()) {
					curr.updateStats();
					curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
				}
				
				
				CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
				CivOnlyChecker check = new CivOnlyChecker(context.fleet);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "pursuing enemies of the Supreme Executor", false, check, check);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "pursuing enemies of the Supreme Executor", false, check, check);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "pursuing enemies of the Supreme Executor", false, check, check);
				
			}
		});
		
		
		e.endCreate();
	}
	
	public static void sendLGKillFleet2() {
		
		if (Global.getSettings().isDevMode()) {
			Global.getSector().getMemoryWithoutUpdate().unset(DelayedFleetEncounter.TIMEOUT_KEY);
		}
		
		Random random = new Random();
		
		PersonAPI commander = Global.getSector().getFaction(Factions.DIKTAT).createRandomPerson(random);
		commander.setPersonality(Personalities.AGGRESSIVE);
		commander.setRankId(Ranks.SPACE_CAPTAIN);
		commander.getStats().setLevel(10);
		commander.getStats().setSkipRefresh(true);
		
		commander.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		commander.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		commander.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		commander.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		commander.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		commander.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		
		commander.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1); // making room for Phase Corps
		commander.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
		commander.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
		
		commander.getStats().setSkillLevel(Skills.NAVIGATION, 1);
		commander.getStats().setSkipRefresh(false);
		
		Random r = Misc.getRandom(random.nextLong(), 3);
		DelayedFleetEncounter e = new DelayedFleetEncounter(r, "LGKillFleet2");

		e.setDelayNone();
		e.setDoNotAbortWhenPlayerFleetTooStrong();
		e.setLocationCoreOnly(true, Factions.DIKTAT);
		e.beginCreate();
		e.triggerCreateFleet(FleetSize.HUGE, FleetQuality.SMOD_1, Factions.LIONS_GUARD, FleetTypes.PATROL_LARGE, new Vector2f());
		e.triggerGetFleetParams().maxNumShips = 20;
		
		e.triggerSetFleetDoctrineComp(5, 0, 0);
		e.triggerSetFleetCommander(commander);
		e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.DEFAULT);
		
		e.triggerFleetMakeFaster(false, 2, true);
		
		e.triggerSetFleetFaction(Factions.DIKTAT);
		e.triggerMakeNoRepImpact();
		e.triggerSetStandardAggroInterceptFlags();
		e.triggerMakeFleetIgnoreOtherFleets();
		e.triggerSetFleetGenericHailPermanent("LGKillFleet2Hail"); // this is the hail trigger

		
		e.triggerCustomAction(new TriggerAction() {
			@Override
			public void doAction(TriggerActionContext context) {
				context.fleet.setNoFactionInName(true);
				context.fleet.setName("The Lion's Shield");
				
				FleetInflater inflater = context.fleet.getInflater();
				if (inflater != null && inflater.getParams() instanceof DefaultFleetInflaterParams) {
					DefaultFleetInflaterParams p = (DefaultFleetInflaterParams) inflater.getParams();
					p.allWeapons = true;
				}

				context.fleet.getFleetData().sort();
				
				for (FleetMemberAPI curr : context.fleet.getFleetData().getMembersListCopy()) {
					curr.updateStats();
					curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
				}
				
				CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
				CivOnlyChecker check = new CivOnlyChecker(context.fleet);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "pursuing enemies of the Supreme Executor", false, check, check);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "pursuing enemies of the Supreme Executor", false, check, check);
				context.fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 16f, "pursuing enemies of the Supreme Executor", false, check, check);
				
			}
		});
		
		e.endCreate();
	}
	
	public static class CivOnlyChecker implements Script {
		public CampaignFleetAPI fleet;
		public CivOnlyChecker(CampaignFleetAPI fleet) {
			this.fleet = fleet;
		}
		@Override
		public void run() {
			if (Misc.isFleetCivOnly(fleet)) {
				fleet.clearAssignments();
			}
		}
	}
	
	public static FleetMemberAPI addToFleet(CampaignFleetAPI fleet, String variantId, Random random) {
		FleetMemberAPI member = fleet.getFleetData().addFleetMember(variantId);
		member.setId(member.getVariant().getHullVariantId() + "_" + random.nextLong());
		ShipVariantAPI v = member.getVariant().clone();
		v.setSource(VariantSource.REFIT);
		v.addTag(Tags.TAG_NO_AUTOFIT);
		if (Misc.isAutomated(member)) {
			v.addTag(Tags.TAG_AUTOMATED_PENALTY);
		}
		member.setVariant(v, false, true);
		return member;
	}
	
	public static PersonAPI createOfficer(
			CampaignFleetAPI fleet, int level, int numElite, SkillPickPreference pref, String personalityId, Random random) {
		FactionAPI faction = fleet.getFaction();
		PersonAPI officer = OfficerManagerEvent.createOfficer(
				faction, level, pref, 
				false, fleet, true, true, numElite, random);
		officer.setPersonality(personalityId);
		return officer;
	}
}











