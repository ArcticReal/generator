package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyStatusFound implements Event{

	private List<PartyStatus> partyStatuss;

	public PartyStatusFound(List<PartyStatus> partyStatuss) {
		this.setPartyStatuss(partyStatuss);
	}

	public List<PartyStatus> getPartyStatuss()	{
		return partyStatuss;
	}

	public void setPartyStatuss(List<PartyStatus> partyStatuss)	{
		this.partyStatuss = partyStatuss;
	}
}
