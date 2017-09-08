package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyContentTypeFound implements Event{

	private List<PartyContentType> partyContentTypes;

	public PartyContentTypeFound(List<PartyContentType> partyContentTypes) {
		this.setPartyContentTypes(partyContentTypes);
	}

	public List<PartyContentType> getPartyContentTypes()	{
		return partyContentTypes;
	}

	public void setPartyContentTypes(List<PartyContentType> partyContentTypes)	{
		this.partyContentTypes = partyContentTypes;
	}
}
