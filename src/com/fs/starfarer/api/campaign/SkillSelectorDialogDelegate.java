package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.characters.SkillSpecAPI;

public interface SkillSelectorDialogDelegate {

	String getTitle();
	List<SkillSpecAPI> getSkills();
	int getMaxSelectable();
	void skillsSelected(List<SkillSpecAPI> selectedSkills);
	void skillSelectionCancelled();

}
