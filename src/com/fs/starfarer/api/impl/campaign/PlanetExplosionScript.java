package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionFleetDamage;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionParams;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.SDResolution;
import com.fs.starfarer.api.util.Misc;

public class PlanetExplosionScript implements EveryFrameScript {

	protected boolean done = false;
	protected boolean playedWindup = false;
	
	protected SectorEntityToken explosion = null;
	protected boolean setLavaPlanetTex = false;
	protected float delay = 0.5f;
	protected float delay2 = 1f;
	protected float elapsed = 0f;
	protected boolean changedType = false;
	
	protected PlanetAPI planet;
	protected transient SoundAPI windup = null;
	
	
	
	public PlanetExplosionScript(PlanetAPI planet) {
		this.planet = planet;
		
		delay = 1.2f;
		
		PlanetSpecAPI lava = null;
		PlanetSpecAPI minor = null;
		for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
			if (spec.getPlanetType().equals(Planets.PLANET_LAVA)) {
				lava = spec;
			}
			if (spec.getPlanetType().equals(Planets.PLANET_LAVA_MINOR)) {
				minor = spec;
			}
		}
		if (lava != null) {
			planet.getSpec().setShieldTexture(lava.getTexture());
			if (minor != null) planet.getSpec().setShieldTexture2(minor.getTexture());
			planet.getSpec().setShieldThickness(0.001f);
			planet.getSpec().setShieldColor(new Color(255,255,255,0));
			planet.applySpecChanges();
		}
		
		Global.getSector().addScript(this);
		

		MarketAPI market = planet.getMarket();
		if (!market.isPlanetConditionMarketOnly()) {
			DecivTracker.removeColony(market, false);
			SDResolution.cleanUpFleetsAffectedByPlanetOrFactionDestruction();
		}
		planet.addTag(Tags.NON_CLICKABLE);
		
	}
	
	protected Object readResolve() {
		return this;
	}


	public void advance(float amount) {
		if (done) return;
		
		elapsed += amount;
		
		float maxDur = 25f;
		
		float b = elapsed / maxDur;
		
		boolean explode = false;
		if (b < 0.75f) {
			b = b / 0.75f;
		} else {
			if (!changedType) {
				float tilt = planet.getSpec().getTilt();
				float pitch = planet.getSpec().getPitch();
				//planet.changeType(Planets.PLANET_LAVA_MINOR, Misc.random);
				planet.changeType(Planets.PLANET_LAVA, Misc.random);
				planet.getSpec().setTilt(tilt);
				planet.getSpec().setPitch(pitch);
				
				PlanetSpecAPI lava = null;
				PlanetSpecAPI minor = null;
				for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
					if (spec.getPlanetType().equals(Planets.PLANET_LAVA)) {
						lava = spec;
					}
					if (spec.getPlanetType().equals(Planets.PLANET_LAVA_MINOR)) {
						minor = spec;
					}
				}
				if (lava != null) {
					planet.getSpec().setShieldTexture(lava.getTexture());
					if (minor != null) planet.getSpec().setShieldTexture2(minor.getTexture());
					planet.getSpec().setShieldThickness(0.001f);
					planet.getSpec().setShieldColor(new Color(255,255,255,0));
				}
				
				planet.applySpecChanges();
				changedType = true;
				explode = true;
			}
			b = 1f - (b - 0.75f) / 0.25f;
		}
		
		if (b < 0f) b = 0f;
		if (b > 1f) b = 1f;

		
		if (explosion == null && amount > 0) {
			float untilWindup = maxDur * 0.75f - elapsed;
			float windupDur = Global.getSettings().getFloat("planetExplosionWindupDur");
			if (untilWindup <= windupDur && (!playedWindup || windup == null)) {
				if (planet.isInCurrentLocation()) {
					windup = Global.getSoundPlayer().playSound("planet_explosion_windup", 1f, 1f, planet.getLocation(), Misc.ZERO);
				}
				playedWindup = true;
			}
			Global.getSoundPlayer().playLoop("planet_explosion_loop", planet, 0.25f + b * 0.75f, 1f, planet.getLocation(), Misc.ZERO);
		}		
		

		if (b > 0.85f) b = 0.85f;
		//if (b > 0.95f) b = 0.3f + 0.7f * (1f - b) / 0.05f;
		
		float bOrig = b;
		b = Math.min(b * 2f, 1f);
		b *= 0.2f;
		int a = (int)(b * 255f);
		if (a > 255) a = 255;

		float bExtra = 0f;
		if (bOrig > 0.75f) {
			bExtra = (bOrig - 0.75f) / 0.25f;
		}
		//bExtra = bOrig * bOrig;
		int aExtra = (int)(bExtra * 255f);
		if (aExtra > 255) aExtra = 255; 
		
		planet.getSpec().setShieldColor(new Color(255,255,255,aExtra));
		planet.getSpec().setShieldColor2(new Color(255,255,255,a));
		
		
		float maxJitter = 50f;
		int numJitter = (int)(20 * bOrig);
		numJitter = 7;
		float maxExpand = maxJitter / planet.getRadius();
		maxExpand *= 0.25f;
		planet.getSpec().setShieldThickness(0.001f + maxExpand * bOrig);
		planet.getSpec().setShieldThickness2(0.001f + maxExpand * bOrig);
		
