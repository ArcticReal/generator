package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyRelationshipFound implements Event{

	private List<PartyRelationship> partyRelationships;

	public PartyRelationshipFound(List<PartyRelationship> partyRelationships) {
		this.setPartyRelationships(partyRelationships);
	}

	public List<PartyRelationship> getPartyRelationships()	{
		return partyRelationships;
	}

	public void setPartyRelationships(List<PartyRelationship> partyRelationships)	{
		this.partyRelationships = partyRelationships;
	}
}
