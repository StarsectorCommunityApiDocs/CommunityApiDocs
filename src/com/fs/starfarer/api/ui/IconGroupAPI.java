package com.fs.starfarer.api.ui;

import java.util.List;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;

public interface IconGroupAPI {
	public interface IconSubgroupAPI extends UIPanelAPI {
		float getNumIcons();
		void setNumIcons(float count);
		CommoditySpecAPI getCommodity();
		IconRenderMode getRenderMode();
		float getLastIconScale();
		Object getCustomData();
	}

	void autoSizeTo(float iconSize, float width);
	void autoSizeWithAdjust(float iconSize, float width, float maxHeight, float maxIconSize);
	float computeRowHeightFor(float width, float maxHeight, float maxIconHeight);

	void addGroup(CommoditySpecAPI spec, int count, float iconScale, IconRenderMode mode, Object custom);
	void addGroup(CommodityOnMarketAPI com, int count, float iconScale, IconRenderMode mode, Object custom);

	float getNumRows();
	void clearGroups();
	float getTotalIconsAndSpaces();
	boolean isEmpty();

	List<IconSubgroupAPI> getSubgroups();

	boolean isWideSpacing();
	boolean isMediumSpacing();
	void setWideSpacing(boolean bool);
	void setMediumSpacing(boolean bool);

	boolean isHighlightOnMouseover();
	void setHighlightOnMouseover(boolean bool);	

	void addIconGroup(String commodityId, IconRenderMode mode, int count, Object custom);
}
