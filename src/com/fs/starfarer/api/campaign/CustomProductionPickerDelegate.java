package com.fs.starfarer.api.campaign;

import java.util.Set;


public interface CustomProductionPickerDelegate {

	public Set<String> getAvailableShipHulls();
	public Set<String> getAvailableWeapons();
	public Set<String> getAvailableFighters();
	
	public float getCostMult();
	public float getMaximumValue();
	
	public boolean withQuantityLimits();
	
	public void notifyProductionSelected(FactionProductionAPI production);
	
	
	public String getWeaponColumnNameOverride();
	public String getNoMatchingBlueprintsLabelOverride();
	public String getMaximumOrderValueLabelOverride();
	public String getCurrentOrderValueLabelOverride();
	public String getCustomOrderLabelOverride();
	public String getNoProductionOrdersLabelOverride();
	public String getItemGoesOverMaxValueStringOverride();
	
	
	/**
	 * Should work for ships and fighters now, in addition to weapons.
	 */
	public boolean isUseCreditSign();
	/**
	 * Should work for ships and fighters now, in addition to weapons.
	 */
	public int getCostOverride(Object item);
}
