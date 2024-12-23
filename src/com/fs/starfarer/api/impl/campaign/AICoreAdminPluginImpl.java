package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;

/**
 * 
 */
public class AICoreAdminPluginImpl implements AICoreAdminPlugin {
	
	public PersonAPI createPerson(String aiCoreId, String factionId, long seed) {
		PersonAPI person = Global.getFactory().createPerson();
		person.setFaction(factionId);
		person.setAICoreId(aiCoreId);
		person.setName(new FullName("Alpha Core", "", Gender.ANY));
		person.setPortraitSprite("graphics/portraits/portrait_ai2b.png");
		
		person.setRankId(null);
		person.setPostId(Ranks.POST_ADMINISTRATOR);
		
//		person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
//		person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
		person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
		person.getStats().setSkillLevel(Skills.HYPERCOGNITION, 1);
		
		
		return person;
	}

	
}
