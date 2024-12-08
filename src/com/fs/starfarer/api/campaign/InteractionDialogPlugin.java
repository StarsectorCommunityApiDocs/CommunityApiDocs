package com.fs.starfarer.api.campaign;

import java.util.Map;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

/**
 * You need to create a class that implements this interface to create your own dialogs.
 * This can be a dialog shown before a fleet engagement, a custom GUI or anything else.
 *
 * To actually show the dialog, you can use Global.getSector().getCampaignUI().showInteractionDialog()
 *
 * If you want to replace the FleetInteractionDialog for a certain fleet encounter, you need to create
 * a {@link com.fs.starfarer.api.campaign.BaseCampaignPlugin} and implement the logic in the pickInteractionDialogPlugin
 * method.
 *
 * <p>Example
 * <pre>
 *     {@code
 *     		// This is a simple dialog with some text, an image and an option.
 *     		// Note: For simplicity sake, error handling and null-checking is omitted in this example
 *     		class MyDialog implements InteractionDialogPlugin{
 *     			interface ExecutableOptionData{
 *     			 	void execute();
 *     			}
 *     			private InteractionDialogAPI dialog = null;
 *     		 	&#64;Override
 *     		 	void init(InteractionDialogAPI dialog){
 *     		     	this.dialog = dialog;
 *     		     	dialog.getTextPanel().addParagraph("Some text")
 *     		     	dialog.getOptionPanel().addOption(
 *     		     		"OptionName",
 *     		     		new ExecutableOptionData(){
 *     		     		 	&#64;Override
 *     		     		 	void execute(){
 *     		     		 	  // implementation of what the option should do when clicked
 *     		     		 	  // For instance, for a leave option:
 *     		     		 	  dialog.dismiss();
 *     		     		 	  // If your GUI e.g. moves to another stage after this, you should clear the options and
 *     		     		 	  // create new options in here. It might also make sense to move this ExecutableOptionData
 *     		     		 	  // to another file, if the implementation gets too long.
 *     		     		 	}
 *     		     		}
 *     		     	);
 *     		     	String spriteName = Global.getSettings().getSpriteName("spriteCategory", "mySpriteName");
 *     		     	dialog.getVisualPanel().showImageVisual(
 *     		     		spriteName,
 *     		     		Global.getSettings().getSprite(spriteName).width
 *     		     		Global.getSettings().getSprite(spriteName).height
 *     		     	);
 *     		 	}
 *     		 	&#64;Override
 *     		 	void optionSelected(String optionText, Object optionData){
 *     		 	    ExecutableOptionData option = (ExecutableOptionData)optionData;
 *     		 	    if(option == null){ return; }
 *     		 	    option.execute();
 *     		 	}
 *
 *     		 	// Override the remaining methods (Ctrl+O in IntelliJ), but leave their implementation empty if their
 *     		 	// return type is void, or return null otherwise.
 *
 *     		}
 *     }
 *
 * </pre>
 *
 */
public interface InteractionDialogPlugin {
	/**
	 * Initialize your GUI in here, by populating options, texts and images	 *
	 *
	 * @param dialog entry point for interacting with the dialog. You should probably store this in a member variable
	 *
	 */
	void init(InteractionDialogAPI dialog);

	/**
	 * This gets called when the player clicks on an option. Execute the logic for the selected option in here
	 * @param optionText the name of the option, i.e. the first argument passed to addOption
	 * @param optionData the data/identifier of the selected option. Cast this to the correct type. Don't forget to
	 *                   null-check the result of the cast.
	 */
	void optionSelected(String optionText, Object optionData);

	/**
	 * Get's called when the player hovers over an option. Can usually be left empty.
	 * @param optionText
	 * @param optionData
	 */
	void optionMousedOver(String optionText, Object optionData);

	/**
	 * Get's called every frame. Can usually be left empty.
	 * @param amount
	 */
	void advance(float amount);

	/**
	 * Get's called after a fleet battle connected to this dialog ends. Can be left empty if the dialog
	 * you are implementing isn't related to a fleet battle.
	 *
	 * This documentation is incomplete.
	 *
	 * @param battleResult
	 */
	void backFromEngagement(EngagementResultAPI battleResult);

	/**
	 * Usually, simply returning null is fine
	 *
	 * @return the context, e.g. campaign entity or fleet or other thing that was interacted with to spawn this dialog	 *
	 */
	Object getContext();

	/**
	 * Usually, simply returning null or an empty map is fine.
	 *
	 * @return a map that maps string keys to memory entries. Only relevant if you want to interact with rulescmd (I think)
	 */
	Map<String, MemoryAPI> getMemoryMap();
}
