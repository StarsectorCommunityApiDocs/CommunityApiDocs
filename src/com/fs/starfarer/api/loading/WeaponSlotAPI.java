package com.fs.starfarer.api.loading;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public interface WeaponSlotAPI {
	boolean isHardpoint();
	boolean isTurret();
	boolean isHidden();
	boolean isSystemSlot();
	boolean isBuiltIn();
	boolean isDecorative();
	
	String getId();
	WeaponAPI.WeaponType getWeaponType();
	WeaponAPI.WeaponSize getSlotSize();
	float getArc();
	void setArc(float arc);
	
	/**
	 * @return center of the arc, with the ship facing 0 degrees (to the right).
	 */
	float getAngle();

	/**
	 * Absolute coordinates of the weapon slot.
	 * @param ship
	 * @return
	 */
	Vector2f computePosition(CombatEntityAPI ship);
	boolean isStationModule();
	
	boolean weaponFits(WeaponSpecAPI spec);
	
	void setAngle(float angle);
	Vector2f getLocation();
	
	float getRenderOrderMod();
	void setRenderOrderMod(float renderOrderMod);
	float computeMidArcAngle(ShipAPI ship);
	List<Vector2f> getLaunchPointOffsets();
	//WeaponSlotAPI clone();
	boolean isWeaponSlot();
	//void setSlotSize(WeaponSize slotSize);
	//void setId(String id);
	//void setNode(String nodeId, Vector2f position);
	
}
