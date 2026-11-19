package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDShipsDestroyedFactor.ShipsDestroyedType;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class SDDevastation extends BaseEventIntel implements FleetEventListener {
	
	public static float PERSONAL_FLEET_DEFEAT_TIMEOUT = 1000f;
	
	public static int PROGRESS_MAX = 500;
	public static int PROGRESS_1 = 100;
	public static int PROGRESS_2 = 200;
	public static int PROGRESS_3 = 300;
	public static int PROGRESS_4 = 400;
	
	public static String KEY = "$sdd_ref";
	
	public static enum Stage {
		START,
		LEVEL_1,
		LEVEL_2,
		LEVEL_3,
		LEVEL_4,
		FINAL,
	}
	

	public static void addFactorCreateIfNecessary(EventFactor factor, InteractionDialogAPI dialog) {
		if (get() == null) {
			new SDDevastation(null, false);
		}
		if (get() != null) {
			get().addFactor(factor, dialog);
		}
	}
	
	public static SDDevastation get() {
		return (SDDevastation) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	
	protected TimeoutTracker<String> personalFleetDefeats = new TimeoutTracker<>();
	
	
	public SDDevastation(TextPanelAPI text, boolean withIntelNotification) {
		super();
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		setup();
		
		// now that the event is fully constructed, add it and send notification
		Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
	}
	
	protected void setup() {
		factors.clear();
		stages.clear();
		
		setMaxProgress(PROGRESS_MAX);
		
		addStage(Stage.START, 0);
		addStage(Stage.LEVEL_1, PROGRESS_1, StageIconSize.SMALL);
		addStage(Stage.LEVEL_2, PROGRESS_2, StageIconSize.MEDIUM);
		addStage(Stage.LEVEL_3, PROGRESS_3, StageIconSize.MEDIUM);
		addStage(Stage.LEVEL_4, PROGRESS_4, StageIconSize.MEDIUM);
		addStage(Stage.FINAL, PROGRESS_MAX, StageIconSize.LARGE);
		
//		getDataFor(Stage.LEVEL_1).keepIconBrightWhenLaterStageReached = true;
//		getDataFor(Stage.LEVEL_2).keepIconBrightWhenLaterStageReached = true;
//		getDataFor(Stage.LEVEL_3).keepIconBrightWhenLaterStageReached = true;
//		getDataFor(Stage.LEVEL_4).keepIconBrightWhenLaterStageReached = true;
//		getDataFor(Stage.FINAL).keepIconBrightWhenLaterStageReached = true;
		
		addMarketFactor("sindria");
		addMarketFactor("volturn");
		addMarketFactor("cruor");
		addFactor(new SDDFuelFactor());
		addFactor(new SDDHighCommandFactor());
	}
	
	protected void addMarketFactor(String marketId) {
		MarketAPI market = Global.getSector().getEconomy().getMarket(marketId);
		if (market == null || market.hasCondition(Conditions.DECIVILIZED)) return;
		addFactor(new SDDInstabilityFactor(market));
	}
	
	protected Object readResolve() {
		return this;
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
			if (esd.id == Stage.LEVEL_1) {
				info.addPara("The Sindrian Diktat is beginning to suffer the effects of civil war.", tc, initPad);
			}
			if (esd.id == Stage.LEVEL_2) {
				info.addPara("The Sindrian Diktat is suffering mounting losses to fleets and industry.", tc, initPad);
			}
			if (esd.id == Stage.LEVEL_3) {
				info.addPara("The Sindrian Diktat cannot stop the devastation spreading across the Askonia system.", tc, initPad);
			}
			if (esd.id == Stage.LEVEL_4) {
				info.addPara("The Sindrian Diktat has been crippled by the ravages of war and insurrection.", tc, initPad);
			}
			if (esd.id == Stage.FINAL) {
				info.addPara("The Askonia system lies in ruin. The Sindrian Diktat has been effectively destroyed.", tc, initPad);
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
		if (stageId == Stage.START) {
			info.addPara("The fleets and infrastructure of the Sindrian Diktat are in a condition much as they were in recent cycles.", initPad);
		} else if (stageId == Stage.LEVEL_1) {
			info.addPara("The uniforms of the Sindrian Diktat are not quite so bright, the hulls of their ships not so perfect and polished. "
					+ "The effects of conflict are beginning to show themselves.", initPad);
		} else if (stageId == Stage.LEVEL_2) {
				info.addPara("The state of Askonia can be seen in faces - no longer so eager for battle, but grim and fearful. "
						+ "Acrid smoke taints the atmo of warship and civilian hab alike, as both suffer violence and destruction.", initPad);
		} else if (stageId == Stage.LEVEL_3) {
			info.addPara("Scars of rebellion and war are evident throughout the Askonia system. Cold wrecks drift on lost orbits; the corridors "
					+ "of civilian habitats have grown checkpoints and battle-damage.", initPad);
		} else if (stageId == Stage.LEVEL_4) {
			info.addPara("Blasted derelicts drift where once trade convoys packed with fuel roamed. The cities burn, industry lies smoldering, "
					+ "and the skies are regularly filled with new novae of battle and slaughter.", initPad);
		} else if (stageId == Stage.FINAL) {
			info.addPara("The Sindrian Diktat is in ruin. It's fleets are burned and scattered, or rife with mutiny; its planets engulfed in endless "
					+ "uprisings, their industries crumbling. The grim faces on the news-holos ask the silent question: what is there left to be fought over?", initPad);
		}
	}
	
	public TooltipCreator getStageTooltipImpl(Object stageId) {
		final EventStageData esd = getDataFor(stageId);
		
		if (esd != null) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					
					if (esd.id == Stage.START) {
						tooltip.addTitle("Untouched");
					} else if (esd.id == Stage.LEVEL_1) {
						tooltip.addTitle("Suffering");
					} else if (esd.id == Stage.LEVEL_2) {
						tooltip.addTitle("Faltering");
					} else if (esd.id == Stage.LEVEL_3) {
						tooltip.addTitle("Widespread Destruction");
					} else if (esd.id == Stage.LEVEL_4) {
						tooltip.addTitle("Devastated");
					} else if (esd.id == Stage.FINAL) {
						tooltip.addTitle("Absolute Ruin");
						//tooltip.addTitle("Ddddddevastated"); // I'm so sorry we can't use this. But.
					}

					addStageDesc(tooltip, esd.id, opad, true);
					
					esd.addProgressReq(tooltip, opad);
				}
			};
		}
		
		return null;
	}


	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("events", "sd_devastation");
	}

	protected String getStageIconImpl(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null) return null;
		
	
		if (EnumSet.of(Stage.START, Stage.LEVEL_1, Stage.LEVEL_2, Stage.LEVEL_3, 
				Stage.LEVEL_4, Stage.FINAL).contains(esd.id)) {
			return Global.getSettings().getSpriteName("events", "sd_devastation_" + ((Stage)esd.id).name());
		}
		return Global.getSettings().getSpriteName("events", "sd_devastation");
	}
	
	
	@Override
	public Color getBarColor() {
		Color color = Misc.getNegativeHighlightColor();
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
		return "Sindrian Diktat Devastation";
	}
	

	public void reportFleetDespawnedToListener(CampaignFleetAPI curr, FleetDespawnReason reason, Object param) {
		if (reason != FleetDespawnReason.DESTROYED_BY_BATTLE && reason != FleetDespawnReason.NO_MEMBERS) return;
		doSpecialFleetCheck(curr);
	}
	public boolean doSpecialFleetCheck(CampaignFleetAPI curr) {
		if (curr.getCommander() == People.getPerson(People.CADEN) &&
				curr.getFleetPoints() < 100) {
			String id = curr.getCommander().getId();
			if (personalFleetDefeats.contains(id)) return false;
			personalFleetDefeats.add(id, PERSONAL_FLEET_DEFEAT_TIMEOUT);
			addFactor(new CadenFleetFactor(curr.getName() + " destroyed", SDDPoints.CADEN_FLEET), null);
			if (SDResolution.get() != null) {
				if (curr.getFaction().getId().equals(Factions.DIKTAT)) {
					SDResolution.get().addFactor(new CadenFleetFactor(curr.getName() + " defeated", 
							SDRPoints.CADEN_FLEET), null);
				} else if (curr.getFaction().getId().equals(Factions.DIKTAT_ACHILLES)) {
					SDResolution.get().addFactor(new CadenFleetFactor(curr.getName() + " defeated", 
							-SDRPoints.CADEN_FLEET), null);
				}
			}
			Global.getSector().getMemoryWithoutUpdate().set("$cadenDefeatedDuringStandoff", true);
			return true;
		} else if (curr.getCommander() == People.getPerson(People.HYDER) &&
				curr.getFleetPoints() < 100) {
			String id = curr.getCommander().getId();
			if (personalFleetDefeats.contains(id)) return false;
			personalFleetDefeats.add(id, PERSONAL_FLEET_DEFEAT_TIMEOUT);
			addFactor(new HyderFleetFactor(curr.getName() + " destroyed", SDDPoints.HYDER_FLEET), null);
			if (SDResolution.get() != null) {
				if (curr.getFaction().getId().equals(Factions.DIKTAT)) {
					SDResolution.get().addFactor(new HyderFleetFactor(curr.getName() + " defeated", 
							SDRPoints.HYDER_FLEET), null);
				} else if (curr.getFaction().getId().equals(Factions.DIKTAT_ACHILLES)) {
					SDResolution.get().addFactor(new HyderFleetFactor(curr.getName() + " defeated", 
							-SDRPoints.HYDER_FLEET), null);
				}
			}
			Global.getSector().getMemoryWithoutUpdate().set("$hyderDefeatedDuringStandoff", true);
			return true;
		} else if (curr.getCommander() == People.getPerson(People.ACHILLES) &&
				curr.getFleetPoints() < 100) {
			String id = curr.getCommander().getId();
			if (personalFleetDefeats.contains(id)) return false;
			personalFleetDefeats.add(id, PERSONAL_FLEET_DEFEAT_TIMEOUT);
			if (SDResolution.get() != null) {
				SDResolution.get().addFactor(new AchillesFleetFactor(), null);
			}
			return true;
		} else if (curr.getCommander() == People.getPerson(People.HECTOR) &&
				curr.getFleetPoints() < 100) {
			String id = curr.getCommander().getId();
			if (personalFleetDefeats.contains(id)) return false;
			personalFleetDefeats.add(id, PERSONAL_FLEET_DEFEAT_TIMEOUT);
			if (SDResolution.get() != null) {
				SDResolution.get().addFactor(new HectorFleetFactor(), null);
			}
			return true;
		}
		return false;
	}
	
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isEnded() || isEnding()) return;
		
		//if (!battle.isPlayerInvolved()) return;
		
