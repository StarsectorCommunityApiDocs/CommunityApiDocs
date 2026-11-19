package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CoreUITabId;

public interface CoreUITabListener {
	void reportAboutToOpenCoreTab(CoreUITabId tab, Object param);
	default void reportOpenedCoreTab(CoreUITabId tab, Object param) {};
}
