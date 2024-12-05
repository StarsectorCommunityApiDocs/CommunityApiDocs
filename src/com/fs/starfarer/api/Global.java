package com.fs.starfarer.api;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

 /**
 * The primary way to access most of the game's state (fleets, stars, ships, people, etc) and many useful classes for creating and modifying state.
 *
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public class Global {
	private static SettingsAPI settingsAPI;
	private static SectorAPI sectorAPI;
	private static FactoryAPI factory;
	private static SoundPlayerAPI soundPlayer;
	private static CombatEngineAPI combatEngine;
	
	
	public static GameState getCurrentState() {
		return settingsAPI.getCurrentState();
	}

	/**
	 * Creates a new {@link Logger} that will add the given class to all log messages it produces.
	 * <pre>
	 * {@code
	 *  class MyClass {
	 *    Logger log = Global.getLogger(MyClass.class);
	 *
	 *    void myMethod() {
	 *      log.info("Hello, world!");
	 *    }
	 *  }
	 *}
	 * </pre>
	 *
	 * @param c the class to add to the log messages
	 */
	@SuppressWarnings("unchecked")
	public static Logger getLogger(Class c) {
		Logger log = Logger.getLogger(c);
		return log;
	}

	/**
	 * Used to create fleets, markets, cargo, crew compositions, jump points, progress indicators,
	 * memory objects, persons, officer data, battles, cargo stacks, communication messages, fleet stubs,
	 * and fleet AI, along with various orbit configurations and fleet members.
	 *
	 * Should only be used in the campaign.
	 */
	public static FactoryAPI getFactory() {
		return factory;
	}

	public static void setFactory(FactoryAPI factory) {
		Global.factory = factory;
	}

	/**
	 * Gets the {@link SoundPlayerAPI}, which is used to play sound effects and music.
	 */
	public static SoundPlayerAPI getSoundPlayer() {
		return soundPlayer;
	}

	public static void setSoundPlayer(SoundPlayerAPI sound) {
		Global.soundPlayer = sound;
	}

	/**
	 * Gets the {@link SettingsAPI}, which contains access to current settings and many useful utility methods.
	 */
	@NotNull
	public static SettingsAPI getSettings() {
		return settingsAPI;
	}

	public static void setSettings(SettingsAPI api) {
		Global.settingsAPI = api;
	}

	/**
	 * Gets the currently loaded save game's {@link SectorAPI}, which contains all {@link com.fs.starfarer.api.campaign.StarSystemAPI}s,
	 * {@link com.fs.starfarer.api.fleet.FleetAPI}s, the sectory {@link com.fs.starfarer.api.campaign.rules.MemoryAPI}, and more.
	 * <p>
	 * On the main menu, this may or may not be null.
	 */
	@Nullable
	public static SectorAPI getSector() {
		return sectorAPI;
	}

	/**
	 * Gets the {@link CombatEngineAPI}, which is typically active in combat, on the main menu, and on the refit screen.
	 * Mainly used to get info about and modify battles.
	 */
	public static CombatEngineAPI getCombatEngine() {
		return combatEngine;
	}

	public static void setCombatEngine(CombatEngineAPI combatEngine) {
		Global.combatEngine = combatEngine;
	}

	public static void setSector(SectorAPI api) {
		Global.sectorAPI = api;
	}

	/**
	 * @deprecated use {@link #getSettings()} instead
	 */
	@Deprecated
	public static SettingsAPI getSettingsAPI() {
		return settingsAPI;
	}

	/**
	 * @deprecated use {@link #getSector()} instead
	 */
	@Deprecated
	public static SectorAPI getSectorAPI() {
		return sectorAPI;
	}
}
