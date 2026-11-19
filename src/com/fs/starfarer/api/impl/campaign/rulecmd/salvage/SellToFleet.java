package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;
import java.util.Random;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class SellToFleet extends BaseCommandPlugin {
	
	public static float RESUPPLY_FLEET_SELL_VALUE_MULT = 1f;
	public static float SCAVENGER_SELL_VALUE_MULT = 0.25f;
	public static float MAX_QUANTITY_AS_FRACTION_OF_CARGO_CAPACITY = 0.5f;
	
	
	protected CampaignFleetAPI playerFleet;
	protected CampaignFleetAPI fleet;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected PersonAPI person;
	protected FactionAPI faction;

	protected boolean buysStuff;
	protected float valueMult;
	protected float maxCargo;
	protected float maxFuel;
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();
		
		if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
			fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			valueMult = fleet.getMemoryWithoutUpdate().getFloat(MemFlags.MEMORY_KEY_FLEET_BUYS_STUFF_RATE);
			if (valueMult <= 0f) {
				fleet = null;
			}
			maxCargo = (int) fleet.getCargo().getMaxCapacity() * MAX_QUANTITY_AS_FRACTION_OF_CARGO_CAPACITY;
			maxCargo -= getAlreadyBoughtCargo();
			maxFuel = (int) fleet.getCargo().getMaxFuel() * MAX_QUANTITY_AS_FRACTION_OF_CARGO_CAPACITY;
			maxFuel -= getAlreadyBoughtFuel();
		}
		
		if (command.equals("willBuyStuff")) {
			return willBuyStuff();
		} else if (command.equals("selectStuff")) {
			return selectStuff();
		}
		
		return true;
	}

	protected boolean willBuyStuff() {
		return fleet != null && fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_FLEET_BUYS_STUFF) &&
				(maxCargo >= 1 || maxFuel >= 1);
	}
	
	protected int getAlreadyBoughtCargo() {
		if (fleet == null) return 0;
		String key = "$alreadyBoughtCargo";
		return fleet.getMemoryWithoutUpdate().getInt(key);
	}
	
	protected void addAlreadyBoughtCargo(int qty) {
		if (fleet == null) return;
		String key = "$alreadyBoughtCargo";
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		mem.set(key, mem.getInt(key) + qty);
	}
	
	protected int getAlreadyBoughtFuel() {
		if (fleet == null) return 0;
		String key = "$alreadyBoughtFuel";
		return fleet.getMemoryWithoutUpdate().getInt(key);
	}
	
	protected void addAlreadyBoughtFuel(int qty) {
		if (fleet == null) return;
		String key = "$alreadyBoughtFuel";
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		mem.set(key, mem.getInt(key) + qty);
	}
	

	protected boolean selectStuff() {
		CargoAPI copy = Global.getFactory().createCargo(false);
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec == null) continue;
			if (spec.isPersonnel()) continue;
			if (spec.isNonEcon()) continue;
			if (spec.isMeta()) continue;
			
			copy.addFromStack(stack);
		}
		copy.sort();
		
		copy = playerCargo;
		final CargoAPI copy2 = copy;
		
		
		final float width = 310f;
		dialog.showCargoPickerDialog("Select goods to transfer", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
			@Override
			public boolean withLogistics() {
				return true;
			}
			@Override
			public boolean withReset() {
				return true;
			}
			@Override
			public void performReset(CargoAPI selected) {
				// the dialog already cancels the transaction, nothing special to do here
			}
			
			@Override
			public QuickTransferStackSizeAdjustor getStackSizeAdjustor() {
				return new QuickTransferStackSizeAdjustor() {
					@Override
					public float returnDesiredStackSize(float currQuantity, CargoStackAPI clickedStack, 
									CargoAPI from, CargoAPI other) {
						if (from == playerCargo && clickedStack != null) {
							if (clickedStack.isFuelStack()) {
								float remaining = maxFuel - other.getFuel();
								if (remaining >= 1f) {
									return (int)Math.min(currQuantity, remaining);
								}
							} else {
								other.updateSpaceUsed();
								float remaining = maxCargo - other.getSpaceUsed();
								if (remaining >= 1f) {
									return (int)Math.min(currQuantity, remaining);
								}
							}
						} else if (other == playerCargo && clickedStack != null) {
							if (clickedStack.isFuelStack()) {
								float extra = from.getFuel() - maxFuel;
								if (extra >= 1f) {
									return (int)Math.ceil(Math.min(currQuantity, extra));
								}
							} else {
								other.updateSpaceUsed();
								float extra = from.getSpaceUsed() - maxCargo;
								if (extra >= 1f) {
									return (int)Math.ceil(Math.min(currQuantity, extra));
								}
							}
						}
						return currQuantity;
					}
				};
			}
			
			@Override
			public CargoFilter getFilter() {
				return new CargoFilter() {
					@Override
					public boolean accept(CargoStackAPI stack) {
						CommoditySpecAPI spec = stack.getResourceIfResource();
						if (spec == null) return false;
						if (spec.isPersonnel()) return false;
						if (spec.isNonEcon()) return false;
						if (spec.isMeta()) return false;
						return true;
					}
				};
			}

			@Override
			public boolean enableConfirm(CargoAPI selected) {
				if (selected.isEmpty()) return false;
				return selected.getSpaceUsed() <= maxCargo && selected.getFuel() <= maxFuel;
			}
			
			public void pickedCargo(CargoAPI cargo) {
				if (cargo.isEmpty()) {
					cancelledCargoSelection();
					return;
				}
				
				cargo.sort();
				cargo.updateSpaceUsed();
				
				addAlreadyBoughtCargo((int)cargo.getSpaceUsed());
				addAlreadyBoughtFuel((int)cargo.getFuel());
				
				Random random = new Random(Misc.getSalvageSeed(fleet));
				for (CargoStackAPI stack : cargo.getStacksCopy()) {
					if (copy2 != playerCargo) {
						playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
					}
					if (stack.isCommodityStack()) { // should be always, but just in case
						int num = (int) stack.getSize();
						AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), num, text);
						
						float retain = (int)(stack.getSize() * (0.35f + random.nextFloat() * 0.65f));
						if (retain < 1) retain = 0;
						stack.setSize(retain);
					}
				}
				
				BaseSalvageSpecial.addExtraSalvage(fleet, cargo);
				
				float credits = computeCreditValue(cargo);

				if (credits > 0) {
					playerCargo.getCredits().add(credits);
					AddRemoveCommodity.addCreditsGainText((int)credits, text);
				}
				
				FireBest.fire(null, dialog, memoryMap, "SellToFleetFinished");
			}
			public void cancelledCargoSelection() {
			}
			public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
			
				
				float opad = 10f;
				int credits = (int) computeCreditValue(combined);
				int quantity = (int) combined.getSpaceUsed();
				int fuel = (int) combined.getFuel();

				Color h = Misc.getHighlightColor();
				Color neg = Misc.getNegativeHighlightColor();
				
