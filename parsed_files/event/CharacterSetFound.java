package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CharacterSetFound implements Event{

	private List<CharacterSet> characterSets;

	public CharacterSetFound(List<CharacterSet> characterSets) {
		this.setCharacterSets(characterSets);
	}

	public List<CharacterSet> getCharacterSets()	{
		return characterSets;
	}

	public void setCharacterSets(List<CharacterSet> characterSets)	{
		this.characterSets = characterSets;
	}
}
