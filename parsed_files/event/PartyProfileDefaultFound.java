package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyProfileDefaultFound implements Event{

	private List<PartyProfileDefault> partyProfileDefaults;

	public PartyProfileDefaultFound(List<PartyProfileDefault> partyProfileDefaults) {
		this.setPartyProfileDefaults(partyProfileDefaults);
	}

	public List<PartyProfileDefault> getPartyProfileDefaults()	{
		return partyProfileDefaults;
	}

	public void setPartyProfileDefaults(List<PartyProfileDefault> partyProfileDefaults)	{
		this.partyProfileDefaults = partyProfileDefaults;
	}
}
