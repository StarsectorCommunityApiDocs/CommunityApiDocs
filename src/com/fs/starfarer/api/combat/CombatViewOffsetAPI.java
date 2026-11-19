package com.fs.starfarer.api.combat;

public interface CombatViewOffsetAPI {

	void setDest(float x, float y);
	void setCurr(float x, float y);
	void setAlwaysFast(boolean alwaysFast);
	void doNotMove();
	float getPanMult();
	float getX();
	float getY();
	void advance(float prevElapsed, float zoomFactor);
	void nudge();

}
