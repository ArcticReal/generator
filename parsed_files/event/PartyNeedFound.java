package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyNeedFound implements Event{

	private List<PartyNeed> partyNeeds;

	public PartyNeedFound(List<PartyNeed> partyNeeds) {
		this.setPartyNeeds(partyNeeds);
	}

	public List<PartyNeed> getPartyNeeds()	{
		return partyNeeds;
	}

	public void setPartyNeeds(List<PartyNeed> partyNeeds)	{
		this.partyNeeds = partyNeeds;
	}
}
