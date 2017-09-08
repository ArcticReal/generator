package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyIcsAvsOverrideFound implements Event{

	private List<PartyIcsAvsOverride> partyIcsAvsOverrides;

	public PartyIcsAvsOverrideFound(List<PartyIcsAvsOverride> partyIcsAvsOverrides) {
		this.setPartyIcsAvsOverrides(partyIcsAvsOverrides);
	}

	public List<PartyIcsAvsOverride> getPartyIcsAvsOverrides()	{
		return partyIcsAvsOverrides;
	}

	public void setPartyIcsAvsOverrides(List<PartyIcsAvsOverride> partyIcsAvsOverrides)	{
		this.partyIcsAvsOverrides = partyIcsAvsOverrides;
	}
}
