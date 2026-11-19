package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.Misc;

public class LeashAssignmentAI extends BaseAssignmentAI {

	protected StarSystemAPI system;
	protected SectorEntityToken jumpPoint;
	
	protected float elapsed = 0f;
	protected float dur = 30f + (float) Math.random() * 20f;
	protected SectorEntityToken toGuard;
	protected String initialApproachText = null;
	
	public LeashAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI system, SectorEntityToken toGuard, String initialApproachText) {
		super();
		this.fleet = fleet;
		this.system = system;
		this.toGuard = toGuard;
		this.initialApproachText = initialApproachText;
		
		giveInitialAssignments();
	}

	@Override
	protected void giveInitialAssignments() {
		pickNext();
	}

	@Override
	protected void pickNext() {
		fleet.clearAssignments();
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, toGuard, 100f);
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_BUSY, true, 1f);
//		Vector2f loc = new Vector2f(fleet.getLocation());
//		loc = Misc.getPointWithinRadius(loc, 15f);
//		fleet.setLocation(loc.x, loc.y);
		
		float dist = Misc.getDistance(fleet.getLocation(), toGuard.getLocation());
		if (dist > 700 && fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_AGGRESSIVE) {
			fleet.addAssignmentAtStart(FleetAssignment.ORBIT_PASSIVE, toGuard, 3f, initialApproachText, null);
			CampaignFleetAIAPI ai = fleet.getAI();
			if (ai instanceof ModularFleetAIAPI) {
				// needed to interrupt an in-progress pursuit
				ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
				m.getStrategicModule().getDoNotAttack().add(m.getTacticalModule().getTarget(), 1f);
				m.getTacticalModule().setTarget(null);
			}
		}
		if (dist <= 700) {
			initialApproachText = null;
		}
	}

}












