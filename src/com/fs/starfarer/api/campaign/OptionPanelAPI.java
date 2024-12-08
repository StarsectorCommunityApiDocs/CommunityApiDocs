package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.ValueDisplayMode;


public interface OptionPanelAPI {
	
	public static interface OptionTooltipCreator {
		void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText);
	}
	
	
	void setTooltipHighlights(Object data, String ... highlights);
	void setTooltipHighlightColors(Object data, Color ... colors);
	
	void clearOptions();

	/**
	 * Adds an additional selectable option to the dialog.
	 * Options are appended in the order that this method is called.
	 *
	 * It usually makes sense to call clearOptions() before adding options to ensure that
	 * options don't pile up.
	 *
	 * @param text will be displayed in the option list
	 * @param data can be anything. This is used to identify the option, i.e. other methods that modify options need you
	 *             to pass the exact same thing you pass here. People often times use Strings or enums as option data.
	 *             Alternatively, you can use objects that implement an interface with e.g. an execute-method, to make the
	 *             option data directly contain the logic that should be executed when the option is selected by the player.
	 *             The value passed here is available in the optionSelected method of {@link com.fs.starfarer.api.campaign.InteractionDialogPlugin}.
	 *             Cast the optionData parameter to the type you used here to evaluate it.
	 */
	void addOption(String text, Object data);
	void addOption(String text, Object data, String tooltip);
	void addOption(String text, Object data, Color color, String tooltip);
	
	/**
	 * Sets an alternate shortcut that works in addition to the number key.
	 * @param data
	 * @param code constant from org.lwjgl.input.Keyboard
	 * @param ctrl whether Control needs to be down to trigger this shortcut.
	 * @param alt whether Alt needs to be down to trigger this shortcut.
	 * @param shift whether Shift needs to be down to trigger this shortcut.
	 * @param putLast ignored
	 */
	void setShortcut(Object data, int code, boolean ctrl, boolean alt, boolean shift, boolean putLast);
	
	/**
	 * Only works for options, not selectors.
	 * @param data
	 * @param enabled
	 */
	void setEnabled(Object data, boolean enabled);
	
	
	void setTooltip(Object data, String tooltipText);
	
	/**
	 * A user-adjustable bar useful for picking a value from a range.
	 * @param text Text to show above the bar.
	 * @param data ID of the bar, used to get/set its state.
	 * @param color Bar color.
	 * @param width Width in pixels, including value label on the right.
	 * @param maxValueWidth Width of the value label on the right.
	 * @param minValue Minimum value (when bar is all the way to the left).
	 * @param maxValue Maximum value (bar all the way to the right).
	 * @param mode How to display the value - as a percentage, X/Y, etc.
	 * @param tooltip Tooltip text. Can be null.
	 */
	void addSelector(String text, Object data, Color color,
					 float width, float maxValueWidth, float minValue, float maxValue,
					 ValueDisplayMode mode, String tooltip);
	
	boolean hasSelector(Object data);
	
	void setSelectorValue(Object data, float value);
	
	float getSelectorValue(Object data);
	float getMinSelectorValue(Object data);
	float getMaxSelectorValue(Object data);
	boolean hasOptions();
	
	List getSavedOptionList();
	void restoreSavedOptions(List list);
	
	void addOptionConfirmation(Object optionId, String text, String yes, String no);
	boolean hasOption(Object data);

	/**
	 * This is what you want to use to add a story point option to a dialog.
	 * Using this method will handle everything a story option needs (story point cost, confirmation, ...), though
	 * you will need to set the color of the option manually.
	 * The alternative is to use use {@link com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption}.
	 *
	 * <p>Example:
	 * <pre>
	 * {@code
	 *   // given an object of type OptionPanelAPI named options
	 *	options.addOption("My story option [1SP, 0% XP]", "myData (can be any type)", Misc.getStoryOptionColor(), "tooltip (can be null)");
	 *	options.addOptionConfirmation(
	 *		"myData (can be any type)",
	 *		new BaseStoryPointActionDelegate() {
	 *			{@literal @}Override
	 *			public String getLogText(){ return "this appears in the log"; }
	 *			{@literal @}Override
	 *			public int getRequiredStoryPoints(){ return 1; }
	 *			{@literal @}Override
	 *			public float getBonusXPFraction(){ return 0f; } // a value between 0 and 1. 1 Means 100% bonus XP
	 *			{@literal @}Override
	 *			public boolean withSPInfo(){ return true; }
	 *			{@literal @}Override
	 *			public String getTitle(){ return "title to display in confirmation box"; }
	 *			{@literal @}Override
	 *			public void createDescription(TooltipMakerAPI info){ info.addPara("description text in confirmation box", 1f); }
	 *		}
	 *	);
	 * }
	 * </pre>
	 *
	 * @param data must be the same thing you passed to addOption
	 * @param confirmDelegate an object implementing the StoryPointActionDelegate interface. cf. example
	 */
	void addOptionConfirmation(Object data, StoryPointActionDelegate confirmDelegate);
	void addOptionTooltipAppender(Object data, OptionTooltipCreator optionTooltipCreator);
	void setOptionText(String text, Object data);
	boolean hasOptionTooltipAppender(Object data);
	boolean optionHasConfirmDelegate(Object data);
	Object getOptionDataBeingConfirmed();
	void removeOption(Object data);

	/**
	 * Calling this will not do everything neccessary to turn an option into a story point option.
	 * Use addOptionConfirmation instead. I believe this method is only used to modify existing story options.
	 * This method needs additional documentation, as I am not 100% sure what its purpose is.
	 *
	 * @param data identifier for the option
	 * @param params
	 * @param delegate
	 */
	void setStoryOptionParams(Object data, StoryOptionParams params, StoryPointActionDelegate delegate);
}
