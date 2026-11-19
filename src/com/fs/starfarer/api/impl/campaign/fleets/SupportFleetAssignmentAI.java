package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;

public class SupportFleetAssignmentAI extends RouteFleetAssignmentAI {

	protected SupportFleetSpawner spawner;

	public SupportFleetAssignmentAI(SupportFleetSpawner spawner, CampaignFleetAPI fleet, RouteData route) {
		super(fleet, route);
		this.spawner = spawner;
	}
	
	protected String getTravelActionText(RouteSegment segment) {
		SectorEntityToken dest = segment.getDestination();
		if (segment.getId() == MercFleetManagerV2.ROUTE_TRAVEL) {
			return "traveling to " + dest.getMarket().getName();
		}
		if (segment.getId() == MercFleetManagerV2.ROUTE_RETURN) {
			return "returning to " + dest.getMarket().getName();
		}
		return "traveling";
	}
	
	protected String getInSystemActionText(RouteSegment segment) {
		if (segment.getId() == SupportFleetSpawner.WAIT_PHASE) {
			return "waiting";
		}
		return "patrolling";
	}
	
	protected String getStartingActionText(RouteSegment segment) {
		return "preparing for departure from " + route.getMarket().getName();
	}
	
	protected String getEndingActionText(RouteSegment segment) {
		return "orbiting " + route.getMarket().getName();
	}
	
}





