package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyAttributeFound implements Event{

	private List<PartyAttribute> partyAttributes;

	public PartyAttributeFound(List<PartyAttribute> partyAttributes) {
		this.setPartyAttributes(partyAttributes);
	}

	public List<PartyAttribute> getPartyAttributes()	{
		return partyAttributes;
	}

	public void setPartyAttributes(List<PartyAttribute> partyAttributes)	{
		this.partyAttributes = partyAttributes;
	}
}
