package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotsAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.tutorial.CampaignTutorialScript;
import com.fs.starfarer.api.impl.campaign.tutorial.SpacerObligation;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 *	$ngcAddOfficer
 *	$ngcSkipTutorial
 *
 */
public class NGCAddStandardStartingScript extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
		final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		data.addScriptBeforeTimePass(new Script() {
			public void run() {
				boolean extendedMode = data.isExtMode();
				if (extendedMode) {
					Global.getSector().getMemoryWithoutUpdate().set("$extendedMode", true);
				}
				
				boolean explorer = memory.getBoolean("$ngcExplorerSelected");
				boolean merc = memory.getBoolean("$ngcMercSelected");
				boolean random = memory.getBoolean("$ngcRandomSelected");
				boolean spacer = memory.getBoolean("$ngcSpacerSelected");
				if (explorer || merc || random) {
					//PirateBaseManager.getInstance().setExtraDays(400f);
					Global.getSector().getMemoryWithoutUpdate().set("$fastStart", true);
					if (explorer) {
						Global.getSector().getMemoryWithoutUpdate().set("$fastStartExplorer", true);
					}
					if (merc) {
						Global.getSector().getMemoryWithoutUpdate().set("$fastStartMerc", true);
					}
					if (random) {
						Global.getSector().getMemoryWithoutUpdate().set("$fastStartRandom", true);
					}
				}
				if (spacer) {
					Global.getSector().getMemoryWithoutUpdate().set("$spacerStart", true);
				}
				
				boolean skipTutorial = memory.getBoolean("$ngcSkipTutorial");
				if (!skipTutorial) {
					Global.getSector().getMemoryWithoutUpdate().set(CampaignTutorialScript.USE_TUTORIAL_RESPAWN, true);
				}
			}
		});
		
		data.addScript(new Script() {
			public void run() {
				CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
				// add crew, supplies, and fuel
				int crew = 0;
				int supplies = 0;
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					crew += Math.ceil(member.getMinCrew() + (member.getMaxCrew() - member.getMinCrew()) * 0.5f);
					supplies += member.getDeploymentCostSupplies() * 4f;
				}
				
				CargoAPI cargo = fleet.getCargo();
				cargo.initPartialsIfNeeded();
				
				cargo.addCrew(crew);
				cargo.addSupplies(10);
				cargo.addCommodity(Commodities.HEAVY_MACHINERY, 10);
				cargo.addFuel(cargo.getMaxFuel() * 0.5f);
				
				boolean extendedMode = data.isExtMode();
				
				boolean addOfficer = memory.getBoolean("$ngcAddOfficer");
				boolean skipTutorial = memory.getBoolean("$ngcSkipTutorial");
				boolean explorer = memory.getBoolean("$ngcExplorerSelected");
				boolean merc = memory.getBoolean("$ngcMercSelected");
				boolean random = memory.getBoolean("$ngcRandomSelected");
				boolean ext1 = memory.getBoolean("$ngcRandomExt1");
				boolean ext2 = memory.getBoolean("$ngcRandomExt2");
				boolean ext3 = memory.getBoolean("$ngcRandomExt3");
				boolean spacer = memory.getBoolean("$ngcSpacerSelected");
				
				if (explorer) {
					cargo.addCommodity(Commodities.HEAVY_MACHINERY, 50);
					cargo.addSupplies(150);
					cargo.removeFuel(cargo.getMaxFuel() * 0.2f);
					adjustStartingHulls(fleet);
				} else if (merc) {
					cargo.addSupplies(100);
					cargo.removeFuel(cargo.getMaxFuel() * 0.2f);
					adjustStartingHulls(fleet);
				} else if (spacer) {
					adjustStartingHulls(fleet);
				} else if (random){
					addOfficer = genRandomStart(fleet);
				} else if (ext1 || ext2 || ext3) {
					addOfficer = genRandomStartExt(fleet, ext1, ext2, ext3);
				}
				
				fleet.getFleetData().ensureHasFlagship();
				
				if (addOfficer) {
					for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
						if (!member.isFlagship()) {
							//PersonAPI officer = OfficerManagerEvent.createOfficer(Global.getSector().getPlayerFaction(), 1, true, SkillPickPreference.NON_CARRIER);
							PersonAPI officer = Global.getSector().getPlayerFaction().createRandomPerson(new Random());
							officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
							//officer.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
							//officer.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
							officer.setRankId(Ranks.SPACE_LIEUTENANT);
							officer.setPostId(Ranks.POST_OFFICER);
							officer.setPersonality(Personalities.STEADY);
							officer.getStats().refreshCharacterStatsEffects();
							
							member.setCaptain(officer);
							fleet.getFleetData().addOfficer(officer);
							break;
						}
					}
				}
				
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					float max = member.getRepairTracker().getMaxCR();
					member.getRepairTracker().setCR(max);
				}
				fleet.getFleetData().setSyncNeeded();
				
				if (skipTutorial) {
					StarSystemAPI system = Global.getSector().getStarSystem("galatia");
					PlanetAPI ancyra = (PlanetAPI) system.getEntityById("ancyra");
					PersonAPI mainContact = TutorialMissionIntel.createMainContact(ancyra);
					PersonAPI jangalaContact = TutorialMissionIntel.getJangalaContact();
					
					TutorialMissionIntel.endGalatiaPortionOfMission(!spacer && !extendedMode, false);
					
					if (spacer) {
						new SpacerObligation();
					}
					
					mainContact.getRelToPlayer().setRel(0.2f);
					jangalaContact.getRelToPlayer().setRel(0.1f);
					Global.getSector().getFaction(Factions.HEGEMONY).getRelToPlayer().setRel(0.15f);
					
					if (spacer) {
						cargo.clear();
						cargo.addCrew(2);
						cargo.addSupplies(15);
						cargo.addFuel(cargo.getMaxFuel() * 1f);
					} else {
						float freeCargo = cargo.getSpaceLeft();
						float addMachinery = Math.min(freeCargo, 15);
						if (random) {
							float r = (float) Math.random();
							if (extendedMode) r *= 0.25f;
							addMachinery = Math.min(freeCargo, 5 + 20 * r);
						}
						if (ext3) addMachinery = 0;
						if (addMachinery > 0) {
							cargo.addCommodity(Commodities.HEAVY_MACHINERY, addMachinery);
							freeCargo -= addMachinery;
						}
						float addSupplies = Math.min(freeCargo, 70);
						if (random) {
							supplies = 0;
							for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
								crew += Math.ceil(member.getMinCrew() + (member.getMaxCrew() - member.getMinCrew()) * 0.5f);
								supplies += member.getDeploymentCostSupplies() * 4f;
							}
							float r = (float) Math.random();
							cargo.removeSupplies(cargo.getSupplies());
							
							freeCargo = cargo.getSpaceLeft();
							addSupplies = Math.min(freeCargo, supplies + 20f + 20f * r);
						}
						if (addSupplies > 0) {
							cargo.addSupplies(addSupplies);
							freeCargo -= addSupplies;
						}
						
						float addFuel = Math.min(cargo.getFreeFuelSpace() - 10f, 70);
						if (random) {
							float r = (float) Math.random();
							cargo.removeFuel(cargo.getFuel());
							addFuel = Math.min(cargo.getFreeFuelSpace(), cargo.getMaxFuel() * (0.25f + 0.25f * r));
						}
						if (ext3) addFuel = cargo.getFreeFuelSpace();
						if (addFuel > 0) {
							cargo.addFuel(addFuel);
						}
						
						
						int maxAdd = cargo.getFreeCrewSpace() - 10;
						if (extendedMode) maxAdd = Math.min(maxAdd, 15);
						float addCrew = Math.min(maxAdd, 100f);
//						addCrew = Math.max(addCrew, fleet.getFleetData().getMinCrew() - cargo.getCrew() + 10);
//						float addCrew = Math.min(cargo.getFreeCrewSpace() - 10f,
//											Math.max(0, (int)((fleet.getFleetData().getMinCrew() - cargo.getCrew()) * 1.25f)));
						if (addCrew > 0) {
							cargo.addCrew((int)addCrew);
						}
						
						if (!random) {
							cargo.getCredits().add(30000);
						}
					}
					
				} else {
					if (random) {
						float addCrew = Math.min(cargo.getFreeCrewSpace() - 10f, 10f);
						if (addCrew > 0) {
							cargo.addCrew((int)addCrew);
						}
					}
					
					fleet.clearAbilities();
					AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
					for (int i = 0; i < 5; i++) {
						slots.setCurrBarIndex(i);
						for (int j = 0; j < 10; j++) {
							AbilitySlotAPI slot = slots.getCurrSlotsCopy().get(j);
							slot.setAbilityId(null);
						}
					}
					
					
					fleet.clearFloatingText();
					fleet.setTransponderOn(false);
					
					
					StarSystemAPI system = Global.getSector().getStarSystem("galatia");
					system.addScript(new CampaignTutorialScript(system));
				}
				
				if (extendedMode) {
					if (cargo.getMaxPersonnel() < fleet.getFleetData().getMinCrew()) {
						fleet.getFleetData().addFleetMember("mercury_Standard");
						
						for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
							String h = member.getHullId();
							if (h.equals("mercury")) {
								ShipVariantAPI v = member.getVariant().clone();
								v.setSource(VariantSource.REFIT);
								v.setHullVariantId(Misc.genUID());
								v.clearWeapons();
								//v.addPermaMod(HullMods.DEGRADED_ENGINES);
								v.addPermaMod(HullMods.DEGRADED_DRIVE_FIELD);
								v.addPermaMod(HullMods.COMP_STORAGE);
								member.setVariant(v, false, false);
								
								float max = member.getRepairTracker().getMaxCR();
								member.getRepairTracker().setCR(max);
								
								break;
							}
						}
						fleet.getFleetData().setSyncNeeded();
						fleet.forceSync();
					}
				}
				
				if (cargo.getCrew() > cargo.getMaxPersonnel()) {
					cargo.removeCrew((int) (cargo.getCrew() - cargo.getMaxPersonnel()));
				}
				if (cargo.getFuel() > cargo.getMaxFuel()) {
					cargo.removeFuel((int) (cargo.getFuel() - cargo.getMaxFuel()));
				}
				if (extendedMode && ext3) { // hyperion
					cargo.removeSupplies(1000);
					cargo.addSupplies(15);
				}
				
				if (extendedMode && !spacer) {
					fleet.getCargo().getCredits().set((int)(10000f + 5000f * (float) Math.random()));
				}
				
				if (extendedMode) {
					setRandomStartLocation(fleet, data);
				}
			}
			
		});
		return true;
	}
	
	public static void setRandomStartLocation(CampaignFleetAPI fleet, CharacterCreationData data) {
		SectorAPI sector = Global.getSector();
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>(StarSystemGenerator.random);
		for (MarketAPI market : sector.getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.getFaction().isHostileTo(Factions.PLAYER)) continue;
			if (market.getStarSystem() == null) continue;
			if (!market.getStarSystem().hasTag(Tags.THEME_CORE_POPULATED)) continue;
			picker.add(market);
		}
		
		MarketAPI pick = picker.pick();
		if (pick != null) {
			sector.setCurrentLocation(pick.getContainingLocation());
			if (fleet.getContainingLocation() != null) {
				fleet.getContainingLocation().removeEntity(fleet);
			}
			pick.getContainingLocation().addEntity(fleet);
			Vector2f loc = Misc.getPointAtRadius(pick.getPrimaryEntity().getLocation(), 250f + (float) StarSystemGenerator.random.nextFloat() * 500f);
			fleet.setLocation(loc.x, loc.y);
		}
		
		if (SystemBountyManager.getInstance() != null) {
			SystemBountyManager.getInstance().setAlwaysPickAMarketIfPossible(true);
			int numBounties = 2 + StarSystemGenerator.random.nextInt(2);
			for (int i = 0; i < numBounties; i++) {
				SystemBountyManager.getInstance().addEvent();
			}
			SystemBountyManager.getInstance().setAlwaysPickAMarketIfPossible(false);
		}
	}
	
	public static boolean genRandomStart(CampaignFleetAPI fleet) {
		Random random = new Random();
		
		fleet.getFleetData().clear();
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		picker.add("shrike_Attack");
		picker.add("mule_Standard");
		picker.add("hammerhead_Balanced");
		picker.add("enforcer_Balanced", 0.5f);
		picker.add("enforcer_Assault", 0.5f);
		picker.add("drover_Starting", 1f);
		
		float qMod = 0f;
		
		float r = random.nextFloat();
		int num = 1;
		boolean addedLarge = false;
		if (r > 0.5f) {
			num = 2;
			qMod -= 0.1f;
			addedLarge = true;
		}
		for (int i = 0; i < num; i++) {
			fleet.getFleetData().addFleetMember(picker.pickAndRemove());
		}
		
		picker.clear();
		picker.add("lasher_Standard");
		picker.add("centurion_Assault");
		picker.add("wolf_Assault", 0.5f);
		picker.add("wolf_CS", 0.5f);
		picker.add("wayfarer_Standard");
		picker.add("hound_hegemony_Standard", 0.5f);
		picker.add("hound_Standard", 0.5f);
		picker.add("cerberus_Standard");
		picker.add("kite_hegemony_Interceptor", 0.5f);
		picker.add("kite_pirates_Raider", 0.5f);
		picker.add("gremlin_Strike");
		picker.add("condor_Attack");
		
		num = 1;
		r = random.nextFloat();
		int extra = (int) (r / 0.34f);
		if (addedLarge) extra = Math.min(extra, 1);
		num += extra;
		qMod -= 0.07f * extra;
		for (int i = 0; i < num; i++) {
			fleet.getFleetData().addFleetMember(picker.pickAndRemove());
		}

		picker.clear();
		picker.add("tarsus_Standard");
		picker.add("buffalo_Standard");
		picker.add("crig_Standard");
		picker.add("mudskipper_Standard");
		fleet.getFleetData().addFleetMember(picker.pickAndRemove());
		
		r = random.nextFloat();
		if (r > 0.75f) {
			fleet.getFleetData().addFleetMember("phaeton_Standard");
		} else if (r > 0.33f) {
			fleet.getFleetData().addFleetMember("dram_Light");
		}
		

		DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
		p.quality = 0.8f + qMod;
		p.mode = ShipPickMode.PRIORITY_THEN_ALL;
		p.persistent = false;
		p.seed = random.nextLong();
		p.timestamp = null;
		
		FleetInflater inflater = Misc.getInflater(fleet, p);
		
		fleet.setFaction(Factions.INDEPENDENT);
		inflater.inflate(fleet);
		fleet.setInflater(null);
		fleet.setFaction(Factions.PLAYER);
		
		fleet.getCargo().addCrew((int)fleet.getFleetData().getMinCrew() - fleet.getCargo().getCrew());
		
		fleet.getCargo().getCredits().set((int)(5000f + 20000f * random.nextFloat()));
		
		fleet.getFleetData().syncIfNeeded();
		fleet.getCargo().sort();
		fleet.getFlagship().setCaptain(Global.getSector().getCharacterData().getPerson());
		fleet.getFlagship().updateStats();
		
		return random.nextFloat() > 0.5f;
	}
	
	public static boolean genRandomStartExt(CampaignFleetAPI fleet, boolean ext1, boolean ext2, boolean ext3) {
		Random random = new Random();
		
		fleet.getFleetData().clear();
		
		boolean addOfficer = false;
		float qMod = 0f;
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		picker.add("shrike_Attack");
		picker.add("scarab_Starting");
		picker.add("tempest_Starting");
		picker.add("shade_Assault");
		
		if (ext1) {
			fleet.getFleetData().addFleetMember(picker.pickAndRemove());
			qMod += 0.4f;
		}
		if (ext2) {
			List<String> worseOptions = new ArrayList<>();
			worseOptions.add("wayfarer_Starting");
			worseOptions.add("vigilance_Starting");
			worseOptions.add("vanguard_Attack");
			
			picker.clear();
			picker.add("wolf_Starting");
			picker.add("lasher_Starting");
			picker.add("centurion_Starting");
			picker.add("brawler_Starting");
			picker.addAll(worseOptions);
			
			String pick = picker.pickAndRemove();
			boolean worseFlagship = worseOptions.contains(pick);
			fleet.getFleetData().addFleetMember(pick);
			
			addOfficer = random.nextFloat() < 0.5f;
			
			// either an officer with a ship from this 2nd set, or no officer but a ship from the original set
			if (addOfficer) {
				picker.clear();
				picker.add("shepherd_Starting");
				picker.add("gremlin_Strike");
				picker.add("kite_Starting");
				picker.add("hound_Starting");
				picker.add("cerberus_Starting");
			}
			fleet.getFleetData().addFleetMember(picker.pickAndRemove());

			if (worseFlagship) {
				picker.clear();
				picker.add("mudskipper_Standard");
				picker.add("hermes_Standard");
				picker.add("mercury_Standard");
				picker.add("mudskipper2_Starting");
				fleet.getFleetData().addFleetMember(picker.pickAndRemove());
			}
		}
		
		if (ext3) {
			fleet.getFleetData().addFleetMember("hyperion_Starting");
			
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				ShipVariantAPI v = member.getVariant().clone();
				v.setSource(VariantSource.REFIT);
				v.setHullVariantId(Misc.genUID());
				member.setVariant(v, false, false);
				
				v.addPermaMod(HullMods.COMP_HULL);
				v.addPermaMod(HullMods.FAULTY_GRID);
				v.addPermaMod(HullMods.FAULTY_AUTO);
				v.addPermaMod(HullMods.DEGRADED_SHIELDS);
				v.addPermaMod(HullMods.DEGRADED_ENGINES);
				v.addPermaMod(HullMods.GLITCHED_SENSORS);
				v.addPermaMod(HullMods.FRAGILE_SUBSYSTEMS);
			}
		}
		
		
		if (!ext3) {
			DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
			p.quality = 0.2f + qMod;
			p.mode = ShipPickMode.PRIORITY_THEN_ALL;
			p.persistent = false;
			p.seed = random.nextLong();
			//p.rProb = 1f;
			p.factionId = Factions.INDEPENDENT;
			if (ext2) {
				p.maxTier = 1;
			}
			p.blockHullmodsWithItemReqs = true;
			p.timestamp = null;
			
			FleetInflater inflater = Misc.getInflater(fleet, p);
			
			fleet.setFaction(Factions.INDEPENDENT);
			inflater.inflate(fleet);
			fleet.setInflater(null);
		}
		
		fleet.setFaction(Factions.PLAYER);
		
		fleet.getCargo().addCrew((int)fleet.getFleetData().getMinCrew() - fleet.getCargo().getCrew());
		
		fleet.getFleetData().syncIfNeeded();
		fleet.getCargo().sort();
		fleet.getFlagship().setCaptain(Global.getSector().getCharacterData().getPerson());
		fleet.getFlagship().updateStats();
		
		return addOfficer;
	}
	
	
	public static void adjustStartingHulls(CampaignFleetAPI fleet) {
		boolean addDmods = true;
		
		//addDmods = false;
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			ShipVariantAPI v = member.getVariant().clone();
			v.setSource(VariantSource.REFIT);
			v.setHullVariantId(Misc.genUID());
			member.setVariant(v, false, false);
			
			String h = member.getHullId();
			
			if (addDmods) {
				if (h.equals("hammerhead")) {
					v.addPermaMod(HullMods.COMP_HULL);
				} else if (h.equals("drover")) {
					v.addPermaMod(HullMods.FAULTY_GRID);
				} else if (h.equals("centurion")) {
					v.addPermaMod(HullMods.COMP_ARMOR);
				} else if (h.equals("lasher")) {
					v.addPermaMod(HullMods.FRAGILE_SUBSYSTEMS);
				}
				
				else if (h.equals("dram")) {
					v.addPermaMod(HullMods.FRAGILE_SUBSYSTEMS);
				}
				
				else if (h.equals("apogee")) {
					//v.addPermaMod(HullMods.FRAGILE_SUBSYSTEMS);
				} else if (h.equals("condor")) {
					v.addPermaMod(HullMods.COMP_ARMOR);
				} else if (h.equals("wayfarer")) {
					v.addPermaMod(HullMods.FRAGILE_SUBSYSTEMS);
				} else if (h.equals("shepherd")) {
					v.addPermaMod(HullMods.FAULTY_GRID);
				}
				
				else if (h.equals("kite_original")) {
					v.addPermaMod(HullMods.COMP_ARMOR);
					v.addPermaMod(HullMods.FAULTY_GRID);
				}
			}
		}
		
		fleet.getFleetData().setSyncNeeded();
	}
	
	

}






