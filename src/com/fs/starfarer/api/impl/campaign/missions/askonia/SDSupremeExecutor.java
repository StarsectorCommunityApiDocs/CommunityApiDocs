package com.fs.starfarer.api.impl.campaign.missions.askonia;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDResolution;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Objectives;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class SDSupremeExecutor extends HubMissionWithSearch {

	
	public static enum Stage {
		MACARIOS_PLOT,
		TALK_TO_CONTACT, // talk to first contact
		TALK_TO_ACHILLES, // talk to Achilles!
		//TALK_TO_HECTOR,
		VISIT_RELAY, // go to the Askonia comm relay to begin the next stage
		WAIT_FOR_OPENING, // alternative to VISIT_RELAY: just let it happen the boring way.
		HECTOR_REVEALED,
		HECTOR_REVEALED2,
		ACHILLES_REVEALED,
		COMPLETED,
	}
	
	protected SectorEntityToken askoniaCommRelay;
	
	// SD
	protected PersonAPI hyder;
	protected PersonAPI caden;
	protected PersonAPI macario;
	
	// PL politics
	protected PersonAPI horus_yaribay;
	protected PersonAPI reynard_hannan;
	protected PersonAPI fortuna_kato;
	
	// Intelligence contacts
	protected PersonAPI inesis;
	protected PersonAPI rao;
	protected PersonAPI siyavong;
	
	// living heirs
	protected PersonAPI hector;
	protected PersonAPI hercules;
	protected PersonAPI achilles;
	
	// possible others
	//protected PersonAPI maeus_moroz;
	//protected PersonAPI standfast;
	//protected PersonAPI imoinu_kato;
	//protected PersonAPI arnaud_iscare;
	protected PersonAPI arroyo;
	
	// places
	protected MarketAPI sindria;
	protected MarketAPI cruor;
	protected MarketAPI volturn;
	protected MarketAPI nortia;
	protected MarketAPI umbra;

	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {

		setPersonOverride(null);
		
		hector = getImportantPerson(People.HECTOR);
		if (hector == null) return false;
		
		achilles = getImportantPerson(People.ACHILLES);
		if (achilles == null) return false;
		
		hercules = getImportantPerson(People.HERCULES);
		if (hercules == null) return false;
		
		hyder = getImportantPerson(People.HYDER);
		if (hyder == null) return false;
	
		caden = getImportantPerson(People.CADEN);
		if (caden == null) return false;
		
		macario = getImportantPerson(People.MACARIO);
		if (macario == null) return false;
		
		horus_yaribay = getImportantPerson(People.HORUS_YARIBAY);
		if (horus_yaribay == null) return false;
		
		reynard_hannan = getImportantPerson(People.REYNARD_HANNAN);
		if (reynard_hannan == null) return false;
		
		fortuna_kato = getImportantPerson(People.FORTUNA_KATO);
		if (fortuna_kato == null) return false;
		
		inesis = getImportantPerson(People.FARO_INESIS);
		if (inesis == null) return false;
		
		arroyo = getImportantPerson(People.ARROYO);
		if (arroyo == null) return false;
		
		rao = getImportantPerson(People.RAO);
		if (rao == null) return false;
		
		siyavong = getImportantPerson(People.SIYAVONG);
		if (siyavong == null) return false;
		
		/*maeus_moroz = getImportantPerson(People.MAEUS_MOROZ);
		if (maeus_moroz == null) return false;
		
		standfast = getImportantPerson(People.STANDFAST);
		if (standfast == null) return false;
		
		imoinu_kato = getImportantPerson(People.IMOINU_KATO);
		if (imoinu_kato == null) return false;

		arnaud_iscare = getImportantPerson(People.GA_RECRUITER);
		if (arnaud_iscare == null) return false;*/
		
		sindria = getMarket("sindria");
		if (sindria == null) return false;
		if (!sindria.getFactionId().equals(Factions.DIKTAT)) return false;
		
		volturn = getMarket("volturn");
		if (volturn == null) return false;
		if (!volturn.getFactionId().equals(Factions.DIKTAT)) return false;
		
		cruor = getMarket("cruor");
		if (cruor == null) return false;
		if (!cruor.getFactionId().equals(Factions.DIKTAT)) return false;
		
		nortia = getMarket("nortia");
		if (nortia == null) return false;
		if (!nortia.getFactionId().equals(Factions.INDEPENDENT)) return false;
		
		umbra = getMarket("umbra");
		if (umbra == null) return false;
		if (!umbra.getFactionId().equals(Factions.PIRATES)) return false;

		
		setStoryMission();
		setStartingStage(Stage.MACARIOS_PLOT);
		addSuccessStages(Stage.COMPLETED);
		
		//setStageTransitionsRepeatable();  no.

		setStageOnGlobalFlag(Stage.TALK_TO_CONTACT, "$sdse_talkToContact");
		setStageOnGlobalFlag(Stage.TALK_TO_ACHILLES, "$sdse_talkToAchilles");
		//setStageOnGlobalFlag(Stage.TALK_TO_HECTOR, "$sdse_talkToHector"); // use VISIT_RELAY for this
		setStageOnGlobalFlag(Stage.VISIT_RELAY, "$sdse_visitRelay");
		setStageOnGlobalFlag(Stage.WAIT_FOR_OPENING, "$sdse_waitForOpening");
		setStageOnGlobalFlag(Stage.HECTOR_REVEALED, "$sdse_hectorDeclaredStageAdvance"); 
		setStageOnGlobalFlag(Stage.HECTOR_REVEALED2, "$sdse_hectorDeclaredStageAdvance2"); 
		setStageOnGlobalFlag(Stage.COMPLETED, "$sdse_achillesDeclaredStageAdvance");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		makeImportant(achilles, "$sdse_talkToAchilles", Stage.TALK_TO_ACHILLES);
		
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$sdse_missionCompleted", true);
		triggerMakeNonStoryCritical(sindria, cruor, volturn, nortia, umbra); // maybe do this post-conflict?
		endTrigger();
		
		//public static Logger log = Logger.getLogger(SDSevenSons.class);
		
		if (!setGlobalReference("$sdse_ref", "$sdse_inProgress")) {
			return false;
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$sdse_stage", getCurrentStage());
		//set("$sdse_goalStringl", getGoalString());
	}

	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		
		if("titleCard".equals(action)) {

			TextPanelAPI text = dialog.getTextPanel();
			text.setFontOrbitronUnnecessarilyLarge();
			Color color = Misc.getBasePlayerColor();
			color = Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();
			text.addPara("SUPREME EXECUTOR", color);
			text.setFontInsignia();
			return true;
		}
		else if ("setCadenPoints".equals(action)) {
			int _points = Global.getSector().getPlayerMemoryWithoutUpdate().getInt("sd_trueBeliever");
			set("$cadenPoints", _points);
			return true;
		}
		else if ("sendDiktatSuppliedMerc".equals(action)) {
			//log.info("SDR_CMD now calling sendDiktatSuppliedMerc()");
			SDResolution.sendDiktatSuppliedMerc();
			return true;
		} 
		else if ("sendCompetingTTExecutive".equals(action)) {
			//log.info("SDR_CMD now calling sendCompetingTTExecutive()");
			SDResolution.sendCompetingTTExecutive();
			return true;
		} 
		else if ("sendLGKillFleet".equals(action)) {
			//log.info("SDR_CMD now calling sendLGKillFleet()");
			SDResolution.sendLGKillFleet();
			return true;
		} 
		else if ("sendLGKillFleet2".equals(action)) {
			//log.info("SDR_CMD now calling sendLGKillFleet()");
			SDResolution.sendLGKillFleet2();
			return true;
		} 
		else if ("startConflict".equals(action)) {
			SDResolution.beginStandoffInAskonia(dialog.getTextPanel(), true);
			return true;
		}
		else if ("fitCheckAchilles".equals(action)) {
			achilles.setPortraitSprite(Global.getSettings().getSpriteName("characters", "achilles_andrada2"));
			return true;
		}
		else if ("fitCheckHector".equals(action)) {
			hector.setPortraitSprite(Global.getSettings().getSpriteName("characters", "hector_andrada2"));
			return true;
		}
		else if ("makeHectorSupremeExecutor".equals(action)) {

			hector.getName().setLast("Andrada");
			hector.setFaction(Factions.DIKTAT);
			hector.setRankId(Ranks.FACTION_LEADER);
			hector.setPostId(Ranks.POST_FACTION_LEADER);
			hector.setImportance(PersonImportance.VERY_HIGH);
					
			return true;
		}
		else if ("makeAchillesSupremeExecutor".equals(action)) {

			achilles.getName().setLast("Andrada");
			achilles.setFaction(Factions.DIKTAT_ACHILLES);
			achilles.setRankId(Ranks.FACTION_LEADER);
			achilles.setPostId(Ranks.POST_FACTION_LEADER);
			achilles.setImportance(PersonImportance.VERY_HIGH);

			return true;
		}
		else if("makeReynardUnimportant".equals(action)) {
			makeUnimportant(reynard_hannan);
			return true;
		}
		else if("updateImportantContact".equals(action)) {
			if (currentStage == Stage.TALK_TO_CONTACT) { 
				
				/*makeUnimportant(rao);
				makeUnimportant(siyavong);
				makeUnimportant(rao);
				makeUnimportant(inesis);
				makeUnimportant(arroyo);
				makeUnimportant(macario);
				makeUnimportant(fortuna_kato);
				makeUnimportant(horus_yaribay);
				makeUnimportant(reynard_hannan);*/
				
				if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstRao")) {
					makeImportant(rao, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
				else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstSiyavong")) {
					makeImportant(siyavong, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
				else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstInesis")) {
					makeImportant(inesis, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
				else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstArroyo")) {
					makeImportant(arroyo, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
				else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstMacario")) {
					makeImportant(macario, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
				else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstFortuna")) {
					makeImportant(fortuna_kato, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
				else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstHorus")) {
					makeImportant(horus_yaribay, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
				else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstReynard")) {
					makeImportant(reynard_hannan, "$sdse_talkToContact", Stage.TALK_TO_CONTACT);
				}
			}
			//else if (currentStage == Stage.VISIT_RELAY) { 
			//	
			//}
			return true;
		}
		else if("ensureAskoniaCommRelay".equals(action)) {
			
			// Make sure Askonia has a comm relay.
			askoniaCommRelay = Global.getSector().getEntityById("sindria_relay");

			// if it doesn't, and there's an empty spot, spawn it.
			// if it doesn't, and the usual spot is filled... replace it, I guess?
			if (askoniaCommRelay == null) {
				MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
				StarSystemAPI askonia = sindria.getStarSystem();
				for (SectorEntityToken objective : askonia.getEntitiesWithTag(Tags.COMM_RELAY)) {
					if (objective.hasTag(Tags.NON_CLICKABLE)) continue;
					if (objective.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
					
					askoniaCommRelay = objective;
					break;
				}
				
				if (askoniaCommRelay == null) {
					SectorEntityToken inner = null;
					float minDist = Float.MAX_VALUE;
					// is there an empty stable location? 
					for (SectorEntityToken objective : askonia.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
						if (objective.hasTag(Tags.NON_CLICKABLE)) continue;
						if (objective.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
						
						float dist = objective.getLocation().length();
						if (dist < minDist) {
							inner = objective;
							minDist = dist;
						}
					}
					
					// if not, is there an objective we can replace?
					if (inner == null) {
						for (SectorEntityToken objective : askonia.getEntitiesWithTag(Tags.OBJECTIVE)) {
							if (objective.hasTag(Tags.NON_CLICKABLE)) continue;
							if (objective.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
							float dist = objective.getLocation().length();
							if (dist < minDist) {
								inner = objective;
								minDist = dist;
							}
						}
						if (inner != null) {
							LocationAPI loc = inner.getContainingLocation();
							SectorEntityToken built = loc.addCustomEntity(null,
									 									 null,
									 Entities.STABLE_LOCATION, // type of object, defined in custom_entities.json
									 Factions.NEUTRAL); // faction
							if (inner.getOrbit() != null) {
								built.setOrbit(inner.getOrbit().makeCopy());
							} else {
								built.setLocation(inner.getLocation().x, inner.getLocation().y);
							}
							loc.removeEntity(inner);
							new Objectives(inner).updateOrbitingEntities(loc, inner, built);
							inner = built;
						}
					}
					
					// if not, let's try to create a stable location
					if (inner == null) {
						List<AddedEntity> added = StarSystemGenerator.addStableLocations(askonia, 1);
						if (!added.isEmpty()) {
							inner = added.get(0).entity;
						}
					}
					
					// if we have a stable location: build a relay
					if (inner != null) {
						askoniaCommRelay = new Objectives(inner).build(Entities.COMM_RELAY_MAKESHIFT, Factions.DIKTAT);
					} else {
						// if we don't, I'm out of ideas
					}
				}
			}
			
			if (askoniaCommRelay == null) {
				// we've tried everything, but no
				return false;
			}
			
			// set ID to match the original
			askoniaCommRelay.setId("sindria_relay");
		
			// then set it as the comm relay to makeImportant!
			makeImportant(askoniaCommRelay, "$sdse_visitRelay", Stage.VISIT_RELAY);
			askoniaCommRelay.addTag(Tags.STORY_CRITICAL);
			
			return true;
		}
		else if(action.equals("makeRelayNotStoryCritical")) {
			askoniaCommRelay = Global.getSector().getEntityById("sindria_relay");
			if(askoniaCommRelay != null) {
				askoniaCommRelay.removeTag(Tags.STORY_CRITICAL);
			}
			return true;
		}

		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		// put reminder to talk to Fortuna Kato first if player said they would and did CONTACT stage. 
		if ((	currentStage == Stage.TALK_TO_ACHILLES || 
				currentStage == Stage.VISIT_RELAY ||
				currentStage == Stage.WAIT_FOR_OPENING ||
				currentStage == Stage.HECTOR_REVEALED || 
				currentStage == Stage.HECTOR_REVEALED2) && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstFortuna") && 
			!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_backingFortuna")) {
			
			if( Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proKato") && 
				!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_toldFortunaKatoWillHelpForCredits")) {
				
				info.addPara("Contact Fortuna Kato to coordinate your support for Achilles Andrada.", opad);
				addStandardMarketDesc("Talk to " + fortuna_kato.getMarket().getOnOrAt(), fortuna_kato.getMarket(), info, opad);
				
				info.addImage(fortuna_kato.getPortraitSprite(), width, 128, opad);
				//info.addImage(fortuna_kato.getFaction().getCrest(), width, 128, opad);
			}
			else if ( 	//Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proKato") &&
						Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_toldFortunaKatoWillHelpForCredits")) {
				
				info.addPara("Contact Fortuna Kato to coordinate your support for Achilles Andrada... for credits.", opad);
				addStandardMarketDesc("Talk to " + fortuna_kato.getMarket().getOnOrAt(), fortuna_kato.getMarket(), info, opad);
				
				info.addImage(fortuna_kato.getPortraitSprite(), width, 128, opad);
				//info.addImage(fortuna_kato.getFaction().getCrest(), width, 128, opad);
			}
		}
		
		if (currentStage == Stage.MACARIOS_PLOT) {
			info.addPara("Dolos Macario has made the first move. You must choose how to respond.", opad);
			//addStandardMarketDesc("Ram gave you coordinates to his safehouse " + volturn.getOnOrAt(), volturn, info, opad);
		}
		else if (currentStage == Stage.WAIT_FOR_OPENING) {
			if( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels") && 
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstArroyo")) {
				info.addPara("Supply the rebels of Askonia and await the heirs' next move.", opad);
				info.addPara("Both Hector and Achilles - and their backers - are positioning themselves to declare as successor to Supreme Executor Philip Andrada.", opad);
			}
			else {

				info.addPara("Both Hector and Achilles - and their backers - are positioning themselves to declare themselves the successor to Supreme Executor Philip Andrada.", opad);
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
					info.addPara("Hector must have supporters within the Sindrian Diktat you can coordinate with - or, perhaps, allies can be bought from the outside.", opad);
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proAchilles")) {
					info.addPara("Achilles might have supporters within the Sindrian Diktat you can coordinate with - or, perhaps, allies can be bought from the outside.", opad);
				}
				else {
					info.addPara("Wait for one of the major players to make a move.", opad);
				}
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_antiMacario")) {
					info.addPara("As for Macario, he must have enemies across the Persean Sector. Or perhaps a counter-intelligence expert could be paid to get to him...", opad);
				}
			}
		}
		else if (currentStage == Stage.TALK_TO_CONTACT) {
			
			// if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaHegemony")
			if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstRao")) {
				info.addPara("Contact Orcus Rao about how to take down Dolos Macario.", opad);
				addStandardMarketDesc("Go to " + rao.getMarket().getOnOrAt(), rao.getMarket(), info, opad);
				info.addImage(rao.getPortraitSprite(), width, 128, opad);
				info.addImage(rao.getFaction().getCrest(), width, 128, opad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstSiyavong")) {
				info.addPara("Contact Finlay Siyavong about how to take down Dolos Macario.", opad);
				addStandardMarketDesc("Go to " + siyavong.getMarket().getOnOrAt(), siyavong.getMarket(), info, opad);
				info.addImage(siyavong.getPortraitSprite(), width, 128, opad);
				info.addImage(siyavong.getFaction().getCrest(), width, 128, opad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstInesis")) {
				info.addPara("Contact Faro Inesis and figure out how to take down Dolos Macario.", opad);
				addStandardMarketDesc("Go to " + inesis.getMarket().getOnOrAt(), inesis.getMarket(), info, opad);
				info.addImage(inesis.getPortraitSprite(), width, 128, opad);
				info.addImage(inesis.getFaction().getCrest(), width, 128, opad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstArroyo")) {
				info.addPara("Contact Rayan Arroyo and figure out how to take down Dolos Macario.", opad);
				addStandardMarketDesc("Go to " + arroyo.getMarket().getOnOrAt(), arroyo.getMarket(), info, opad);
				info.addImage(arroyo.getPortraitSprite(), width, 128, opad);
				info.addImage(arroyo.getFaction().getCrest(), width, 128, opad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstFortuna")) {
				info.addPara("Contact Fortuna Kato about how to take down Dolos Macario.", opad); // TODO is this PC's goal?
				addStandardMarketDesc("Go to " + fortuna_kato.getMarket().getOnOrAt(), fortuna_kato.getMarket(), info, opad);
				info.addImage(fortuna_kato.getPortraitSprite(), width, 128, opad);
				info.addImage(fortuna_kato.getFaction().getCrest(), width, 128, opad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstHorus")) {
				info.addPara("Contact Horus Yaribay about how to take down Dolos Macario.", opad); // TODO is this PC's goal?
				addStandardMarketDesc("Go to " + horus_yaribay.getMarket().getOnOrAt(), horus_yaribay.getMarket(), info, opad);
				info.addImage(horus_yaribay.getPortraitSprite(), width, 128, opad);
				info.addImage(horus_yaribay.getFaction().getCrest(), width, 128, opad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstReynard")) {
				info.addPara("Contact Reynard Hannan about how to take down Dolos Macario.", opad); // TODO is this PC's goal?
				addStandardMarketDesc("Go to " + reynard_hannan.getMarket().getOnOrAt(), reynard_hannan.getMarket(), info, opad);
				info.addImage(reynard_hannan.getPortraitSprite(), width, 128, opad);
				info.addImage(reynard_hannan.getFaction().getCrest(), width, 128, opad);
			}
			else {
				info.addPara("Talk to your contact.", opad);
			}
			
			if( Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proKato") && 
				!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_toldFortunaKatoWillHelpForCredits") &&
				!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstFortuna") &&
				!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_backingFortuna")) {
				
				info.addPara("You also told Fortuna Kato you would help her support Achilles Andrada.", opad);
				addStandardMarketDesc("Contact " + fortuna_kato.getMarket().getOnOrAt(), fortuna_kato.getMarket(), info, opad);
			}
			else if ( 	Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proKato") &&
						Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_toldFortunaKatoWillHelpForCredits") &&
						!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstFortuna") &&
						!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_backingFortuna")) {
				
				info.addPara("You also told Fortuna Kato you would help her support Achilles Andrada... for credits.", opad);
				addStandardMarketDesc("Contact " + fortuna_kato.getMarket().getOnOrAt(), fortuna_kato.getMarket(), info, opad);
			}
		}
		else if (currentStage == Stage.TALK_TO_ACHILLES) {
			 if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAchilles")) {
				 info.addPara("Contact Achilles Andrada and tell him to expect covert support from Inesis.", opad);
			 }
			 else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistAchilles")) {
				 info.addPara("Contact Achilles Andrada and tell him to expect covert support from Arroyo.", opad);
			 }
			 else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkToAchillesToGiveSupport")) { 
				 info.addPara("Contact Achilles Andrada and tell him you will give him your support.", opad);
			 }
			 else {
				 info.addPara("Contact Achilles Andrada.", opad);
			 }
			 addStandardMarketDesc("Go to " + achilles.getMarket().getOnOrAt(), achilles.getMarket(), info, opad);
		}
		else if (currentStage == Stage.VISIT_RELAY) {
			if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario")) {
				 if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkHyder")) {
					 info.addPara("Upload Inesis' software attack into the Askonia comms relay - then contact Oxana Hyder.",opad);
					 info.addImage(hyder.getPortraitSprite(), width, 128, opad);
					 //info.addImage(hyder.getFaction().getCrest(), width, 128, opad);
				 }
				 else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkCaden")) {
					 info.addPara("Upload Inesis' software attack into the Askonia comms relay - then contact Horatio Caden.", opad);
					 info.addImage(caden.getPortraitSprite(), width, 128, opad);
					 info.addImage(caden.getFaction().getCrest(), width, 128, opad);
				 }
				 else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkHector")) {
					 info.addPara("Upload Inesis' software attack into the Askonia comms relay - then contact Hector Andrada.", opad);
					 info.addImage(hector.getPortraitSprite(), width, 128, opad);
					 info.addImage(hector.getFaction().getCrest(), width, 128, opad);
				 }
				 else {
					 info.addPara("Hack into the Askonia comms relay and upload Inesis' software attack.", opad);
				 }
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistHector")) {
				 
				 if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkMacario")) { 
					 info.addPara("Hack into the Askonia comms relay and contact Macario. Tell him about the support you've secured from Arroyo.", opad);
				 }
				 else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkHector")) { 
					 info.addPara("Hack into the Askonia comms relay and contact Hector. Tell him about the support you've secured from Arroyo.", opad);
					 
				 }
			 }
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_visitRelayForRao")) {
				info.addPara("Hack into the comms relay in the Askonia system and upload Rao's software attack.", opad);
				info.addPara("Once that's done, use the relay to contact Oxana Hyder.", opad);
				info.addImage(hyder.getPortraitSprite(), width, 128, opad);
				info.addImage(hyder.getFaction().getCrest(), width, 128, opad);
				
				
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstHyder")) {
				info.addPara("Hack into the Askonia comms relay in order to speak to Oxana Hyder on a secure channel.",opad);
				info.addImage(hyder.getPortraitSprite(), width, 128, opad);
				info.addImage(hyder.getFaction().getCrest(), width, 128, opad);
			
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstMacario")) {
				info.addPara("Hack into the Askonia comms relay in order to speak to Dolos Macario on a secure channel.",opad);
				info.addImage(macario.getPortraitSprite(), width, 128, opad);
				info.addImage(macario.getFaction().getCrest(), width, 128, opad);
			
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstMacarioLoyal")) {
					info.addPara("Convince Macario that he made a mistake and that you are key to securing the future of the Sindrian Diktat.",opad);
					if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
						info.addPara("You will do your part to ensure that Hector Andrada reigns as Supreme Executor.",opad);
					}
				}
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_visitRelayForInesis") && 
						Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector") &&
						Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
				info.addPara("Go to the comms relay in the Askonia system and hack it in order to contact Hector.", opad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstCaden")) {
				info.addPara("Hack into the Askonia comms relay then open contact with Horatio Caden.", opad);
					 info.addImage(caden.getPortraitSprite(), width, 128, opad);
					 info.addImage(caden.getFaction().getCrest(), width, 128, opad);
			}
			else {
				 info.addPara("Go to the comms relay in the Askonia system.", opad);
			}
			
			//if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkToHector")) { 
			//	info.addPara("Go to the comms relay in the Askonia system and open contact with Hector Andrada.", opad);
			//}
		}
		else if (currentStage == Stage.HECTOR_REVEALED) {
			info.addPara("Hector has declared himself Supreme Executor with the support of Macario and Caden.", opad);
		}
		else if (currentStage == Stage.HECTOR_REVEALED2) {
			info.addPara("Hector has declared himself Supreme Executor with the support of Macario and Caden.", opad);
		}
		
		
		
		// misc and secondary objectives to note!
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_agreedToHelpRao") &&
				!( Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$helpedVolturnSDC") && 
				Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$didIscareACAintro") && 
				Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$helpedUmbraARC"))) {
			// so this remains until player contacts all three rebels.
			info.addPara("Rao has requested that you open contact with rebel leaders on Volturn, Nortia, and Umbra.", opad);
		}
		
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkOrcusSecondary") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_deniedRaoOffer") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_agreedToHelpRao")) 
		{
			info.addPara("Talk to Orcus Rao on Ragnar Complex in the Valhalla system.", opad);
		}
		
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkInesisSecondary") && 
			!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_inesisDidSecondaryStart")) 
		{
			info.addPara("Talk to Faro Inesis on Achaman Station in the Magec system.", opad);
		}
		
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkArroyoSecondary") && 
			!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_arroyoDidSecondaryStart")) 
		{
			info.addPara("Talk to Rayan Arroyo on Achaman Station in the Magec system.", opad);
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_horusSetUpMeetingACA") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedCharterists")) {
			info.addPara("Horus Yaribay as requested that you assist the Charterists - the ACA - on Nortia. A meeting has been set up in a spacer bar.", opad);
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_reynardDoneFirstTalk") && 
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_proReynard") && 
				!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedACAfinished")) {
			info.addPara("Reynard Hannan asked you to funnel supplies to Imoinu Kato and the ARC rebels of Umbra, in the Askonia system.", opad);
		}
		
		if( Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_sdTalkViaFleets") && 
			Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector") ) {
			info.addPara("Dolos Macario has taken control of the Sindrian Diktat comms network. To speak to leaders such as Oxana Hyder, "
					+ "Horacio Caden, and even Hector Andrada himself, you will have to contact their fleets directly.", opad);
		}
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector")) {
			info.addPara("You have secured Faro Inesis' support for Hector.", opad);
		} 
		
		if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistHector")) {
			info.addPara("You have secured Rayan Arroyo's support for Hector.", opad);
		}
		
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_offeredToAskRaoForHyderDefection") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gaveHyderRaoFinalOffer") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotRaoHyderDefectionAgreement")) {
			info.addPara("Ask Orcus Rao about offering Oxana Hyder a deal if she turns herself over to the Hegemony.", opad);
		}
		
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_offeredToAskRaoForHyderDefection") && 
			!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gaveHyderRaoFinalOffer") && 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotRaoHyderDefectionAgreement")) {
			info.addPara("Bring Orcus Rao's offer to Oxana Hyder.", opad);
		}

	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		
		
		if (currentStage == Stage.MACARIOS_PLOT) {
			info.addPara("Dolos Macario has made the first move.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.WAIT_FOR_OPENING) {
			if( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels") && 
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstArroyo")) {
				info.addPara("Supply the rebels of Askonia and await the Andradas' next move.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_horusSetUpMeetingACA")) {
				info.addPara("Speak with Siyavong, supply the ACA rebels for Horus, and wait for Achilles to make his move.", tc, pad);
			}
			else if ( 	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_proReynard") &&
						Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_reynardDoneFirstTalk")) {
				info.addPara("Supply Imoinu Kato of the ARC at Reynard Hanna's request then wait for the Andrada heirs' next move.", tc, pad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector")){
				info.addPara("Use the Sindria Relay to tell Hector Andrada about securing support for his cause - and gather other allies, if you can.", tc, pad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkArroyoSecondary") && 
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotArroyoToHelp")) {
				info.addPara("Talk to Rayan Arroyo on Eochu Bres in the Hybrasil system.", tc, pad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkInesisSecondary") && 
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisHelp")) {
				info.addPara("Talk to Faro Inesis on Achaman Enterprise Station in the Magec system.", tc, pad);
			}  
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkOrcusSecondary") && 
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$global.sdse_deniedRaoOffer") && 
					!Global.getSector().getMemoryWithoutUpdate().getBoolean("$global.sdse_agreedToHelpRao") ) {
				info.addPara("Talk to Orcus Rao on Ragnar Complex in the Valhalla system.", tc, pad);
			}
			else {
				//info.addPara("Wait for someone to make their move...", tc, pad);
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
					info.addPara("Find Hector's allies in the Sindrian Diktat ...or buy them elsewhere.", tc, pad);
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proAchilles")) {
					info.addPara("Find Achilles' allies in the Sindrian Diktat, or elsewhere...", tc, pad);
				}
				else {
					info.addPara("Wait for someone to make their move...", tc, pad);
				}
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_antiMacario")) {
					info.addPara("Macario has enemies everywhere. You just need to find one.", tc, pad);
				}
			}
			
			return true;
		}
		else if (currentStage == Stage.TALK_TO_CONTACT) {
			
			if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstRao")) {
				info.addPara("Contact Orcus Rao on Ragnar Station, in the Valhalla system.", tc, pad);
				
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstSiyavong")) {
				info.addPara("Contact Finlay Siyavong on Kazeron, in the Thule system.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstReynard")) {
				info.addPara("Contact Reynard Hannan on Kazeron, in the Thule system.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstHorus")) {
				info.addPara("Contact Horus Yaribay on Kazeron, in the Thule system.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstFortuna")) {
				info.addPara("Contact Fortuna Kato on Kazeron, in the Thule system.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstInesis")) {
				info.addPara("Contact Faro Inesis on Achaman Enterprise Station, in the Magec system.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstArroyo")) {
				info.addPara("Contact Rayan Arroyo on Eochu Bres, in the Hybrasil system.", pad);
			}
			else {
				info.addPara("Talk to your contact about how to take down Macario.", tc, pad);
			}
					
			return true;
		}
		else if (currentStage == Stage.TALK_TO_ACHILLES) { 
			
			info.addPara("Contact Achilles Andrada on Yesod, in the Zagan system.", tc, pad);
		}
		else if (currentStage == Stage.VISIT_RELAY) {
			//info.addPara("Visit the comms relay in the Askonia system.", tc, pad);
			
			if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstHyder")) {
				info.addPara("Hack the comms relay in the Askonia system to open a secure channel to Oxana Hyder.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstCaden")) {
				info.addPara("Hack the comms relay in the Askonia system to open a secure channel to Horatio Caden.", tc, pad);
			}
			else if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstMacario")) {
				info.addPara("Hack the comms relay in the Askonia system to open a secure channel to Dolos Macario.", tc, pad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario")) {
				 if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkHyder")) {
					 info.addPara("Hack into the Askonia comms relay. Upload Inesis' attack - then contact Hyder.", tc, pad);
				 }
				 else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$global.sdse_talkCaden")) {
					 info.addPara("Hack into the Askonia comms relay. Upload Inesis' attack - then contact Caden.", tc, pad);
				 }
				 else {
					 info.addPara("Hack into the comms relay in the Askonia system and upload Inesis' attack.", tc, pad);
				 }
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_visitRelayForRao")) {
				info.addPara("Hack into the comms relay in the Askonia system and upload Rao's software attack.", tc, pad);
				// TODO
				info.addPara("Then use the relay to contact Oxana Hyder.", tc, pad);
			}
			else if (	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_visitRelayForInesis") && 
						Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector") &&
						Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
				info.addPara("Hack into the comms relay in the Askonia system and contact Hector.", tc, pad);
			}
			else {
				info.addPara("Hack into the comms relay in the Askonia system.", tc, pad);
			}
			
			return true;
		}
		else if (currentStage == Stage.HECTOR_REVEALED) {
			info.addPara("Hector 'Palatin' Andrada declared himself Supreme Executor.", tc, pad);
			info.addPara("Achilles is sure to make his own declaration soon...", tc, pad);	
		}
		else if (currentStage == Stage.HECTOR_REVEALED2) {
			info.addPara("Achilles 'Kalian' Andrada, backed by Fortuna Kato, is sure to make his own declaration soon...", tc, pad);
		}
		else if (currentStage == Stage.ACHILLES_REVEALED) {
			info.addPara("Begun the Clone Wars have.", tc, pad);
		}
		
		/*else if (currentStage == Stage.HECTOR_REVEALED) {
			info.addPara("HECTOR_REVEALED stage.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.FIND_THEM) { 
			info.addPara("Find the seven sons of Philip Andrada.", tc, pad);
			
			return true;
		}*/

		return false;
	}

	@Override
	public String getBaseName() {
		return "Heir to Andrada";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}
