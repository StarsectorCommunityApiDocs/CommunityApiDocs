package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.TooltipMakerAPI;



public interface CargoPickerListener {
	
	public interface CargoFilter {
		boolean accept(CargoStackAPI stack);
	}
	
	public interface QuickTransferStackSizeAdjustor {
		float returnDesiredStackSize(float currQuantity, CargoStackAPI clickedStack, CargoAPI from, CargoAPI other);
	}
	
	void pickedCargo(CargoAPI cargo);
	void cancelledCargoSelection();
	void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined);
	default boolean enableConfirm(CargoAPI selected) {
		return !selected.isEmpty();
	}
	default void performReset(CargoAPI selected) {
		
	}
	default boolean enableReset(CargoAPI selected) {
		return !selected.isEmpty();
	}
	default CargoFilter getFilter() {
		return null;
	}
	default boolean withLogistics() {
		return false;
	}
	default boolean withReset() {
		return false;
	}
	default QuickTransferStackSizeAdjustor getStackSizeAdjustor() {
		return null;
	}
}
