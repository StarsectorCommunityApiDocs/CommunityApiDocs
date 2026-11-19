package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDInsurgentACAFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDInsurgentARCFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDInsurgentVSDCFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDRaoBacksAntisFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDTestDevastationFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDTestDevastationSubFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDDevastation;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRArroyoFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRCadenFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRGensKatoFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRInesisFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRInsurgentACAFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRMacarioFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRPlayerColonyFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRTestAchillesFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRTestHectorFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRTriTachyonFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDRVolturnVSDCFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDResolution;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * For Sindrian Diktat Resolution ("SDR") related calls. ...Also for Sindrian
 * Diktat Devastation ("SDD"), come to think of it.
 * 
 * SDR_CMD <action> <parameters>
 */
public class SDR_CMD extends BaseCommandPlugin {
	
	private static final Logger log = Logger.getLogger(SDR_CMD.class);
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		if (dialog == null)
			return false;

		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		SectorEntityToken other = dialog.getInteractionTarget();
		CargoAPI cargo = pf.getCargo();
		
		String action = params.get(0).getString(memoryMap);

		// we'll need these for doing pretty much anything in this class.
		SDResolution sdr = SDResolution.get();
		SDDevastation sdd = SDDevastation.get();
		if (sdr == null)
			return false;
		if (sdd == null)
			return false;

		

		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null)
			return false; // should not be possible unless there are other big problems already

		// MarketAPI market = dialog.getInteractionTarget().getMarket();
		StarSystemAPI system = null;
		if (dialog.getInteractionTarget().getContainingLocation() instanceof StarSystemAPI) {
			system = (StarSystemAPI) dialog.getInteractionTarget().getContainingLocation();
		}
		
		//log.info("SDR_CMD called action: " + action);

		if ("updateRaoSupportRebels".equals(action)) {
			sdd.addFactor(new SDDRaoBacksAntisFactor(), dialog);
		} else if ("updateArroyoAssistHeir".equals(action)) 
		{
			// be sure you set the global flag first, btw.
			sdr.addOrReplaceFactor(new SDRArroyoFactor(), dialog);
		} 
		else if ("updateArroyoAssistRebels".equals(action)) 
		{
			sdd.addOrReplaceFactor(new SDRArroyoFactor(), dialog);
			
			// he adds a little to all the rebels, too.
			sdd.addOrReplaceFactor(new SDDInsurgentVSDCFactor(), dialog);
			sdd.addOrReplaceFactor(new SDDInsurgentARCFactor(), dialog);
			sdd.addOrReplaceFactor(new SDDInsurgentACAFactor(), dialog);
			
			sdr.addOrReplaceFactor(new SDRVolturnVSDCFactor(), dialog);
			sdr.addOrReplaceFactor(new SDRInsurgentACAFactor(), dialog);
			
		} else if ("removeAllArroyoEffects".equals(action)) {
			Class _class = SDRArroyoFactor.class;
			if (sdd.getFactorOfClass(_class) != null)
				sdd.removeFactor(sdd.getFactorOfClass(SDRArroyoFactor.class));
			if (sdr.getFactorOfClass(_class) != null)
				sdr.removeFactor(sdr.getFactorOfClass(SDRArroyoFactor.class));
			
			sdd.addOrReplaceFactor(new SDDInsurgentVSDCFactor(), dialog);
			sdd.addOrReplaceFactor(new SDDInsurgentARCFactor(), dialog);
			sdd.addOrReplaceFactor(new SDDInsurgentACAFactor(), dialog);
			
			sdr.addOrReplaceFactor(new SDRInsurgentACAFactor(), dialog);
			sdr.addOrReplaceFactor(new SDRVolturnVSDCFactor(), dialog);
			
		} else if ("updateVSDCeffects".equals(action)) {
			sdd.addOrReplaceFactor(new SDDInsurgentVSDCFactor(), dialog);
			sdr.addOrReplaceFactor(new SDRVolturnVSDCFactor(), dialog);
		} else if ("addVSDCunrest".equals(action)) {
			addVSDCStartUnrest();
		} else if ("updateGensKatoEffects".equals(action)) {
			sdr.addOrReplaceFactor(new SDRGensKatoFactor(), dialog);
		} else if ("updateARCeffects".equals(action)) {
			sdd.addOrReplaceFactor(new SDDInsurgentARCFactor(), dialog);
		} else if ("addARCunrest".equals(action)) {
			addARCStartUnrest();
		} else if ("updateACAeffects".equals(action)) {
			sdd.addOrReplaceFactor(new SDDInsurgentACAFactor(), dialog);
			sdr.addOrReplaceFactor(new SDRInsurgentACAFactor(), dialog);
		} else if ("addTriTachyonEffects".equals(action)) {
			sdd.addOrReplaceFactor(new SDRTriTachyonFactor(), dialog);
			// $global.sdse_gotInesisSupportForAchilles
		} 
		else if ("addInesisEffects".equals(action)) {
			sdr.addOrReplaceFactor(new SDRInesisFactor(), dialog);
		} 
		else if ("doStartUpdate".equals(action)) {
			// Fire this at the start of the event to get all the 'default' effects in.
			doConflictStartUpdate(dialog, sdr, sdd);
		}
		else if ("addSDInsurgentActivity".equals(action)) {
			
			// params 1 is amount
			int _amount = params.get(1).getInt(memoryMap);
			
			// params 2 is reason (default: "insurgency")
			String _reason = "insurgency";
			
			if(params.size() > 2)
				_reason = params.get(2).getString(memoryMap);
			
			addSDUnrest(_amount, _reason);
		} 
		else if ("hectorCheckRagnar".equals(action)) {

			boolean _disruption = false;
			MarketAPI market = Global.getSector().getEconomy().getMarket("ragnar_complex");
			if(market != null) {
				Industry ind1 = market.getIndustry(Industries.MILITARYBASE);
				Industry ind2 = market.getIndustry(Industries.SPACEPORT);
				Industry ind3 = market.getIndustry(Industries.BATTLESTATION);
				if (ind1 != null && ind1.isDisrupted()) _disruption = true;
				if (ind2 != null && ind2.isDisrupted()) _disruption = true;
				if (ind3 != null && ind3.isDisrupted()) _disruption = true;
			}
				
			Global.getSector().getMemoryWithoutUpdate().set("$sdr_hitRagnarForHector", _disruption);
		}
		else if ("updateCaden".equals(action)) {
			sdr.addOrReplaceFactor(new SDRCadenFactor(), dialog);
			sdr.addOrReplaceFactor(new SDRMacarioFactor(), dialog);
		}
		else if ("doHyderDecision".equals(action)) {

			// Does Hyder swap sides? IF player got trust, then (generally) she should match the player's preference.
			
			// $global.gotHyderTrust
			
			int _toAchillesScore = 0;
			int _toHectorScore = 0;
			
			// goes with status quo if she remains uninformed
			if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_hyderRejectedFilesContact"))
				_toHectorScore++;
			
			// sticks to her guns if you insult her
			if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_insultedHyder"))
				_toHectorScore++;
			
			// if you plant the seed of doubt... she'll favour Achilles
			if(	Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_offeredToAskRaoForHyderDefection") || 
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_hyderSaidFindAnAppropriateHeir")) {
				_toAchillesScore++;
			}
			
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$gotHyderTrust")) {
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
					_toHectorScore++;
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proAchilles")) {
					_toAchillesScore++;
				}
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_antiMacario")) {
					_toAchillesScore++;
				}
			}
			else {
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proHector")) {
					_toAchillesScore++;
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdse_proAchilles")) {
					_toHectorScore++;
				}
			}
			
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_gotCadenBackHyder")) 
				_toHectorScore++;
			
			if(	Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_suggestedHyderHelpAchilles"))
				_toAchillesScore++;
			if(	Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sdr_suggestHyderBackHector"))
				_toHectorScore++;
			
			
			/*if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_agreedToHelpRao")) {
				// sdtu_offeredHyderHegemonyOut
			}
			if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_suggestedHyderHelpAchilles") ||
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_hyderDecidedToBackAchilles")) {	
			}*/
			
			int _arrestScore = 0; 
			
			// Reasons for Macario to nab her early?: 
			// let's say you need two of these - they're both mutually exclusive, so two is the max. It shows effort!
			// Actually: add the Executor thing in. It gives Macario ammo vs. Hyder!
			// !$global.gotHyderTrust
			if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_macarioBlamedHyderForExecutor")) {
				_arrestScore++;
			}
			
			if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_toldCadenMacarioBacksHyder") || 
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_toldCadenHyderBacksHyder"))
				_arrestScore++;
			
			if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_toldMacarioHyderBacksHyder") ||
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_toldMacarioHyderWantsToDefect")) 
				_arrestScore++;
			
			// if sabotaging Macario... they hey, maybe he's less effective?
			if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackInesis") || 
				Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHackRao")) {
				if( _arrestScore == 2 ) {
					Global.getSector().getMemoryWithoutUpdate().set("$sdse_macarioArrestHyderFail", true);
				}
				_arrestScore--;
			}
				

			log.info("SDR_CMD hyderDecision scores:\n"
					+ "_toAchillesScore: " + _toAchillesScore + "\n"
					+ "_toHectorScore: " + _toHectorScore + "\n"
					+ "_arrestScore: " + _arrestScore);
			
			// Macario comes first.
			if (_arrestScore > 1) {
				Global.getSector().getMemoryWithoutUpdate().set("$sdr_macarioArrestHyder", true);
				
				// still need to set her decision!
				if (_toHectorScore < _toAchillesScore) {
					Global.getSector().getMemoryWithoutUpdate().set("$sdr_hyderDecidedToBackAchilles", true);	
				}
				else {
					Global.getSector().getMemoryWithoutUpdate().set("$sdr_hyderDecidedToBackHector", true);	
				}
					
				SDResolution.triggerHyderChangesSides(); // this handles her arrest
				log.info("	RESULT: Hyder will be arrested by Macario");
			}
			else if ( _toAchillesScore >= _toHectorScore) {
				// in a tie, she goes to Achilles because it's more ~ dRaMa ~
				Global.getSector().getMemoryWithoutUpdate().set("$sdr_hyderDecidedToBackAchilles", true);	
				SDResolution.triggerHyderChangesSides();
				log.info("	RESULT: Hyder will switch sides to Achilles");
			}
			else {
				Global.getSector().getMemoryWithoutUpdate().set("$sdr_hyderDecidedToBackHector", true);	
				SDResolution.triggerHyderChangesSides();
				log.info("	RESULT: Hyder will stay and back Hector");
			}
	
		}
		else if ("testHector100".equals(action)) {
			Global.getSector().getMemoryWithoutUpdate().set("$DEV_sdrHectorPoints", 100, 0f);
			sdr.addFactor(new SDRTestHectorFactor(), dialog);
		}
		else if ("testAchilles100".equals(action)) {
			Global.getSector().getMemoryWithoutUpdate().set("$DEV_sdrAchillesPoints", 100, 0f);
			sdr.addFactor(new SDRTestAchillesFactor(), dialog);
		}
		else if ("testHector75".equals(action)) {
			Global.getSector().getMemoryWithoutUpdate().set("$DEV_sdrHectorPoints", 75, 0f);
			sdr.addFactor(new SDRTestHectorFactor(), dialog);
		}
		else if ("testAchilles75".equals(action)) {
			Global.getSector().getMemoryWithoutUpdate().set("$DEV_sdrAchillesPoints", 75, 0f);
			sdr.addFactor(new SDRTestAchillesFactor(), dialog);
		}
		else if ("testDevastation100".equals(action)) {
			sdd.addFactor(new SDDTestDevastationFactor(), dialog);
		}
		else if ("testDevastationSub100".equals(action)) {
			sdd.addFactor(new SDDTestDevastationSubFactor(), dialog);
		}
		else if ("updatePlayerFactionEffect".equals(action)) {
			if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToAchilles")) {
				sdr.addOrReplaceFactor(new SDRPlayerColonyFactor(), dialog);
			}
			else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToHector")) {
				sdr.addOrReplaceFactor(new SDRPlayerColonyFactor(), dialog);
			}
		}
		
		return false;
	}
	
	protected void doConflictStartUpdate(InteractionDialogAPI dialog, SDResolution sdr, SDDevastation sdd) {
		
		// Diktat.
		if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_setCadenOffAboutMacario") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_toldCadenMacarioBacksHyder") ||
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_toldCadenMacarioWantsToPuppet")	) {
			
			sdr.addOrReplaceFactor(new SDRCadenFactor(), dialog);
		}
		
		// TriTachyon
		sdr.addOrReplaceFactor(new SDRTriTachyonFactor(), dialog);

		// Arroyo
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotArroyoToHelp")) {
			if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels")) {
				sdd.addOrReplaceFactor(new SDRArroyoFactor(), dialog);
			} 
			else if (	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistAchilles") || 
						Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistHector")) {
				sdr.addOrReplaceFactor(new SDRArroyoFactor(), dialog);
			}
		}

		// Inesis
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAchilles")
				|| Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHectorNoMac")
				|| Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForAntiMacario")
				|| Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_gotInesisSupportForHector")) {
			sdr.addOrReplaceFactor(new SDRInesisFactor(), dialog);
		}

		// Askonia Rebels
		sdd.addOrReplaceFactor(new SDDInsurgentVSDCFactor(), dialog);
		sdr.addOrReplaceFactor(new SDRVolturnVSDCFactor(), dialog);

		sdd.addOrReplaceFactor(new SDDInsurgentARCFactor(), dialog);

		sdd.addOrReplaceFactor(new SDDInsurgentACAFactor(), dialog);
		sdr.addOrReplaceFactor(new SDRInsurgentACAFactor(), dialog);
		
		// Reset rebel leader support timers, too - don't want to have to wait a month to resupply them.
		// (Arnaud Iscare has no timer.)
		if (Global.getSector().getImportantPeople().getData(People.IMOINU_KATO) != null) {
			PersonAPI imoinu = Global.getSector().getImportantPeople().getData(People.IMOINU_KATO).getPerson();
			imoinu.getMemoryWithoutUpdate().unset("$gaveSupportRecently");
		}
		
		if (Global.getSector().getImportantPeople().getData(People.STANDFAST) != null) {
			PersonAPI imoinu = Global.getSector().getImportantPeople().getData(People.STANDFAST).getPerson();
			imoinu.getMemoryWithoutUpdate().unset("$gaveSupportRecently");
		}

		// Hegemony
		sdd.addFactor(new SDDRaoBacksAntisFactor(), dialog);

		// PL
		if(	
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstSiyavong") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstHorus") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstFortuna") || 
			Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_talkFirstReynard") ||
			Global.getSector().getPlayerMemoryWithoutUpdate().getString("$fcm_faction") == Factions.PERSEAN) {
			// If the Player is AT ALL involved with the Persean League, then Reynard pulls his sabotage.
			
			Global.getSector().getMemoryWithoutUpdate().set("$sdr_perseanInvolved", true);
			Global.getSector().getMemoryWithoutUpdate().set("$sdr_hannanBlockingKato", true);
			
		}
				
		sdr.addFactor(new SDRGensKatoFactor(), dialog);
	
		// player faction
		if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$pledgedColonySupportToAchilles")) {
			sdr.addFactor(new SDRPlayerColonyFactor(), dialog);
		}
	}
	
	protected void addACAStartUnrest() {
		// it's going to be a bit less than the other factions.
		int _amount = 1;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedACAfinished"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHack"))
			_amount++;
			
		addSDUnrest(_amount, "ACA insurgent activity");
	}
	
	protected void addARCStartUnrest() {
		int _amount = 1;
		
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveFuelARC"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesARC"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveWeaponsARC"))
			_amount++;
		if (Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$imoinuCalledComrade"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHack"))
			_amount++;
		
		addSDUnrest(_amount, "ARC insurgent activity");
	}
	
	protected void addVSDCStartUnrest() {
		int _amount = 1;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveWeaponsVSDC"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveCreditsVSDC"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerGaveSuppliesVSDC"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_playerHelpedVSDCfinished"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdr_arroyoAssistRebels"))
			_amount++;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdse_uploadedAskoniaCommsHack"))
			_amount++;
		
		addSDUnrest(_amount, "VSDC insurgent activity");
	}
	
	// For adding unrest after the askoniaStandoff start.
	// Or at the start! Whatever works.
	protected void addSDUnrest(int _amount, String _reason) {

		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		if (volturn != null)
			RecentUnrest.get(volturn).add(_amount, _reason);

		MarketAPI cruor = Global.getSector().getEconomy().getMarket("cruor");
		if (cruor != null)
			RecentUnrest.get(cruor).add(_amount, _reason);

		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		if (sindria != null)
			RecentUnrest.get(sindria).add(_amount, _reason);
	}
}
