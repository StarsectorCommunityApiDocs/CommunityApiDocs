package com.fs.starfarer.api.combat;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.combat.threat.ThreatCombatStrategyAI;

public class AdmiralAIConfig {

	public static interface FleetMemberFilter {
		boolean matches(FleetMemberAPI member);
	}
	
	/**
	 * Key: base hull id
	 * Value: maximum number to deploy at a time, not guaranteed to be respected
	 */
	public Map<String, Integer> softDeploymentLimits = new LinkedHashMap<>();
	public Map<FleetMemberFilter, Integer> softDeploymentLimitFilters = new LinkedHashMap<>();
	
	public AdmiralAIConfig() {
		softDeploymentLimits.put("fabricator_unit", ThreatCombatStrategyAI.MAX_FABRICATORS);
	}
	
	
}
