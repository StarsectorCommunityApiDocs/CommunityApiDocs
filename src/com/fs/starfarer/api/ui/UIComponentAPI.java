package com.fs.starfarer.api.ui;

public interface UIComponentAPI extends UILogicAPI {
	PositionAPI getPosition();

	void render(float alphaMult);
	//void processInput(List<InputEventAPI> events);
	void advance(float amount);

	void setOpacity(float opacity);
	float getOpacity();

	void focusLost();

	float getWidth();
	float getHeight();
	float getX();
	float getY();
	
	UIPanelAPI getParent();
	void setParent2(UIPanelAPI parent);
	void setPosition2(PositionAPI position);
}