//		public static int CADEN_FLEET = 10;
//		public static int HYDER_FLEET = 15;
//		public static int FP_PER_POINT = 30;
//		public static float FP_MULT_TRADE_FLEET = 0f;
		
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		if (sindria == null) return;
		StarSystemAPI askonia = sindria.getStarSystem();
		
		for (CampaignFleetAPI curr : battle.getSnapshotBothSides()) {
			if (curr.getContainingLocation() != askonia) break;
			
			if (curr.isStationMode() && curr.getFleetData().getMembersListCopy().isEmpty()) {
				boolean diktat = curr.getFaction().getId().equals(Factions.DIKTAT); 
				boolean achilles = curr.getFaction().getId().equals(Factions.DIKTAT_ACHILLES);
				
				addFactor(new SDStationDestroyedFactor(SDDPoints.STATION_DESTROYED, curr.getName()));
				if (SDResolution.get() != null) {
					if (diktat) {
						SDResolution.get().addFactor(new SDStationDestroyedFactor(SDRPoints.STATION_DESTROYED, curr.getName()));
					} else if (achilles) {
						SDResolution.get().addFactor(new SDStationDestroyedFactor(-SDRPoints.STATION_DESTROYED, curr.getName()));
					}
				}
			}
		}
		
		
		for (CampaignFleetAPI curr : battle.getBothSides()) {
			doSpecialFleetCheck(curr);
		}
		
		float fpDestroyedDiktat = 0;
		float fpDestroyedAchilles = 0;
		float lgFPDestroyed = 0;
		float fpDestroyedTrader = 0;
		for (CampaignFleetAPI other : battle.getSnapshotBothSides()) {
			if (isSpecialFleet(other)) continue;
			if (other.getFaction() == null) continue;
			boolean diktat = other.getFaction().getId().equals(Factions.DIKTAT); 
			boolean achilles = other.getFaction().getId().equals(Factions.DIKTAT_ACHILLES); 
			if (!diktat && !achilles) continue;
			
			boolean trader = Misc.isTrader(other);
			boolean lg = other.getName().toLowerCase().contains("lion's guard");
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(other)) {
				if (trader) {
					fpDestroyedTrader += loss.getFleetPointCost() * SDDPoints.FP_MULT_TRADE_FLEET;
				} else {
					if (lg) {
						lgFPDestroyed += loss.getFleetPointCost() * SDDPoints.FP_MULT_LG_FLEET;
					} else {
						if (diktat) {
							fpDestroyedDiktat += loss.getFleetPointCost();
						} else if (achilles) {
							fpDestroyedAchilles += loss.getFleetPointCost();
						}
					}
				}
			}
		}

		int points = computeProgressPoints(fpDestroyedDiktat + fpDestroyedAchilles, SDDPoints.FP_PER_POINT);
		if (points > 0) {
			addFactor(new SDShipsDestroyedFactor(points, ShipsDestroyedType.GENERAL));
		}
		
		points = computeProgressPoints(lgFPDestroyed, SDDPoints.FP_PER_POINT);
		if (points > 0) {
			addFactor(new SDLGShipsDestroyedFactor(points));
		}
		
		points = computeProgressPoints(fpDestroyedTrader, SDDPoints.FP_PER_POINT);
		if (points > 0) {
			addFactor(new SDDTradeShipsDestroyedFactor(points));
		}
		
		if (SDResolution.get() != null) {
			points = computeProgressPoints(fpDestroyedDiktat + lgFPDestroyed, SDRPoints.FP_PER_POINT);
			if (points > 0) {
				SDResolution.get().addFactor(new SDShipsDestroyedFactor(points, ShipsDestroyedType.ABSOLUTIST));
			}
			points = computeProgressPoints(fpDestroyedAchilles, SDRPoints.FP_PER_POINT);
			if (points > 0) {
				SDResolution.get().addFactor(new SDShipsDestroyedFactor(-points, ShipsDestroyedType.REFORMER));
			}
		}
		
		
	}
	
	public static boolean isSpecialFleet(CampaignFleetAPI fleet) {
		if (fleet == null) return false;
		return fleet.getCommander() == People.getPerson(People.HYDER) ||
				fleet.getCommander() == People.getPerson(People.ACHILLES) ||
				fleet.getCommander() == People.getPerson(People.HECTOR) ||
				fleet.getCommander() == People.getPerson(People.CADEN);
	}


	public static int computeProgressPoints(float fleetPointsDestroyed, float pointsPer) {
		if (fleetPointsDestroyed <= 0) return 0;

		int points = Math.round(fleetPointsDestroyed / pointsPer);
		if (points < 1) points = 1;
		return points;
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
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		personalFleetDefeats.advance(days);
		//setProgress(getProgress() + 10);
	}
	
	
	@Override
	protected void notifyStageReached(EventStageData stage) {
		
		if (stage.id == Stage.LEVEL_1) {
		}
	}
	
	public boolean withMonthlyFactors() {
		return true; // ??? maybe
	}
	
	protected String getSoundForStageReachedUpdate(Object stageId) {
		return super.getSoundForStageReachedUpdate(stageId);
	}

	@Override
	protected String getSoundForOneTimeFactorUpdate(EventFactor factor) {
		return null;
	}
	
	
}








