package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel.ContactState;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * HasContactFromFaction $factionId
 */
public class HasContactFromFaction extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String id = params.get(0).getString(memoryMap);
		FactionAPI faction = Global.getSector().getFaction(id);
		Boolean useVIPBlacklist = false;
		
		// Sometimes we just want a "normal" contact, not a major character who can incidentally become a contact.
		if (params.size() > 1) {
			useVIPBlacklist = params.get(1).getBoolean(memoryMap);
		}
		
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			ContactState state = ((ContactIntel)intel).getState();
			if (state != ContactState.NON_PRIORITY && state != ContactState.PRIORITY) continue;
			PersonAPI person = ((ContactIntel)intel).getPerson();
			if (person == null) continue;
			if (useVIPBlacklist && 
					(	person.getId().equals(People.REYNARD_HANNAN) || 
						person.getId().equals(People.HORUS_YARIBAY) || 
						person.getId().equals(People.ARROYO) || 
						person.getId().equals(People.FARO_INESIS) || 
						person.getId().equals(People.RAO) ||
						person.getId().equals(People.GLAMOR_ROTANEV))) continue;
			if (person.getFaction() == faction) {
				return true;
			}
		}

		return false;
		
	}
}














