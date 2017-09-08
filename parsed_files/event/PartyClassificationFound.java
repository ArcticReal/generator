package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyClassificationFound implements Event{

	private List<PartyClassification> partyClassifications;

	public PartyClassificationFound(List<PartyClassification> partyClassifications) {
		this.setPartyClassifications(partyClassifications);
	}

	public List<PartyClassification> getPartyClassifications()	{
		return partyClassifications;
	}

	public void setPartyClassifications(List<PartyClassification> partyClassifications)	{
		this.partyClassifications = partyClassifications;
	}
}
