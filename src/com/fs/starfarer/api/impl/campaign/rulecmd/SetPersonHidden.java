package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetPersonHidden set person hidden boolean by character id
 */
public class SetPersonHidden extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		PersonAPI person;
		boolean hidden;
		
		// if only the bool parameter is given, assume it's the conversation target... if they exist.
		if (params.size() == 1) {
			SectorEntityToken entity = dialog.getInteractionTarget();
			if (entity.getActivePerson() == null) return false;
			person = entity.getActivePerson();
			hidden = params.get(0).getBoolean(memoryMap);
		}
		// if a person id is given (so, 2 params), we shall follow The Old Ways
		// if you wanted to just pass in the person ID. The person has to have been
		// added to ImportantPeople at some point (as the people defined in People are)
		else {
			String id = params.get(0).getString(memoryMap);
			person = Global.getSector().getImportantPeople().getPerson(id);
			hidden = params.get(1).getBoolean(memoryMap);
		}
		
		//VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
		//PersonAPI person = (PersonAPI) var.memory.get(var.name);
		
		if (person == null || person.getMarket() == null) return false;
		if (person.getMarket().getCommDirectory() == null) return false;
		if (person.getMarket().getCommDirectory().getEntryForPerson(person.getId()) == null) return false;

		person.getMarket().getCommDirectory().getEntryForPerson(person.getId()).setHidden(hidden);
		return true;
		
	}
}
