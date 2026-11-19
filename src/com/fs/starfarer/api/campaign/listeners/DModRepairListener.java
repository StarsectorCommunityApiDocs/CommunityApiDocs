package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface DModRepairListener {
	void reportDModsRepaired(FleetMemberAPI member, ShipVariantAPI variant, int parts);
}
