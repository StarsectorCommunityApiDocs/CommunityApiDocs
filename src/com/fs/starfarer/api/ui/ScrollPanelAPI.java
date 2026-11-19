package com.fs.starfarer.api.ui;

import java.awt.Color;

public interface ScrollPanelAPI extends UIPanelAPI {

	float getXOffset();
	float getYOffset();
	void setXOffset(float xOffset);
	void setYOffset(float yOffset);
	
	void setOffset(float xOffset, float yOffset);
	void clampOffset();
	void resetOffset();

	boolean isLeftScrollbar();
	void setLeftScrollbar(boolean bool);
	UIPanelAPI getContentContainer();
	void setContentSize(float width, float height);

	void scrollToBottom();
	void scrollToY(float y);
	boolean isScrollingToBottom();
	boolean isScrolling();
	void setShowScrollbars(boolean bool);
	void setUseMWheel(boolean bool);
	void setRoundOffset(boolean bool);
	void setVisiblityPad(float pad);
	void setShowGrid(float width, float height, boolean showGrid);
	void updateGrid(float rowCount, float columnCount);
	Color getDarkColor();
	void setDarkColor(Color color);
	void setUseSimpleShadows(boolean bool);
	Color getScrollWidgetColor();
	void setScrollWidgetColor(Color color);
	Color getScrollRailColor();
	void setScrollRailColor(Color color);
	float getMaxShadowHeight();
	void setMaxShadowHeight(float height);
	void ensureVisible(UIComponentAPI comp);
	void saveVerticalScrollState();
	void restoreVerticalScrollState();	

}
