package com.fs.starfarer.api.impl.campaign.missions.askonia;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.DismissDialogDelegate;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.BaseFleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.intel.misc.SevenSonsIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager.RemnantFleetInteractionConfigGen;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SDSevenSons extends HubMissionWithSearch {

	//public static float EXTACT_AGENT_RAID_DIFFICULTY = 200f;
	
	public static enum Stage {
		CRACK_FILES,
		
			TALK_GARGOYLE,
			TALK_ORCUS,
			//TALK_DAUD, // because it'll shunt you to Orcus Rao
			TALK_ARROYO,
			TALK_HORUS_YARIBAY, // to siyavong REMOVE 
			TALK_REYNARD_HANNAN, // to siyavong REMOVE
			TALK_SIYAVONG,
			TALK_INESIS,
			//TALK_CONTACT,
			TALK_TO_AI,
			
		FIND_THEM,
		COMPLETED,
	}
	
	protected PersonAPI gargoyle;
	protected PersonAPI rao;
	protected PersonAPI arroyo;
	protected PersonAPI horus_yaribay;
	protected PersonAPI reynard_hannan;
	protected PersonAPI hyder;
	protected PersonAPI caden;
	protected PersonAPI macario;
	protected PersonAPI cydonia;
	protected PersonAPI ko_jove;
	protected PersonAPI inesis;
	protected PersonAPI siyavong;
	protected PersonAPI zgr;

	protected PersonAPI hector;
	protected PersonAPI hercules;
	protected PersonAPI achilles;
	
	protected PersonAPI gallu_blanc;
	protected PersonAPI maeus_moroz;
	protected PersonAPI qaras_contact;
	protected PersonAPI qaras_agent;
	
	protected boolean useSons = false;

	public static String FOUND_PERSEUS = "$sd7s_foundPerseus";
	public static String FOUND_HERCULES = "$sd7s_foundHercules";
	public static String FOUND_JASON = "$sd7s_foundJason";
	public static String FOUND_HECTOR = "$sd7s_foundHector";
	public static String FOUND_ACHILLES = "$sd7s_foundAchilles";
	public static String FOUND_LEONIDAS = "$sd7s_foundLeonidas";
	public static String FOUND_PERICLES = "$sd7s_foundPericles";
	
	public static String DEFEATED_QARAS_AGENT_FLEET = "$sd7s_qarasAgentDefeated";
	
	protected Color crasisColor;
	protected Color aureolinColor;
	
	protected MarketAPI nomios;
	protected MarketAPI yesod;
	protected MarketAPI orthrus;
	protected MarketAPI qaras;
	protected MarketAPI sindria;
	protected SectorEntityToken elysium;
	protected SectorEntityToken irkalla;
	protected SectorEntityToken galatia_academy;
	
	protected int sonsFoundCount = 0;
	protected int numberOfSons = 7;
	protected Random random = new Random();
	
	protected int xpRewardBase = 14000;
	protected int xpRewardIncrease = 3000;
	protected int num_rewards_given = 0;
	
	protected MarketAPI aiColony;
	
	public static Logger log = Logger.getLogger(SDSevenSons.class);

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$sd7s_ref", "$sd7s_inProgress")) {
			return false;
		}
		
		setPersonOverride(null);
		
		gargoyle = getImportantPerson(People.GARGOYLE);
		if (gargoyle == null) return false;
		
		rao = getImportantPerson(People.RAO);
		if (rao == null) return false;
		
		inesis = getImportantPerson(People.FARO_INESIS);
		if (inesis == null) return false;
		
		arroyo = getImportantPerson(People.ARROYO);
		if (arroyo == null) return false;
		
		horus_yaribay = getImportantPerson(People.HORUS_YARIBAY);
		if (horus_yaribay == null) return false;
		
		reynard_hannan = getImportantPerson(People.REYNARD_HANNAN);
		if (reynard_hannan == null) return false;
		
		siyavong = getImportantPerson(People.SIYAVONG);
		if (siyavong == null) return false;
		
		cydonia = getImportantPerson(People.CYDONIA);
		if (cydonia == null) return false;
		
		hyder = getImportantPerson(People.HYDER);
		if (hyder == null) return false;
		
		caden = getImportantPerson(People.CADEN);
		if (caden == null) return false;
		
		macario = getImportantPerson(People.MACARIO);
		if (macario == null) return false;
		
		ko_jove = getImportantPerson(People.KO_JOVE);
		if (ko_jove == null) return false;
		
		nomios = getMarket("nomios");
		if (nomios == null) return false;
		if (!nomios.getFactionId().equals("independent")) return false;
		
		sindria = getMarket("sindria");
		if (sindria == null) return false;
		if (!sindria.getFactionId().equals("sindrian_diktat")) return false;
		
		gallu_blanc = getImportantPerson(People.GALLU_BLANC);
		if(gallu_blanc == null) return false;
		
		yesod = getMarket("yesod");
		if (yesod == null) return false;
		if (!yesod.getFactionId().equals("persean")) {
			log.warn("SDSevenSons failed to find Yesod.");
			return false;
		} 
		
		orthrus = getMarket("orthrus");
		if (orthrus == null) return false;
		if (!orthrus.getFactionId().equals("independent")) {
			log.warn("SDSevenSons failed to find Orthrus.");
			return false;
		}
		
		galatia_academy = Global.getSector().getStarSystem("galatia").getEntityById("station_galatia_academy");
		if (galatia_academy == null) {
			return false;
		}
		
		qaras = getMarket("qaras");
		if (qaras == null) return false;

		addLargeSons();
		
		maeus_moroz = getImportantPerson(People.MAEUS_MOROZ);
		if(maeus_moroz == null) return false;
		
		
		// set a fake Cydonia date if none exists
		//"Dec 11, c205"
		//"205.12.11"
		String cydoniaDate = (String) Global.getSector().getMemoryWithoutUpdate().get("$sd7s_cydoniaDate");
		if(cydoniaDate == null) {
			Global.getSector().getMemoryWithoutUpdate().set("$sd7s_cydoniaDate", "Dec 11, c205");
			Global.getSector().getMemoryWithoutUpdate().set("$sd7s_cydoniaDate2", "205.12.11");
			//Global.getSector().getMemoryWithoutUpdate().set("$sd7s_cydoniaDate", Global.getSector().getClock().getDateString());  //getShortDate());
			//Global.getSector().getMemoryWithoutUpdate().set("$sd7s_cydoniaDate2", Global.getSector().getClock().getShortDate());
		}
		
		aureolinColor = new Color(210,175,170,255);
		crasisColor = new Color(150,165,190,255);
		
		/*
		volturn = getMarket("volturn");
		if (volturn == null) return false;
		if (!volturn.getFactionId().equals("sindrian_diktat")) return false;
		
		sindria = getMarket("sindria");
		if (sindria == null) return false;
		if (!sindria.getFactionId().equals("sindrian_diktat")) return false;
		*/
		
		
		hector = getImportantPerson(People.HECTOR);
		hercules = getImportantPerson(People.HERCULES);
		achilles = getImportantPerson(People.ACHILLES);
			
		setStoryMission();
		setStartingStage(Stage.CRACK_FILES);
		addSuccessStages(Stage.COMPLETED);
		
		setStageOnGlobalFlag(Stage.TALK_GARGOYLE, "$sd7s_decryptViaGargoyle");
		makeImportant(gargoyle, "$sd7s_decrypt", Stage.TALK_GARGOYLE);
		
		setStageOnGlobalFlag(Stage.TALK_ARROYO, "$sd7s_decryptViaArroyo");
		makeImportant(arroyo, "$sd7s_decrypt", Stage.TALK_ARROYO);
		
		setStageOnGlobalFlag(Stage.TALK_INESIS, "$sd7s_decryptViaInesis");
		makeImportant(inesis, "$sd7s_decrypt", Stage.TALK_INESIS);
		
		setStageOnGlobalFlag(Stage.TALK_SIYAVONG, "$sd7s_decryptViaSiyavong");
		makeImportant(siyavong, "$sd7s_decrypt", Stage.TALK_SIYAVONG);
		
		setStageOnGlobalFlag(Stage.TALK_ORCUS, "$sd7s_decryptViaOrcusRao");
		makeImportant(rao, "$sd7s_decrypt", Stage.TALK_ORCUS);
		
		setStageOnGlobalFlag(Stage.TALK_TO_AI, "$sd7s_decryptViaOwnAI");
		
		setStageOnGlobalFlag(Stage.FIND_THEM, "$sd7s_decryptedAndradaFiles");
		
		makeImportant(galatia_academy, "$sd7s_findHercules", Stage.FIND_THEM);
		makeImportant(nomios.getPlanetEntity(), "$sd7s_findPerseus", Stage.FIND_THEM);
		makeImportant(yesod.getPlanetEntity(), "$sd7s_findAchilles", Stage.FIND_THEM);
		makeImportant(qaras.getPlanetEntity(), "$sd7s_findJason", Stage.FIND_THEM);
		makeImportant(orthrus.getPlanetEntity(), "$sd7s_findPericles", Stage.FIND_THEM);
		// and the other two are asteroids spawn in-situ
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$sd7s_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();

		
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$sd7s_missionCompleted", true);
		triggerMakeNonStoryCritical(orthrus, yesod, qaras, nomios);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$sd7s_stage", getCurrentStage());
		set("$sd7s_crasisColor", new Color(140,170,225,255)); // Color(140,225,170,255)); 
		set("$sd7s_aureolinColor", new Color(230,180,160,255)); // Color(230,220,160,255)); 
		set("$sd7s_sonsFoundCount", getSonsFoundCount());
		
		aiColony = findColonyWithAIAdmin();
		if(aiColony == null) {
			set("$sd7s_playerHasAIAdmin", false);
		}
		else {
			set("$sd7s_playerHasAIAdmin", true);
			set("$sd7s_aiColonyName", aiColony.getName());
			set("$sd7s_aiColonySystemName", aiColony.getStarSystem().getName());
		}
	}
	
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == SevenSonsIntel.BUTTON_SHOW_SEVEN_SONS) {
			SevenSonsIntel.toggleSonsList(this, ui);
			return;
		}
		else if (buttonId == SevenSonsIntel.BUTTON_SONS_INTEL_DIALOG) {
			// launch dialog here.
			RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("SevenSonsAndradaFileBegin");
			Global.getSector().getCampaignUI().showInteractionDialogFromCargo(plugin, 
				Global.getSector().getPlayerFleet(), new DismissDialogDelegate() {
					@Override
					public void dialogDismissed() {}
				});
			return;
		}
		else if (buttonId == SevenSonsIntel.BUTTON_SONS_DECRYPT_DIALOG) {
			// launch dialog here.
			RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("SevenSonsAndradaDecryptBegin");
			Global.getSector().getCampaignUI().showInteractionDialogFromCargo(plugin, 
				Global.getSector().getPlayerFleet(), new DismissDialogDelegate() {
					@Override
					public void dialogDismissed() {
						ui.updateUIForItem(SDSevenSons.this);
						
					}
				});
			return;
		}
		super.buttonPressConfirmed(buttonId, ui);
	}

	@Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction(Factions.DIKTAT);
    } 
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map, Object currentStage) {
		if (currentStage == Stage.FIND_THEM) {
			return null;
		}
		else 
			return super.getMapLocation(map, currentStage);
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		
		if("setupSons".equals(action)) 
		{
			addLocations();
			return true;
		}
		else if ("giveFoundXP".equals(action)) 
		{
			num_rewards_given++;
			long xp_to_give = (long)(num_rewards_given * xpRewardIncrease + xpRewardBase);
			Global.getSector().getPlayerStats().addXP(xp_to_give, dialog.getTextPanel());
			return true;
		}
		else if ("makeAIColonyImportant".equals(action)) {
			makeImportant(aiColony, "$sd7s_decrypt", Stage.TALK_TO_AI);
			return true;
		}
		else if ("giveLastFoundXP".equals(action))
		{
			num_rewards_given++;
			long xp_to_give = (long)(num_rewards_given * xpRewardIncrease + xpRewardBase);
			
			if (num_rewards_given < sonsFoundCount)
			{
				for (int i = num_rewards_given; i < sonsFoundCount; i++)
				{
					xp_to_give += (long)(i * xpRewardIncrease + xpRewardBase);
				}
			}
			
			Global.getSector().getPlayerStats().addXP(xp_to_give, dialog.getTextPanel());
			return true;
		}
		else if ("checkSonsFound".equals(action)) {
			
			getSonsFoundCount();
			//System.out.print("checkShrinesVisited = " + Integer.toString(visited_shrines));
			
			if( sonsFoundCount >= numberOfSons) {
				Global.getSector().getMemoryWithoutUpdate().set("$sd7s_foundAllSons", true);
			}
			return true;
		}
		//else if ("makeCydoniaImportant".equals(action)) {
		//	makeImportant(cydonia, "$perseusInfo"); // "$sd7s_findPerseus", Stage.FIND_THEM);
		//	return true;
		//}
		else if ("damageFleet".equals(action)) {
			damageFleet(4f,2);
			return true;
		}
		else if ("damageFleetMore".equals(action)) {
			damageFleet(8f,4);
			return true;
		}
		else if ("spawnIrkallaMerc".equals(action)) {
			spawnIrkallaMercFleet();
			return true;
		}
		else if ("spawnElysiumMerc".equals(action)) {
			spawnElysiumMercFleet();
			return true;
		}
		else if ("spawnMorozFleet".equals(action)) {
			spawnMorozFleet(random, false);
			return true;
		}
		else if ("changeTitle".equals(action)) {
			useSons = true;
			return true;
		}
		else if ("saveTheDate".equals(action)) {
			
			//System.out.println("SD7S doing saveTheData");
			Global.getSector().getMemoryWithoutUpdate().set("$sd7s_cydoniaDate", Global.getSector().getClock().getDateString());  //getShortDate());
			Global.getSector().getMemoryWithoutUpdate().set("$sd7s_cydoniaDate2", Global.getSector().getClock().getShortDate());
			return true;
		}
		else if ("shootEm".equals(action)) {
			Global.getSoundPlayer().playSound("storyevent_diktat_execution", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			return true;
		}
		else if ("goBoom".equals(action)) {
			
			Global.getSoundPlayer().playSound("explosion_from_damage", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			Global.getSoundPlayer().playSound("collision_asteroids", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			Global.getSoundPlayer().playSound("collision_ships", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			return true;
		}
		else if ("addIntelImageIrkalla".equals(action)) {
			TextPanelAPI text = dialog.getTextPanel();
			text.addImage("intel", "sd7s_irkalla");
			return true;
		}
		else if ("addIntelImageElysium".equals(action)) {
			TextPanelAPI text = dialog.getTextPanel();
			text.addImage("intel", "sd7s_elysium");
			return true;
		}
		//else if ("createQarasAgentAndFleet".equals(action)) {
		//	return createQarasAgentAndFleet();
		//}
		else if ("annoyQaras".equals(action)) {
			if (dialog == null) return false;
		
			WeightedRandomPicker<PersonAPI> picker = new WeightedRandomPicker<PersonAPI>();
			for (PersonDataAPI pd : Global.getSector().getImportantPeople().getPeopleCopy()) {
				PersonAPI person = pd.getPerson();
				if(!person.getId().equals(People.KO_JOVE)) 
					if (	Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
							Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
							Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
							Ranks.POST_STATION_COMMANDER.equals(person.getPostId())) {
						
						picker.add(person);
					}
			}
			
			int num = 4; //Misc.random.nextInt(2) + 1;
			for (int i = 0; i < num && !picker.isEmpty(); i++) {
				PersonAPI person = picker.pick();
				CustomRepImpact custom = new CustomRepImpact();
				custom.delta = -RepRewards.HIGH;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, custom,
											  null, dialog.getTextPanel(), true, true), 
											  person);
			}
			
			return true;
		}
		else if ("makeHorusImportant".equals(action)) {
			makeImportant(horus_yaribay, "$sd7s_bringFiles", Stage.CRACK_FILES);
			return true;
		}
		else if ("makeReynardImportant".equals(action)) {
			makeImportant(reynard_hannan, "$sd7s_bringFiles", Stage.CRACK_FILES);
			return true;
		}
		else if ("makeDaudImportant".equals(action)) {
			PersonAPI _daud = getImportantPerson(People.DAUD);
			if(_daud == null) {
				// Oh, that's not good.
				return false;
			}
			makeImportant(_daud, "$sd7s_bringFiles", Stage.CRACK_FILES);
			return true;
		}
		else if ("makeArroyoImportant".equals(action)) {
			makeImportant(arroyo, "$sd7s_bringFiles", Stage.CRACK_FILES);
			return true;
		}
		else if ("makeZGRimportant".equals(action)) {
			if(zgr == null)
				zgr = getImportantPerson(People.GLAMOR_ROTANEV);
			if (zgr == null) return false;
			makeImportant(zgr, "$sd7s_bringFiles", Stage.CRACK_FILES);
			return true;
		}
		else if ("purgeNomios".equals(action)) {
			//log.info("called purgeNomios");
			//PersonAPI nomiosSO;
			//PersonAPI nomiosPM;
			
			if(nomios == null) return false;
			List<PersonAPI> nomiosPeople = nomios.getPeopleCopy();
			for(PersonAPI oldPerson : nomiosPeople) {
				//log.info("found person: " + oldPerson.getNameString() + ", who is the " + oldPerson.getPostId() );
				if(	oldPerson.getPostId().equals(Ranks.POST_PORTMASTER) ||
					oldPerson.getPostId().equals(Ranks.POST_SUPPLY_OFFICER)) 
				{
					String postID = oldPerson.getPostId();
					//oldPerson.addTag(Tags.INVOLUNTARY_RETIREMENT);
					oldPerson.getMemoryWithoutUpdate().set("$former_" + oldPerson.getPostId(), true);
					oldPerson.setPostId(Ranks.POST_CITIZEN);
					//nomios.getCommDirectory().removePerson(person);
					
					
					PersonAPI newPerson = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson();
					//newPerson.addTag("replacement");
					newPerson.setPostId(postID);
					newPerson.getMemoryWithoutUpdate().set("$replacement", true);
					nomios.addPerson(newPerson);
					nomios.getCommDirectory().addPerson(newPerson);
					nomios.getCommDirectory().getEntryForPerson(newPerson).setHidden(false);
					
					nomios.getCommDirectory().removePerson(oldPerson);
					nomios.getCommDirectory().addPerson(oldPerson, 1000); // back of the comm directory for you, buddy
					//nomios.getCommDirectory().getEntryForPerson(person.getId()).setHidden(true);
					
					//log.info("    replacing them with new person: " + newPerson.getNameString());
					
					// make sure the portraits are different, for the love of god.
					// I really don't want to add a while loop here, so maybe it's good enough like this?
					// Well, should reduce instances of (more) clones by quite a lot, at least. -dgb
					
					// Uncomment this to test the "ensure not same portrait" code
					//newPerson.setPortraitSprite(oldPerson.getPortraitSprite());
					
					if (oldPerson.getPortraitSprite().equals(newPerson.getPortraitSprite())) {
						WeightedRandomPicker<String> portraits = newPerson.getFaction().getPortraits(newPerson.getGender()).clone();
						portraits.remove(oldPerson.getPortraitSprite());
						String portrait = portraits.pick();
						if (portrait != null) { // would be null if the faction somehow only had that one portrait
							newPerson.setPortraitSprite(portrait);
						}
					}
				}
			}
			return true;
		}
	
		// Seven Sons intel.
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (entity.getMarket() != null && !entity.getMarket().isPlanetConditionMarketOnly()) {
			PlanetAPI planet = entity.getMarket().getPlanetEntity();
			if (planet != null) {
				entity = planet;
			}
			// TODO try station/custom_entity too?
			// Irkalla and Elysium aren't being caught here, maybe?
		}
		
		if ("addIntel".equals(action)) {
			if (params.size() > 1) {
				String id = params.get(1).getString(memoryMap);
				entity = SevenSonsIntel.getEntity(id);
			}
			SevenSonsIntel.addSevenSonsIntelIfNeeded(entity, dialog.getTextPanel());
			return true;
		} 
		else if ("setVisited".equals(action)) {
			if (params.size() > 1) {
				String id = params.get(1).getString(memoryMap);
				entity = SevenSonsIntel.getEntity(id);
			}
			SevenSonsIntel.addSevenSonsIntelIfNeeded(entity, dialog.getTextPanel(), true);
			SevenSonsIntel.setVisited(entity, dialog.getTextPanel());
			
			//System.out.println("received setVisited for entity id: " + entity.getId());
			
			// and make unimportant to this mission.
			Misc.makeUnimportant(entity, getMissionId());
			
			return true;	
		} 
		/*else if ("playMusic".equals(action)) {
			String shrineMusic = "music_luddite_shrine";
			Global.getSoundPlayer().playCustomMusic(1, 1, shrineMusic, true);
			// failsafe is not actually necessary; leaving the entity will give the music player a kick anyway
//			if (!Global.getSector().hasScript(ShrineMusicStopFailsafe.class)) {
//				Global.getSector().addScript(new ShrineMusicStopFailsafe(shrineMusic));
//			}
		} else if ("endMusic".equals(action)) {
			Global.getSoundPlayer().restartCurrentMusic();
		}*/
		
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		//info.addImage(robed_man.getPortraitSprite(), width, 128, opad);
		
		if (currentStage == Stage.CRACK_FILES) {
			info.addPara("Find a way to decrypt the ANDRADA files.", opad);
					
			// HEGEMONY
			if ( Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaHegemony") && 
				 !Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaOrcusRao")) {
				
				if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaDaud")) 
				{
					info.addPara("Take the files to the office of High Hegemon Baikal Daud.", opad);
				}
				else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaContact")) 
				{
					info.addPara("Take the files to your Hegemony contact.", opad);
				}
				else 
				{
					info.addPara("Take the files to any sufficiently senior Hegemony officer or administrator.", opad);
				}
			}
			// TRI-TACHYON
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaTT") && 
				!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaArroyo")) 
				{
				
				if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaContact")) 
				{
					info.addPara("Take the files to your Tri-Tachyon contact.", opad);
				}
				else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaZGR"))
				{
					info.addPara("Take the files to Director Glamor-Rotanev, on Eochu Bres.", opad);
				}
				else 
				{
					info.addPara("Take the files to a senior Tri-Tachyon executive or administrator.", opad);
				}
			}
			// PERSEAN LEAGUE
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaPL")){
				if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaContact")) {
					info.addPara("Take the files to your Persean League contact.", opad);
					info.addImage(siyavong.getFaction().getCrest(), width, 128, opad);
				}
				else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaReynard")) {
					info.addPara("Take the files to Reynard Hannan, on Kazeron.", opad);
					info.addImage(reynard_hannan.getPortraitSprite(), width, 128, opad);
				}
				else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaYaribay")) {
					info.addPara("Take the files to Horus Yaribay, on Kazeron.", opad);
					info.addImage(horus_yaribay.getPortraitSprite(), width, 128, opad);
				}
				else {
					info.addPara("Take the files to a sufficiently senior Persean League officer or administrator.", opad);
					info.addImage(siyavong.getFaction().getCrest(), width, 128, opad);
				}
			}
			else {
				info.addPara("You can review your possible decisions by opening the intel workspace below.", opad);
			}
			
			//addStandardMarketDesc("Ram gave you coordinates to his safehouse " + volturn.getOnOrAt(), volturn, info, opad);
		}
			else if (currentStage == Stage.TALK_GARGOYLE) {
				info.addPara("Take the files to Gargoyle and convince the hacker to decrypt them.", opad);
				addStandardMarketDesc("Gargoyle is " + gargoyle.getMarket().getOnOrAt(), gargoyle.getMarket(), info, opad);
			}
			else if (currentStage == Stage.TALK_ORCUS) {
				info.addPara("Take the files to Orcus Rao.", opad);
				addStandardMarketDesc(rao.getRank() + " Rao is " + rao.getMarket().getOnOrAt(), rao.getMarket(), info, opad);
			}
			else if (currentStage == Stage.TALK_ARROYO) {
				info.addPara("Take the files to Rayan Arroyo. He'll know how to turn this situation toward his own profit.", opad);
				addStandardMarketDesc("Arroyo is " + arroyo.getMarket().getOnOrAt(), arroyo.getMarket(), info, opad);
			}
			else if (currentStage == Stage.TALK_INESIS) {
				info.addPara("Take the files to Faro Inesis to see if you can use Tri-Tachyon black-site resources to decrypt them.", opad);
				addStandardMarketDesc("Inesis is " + inesis.getMarket().getOnOrAt(), inesis.getMarket(), info, opad);
			}
			else if (currentStage == Stage.TALK_SIYAVONG) {
				info.addPara("Take the files to Special Agent Finlay Siyavong of Fleet Intelligence to see if he will help you decrypt them.", opad);
				addStandardMarketDesc("Siyavong is " + siyavong.getMarket().getOnOrAt(), siyavong.getMarket(), info, opad);
			}
			/*else if (currentStage == Stage.TALK_HORUS_YARIBAY) {
				info.addPara("Take the files to Horus Yaribay and convince him they're his 'in' with the High Hegemon.", opad);
				addStandardMarketDesc("Horus Yaribay is " + horus_yaribay.getMarket().getOnOrAt(), horus_yaribay.getMarket(), info, opad);
			}
			else if (currentStage == Stage.TALK_REYNARD_HANNAN) {
				info.addPara("Take the files to Reynard Hannan and show him what you're worth.", opad);
				addStandardMarketDesc("Reynard Hannan is " + reynard_hannan.getMarket().getOnOrAt(), reynard_hannan.getMarket(), info, opad);
			}*/
			else if (currentStage == Stage.TALK_TO_AI) {
				if(aiColony != null) {
					info.addPara("You've decided to use your alpha-core AI administrator's abilities to decrypt the files.", opad);
					addStandardMarketDesc("Take the ANDRADA files to your 'special executive consultant' team on", aiColony, info, opad);
				}
			
		}
		else if (currentStage == Stage.FIND_THEM) {
			info.addPara("Find the seven clone-sons of Philip Andrada.", opad);
		// Perseus
			if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_PERSEUS)) {
				//info.addPara("Perseus 'Saturnian' Andrada was last reported to be involved in some kind of operation on Nomios overseen by one 'Doctor Cydonia'.", opad);
				addStandardMarketDesc("Perseus 'Saturnian' Andrada was last reported to be involved in a procedure carried out by 'Doctor' Cydonia on", nomios, info, opad);
				
				if(	Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_gotCydoniaBarStory") || 
					Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$metCydonia")) { 
					addStandardMarketDesc("Doctor Cydonia can be found at", cydonia.getMarket(), info, opad);
				}
				
			}
			else {
				info.addPara("Perseus 'Saturnian' Andrada: deceased due to failed operation performed by 'Doctor' Cydonia.", opad);
			}
		// Hercules
			if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_HERCULES)) {
				info.addPara("Hercules 'Cyrino' Andrada is an assistant academician in the Galatia Academy", opad);
			}
			else {
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_allowedHerculesAssassination")) {
					info.addPara("Hercules 'Cyrino' Andrada was killed in front of you by an unknown assassin.", opad);
				}
				else {
					info.addPara("Hercules 'Cyrino' Andrada has gone into hiding.", opad);
				}
					
			}
		// Jason
			if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_JASON)) {
				addStandardMarketDesc("Jason 'Korin' Andrada was last reported to be on", qaras, info, opad);
			}
			else {
				info.addPara("Jason 'Korin' Andrada was killed as a bystander in a shooting.", opad);
			}
			
		// Achilles
			if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_ACHILLES)) {
				addStandardMarketDesc("Achilles 'Kalian' Andrada was last reported to be on", yesod, info, opad);
			}
			else {
				info.addPara("Achilles 'Kalian' Andrada has gone into hiding, for now. Fortuna Kato appears to be planning to back his bid to take over the Sindrian Diktat.", opad);
				// or..
			}
						
		// Leonidas
			if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_LEONIDAS)) {
				info.addPara("Leonidas 'Esculus' Andrada was reported to be on an asteroid in the Duzahk system, 17054 Irkalla.", opad);
			}
			else {
				//if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_LEONIDAS)) {
				info.addPara("Leonidas 'Esculus' Andrada is deceased.", opad);
			}
			
		// Pericles
			if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_PERICLES)) {
				addStandardMarketDesc("Pericles 'Aventine' Andrada was reported working as a mercenary with his adoptive guardian, Maeus Moroz, on", orthrus, info, opad);
			}
			else {
				info.addPara("Pericles 'Aventine' Andrada is dead, assassinated by an unknown party.", opad);
			}
		// Hector
			if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_HECTOR)) {
				info.addPara("Hector 'Palatin' Andrada was reported to be on an asteroid in the Hybrasil system, 58966 Elysium.", opad);
			}
			else {
				info.addPara("Hector  'Palatin' Andrada left Elysium 68966, presumably with the aid of Dolos Macario.", opad);
			}
		}
		
		if (currentStage == Stage.FIND_THEM) {
			SevenSonsIntel.addShowSevenSonsButton(this, width, height, info);
			SevenSonsIntel.addSonsIntelDialogButton(this, width, height, info); 
		}
		else if (currentStage == Stage.CRACK_FILES) {
			SevenSonsIntel.addSonsDecryptionDialogButton(this, width, height, info); 
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		
		
		if (currentStage == Stage.CRACK_FILES) {
			info.addPara("Find a way to decrypt the ANDRADA files.", tc, pad);
			
			// TODO more of these?
			if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaReynard")) {
				info.addPara("Take the files to Reynard Hannan, on Kazeron.", tc, pad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaYaribay")) {
				info.addPara("Take the files to Horus Yaribay, on Kazeron.", tc, pad);
			}
			else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaZGR")) {
				info.addPara("Take the files to Zunya Glamor-Rotanev, on Eochu Bres.", tc, pad);
			}
			//else if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_decryptViaTT")) {
			//	
			//}
			
			return true;
		}
			else if (currentStage == Stage.TALK_GARGOYLE) {
				info.addPara("Ask Gargoyle to decrypt the ANDRADA files.", tc, pad);
				addStandardMarketDesc("Gargoyle is " + gargoyle.getMarket().getOnOrAt(), tc, gargoyle.getMarket(), info, pad);
				return true;
			}
			else if (currentStage == Stage.TALK_ORCUS) {
				info.addPara("Take the files to Orcus Rao.", tc, pad);
				addStandardMarketDesc(rao.getRank() + " Rao is " + rao.getMarket().getOnOrAt(), tc, rao.getMarket(), info, pad);
			}
			else if (currentStage == Stage.TALK_ARROYO) {
				info.addPara("Take the files to Rayan Arroyo.", tc, pad);
				addStandardMarketDesc("Arroyo is " + arroyo.getMarket().getOnOrAt(), tc, arroyo.getMarket(), info, pad);
			}
			else if (currentStage == Stage.TALK_INESIS) {
				info.addPara("Take the files to Faro Inesis.", tc, pad);
				addStandardMarketDesc("Inesis is " + inesis.getMarket().getOnOrAt(), tc, inesis.getMarket(), info, pad);
			}
			else if (currentStage == Stage.TALK_SIYAVONG) {
				info.addPara("Take the files to Finlay Siyavong.", tc, pad);
				addStandardMarketDesc("Siyavong is " + siyavong.getMarket().getOnOrAt(), tc, siyavong.getMarket(), info, pad);
			}
			/*else if (currentStage == Stage.TALK_HORUS_YARIBAY) {
				info.addPara("Take the files to Horus Yaribay.", tc, pad);
				addStandardMarketDesc("Horus Yaribay is " + horus_yaribay.getMarket().getOnOrAt(), tc, horus_yaribay.getMarket(), info, pad);
			}
			else if (currentStage == Stage.TALK_REYNARD_HANNAN) {
				info.addPara("Take the files to Reynard Hannan.", tc, pad);
				addStandardMarketDesc("Reynard Hannan is " + reynard_hannan.getMarket().getOnOrAt(), tc, reynard_hannan.getMarket(), info, pad);
			}*/
			else if (currentStage == Stage.TALK_TO_AI) {
				if(aiColony != null) {
					addStandardMarketDesc("Take the ANDRADA files to your 'special executive consultant' team on", tc, aiColony, info, pad);
				}
				//else // oh no
			}

		else if (currentStage == Stage.FIND_THEM) { 
			info.addPara("Find the seven sons of Philip Andrada.", tc, pad);
			
			return true;
		}

		return false;
	}

	@Override
	public String getBaseName() {
		if(useSons) return "Seven Sons";
		return "Seven Suns";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	
	private void addLocations() {
		MarketAPI donn = getMarket("donn");
		StarSystemAPI hybrasil = donn.getStarSystem();
		
		elysium = hybrasil.addCustomEntity("58966elysium", "58966 Elysium", Entities.ASTEROID_BASE, Factions.NEUTRAL);
		elysium.setCircularOrbit(
				hybrasil.getCenter(),
				donn.getPlanetEntity().getCircularOrbitAngle() + 60f,
				donn.getPlanetEntity().getCircularOrbitRadius(),
				donn.getPlanetEntity().getCircularOrbitPeriod()); 
		elysium.getMemoryWithoutUpdate().set("$sd7s_elysium", true);
		
		// fake market so raids work
		/*MarketAPI market = Global.getFactory().createMarket("elysium_market", elysium.getName(), 3);
		market.setSize(3);
		market.setHidden(true);
		market.setFactionId(Factions.INDEPENDENT);
		market.setSurveyLevel(SurveyLevel.FULL);
		market.setFactionId(elysium.getFaction().getId());
		market.getMemoryWithoutUpdate().set(MemFlags.MARKET_HAS_CUSTOM_INTERACTION_OPTIONS, true);
		market.setPrimaryEntity(elysium);
		elysium.setMarket(market);*/
		
		makeImportant(elysium, "$sd7s_findHector", Stage.FIND_THEM);
		
		StarSystemAPI duzahk = Global.getSector().getStarSystem("duzahk");
		List<SectorEntityToken> jumpPoints = duzahk.getJumpPoints();
		SectorEntityToken lastJumpPoint = jumpPoints.get(jumpPoints.size()-1);
			
		irkalla = duzahk.addCustomEntity("17054irkalla", "17054 Irkalla", Entities.ASTEROID_BASE, Factions.NEUTRAL);
		irkalla.setCircularOrbit(
				duzahk.getCenter(),
				lastJumpPoint.getCircularOrbitAngle() - 60f,
				lastJumpPoint.getCircularOrbitRadius(),
				lastJumpPoint.getCircularOrbitPeriod()); 
		irkalla.getMemoryWithoutUpdate().set("$sd7s_irkalla", true);
		
		makeImportant(irkalla, "$sd7s_findLeonidas", Stage.FIND_THEM);
		
	}
	
	private void damageFleet( float damageMult, int numTargets) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		WeightedRandomPicker<FleetMemberAPI> targets = new WeightedRandomPicker<FleetMemberAPI>();
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			float w = 1f;
			switch (member.getHullSpec().getHullSize()) {
				case CAPITAL_SHIP: w = 12f; break;
				case CRUISER: w = 8f; break;
				case DESTROYER: w = 4f; break;
				case FRIGATE: w = 2f; break;
			}
			targets.add(member, w);
		}
		
		for (int i = 0; i < numTargets; i++) {
			FleetMemberAPI member = targets.pick();
			if (member != null) {
				Misc.applyDamage(member, null, damageMult, true, "asteroid_base_defenses", "Asteroid base defenses",
								 true, null, member.getShipName() + " suffers damage from asteroid base defenses");
			}
			
			damageMult--;
			if(damageMult == 0f) 
				damageMult = 1f;
		}
		
		Global.getSoundPlayer().playSound("hit_heavy", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
	}
	
	public static void addLargeSons() {
		
		SectorEntityToken galatia_academy = Global.getSector().getStarSystem("galatia").getEntityById("station_galatia_academy");
		MarketAPI market = galatia_academy == null ? null : galatia_academy.getMarket();
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(People.HERCULES);
			person.setImportance(PersonImportance.LOW);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_ASSISTANT_ACADEMICIAN);
			person.setVoice(Voices.SCIENTIST);
			person.getName().setFirst("Hercules");
			person.getName().setLast("Cyrino");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "hercules_andrada"));
			
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			Global.getSector().getImportantPeople().addPerson(person); // so the person can be retrieved by id 

			PersonAPI person2 = Global.getFactory().createPerson();
			person2.setId(People.BENOIT);
			person2.setImportance(PersonImportance.VERY_LOW);
			person2.setFaction(Factions.INDEPENDENT);
			person2.setGender(Gender.FEMALE);
			person2.setRankId(Ranks.CITIZEN);
			person2.setPostId(Ranks.POST_ASSISTANT_ACADEMICIAN);
			person2.setVoice(Voices.SCIENTIST);
			person2.getName().setFirst("Erdene");
			person2.getName().setLast("Benoit");
			person2.setPortraitSprite(Global.getSettings().getSpriteName("characters", "benoit"));
			
			market.getCommDirectory().addPerson(person2);
			market.getCommDirectory().getEntryForPerson(person2).setHidden(true);
			market.addPerson(person2);
			Global.getSector().getImportantPeople().addPerson(person2); // so the person can be retrieved by id 

			PersonAPI person3 = Global.getFactory().createPerson();
			person3.setId(People.LOTT);
			person3.setImportance(PersonImportance.MEDIUM);
			person3.setFaction(Factions.INDEPENDENT);
			person3.setGender(Gender.MALE);
			person3.setRankId(Ranks.CITIZEN);
			person3.setPostId(Ranks.POST_SECURITY_CHIEF);
			person3.setVoice(Voices.SOLDIER);
			person3.getName().setFirst("Marcellus");
			person3.getName().setLast("Lott");
			person3.setPortraitSprite(Global.getSettings().getSpriteName("characters", "lott"));
			
			market.getCommDirectory().addPerson(person3);
			market.getCommDirectory().getEntryForPerson(person3).setHidden(true);
			market.addPerson(person3);
			Global.getSector().getImportantPeople().addPerson(person3); // so the person can be retrieved by id 
		}
		
		market =  Global.getSector().getEconomy().getMarket("yesod");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(People.ACHILLES);
			person.setImportance(PersonImportance.MEDIUM);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SPACE_LIEUTENANT);
			person.setPostId(Ranks.POST_OFFICER);
			person.setVoice(Voices.SOLDIER);
			person.getName().setFirst("Achilles");
			person.getName().setLast("Kalian");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "achilles_andrada"));
			person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
			person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 1);
			person.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
			person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			person.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
			//person.getStats().setSkillLevel(Skills.NAVIGATION, 1); // actually, don't fly too fast, it's dangerous.
			person.getStats().setLevel(5);
			
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			Global.getSector().getImportantPeople().addPerson(person); // so the person can be retrieved by id 
		}
		
		market = Global.getSector().getEconomy().getMarket("sindria");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(People.HECTOR);
			person.setImportance(PersonImportance.MEDIUM);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_UNKNOWN);
			person.setVoice(Voices.ARISTO);
			person.getName().setFirst("Hector");
			person.getName().setLast("Palatin");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "hector_andrada"));
			
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			Global.getSector().getImportantPeople().addPerson(person); // so the person can be retrieved by id 
		}
		
		market =  Global.getSector().getEconomy().getMarket("orthrus");
		if (market != null) {
			ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
			
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(People.MAEUS_MOROZ);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.MALE);
			person.setVoice(Voices.SOLDIER);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_MERCENARY);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Maeus");
			person.getName().setLast("Moroz");
			person.addTag(Tags.CONTACT_MILITARY);
			person.addTag(Tags.CONTACT_UNDERWORLD);
			person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
			person.getStats().setSkillLevel(Skills.FIELD_REPAIRS, 1);
			person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			person.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
			person.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
			person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
			person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 1);
			person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
			person.getStats().setLevel(8);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "maeus_moroz"));
			market.addPerson(person);
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}		
		
		// And the "contractors"... 
		
		PersonAPI person = Global.getFactory().createPerson();
		person.setId(People.CRASIS);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(Gender.ANY);
		person.setRankId(Ranks.AI_CORE);
		person.setPostId(Ranks.POST_ALPHA_CORE);
		person.setImportance(PersonImportance.MEDIUM);
		person.getName().setFirst("Crasis");
		//person.getName().setLast("Crasis");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", People.CRASIS));
		Global.getSector().getImportantPeople().addPerson(person);
		
		person = Global.getFactory().createPerson();
		person.setId(People.AUREOLIN);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(Gender.ANY);
		person.setRankId(Ranks.AI_CORE);
		person.setPostId(Ranks.POST_GAMMA_CORE);
		person.setImportance(PersonImportance.MEDIUM);
		person.getName().setFirst("Aureolin");
		//person.getName().setLast("Aureolin");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", People.AUREOLIN));
		Global.getSector().getImportantPeople().addPerson(person);

	}
	
	private int getSonsFoundCount() {
		int _count = 0;
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_PERSEUS) == true) _count++;
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_HERCULES) == true) _count++;
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_JASON) == true) _count++;
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_HECTOR) == true) _count++;
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_ACHILLES) == true) _count++;
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_LEONIDAS) == true) _count++;
		if( Global.getSector().getMemoryWithoutUpdate().getBoolean(FOUND_PERICLES) == true) _count++;
		sonsFoundCount = _count;
		//sonsFoundKilledCount
		return sonsFoundCount;
	}
	
	private void spawnIrkallaMercFleet() {
		// CARRIERS!
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		FleetCreatorMission m = new FleetCreatorMission(random);
		
		m.beginFleet();
		m.createQualityFleet(3, Factions.MERCENARY, irkalla.getLocationInHyperspace());
		m.triggerSetFleetFaction(Factions.INDEPENDENT);
		m.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		m.triggerMakeNoRepImpact();
		m.triggerFleetAllowLongPursuit();
		m.triggerFleetSetName("Gray Arsenal Contract Fleet");

		m.triggerMakeHostileAndAggressive();
		m.triggerSetFleetDoctrineComp(1, 4, 1);
		m.triggerFleetMakeFaster(true, 1, true);
		m.triggerFleetMakeImportant(null, Stage.COMPLETED);
		
		if(random == null) random = new Random();
		Random r = Misc.getRandom(random.nextLong(), 7);
		
		m.triggerAddShips("doom_Support");
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(r);
		picker.add("venture_Balanced", 5);
		picker.add("heron_Attack", 5);
		picker.add("heron_Attack1", 5);
		picker.add("heron_Attack2", 5);
		picker.add("condor_Attack", 5);
		picker.add("condor_Strike", 5);
		picker.add("condor_Support", 5);
		picker.add("drover_Strike", 5);
		picker.add("drover_Support", 5);
		picker.add("gemini_Standard", 5);
		picker.add("shepherd_Frontier", 10);
		picker.add("monitor_Escort", 10);
		picker.add("brawler_Elite", 10);

		int add = 11;
		while (!picker.isEmpty() && add > 0) {
			m.triggerAddShips(picker.pickAndRemove());
			add--;
		}
		
		m.triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
		m.triggerMakeFleetIgnoredByOtherFleets();
		m.triggerSetFleetGenericHailPermanent("SD7SirkMercHail");
		m.triggerSetFleetFlagPermanent("$sd7s_carrierMerc");
		
		m.setFleetSource(arroyo.getMarket());
		m.triggerSetFleetMissionRef("$sd7s_ref");
		
		CampaignFleetAPI fleet = m.createFleet();
		if (fleet != null) {
			irkalla.getStarSystem().addEntity(fleet);
			
			//SectorEntityToken _jp = irkalla.getStarSystem().getJumpPoints().get(0);
			//fleet.setLocation(
			//		_jp.getLocation().x,
			//		_jp.getLocation().y);
			fleet.setLocation(
					irkalla.getLocation().x + 600,
					irkalla.getLocation().y - 600);
			
			m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
			m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
			// FLEET_FIGHT_TO_THE_LAST ?
		
			fleet.clearAssignments();
			fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 24f, "fulfilling contractual obligations");
			fleet.addAssignment(FleetAssignment.STANDING_DOWN, null, 0.5f, "performing sensors check");
			fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, irkalla, 16f, "fulfilling contractual obligations");
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, irkalla, 8f, "performing clean-up");

			Misc.giveStandardReturnAssignments(fleet, arroyo.getMarket().getPrimaryEntity(), null, false);
		}
	}

	private void spawnElysiumMercFleet() {
		// Guns guns guns!!
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			
		FleetCreatorMission m = new FleetCreatorMission(random);
		
		m.beginFleet();
		m.createQualityFleet(3, Factions.MERCENARY, orthrus.getLocationInHyperspace());
		m.triggerSetFleetFaction(Factions.INDEPENDENT);
		m.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		//m.triggerSetFleetMaxNumShips(18);
		m.triggerMakeNoRepImpact();
		m.triggerFleetAllowLongPursuit();
		m.triggerFleetSetName("Cadmus Corps Mercenaries");
		m.triggerMakeHostileAndAggressive();

		m.triggerFleetMakeFaster(true, 1, true);
		m.triggerFleetMakeImportant(null, Stage.COMPLETED);
		
		m.triggerAddShips("champion_Elite");
		m.triggerAddShips("dominator_Support");
		m.triggerAddShips("eagle_Balanced");
		m.triggerAddShips("falcon_CS");
		m.triggerAddShips("falcon_CS");
		
		if(random == null) random = new Random();
		Random r = Misc.getRandom(random.nextLong(), 7);
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(r);

		picker.add("sunder_Assault", 5);
		picker.add("sunder_CS", 5);
		picker.add("sunder_Overdriven", 5);
		picker.add("sunder_Support", 5);
		picker.add("manticore_Assault", 5);
		picker.add("manticore_Balanced", 5);
		picker.add("manticore_Support", 5);
		picker.add("brawler_Elite", 10);
		picker.add("brawler_Elite", 10);
		picker.add("brawler_Support", 10);
		picker.add("brawler_Support", 10);
		picker.add("mudskipper2_Hellbore", 5);
		picker.add("mudskipper2_Hellbore", 5);
		picker.add("mudskipper2_CS", 5);
		picker.add("mudskipper2_CS", 5);

		int add = 13;
		while (!picker.isEmpty() && add > 0) {
			m.triggerAddShips(picker.pickAndRemove());
			add--;
		}
		
		m.triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
		m.triggerMakeFleetIgnoredByOtherFleets();
		m.triggerSetFleetGenericHailPermanent("SD7SelyMercHail");
		m.triggerSetFleetFlagPermanent("$sd7s_elysiumMerc");
		
		m.setFleetSource(arroyo.getMarket());
		m.triggerSetFleetMissionRef("$sd7s_ref");
		
		m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
		m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
		m.triggerFleetAddCommanderSkill(Skills.FIELD_REPAIRS, 1);
		m.triggerFleetAddCommanderSkill(Skills.TARGET_ANALYSIS, 1);
		m.triggerFleetAddCommanderSkill(Skills.BALLISTIC_MASTERY, 1);
		m.triggerSetFleetMissionRef("$sd7s_ref");
		CampaignFleetAPI fleet = m.createFleet();
		
		if (fleet != null) {
			
			elysium.getStarSystem().addEntity(fleet);
			fleet.setLocation(
					elysium.getLocation().x + 800,
					elysium.getLocation().y + 500);
			
			m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
			m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
			// FLEET_FIGHT_TO_THE_LAST ?
		
			fleet.clearAssignments();
			fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 24f, "fulfilling contractual obligations");
			fleet.addAssignment(FleetAssignment.STANDING_DOWN, null, 0.5f, "performing sensors check");
			fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, elysium, 16f, "fulfilling contractual obligations");
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, elysium, 8f, "peforming clean-up");

			Misc.giveStandardReturnAssignments(fleet, arroyo.getMarket().getPrimaryEntity(), null, false);
		}
	}

	public static CampaignFleetAPI spawnMorozFleet(Random random, boolean forAskoniaStandoff) {
		if (random == null) random = new Random();
		
		MarketAPI orthrus = Global.getSector().getEconomy().getMarket("orthrus");
		PersonAPI maeus_moroz = Global.getSector().getImportantPeople().getData(People.MAEUS_MOROZ).getPerson();
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			
		FleetCreatorMission m = new FleetCreatorMission(random);
		
		m.beginFleet();
		//m.triggerCreateFleet(FleetSize.SMALL, FleetQuality.SMOD_1, Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, orthrus.getPrimaryEntity());
		int difficulty = 3;
		if (forAskoniaStandoff) difficulty = 8;
		m.createQualityFleet(difficulty, Factions.MERCENARY, orthrus.getLocationInHyperspace());
		m.triggerSetFleetFaction(Factions.INDEPENDENT);
		if (forAskoniaStandoff) {
			m.triggerSetFleetOfficers(OfficerNum.ALL_SHIPS, OfficerQuality.HIGHER);
		} else {
			m.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		}
		//m.triggerSetFleetMaxNumShips(18);
		if (!forAskoniaStandoff) {
			m.triggerMakeNoRepImpact();
		}
		m.triggerFleetAllowLongPursuit();
		m.triggerFleetSetName("Samarra Wolves");
		//m.triggerMakeHostile();

		//m.triggerMakeHostileAndAggressive();
		//m.triggerSetFleetDoctrineComp(5, 0, 0);
		m.triggerFleetMakeFaster(true, 1, true);
		if (!forAskoniaStandoff) {
			m.triggerFleetMakeImportant(null, Stage.COMPLETED);
		}
		m.triggerAddShips("onslaught_Outdated", "dominator_XIV_Elite", "dominator_XIV_Elite");
		//m.triggerAddShips("dominator_XIV_Elite");
		//m.triggerAddShips("dominator_XIV_Elite");
		
		Random r = Misc.getRandom(random.nextLong(), 7);
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(r);
		picker.add("venture_p_Pirate", 5);
		picker.add("venture_p_Pirate", 5);
		picker.add("venture_p_Pirate", 5);
		picker.add("eradicator_pirates_Attack", 5);
		picker.add("eradicator_pirates_Attack", 5);
		picker.add("eradicator_pirates_Attack", 5);
		picker.add("eradicator_pirates_Overdriven", 5);
		picker.add("eradicator_pirates_Overdriven", 5);
		picker.add("eradicator_pirates_Overdriven", 5);
		picker.add("enforcer_XIV_Elite", 5);
		picker.add("enforcer_XIV_Elite", 5);
		picker.add("enforcer_XIV_Elite", 5);
		picker.add("enforcer_XIV_Elite", 5);
		picker.add("manticore_Assault", 5);
		picker.add("manticore_Assault", 5);
		picker.add("brawler_Support", 5);
		picker.add("brawler_Support", 5);
		picker.add("centurion_Assault", 5);
		picker.add("centurion_Assault", 5);

		int add = 13;
		if (forAskoniaStandoff) {
			add = 25;
			m.triggerAddShips("legion_Escort");
			m.triggerMakeNonHostile();
		}
		while (!picker.isEmpty() && add > 0) {
			m.triggerAddShips(picker.pickAndRemove());
			add--;
		}

		if (!forAskoniaStandoff) {
			m.triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
			m.triggerMakeFleetIgnoredByOtherFleets();
			m.triggerSetFleetGenericHailPermanent("SD7SmorozHail");
			m.triggerSetFleetFlagPermanent("$sd7s_morozMerc");
			m.triggerSetFleetMissionRef("$sd7s_ref");
		}

		//m.triggerPickLocationAroundEntity(orthrus.getPlanetEntity(), 100f);
//		m.triggerPickSetLocation(
//				orthrus.getStarSystem(),
//				orthrus.getPlanetEntity().getLocation());
		m.setFleetSource(orthrus);
//		m.triggerSpawnFleetAtPickedLocation("$sd7s_maeusMorozFleet", "$sd7s_ref");
		//m.triggerFleetNoAutoDespawn();
		//m.triggerOrderFleetInterceptPlayer();
		
		CampaignFleetAPI fleet = m.createFleet();
		if (fleet != null) {
			orthrus.getStarSystem().addEntity(fleet);
			
			fleet.setLocation(
					orthrus.getPlanetEntity().getLocation().x,
					orthrus.getPlanetEntity().getLocation().y);
			
			if (forAskoniaStandoff) {
				fleet.setCommander(maeus_moroz);
				fleet.getFlagship().setCaptain(maeus_moroz);
				return fleet;
			}
			
			m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
			m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
			// FLEET_FIGHT_TO_THE_LAST
		
			fleet.clearAssignments();
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, orthrus.getPrimaryEntity(), 1f, "preparing for revenge");
			fleet.addAssignment(FleetAssignment.INTERCEPT, pf, 30f, "seeking vengeance");
			fleet.setCommander(maeus_moroz);
			fleet.getFlagship().setCaptain(maeus_moroz);
			
			Misc.giveStandardReturnAssignments(fleet, orthrus.getPrimaryEntity(), null, false);
			//fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, intel.getEntity(), 20f, "moving to attack " + intel.getEntity().getName());
			//Misc.giveStandardReturnToSourceAssignments(fleet, false);
			//fleet.addScript(new MissionFleetAutoDespawn(this, fleet));
			//fleet.addAssignmentAtStart(FleetAssignment.INTERCEPT, pf, 1f, null);	
			
			//Misc.addDefeatTrigger(fleet, "SD7SMorozDefeated");
			
			fleet.addEventListener(new BaseFleetEventListener() {
				@Override
				public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
					if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
						Global.getSector().getMemoryWithoutUpdate().set("$sd7s_morozDefeated", true);
					}
				}
				@Override
				public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
					// defeated in any battle...
					if (battle.wasFleetDefeated(fleet, primaryWinner)) {
						if(primaryWinner.equals(Global.getSector().getPlayerFleet())) {
							Global.getSector().getMemoryWithoutUpdate().set("$sd7s_morozDefeatedByPC", true);
						}
						fleet.getMemoryWithoutUpdate().set("$defeated", true);
						Global.getSector().getMemoryWithoutUpdate().set("$sd7s_morozDefeated", true);
					}
				}
			});
		}
		return fleet;
	}
	
	/*private boolean createQarasAgentAndFleet() {
		
		PersonAPI qaras_agent = Global.getSector().getFaction(Factions.DIKTAT).createRandomPerson();
		qaras_agent.setRankId(Ranks.SPACE_COMMANDER);
		qaras_agent.setPostId(Ranks.AGENT);
		qaras_agent.setImportance(PersonImportance.LOW);

		if (!setPersonMissionRef(qaras_agent, "$sd7s_ref")) return false;
	
		qaras_agent.setId("sd7s_qaras_agent");
		Global.getSector().getImportantPeople().addPerson(qaras_agent);
		
		FleetCreatorMission m = new FleetCreatorMission(random);

		//CampaignFleetAPI fleet = m.createFleet();
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.TRADE_SMUGGLER, qaras);
		if (fleet != null) {
			//m.fleet
			fleet.getMemoryWithoutUpdate().set("$sd7s_ref", this);
			fleet.setName("Unidentified Ship");
			//Misc.makeImportant(fleet, "sd7s_qaras_agent"); // ??
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true); // so it keeps transponder on
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_FLEE_PLAYER, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
			fleet.getFleetData().addFleetMember("mercury_Standard");
			fleet.getFleetData().ensureHasFlagship();
		
			//fleet.clearAbilities();
			
			//Misc.addDefeatTrigger(fleet, "SD7SqarasAgentFleetDefeated");
			Misc.makeImportant(fleet, "sd7s");
			qaras.getStarSystem().addEntity(fleet);
			
			fleet.setLocation(
					qaras.getPlanetEntity().getLocation().x,
					qaras.getPlanetEntity().getLocation().y);
			
			m.setFlagWithReason(fleet, MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true);

			fleet.clearAssignments();
			fleet.setNoFactionInName(true);
			fleet.setCommander(qaras_agent);
			
			if(sindria == null) sindria = getMarket("sindria");
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
		
		return true;
	}*/
	
	public static class SD7SqarasAgentFIDConfig implements FIDConfigGen {
		public FIDConfig createConfig() {
			FIDConfig config = new FIDConfig();

			config.showTransponderStatus = false;
			config.showEngageText = false;
			config.alwaysPursue = true;
			config.dismissOnLeave = false;
			config.withSalvage = false;
			config.printXPToDialog = true;
			config.noSalvageLeaveOptionText = "Continue";

			config.delegate = new BaseFIDDelegate() {
				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
					new RemnantFleetInteractionConfigGen().createConfig().delegate.postPlayerSalvageGeneration(dialog, context, salvage);
				}
				
				public void notifyLeave(InteractionDialogAPI dialog) {
					
					SectorEntityToken other = dialog.getInteractionTarget();
					if (!(other instanceof CampaignFleetAPI)) {
						dialog.dismiss();
						return;
					}
					
					CampaignFleetAPI fleet = (CampaignFleetAPI) other;
					if (!fleet.isEmpty()) {
						dialog.dismiss();
						return;
					}
					
					Global.getSector().getMemoryWithoutUpdate().set(DEFEATED_QARAS_AGENT_FLEET, true);
					
					dialog.setInteractionTarget(fleet);
					RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("SD7SQarasAgentDefeated");
					dialog.setPlugin(plugin);
					plugin.init(dialog);
				}
				
			};
			
			return config;  
		}
	}

	protected MarketAPI findColonyWithAIAdmin() {
		// This is called by updateInteractionDataImpl()
		
		float bestScore = 0f;
		MarketAPI best = null;
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			if (market.getAdmin() != null && market.getAdmin().isAICore()) {
				float score = market.getSize() * 1000000f + market.getDaysInExistence();
				if (score > bestScore) {
					best = market;
					bestScore = score;
				}
			}
		}
		return best;
	}
	
	protected PersonAPI findBestContactFromFaction() {
		float bestScore = 0f;
		PersonAPI person = null;
		/*Global.getSector().getIntelManager().getIntel(ContactIntel.class) {
			person = ((ContactIntel)intel).getPerson();
			if (person == null) continue;
			if (person.getFaction() == faction) {
				return true;
			}
		}*/
		return person;
	}
}





