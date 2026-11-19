package com.fs.starfarer.api.ui;

import java.awt.Color;

public interface SliderAPI {
	Color getBonusColor();
	void setBonusColor(Color color);
	float getBonusAmount();
	void setBonusAmount(float amount);
	void setShowDecimalForValueOnlyMode(boolean bool);
	int getNumSubivisions();
	void setNumSubivisions(int num);
	void setFlashOnOverflowFraction(float fraction);
	boolean isShowNoText();
	void setShowNoText(boolean bool);
	void setClampCurrToMax(boolean bool);
	void setShowValueOnly(boolean bool);
	void setPotentialDecreaseAmount(float amount);
	float getShowNotchOnIfBelowProgress();
	void setShowNotchOnIfBelowProgress(float progress);
	void setHighlightOnMouseover(boolean bool);
	LabelAPI getValue();
	void setText(String text);
	void setUserAdjustable(boolean adjustable);
	
	/**
	 * From rangeMin to rangeMax, inclusive.
	 */
	float getProgress();
	void setProgress(float progress);
	void setLineUpTextOnCenter(boolean bool, float xOffset);
	void setShowPercent(boolean bool);
	void setShowPercentAndTitle(boolean bool);
	
	/**
	 * Default value is 100.
	 */
	void setScrollSpeed(float speed);
	boolean isShowLabelOnly();
	void setShowLabelOnly(boolean bool);
//	FaderAPI getBarHighlightFader();
//	FaderAPI getHighlight();
	Color getBarColor();
	void setBarColor(Color color);
	void setBarColorOverflow(Color color);
	
	float getXCoordinateForProgressValue(float progress);
	void setHighlightBrightnessOverride(float override);
	/**
	 * Default is false.
	 */
	void setRoundBarValue(boolean bool);
	
	/**
	 * Default is 1.
	 */
	int getRoundingIncrement();
	void setRoundingIncrement(int increment);
	boolean isShowAdjustableIndicator();
	void setShowAdjustableIndicator(boolean bool);
	Color getWidgetColor();
	void setWidgetColor(Color color);
	
	/** Invalidates GLListToken if necessary, and snaps the visible progress value to the current one.
	 *  Makes sure the bar is rendered in its up-to-date state.
	 */
	void forceSync();
	void setTextColor(Color color);
	void setTextValueColor(Color color);

	/**
	 * If above rangeMin, fills in part of the slider, and the current value can't go below this.
	 */
	float getMin();
	
	/**
	 * Exceeding this value fills a portion of the bar with the overflow color. 
	 */
	float getMax();
	void setMin(float min);
	void setMax(float max);

	/**
	 * Minimum value of slider range.
	 */
	float getRangeMin();
	/**
	 * Maximum value of slider range.
	 */
	float getRangeMax();
	void setRangeMin(float min);
	void setRangeMax(float max);
}



