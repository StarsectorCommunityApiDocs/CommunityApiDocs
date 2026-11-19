package com.fs.starfarer.api.impl.campaign.world;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin.AbyssalLightParams;
import com.fs.starfarer.api.impl.campaign.enc.AbyssalLightDwellerEPEC;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

public class JuvenileDweller {

	public static Vector2f LOCATION = new Vector2f(-30000, -50000);

	public static void spawnIfNeeded() {
		String key = "$spawnedJuvenileDweller";
		if (Global.getSector().getMemoryWithoutUpdate().contains(key)) return;
		Global.getSector().getMemoryWithoutUpdate().set(key, true);
		
		float size = 300f;
		AbyssalLightParams params = new AbyssalLightParams(size, size);
		params.permanent = true;
		params.frequencyChangeMult = 0.25f;
		
		SectorEntityToken light = Global.getSector().getHyperspace().addCustomEntity(
								null, null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL, params);
		light.setLocation(LOCATION.x, LOCATION.y);
		
		params.color = AbyssalLightDwellerEPEC.DWELLER_LIGHT_COLOR;
		light.getMemoryWithoutUpdate().set("$juvenileDweller", true);
		light.addTag(Tags.DWELLER_LIGHT);
		
		long seed = StarSystemGenerator.random.nextLong();
		light.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
	}
}

	
	
	
	
	
	