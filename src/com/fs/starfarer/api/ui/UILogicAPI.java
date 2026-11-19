package com.fs.starfarer.api.ui;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

public interface UILogicAPI {
	void processInput(List<InputEventAPI> events);
	void advance(float amount);
	void focusLost();
}
