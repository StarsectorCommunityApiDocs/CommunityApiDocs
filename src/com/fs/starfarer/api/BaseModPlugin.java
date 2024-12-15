package com.fs.starfarer.api;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.DroneLauncherShipSystemAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.thoughtworks.xstream.XStream;

/**
 * Extend this class instead of implementing ModPlugin for convenience if you do not
 * intend to implement all the methods. This will also help avoid your mod breaking
 * when new methods are added to ModPlugin, since default implemenations will be
 * added here and your implementation will inherit them.
 * <p>
 * While having a ModPlugin for your mod is not strictly necessary, it serves as an entry point for all kinds
 * of useful things, such as faction initialization, adding custom scripts and picking AI plugins.
 * <p>
 * You need to instruct the game to load your implementation of this class by e.g. adding it to your mod_info.json:
 * "modPlugin":"my.package.name.MyModBasePlugin"
 * 
 * @author Alex Mosolov
 *
 *
 * Copyright 2013 Fractal Softworks, LLC
 */
public class BaseModPlugin implements ModPlugin {

	/**
	 * Code in here gets executed after the game gets saved. The main use for this is in combination with beforeGameSave.
	 * If you did clean-up code in beforeGameSave, you can re-add your stuff in here.
	 *
	 * <p>Example:</p>
	 * <pre>
	 *     <code>
	 *         Global.getSector().getListenerManager().addListener(new MyListener());
	 *     </code>
	 * </pre>
	 *
	 * Original doc:
	 * <p>
	 * {@inheritDoc}
	 */
	public void afterGameSave() {
		
	}

	/**
	 * Code in here gets executed before the game gets saved. This is mainly useful for doing cleanup.
	 * Anything that is part of the sector gets serialized when the game is saved. If you, for instance, add custom
	 * listeners to the sector and the game gets saved, they are added to the save file. If you
	 * then later e.g. rename the class of those listeners, that will break mod compatibility and users won't be able
	 * to upgrade your mod in an ongoing campaign. Also, players won't be able to remove your mod from an ongoing
	 * campaign. To avoid this issue, you can simply clean up anything that you added to the sector that doesn't need to
	 * be saved in this method and then re-add it in the afterGameSave-method.
	 *
	 * <p>Example:</p>
	 * <pre>
	 *     <code>
	 *			while (Global.getSector().getListenerManager().hasListenerOfClass(MyListener.class)){
	 *             Global.getSector().getListenerManager().removeListenerOfClass(MyListener.class);
	 *         }
	 *     </code>
	 * </pre>
	 *
	 * Original doc:
	 * <p>
	 * {@inheritDoc}
	 */
	public void beforeGameSave() {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void onGameSaveFailed() {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void onApplicationLoad() throws Exception {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void onEnabled(boolean wasEnabledBefore) {
		
	}

	/**
	 * Code in here gets executed whenever the game is loaded.
	 * <p>
	 * In here, you should do all kinds of initialization logic, such as adding transient scripts,
	 * registering campaign plugins, adding custom sensor ghosts etc.
	 * <p>
	 * Original doc:
	 * <p>
	 * {@inheritDoc}
	 *
	 * @param newGame if true, a new campaign has just been started. Sector generation logic should only
	 *                be executed if this is true.
	 */
	public void onGameLoad(boolean newGame) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void onNewGame() {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void onNewGameAfterEconomyLoad() {
		
	}

	/**
	 * This gets called whenever the AI for a ship gets assigned.
	 *
	 * <p>Example:</p>
	 * <pre>
	 *     <code>
	 *			if(ship.getVariant().getHullVariantId().equals("myVariantId")){
	 *             return new PluginPick<ShipAIPlugin>(new MyAi(), CampaignPlugin.PickPriority.MOD_GENERAL)
	 *         }
	 *         return null;
	 *     </code>
	 * </pre>
	 * <p>
	 * Original doc:
	 * <p>
	 * {@inheritDoc}
	 *
	 * @param member fleetMemeber of the ship in question. Can be used to inform your decision if/which AI to choose
	 * @param ship the ship in question
	 * @return a plugin pick containing the chosen ship AI, or simply null to use the default AI
	 */
	public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
		return null;
	}

	/**
	 * This code gets executed whenever the game selects an autofire AI for a weapon, which is usually at start of combat
	 * This is mainly relevant if your weapon has some quirks that the regular AI doesn't understand.
	 *
	 * <p>Example:</p>
	 * <pre>
	 *     <code>
	 *         if(weapon.getId().equals("myWeaponId")){
	 *             return new PluginPick<>(new MyAi(), CampaignPlugin.PickPriority.MOD_GENERAL);
	 *         }
	 *         return null;
	 *     </code>
	 * </pre>
	 * <p>
	 * Original doc:
	 * <p>
	 * {@inheritDoc}
	 *
	 * @param weapon the weapon in question
	 * @return a plugin pick containing the chosen AI, or simply null to use the default
	 */
	public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
		return null;
	}

	public PluginPick<ShipAIPlugin> pickDroneAI(ShipAPI drone,
			ShipAPI mothership, DroneLauncherShipSystemAPI system) {
		return null;
	}

	/**
	 * This method is how you can assign custom AI to missiles. If you want to write your own missile AI,
	 * I would recommend having a look at the MagicMissileAI available in MagicLib for inspiration.
	 *
	 * <p>Example:</p>
	 * <pre>
	 *     <code>
	 *         if(missile.getProjectileSpecId().equals("myMissileProjId")){
	 *             return new PluginPick<>(new MyAi(), CampaignPlugin.PickPriority.MOD_GENERAL);
	 *         }
	 *         return null;
	 *     </code>
	 * </pre>
	 * Original doc:
	 * <p>
	 * {@inheritDoc}
	 *
	 * @param missile the missile in question. This is usually the main deciding factor.
	 * @param launchingShip the ship that launched the missile. This can usually be ignored
	 * @return a plugin pick containing the chosen AI, or simply null to use the default
	 */
	public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile,
			ShipAPI launchingShip) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void onNewGameAfterTimePass() {
		
	}

	public void configureXStream(XStream x) {
		
	}

	public void onNewGameAfterProcGen() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDevModeF8Reload() {
		// TODO Auto-generated method stub
		
	}

}
