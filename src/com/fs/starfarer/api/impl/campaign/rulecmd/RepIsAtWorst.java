package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * RepIsAtWorst <factionId> RepLevel
 */
public class RepIsAtWorst extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		// if only parameter given is the repLevelStr, then apply it to active person.
		// If no active person, apply to entity's parent faction.
		// If no parent faction, return false.		 
		if(params.size() == 1) {
			SectorEntityToken entity = dialog.getInteractionTarget();
			if (entity == null) return false;
			
			String repLevelStr = params.get(0).getString(memoryMap);
			RepLevel repLevel = RepLevel.valueOf(repLevelStr);
			
			if (entity.getActivePerson() != null) {
				PersonAPI person = entity.getActivePerson();
				return person.getRelToPlayer().isAtWorst(repLevel);
			} else if(entity.getFaction() != null) {
				String factionId = entity.getFaction().getId();
				FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
				return player.isAtWorst(factionId, repLevel);
			}
			return false;
		}
		
		String factionId = params.get(0).getString(memoryMap);
		String repLevelStr = params.get(1).getString(memoryMap);
		RepLevel repLevel = RepLevel.valueOf(repLevelStr);
		
		PersonAPI person = People.getPerson(factionId);
		if (person != null) {
			return person.getRelToPlayer().isAtWorst(repLevel);
		}
		
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		return player.isAtWorst(factionId, repLevel);
	}
	

}
