package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEntityPickerListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.BaseFleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.PlayerFleetPersonnelTracker;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.SupportFleetSpawner;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.CryosleeperIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.HypershuntIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.SupportFleetIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.WormholeIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.askonia.SDSevenSons.SD7SqarasAgentFIDConfig;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl;
import com.fs.starfarer.api.impl.campaign.world.NamelessRock;
import com.fs.starfarer.api.impl.combat.threat.DisposableThreatFleetManager;
import com.fs.starfarer.api.impl.combat.threat.DisposableThreatFleetManager.ThreatFleetCreationParams;
import com.fs.starfarer.api.impl.combat.threat.ThreatFleetBehaviorScript;
import com.fs.starfarer.api.ui.MarkerData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Pair;

/**
 * 
 */
public class MiscCMD extends BaseCommandPlugin {

	public static float MILITARY_BASE_SUPPORT_RANGE_LY = Global.getSettings().getFloat("militaryBaseSupportFleetRange");
	public static float HIGH_COMMAND_SUPPORT_RANGE_LY = Global.getSettings().getFloat("highCommandSupportFleetRange");
	
	public static float RESUPPLY_QUANTITY_MULT = Global.getSettings().getFloat("resupplyQuantityMult");
	
	public static int RESUPPLY_SUPPLIES = 300;
	public static int RESUPPLY_FUEL = 750;
	public static int RESUPPLY_VOLATILES = 20;
	public static int RESUPPLY_TRANSPLUTONICS = 10;
	public static int RESUPPLY_MACHINERY = 20;
	public static int RESUPPLY_CREW = 200;
	
	public static enum SupportFleetType {
		RESUPPLY("Supply Fleet"),
		TASK_FORCE("Task Force");
		
		private String name;
		private SupportFleetType(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		
	}
	
