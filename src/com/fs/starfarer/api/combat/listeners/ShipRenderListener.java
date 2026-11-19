package com.fs.starfarer.api.combat.listeners;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;

public interface ShipRenderListener {
	public void reportRenderedShipSprite(ShipAPI ship, CombatEngineLayers layer, ViewportAPI viewport, boolean withModules);
	public void reportRenderedDHullOverlay(ShipAPI ship, CombatEngineLayers layer, ViewportAPI viewport, boolean withModules);
	public void reportRenderedExtraOverlay(ShipAPI ship, CombatEngineLayers layer, ViewportAPI viewport, boolean withModules);
}
