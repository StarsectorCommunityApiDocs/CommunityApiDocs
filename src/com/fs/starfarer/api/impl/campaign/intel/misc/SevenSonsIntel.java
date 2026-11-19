package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.missions.askonia.SDSevenSons;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SevenSonsIntel extends BaseIntelPlugin {

	public static String VISITED = "$sd7s_foundSon";

	protected SectorEntityToken entity;
	
	public static void addSevenSonsIntelIfNeeded(String id, TextPanelAPI text) {
		addSevenSonsIntelIfNeeded(getEntity(id), text);
	}
	public static void addSevenSonsIntelIfNeeded(SectorEntityToken entity, TextPanelAPI text) {
		addSevenSonsIntelIfNeeded(entity, text, false);
	}
	public static void addSevenSonsIntelIfNeeded(SectorEntityToken entity, TextPanelAPI text, boolean quiet) {
		if (getSevenSonsIntel(entity) == null) {
			SevenSonsIntel intel = new SevenSonsIntel(entity);
			Global.getSector().getIntelManager().addIntel(intel, quiet, text);
		}
	}
	
	public static void setVisited(SectorEntityToken entity, TextPanelAPI text) {
		if (isVisited(entity)) return;
		
		entity.getMemoryWithoutUpdate().set(VISITED, true);
		if (text != null) {
			SevenSonsIntel intel = getSevenSonsIntel(entity);
			if (intel != null) {
				Global.getSector().getIntelManager().addIntelToTextPanel(intel, text);
			}
		}
	}
	
	public static boolean isVisited(SectorEntityToken entity) {
		return entity.getMemoryWithoutUpdate().getBoolean(VISITED) ||
				(entity.getMarket() != null && 
				entity.getMarket().getMemoryWithoutUpdate().getBoolean(VISITED));
	}
	
	
	public static SevenSonsIntel getSevenSonsIntel(SectorEntityToken entity) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(SevenSonsIntel.class)) {
			if (((SevenSonsIntel)intel).getEntity() == entity) return (SevenSonsIntel)intel;
		}
		return null;
	}
	
	public static SectorEntityToken getEntity(String id) {
		MarketAPI market = Global.getSector().getEconomy().getMarket(id);
		if (market != null) {
			if (market.getPlanetEntity() != null) {
				return market.getPlanetEntity();
			}
			return market.getPrimaryEntity();
		} else {
			return Global.getSector().getEntityById(id);
		}
	}
	
	
	public SevenSonsIntel(SectorEntityToken entity) {
		this.entity = entity;
	}
	
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
//		if (mode == ListInfoMode.INTEL) {
//			info.addPara("Located in the " + gate.getContainingLocation().getNameWithLowercaseTypeShort(), tc, initPad);
//			initPad = 0f;
//		}
		
		boolean visited = isVisited(entity);
		
		if (visited) {
			Color c = Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor();
			info.addPara("Visited", c, initPad);
			initPad = 0f;
		} else {
			info.addPara("Not Visited", tc, initPad);
			initPad = 0f;
		}
		
		if (mode == ListInfoMode.INTEL) {
			// not sure if this is needed
			//info.addPara("In the " + entity.getContainingLocation().getNameWithLowercaseType(), tc, 0f);
		}
		
		unindent(info);
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		String pre = "";
		String post = "";
		if (mode == ListInfoMode.MESSAGES) {
			pre = "Discovered: ";
		}
		if (mode == ListInfoMode.INTEL) {
			//post = " - " + entity.getContainingLocation().getNameWithTypeShort();
		}
		
		Color c = getTitleColor(mode);
		info.addPara(pre + getName() + post, c, 0f);
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		int _portraitWidth = 128;
		
		String id = entity.getId();
		
		//if (entity.getMarket() != null && !entity.getMarket().isPlanetConditionMarketOnly()) {
		//	id = entity.getMarket().getId();
		//}
		
		boolean visited = isVisited(entity);
		
		if (id.equals("nomios")) { // perseus
			Description desc = Global.getSettings().getDescription("sd7s_perseus", Type.CUSTOM);
			
			if (visited) {
				//info.addImage(Global.getSettings().getSpriteName("characters", "andrada"), width, opad);
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_gotFullPerseusStory")) {
					info.addPara(desc.getText3(), opad);
				}
				else {
					info.addPara(desc.getText2(), opad);
				}
			}
			else {
				info.addPara(desc.getText1(), opad);
				
				if(	Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_learnedDetailsOfPerseusEnd") ||
					Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_gotCydoniaBarStory") || 
					Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$metCydonia")) { 

					//info.addPara("Doctor Cydonia, now ensconced in Kanta's Den", opad);
					MarketAPI kantasDen = Global.getSector().getEconomy().getMarket("kantas_den");
						
					if (kantasDen != null) {
						if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$metCydonia")) {
							info.addImage(Global.getSettings().getSpriteName("characters", "doctor"), _portraitWidth, opad);
						}
						BaseHubMission.addStandardMarketDesc("Cydonia can be found " + kantasDen.getOnOrAt(), kantasDen, info, opad);
					}
				}	
			}
		}
		else if (id.equals("yesod")) { // achilles
			
			Description desc = Global.getSettings().getDescription("sd7s_achilles", Type.CUSTOM);
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("characters", "achilles_andrada"), _portraitWidth, opad);
				info.addPara(desc.getText2(), opad);
				
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_yesodDidSiyavongIntro")) {
					info.addPara("You learned that Finlay Siyavong and Persean League Fleet Intelligence are involved in the Achilles scheme.", opad);
				}
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_heardFortunaKatoHannanPlan")) {
					info.addPara("Fortuna Kato believes that Achilles will agree to joining the Persean League, which will help her end the dominance of Gens Hannan.", opad);
				}
				
				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_toldFortunaKatoWillHelp")) {
					info.addPara("You told Fortuna Kato that you would help her with her plan for Achilles.", opad);
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_deniedFortuna")) {
					info.addPara("You told Fortuna Kato that you disagreed with her plan for Achilles.", opad);
				}

			}
			else {
				info.addPara(desc.getText1(), opad);
			}
		}
		else if (id.equals("qaras")) { // jason
			
			Description desc = Global.getSettings().getDescription("sd7s_jason", Type.CUSTOM);
			if (visited) {
				//info.addImage(Global.getSettings().getSpriteName("characters", "andrada"), _portraitWidth, opad);
				info.addPara(desc.getText2(), opad);
			}
			else {
				info.addPara(desc.getText1(), opad);
			}
			
		}
		else if (id.equals("58966elysium")) { // hector
			
			Description desc = Global.getSettings().getDescription("sd7s_hector", Type.CUSTOM);
			if (visited) {

				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_thinksKilledHector")) { 
					info.addPara(desc.getText3(), opad);
					info.addImage(Global.getSettings().getSpriteName("characters", "hector_andrada"), _portraitWidth, opad);
				}
				else {
					info.addPara(desc.getText2(), opad);
					info.addImage(Global.getSettings().getSpriteName("characters", "hector_andrada"), _portraitWidth, opad);
				}
			}
			else {
				info.addPara(desc.getText1(), opad);
				info.addImage(Global.getSettings().getSpriteName("intel", "sd7s_elysium"), width, opad);
			}
			
		}
		else if (id.equals("station_galatia_academy")) { // hercules
			
			Description desc = Global.getSettings().getDescription("sd7s_hercules", Type.CUSTOM);
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("characters", "hercules_andrada"), _portraitWidth, opad);
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_allowedHerculesAssassination")) {
					info.addPara(desc.getText3(), opad); // dead
				}
				else {
					info.addPara(desc.getText2(), opad); // alive
				}
			}
			else {
				info.addPara(desc.getText1(), opad);
				info.addImage(Global.getSettings().getSpriteName("illustrations", "galatia_academy"), width, opad);
			}
			
		}
		else if (id.equals("17054irkalla")) { // leonidas
			
			Description desc = Global.getSettings().getDescription("sd7s_leonidas", Type.CUSTOM);
			if (visited) {
				info.addPara(desc.getText2(), opad);

				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidas")) {
					info.addPara("You took Leonidas from his life-support cradle and placed him in cryosleep.", opad);
					if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToOrcus")) {
						info.addPara("You transferred his cryopod to Hegemony authority via Orcus Rao.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToSiyavong")) {
						info.addPara("You handed his cryopod over to Persean League authority via Finlay Siyavong.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToNomios")) {
						info.addPara("You handed his cryopod over to the Nomios Cryosanctum for long-term safekeeping.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToKanta")) {
						info.addPara("You handed his cryopod over to the Warlord Kanta in exchange for her protection.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasOwnCryoRevival ")) {
						info.addPara("You took his cryopod to one of your own colonies and put Leonidas into an advanced recovery program run by your cryorevival facilities.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasOwnBunker  ")) {
						info.addPara("You took his cryopod to one of your own colonies and placed it in a bunker, where it - and he - can be kept safe.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToInesis")) {
						info.addPara("You sold his cryopod to Tri-Tachyon via Faro Inesis.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToKanta")) {
						info.addPara("You handed his cryopod over to the Warlord Kanta in exchange for her protection.", opad);
					}
					else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_tookLeonidasToKanta")) {
						info.addPara("You handed his cryopod over to the Warlord Kanta in exchange for her protection.", opad);
					}

				}
				else if (Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_leftLeonidas")) {
					info.addPara("You left Leonidas in his life-support cradle, in Irkalla, to whatever his fate will be.", opad);
				}
				else if (Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_killedLeonidas")) {
					info.addPara("You deactived Leonidas' life-support cradle. He is dead.", opad);
				}
				
			}
			else {
				info.addPara(desc.getText1(), opad);
				info.addImage(Global.getSettings().getSpriteName("intel", "sd7s_irkalla"), width, opad);
			}
			
		}
		else if (id.equals("orthrus")) { // pericles
			
			Description desc = Global.getSettings().getDescription("sd7s_pericles", Type.CUSTOM);
			if (visited) {
				
				info.addPara(desc.getText2(), opad);
				//info.addImage(Global.getSettings().getSpriteName("characters", "andrada"), width, opad);

				if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_triedToShootMoroz")) {
					info.addPara("Maeus Moroz unjustly blamed you for killing his son, then you tried and failed to shoot him in a bar on Orthrus.", opad);
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_shotMoroz")) {
					info.addPara("You shot and killed Maeus Moroz in a bar on Orthrus because he unjustly blamed you for killing his son.", opad);
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_challengedByMoroz")) { 
					info.addPara("Maeus Moroz blamed you for his son's death and challenged you to fleet combat.", opad);
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_maeusWillFightDolosRogue")) { 
					info.addPara("You calmed down Maeus Moroz and explained the situation. He now seeks vengeance upon Dolos Macario.", opad);
				}
				else if(Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$sd7s_maeusWillHelpFight")) { 
					info.addPara("You calmed down Maeus Moroz and explained the situation. He now seeks vengeance upon Dolos Macario and will aid you in fighting him.", opad);
				}
				
				if(Global.getSector().getMemoryWithoutUpdate().getBoolean("$sd7s_morozDefeatedByPC")) {
					info.addPara("You defeated the Wolves of Samarra, Maeus Moroz's fleet, in space combat.", opad);
				}
			}
			else {
				info.addPara(desc.getText1(), opad);
			}
			
		}
		else if (id.equals("qaras")) { // jason
			
			Description desc = Global.getSettings().getDescription("sd7s_jason", Type.CUSTOM);
			if (visited) {
				//info.addImage(Global.getSettings().getSpriteName("characters", "andrada"), width, opad);
				info.addPara(desc.getText2(), opad);
			}
			else {
				info.addPara(desc.getText1(), opad);
			}
			
		}
		
