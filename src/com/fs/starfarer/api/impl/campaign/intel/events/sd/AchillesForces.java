package com.fs.starfarer.api.impl.campaign.intel.events.sd;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.missions.hub.TriggerFleetAssignmentAI;
import com.fs.starfarer.api.util.Misc;



public class AchillesForces {

	public static final String ACHILLES_FLEET = "$SDR_achilles";
	public static final String HECTOR_FLEET = "$SDR_hector";
	
	
	public static void create() {
		MarketAPI yesod = Global.getSector().getEconomy().getMarket("yesod");
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		StarSystemAPI askonia = volturn.getStarSystem();
		
		Random random = new Random();
		
		{
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		String factionId = Factions.PERSEAN;
		
		Vector2f loc = yesod.getLocationInHyperspace();
		m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_1, factionId, FleetTypes.PATROL_LARGE, loc);

		m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.HIGHER);
		PersonAPI achilles = People.getPerson(People.ACHILLES);
		achilles.getName().setLast("Andrada");
		m.triggerSetFleetCommander(achilles);
		
		m.triggerMakeNonHostileToFaction(Factions.PERSEAN);
		m.triggerMakeNonHostileToFaction(Factions.PIRATES);
		m.triggerMakeNonHostileToFaction(Factions.LUDDIC_PATH);

		m.triggerSetWarFleet();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, yesod);
		m.triggerSetFleetFlagPermanent(ACHILLES_FLEET);
		m.triggerFleetSetNoFactionInName();
		m.triggerFleetSetName("Achilles' Fleet");
		m.triggerSetFleetFaction(Factions.DIKTAT_ACHILLES);
		
		//m.triggerPatrolAllowTransponderOff();
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		fleet.removeScriptsOfClass(TriggerFleetAssignmentAI.class);
		yesod.getContainingLocation().addEntity(fleet);
		fleet.setLocation(yesod.getPlanetEntity().getLocation().x, yesod.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
		giveGoToVolturnOrders(fleet);
		}
		
		for (int i = 0; i < 5; i++) {
			FleetCreatorMission m = new FleetCreatorMission(random);
			m.beginFleet();

			String factionId = Factions.PERSEAN;
			if (i >= 2) factionId = Factions.MERCENARY;

			FleetSize size = FleetSize.VERY_LARGE;
			if (random.nextFloat() < 0.5f) {
				size = FleetSize.LARGE;
			}
			Vector2f loc = yesod.getLocationInHyperspace();
			m.triggerCreateFleet(size, FleetQuality.DEFAULT, factionId, FleetTypes.TASK_FORCE, loc);

			//m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.HIGHER);

			m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);

			m.triggerMakeNonHostileToFaction(Factions.PERSEAN);
			m.triggerMakeNonHostileToFaction(Factions.PIRATES);
			m.triggerMakeNonHostileToFaction(Factions.LUDDIC_PATH);

			m.triggerSetWarFleet();
			m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, yesod);
			//m.triggerFleetSetNoFactionInName();
			m.triggerSetFleetFaction(Factions.DIKTAT_ACHILLES);

			CampaignFleetAPI fleet = m.createFleet();
			fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
			fleet.removeScriptsOfClass(TriggerFleetAssignmentAI.class);
			yesod.getContainingLocation().addEntity(fleet);
			fleet.setLocation(yesod.getPlanetEntity().getLocation().x, yesod.getPlanetEntity().getLocation().y);
			fleet.setFacing((float) random.nextFloat() * 360f);

			giveGoToVolturnOrders(fleet);
		}
		
	}
	
	public static void giveGoToVolturnOrders(CampaignFleetAPI fleet) {
		MarketAPI yesod = Global.getSector().getEconomy().getMarket("yesod");
		MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
		StarSystemAPI askonia = volturn.getStarSystem();
		
		fleet.clearAssignments();
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, yesod.getPrimaryEntity(), 3f, "preparing for departure", null);
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, volturn.getPrimaryEntity(), 10000f, new Script() {
			@Override
			public void run() {
				fleet.addScript(new LeashAssignmentAI(fleet, askonia, volturn.getPrimaryEntity(), null));
				for (CampaignFleetAPI curr : askonia.getFleets()) {
					float dist = Misc.getDistance(curr, volturn.getPrimaryEntity());
					if (dist < 2000f && curr.getMemoryWithoutUpdate().getBoolean(ACHILLES_FLEET)) {
						SDResolution.achillesArrivedAtVolturn();
					}
				}
//				if (fleet.getMemoryWithoutUpdate().getBoolean(ACHILLES_FLEET)) {
//					SDResolution.achillesArrivedAtVolturn();
//				}
			}
		});
	}
	
	
	
	public static void createHectorFleet() {
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		StarSystemAPI askonia = sindria.getStarSystem();
		
		Random random = new Random();
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		String factionId = Factions.DIKTAT;
		
		Vector2f loc = sindria.getLocationInHyperspace();
		m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_1, factionId, FleetTypes.PATROL_LARGE, loc);

		m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.HIGHER);
		PersonAPI hector = People.getPerson(People.HECTOR);
		hector.getName().setLast("Andrada");
		m.triggerSetFleetCommander(hector);
		
		m.triggerMakeNonHostileToFaction(Factions.PERSEAN);
		m.triggerMakeNonHostileToFaction(Factions.PIRATES);
		m.triggerMakeNonHostileToFaction(Factions.LUDDIC_PATH);

		m.triggerSetWarFleet();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, sindria);
		m.triggerSetFleetFlagPermanent(HECTOR_FLEET);
		m.triggerFleetSetNoFactionInName();
		m.triggerFleetSetName("Hector's Fleet");
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		fleet.removeScriptsOfClass(TriggerFleetAssignmentAI.class);
		sindria.getContainingLocation().addEntity(fleet);
		fleet.setLocation(sindria.getPlanetEntity().getLocation().x, sindria.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
		fleet.addScript(new LeashAssignmentAI(fleet, askonia, sindria.getPrimaryEntity(), null));
	}
	
}




