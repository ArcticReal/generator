package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyContactMechFound implements Event{

	private List<PartyContactMech> partyContactMechs;

	public PartyContactMechFound(List<PartyContactMech> partyContactMechs) {
		this.setPartyContactMechs(partyContactMechs);
	}

	public List<PartyContactMech> getPartyContactMechs()	{
		return partyContactMechs;
	}

	public void setPartyContactMechs(List<PartyContactMech> partyContactMechs)	{
		this.partyContactMechs = partyContactMechs;
	}
}
