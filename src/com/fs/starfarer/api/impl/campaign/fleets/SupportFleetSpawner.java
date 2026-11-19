package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MiscCMD.SupportFleetSize;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MiscCMD.SupportFleetType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SellToFleet;
import com.fs.starfarer.api.util.Misc;

public class SupportFleetSpawner implements RouteFleetSpawner {

	public static String ROUTE_SOURCE = "support_fleet";
	
	public static int PREP_PHASE = 0;
	public static int TRAVEL_PHASE = 1;
	public static int WAIT_PHASE = 2;
	public static int RETURN_PHASE = 3;
	
	public static float WAIT_DAYS = 120f;
	
	
	public static RouteData addSupportFleetRoute(SupportFleetType type, SupportFleetSize size, 
						SectorEntityToken dest, MarketAPI from, boolean fastMode) {
		
		//StarSystemAPI system = dest.getStarSystem();
		StarSystemAPI system = Misc.getStarSystemForAnchor(dest);
		if (system == null) return null;
		
		final JumpPointAPI inner = Misc.getDistressJumpPoint(system);
		if (inner == null) return null;

		OptionalFleetData extra = new OptionalFleetData(from);
		extra.fleetType = FleetTypes.SUPPLY_FLEET;
		if (type == SupportFleetType.TASK_FORCE) {
			extra.fleetType = FleetTypes.TASK_FORCE;
		}

		float days = RouteLocationCalculator.getTravelDays(from.getPrimaryEntity(), dest);
		
		SupportFleetSpawner spawner = new SupportFleetSpawner(type, size, dest, from, days);
		RouteData route = RouteManager.getInstance().addRoute(ROUTE_SOURCE, from, 
				Misc.genRandomSeed(), extra, spawner, null);
		extra.strength = getBaseCombatFP(type, size);
		extra.strength = Misc.getAdjustedStrength(extra.strength, from);
		
		
		route.addSegment(new RouteSegment(PREP_PHASE, 1f, from.getPrimaryEntity()));
		route.addSegment(new RouteSegment(TRAVEL_PHASE, days, from.getPrimaryEntity(), inner));
		
		float waitDays = WAIT_DAYS;
		route.addSegment(new RouteSegment(WAIT_PHASE, waitDays, inner));
		route.addSegment(new RouteSegment(RETURN_PHASE, days, inner, from.getPrimaryEntity()));
		
		if (fastMode) {
			route.getSegments().get(0).setElapsed(route.getSegments().get(0).daysMax);
			route.getSegments().get(1).setElapsed(days - 4f);
		} else {
			if (!Global.getSettings().isDevMode()) {
				route.setDelay(1f);
			}
		}
		
		return route;
	}
	
	public static float getBaseCombatFP(SupportFleetType type, SupportFleetSize size) {
		float fp = 0f;
		switch (size) {
		case LARGE:
			fp = 400;
			break;
		case MEDIUM:
			fp = 300;
			break;
		case SMALL:
			fp = 200;
			break;
		}
		
		if (type == SupportFleetType.RESUPPLY) {
			fp *= 0.25f;
		}
		return fp;
	}
	

	protected SupportFleetType type;
	protected SupportFleetSize size;
	protected SectorEntityToken dest;
	protected MarketAPI from;
	protected float travelDays;
	
	public SupportFleetSpawner(SupportFleetType type, SupportFleetSize size, SectorEntityToken dest, MarketAPI from, float days) {
		this.type = type;
		this.size = size;
		this.dest = dest;
		this.from = from;
		this.travelDays = days;
	}
	
