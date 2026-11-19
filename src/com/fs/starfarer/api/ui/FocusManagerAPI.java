package com.fs.starfarer.api.ui;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

public interface FocusManagerAPI {
	
	public void processInput(List<InputEventAPI> events);
	public List<UILogicAPI> getFocusOwnerStack();
	public void setFocusOwnerStack(List<UILogicAPI> focusOwnerStack);

	public boolean isSuspendTooltips();
	public void setSuspendTooltips(boolean suspendTooltips);
	
	public void grabFocusForAllEvents(UILogicAPI grabber);
	
	public void releaseFocusForAllEvents(UILogicAPI grabber);
	public void releaseFocusForAllEventsIfOwner(UILogicAPI grabber);
	public void pushFocusOwner(UILogicAPI owner);
	
	public void popFocusOwner(UILogicAPI owner);
	public void removeFromFocusOwnerStack(UILogicAPI owner);
	public void popFocusOwnerIfOwner(UILogicAPI owner);
	public void clear();
	public UILogicAPI getAllEventsFocusOwner();
}






