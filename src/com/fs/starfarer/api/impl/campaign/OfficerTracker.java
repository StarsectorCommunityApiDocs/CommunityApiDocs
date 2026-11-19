package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.listeners.AppliedPostCombatCRLossToPlayerFleetMemberListener;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class OfficerTracker implements EveryFrameScript, AppliedPostCombatCRLossToPlayerFleetMemberListener {
	

	public static class OfficerDeployments {
		public OfficerDataAPI officer;
		public FleetMemberAPI member;
		public Long seenWithOfficer = null;
		public Long seenWithoutOfficer = null;
		public void resetTimes() {
			seenWithOfficer = null;
			seenWithoutOfficer = null;
		}
	}
	
	public static String HAS_REASSIGNMENT_COMPLAINT = "$hasReassignmentComplaint";
	public static String REASSIGNMENT_SHIP_NAME = "$reassignedShipName";
	
	public static float MAX_DAYS_TO_KEEP_TRACK = 365;
	public static String KEY = "$officerTracker_ref";
	
	public static Logger log = Logger.getLogger(OfficerTracker.class);
	
	protected IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
	protected LinkedHashMap<String, OfficerDeployments> data = new LinkedHashMap<>();
	
	public static OfficerTracker get() {
		return (OfficerTracker) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public OfficerTracker() {
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}

	@Override
	public void advance(float amount) {
		//if (!Misc.isExtended()) return;
		//if (!Misc.combatPayEnabled(officer))
		
		//if (true) return;
		float days = Global.getSector().getClock().convertToDays(amount);
		tracker.advance(days);
		if (tracker.intervalElapsed()) {
			cleanUpOldData();
			
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			List<FleetMemberAPI> membersCopy = pf.getFleetData().getMembersListCopy();
			for (FleetMemberAPI curr : membersCopy) {
				OfficerDataAPI od = getOfficerData(curr);
				if (od != null) {
					applyFMStateToOfficerIfNecessary(curr, false);
				}
			}
		}
	}
	
	
	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public boolean runWhilePaused() {
		return false;
	}
	
	public void cleanUpOldData() {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null) return;
		CampaignClockAPI clock = Global.getSector().getClock();
		
		List<OfficerDataAPI> officersCopy = pf.getFleetData().getOfficersCopy();
		List<FleetMemberAPI> membersCopy = pf.getFleetData().getMembersListCopy();

		for (OfficerDeployments dep : new ArrayList<OfficerDeployments>(data.values())) {
			String id = dep.officer.getPerson().getId();
			
			boolean found = false;
			for (OfficerDataAPI od : officersCopy) {
				if (od.getPerson().getMemoryWithoutUpdate().getBoolean(HAS_REASSIGNMENT_COMPLAINT)) {
					continue;
				}
				if (od == dep.officer) {
					found = true;
					break;
				}
			}
			if (!found || dep.officer == null) {
				data.remove(id);
				continue;
			}
			
			found = false;
			for (FleetMemberAPI curr : membersCopy) {
				if (curr == dep.member) {
					found = true;
					break;
				}
			}
			if (!found || dep.member == null) {
				data.remove(id);
				continue;
			}
			
			
			if (dep.seenWithOfficer != null) {
				float days = clock.getElapsedDaysSince(dep.seenWithOfficer);
				if (days >= MAX_DAYS_TO_KEEP_TRACK) {
					dep.seenWithOfficer = null;
					dep.seenWithoutOfficer = null;
				}
			}
			if (dep.seenWithoutOfficer != null) {
				float days = clock.getElapsedDaysSince(dep.seenWithoutOfficer);
				if (days >= MAX_DAYS_TO_KEEP_TRACK) {
					dep.seenWithoutOfficer = null;
				}
			}
			
			if (dep.seenWithOfficer == null && dep.seenWithoutOfficer == null) {
				data.remove(id);
				continue;
			}
		}
		
	}

	@Override
	public void appliedPostCombatCRLossToFleetMember(FleetMemberAPI member) {
		applyFMStateToOfficerIfNecessary(member, true);
	}
	
	public void applyFMStateToOfficerIfNecessary(FleetMemberAPI member, boolean wasDeployed) {
		cleanUpOldData();
		
		OfficerDeployments dep = getDeploymentsData(member);
		if (dep == null) return;
		boolean withOfficer = dep.officer.getPerson() == member.getCaptain();
		if (member.getCaptain() != null && member.getCaptain().isPlayer()) {
			return; // don't complain if they were replaced by player
		}
		
		if (Misc.isMercenary(dep.officer.getPerson()) || dep.officer.getPerson().getAICoreId() != null) {
			return; // mercs and AI cores don't complain, though AI cores would not normally have an OfficerData anyway
		}
		
		long ts = Global.getSector().getClock().getTimestamp();
		
		if (dep.member != member) {
			if (dep.member.getHullSpec().getHullSize().ordinal() <= member.getHullSpec().getHullSize().ordinal()) {
				dep.member = member;
				dep.resetTimes();
				if (wasDeployed) {
					dep.seenWithOfficer = ts;
				}
				return;
			}
		}
			
		if (!withOfficer && wasDeployed) {
			dep.seenWithoutOfficer = ts;
		}
		
		if (withOfficer && dep.seenWithOfficer != null && dep.seenWithoutOfficer != null && 
				dep.seenWithoutOfficer > dep.seenWithOfficer) {
			dep.officer.getPerson().getMemoryWithoutUpdate().set(HAS_REASSIGNMENT_COMPLAINT, true);
			dep.officer.getPerson().getMemoryWithoutUpdate().set(REASSIGNMENT_SHIP_NAME, dep.member.getShipName());
			data.remove(dep.officer.getPerson().getId());
			// failsafe for hotfix RC8; the above should work, but just in case
			Set<String> remove = new LinkedHashSet<>();
			for (String id : data.keySet()) {
				OfficerDeployments curr = data.get(id);
				if (curr != null && curr.member == member) {
					remove.add(id);
				}
			}
			for (String id : remove) {
				data.remove(id);
			}
			return;
		}
		
		if (withOfficer && (dep.seenWithOfficer == null || wasDeployed)) {
			dep.seenWithOfficer = ts;
		}
	}
	
	public OfficerDeployments getDeploymentsData(FleetMemberAPI member) {
		OfficerDataAPI od = getOfficerData(member);
		if (od != null) {
			if (od.getPerson().getMemoryWithoutUpdate().getBoolean(HAS_REASSIGNMENT_COMPLAINT)) {
				return null;
			}
			if (!Misc.combatPayEnabled(od.getPerson())) {
				return null;
			}
			OfficerDeployments dep = data.get(od.getPerson().getId());
			if (dep == null) {
				dep = new OfficerDeployments();
				dep.member = member;
				dep.officer = od;
				data.put(od.getPerson().getId(), dep);
			}
			return dep;
		}
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null) return null;
		
		for (OfficerDeployments curr : data.values()) {
			if (curr.member == member) {
				return curr;
			}
		}
		return null;
	}
	
	
	public OfficerDataAPI getOfficerData(FleetMemberAPI member) {
		if (member == null || member.getCaptain() == null) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null) return null;
		for (OfficerDataAPI od : pf.getFleetData().getOfficersCopy()) {
			if (od.getPerson() == member.getCaptain()) {
				return od;
			}
		}
		return null;
	}

}