//		if (bOrig > 0.9f && !setLavaPlanetTex) {
//			setLavaPlanetTex = true;
//			for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
//				if (spec.getPlanetType().equals(Planets.PLANET_LAVA)) {
//					planet.getSpec().setTexture(spec.getTexture());
//					break;
//				}
//			}	
//		}
		
		planet.applySpecChanges();
		
		
		float angVelExtra = bOrig * bOrig * 360f;
		float angle = planet.getAngle();
		angle += angVelExtra * amount;
		planet.setAngle(angle);
		
		planet.advance(amount);
		planet.setShieldTextureJitter(bOrig * maxJitter, numJitter);
		
		if (elapsed > maxDur) {
			planet.getSpec().setShieldTexture(null);
			planet.getSpec().setShieldTexture2(null);
			planet.applySpecChanges();
			planet.setShieldTextureJitter(0, 0);

			done = true;
		}
		
		if (explode) {
			MarketAPI market = planet.getMarket();
			if (!market.isPlanetConditionMarketOnly()) {
				for (SectorEntityToken curr : market.getConnectedEntities()) {
					if (!(curr instanceof PlanetAPI)) {
						curr.getContainingLocation().removeEntity(curr);
					}
				}
			}
			planet.removeTag(Tags.NON_CLICKABLE);
			
			planet.setCustomDescriptionId("planet_sindria_exploded");
			planet.setCustomInteractionDialogImageVisual(null);
			
			if (windup != null && windup.isPlaying()) {
				windup.stop();
			}
			
			LocationAPI cl = planet.getContainingLocation();
			Vector2f loc = planet.getLocation();
			Vector2f vel = planet.getVelocity();

			float size = planet.getRadius() + 250f;
			Color color = new Color(255, 165, 100);
			ExplosionParams params = new ExplosionParams(color, cl, loc, size, 2f);
			params.baseSizeMult = 2.5f;
			params.initialRadiusMult = 3f;
			params.initialHitGlowMult = 5f;
			params.particleNumMult = 6f;
			params.useInitialSqrtParticleDistribution = true;
			params.explosionSoundId = "planet_explosion";
			params.damage = ExplosionFleetDamage.EXTREME;

			explosion = cl.addCustomEntity(Misc.genUID(), "Sindria Explosion", 
					Entities.EXPLOSION, Factions.NEUTRAL, params);
			explosion.setLocation(loc.x, loc.y);
			
		}
	}
	
	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
}




