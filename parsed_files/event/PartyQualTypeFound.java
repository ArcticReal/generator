package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyQualTypeFound implements Event{

	private List<PartyQualType> partyQualTypes;

	public PartyQualTypeFound(List<PartyQualType> partyQualTypes) {
		this.setPartyQualTypes(partyQualTypes);
	}

	public List<PartyQualType> getPartyQualTypes()	{
		return partyQualTypes;
	}

	public void setPartyQualTypes(List<PartyQualType> partyQualTypes)	{
		this.partyQualTypes = partyQualTypes;
	}
}
