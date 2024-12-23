package com.fs.starfarer.api.impl.campaign.tutorial;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;

public class TutorialLevelUpDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		CONT1,
		CONT2,
		CONT3,
		CONT4,
		;
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		//visual.showImagePortion("illustrations", "jump_point_hyper", 640, 400, 0, 0, 480, 300);
		visual.showFleetInfo("Your fleet", playerFleet, null, null);
	
		//dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(option);
		}
		
		
		String control = Global.getSettings().getControlStringForEnumName("CORE_CHARACTER");
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("You've gained a level!");
			textPanel.addPara("You gain %s skill point with each level-up, " +
					"and an additional point at the start of the campaign.",
					Misc.getHighlightColor(), "1");
			
			textPanel.addPara("You also gain " + Misc.STORY + " points as you gain experience; these can be used to " +
					"take exceptional actions - to \"make your own story\", in a way. The tutorial will introduce " +
					"using these a bit later.",
					Misc.getStoryOptionColor(), Misc.STORY + " points");
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			textPanel.addParagraph("Skill points are used to learn skills. Skills are arranged in four aptitudes - " +
					"Combat, Leadership, Technology, and Industry. Each aptitude has a number of tiers, you advance to the higher tiers by picking skills from the lower ones.");
			
			textPanel.addPara("Skills that affect your piloted ship - all Combat skills, and a few skills in other " +
					"aptitudes - can be made \"elite\" at the cost of a " + Misc.STORY + " point.",
					Misc.getStoryOptionColor(), Misc.STORY + " point");
			
			String max = "" + (int) Global.getSettings().getLevelupPlugin().getMaxLevel();
			textPanel.addPara("The maximum level you can reach is %s.",
					Misc.getHighlightColor(), max);
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT3, null);
			break;
		case CONT2:
//			textPanel.addParagraph("Each aptitude has skills ");
//			options.clearOptions();
//			options.addOption("Continue", OptionId.CONT3, null);
			break;
		case CONT3:
			textPanel.addPara("Press %s to open the character tab and consider your options. You don't " +
					"have to actually spend the points now if you don't want to.",
					Misc.getHighlightColor(), control);
			
			options.clearOptions();
			options.addOption("Finish", OptionId.CONT4, null);
			break;
		case CONT4:
			Global.getSector().setPaused(false);
			dialog.dismiss();
			break;
		}
	}
	

	
	
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
}



