package com.fs.starfarer.api.impl.campaign.shared;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class PersonBountyEventData {
	
	public static int BUFFER_DEFAULT = 1;
	public static int BUFFER_MAX = 3;
	public static int BUFFER_PER_SUCCESS = 2;

	private List<String> participatingFactions = new ArrayList<String>();
	
	private int level = 0;
	private int successesThisLevel = 0;
	private int buffer = BUFFER_DEFAULT;

	public PersonBountyEventData() {
		addParticipatingFaction(Factions.HEGEMONY);
		addParticipatingFaction(Factions.DIKTAT);
		addParticipatingFaction(Factions.LUDDIC_CHURCH);
		addParticipatingFaction(Factions.TRITACHYON);
		addParticipatingFaction(Factions.INDEPENDENT);
		addParticipatingFaction(Factions.PERSEAN);
	}

	public void reportSuccess(int completedLevel) {
		if (completedLevel < level) {
			if (buffer > 0) {
				buffer--;
			} else {
				successesThisLevel--;
				// don't overdo toning it down
//				if (completedLevel < level - 1) {
//					successesThisLevel--;
//				}
			}
		}
		
		// do not increment successesThisLevel unless bounty level is higher
		
		if (completedLevel >= level + 1) {
			buffer += BUFFER_PER_SUCCESS;
			successesThisLevel++;
//			if (completedLevel >= level + 1) {
//				successesThisLevel++;
//			}
			if (completedLevel >= level + 2) {
				successesThisLevel++;
				buffer++;
			}
		} else if (completedLevel == level) {
			buffer++;
		}
		
		if (buffer > BUFFER_MAX) buffer = BUFFER_MAX;
		
		int threshold = getThresholdForLevel(level);
		if (successesThisLevel >= threshold) {
			level++;
			successesThisLevel = 0;
			buffer = BUFFER_DEFAULT;
		} else if (successesThisLevel < 0) {
			level--;
			successesThisLevel = 0;
			buffer = BUFFER_DEFAULT;
		}
		
		if (level < 0) level = 0;
		if (level > 10) level = 10;
	}
	
	public int getThresholdForLevel(int level) {
//		if (level == 0) return 2;
//		if (level == 1) return 2;
//		if (level == 2) return 3;
//		
//		if (level == 3) return 4;
//		if (level == 4) return 5;
//		if (level == 5) return 6;
//		if (level == 6) return 7;
//		
//		if (level == 7) return 8;
//		if (level == 8) return 9;
//		if (level == 9) return 10;
//		
//		return 11; // 67 total
		
		if (level == 0) return 2;
		if (level == 1) return 2;
		if (level == 2) return 2;
		
		if (level == 3) return 2;
		if (level == 4) return 2;
		if (level == 5) return 2;
		if (level == 6) return 2;
		
		if (level == 7) return 3;
		if (level == 8) return 3;
		if (level == 9) return 3;		
		
		return 4;
//		if (level == 7) return 4;
//		if (level == 8) return 5;
//		if (level == 9) return 6;
//		
//		return 6; // 39 total
	}
	
	public int getBuffer() {
		return buffer;
	}

	public int getLevel() {
		//if (true) return 10;
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public void setSuccessesThisLevel(int successesThisLevel) {
		this.successesThisLevel = successesThisLevel;
	}

	public List<String> getParticipatingFactions() {
		return participatingFactions;
	}
	
	public void addParticipatingFaction(String factionId) {
		participatingFactions.add(factionId);
	}
	
	public void removeParticipatingFaction(String factionId) {
		participatingFactions.remove(factionId);
	}
	
	public boolean isParticipating(String factionId) {
		return participatingFactions.contains(factionId);
	}

	public int getSuccessesThisLevel() {
		return successesThisLevel;
	}
	
}

