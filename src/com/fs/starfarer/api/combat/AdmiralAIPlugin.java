package com.fs.starfarer.api.combat;

public interface AdmiralAIPlugin {

	void preCombat();
	void advance(float amount);
	default void setNoOrders(boolean noOrders) {
		
	}

}
