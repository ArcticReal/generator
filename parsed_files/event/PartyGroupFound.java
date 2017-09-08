package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyGroupFound implements Event{

	private List<PartyGroup> partyGroups;

	public PartyGroupFound(List<PartyGroup> partyGroups) {
		this.setPartyGroups(partyGroups);
	}

	public List<PartyGroup> getPartyGroups()	{
		return partyGroups;
	}

	public void setPartyGroups(List<PartyGroup> partyGroups)	{
		this.partyGroups = partyGroups;
	}
}
