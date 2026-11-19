package com.fs.starfarer.api.combat;

import java.util.List;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface AdmiralAIPlugin {

	public static interface AdmiralPluginDelegate {
		void doAdditionalInitialDeployment();
		boolean allowedToDeploy(List<FleetMemberAPI> chosenSoFar, FleetMemberAPI member);
	}
	
	void preCombat();
	void advance(float amount);
	default void setNoOrders(boolean noOrders) {}
	default void setDelegate(AdmiralPluginDelegate delegate) {}
	AdmiralAIConfig getConfig();
}