	public static enum SupportFleetSize{
		SMALL("Small"),
		MEDIUM("Medium"),
		LARGE("Large");
		private String name;
		private SupportFleetSize(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected MemoryAPI memory;
	protected MemoryAPI local;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;

	
	public MiscCMD() {
	}
	
	public MiscCMD(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		
		
	}
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		
		init(entity);
		
		memory = getEntityMemory(memoryMap);
		local = memoryMap.get(MemKeys.LOCAL);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		if (command.equals("retirementGameEnd")) {
			dialog.showRetirementGameEndDialog();
			dialog.hideTextPanel();
			dialog.hideVisualPanel();
			dialog.getOptionPanel().clearOptions();
			dialog.getOptionPanel().addOption("", new Object());
			dialog.getOptionPanel().clearOptions();
			dialog.setPromptText("");
		} else if (command.equals("cryosanctumGameEnd")) {
			dialog.showCryosanctumGameEndDialog();
			dialog.hideTextPanel();
			dialog.hideVisualPanel();
			dialog.getOptionPanel().clearOptions();
			dialog.getOptionPanel().addOption("", new Object());
			dialog.getOptionPanel().clearOptions();
			dialog.setPromptText("");
		} else if (command.equals("givePostSDRachillesReward")) {
			int _mult = 4 - Global.getSector().getMemoryWithoutUpdate().getInt("$sddDevastationLevel");
			
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistAchilles")) _mult++;
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAchilles")) _mult++;
			if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_suggestedHyderHelpAchilles")) _mult++;
			if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToAchilles")) _mult++;
			
			// add credits... value, anyway.
			
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_achillesWonPL")) {
				Global.getSector().getMemoryWithoutUpdate().set("$postSDRachillesRewards", _mult * 30000, 0);
			}
			else {
				Global.getSector().getMemoryWithoutUpdate().set("$postSDRachillesRewards", _mult * 50000, 0);
			}
			
			
		} else if(command.equals("fleetClearOrdersAndReturn")) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			if(fleet != null) {
				Misc.giveStandardReturnToSourceAssignments(fleet, true);
			}
		} else if (command.equals("printResupplyFleetData")) {
			printResupplyFleetData();
		} else if (command.equals("printTaskForceData")) {
			printTaskForceData();
		} else if (command.equals("playerHasMilitaryBases")) {
			return playerHasMilitaryBaseOrHighCommand();
		} else if (command.equals("playerHasMilitaryBasesOrHQ")) {
			return playerHasMilitaryBaseOrHighCommand() || playerHasPatrolHQ();
		} else if (command.equals("playerOnlyHasPatrolHQ")) {
			return playerOnlyHasPatrolHQ();
		} else if (command.equals("taskForceCheckNum")) {
			if (local != null) {
				int resupply = SupportFleetIntel.getNumSupportFleets(SupportFleetType.RESUPPLY, false);
				int taskForce = SupportFleetIntel.getNumSupportFleets(SupportFleetType.TASK_FORCE, false);
				local.set("$numResupplyFleets", resupply, 0f);
				local.set("$numTaskForces", taskForce, 0f);
			}
		} else if (command.equals("unsetMarketHostileTimeout")) {
			if (params.size() > 1) {
				//SectorEntityToken entity = dialog.getInteractionTarget();

				//MarketAPI market = entity.getActivePerson();
				MarketAPI _targetMarket = Global.getSector().getEconomy().getMarket(params.get(1).getString(memoryMap));
				if (_targetMarket == null) return false;
				_targetMarket.getMemoryWithoutUpdate().unset("$playerHostileTimeout");
			}
			
		} else if (command.equals("taskForceExtend")) {
			if (entity instanceof CampaignFleetAPI fleet) {
				SupportFleetIntel intel = SupportFleetIntel.getFleetIntel(fleet);
				if (intel != null) {
					intel.extendOpTime();
				}
			}
		} else if (command.equals("taskForceFollow")) {
			if (entity instanceof CampaignFleetAPI fleet) {
				SupportFleetIntel intel = SupportFleetIntel.getFleetIntel(fleet);
				if (intel != null) {
					intel.giveFollowAssignment();
				}
			}
		} else if (command.equals("taskForceStay")) {
			if (entity instanceof CampaignFleetAPI fleet) {
				SupportFleetIntel intel = SupportFleetIntel.getFleetIntel(fleet);
				if (intel != null) {
					intel.giveStayAssignment();
				}
			}
		} else if (command.equals("setDaysSinceStart1000")) {
			PirateBaseManager.getInstance().setUnadjustedDaysSinceStart(1000);
		} else if (command.equals("macarioToPreacher")) {
			if (Global.getSector().getImportantPeople().getData(People.MACARIO) == null) return false;
			PersonAPI macario = Global.getSector().getImportantPeople().getData(People.MACARIO).getPerson();
			macario.setRankId(Ranks.FUGITIVE);
			macario.setPostId(Ranks.POST_EXILE);
			macario.setFaction(Factions.INDEPENDENT);
			macario.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dolos_macario2"));
		
		} else if (command.equals("giveResupply")) {
			if (entity instanceof CampaignFleetAPI fleet) {
				String size = fleet.getMemoryWithoutUpdate().getString("$supportFleetSize");
				float qtyMult = getResupplyQuantityMult(Misc.getSourceMarket(fleet), size);
				
				float spawnFP = fleet.getMemoryWithoutUpdate().getInt("$supportFleetSpawnFP");
				float fp = fleet.getFleetPoints();
				float f = (fp / Math.max(1f, spawnFP));
				if (f > 1f) f = 1f;
				if (f < 0.1f) f = 0.1f;
				qtyMult *= f;
				
				Random random = Misc.getRandom(Misc.getSalvageSeed(fleet), 7);
				int supplies = (int) ((RESUPPLY_SUPPLIES + random.nextInt((int) (RESUPPLY_SUPPLIES * 0.25f))) * qtyMult);
				int fuel = (int) ((RESUPPLY_FUEL + random.nextInt((int) (RESUPPLY_FUEL * 0.25f))) * qtyMult);
				int volatiles = (int) ((RESUPPLY_VOLATILES + random.nextInt((int) (RESUPPLY_VOLATILES * 0.25f))) * qtyMult);
				int tp = (int) ((RESUPPLY_TRANSPLUTONICS + random.nextInt((int) (RESUPPLY_TRANSPLUTONICS * 0.25f))) * qtyMult);
				int machinery = (int) ((RESUPPLY_MACHINERY + random.nextInt((int) (RESUPPLY_MACHINERY * 0.25f))) * qtyMult);
				
				float extMult = Misc.isExtended() ? Global.getSettings().getFloat("ext_resupplyCrewBroughtMult") : 1f;
				int crew = (int) ((RESUPPLY_CREW + random.nextInt((int) (RESUPPLY_CREW * 0.25f))) * qtyMult * extMult);
				crew = Math.min(crew, playerFleet.getCargo().getFreeCrewSpace());
				
//				int totalValue = (int) (Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getBasePrice() * supplies);
//				totalValue += Global.getSettings().getCommoditySpec(Commodities.FUEL).getBasePrice() * fuel;
//				totalValue += Global.getSettings().getCommoditySpec(Commodities.VOLATILES).getBasePrice() * volatiles;
//				totalValue += Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY).getBasePrice() * machinery;
				
				cargo.addCommodity(Commodities.SUPPLIES, supplies);
				AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, supplies, text);
				
				cargo.addCommodity(Commodities.FUEL, fuel);
				AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, fuel, text);
				
