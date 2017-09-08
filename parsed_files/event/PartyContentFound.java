package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyContentFound implements Event{

	private List<PartyContent> partyContents;

	public PartyContentFound(List<PartyContent> partyContents) {
		this.setPartyContents(partyContents);
	}

	public List<PartyContent> getPartyContents()	{
		return partyContents;
	}

	public void setPartyContents(List<PartyContent> partyContents)	{
		this.partyContents = partyContents;
	}
}
