package com.fs.starfarer.api.ui;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;

public interface SectorMapAPI {
	SectorEntityToken getConstellationLabelEntity(Constellation c);
	SectorEntityToken getIntelIconEntity(IntelInfoPlugin plugin);
	void addPing(SectorEntityToken entity, Color color, float maxRange, int num);
	void addPing(SectorEntityToken entity, Color color, float maxRange, int num, float interval);
}
