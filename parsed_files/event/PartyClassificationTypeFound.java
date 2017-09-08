package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyClassificationTypeFound implements Event{

	private List<PartyClassificationType> partyClassificationTypes;

	public PartyClassificationTypeFound(List<PartyClassificationType> partyClassificationTypes) {
		this.setPartyClassificationTypes(partyClassificationTypes);
	}

	public List<PartyClassificationType> getPartyClassificationTypes()	{
		return partyClassificationTypes;
	}

	public void setPartyClassificationTypes(List<PartyClassificationType> partyClassificationTypes)	{
		this.partyClassificationTypes = partyClassificationTypes;
	}
}