//				panel.setParaFontOrbitron();
//				panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), 1f);
//				panel.setParaFontDefault();
//				panel.addImage(faction.getLogo(), width * 1f, 3f);
				panel.setParaSmallInsignia();
				panel.addPara("You will receive %s of the base value of the commodities sold.", opad, 
						h, "" + (int) (valueMult * 100f) + "%");
				
				//panel.beginGridFlipped(width, 1, 40f, 10f);
				panel.beginGrid(width - opad, 1);
				panel.setGridFontSmallInsignia();
				//panel.beginGrid(150f, 1);
				
				boolean overCargo = combined.getSpaceUsed() > maxCargo;
				boolean overFuel = combined.getFuel() > maxFuel;
				
				panel.addToGrid(0, 0, "Cargo selected", "" + quantity + " / " + (int) maxCargo, overCargo ? neg : h);
				panel.addToGrid(0, 1, "Fuel selected", "" + fuel + " / " + (int) maxFuel, overFuel ? neg : h);
				panel.addToGrid(0, 3, "Credits: ", Misc.getDGSCredits(credits));
				panel.addGrid(opad + opad);
				
				if (overCargo || overFuel) {
					panel.addPara("Selected goods quantity exceeds capacity of recipient.", neg, opad * 2f);
				}
				
//				panel.addPara("The total for the selected goods is %s.",
//						opad * 1f, h,
//						Misc.getWithDGS(credits) + Strings.C);
			}
		});
		
		return true;
	}

	protected float computeCreditValue(CargoAPI cargo) {
		float total = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null) {
				total += spec.getBasePrice() * stack.getSize();
			}
		}
		total *= valueMult;
		return total;
	}
	
	
}















