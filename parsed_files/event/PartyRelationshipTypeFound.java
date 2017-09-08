package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyRelationshipTypeFound implements Event{

	private List<PartyRelationshipType> partyRelationshipTypes;

	public PartyRelationshipTypeFound(List<PartyRelationshipType> partyRelationshipTypes) {
		this.setPartyRelationshipTypes(partyRelationshipTypes);
	}

	public List<PartyRelationshipType> getPartyRelationshipTypes()	{
		return partyRelationshipTypes;
	}

	public void setPartyRelationshipTypes(List<PartyRelationshipType> partyRelationshipTypes)	{
		this.partyRelationshipTypes = partyRelationshipTypes;
	}
}
