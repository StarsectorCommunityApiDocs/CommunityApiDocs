package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;

public interface CustomProductionListener {
	void reportCustomProductionGenerated(MonthlyReport report, ProductionReportIntel intel);
}
