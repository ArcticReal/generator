package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyIdentificationTypeFound implements Event{

	private List<PartyIdentificationType> partyIdentificationTypes;

	public PartyIdentificationTypeFound(List<PartyIdentificationType> partyIdentificationTypes) {
		this.setPartyIdentificationTypes(partyIdentificationTypes);
	}

	public List<PartyIdentificationType> getPartyIdentificationTypes()	{
		return partyIdentificationTypes;
	}

	public void setPartyIdentificationTypes(List<PartyIdentificationType> partyIdentificationTypes)	{
		this.partyIdentificationTypes = partyIdentificationTypes;
	}
}
