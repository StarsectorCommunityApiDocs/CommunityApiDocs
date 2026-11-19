package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.fleets.SupportFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.SupportFleetSpawner;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MiscCMD.SupportFleetSize;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MiscCMD.SupportFleetType;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class SupportFleetIntel extends BaseIntelPlugin {

	public static String SUPPORT_FLEET_FOLLOW = "$supportFleetFollow";
	public static String SUPPORT_FLEET_STAY = "$supportFleetStay";
	
	public static String ARRIVAL_UPDATE = "arrival_update";
	public static String RETURN_UPDATE = "return_update";
	public static String RETURN_WARNING = "return_warning";
	
	public static float MAX_OPERATIONAL_TIME = 90f;
	public static float OPERATIONAL_TIME_PER_EXTENSION = 60f;
	public static float OP_TIME_WARNING_DAYS = 5f;
	
	protected RouteData route;
	protected StarSystemAPI dest;
	protected SupportFleetType type;
	protected SupportFleetSize size;
	protected boolean sentArrival = false;
	protected boolean sentReturn = false;
	protected boolean sentWarning = false;
	protected float operationalTimeRemaining = MAX_OPERATIONAL_TIME;
	
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	protected float daysOutOfSight = 0f;

	public SupportFleetIntel(RouteData route, StarSystemAPI dest, SupportFleetType type, SupportFleetSize size) {
		this.route = route;
		this.dest = dest;
		this.type = type;
		this.size = size;
		Global.getSector().addScript(this);
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (isWaiting() && !sentArrival) {
			if (!isFollowing() && !isStaying()) {
				sendUpdateIfPlayerHasIntel(ARRIVAL_UPDATE, false);
			}
			sentArrival = true;
		}
		if (isReturning() && !sentReturn) {
			sendUpdateIfPlayerHasIntel(RETURN_UPDATE, false);
			endAfterDelay();
			sentReturn = true;
		}
		
		if (!intelIsInControl()) return;
		
		CampaignFleetAPI fleet = route.getActiveFleet();
		if (fleet == null) return;
		
		
		float days = Global.getSector().getClock().convertToDays(amount);
		interval.advance(days);
		
		operationalTimeRemaining -= days;
		if (operationalTimeRemaining <= 0 && !isReturning()) {
			Misc.giveStandardReturnToSourceAssignments(fleet, true);
		}
		
		if (isFollowing() && !fleet.isVisibleToPlayerFleet()) {
			daysOutOfSight += days;
		} else {
			daysOutOfSight = 0f;
		}
		

		if (!interval.intervalElapsed()) return;
		if (isReturning()) return;
		
		if (operationalTimeRemaining < OP_TIME_WARNING_DAYS && !sentWarning) {
			sendUpdateIfPlayerHasIntel(RETURN_WARNING, false);
			sentWarning = true;
		}
		
		if (daysOutOfSight > 2f && fleet.getBattle() == null) {
			boolean prettyFar = !fleet.isInCurrentLocation();
			if (!prettyFar) {
				float dist = Misc.getDistance(fleet.getLocation(), Global.getSector().getPlayerFleet().getLocation());
				float sr = Global.getSettings().getMaxSensorRange();
				if (fleet.isInHyperspace()) {
					sr = Global.getSettings().getMaxSensorRangeHyper();
				}
				if (dist > sr) prettyFar = true;
			}
			
			boolean tooFar = Global.getSector().getPlayerFleet().getContainingLocation().hasTag(Tags.SYSTEM_ABYSSAL);
			tooFar |= Global.getSector().getPlayerFleet().getContainingLocation().hasTag(Tags.THEME_HIDDEN);
			tooFar |= !fleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspace();
			if (!tooFar) {
				float dist = Misc.getDistance(fleet.getLocation(), Global.getSector().getPlayerFleet().getLocation());
				float threshold = 10000f;
				if (fleet.isInHyperspace()) {
					threshold = 6000;
				}
				if (dist > threshold) tooFar = true;
			}
					
			
			if (prettyFar && !tooFar) {
				if (fleet.getContainingLocation() != Global.getSector().getPlayerFleet().getContainingLocation()) {
					fleet.getContainingLocation().removeEntity(fleet);
					Global.getSector().getPlayerFleet().getContainingLocation().addEntity(fleet);
				}
				float dir = Misc.getAngleInDegrees(Global.getSector().getPlayerFleet().getLocation(), fleet.getLocation());
				Vector2f loc = HubMissionWithTriggers.pickLocationWithinArc(Misc.random, Global.getSector().getPlayerFleet(),
						dir, 60f, 2000f, 2500f, 3000f);
				fleet.setLocation(loc.x, loc.y);
				daysOutOfSight = 0f;
				
			}
		}
		
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (isFollowing()) {
			if (curr == null || curr.getTarget() != player || curr.getAssignment() != FleetAssignment.FOLLOW) {
				giveFollowAssignment();
			}
		}
		if (isStaying()) {
			if (curr == null || curr.getAssignment() != FleetAssignment.HOLD) {
				giveStayAssignment();
			}
		}
		
		// send fleet back after delay, track operating time remaining and show it in description
	}
	
	public boolean intelIsInControl() {
		if (!route.isExpired()) return false;
		CampaignFleetAPI fleet = route.getActiveFleet();
		if (fleet == null) return false;
		return true;
	}
	
	
	public void extendOpTime() {
		sentWarning = false;
		operationalTimeRemaining += OPERATIONAL_TIME_PER_EXTENSION;
		if (getFleet() != null) {
			getFleet().getMemoryWithoutUpdate().unset(Misc.FLEET_RETURNING_TO_DESPAWN);
		}
	}
	
	public void giveFollowAssignment() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet == null) return;
		
		fleet.clearAssignments();
		fleet.getAI().addAssignment(FleetAssignment.FOLLOW, playerFleet, 100000f, "following your fleet", null);
		fleet.getMemoryWithoutUpdate().set(SUPPORT_FLEET_FOLLOW, true, 10000f);
		fleet.getMemoryWithoutUpdate().unset(SUPPORT_FLEET_STAY);
		fleet.getMemoryWithoutUpdate().unset(Misc.FLEET_RETURNING_TO_DESPAWN);
		
		cleanUpSupportFleetRoute(fleet);
	}
	
	public void giveStayAssignment() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		SectorEntityToken loc = fleet.getContainingLocation().createToken(fleet.getLocation());
		fleet.clearAssignments();
		fleet.getAI().addAssignment(FleetAssignment.HOLD, loc, 100000f, "maintaining position", null);
		fleet.getMemoryWithoutUpdate().set(SUPPORT_FLEET_STAY, true, 10000f);
		fleet.getMemoryWithoutUpdate().unset(SUPPORT_FLEET_FOLLOW);
		fleet.getMemoryWithoutUpdate().unset(Misc.FLEET_RETURNING_TO_DESPAWN);
		
		cleanUpSupportFleetRoute(fleet);
	}
	
	protected void cleanUpSupportFleetRoute(CampaignFleetAPI fleet) {
		if (fleet == null) return;
		if (!route.isExpired()) {
			RouteManager.getInstance().removeRoute(route);
			route.expire();
			fleet.removeScriptsOfClass(SupportFleetAssignmentAI.class);
		}
	}	
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}
	
	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	public float getETA() {
		RouteSegment curr = route.getCurrent();
		if (curr == null) return 0f;
		
		if (curr.getId() == SupportFleetSpawner.TRAVEL_PHASE || 
				route.getCurrentSegmentId() == SupportFleetSpawner.PREP_PHASE) {
			float total = 0f;
			for (RouteSegment seg : route.getSegments()) {
				total += Math.max(0f, seg.getDaysMax() - seg.getElapsed());
				if (seg.getId() == SupportFleetSpawner.TRAVEL_PHASE) {
					break;
				}
			}
			return total;
		}
		return 0f;
	}
	
	public float getWaitTimeRemaining() {
		RouteSegment curr = route.getCurrent();
		if (curr == null) return 0f;
		
		if (curr.getId() == SupportFleetSpawner.WAIT_PHASE) {
			return Math.max(0f, curr.getDaysMax() - curr.getElapsed());
		}
		return 0f;
	}
	
	public boolean isOrganizing() {
		if (route.isExpired()) return false;
		RouteSegment curr = route.getCurrent();
		if (curr == null || curr.getElapsed() >= curr.getDaysMax()) return false;
		return curr.getId() == SupportFleetSpawner.PREP_PHASE || route.getDelay() > 0;
	}
	public boolean isTraveling() {
		if (route.isExpired()) return false;
		if (isOrganizing()) return true;
		RouteSegment curr = route.getCurrent();
		if (curr == null) return false;
		return curr.getId() == SupportFleetSpawner.TRAVEL_PHASE;
	}
	public boolean isWaiting() {
		if (route.isExpired()) return false;
		RouteSegment curr = route.getCurrent();
		if (curr == null) return false;
		return curr.getId() == SupportFleetSpawner.WAIT_PHASE;
	}
	public boolean isReturning() {
		CampaignFleetAPI fleet = route.getActiveFleet();
		if (fleet != null && fleet.getMemoryWithoutUpdate().contains(Misc.FLEET_RETURNING_TO_DESPAWN)) {
			return true;
		}
		
		if (route.isExpired()) return false;
		RouteSegment curr = route.getCurrent();
		if (curr == null) return false;
		return curr.getId() == SupportFleetSpawner.RETURN_PHASE;
	}
	
	public boolean isFollowing() {
		CampaignFleetAPI fleet = route.getActiveFleet();
		if (fleet != null && fleet.getMemoryWithoutUpdate().contains(SUPPORT_FLEET_FOLLOW)) {
			return true;
		}
		return false;
	}
	public boolean isStaying() {
		CampaignFleetAPI fleet = route.getActiveFleet();
		if (fleet != null && fleet.getMemoryWithoutUpdate().contains(SUPPORT_FLEET_STAY)) {
			return true;
		}
		return false;
	}
	
	public String getName() {
		String suffix = "";
		if (!intelIsInControl()) {
			if (isOrganizing()) suffix = " - Organizing";
			else if (isTraveling()) suffix = " - En Route";
			else if (isWaiting()) suffix = " - Waiting";
			else if (isReturning()) suffix = " - Returning";
			
			if (getListInfoParam() == ARRIVAL_UPDATE) {
				suffix = " - Arrived";
			}
		}
		if (getListInfoParam() == RETURN_WARNING) {
			suffix = " - Warning";
		}
		String title = type.getName() + suffix;
		return title;
	}
	
	
	@Override
	public String getCommMessageSound() {
		if (getListInfoParam() == RETURN_WARNING) {
			return getSoundMajorPosting();
		}
		return super.getCommMessageSound();
	}

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = getBulletColorForMode(mode);
		
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;

		bullet(info);
		if (getListInfoParam() == RETURN_WARNING) {
			info.addPara("Returning to base soon", tc, initPad);	
		} else if (intelIsInControl() && !isReturning()) {
			addDays(info, "Operational time remaining:", "", operationalTimeRemaining, tc, initPad);
		} else if (isOrganizing()) {
			if (route.getMarket() != null) {
				MarketAPI from = route.getMarket();
				info.addPara("Being organized " + from.getOnOrAt() + " " + from.getName(), tc, initPad);
				initPad = 0f;
				float eta = getETA();
				addDays(info, "Estimated ",  "until arrival", eta, tc, initPad);
			}
		} else if (isTraveling()) {
			info.addPara("Traveling to " + dest.getNameWithLowercaseTypeShort(), tc, initPad);
			initPad = 0f;
			float eta = getETA();
			addDays(info, "Estimated ",  "until arrival", eta, tc, initPad);
		} else if (isWaiting()) {
			info.addPara("Waiting in " + dest.getNameWithLowercaseTypeShort(), tc, initPad);
			initPad = 0f;
			float eta = getWaitTimeRemaining();
			addDays(info, "Estimated ",  "until departure", eta, tc, initPad);
		} else if (isReturning()) {
			info.addPara("Returning to " + route.getMarket().getName(), tc, initPad);
		}
		unindent(info);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		String desc = "";
		if (type == SupportFleetType.RESUPPLY) {
			desc = "A supply fleet carrying fuel, supplies, crew, volatiles, and heavy machinery";
			if (isReturning()) desc = "The supply fleet";
		} else {
			desc = "A combat task force";
			if (isReturning()) desc = "The task force";
		}
		
		if (intelIsInControl() && !isReturning()) {
			desc += " is carrying out your direct orders.";
		} else if (isOrganizing()) {
			desc += " is being organized " + route.getMarket().getOnOrAt() + " " +
					route.getMarket().getName() + " and will be dispatched to the " + 
					dest.getNameWithLowercaseType() + " shortly.";
		} else if (isTraveling()) {
			desc += " has been dispatched from " + route.getMarket().getName() + " and is en route to the " + 
						dest.getNameWithLowercaseType() + "."; 
		} else if (isWaiting()) {
			desc += " is waiting at the innermost jump-point of the " + dest.getNameWithLowercaseType() + ".";
		} else if (isReturning()) {
			desc += " is returning to " + route.getMarket().getName() + ".";
		}
		info.addPara(desc, opad);
		if (!isReturning()) {
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (isReturning()) {
			return route.getMarket().getPrimaryEntity();
		}
		if (intelIsInControl() && getFleet() != null) {
			return getFleet();
		}
		JumpPointAPI inner = Misc.getDistressJumpPoint(dest);
		if (inner != null) return inner;
		return super.getMapLocation(map);
	}

	@Override
	public String getIcon() {
		if (type == SupportFleetType.TASK_FORCE) {
			return Global.getSettings().getSpriteName("intel", "combat_fleet");
		}
		return Global.getSettings().getSpriteName("intel", "support_fleet");
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getPlayerFaction();
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MILITARY);
		return tags;
	}
	
	public CampaignFleetAPI getFleet() {
		return route.getActiveFleet();
	}
	
	public SupportFleetType getType() {
		return type;
	}

	public SupportFleetSize getSize() {
		return size;
	}

	public static SupportFleetIntel getFleetIntel(CampaignFleetAPI fleet) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(SupportFleetIntel.class)) {
			if (((SupportFleetIntel)intel).getFleet() == fleet) return (SupportFleetIntel)intel;
		}
		return null;
	}
	
	public static int getNumSupportFleets(SupportFleetType type, boolean countReturning) {
		int num = 0;
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(SupportFleetIntel.class)) {
			SupportFleetIntel sfi = ((SupportFleetIntel)intel);
			if (sfi.getType() == type && (!sfi.isReturning() || countReturning)) {
				num++;
			}
		}
		return num;
	}
}




