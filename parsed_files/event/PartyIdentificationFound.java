package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyIdentificationFound implements Event{

	private List<PartyIdentification> partyIdentifications;

	public PartyIdentificationFound(List<PartyIdentification> partyIdentifications) {
		this.setPartyIdentifications(partyIdentifications);
	}

	public List<PartyIdentification> getPartyIdentifications()	{
		return partyIdentifications;
	}

	public void setPartyIdentifications(List<PartyIdentification> partyIdentifications)	{
		this.partyIdentifications = partyIdentifications;
	}
}