				cargo.addCommodity(Commodities.VOLATILES, volatiles);
				AddRemoveCommodity.addCommodityGainText(Commodities.VOLATILES, volatiles, text);
				
				cargo.addCommodity(Commodities.RARE_METALS, tp);
				AddRemoveCommodity.addCommodityGainText(Commodities.RARE_METALS, tp, text);
				
				cargo.addCommodity(Commodities.HEAVY_MACHINERY, machinery);
				AddRemoveCommodity.addCommodityGainText(Commodities.HEAVY_MACHINERY, machinery, text);
				
				cargo.addCommodity(Commodities.CREW, crew);
				AddRemoveCommodity.addCommodityGainText(Commodities.CREW, crew, text);
				
				Misc.giveStandardReturnToSourceAssignments(fleet, true);
			}
		} else if (command.equals("fleetConfirmedNormal")) {
			sendSupportFleet(false);
		} else if (command.equals("fleetConfirmedInstant")) {
			sendSupportFleet(true);
		} else if (command.equals("playerHasHighCommand")) {
			return playerHasHighCommand();
		} else if (command.equals("loseContact")) {
			PersonAPI person = null;
			boolean _dead = false;
			if (params.size() == 1) {
				//SectorEntityToken entity = dialog.getInteractionTarget();
				if (entity.getActivePerson() == null) return false;
				person = entity.getActivePerson();
			}
			else if (params.size() >= 2) {
				String id = params.get(1).getString(memoryMap);
				person = Global.getSector().getImportantPeople().getPerson(id);
			}
			if (person == null) return false;
			
			if (params.size() == 3 && params.get(2).getBoolean(memoryMap)) {
				_dead = true;
			}

			ContactIntel intel = ContactIntel.getContactIntel(person);
			if (intel == null) return false;
			
			if(!_dead) {
				CustomRepImpact impact = new CustomRepImpact();
				impact.delta = -0.75f;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, 
								impact, null, text, true, true),
								person);
			}
			
			intel.loseContact(dialog);
			
			return true;
		} else if (command.equals("pickFleetDestination")) {
			final ArrayList<SectorEntityToken> systems = new ArrayList<SectorEntityToken>();
			//boolean highCommand = playerHasHighCommand();
			final List<MarketAPI> bases = getPlayerMilitaryBases();
			
			for (StarSystemAPI curr : Global.getSector().getStarSystems()) {
				if (curr.hasTag(Tags.THEME_HIDDEN)) continue;
				if (curr.hasTag(Tags.SYSTEM_NOT_SUPPORT_FLEET_TARGET)) continue;
				if (curr.isDeepSpace()) continue;
				if (curr.getHyperspaceAnchor() == null) continue;
				if (Misc.getStarSystemForAnchor(curr.getHyperspaceAnchor()) == null) continue;
				float depth = Misc.getAbyssalDepth(curr.getLocation());
				if (depth > HyperspaceAbyssPluginImpl.DEPTH_THRESHOLD_FOR_ABYSSAL_STAR_SYSTEM) continue;
				if (!isInRangeOfBase(curr, bases)) continue;
				
				systems.add(curr.getHyperspaceAnchor());
			}
			
			final SupportFleetType type = Enum.valueOf(SupportFleetType.class, local.getString("$fleetType")); 
			final SupportFleetSize size = Enum.valueOf(SupportFleetSize.class, local.getString("$fleetSize"));
			
//			String title = "Select destination star system for " + Misc.lcFirst(size.getName()) + " " +
//							Misc.lcFirst(type.getName());
			String title = "Select destination star system for: " + type.getName() + " - " + size.getName() + ""; 
			
			dialog.showCampaignEntityPicker(title, "Target:", "Continue", 
					Global.getSector().getPlayerFaction(), systems, 
				new BaseCampaignEntityPickerListener() {
					@Override
					public List<MarkerData> getMarkers() {
						ArrayList<MarkerData> result = new ArrayList<MarkerData>();
						result.add(new MarkerData(Global.getSector().getPlayerFleet().getLocationInHyperspace(), null, null));
						for (MarketAPI base : bases) {
							float scale = 1f;
							if (isHighCommand(base)) scale += 0.5f;
							Color c = Global.getSettings().getBasePlayerColor();
							c = Misc.getPositiveHighlightColor();
							MarkerData data = new MarkerData(base.getLocation(), null, c, scale);
							result.add(data);
						}
						return result;
					}
					public void pickedEntity(SectorEntityToken entity) {
						int cost = getFleetCost(entity, bases, type, size);
						local.set("$fleetCost", Misc.getDGSCredits(cost), 0f);
						local.set("$fleetDest", entity, 0f);
						local.set("$fleetTypeNameInText", size.getName().toLowerCase() + " " + type.getName().toLowerCase(), 0f);
						
						int days = getFleetETA(entity, bases);
						local.set("$fleetDays", "" + days, 0f);
						
						StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
						local.set("$fleetDestinationName", system.getNameWithLowercaseType(), 0f);
						FireBest.fire(null, dialog, memoryMap, "CallFleetTargetSelected");
					}
					public void cancelledEntityPicking() {
						
					}
					public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
						StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
						if (system != null) {
							return system.getNameWithLowercaseTypeShort();
						}
						return null;
					}
					public String getSelectedTextOverrideFor(SectorEntityToken entity) {
						StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
						if (system != null) {
							return system.getNameWithLowercaseType();
						}
						return null;
					}
					public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {
						int days = getFleetETA(entity, bases);
						int cost = getFleetCost(entity, bases, type, size);
						int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
						
						Color reqColor = Misc.getHighlightColor();
						Color availableColor = Misc.getHighlightColor();
						if (cost > credits) {
							reqColor = Misc.getNegativeHighlightColor();
						}
						
						info.setParaSmallInsignia();
						info.beginGrid(250f, 2, Misc.getGrayColor());
						info.setGridFontSmallInsignia();
						info.addToGrid(0, 0, "    Estimated days to arrival:", "" + days, availableColor);
						info.addToGrid(1, 0, "    Credits required:", Misc.getDGSCredits(cost), reqColor);
						info.addGrid(0);
					}

					public boolean canConfirmSelection(SectorEntityToken entity) {
						int cost = getFleetCost(entity, bases, type, size);
						int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
						return cost <= credits;
					}
					public float getFuelColorAlphaMult() {
						return 0f;
					}
					public float getFuelRangeMult() { // just for showing it on the map when picking destination
						return 0f;
					}
				});
		} else if (command.equals("addCryosleeperIntel")) {
			if (CryosleeperIntel.getCryosleeperIntel(entity) == null) {
				new CryosleeperIntel(entity, dialog.getTextPanel());
			}
		} else if (command.equals("addHypershuntIntel")) {
			if (HypershuntIntel.getHypershuntIntel(entity) == null) {
				new HypershuntIntel(entity, dialog.getTextPanel());
			}
		} else if (command.equals("addWormholeIntel")) {
			addWormholeIntelIfNeeded(entity, dialog.getTextPanel(), true);
		} else if (command.equals("addMarineXP")) {
			float amount = params.get(1).getFloat(memoryMap);
			PlayerFleetPersonnelTracker.getInstance().update();
			PlayerFleetPersonnelTracker.getInstance().getMarineData().addXP(amount);
		} else if (command.equals("mk1_spawnThreatFleet")) {
			StarSystemAPI system = Global.getSector().getStarSystem(NamelessRock.NAMELESS_ROCK_LOCATION_ID);
			
			boolean moreDangerous = Misc.isExtended() && Global.getSettings().getBoolean("ext_moreDangerousMkIThreat");
			
			ThreatFleetCreationParams p = new ThreatFleetCreationParams();
			p.numHives = 1;
			p.numOverseers = 1;
			p.numDestroyers = 2;
			p.numFrigates = 4;
			if (moreDangerous) {
				p.numFabricators = 1;
				p.numHives = 2;
				p.numOverseers = 4;
			}
			p.fleetType = FleetTypes.PATROL_SMALL;
			
			CampaignFleetAPI fleet = DisposableThreatFleetManager.createThreatFleet(p, new Random());
			ThreatFleetBehaviorScript behavior = new ThreatFleetBehaviorScript(fleet, system);
			behavior.setSeenByPlayer();
			fleet.addScript(behavior);
			
			fleet.addAssignmentAtStart(FleetAssignment.INTERCEPT, Global.getSector().getPlayerFleet(), 10f, null);
			fleet.getMemoryWithoutUpdate().set(FleetAIFlags.LAST_SEEN_TARGET_LOC, 
											   new Vector2f(Global.getSector().getPlayerFleet().getLocation()), 10f);
			
			system.addEntity(fleet);
			
			if (false && moreDangerous) {
				// instead of this: made it require 4 SP to recover in ext mode, plus a load of d-mods
				float radius = 150f + 150f * (float) Math.random();
				Vector2f loc = Misc.getPointAtRadius(playerFleet.getLocation(), radius);
				fleet.setLocation(loc.x, loc.y);
				//fleet.getStats().getFleetwideMaxBurnMod().modifyFlat("ambush", 10);
				fleet.addAbility(Abilities.EMERGENCY_BURN);
				fleet.getAbility(Abilities.EMERGENCY_BURN).activate();
			} else {
				float radius = 1000f + 500f * (float) Math.random();
				Vector2f loc = Misc.getPointAtRadius(playerFleet.getLocation(), radius);
				fleet.setLocation(loc.x, loc.y);
			}
			
		}
		else if (command.equals("sd7sCreateQarasAgentAndFleet")) {
			// Because it is remotely possible to have to spawn this after SD7S has completed!

			MarketAPI qaras = Global.getSector().getEconomy().getMarket("qaras");
			if (qaras == null) return false;
			
			MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
			if (sindria == null) return false;
		
			PersonAPI qaras_agent = Global.getSector().getFaction(Factions.DIKTAT).createRandomPerson();
			qaras_agent.setRankId(Ranks.SPACE_COMMANDER);
			qaras_agent.setPostId(Ranks.AGENT);
			qaras_agent.setImportance(PersonImportance.LOW);
	
			//if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_inProgress")) {
			//	if (!setPersonMissionRef(qaras_agent, "$sd7s_ref")) return false;
			//}
			
			qaras_agent.setId("sd7s_qaras_agent");
			Global.getSector().getImportantPeople().addPerson(qaras_agent);
		
			Random random = new Random();
			FleetCreatorMission m = new FleetCreatorMission(random);
	
			CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.TRADE_SMUGGLER, qaras);
			if (fleet != null) {
				fleet.setName("Unidentified Ship");
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_FLEE_PLAYER, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
				fleet.getFleetData().addFleetMember("mercury_Standard");
				fleet.getFleetData().ensureHasFlagship();
			
				Misc.makeImportant(fleet, "sd7s");
				qaras.getStarSystem().addEntity(fleet);
				
				fleet.setLocation(
						qaras.getPlanetEntity().getLocation().x,
						qaras.getPlanetEntity().getLocation().y);
				
				m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true);
	
				fleet.clearAssignments();
				fleet.setNoFactionInName(true);
				fleet.setCommander(qaras_agent);
				
				Misc.giveStandardReturnAssignments(fleet, sindria.getPrimaryEntity(), "Fleeing to", true);
				
				fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new SD7SqarasAgentFIDConfig());
				
				fleet.addEventListener(new BaseFleetEventListener() {
					
					@Override
					public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
						if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
							Global.getSector().getMemoryWithoutUpdate().set("$sd7s_qarasAgentDefeated", true);
						}
					}
					
					@Override
					public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
						// defeated in any battle...
						if (battle.wasFleetDefeated(fleet, primaryWinner)) {
							if(primaryWinner.equals(Global.getSector().getPlayerFleet())) {
								Global.getSector().getMemoryWithoutUpdate().set("$sd7s_qarasAgentDefeatedByPC", true);
							}
							fleet.getMemoryWithoutUpdate().set("$defeated", true);
							Global.getSector().getMemoryWithoutUpdate().set("$sd7s_qarasAgentDefeated", true);
						}
					}
				});
			}	
		}
		
		return true;
	}
	
	protected float getResupplyQuantityMult(MarketAPI source, String size) {
		float numShipsMult = 1f;
		if (source != null) {
			numShipsMult = source.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
		}
		float qtyMult = 1f;
		if (size == null) size = SupportFleetSize.SMALL.name();
		if (size.equals(SupportFleetSize.MEDIUM.name())) {
			qtyMult = 2f;
		} else if (size.equals(SupportFleetSize.LARGE.name())) {
			qtyMult = 5f;
		}
		qtyMult *= RESUPPLY_QUANTITY_MULT;
		if (numShipsMult > 1f) {
			qtyMult += numShipsMult * 0.05f;
		}
		return qtyMult;
	}
	
	private void printTaskForceData() {
		Color h = Misc.getHighlightColor();
		
		text.setFontSmallInsignia();
		text.addPara("A %s task force will sometimes include a capital ship, with an appropriate number "
				+ "of cruisers and smaller craft.",
				h, SupportFleetSize.SMALL.getName().toLowerCase(), "capital ship");
		text.addPara("A %s task force is usually constructed around the core of one or two capital ships.",
				h, SupportFleetSize.MEDIUM.getName().toLowerCase(), "one or two capital ships");
		text.addPara("A %s task force typically boasts multiple capital ships, with appropirate support craft.",
				h, SupportFleetSize.LARGE.getName().toLowerCase(), "multiple capital ships");
		text.setFontInsignia();
		text.addPara("The exact composition of the force will vary depending on what craft can be assembled on demand.");
	}

	private void printResupplyFleetData() {
		float iw = 300f; 
		float vw = 50f; 
		float opad = 10f;
		float initPad = 0f;
		
		Color h = Misc.getHighlightColor();
		TooltipMakerAPI info = text.beginTooltip();
		for (SupportFleetSize size : EnumSet.allOf(SupportFleetSize.class)) {
			float qtyMult = getResupplyQuantityMult(null, size.name());
			info.addPara(size.getName() + " " + SupportFleetType.RESUPPLY.getName().toLowerCase() + " (approximate):",
					initPad, h, size.getName());
			info.beginGridFlipped(iw, 1, vw, opad);
			info.addToGrid(0, 0, "supplies", "" + Misc.getWithDGS(Misc.getRounded(RESUPPLY_SUPPLIES * qtyMult)));
			info.addToGrid(0, 1, "fuel", "" + Misc.getWithDGS(Misc.getRounded(RESUPPLY_FUEL * qtyMult)));
			info.addGrid(3f);
			initPad = opad;
		}
		//info.addPara("In addition, the resupply fleet will bring a quantity of volatiles, heavy machinery, and crew.", opad);
		text.addTooltip();
		text.addPara("In addition, the resupply fleet will bring a quantity of volatiles, transplutonics, heavy machinery, and crew.");
	}

	protected boolean isInRangeOfBase(StarSystemAPI system, List<MarketAPI> bases) {
		for (MarketAPI base : bases) {
			boolean highCommand = false;
			for (Industry ind : base.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
					highCommand = true;
					break;
				}
			}
			float checkRange = MILITARY_BASE_SUPPORT_RANGE_LY;
			if (highCommand) checkRange = HIGH_COMMAND_SUPPORT_RANGE_LY;
			
			float dist = Misc.getDistanceLY(system.getLocation(), base.getLocationInHyperspace());
			if (dist > checkRange && Math.round(dist * 10f) <= checkRange * 10f) {
				dist = checkRange;
			}
			if (dist <= checkRange) {
				return true;
			}
		}
		return false;
	}
	
	protected int getFleetETA(SectorEntityToken target, List<MarketAPI> bases) {
		if (bases.isEmpty()) return 0;
		Pair<MarketAPI, Float> from = findClosest(target, bases);
		if (from == null) return 0;
		float days = RouteLocationCalculator.getTravelDays(from.one.getPrimaryEntity(), target);
		if (days < 2) days = 2;
		return (int) days;
	}
	
	protected int getFleetCost(SectorEntityToken target, List<MarketAPI> bases, 
								SupportFleetType type, SupportFleetSize size) {
		if (bases.isEmpty()) return 0;
		Pair<MarketAPI, Float> from = findClosest(target, bases);
		float days = RouteLocationCalculator.getTravelDays(from.one.getPrimaryEntity(), target);
		
		float cost = 0f;
		float mult = 1f;
		switch (type) {
		case TASK_FORCE:
			cost = Global.getSettings().getIntFromArray("taskForceCostBase", size.ordinal());
			if (Misc.isExtended()) {
				mult = Global.getSettings().getFloat("ext_taskForceCostMult");
			}
			break;
		case RESUPPLY:
			cost = Global.getSettings().getIntFromArray("resupplyFleetCostBase", size.ordinal());
			if (Misc.isExtended()) {
				mult = Global.getSettings().getFloat("ext_resupplyFleetCostMult");
			}
			break;
		}
		
		cost *= 1f + ((int)days * 0.01f);
		
		return (int) (cost * mult);
	}

	protected Pair<MarketAPI, Float> findClosest(SectorEntityToken target, List<MarketAPI> bases) {
		float minDist = Float.MAX_VALUE;
		MarketAPI closest = null;
		for (MarketAPI curr : bases) {
			float dist = Misc.getDistanceLY(target.getLocationInHyperspace(), curr.getLocation());
			if (dist < minDist) {
				minDist = dist;
				closest = curr;
			}
		}
		return new Pair<>(closest, minDist);
	}
	
	protected boolean playerHasHighCommand() {
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isHighCommand(MarketAPI market) {
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean playerOnlyHasPatrolHQ() {
		return playerHasPatrolHQ() && !playerHasMilitaryBaseOrHighCommand();
	}
	protected boolean playerHasPatrolHQ() {
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_PATROL)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean playerHasMilitaryBaseOrHighCommand() {
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_MILITARY) ||
						ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected List<MarketAPI> getPlayerMilitaryBases() {
		List<MarketAPI> result = new ArrayList<>();
		boolean allowPatrolHQ = playerOnlyHasPatrolHQ();
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_MILITARY) ||
						ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
					result.add(market);
					continue;
				} else if (allowPatrolHQ && ind.getSpec().hasTag(Industries.TAG_PATROL)) {
					result.add(market);
					continue;
				}
			}
		}
		return result;
	}
	
	public static void addWormholeIntelIfNeeded(SectorEntityToken entity, TextPanelAPI textPanel, boolean deployed) {
		if (WormholeIntel.getWormholeIntel(entity) == null) {
			new WormholeIntel(entity, textPanel, deployed);
		}
	}
	
	public void sendSupportFleet(boolean fastMode) {
		final List<MarketAPI> bases = getPlayerMilitaryBases();
		SectorEntityToken entity = local.getEntity("$fleetDest");
		if (entity == null) return;
		Pair<MarketAPI, Float> from = findClosest(entity, bases);
		if (from == null) return;
		
		SupportFleetType type = Enum.valueOf(SupportFleetType.class, local.getString("$fleetType")); 
		SupportFleetSize size = Enum.valueOf(SupportFleetSize.class, local.getString("$fleetSize"));
		RouteData route = SupportFleetSpawner.addSupportFleetRoute(type, size, entity, from.one, fastMode);
		
		StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
		if (system == null) return;
		
		if (route != null) {
			SupportFleetIntel intel = new SupportFleetIntel(route, system, type, size);
			Global.getSector().getIntelManager().addIntel(intel, false, text);
		}
	}
	
}




















