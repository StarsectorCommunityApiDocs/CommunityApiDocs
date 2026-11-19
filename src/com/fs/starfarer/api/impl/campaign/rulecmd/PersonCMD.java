package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	PersonCMD <action> <parameters>
 *  First parameter is the person id. If not person id is given, will use current active person - if any. -dgb
 */
public class PersonCMD extends BaseCommandPlugin {
	
	//public static Logger log = Logger.getLogger(PersonCMD.class);
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		if(params == null || params.size() == 0) return false;
		
		/* examples calls to handle:
		 * PersonCMD makeUnimportant gargoyle $reasonFlag
		 * PersonCMD makeUnimportant gargoyle
		 * PersonCMD makeUnimportant
		 * PersonCMD makeUnimportant $reasonFlag
		 * 
		 * (also with "makeImportant")
		 * 
		 * 1. attempt to get person from dialog
		 * 2. even if successful, check if first param is a person!
		 * 3. if no person found during these steps, then Oops!
		 * 
		 * next param (which can be param 0) is the action
		 * next param is whatever else ie. reason. This is optional, of course.
		 */
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
		
		PersonAPI person;
		String action = params.get(0).getString(memoryMap); // action is always param 0
		int paramsUsed = 1;
		SectorEntityToken entity = dialog.getInteractionTarget();
		
		if (entity.getActivePerson() == null || params.size() > 1) {
			String id = params.get(paramsUsed).getString(memoryMap);
			
			// if no person found in dialog, and not person found via param, then what are you even doing here??
			if(Global.getSector().getImportantPeople().getPerson(id) == null) {
				System.out.println("could not find person id: " + id );
				return false;
			}
				
			person = Global.getSector().getImportantPeople().getPerson(id);
			paramsUsed++;
		}
		else {
			person = entity.getActivePerson();
		} 

		if(person == null) return false;
		
		if ("makeUnimportant".equals(action)) {
			if(params.size() > paramsUsed ) {
				String reason = params.get(paramsUsed).getString(memoryMap);
				Misc.makeUnimportant(person, reason);
			}
			else {
				Misc.makeUnimportant(person, "generic");
			}
			return true;
		}
		else if ("makeImportant".equals(action)) {
			if(params.size() > paramsUsed ) {
				String reason = params.get(paramsUsed).getString(memoryMap);
				Misc.makeImportant(person, reason);
			}
			else {
				Misc.makeImportant(person, "generic");
			}
			return true;
		}
		else if ("isContact".equals(action)) {
			
			// check if a person id was given as a parameter
			// if so, we use that instead of the person we're in dialog with
			if(paramsUsed == 1) {
				String id = params.get(paramsUsed).getString(memoryMap);
				if(Global.getSector().getImportantPeople().getPerson(id) != null) {
					person = Global.getSector().getImportantPeople().getPerson(id);
				}
			}

			return ContactIntel.playerHasContact(person, false);
		}
		
		return false;
	}

	/*protected void makeUnimportant(InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		ShipVariantAPI v = Global.getSettings().getVariant("colossus_Standard").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
		
	}*/
}