//		String buttonText = "Show shrines";
//		if (info.getIntelUI().isShowingCustomIntelSubset()) {
//			buttonText = "Go back";
//		}
//		
//		info.addSpacer(height - info.getHeightSoFar() - 20f - 20f);
//		ButtonAPI button = addGenericButton(info, width, buttonText, BUTTON_SHOW_SHRINES);
//		button.setShortcut(Keyboard.KEY_T, true);
		addShowSevenSonsButton(this, width, height, info);
		//addSonsIntelDialogButton(this, width, height, info);
		//addBulletPoints(info, ListInfoMode.IN_DESC);
		
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "seven_sons_intel");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_SEVEN_SONS);
		return tags;
	}
	
	public String getSortString() {
		return getName();
	}
	
	public String getName() {
//		String onOrAt = "on";
//		if (entity.getMarket() != null && !entity.getMarket().isPlanetConditionMarketOnly()) {
//			onOrAt = entity.getMarket().getOnOrAt();
//		}
//		return "Luddic Shrine " + onOrAt + " " + entity.getName();
		return "Andrada's Heir - " + entity.getName();
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getFaction(Factions.DIKTAT);
	}

	public String getSmallDescriptionTitle() {
		//return getName() + " - " + entity.getContainingLocation().getNameWithTypeShort();
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return entity;
	}
	
	@Override
	public boolean shouldRemoveIntel() {
		return false;
	}

	@Override
	public String getCommMessageSound() {
		return "ui_discovered_entity";
	}

	public SectorEntityToken getEntity() {
		return entity;
	}
	
	public static String BUTTON_SHOW_SEVEN_SONS = "button_show_seven_sons";
	public static String BUTTON_SONS_INTEL_DIALOG = "button_sons_intel_dialog";
	public static String BUTTON_SONS_DECRYPT_DIALOG = "button_sons_decrypt_dialog";
	
	public static void addSonsIntelDialogButton(IntelInfoPlugin curr, float width, float height, TooltipMakerAPI info) {
		if (!Global.getSector().getIntelManager().hasIntelOfClass(SDSevenSons.class)) return;
		
		if (!info.getIntelUI().isShowingCustomIntelSubset() && 
				curr instanceof SevenSonsIntel) {
			return;
		}
		
		String buttonText = "Access ANDRADA files";
		
		//if (info.getIntelUI().isShowingCustomIntelSubset()) {
		//	buttonText = "Go back";
		//}
		
		info.addSpacer(height - info.getHeightSoFar() - 20f - 20f - 20f -10f);
		ButtonAPI button = ((BaseIntelPlugin)curr).addGenericButton(info, width, buttonText, BUTTON_SONS_INTEL_DIALOG);
		//button.setShortcut(Keyboard.KEY_T, true);
	}
	
	public static void addSonsDecryptionDialogButton(IntelInfoPlugin curr, float width, float height, TooltipMakerAPI info) {
		if (!Global.getSector().getIntelManager().hasIntelOfClass(SDSevenSons.class)) return;
		
		if (!info.getIntelUI().isShowingCustomIntelSubset() && 
				curr instanceof SevenSonsIntel) {
			return;
		}
		
		String buttonText = "Review the ANDRADA workspace";

		//info.addSpacer(height - info.getHeightSoFar() - 20f -10f ); // - 20f - 20f -10f);
		ButtonAPI button = ((BaseIntelPlugin)curr).addGenericButton(info, width, buttonText, BUTTON_SONS_DECRYPT_DIALOG);
	}
	
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_SHOW_SEVEN_SONS) {
			toggleSonsList(this, ui);
			return;
		}
		else if (buttonId == BUTTON_SONS_INTEL_DIALOG) {
			// launch dialog here.
			RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("SevenSonsAndradaFileBegin");
			
			ui.showDialog(Global.getSector().getPlayerFleet(), "SevenSonsAndradaFileBegin");
			
			/*Global.getSector().getCampaignUI().showInteractionDialogFromCargo(plugin, 
				Global.getSector().getPlayerFleet(), new DismissDialogDelegate() {
					@Override
					public void dialogDismissed() {
					}
				});*/
			return;
		}
		else if (buttonId == BUTTON_SONS_INTEL_DIALOG) {
			RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("SevenSonsAndradaDecryptBegin");
			ui.showDialog(Global.getSector().getPlayerFleet(), "SevenSonsAndradaDecryptBegin");
		}
		super.buttonPressConfirmed(buttonId, ui);
	}
	
	public static void addShowSevenSonsButton(IntelInfoPlugin curr, float width, float height, TooltipMakerAPI info) {
		if (!Global.getSector().getIntelManager().hasIntelOfClass(SDSevenSons.class)) return;
		
		if (!info.getIntelUI().isShowingCustomIntelSubset() && 
				curr instanceof SevenSonsIntel) {
			return;
		}
		
		String buttonText = "Show Seven Sons";
		if (info.getIntelUI().isShowingCustomIntelSubset()) {
			buttonText = "Go back";
		}
		
		info.addSpacer(height - info.getHeightSoFar() - 20f - 20f);
		ButtonAPI button = ((BaseIntelPlugin)curr).addGenericButton(info, width, buttonText, BUTTON_SHOW_SEVEN_SONS);
		button.setShortcut(Keyboard.KEY_T, true);
	}
	
	public static void toggleSonsList(IntelInfoPlugin curr, IntelUIAPI ui) {
		if (ui.isShowingCustomIntelSubset()) {
			ui.updateIntelList(true);
			ui.updateUIForItem(curr);
			//for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(LuddicShrineIntel.class)) {
			for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(SevenSonsIntel.class)) {
				ui.selectItem(intel);
				break;
			}
		} else {
			List<IntelInfoPlugin> show = new ArrayList<IntelInfoPlugin>();
			for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(SevenSonsIntel.class)) {
				show.add(intel);
			}
			//for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(SevenSonsIntel.class)) {
			//	show.add(intel);
			//}
			Global.getSector().getIntelManager().sortIntel(show);
			ui.updateIntelList(true, show);
			ui.updateUIForItem(curr);
		}
		return;
	}
	
}
