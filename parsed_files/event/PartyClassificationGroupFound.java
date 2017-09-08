package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyClassificationGroupFound implements Event{

	private List<PartyClassificationGroup> partyClassificationGroups;

	public PartyClassificationGroupFound(List<PartyClassificationGroup> partyClassificationGroups) {
		this.setPartyClassificationGroups(partyClassificationGroups);
	}

	public List<PartyClassificationGroup> getPartyClassificationGroups()	{
		return partyClassificationGroups;
	}

	public void setPartyClassificationGroups(List<PartyClassificationGroup> partyClassificationGroups)	{
		this.partyClassificationGroups = partyClassificationGroups;
	}
}