	@Override
	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = route.getRandom();
		
		
		CampaignFleetAPI fleet = null;
		if (type == SupportFleetType.TASK_FORCE) {
			Vector2f loc = from.getPrimaryEntity().getLocation();
			
			FleetCreatorMission m = new FleetCreatorMission(random);
			m.beginFleet();
			float fSize = 0f;
			int tugs = 0;
			switch (this.size) {
			case LARGE: 
				fSize = 1.0f;
				tugs = 2;
				break;
			case MEDIUM:
				fSize = 0.75f;
				tugs = 1;
				break;
			case SMALL:
				fSize = 0.5f;
				tugs = 1;
				break;
			}
			
			float sizeMult = from.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
			if (sizeMult > 1f) {
				fSize += sizeMult * 0.05f;
			}
			
			FactionDoctrineAPI doctrine = Global.getSector().getPlayerFaction().getDoctrine();
			// already accounted for in sizeMult
			//fSize += (float) (doctrine.getNumShips() - 1f) * 0.025f;
			float officerMult = 1f;
			officerMult += (float) (doctrine.getOfficerQuality() - 1f) * 1f;
			
			m.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.DEFAULT, Factions.PLAYER, FleetTypes.TASK_FORCE, loc);
			//m.getPreviousCreateFleetAction().params.ignoreMarketFleetSizeMult = false;
			m.getPreviousCreateFleetAction().fSizeOverride = fSize;
			m.getPreviousCreateFleetAction().params.ignoreMarketFleetSizeMult = true;
			
			m.getPreviousCreateFleetAction().params.officerNumberMult = officerMult; // max officers, within limit
			if (doctrine.getOfficerQuality() >= 4) {
				m.getPreviousCreateFleetAction().params.officerLevelLimit = 6;
			}
			
			m.triggerSetFleetOfficers(OfficerNum.DEFAULT, OfficerQuality.DEFAULT);
			m.triggerSetWarFleet();
			m.triggerFleetMakeFaster(true, tugs, false);
			m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, from.getId());
			
			fleet = m.createFleet();
		} else {
			float combat = getBaseCombatFP(type, size);
			float strMult = 0.75f + 0.25f * Math.max(0f, 1f - travelDays/40f);
			float nonCombatMult = 0.25f + 0.75f * Math.min(1f, travelDays/40f);
			
			
			float tanker = 0f;
			float freighter = 0f;
			float transport = 0f;
			
			String fleetType = FleetTypes.SUPPLY_FLEET;
			nonCombatMult = 0.75f + 0.25f * Math.min(1f, travelDays/40f);
			tanker = 30f;
			freighter = 20f;
			transport = 10f;
			switch (size) {
			case LARGE:
				nonCombatMult *= 3f;
				break;
			case MEDIUM:
				nonCombatMult *= 2f;
				break;
			case SMALL:
				break;
			}
			
			combat *= strMult;
			tanker *= nonCombatMult;
			freighter *= nonCombatMult;
			
			FleetParamsV3 params = new FleetParamsV3(
					route.getMarket(), 
					null,
					Factions.PLAYER,
					route.getQualityOverride(),
					fleetType,
					combat, // combatPts
					freighter, // freighterPts 
					tanker, // tankerPts
					transport/2f, // transportPts
					transport/2f, // linerPts
					0f, // utilityPts
					0f // qualityMod
					);
			// otherwise, either small-fleet-size-mult fleets are too expensive
			// or you get net-cheaper supplies etc on the high end
			params.ignoreMarketFleetSizeMult = true;
			params.timestamp = route.getTimestamp();
			params.random = random;
			fleet = FleetFactoryV3.createFleet(params);
		}
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		fleet.setFacing((float) Math.random() * 360f);
		
		// this will all get overridden by the assignment AI, depending on route-time elapsed etc
		route.getMarket().getContainingLocation().addEntity(fleet);
		fleet.setLocation(route.getMarket().getPrimaryEntity().getLocation().x, route.getMarket().getPrimaryEntity().getLocation().x);
		
		if (type == SupportFleetType.RESUPPLY) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_BUYS_STUFF, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_BUYS_STUFF_RATE, SellToFleet.RESUPPLY_FLEET_SELL_VALUE_MULT);
		}
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true);
		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true); // for following playeer
		fleet.getMemoryWithoutUpdate().set("$supportFleet", true);
		fleet.getMemoryWithoutUpdate().set("$supportFleetType", type.name());
		fleet.getMemoryWithoutUpdate().set("$supportFleetSize", size.name());
		fleet.getMemoryWithoutUpdate().set("$supportFleetSpawnFP", fleet.getFleetPoints());
		
		SupportFleetAssignmentAI ai = new SupportFleetAssignmentAI(this, fleet, route);
		fleet.addScript(ai);
		
		fleet.setNoAutoDespawn(true);
		
		return fleet;
	}
	

	@Override
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	@Override
	public boolean shouldRepeat(RouteData route) {
		return false;
	}
	
	@Override
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
	}

}
