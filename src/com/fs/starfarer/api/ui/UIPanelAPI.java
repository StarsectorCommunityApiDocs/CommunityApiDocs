package com.fs.starfarer.api.ui;

import java.util.List;

import com.fs.starfarer.api.util.FaderUtil;

public interface UIPanelAPI extends UIComponentAPI {

	PositionAPI addComponent(UIComponentAPI custom);
	void removeComponent(UIComponentAPI component);
	void bringComponentToTop(UIComponentAPI c);
	void sendToBottom(UIComponentAPI c);
	
	
	/**
	 * The only method that accepts an arbitrary, non-core implementation of UIComponentAPI.
	 * Will wrap it with an internal implementation that delegate functionality to the passed-in component.
	 * Returns the wrapper; if need to call removeComponent, pass in the wrapper returned by this method. 
	 * @param component
	 * @param fader
	 * @return
	 */
	UIComponentAPI wrapAndAdd(UIComponentAPI component, FaderUtil fader);
	
	/**
	 * Intended for use with custom components wrapped by UIPanelAPI.wrapAndAdd().
	 * @return
	 */
	PositionAPI createPosition();
	void clearChildren();
	List<UIComponentAPI> getChildrenCopy2();
}
