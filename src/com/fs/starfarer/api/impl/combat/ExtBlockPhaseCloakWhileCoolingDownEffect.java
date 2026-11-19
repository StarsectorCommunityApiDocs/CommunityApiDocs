package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class ExtBlockPhaseCloakWhileCoolingDownEffect implements EveryFrameWeaponEffectPlugin {

	protected static Object STATUSKEY1 = new Object();
	
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused()) return;
		
		ShipAPI ship = weapon.getShip();
		if (ship == null || ship.isHulk()) return;

		if (!Misc.isExtended() || !Global.getSettings().getBoolean("ext_certainWeaponCooldownsBlockPhaseCloak")) {
			return;
		}
		
		if (weapon.getCooldownRemaining() > 0 && ship.isReallyPhaseShip() && !ship.isPhased()) {
			ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
			if (ship.getSystem() != null && ship.getSystem().getSpecAPI() != null) {
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
						ship.getSystem().getSpecAPI().getIconSpriteName(), "recharge interference", "phase cloak offline", true);
			}
		}
		
	}
}
