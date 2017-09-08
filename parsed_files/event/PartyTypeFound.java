package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyTypeFound implements Event{

	private List<PartyType> partyTypes;

	public PartyTypeFound(List<PartyType> partyTypes) {
		this.setPartyTypes(partyTypes);
	}

	public List<PartyType> getPartyTypes()	{
		return partyTypes;
	}

	public void setPartyTypes(List<PartyType> partyTypes)	{
		this.partyTypes = partyTypes;
	}
}
