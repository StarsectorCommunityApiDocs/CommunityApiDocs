package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;

public class ShipAIConfig implements Cloneable {

	
	@Override
	public ShipAIConfig clone() {
		try {
			return (ShipAIConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			return null; // should not happen
		}
	}

	public boolean alwaysStrafeOffensively = false;
	public boolean backingOffWhileNotVentingAllowed = true;
	public boolean turnToFaceWithUndamagedArmor = true;
	public boolean forceAllowTurnToUndamagedArmorIncapOnly = false;
	
	public boolean burnDriveIgnoreEnemies = false;
	
	public String personalityOverride = null;

	public ShipAIConfig() {
		this(null);
	}
	
	public ShipAIConfig(ShipAPI shipIfAny) {
		if (shipIfAny != null && shipIfAny.getHullSpec().getHints().contains(ShipTypeHints.DO_NOT_TURN_TO_ARMOR)) {
			turnToFaceWithUndamagedArmor = false;
			forceAllowTurnToUndamagedArmorIncapOnly = true;
		}
	}
	
	public void copyFrom(ShipAIConfig other) {
		if (other == null) {
			return;
		}
		alwaysStrafeOffensively = other.alwaysStrafeOffensively;
		backingOffWhileNotVentingAllowed = other.backingOffWhileNotVentingAllowed;
		turnToFaceWithUndamagedArmor = other.turnToFaceWithUndamagedArmor;
		burnDriveIgnoreEnemies = other.burnDriveIgnoreEnemies;
		personalityOverride = other.personalityOverride;
	}
}
