package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * PersonIsDefaultMarketPost
 * Returns true if the queried person holds one of the default market posts, otherwise false.
 * If called without an argument for person's ID, will query the active person.
 */
public class PersonIsDefaultMarketPost extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		PersonAPI person;

		if(params.size() > 0) {
			person = People.getPerson(params.get(0).getString(memoryMap));
		}
		else {
			SectorEntityToken entity = dialog.getInteractionTarget();
			if (entity.getActivePerson() == null) return false;
			person = entity.getActivePerson();
		}
		
		if (person != null && person.getPostId() != null) {
			if(	person.getPostId().equals(Ranks.POST_ADMINISTRATOR) || 
				person.getPostId().equals(Ranks.POST_SUPPLY_OFFICER) ||
				person.getPostId().equals(Ranks.POST_BASE_COMMANDER) ||
				person.getPostId().equals(Ranks.POST_STATION_COMMANDER) || 
				person.getPostId().equals(Ranks.POST_PORTMASTER) ) {
				
				return true;
			}
		}
		
		return false;
	}
	

}
