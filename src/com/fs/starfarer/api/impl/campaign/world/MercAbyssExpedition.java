package com.fs.starfarer.api.impl.campaign.world;

public class MercAbyssExpedition {

//	public static String NASCENT_WELL_KEY = "$mercAbyssExpedition_well";
//	
//	public void generate(SectorAPI sector) {
//		StarSystemAPI system = sector.createStarSystem("Unknown Location");
//		system.setName("Unknown Location"); // to get rid of "Star System" at the end of the name
//		system.setType(StarSystemType.DEEP_SPACE);
//		system.addTag(Tags.THEME_UNSAFE);
//		system.addTag(Tags.THEME_HIDDEN);
//		system.addTag(Tags.THEME_SPECIAL);
//		LocationAPI hyper = Global.getSector().getHyperspace();
//		
//		StarSystemAPI system = sector.createStarSystem("Deep Space");
//		//system.setType(StarSystemType.NEBULA);
//		system.setName("Deep Space"); // to get rid of "Star System" at the end of the name
//		system.setOptionalUniqueId(Misc.genUID());
//		system.setType(StarSystemType.DEEP_SPACE);
//		system.addTag(Tags.THEME_HIDDEN);
//		system.addTag(Tags.THEME_SPECIAL);
//		system.addTag(Tags.TEMPORARY_LOCATION);
//		system.addTag(Tags.SYSTEM_ABYSSAL);		
//		
//		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");
//		
//		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
//		//system.getLocation().set(2500, 3000);
//		system.getLocation().set(4000, 2500);
//		
//		HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
//		NebulaEditor editor = new NebulaEditor(hyperTerrain);
//		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, 200, 0, 360f);
////		editor.regenNoise();
////		editor.noisePrune(0.8f);
////		editor.regenNoise();
//
//		SectorEntityToken center = system.initNonStarCenter();
//		
//		system.setLightColor(new Color(225,170,255,255)); // light color in entire system, affects all entities
//		center.addTag(Tags.AMBIENT_LS);
//		
//		String type = "barren";
//		type = "irradiated";
//		PlanetAPI rock = system.addPlanet("site_alpha", center, "Alpha Site", type, 0, 150, 1200, 40);
//		//rock.setCustomDescriptionId("???");
//		rock.getMemoryWithoutUpdate().set("$ttBlackSite", true);
//		
//		rock.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
//		rock.getMarket().addCondition(Conditions.COLD);
//		rock.getMarket().addCondition(Conditions.DARK);
//		rock.getMarket().addCondition(Conditions.IRRADIATED);
//		rock.getMarket().addCondition(Conditions.RUINS_SCATTERED);
//		
//		rock.getMarket().getMemoryWithoutUpdate().set("$ruinsExplored", true);
//		//If this is done, shows up in intel planet list; doesn't matter either way
//		//Misc.setFullySurveyed(rock.getMarket(), null, false);
//		
//		CoreLifecyclePluginImpl.addRuinsJunk(rock);
//		
//		rock.setOrbit(null);
//		rock.setLocation(1200, 300);
//		
//		SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
//						new MagneticFieldParams(150f, // terrain effect band width 
//						500, // terrain effect middle radius
//						rock, // entity that it's around
//						350f, // visual band start
//						650f, // visual band end
//						new Color(60, 60, 150, 90), // base color
//						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
//						new Color(130, 60, 150, 130),
//						new Color(150, 30, 120, 150), 
//						new Color(200, 50, 130, 190),
//						new Color(250, 70, 150, 240),
//						new Color(200, 80, 130, 255),
//						new Color(75, 0, 160, 255), 
//						new Color(127, 0, 255, 255)
//						));
//		field.setCircularOrbit(rock, 0, 0, 75);
//		
//		CustomCampaignEntityAPI beacon = system.addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
//		beacon.setCircularOrbitPointingDown(rock, 0, 2500, 60);
//		
//		beacon.getMemoryWithoutUpdate().set("$ttBlackSite", true);
//		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_ID_KEY, Pings.WARNING_BEACON3);
//		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_FREQ_KEY, 1.5f);
//		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_COLOR_KEY, new Color(250,125,0,255));
//		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.GLOW_COLOR_KEY, new Color(250,55,0,255));
//		
//		
//		SectorEntityToken cache = BaseThemeGenerator.addSalvageEntity(system, Entities.ALPHA_SITE_WEAPONS_CACHE, Factions.NEUTRAL);
//		cache.getMemoryWithoutUpdate().set("$ttWeaponsCache", true);
//		cache.addTag(Tags.SALVAGE_ENTITY_FULL_DROP_IN_EXT_MODE);
//		cache.getLocation().set(11111, 11111);
//		
//		if (Misc.isExtended() && Global.getSettings().getBoolean("ext_strongerAlphaSiteDefenders")) {
//			Misc.setDefenderOverride(cache, new DefenderDataOverride(Factions.REMNANTS, 1f, 90, 110, 3));
//		}
//		
////		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("rock_jump_point", "Unstable Jump-point");
////		OrbitAPI orbit = Global.getFactory().createCircularOrbit(rock, 0, 2500, 90);
////		jumpPoint.setOrbit(orbit);
////		//jumpPoint.setRelatedPlanet(magec2);
////		jumpPoint.setStandardWormholeToHyperspaceVisual();
////		system.addEntity(jumpPoint);
//		
//		system.generateAnchorIfNeeded();
//		
//		NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(beacon, 50f);
//		well.addTag(Tags.NO_ENTITY_TOOLTIP);
//		well.setColorOverride(new Color(125, 50, 255));
//		hyper.addEntity(well);
//		well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(beacon, 0);
//		
//		addFleet(rock);
//		
//		SectorEntityToken hamatsu = addDerelict(system, beacon, 
//							"venture_Outdated", "ISS Hamatsu", HAMATSU_ID, ShipCondition.BATTERED, 200, true);
//		hamatsu.getMemoryWithoutUpdate().set("$hamatsu", true);
//		
//		Global.getSector().getMemoryWithoutUpdate().set(NASCENT_WELL_KEY, well);
//	}
//	
//	public static NascentGravityWellAPI getWell() {
//		return (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(NASCENT_WELL_KEY);
//	}
//	
//	
//	public static void addFleet(SectorEntityToken rock) {
//		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.NEUTRAL, FleetTypes.PATROL_LARGE, null);
//		fleet.setName("Unidentified Vessel");
//		fleet.setNoFactionInName(true);
//		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
//		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
//		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
//		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
//		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true); // so it keeps transponder on
//		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
//		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
//		fleet.getMemoryWithoutUpdate().set("$ziggurat", true);
//		
//		fleet.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
//		
//		
//		fleet.getFleetData().addFleetMember("ziggurat_Experimental");
//		fleet.getFleetData().ensureHasFlagship();
//		
//		fleet.clearAbilities();
////		fleet.addAbility(Abilities.TRANSPONDER);
////		fleet.getAbility(Abilities.TRANSPONDER).activate();
//		
//		// so it never shows as "Unidentified Fleet" but isn't easier to spot due to using the actual transponder ability
//		fleet.setTransponderOn(true);
//		
//		PersonAPI person = createZigguratCaptain();
//		fleet.setCommander(person);
//		
//		FleetMemberAPI flagship = fleet.getFlagship();
//		flagship.setCaptain(person);
//		flagship.updateStats();
//		flagship.getRepairTracker().setCR(flagship.getRepairTracker().getMaxCR());
//		flagship.setShipName("TTS Xenorphica");
//		
//		// to "perm" the variant so it gets saved and not recreated from the "ziggurat_Experimental" id
//		flagship.setVariant(flagship.getVariant().clone(), false, false);
//		flagship.getVariant().setSource(VariantSource.REFIT);
//		flagship.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
//
//		
//		Vector2f loc = new Vector2f(rock.getLocation().x + 300 * ((float) Math.random() - 0.5f),
//									rock.getLocation().y + 300 * ((float) Math.random() - 0.5f));
//		fleet.setLocation(loc.x, loc.y);
//		rock.getContainingLocation().addEntity(fleet);
//		
//		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
//				   new ZigFIDConfig());
//		
//		fleet.addScript(new ZigLeashAssignmentAI(fleet, rock));
//		
//	}
//	
//	
//	public static PersonAPI createZigguratCaptain() {
//		PersonAPI person = Global.getFactory().createPerson();
//		person.setName(new FullName("Motes", "", Gender.ANY));
//		person.setFaction(Factions.NEUTRAL);
//		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ziggurat_captain"));
//		person.setPersonality(Personalities.RECKLESS);
//		person.setRankId(Ranks.SPACE_CAPTAIN);
//		person.setPostId(null);
//		
//		person.getStats().setSkipRefresh(true);
//		
//		person.getStats().setLevel(10);
//		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
//		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
//		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
//		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
//		person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
//		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
//		//person.getStats().setSkillLevel(Skills.RELIABILITY_ENGINEERING, 2);
//		//person.getStats().setSkillLevel(Skills.RANGED_SPECIALIZATION, 2);
//		person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
//		person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
//		//person.getStats().setSkillLevel(Skills.PHASE_MASTERY, 2);
//		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
//		person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
//		
//		person.getStats().setSkillLevel(Skills.NAVIGATION, 1);
//		
//		person.getStats().setSkipRefresh(false);
//		
//		return person;
//	}
//	
//	public static SectorEntityToken addDerelict(StarSystemAPI system, SectorEntityToken focus, 
//							   String variantId, String name, String id, 
//							   ShipCondition condition, float orbitRadius, boolean recoverable) {
//		DerelictShipData params = new DerelictShipData(new PerShipData(variantId, condition, 0f), false);
//		if (name != null) {
//			params.ship.shipName = name;
//			params.ship.nameAlwaysKnown = true;
//			params.ship.fleetMemberId = id;
//		}
//		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
//		ship.setDiscoverable(true);
//
//		float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
//		ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);
//
//		if (recoverable) {
//			ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
//			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
//		}
//		return ship;
//	}
	
}













